package com.knoxhack.echologisticsnetwork.registry;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.menu.LogisticsMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
   private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, EchoLogisticsNetwork.MODID);

   public static final DeferredHolder<MenuType<?>, MenuType<LogisticsMenu>> LOGISTICS =
      MENUS.register("logistics", () -> IMenuTypeExtension.create(LogisticsMenu::fromNetwork));

   private ModMenus() {
   }

   public static void register(IEventBus eventBus) {
      MENUS.register(eventBus);
   }
}
