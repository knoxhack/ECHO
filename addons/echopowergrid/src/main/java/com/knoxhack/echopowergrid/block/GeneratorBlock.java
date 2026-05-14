package com.knoxhack.echopowergrid.block;

import com.knoxhack.echopowergrid.block.entity.GeneratorBlockEntity;
import com.knoxhack.echopowergrid.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class GeneratorBlock extends Block implements EntityBlock {
    private final long generationRate;
    private final long bufferSize;
    private final boolean usesFuel;

    public GeneratorBlock(long generationRate, long bufferSize, boolean usesFuel, Properties properties) {
        super(properties);
        this.generationRate = generationRate;
        this.bufferSize = bufferSize;
        this.usesFuel = usesFuel;
    }

    public long getGenerationRate() { return generationRate; }
    public long getBufferSize() { return bufferSize; }
    public boolean usesFuel() { return usesFuel; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ModBlockEntities.GENERATOR.get() ? (l, p, s, be) -> GeneratorBlockEntity.tick(l, p, s, (GeneratorBlockEntity) be) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof GeneratorBlockEntity gen) {
            gen.onUse(player, stack);
        }
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
