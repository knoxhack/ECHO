package com.knoxhack.echopowergrid.block;

import com.knoxhack.echopowergrid.api.EchoGridState;
import com.knoxhack.echopowergrid.api.EchoPowerNetwork;
import com.knoxhack.echopowergrid.grid.PowerNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BreakerBlock extends Block {
    public static final BooleanProperty TRIPPED = BooleanProperty.create("tripped");

    public BreakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TRIPPED, false));
    }

    public boolean isTripped(BlockState state) {
        return state.getValue(TRIPPED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TRIPPED);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (state.getValue(TRIPPED)) {
            level.setBlock(pos, state.setValue(TRIPPED, false), 3);
            player.sendSystemMessage(Component.literal("ECHO GRID // Breaker reset. Circuit restored."));
            PowerNetworkManager.get(level).markDirty(pos);
        } else {
            player.sendSystemMessage(Component.literal("ECHO GRID // Breaker nominal. No action needed."));
        }
        return InteractionResult.SUCCESS;
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            PowerNetworkManager.get(level).onBlockPlaced(pos);
        }
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            PowerNetworkManager.get(level).onBlockRemoved(pos);
        }
    }

    public static void tryTrip(Level level, BlockPos pos, EchoPowerNetwork network) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BreakerBlock && !state.getValue(TRIPPED)) {
            level.setBlock(pos, state.setValue(TRIPPED, true), 3);
            network.state = EchoGridState.TRIPPED;
        }
    }
}
