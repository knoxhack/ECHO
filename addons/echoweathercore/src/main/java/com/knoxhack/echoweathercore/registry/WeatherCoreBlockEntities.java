package com.knoxhack.echoweathercore.registry;

import com.knoxhack.echoweathercore.EchoWeatherCore;
import com.knoxhack.echoweathercore.blockentity.WeatherStationBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WeatherCoreBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoWeatherCore.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WeatherStationBlockEntity>> WEATHER_STATION = BLOCK_ENTITIES.register("weather_station",
        () -> new BlockEntityType<>(WeatherStationBlockEntity::new, Set.of((Block) WeatherCoreBlocks.WEATHER_STATION.get())));

    private WeatherCoreBlockEntities() {}

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
