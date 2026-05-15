package com.knoxhack.echomultiblockcore;

import com.knoxhack.echomultiblockcore.client.MultiblockControllerScreen;
import com.knoxhack.echomultiblockcore.client.MultiblockCrateScreen;
import com.knoxhack.echomultiblockcore.client.MultiblockPreviewRenderer;
import com.knoxhack.echomultiblockcore.client.RobotAnimationClientState;
import com.knoxhack.echomultiblockcore.registry.ModMenus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = EchoMultiblockCore.MODID, dist = Dist.CLIENT)
public final class EchoMultiblockCoreClient {
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(EchoMultiblockCore.MODID, "build_assist"));
    public static final KeyMapping PREVIEW_ROTATE_KEY = new KeyMapping(
            "key.echomultiblockcore.preview_rotate",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY);
    public static final KeyMapping PREVIEW_MIRROR_KEY = new KeyMapping(
            "key.echomultiblockcore.preview_toggle_mirror",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KEY_CATEGORY);
    public static final KeyMapping PREVIEW_LAYER_UP_KEY = new KeyMapping(
            "key.echomultiblockcore.preview_layer_up",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_UP,
            KEY_CATEGORY);
    public static final KeyMapping PREVIEW_LAYER_DOWN_KEY = new KeyMapping(
            "key.echomultiblockcore.preview_layer_down",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_DOWN,
            KEY_CATEGORY);

    public EchoMultiblockCoreClient() {
        NeoForge.EVENT_BUS.addListener(MultiblockPreviewRenderer::render);
        NeoForge.EVENT_BUS.addListener(EchoMultiblockCoreClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(EchoMultiblockCoreClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(EchoMultiblockCoreClient::onClientTick);
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !MultiblockPreviewRenderer.holdingBlueprint()) {
            return;
        }
        if (PREVIEW_ROTATE_KEY.matches(event.getKeyEvent())) {
            MultiblockPreviewRenderer.rotatePreview();
        } else if (PREVIEW_MIRROR_KEY.matches(event.getKeyEvent())) {
            MultiblockPreviewRenderer.toggleMirror();
        } else if (PREVIEW_LAYER_UP_KEY.matches(event.getKeyEvent())) {
            MultiblockPreviewRenderer.layerUp();
        } else if (PREVIEW_LAYER_DOWN_KEY.matches(event.getKeyEvent())) {
            MultiblockPreviewRenderer.layerDown();
        }
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        MultiblockPreviewRenderer.renderHud(event.getGuiGraphics(),
                event.getPartialTick().getGameTimeDeltaPartialTick(true));
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        RobotAnimationClientState.tick();
    }

    @EventBusSubscriber(modid = EchoMultiblockCore.MODID, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.registerCategory(KEY_CATEGORY);
            event.register(PREVIEW_ROTATE_KEY);
            event.register(PREVIEW_MIRROR_KEY);
            event.register(PREVIEW_LAYER_UP_KEY);
            event.register(PREVIEW_LAYER_DOWN_KEY);
        }

        @SubscribeEvent
        static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.CONTROLLER.get(), MultiblockControllerScreen::new);
            event.register(ModMenus.CRATE.get(), MultiblockCrateScreen::new);
        }

        @SubscribeEvent
        static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            if (!ModList.get().isLoaded("echorendercore")) {
                return;
            }
            try {
                Class.forName("com.knoxhack.echomultiblockcore.integration.MultiblockRenderCoreClientIntegration")
                        .getMethod("registerBlockRenderers", EntityRenderersEvent.RegisterRenderers.class)
                        .invoke(null, event);
            } catch (ReflectiveOperationException | LinkageError exception) {
                EchoMultiblockCore.LOGGER.warn("ECHO MultiblockCore RenderCore block integration could not be registered.", exception);
            }
        }
    }
}
