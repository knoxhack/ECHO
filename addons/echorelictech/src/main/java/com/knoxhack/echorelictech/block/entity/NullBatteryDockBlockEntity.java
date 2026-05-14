package com.knoxhack.echorelictech.block.entity;

import com.knoxhack.echorelictech.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NullBatteryDockBlockEntity extends BlockEntity {
    public NullBatteryDockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NULL_BATTERY_DOCK.get(), pos, state);
    }
}
