package com.knoxhack.signalos.registry;

import com.knoxhack.signalos.SignalOS;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SignalOS.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SIGNALOS_TAB = CREATIVE_MODE_TABS.register(
            "signalos",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.signalos"))
                    .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                    .icon(() -> ModBlocks.TERMINAL_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(ModBlocks.TERMINAL_ITEM.get()))
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
