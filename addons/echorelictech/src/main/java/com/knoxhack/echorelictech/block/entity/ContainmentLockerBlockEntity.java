package com.knoxhack.echorelictech.block.entity;

import com.knoxhack.echorelictech.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ContainmentLockerBlockEntity extends BlockEntity {
    public ContainmentLockerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTAINMENT_LOCKER.get(), pos, state);
    }
}
