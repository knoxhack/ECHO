package com.knoxhack.echoholomap;

import com.knoxhack.echoholomap.client.HoloMapMiniMapOverlay;
import com.knoxhack.echoholomap.client.HoloMapFullScreenMapScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = EchoHoloMap.MODID, dist = Dist.CLIENT)
public final class EchoHoloMapClient {
    private static final Identifier MINIMAP_LAYER =
            Identifier.fromNamespaceAndPath(EchoHoloMap.MODID, "minimap");
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(EchoHoloMap.MODID, "holomap"));
    public static final KeyMapping TOGGLE_MINIMAP_KEY = new KeyMapping(
            "key.echoholomap.toggle_minimap",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KEY_CATEGORY);
    public static final KeyMapping OPEN_MAP_KEY = new KeyMapping(
            "key.echoholomap.open_map",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KEY_CATEGORY);
    public static final KeyMapping MINIMAP_ZOOM_IN_KEY = new KeyMapping(
            "key.echoholomap.minimap_zoom_in",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            KEY_CATEGORY);
    public static final KeyMapping MINIMAP_ZOOM_OUT_KEY = new KeyMapping(
            "key.echoholomap.minimap_zoom_out",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            KEY_CATEGORY);
    public static final KeyMapping MINIMAP_CYCLE_CORNER_KEY = new KeyMapping(
            "key.echoholomap.minimap_cycle_corner",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_BACKSLASH,
            KEY_CATEGORY);

    public EchoHoloMapClient() {
        NeoForge.EVENT_BUS.addListener(EchoHoloMapClient::onKeyInput);
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
        if (TOGGLE_MINIMAP_KEY.matches(event.getKeyEvent())) {
            HoloMapMiniMapOverlay.toggle();
        } else if (OPEN_MAP_KEY.matches(event.getKeyEvent())) {
            minecraft.setScreen(new HoloMapFullScreenMapScreen());
        } else if (MINIMAP_ZOOM_IN_KEY.matches(event.getKeyEvent())) {
            HoloMapMiniMapOverlay.zoomIn();
        } else if (MINIMAP_ZOOM_OUT_KEY.matches(event.getKeyEvent())) {
            HoloMapMiniMapOverlay.zoomOut();
        } else if (MINIMAP_CYCLE_CORNER_KEY.matches(event.getKeyEvent())) {
            HoloMapMiniMapOverlay.cycleCorner();
        }
    }

    private static void registerTerminalClientIntegration() {
        try {
            Class.forName("com.knoxhack.echoholomap.integration.HoloMapTerminalClientIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            EchoHoloMap.LOGGER.warn("ECHO HoloMap terminal client integration could not be registered.", exception);
        }
    }

    @EventBusSubscriber(modid = EchoHoloMap.MODID, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.registerCategory(KEY_CATEGORY);
            event.register(TOGGLE_MINIMAP_KEY);
            event.register(OPEN_MAP_KEY);
            event.register(MINIMAP_ZOOM_IN_KEY);
            event.register(MINIMAP_ZOOM_OUT_KEY);
            event.register(MINIMAP_CYCLE_CORNER_KEY);
        }

        @SubscribeEvent
        static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.AIR_LEVEL, MINIMAP_LAYER, HoloMapMiniMapOverlay::render);
        }
    }
}
