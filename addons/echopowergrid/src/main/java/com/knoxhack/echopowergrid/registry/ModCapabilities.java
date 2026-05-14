package com.knoxhack.echopowergrid.registry;

import com.knoxhack.echopowergrid.block.entity.BatteryBlockEntity;
import com.knoxhack.echopowergrid.block.entity.GeneratorBlockEntity;
import com.knoxhack.echopowergrid.block.entity.PowerConsumerBlockEntity;
import com.knoxhack.echopowergrid.capability.EpEnergyHandler;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {}

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, (BlockEntityType<GeneratorBlockEntity>) ModBlockEntities.GENERATOR.get(),
            (be, side) -> new EpEnergyHandler(be, () -> {}));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, (BlockEntityType<BatteryBlockEntity>) ModBlockEntities.BATTERY.get(),
            (be, side) -> new EpEnergyHandler(be, () -> {}));
    }
}
