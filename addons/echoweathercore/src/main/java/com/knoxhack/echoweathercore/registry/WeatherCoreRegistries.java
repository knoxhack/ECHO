package com.knoxhack.echoweathercore.registry;

import net.neoforged.bus.api.IEventBus;

public final class WeatherCoreRegistries {
    private WeatherCoreRegistries() {}

    public static void register(IEventBus eventBus) {
        WeatherCoreItems.register(eventBus);
        WeatherCoreBlocks.register(eventBus);
        WeatherCoreBlockEntities.register(eventBus);
        WeatherCoreMenus.register(eventBus);
    }
}
