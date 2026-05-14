package com.knoxhack.echoblockworks.registry;

import com.knoxhack.echoblockworks.EchoBlockworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
   private static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoBlockworks.MODID);

   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKWORKS = TABS.register("blockworks",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.echoblockworks.blockworks"))
         .withTabsBefore(CreativeModeTabs.BUILDING_BLOCKS)
         .icon(() -> ModBlocks.blockForId("reinforced_metal_panel")
            .orElse(ModBlocks.BLOCKWORKS_TABLE).get().asItem().getDefaultInstance())
         .displayItems((parameters, output) -> ModItems.creativeItems().forEach(output::accept))
         .build());

   private ModCreativeTabs() {
   }

   public static void register(IEventBus eventBus) {
      TABS.register(eventBus);
   }
}
