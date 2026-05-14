package com.knoxhack.echorelictech.registry;

import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.block.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoRelicTech.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RelicAnalyzerBlockEntity>> RELIC_ANALYZER =
        BLOCK_ENTITIES.register("relic_analyzer", () -> new BlockEntityType<>(RelicAnalyzerBlockEntity::new,
            Set.of((Block) ModBlocks.RELIC_ANALYZER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrototypeWorkbenchBlockEntity>> PROTOTYPE_WORKBENCH =
        BLOCK_ENTITIES.register("prototype_workbench", () -> new BlockEntityType<>(PrototypeWorkbenchBlockEntity::new,
            Set.of((Block) ModBlocks.PROTOTYPE_WORKBENCH.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContainmentLockerBlockEntity>> CONTAINMENT_LOCKER =
        BLOCK_ENTITIES.register("containment_locker", () -> new BlockEntityType<>(ContainmentLockerBlockEntity::new,
            Set.of((Block) ModBlocks.CONTAINMENT_LOCKER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NullBatteryDockBlockEntity>> NULL_BATTERY_DOCK =
        BLOCK_ENTITIES.register("null_battery_dock", () -> new BlockEntityType<>(NullBatteryDockBlockEntity::new,
            Set.of((Block) ModBlocks.NULL_BATTERY_DOCK.get())));

    private ModBlockEntities() {}

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
