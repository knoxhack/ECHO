package com.knoxhack.echorecovery.registry;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.menu.GraveMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, EchoRecovery.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GraveMenu>> GRAVE =
        MENUS.register("grave", () -> IMenuTypeExtension.create(GraveMenu::new));

    private ModMenus() {}

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
