package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Temporary bridge from retired Ashfall legacy reputation buckets into Echo Core faction IDs.
 */
public final class AshfallFactionBridge {
    private AshfallFactionBridge() {
    }

    public static Identifier coreFactionId(ReputationData.Faction faction) {
        return switch (faction) {
            case REMNANTS -> AshfallBiomeFactions.RADWARDEN_COMPACT;
            case SALVAGERS -> AshfallBiomeFactions.CRASHBREAK_SALVAGE;
            case MUTANTS -> AshfallBiomeFactions.SPOREBOUND_SANCTUM;
        };
    }

    public static int reputation(Player player, ReputationData.Faction faction) {
        if (player == null || faction == null) {
            return 0;
        }
        Identifier id = coreFactionId(faction);
        Optional<Integer> core = EchoCoreServices.factionProfile(player, id)
                .map(profile -> profile.reputation());
        if (core.isPresent()) {
            return core.get();
        }
        return 0;
    }

    public static boolean isHostile(Player player, ReputationData.Faction faction, int threshold) {
        return reputation(player, faction) < threshold;
    }

    public static void addReputation(Player player, ReputationData.Faction faction, int delta) {
        if (player != null && faction != null && delta != 0) {
            EchoCoreServices.addFactionReputation(player, coreFactionId(faction), delta);
        }
    }

    public static void markLegacyContact(Player player, ReputationData.Faction faction) {
        if (player != null && faction != null) {
            EchoCoreServices.markFactionContacted(player, coreFactionId(faction));
        }
    }

    public static String retirementLine(ReputationData.Faction faction) {
        Identifier id = coreFactionId(faction);
        return "Legacy " + faction.getDisplayName() + " checks are bridged to "
                + EchoAshfallProtocol.MODID + ":" + id.getPath() + ".";
    }
}
