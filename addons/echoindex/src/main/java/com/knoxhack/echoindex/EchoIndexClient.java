package com.knoxhack.echoindex;

import com.knoxhack.echoindex.client.IndexOverlay;
import com.knoxhack.echoindex.client.IndexRecipeScreen;
import com.knoxhack.echoindex.content.IndexSourceReloadListener;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = EchoIndex.MODID, dist = Dist.CLIENT)
public class EchoIndexClient {
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(EchoIndex.MODID, "index"));
    public static final KeyMapping SHOW_RECIPE_KEY = new KeyMapping(
            "key.echoindex.recipe",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY);
    public static final KeyMapping SHOW_USAGE_KEY = new KeyMapping(
            "key.echoindex.usage",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            KEY_CATEGORY);
    public static final KeyMapping BOOKMARK_KEY = new KeyMapping(
            "key.echoindex.bookmark",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY);

    public EchoIndexClient(ModContainer container) {
        NeoForge.EVENT_BUS.addListener(EchoIndexClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(IndexOverlay::onRender);
        NeoForge.EVENT_BUS.addListener(IndexOverlay::onMouseClicked);
        NeoForge.EVENT_BUS.addListener(IndexOverlay::onMouseScrolled);
        NeoForge.EVENT_BUS.addListener(IndexOverlay::onKeyPressed);
        NeoForge.EVENT_BUS.addListener(IndexOverlay::onCharTyped);
        if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalClientIntegration();
        }
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (SHOW_RECIPE_KEY.matches(event.getKeyEvent())) {
            minecraft.setScreen(new IndexRecipeScreen(minecraft.player.getMainHandItem(), IndexRecipeScreen.Mode.RECIPES));
        } else if (SHOW_USAGE_KEY.matches(event.getKeyEvent())) {
            minecraft.setScreen(new IndexRecipeScreen(minecraft.player.getMainHandItem(), IndexRecipeScreen.Mode.USES));
        }
    }

    private static void registerTerminalClientIntegration() {
        try {
            Class.forName("com.knoxhack.echoindex.integration.IndexTerminalClientIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            EchoIndex.LOGGER.warn("ECHO: Index terminal client integration could not be registered.", exception);
        }
    }

    @EventBusSubscriber(modid = EchoIndex.MODID, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.registerCategory(KEY_CATEGORY);
            event.register(SHOW_RECIPE_KEY);
            event.register(SHOW_USAGE_KEY);
            event.register(BOOKMARK_KEY);
        }

        @SubscribeEvent
        static void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
            event.addListener(EchoIndex.id("sources"), new IndexSourceReloadListener());
        }
    }
}
