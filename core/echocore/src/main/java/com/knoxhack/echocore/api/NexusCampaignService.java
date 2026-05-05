package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.world.entity.player.Player;

/**
 * Optional service exposed by mods that own a longer Nexus campaign layer.
 * This complements NexusPathService without changing the older permanent path contract.
 */
public interface NexusCampaignService {
    NexusCampaignService NOOP = new NexusCampaignService() {
    };

    default String pathId(Player player) {
        return "";
    }

    default int instability(Player player) {
        return 0;
    }

    default boolean isWarfrontComplete(Player player) {
        return false;
    }

    default boolean isFinalProtocolComplete(Player player) {
        return false;
    }

    default List<String> relaySummary(Player player) {
        return List.of();
    }

    default boolean isFinalBossDefeated(Player player) {
        return false;
    }

    default String statusLine(Player player) {
        return "No Nexus campaign signal is available from the active ECHO stack.";
    }
}
