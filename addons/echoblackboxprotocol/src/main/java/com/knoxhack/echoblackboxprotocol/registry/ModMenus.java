package com.knoxhack.echoblackboxprotocol.registry;

import com.knoxhack.echoblackboxprotocol.EchoBlackboxProtocol;
import com.knoxhack.echoblackboxprotocol.menu.BlackboxMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
   private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, EchoBlackboxProtocol.MODID);

   public static final DeferredHolder<MenuType<?>, MenuType<BlackboxMachineMenu>> BLACKBOX_MACHINE =
      MENUS.register("blackbox_machine", () -> IMenuTypeExtension.create(BlackboxMachineMenu::fromNetwork));

   private ModMenus() {
   }

   public static void register(IEventBus eventBus) {
      MENUS.register(eventBus);
   }
}
