package com.knoxhack.echodatacore.legacy;

import com.knoxhack.echocore.api.DataValueKind;
import com.knoxhack.echocore.api.IDataKey;
import com.knoxhack.echodatacore.EchoDataCore;
import com.mojang.serialization.Codec;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class DataCoreLegacyAdapters {
    private static final Map<String, String> ROOTS_BY_NAMESPACE = Map.ofEntries(
            Map.entry("echocore", "echocore_progress_ledger"),
            Map.entry("echoorbitalremnants", "echoorbitalremnants_progress"),
            Map.entry("echoconvoyprotocol", "echoconvoyprotocol"),
            Map.entry("echoagriculturereclamation", "echoagriculturereclamation_progress"),
            Map.entry("echoindustrialnexus", "echoindustrialnexus_progress"),
            Map.entry("echostationfall", "echostationfall_progress"),
            Map.entry("echoblackboxprotocol", "echoblackboxprotocol_progress"),
            Map.entry("signalos", "signalos")
    );

    private DataCoreLegacyAdapters() {
    }

    public static Optional<CompoundTag> read(Player player, IDataKey<?> key) {
        if (player == null || key == null) {
            return Optional.empty();
        }
        String rootName = rootName(key);
        if (rootName.isBlank()) {
            return Optional.empty();
        }
        CompoundTag root = player.getPersistentData().getCompoundOrEmpty(rootName);
        if (root.isEmpty()) {
            return Optional.empty();
        }
        String field = fieldName(key);
        if (field.isBlank() || !root.contains(field)) {
            return Optional.empty();
        }
        return legacyEntry(key, root, field);
    }

    public static Map<Identifier, String> snapshot(Player player) {
        Map<Identifier, String> snapshot = new LinkedHashMap<>();
        if (player == null) {
            return snapshot;
        }
        for (Map.Entry<String, String> rootEntry : ROOTS_BY_NAMESPACE.entrySet()) {
            CompoundTag root = player.getPersistentData().getCompoundOrEmpty(rootEntry.getValue());
            if (root.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Tag> value : root.entrySet()) {
                Identifier id = Identifier.tryParse(rootEntry.getKey() + ":legacy/" + rootEntry.getValue() + "/" + cleanPath(value.getKey()));
                if (id != null) {
                    snapshot.put(id, value.getValue().toString());
                }
            }
        }
        addAttachmentSnapshot(snapshot, player, "echoashfallprotocol", "quest_data",
                "com.knoxhack.echoashfallprotocol.echo.QuestData",
                Map.of(
                        "completed_missions", "getCompletedMissionIds",
                        "unlocked_missions", "getUnlockedMissionIds",
                        "discovered_pois", "getDiscoveredPOIs",
                        "collected_power_nodes", "getCollectedPowerNodes"));
        addAttachmentSnapshot(snapshot, player, "echoterminal", "terminal_player_data",
                "com.knoxhack.echoterminal.player.TerminalPlayerData",
                Map.of(
                        "read_archives", "readArchiveIds",
                        "tracked_mission", "trackedMission"));
        addAttachmentSnapshot(snapshot, player, "echonexusprotocol", "nexus_player_data",
                "com.knoxhack.echonexusprotocol.data.NexusPlayerData",
                Map.of(
                        "research_unlocks", "researchUnlocks",
                        "scanned_ids", "scannedIds",
                        "blackbox_fragments", "blackboxFragments",
                        "ending_path", "endingPath",
                        "final_choice_state", "finalChoiceState"));
        return snapshot;
    }

    private static String rootName(IDataKey<?> key) {
        String path = key.id().getPath();
        if (path.startsWith("legacy/")) {
            String[] parts = path.split("/", 3);
            return parts.length >= 2 ? parts[1] : "";
        }
        if ("echocore".equals(key.id().getNamespace()) && path.startsWith("profile/")) {
            return "echocore_profile";
        }
        if ("echocore".equals(key.id().getNamespace()) && path.startsWith("faction/")) {
            return "echocore_factions";
        }
        return ROOTS_BY_NAMESPACE.getOrDefault(key.id().getNamespace(), "");
    }

    private static String fieldName(IDataKey<?> key) {
        String path = key.id().getPath();
        if (path.startsWith("legacy/")) {
            String[] parts = path.split("/", 3);
            return parts.length >= 3 ? parts[2] : "";
        }
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static Optional<CompoundTag> legacyEntry(IDataKey<?> key, CompoundTag root, String field) {
        Object value = switch (key.kind()) {
            case FLAG -> root.getBooleanOr(field, false);
            case COUNTER -> root.getLongOr(field, root.getIntOr(field, 0));
            case STRING, ENUM -> root.getStringOr(field, "");
            case RECORD -> root.getCompoundOrEmpty(field);
        };
        CompoundTag entry = new CompoundTag();
        entry.putString("kind", key.kind().name());
        try {
            store(entry, key, value);
            return Optional.of(entry);
        } catch (RuntimeException exception) {
            EchoDataCore.LOGGER.debug("Legacy DataCore adapter could not read {} from field {}.", key.id(), field, exception);
            return Optional.empty();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void store(CompoundTag entry, IDataKey key, Object value) {
        Codec codec = key.codec();
        entry.store("value", codec, NbtOps.INSTANCE, value);
    }

    private static void addAttachmentSnapshot(
            Map<Identifier, String> snapshot,
            Player player,
            String namespace,
            String attachmentName,
            String className,
            Map<String, String> methods) {
        try {
            Class<?> type = Class.forName(className);
            Method get = type.getMethod("get", Player.class);
            Object data = get.invoke(null, player);
            if (data == null) {
                return;
            }
            for (Map.Entry<String, String> methodEntry : methods.entrySet()) {
                Method method = type.getMethod(methodEntry.getValue());
                Object value = method.invoke(data);
                Identifier id = Identifier.tryParse(namespace + ":legacy/" + attachmentName + "/" + methodEntry.getKey());
                if (id != null) {
                    snapshot.put(id, summarize(value));
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Optional attachment owner is not loaded.
        }
    }

    private static String summarize(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            return "count=" + collection.size() + " " + collection.stream().limit(12).toList();
        }
        return value.toString();
    }

    private static String cleanPath(String value) {
        return value == null ? "unknown" : value.replace(':', '_').replace('|', '_').replace(' ', '_');
    }
}
