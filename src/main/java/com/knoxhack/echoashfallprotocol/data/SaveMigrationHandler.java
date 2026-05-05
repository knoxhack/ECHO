package com.knoxhack.echoashfallprotocol.data;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.faction.FactionQuestData;
import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Handles save migration for ECHO exploration and faction progress.
 * 
 * Migration rules:
 * - Existing saves: Neutral reputation (0) for all factions
 * - Existing saves: 0 research points, no perks unlocked
 * - Existing recipes: Remain unlocked (no gating retroactive)
 * - New Tier 2+ recipes: Require schematic fragments
 */
public class SaveMigrationHandler {

    // Migration version - increment when data format changes
    public static final int CURRENT_MIGRATION_VERSION = 2;

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

        // Reset the retired three-faction player-facing reputation model now that Echo Core owns faction standing.
        ReputationData reputation = ReputationData.get(player);
        if (fromVersion < 2) {
            reputation.resetLegacyProgress();
            ReputationData.saveAndSync(player, reputation);
            FactionQuestData factionQuestData = FactionQuestData.get(player);
            factionQuestData.clearLegacyFactionProgress();
            FactionQuestData.saveAndSync(player, factionQuestData);
            EchoAshfallProtocol.LOGGER.debug("Reset legacy Ashfall faction reputation and quests for {}",
                    player.getName().getString());
        }

        // Initialize research data for existing players
        ResearchData research = ResearchData.get(player);
        if (research.getPoints() == 0 && research.getUnlockedPerks().isEmpty()) {
            EchoAshfallProtocol.LOGGER.debug("Initialized research data for {}", 
                player.getName().getString());
        }

        // Send notification to player about new content
        player.sendSystemMessage(Component.translatable(
            "message.EchoAshfallProtocol.migration_complete",
            "v1.2.0 Nexus + Orbital Endgame"
        ));

        EchoAshfallProtocol.LOGGER.info("Save migration completed for player {}", 
            player.getName().getString());
    }
}
