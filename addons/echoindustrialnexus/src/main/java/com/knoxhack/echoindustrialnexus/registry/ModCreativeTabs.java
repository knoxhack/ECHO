package com.knoxhack.echoindustrialnexus.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
   private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "echoindustrialnexus");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INDUSTRIAL_NEXUS = TABS.register(
      "industrial_nexus",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.echoindustrialnexus.industrial_nexus"))
         .withTabsBefore(new ResourceKey[]{CreativeModeTabs.REDSTONE_BLOCKS})
         .icon(() -> ((Item)ModItems.THERMAL_WRENCH.get()).getDefaultInstance())
         .displayItems((parameters, output) -> ModItems.creativeItems().forEach(output::accept))
         .build()
   );

   private ModCreativeTabs() {
   }

   public static void register(IEventBus eventBus) {
      TABS.register(eventBus);
   }
}
