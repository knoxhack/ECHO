package com.knoxhack.signalos.client;

import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.network.SignalOsOpenTerminalPacket;
import com.knoxhack.signalos.registry.ModMenus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = SignalOS.MODID, dist = Dist.CLIENT)
public class SignalOSClient {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(SignalOS.MODID, "terminal"));

    public static final KeyMapping OPEN_TERMINAL_KEY = new KeyMapping(
            "key.signalos.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            KEY_CATEGORY);

    public SignalOSClient(ModContainer container) {
        NeoForge.EVENT_BUS.addListener(SignalOSClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(SignalOSClient::onCharacterTyped);
        if (ModList.get().isLoaded("echorendercore")) {
            registerRenderCoreScreenIntegration();
        }
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS || !OPEN_TERMINAL_KEY.matches(event.getKeyEvent())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (minecraft.screen == null) {
            EchoNetClientActions.sendServerboundAction(new SignalOsOpenTerminalPacket());
        } else if (minecraft.screen instanceof SignalOsTerminalScreen) {
            minecraft.setScreen(null);
        }
    }

    private static void onCharacterTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (event.getScreen() instanceof SignalOsTerminalScreen screen && screen.handleCharTyped(event.getCharacterEvent())) {
            event.setCanceled(true);
        } else if (event.getScreen() instanceof SignalOsServerRackScreen screen
                && screen.handleCharTyped(event.getCharacterEvent())) {
            event.setCanceled(true);
        }
    }

    private static void registerRenderCoreScreenIntegration() {
        try {
            Class.forName("com.knoxhack.signalos.integration.SignalOsRenderCoreClientIntegration")
                    .getMethod("registerScreenVisuals")
                    .invoke(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            SignalOS.LOGGER.warn("SignalOS RenderCore screen integration could not be registered.", exception);
        }
    }

    private static void registerRenderCoreBlockRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (!ModList.get().isLoaded("echorendercore")) {
            return;
        }
        try {
            Class.forName("com.knoxhack.signalos.integration.SignalOsRenderCoreClientIntegration")
                    .getMethod("registerBlockRenderers", EntityRenderersEvent.RegisterRenderers.class)
                    .invoke(null, event);
        } catch (ReflectiveOperationException | LinkageError exception) {
            SignalOS.LOGGER.warn("SignalOS RenderCore block integration could not be registered.", exception);
        }
    }

    @EventBusSubscriber(modid = SignalOS.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.registerCategory(KEY_CATEGORY);
            event.register(OPEN_TERMINAL_KEY);
        }

        @SubscribeEvent
        static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.TERMINAL.get(), SignalOsTerminalScreen::new);
            event.register(ModMenus.SERVER_RACK.get(), SignalOsServerRackScreen::new);
        }

        @SubscribeEvent
        static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            registerRenderCoreBlockRenderers(event);
        }
    }
}
