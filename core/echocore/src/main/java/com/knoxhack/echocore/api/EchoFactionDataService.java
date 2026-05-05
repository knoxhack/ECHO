package com.knoxhack.echocore.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Shared per-player faction persistence under the echocore NBT root.
 */
public final class EchoFactionDataService {
    private static final String ROOT_KEY = "echocore_factions";
    private static final String CONTACTED_KEY = "contacted";
    private static final String REPUTATION_KEY = "reputation";
    private static final String COMPLETED_KEY = "completed_contracts";
    private static final String ACTIVE_KEY = "active_contract";
    private static final String COOLDOWN_KEY = "cooldown_until";
    private static final String MEMORY_KEY = "npc_memory";
    private static final String CONTACT_COUNT_KEY = "contact_count";
    private static final String LAST_INTERACTION_KEY = "last_interaction_tick";
    private static final String LAST_ROLE_KEY = "last_role_id";

    private EchoFactionDataService() {
    }

    public static List<EchoFactionProfile> profiles(Player player) {
        return EchoFactionRegistry.definitions().stream()
                .map(definition -> profile(player, definition))
                .toList();
    }

    public static Optional<EchoFactionProfile> profile(Player player, Identifier factionId) {
        return EchoFactionRegistry.definition(factionId)
                .map(definition -> profile(player, definition));
    }

    public static EchoFactionProfile profile(Player player, EchoFactionDefinition definition) {
        CompoundTag data = factionTag(player, definition.id(), false);
        int reputation = data.getIntOr(REPUTATION_KEY, 0);
        boolean contacted = data.getBooleanOr(CONTACTED_KEY, false) || reputation != 0;
        Optional<Identifier> activeContract = parseOptionalIdentifier(data.getStringOr(ACTIVE_KEY, ""));
        return new EchoFactionProfile(
                definition,
                reputation,
                EchoFactionStanding.fromReputation(reputation, contacted),
                contacted,
                countTokens(data.getStringOr(COMPLETED_KEY, "")),
                parseIdentifierList(data.getStringOr(COMPLETED_KEY, "")),
                activeContract,
                data.getLongOr(COOLDOWN_KEY, 0L),
                data.getStringOr(MEMORY_KEY, ""),
                data.getIntOr(CONTACT_COUNT_KEY, contacted ? 1 : 0),
                data.getLongOr(LAST_INTERACTION_KEY, 0L),
                data.getStringOr(LAST_ROLE_KEY, ""));
    }

    public static void markContacted(Player player, Identifier factionId) {
        mutate(player, factionId, data -> data.putBoolean(CONTACTED_KEY, true));
    }

    public static void recordInteraction(Player player, Identifier factionId, String roleId, long gameTime) {
        mutate(player, factionId, data -> {
            data.putBoolean(CONTACTED_KEY, true);
            data.putInt(CONTACT_COUNT_KEY, Math.max(1, data.getIntOr(CONTACT_COUNT_KEY, 0) + 1));
            data.putLong(LAST_INTERACTION_KEY, Math.max(0L, gameTime));
            data.putString(LAST_ROLE_KEY, roleId == null ? "" : roleId.trim());
        });
    }

    public static void setReputation(Player player, Identifier factionId, int reputation) {
        mutate(player, factionId, data -> {
            data.putInt(REPUTATION_KEY, clampReputation(reputation));
            if (reputation != 0) {
                data.putBoolean(CONTACTED_KEY, true);
            }
        });
    }

    public static void addReputation(Player player, Identifier factionId, int delta) {
        mutate(player, factionId, data -> {
            int next = clampReputation(data.getIntOr(REPUTATION_KEY, 0) + delta);
            data.putInt(REPUTATION_KEY, next);
            if (delta != 0) {
                data.putBoolean(CONTACTED_KEY, true);
            }
        });
    }

    public static boolean acceptContract(Player player, Identifier factionId, Identifier contractId) {
        Optional<EchoFactionDefinition> definition = EchoFactionRegistry.definition(factionId);
        if (definition.isEmpty()) {
            return false;
        }
        Optional<EchoFactionContract> contract = contract(definition.get(), contractId);
        if (contract.isEmpty()) {
            return false;
        }
        CompoundTag data = factionTag(player, factionId, true);
        int reputation = data.getIntOr(REPUTATION_KEY, 0);
        if (reputation < contract.get().requiredReputation()
                || !data.getStringOr(ACTIVE_KEY, "").isBlank()
                || hasToken(data.getStringOr(COMPLETED_KEY, ""), contractId.toString())) {
            persistFactionTag(player, factionId, data);
            return false;
        }
        data.putBoolean(CONTACTED_KEY, true);
        data.putString(ACTIVE_KEY, contractId.toString());
        persistFactionTag(player, factionId, data);
        return true;
    }

    public static boolean completeContract(Player player, Identifier factionId, Identifier contractId) {
        Optional<EchoFactionDefinition> definition = EchoFactionRegistry.definition(factionId);
        if (definition.isEmpty()) {
            return false;
        }
        Optional<EchoFactionContract> contract = contract(definition.get(), contractId);
        if (contract.isEmpty()) {
            return false;
        }
        CompoundTag data = factionTag(player, factionId, true);
        if (!data.getStringOr(ACTIVE_KEY, "").equals(contractId.toString())) {
            persistFactionTag(player, factionId, data);
            return false;
        }
        data.putString(ACTIVE_KEY, "");
        data.putString(COMPLETED_KEY, addToken(data.getStringOr(COMPLETED_KEY, ""), contractId.toString()));
        data.putInt(REPUTATION_KEY, clampReputation(data.getIntOr(REPUTATION_KEY, 0) + contract.get().reputationReward()));
        data.putBoolean(CONTACTED_KEY, true);
        persistFactionTag(player, factionId, data);
        return true;
    }

    public static void setCooldownUntil(Player player, Identifier factionId, long gameTime) {
        mutate(player, factionId, data -> data.putLong(COOLDOWN_KEY, Math.max(0L, gameTime)));
    }

    public static void rememberNpc(Player player, Identifier factionId, String memoryLine) {
        mutate(player, factionId, data -> data.putString(MEMORY_KEY, memoryLine == null ? "" : memoryLine.trim()));
    }

    public static void resetFaction(Player player, Identifier factionId) {
        CompoundTag root = rootTag(player, true);
        root.remove(factionId.toString());
        player.getPersistentData().put(ROOT_KEY, root);
    }

    public static CompoundTag exportRoot(Player player) {
        return rootTag(player, false).copy();
    }

    public static void importRoot(Player player, CompoundTag root) {
        if (player == null) {
            return;
        }
        player.getPersistentData().put(ROOT_KEY, root == null ? new CompoundTag() : root.copy());
    }

    private static Optional<EchoFactionContract> contract(EchoFactionDefinition definition, Identifier contractId) {
        return definition.contracts().stream()
                .filter(contract -> contract.id().equals(contractId))
                .findFirst();
    }

    private static void mutate(Player player, Identifier factionId, FactionMutation mutation) {
        if (player == null || factionId == null || mutation == null) {
            return;
        }
        CompoundTag data = factionTag(player, factionId, true);
        mutation.accept(data);
        persistFactionTag(player, factionId, data);
    }

    private static CompoundTag rootTag(Player player, boolean create) {
        if (player == null) {
            return new CompoundTag();
        }
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompoundOrEmpty(ROOT_KEY);
        if (create && !persistent.contains(ROOT_KEY)) {
            persistent.put(ROOT_KEY, root);
        }
        return root;
    }

    private static CompoundTag factionTag(Player player, Identifier factionId, boolean create) {
        if (player == null || factionId == null) {
            return new CompoundTag();
        }
        CompoundTag root = rootTag(player, create);
        CompoundTag faction = root.getCompoundOrEmpty(factionId.toString());
        if (create && !root.contains(factionId.toString())) {
            root.put(factionId.toString(), faction);
            player.getPersistentData().put(ROOT_KEY, root);
        }
        return faction;
    }

    private static void persistFactionTag(Player player, Identifier factionId, CompoundTag data) {
        if (player == null || factionId == null || data == null) {
            return;
        }
        CompoundTag root = rootTag(player, true);
        root.put(factionId.toString(), data);
        player.getPersistentData().put(ROOT_KEY, root);
    }

    private static int clampReputation(int reputation) {
        return Math.max(-100, Math.min(100, reputation));
    }

    private static Optional<Identifier> parseOptionalIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Identifier.parse(value));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static int countTokens(String stored) {
        if (stored == null || stored.isBlank()) {
            return 0;
        }
        int count = 0;
        for (String token : stored.split("\\|")) {
            if (!token.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private static List<Identifier> parseIdentifierList(String stored) {
        if (stored == null || stored.isBlank()) {
            return List.of();
        }
        List<Identifier> ids = new ArrayList<>();
        for (String token : stored.split("\\|")) {
            parseOptionalIdentifier(token).ifPresent(id -> {
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            });
        }
        return List.copyOf(ids);
    }

    private static boolean hasToken(String stored, String token) {
        return ("|" + (stored == null ? "" : stored) + "|").contains("|" + token + "|");
    }

    private static String addToken(String stored, String token) {
        String existing = stored == null ? "" : stored;
        if (hasToken(existing, token)) {
            return existing;
        }
        return existing.isBlank() ? token : existing + "|" + token;
    }

    @FunctionalInterface
    private interface FactionMutation {
        void accept(CompoundTag data);
    }
}
