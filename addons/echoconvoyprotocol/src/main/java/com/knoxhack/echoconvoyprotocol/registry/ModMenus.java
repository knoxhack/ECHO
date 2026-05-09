package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.menu.ConvoyStationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
   private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, EchoConvoyProtocol.MODID);

   public static final DeferredHolder<MenuType<?>, MenuType<ConvoyStationMenu>> CONVOY_STATION =
      MENUS.register("convoy_station", () -> IMenuTypeExtension.create(ConvoyStationMenu::fromNetwork));

   private ModMenus() {
   }

   public static void register(IEventBus eventBus) {
      MENUS.register(eventBus);
   }
}
