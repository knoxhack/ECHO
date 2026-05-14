package com.knoxhack.echoweathercore.registry;

import com.knoxhack.echoweathercore.EchoWeatherCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WeatherCoreMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, EchoWeatherCore.MODID);

    private WeatherCoreMenus() {}

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
