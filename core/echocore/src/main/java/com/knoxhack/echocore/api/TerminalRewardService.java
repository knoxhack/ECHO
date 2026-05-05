package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Optional terminal storage service for mission rewards.
 */
public interface TerminalRewardService {
    TerminalRewardService NOOP = new TerminalRewardService() {
        @Override
        public boolean storeRewards(ServerPlayer player, String missionId, List<ItemStack> rewards) {
            return false;
        }

        @Override
        public boolean claimRewards(ServerPlayer player) {
            return false;
        }

        @Override
        public int pendingRewardCount(Player player) {
            return 0;
        }
    };

    boolean storeRewards(ServerPlayer player, String missionId, List<ItemStack> rewards);

    boolean claimRewards(ServerPlayer player);

    default int pendingRewardCount(Player player) {
        return 0;
    }
}
