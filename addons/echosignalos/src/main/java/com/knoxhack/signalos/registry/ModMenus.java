package com.knoxhack.signalos.registry;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.menu.SignalOsTerminalMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SignalOS.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SignalOsTerminalMenu>> TERMINAL =
            MENUS.register("terminal", () -> IMenuTypeExtension.create(SignalOsTerminalMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
