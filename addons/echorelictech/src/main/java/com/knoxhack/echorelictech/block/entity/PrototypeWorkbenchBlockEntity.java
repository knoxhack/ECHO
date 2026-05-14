package com.knoxhack.echorelictech.block.entity;

import com.knoxhack.echorelictech.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PrototypeWorkbenchBlockEntity extends BlockEntity {
    public PrototypeWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROTOTYPE_WORKBENCH.get(), pos, state);
    }
}
