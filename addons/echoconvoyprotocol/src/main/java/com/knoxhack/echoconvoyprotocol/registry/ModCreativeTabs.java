package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
   private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoConvoyProtocol.MODID);

   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONVOY_PROTOCOL =
      CREATIVE_TABS.register("convoy_protocol", () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.echoconvoyprotocol.convoy_protocol"))
         .icon(() -> ModItems.ROUTE_BEACON.get().getDefaultInstance())
         .displayItems((parameters, output) -> ModItems.creativeItems().forEach(item -> output.accept(item.get())))
         .build());

   private ModCreativeTabs() {
   }

   public static void register(IEventBus eventBus) {
      CREATIVE_TABS.register(eventBus);
   }
}
