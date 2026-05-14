package com.knoxhack.echorelictech.block.entity;

import com.knoxhack.echorelictech.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RelicAnalyzerBlockEntity extends BlockEntity {
    public RelicAnalyzerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RELIC_ANALYZER.get(), pos, state);
    }
}
