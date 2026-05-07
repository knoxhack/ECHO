package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.menu.NexusMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
   private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "echonexusprotocol");
   public static final DeferredHolder<MenuType<?>, MenuType<NexusMachineMenu>> NEXUS_MACHINE = MENUS.register("nexus_machine", () -> IMenuTypeExtension.create(NexusMachineMenu::fromNetwork));
   private ModMenus() {}
   public static void register(IEventBus eventBus) { MENUS.register(eventBus); }
}