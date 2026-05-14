package com.knoxhack.echopowergrid.block;

import com.knoxhack.echopowergrid.api.EchoPowerGridApi;
import com.knoxhack.echopowergrid.api.PowerGridSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MeterBlock extends Block {
    public MeterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        PowerGridSnapshot snap = EchoPowerGridApi.getSnapshot(level, pos);
        player.sendSystemMessage(Component.literal("ECHO GRID // Power Meter"));
        player.sendSystemMessage(Component.literal("  Gen: " + snap.totalGeneration() + " EP/t"));
        player.sendSystemMessage(Component.literal("  Demand: " + snap.totalDemand() + " EP/t"));
        player.sendSystemMessage(Component.literal("  Stored: " + snap.totalStored() + "/" + snap.totalCapacity()));
        player.sendSystemMessage(Component.literal("  Available: " + snap.availablePower() + " EP/t"));
        player.sendSystemMessage(Component.literal("  State: " + snap.state()));
        player.sendSystemMessage(Component.literal("  Nodes: " + snap.nodeCount()));
        return InteractionResult.SUCCESS;
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            com.knoxhack.echopowergrid.grid.PowerNetworkManager.get(level).onBlockPlaced(pos);
        }
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            com.knoxhack.echopowergrid.grid.PowerNetworkManager.get(level).onBlockRemoved(pos);
        }
    }
}
