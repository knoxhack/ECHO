package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import net.minecraft.server.level.ServerPlayer;

/**
 * Applies early faction rewards so reputation matters before the late game.
 */
public final class FactionProgressionHelper {

    private FactionProgressionHelper() {
    }

    public static void syncMilestones(ServerPlayer player) {
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            syncMilestones(player, faction);
        }
    }

    public static void syncMilestones(ServerPlayer player, ReputationData.Faction faction) {
        QuestData quest = QuestData.get(player);
        ResearchData research = ResearchData.get(player);
        int rep = AshfallFactionBridge.reputation(player, faction);
        String prefix = "faction_" + faction.name().toLowerCase();

        if (rep >= 25 && !quest.isAssetDiscovered(prefix + "_safehouse")) {
            quest.discoverAsset(prefix + "_safehouse");

            String schematic = getFactionSchematic(faction);
            if (!research.hasSchematic(schematic)) {
                research.unlockSchematic(schematic);
                ResearchData.saveAndSync(player, research);
            }

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6[ECHO-7]§r " + faction.getDisplayName() + " allied status reached. " +
                "Trade discount increased and " + schematic + " schematics archived."
            ));
        }

        if (rep >= 50 && !quest.isAssetDiscovered(prefix + "_priority")) {
            quest.discoverAsset(prefix + "_priority");
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§b[ECHO-7]§r " + faction.getDisplayName() + " priority status granted. Supply lanes opened."
            ));
        }

        if (rep >= 75 && !quest.isAssetDiscovered(prefix + "_elite")) {
            quest.discoverAsset(prefix + "_elite");
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§d[ECHO-7]§r Elite standing with " + faction.getDisplayName() +
                " confirmed. High-value schematics and contracts unlocked."
            ));
        }

        if (rep >= 100 && !quest.isAssetDiscovered(prefix + "_command")) {
            quest.discoverAsset(prefix + "_command");
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§a[ECHO-7]§r " + faction.getDisplayName() +
                " command-tier support active. Maximum field discount online."
            ));
        }

        QuestData.saveAndSync(player, quest);
    }

    private static String getFactionSchematic(ReputationData.Faction faction) {
        return switch (faction) {
            case REMNANTS -> "weapons";
            case SALVAGERS -> "machines";
            case MUTANTS -> "medical";
        };
    }
}
