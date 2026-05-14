package com.knoxhack.echopowergrid.registry;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.block.entity.GeneratorBlockEntity;
import com.knoxhack.echopowergrid.block.entity.BatteryBlockEntity;
import com.knoxhack.echopowergrid.block.entity.PowerConsumerBlockEntity;
import com.knoxhack.echopowergrid.block.entity.SubstationBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoPowerGrid.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR =
        BLOCK_ENTITIES.register("generator", () -> new BlockEntityType<>(GeneratorBlockEntity::new,
            Set.of((Block) ModBlocks.HAND_CRANK_GENERATOR.get(), (Block) ModBlocks.SCRAP_BURNER_GENERATOR.get(),
                   (Block) ModBlocks.SOLAR_PANEL.get(), (Block) ModBlocks.CREATIVE_POWER_SOURCE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>> BATTERY =
        BLOCK_ENTITIES.register("battery", () -> new BlockEntityType<>(BatteryBlockEntity::new,
            Set.of((Block) ModBlocks.SMALL_BATTERY_BANK.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerConsumerBlockEntity>> CONSUMER =
        BLOCK_ENTITIES.register("consumer", () -> new BlockEntityType<>(PowerConsumerBlockEntity::new,
            Set.of((Block) ModBlocks.CREATIVE_POWER_SINK.get(), (Block) ModBlocks.TEST_POWER_CONSUMER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SubstationBlockEntity>> SUBSTATION =
        BLOCK_ENTITIES.register("substation", () -> new BlockEntityType<>(SubstationBlockEntity::new,
            Set.of((Block) ModBlocks.OUTPOST_SUBSTATION.get())));

    private ModBlockEntities() {}

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
