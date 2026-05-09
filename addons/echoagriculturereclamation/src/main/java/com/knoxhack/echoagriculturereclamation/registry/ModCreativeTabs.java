package com.knoxhack.echoagriculturereclamation.registry;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
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
   private static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoAgricultureReclamation.MODID);

   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AGRICULTURE_RECLAMATION = TABS.register(
      "agriculture_reclamation",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.echoagriculturereclamation.agriculture_reclamation"))
         .withTabsBefore(new ResourceKey[]{CreativeModeTabs.FUNCTIONAL_BLOCKS})
         .icon(() -> ((Item)ModItems.RECOVERED_SEED_CAPSULE.get()).getDefaultInstance())
         .displayItems((parameters, output) -> ModItems.creativeItems().forEach(output::accept))
         .build()
   );

   private ModCreativeTabs() {
   }

   public static void register(IEventBus eventBus) {
      TABS.register(eventBus);
   }
}
