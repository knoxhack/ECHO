package com.knoxhack.signalos.client;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.network.SignalOsOpenTerminalPacket;
import com.knoxhack.signalos.registry.ModMenus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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
            ClientPacketDistributor.sendToServer(new SignalOsOpenTerminalPacket());
        } else if (minecraft.screen instanceof SignalOsTerminalScreen) {
            minecraft.setScreen(null);
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
        }
    }
}
