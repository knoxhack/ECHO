package com.knoxhack.echoterminal.client.screen;

import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import java.util.Objects;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class EchoTerminalScreens {
    private static volatile EchoTerminalScreenProvider primaryProvider;

    private EchoTerminalScreens() {
    }

    public static void registerPrimary(EchoTerminalScreenProvider provider) {
        primaryProvider = Objects.requireNonNull(provider, "provider");
    }

    public static AbstractContainerScreen<EchoTerminalMenu> create(EchoTerminalMenu menu, Inventory playerInventory, Component title) {
        EchoTerminalScreenProvider provider = primaryProvider;
        if (provider != null) {
            try {
                AbstractContainerScreen<EchoTerminalMenu> screen = provider.create(menu, playerInventory, title);
                if (screen != null) {
                    return screen;
                }
            } catch (RuntimeException ignored) {
                primaryProvider = null;
            }
        }
        return new EchoTerminalScreen(menu, playerInventory, title);
    }

    public static boolean isManagedTerminalScreen(Screen screen) {
        if (screen instanceof EchoTerminalScreen) {
            return true;
        }
        EchoTerminalScreenProvider provider = primaryProvider;
        return provider != null && provider.isTerminalScreen(screen);
    }
}
