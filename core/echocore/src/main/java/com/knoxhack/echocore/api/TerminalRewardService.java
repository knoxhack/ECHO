package com.knoxhack.echocore.api;

import java.util.List;
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
    };

    boolean storeRewards(ServerPlayer player, String missionId, List<ItemStack> rewards);

    boolean claimRewards(ServerPlayer player);
}
