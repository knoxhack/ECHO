package com.knoxhack.echoterminal.service;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.TerminalPlacementService;
import com.knoxhack.echocore.api.TerminalRewardService;
import com.knoxhack.echoterminal.block.entity.EchoTerminalBlockEntity;
import com.knoxhack.echoterminal.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class EchoTerminalCoreServices {
    private static final int TERMINAL_SEARCH_RADIUS = 48;

    private EchoTerminalCoreServices() {
    }

    public static void register() {
        EchoCoreServices.registerTerminalPlacementService(new TerminalPlacementService() {
            @Override
            public boolean placeTerminal(Level level, BlockPos pos, Player owner) {
                if (level == null || pos == null || level.isClientSide()) {
                    return false;
                }
                level.setBlockAndUpdate(pos, ModBlocks.ECHO_TERMINAL_BLOCK.get().defaultBlockState());
                if (level.getBlockEntity(pos) instanceof EchoTerminalBlockEntity terminal && owner != null) {
                    terminal.setOwnerIfMissing(owner);
                }
                return true;
            }

            @Override
            public BlockState structureBlockState() {
                return ModBlocks.ECHO_TERMINAL_BLOCK.get().defaultBlockState();
            }

            @Override
            public boolean isTerminalBlock(BlockState state) {
                return state != null && state.is(ModBlocks.ECHO_TERMINAL_BLOCK.get());
            }
        });

        EchoCoreServices.registerTerminalRewardService(new TerminalRewardService() {
            @Override
            public boolean storeRewards(ServerPlayer player, String missionId, List<ItemStack> rewards) {
                EchoTerminalBlockEntity terminal = findOwnedTerminal(player);
                if (terminal == null) {
                    return false;
                }
                boolean stored = terminal.storeRewards(missionId, rewards);
                if (stored) {
                    player.sendSystemMessage(Component.literal("[ECHO-7] Rewards stored in terminal."), true);
                }
                return stored;
            }

            @Override
            public boolean claimRewards(ServerPlayer player) {
                EchoTerminalBlockEntity terminal = findOwnedTerminal(player);
                return terminal != null && terminal.claimAllRewards(player);
            }
        });
    }

    private static EchoTerminalBlockEntity findOwnedTerminal(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        BlockPos center = player.blockPosition();
        for (int dy = -16; dy <= 16; dy++) {
            for (int dx = -TERMINAL_SEARCH_RADIUS; dx <= TERMINAL_SEARCH_RADIUS; dx++) {
                for (int dz = -TERMINAL_SEARCH_RADIUS; dz <= TERMINAL_SEARCH_RADIUS; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (player.level().getBlockEntity(pos) instanceof EchoTerminalBlockEntity terminal
                            && terminal.isOwner(player)) {
                        return terminal;
                    }
                }
            }
        }
        return null;
    }
}
