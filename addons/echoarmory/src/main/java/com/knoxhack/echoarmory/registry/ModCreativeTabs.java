package com.knoxhack.echoarmory.registry;

import com.knoxhack.echoarmory.EchoArmory;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
   private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoArmory.MODID);

   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARMORY =
      CREATIVE_TABS.register("armory", () -> CreativeModeTab.builder()
         .title(Component.literal("ECHO: Armory"))
         .icon(() -> ModItems.FROST_BLADE.get().getDefaultInstance())
         .displayItems((parameters, output) -> ModItems.creativeItems().forEach(item -> output.accept(item.get())))
         .build());

   private ModCreativeTabs() {
   }

   public static void register(IEventBus eventBus) {
      CREATIVE_TABS.register(eventBus);
   }
}
