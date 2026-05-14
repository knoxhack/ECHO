package com.knoxhack.signalos.block;

import com.knoxhack.signalos.block.entity.SignalOsTerminalBlockEntity;
import com.knoxhack.signalos.integration.SignalOsMissionHooks;
import com.knoxhack.signalos.service.SignalOsTerminalServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SignalOsTerminalBlock extends Block implements EntityBlock {
    public SignalOsTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SignalOsTerminalBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            SignalOsTerminalServices.openBlockTerminal(serverPlayer, level, pos);
            SignalOsMissionHooks.recordBootTerminal(serverPlayer, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        }
        return InteractionResult.SUCCESS;
    }
}
