package com.knoxhack.echolens.client;

import com.knoxhack.echolens.EchoLens;
import java.lang.reflect.Constructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class LensClientActions {
    private LensClientActions() {
    }

    public static void openIndexRecipes(ItemStack stack) {
        openIndexRecipeScreen(stack, "RECIPES");
    }

    public static void openIndexUses(ItemStack stack) {
        openIndexRecipeScreen(stack, "USES");
    }

    public static void trackInIndex(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (!ModList.get().isLoaded("echoindex")) {
            tell("ECHO: Index is not installed.");
            return;
        }
        try {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Class<?> packetClass = Class.forName("com.knoxhack.echoindex.network.IndexActionPacket");
            Class<?> actionClass = Class.forName("com.knoxhack.echoindex.network.IndexActionPacket$Action");
            Object action = Enum.valueOf(actionClass.asSubclass(Enum.class), "BOOKMARK");
            Constructor<?> constructor = packetClass.getConstructor(actionClass, Identifier.class);
            Object payload = constructor.newInstance(action, itemId);
            if (payload instanceof CustomPacketPayload packet) {
                ClientPacketDistributor.sendToServer(packet);
                tell("Tracking " + itemId + " in ECHO: Index.");
            }
        } catch (ReflectiveOperationException exception) {
            EchoLens.LOGGER.warn("Could not send ECHO: Index track request from Lens.", exception);
            tell("ECHO: Index tracking is unavailable.");
        }
    }

    private static void openIndexRecipeScreen(ItemStack stack, String modeName) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (!ModList.get().isLoaded("echoindex")) {
            tell("ECHO: Index is not installed.");
            return;
        }
        try {
            Class<?> screenClass = Class.forName("com.knoxhack.echoindex.client.IndexRecipeScreen");
            Class<?> modeClass = Class.forName("com.knoxhack.echoindex.client.IndexRecipeScreen$Mode");
            Object mode = Enum.valueOf(modeClass.asSubclass(Enum.class), modeName);
            Constructor<?> constructor = screenClass.getConstructor(ItemStack.class, modeClass);
            Screen screen = (Screen) constructor.newInstance(stack.copy(), mode);
            Minecraft.getInstance().setScreen(screen);
        } catch (ReflectiveOperationException exception) {
            EchoLens.LOGGER.warn("Could not open ECHO: Index {} screen from Lens.", modeName, exception);
            tell("ECHO: Index recipe view is unavailable.");
        }
    }

    private static void tell(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.literal(message));
        }
    }
}
