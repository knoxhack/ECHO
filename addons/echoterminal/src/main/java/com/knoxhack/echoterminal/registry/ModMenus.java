package com.knoxhack.echoterminal.registry;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, EchoTerminal.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<EchoTerminalMenu>> ECHO_TERMINAL =
            MENUS.register("echo_terminal", () -> IMenuTypeExtension.create(EchoTerminalMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
