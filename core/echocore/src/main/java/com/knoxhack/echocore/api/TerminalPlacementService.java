package com.knoxhack.echocore.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Optional terminal provider. The fallback is intentionally inert so gameplay mods can run without Terminal.
 */
public interface TerminalPlacementService {
    TerminalPlacementService NOOP = new TerminalPlacementService() {
        @Override
        public boolean placeTerminal(Level level, BlockPos pos, Player owner) {
            return false;
        }
    };

    boolean placeTerminal(Level level, BlockPos pos, Player owner);

    default BlockState structureBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    default boolean isTerminalBlock(BlockState state) {
        return false;
    }
}
