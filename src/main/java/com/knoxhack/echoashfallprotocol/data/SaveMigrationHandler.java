package com.knoxhack.echoashfallprotocol.data;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoFactionDataService;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Handles save migration for ECHO exploration progress.
 * 
 * Migration rules:
 * - Existing saves: Neutral reputation (0) for all factions
 * - Existing saves: 0 research points, no perks unlocked
 * - Existing recipes: Remain unlocked (no gating retroactive)
 * - New Tier 2+ recipes: Require schematic fragments
 */
public class SaveMigrationHandler {

    // Migration version - increment when data format changes
    public static final int CURRENT_MIGRATION_VERSION = 3;
    private static final String CONTACTED_KEY = "contacted";
    private static final String REPUTATION_KEY = "reputation";
    private static final String COMPLETED_KEY = "completed_contracts";
    private static final String COOLDOWN_KEY = "cooldown_until";
    private static final String MEMORY_KEY = "npc_memory";
    private static final String CONTACT_COUNT_KEY = "contact_count";
    private static final String LAST_INTERACTION_KEY = "last_interaction_tick";
    private static final String LAST_ROLE_KEY = "last_role_id";
    private static final Map<String, String> FACTION_ALIASES = Map.ofEntries(
            Map.entry("echoashfallprotocol:survivor_network", "echoashfallprotocol:radwarden_compact"),
            Map.entry("echoashfallprotocol:ashland_rangers", "echoashfallprotocol:radwarden_compact"),
            Map.entry("echoashfallprotocol:thawbound_collective", "echoashfallprotocol:radwarden_compact"),
            Map.entry("echoashfallprotocol:remnant_collective", "echoashfallprotocol:radwarden_compact"),
            Map.entry("echoashfallprotocol:dustline_freeholds", "echoashfallprotocol:crashbreak_salvage"),
            Map.entry("echoashfallprotocol:metro_archivists", "echoashfallprotocol:crashbreak_salvage"),
            Map.entry("echoashfallprotocol:rustworks_union", "echoashfallprotocol:crashbreak_salvage"),
            Map.entry("echoashfallprotocol:salvager_guild", "echoashfallprotocol:crashbreak_salvage"),
            Map.entry("echocore:survivors", "echoashfallprotocol:crashbreak_salvage"),
            Map.entry("echoashfallprotocol:scarbound_conclave", "echoashfallprotocol:sporebound_sanctum"),
            Map.entry("echoashfallprotocol:mutant_front", "echoashfallprotocol:sporebound_sanctum"),
            Map.entry("echoorbitalremnants:orbital_remnants", "echoashfallprotocol:radwarden_compact"),
            Map.entry("echoorbitalremnants:void_salvagers", "echoashfallprotocol:crashbreak_salvage"),
            Map.entry("echoorbitalremnants:nexus_choir", "echoashfallprotocol:sporebound_sanctum"),
            Map.entry("echoarmory:remnant_collective", "echoashfallprotocol:radwarden_compact"),
            Map.entry("echoarmory:salvager_guild", "echoashfallprotocol:crashbreak_salvage"),
            Map.entry("echoarmory:construct_foundry", "echoashfallprotocol:crashbreak_salvage"));

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Get migration data
        MigrationData migration = player.getData(ModAttachments.MIGRATION_DATA);
        
        // Check if migration is needed
        if (migration.getVersion() < CURRENT_MIGRATION_VERSION) {
            performMigration(player, migration.getVersion());
            migration.setVersion(CURRENT_MIGRATION_VERSION);
        }
    }

    private static void performMigration(ServerPlayer player, int fromVersion) {
        EchoAshfallProtocol.LOGGER.info("Performing save migration for player {} from version {}", 
            player.getName().getString(), fromVersion);

        // Initialize research data for existing players
        ResearchData research = ResearchData.get(player);
        if (research.getPoints() == 0 && research.getUnlockedPerks().isEmpty()) {
            EchoAshfallProtocol.LOGGER.debug("Initialized research data for {}", 
                player.getName().getString());
        }

        if (fromVersion < 3) {
            migrateFactionAliases(player);
        }

        // Send notification to player about new content
        player.sendSystemMessage(Component.translatable(
            "message.EchoAshfallProtocol.migration_complete",
            "v1.3.1 three-faction alignment"
        ));

        EchoAshfallProtocol.LOGGER.info("Save migration completed for player {}", 
            player.getName().getString());
    }

    private static void migrateFactionAliases(ServerPlayer player) {
        CompoundTag root = EchoFactionDataService.exportRoot(player);
        boolean changed = false;
        for (Map.Entry<String, String> alias : FACTION_ALIASES.entrySet()) {
            if (!root.contains(alias.getKey())) {
                continue;
            }
            CompoundTag source = root.getCompoundOrEmpty(alias.getKey()).copy();
            CompoundTag target = root.getCompoundOrEmpty(alias.getValue()).copy();
            mergeFactionTag(target, source);
            root.put(alias.getValue(), target);
            root.remove(alias.getKey());
            changed = true;
        }
        if (changed) {
            EchoFactionDataService.importRoot(player, root);
            EchoCoreServices.syncFactionDataToClient(player);
        }
    }

    private static void mergeFactionTag(CompoundTag target, CompoundTag source) {
        target.putBoolean(CONTACTED_KEY,
                target.getBooleanOr(CONTACTED_KEY, false) || source.getBooleanOr(CONTACTED_KEY, false));
        target.putInt(REPUTATION_KEY,
                Math.max(target.getIntOr(REPUTATION_KEY, 0), source.getIntOr(REPUTATION_KEY, 0)));
        target.putString(COMPLETED_KEY,
                mergeTokens(target.getStringOr(COMPLETED_KEY, ""), source.getStringOr(COMPLETED_KEY, "")));
        target.putLong(COOLDOWN_KEY,
                Math.max(target.getLongOr(COOLDOWN_KEY, 0L), source.getLongOr(COOLDOWN_KEY, 0L)));
        target.putString(MEMORY_KEY,
                mergeMemory(target.getStringOr(MEMORY_KEY, ""), source.getStringOr(MEMORY_KEY, "")));
        target.putInt(CONTACT_COUNT_KEY,
                target.getIntOr(CONTACT_COUNT_KEY, 0) + source.getIntOr(CONTACT_COUNT_KEY, 0));
        if (source.getLongOr(LAST_INTERACTION_KEY, 0L) >= target.getLongOr(LAST_INTERACTION_KEY, 0L)) {
            target.putLong(LAST_INTERACTION_KEY, source.getLongOr(LAST_INTERACTION_KEY, 0L));
            target.putString(LAST_ROLE_KEY, source.getStringOr(LAST_ROLE_KEY, ""));
        }
    }

    private static String mergeTokens(String left, String right) {
        Set<String> tokens = new LinkedHashSet<>();
        addTokens(tokens, left);
        addTokens(tokens, right);
        return String.join("|", tokens);
    }

    private static void addTokens(Set<String> tokens, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (String token : value.split("\\|")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
    }

    private static String mergeMemory(String left, String right) {
        if (right == null || right.isBlank()) {
            return left == null ? "" : left;
        }
        if (left == null || left.isBlank()) {
            return right;
        }
        return left.contains(right) ? left : left + " | " + right;
    }
}
