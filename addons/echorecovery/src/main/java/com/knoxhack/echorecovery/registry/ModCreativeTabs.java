package com.knoxhack.echorecovery.registry;

import com.knoxhack.echorecovery.EchoRecovery;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoRecovery.MODID);

    public static final Supplier<CreativeModeTab> RECOVERY_TAB = TABS.register("recovery_tab",
        () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.RECOVERY_COMPASS.get()))
            .title(Component.literal("ECHO Recovery"))
            .displayItems((params, output) -> {
                ModBlocks.blockItems().forEach(b -> output.accept(b.get()));
                output.accept(ModItems.GRAVE_KEY.get());
                output.accept(ModItems.RECOVERY_COMPASS.get());
                output.accept(ModItems.DEATH_RECORD.get());
                output.accept(ModItems.RECOVERY_TOKEN.get());
            })
            .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
