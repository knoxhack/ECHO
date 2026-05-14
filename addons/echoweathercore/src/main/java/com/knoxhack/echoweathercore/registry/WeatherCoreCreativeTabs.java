package com.knoxhack.echoweathercore.registry;

import com.knoxhack.echoweathercore.EchoWeatherCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WeatherCoreCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoWeatherCore.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEATHERCORE_TAB = TABS.register(
        "weathercore",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.echoweathercore.weathercore"))
            .withTabsBefore(new ResourceKey[]{CreativeModeTabs.FUNCTIONAL_BLOCKS})
            .icon(() -> ((Item) WeatherCoreItems.STORM_SCANNER.get()).getDefaultInstance())
            .displayItems((parameters, output) -> WeatherCoreItems.creativeItems().forEach(output::accept))
            .build()
    );

    private WeatherCoreCreativeTabs() {}

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
