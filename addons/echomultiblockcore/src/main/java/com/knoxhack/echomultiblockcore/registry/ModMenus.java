package com.knoxhack.echomultiblockcore.registry;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.menu.MultiblockControllerMenu;
import com.knoxhack.echomultiblockcore.menu.MultiblockCrateMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, EchoMultiblockCore.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MultiblockControllerMenu>> CONTROLLER =
            MENUS.register("controller", () -> IMenuTypeExtension.create(MultiblockControllerMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<MultiblockCrateMenu>> CRATE =
            MENUS.register("crate", () -> IMenuTypeExtension.create(MultiblockCrateMenu::fromNetwork));

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
