package com.knoxhack.echolens;

import com.knoxhack.echolens.client.LensClientActions;
import com.knoxhack.echolens.client.LensHudOverlay;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = EchoLens.MODID, dist = Dist.CLIENT)
public class EchoLensClient {
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(EchoLens.MODID, "lens"));
    public static final KeyMapping DEEP_SCAN_KEY = new KeyMapping(
            "echolens.key.deep_scan",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            KEY_CATEGORY);

    public EchoLensClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(EchoLensClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(EchoLensClient::onRenderGui);
        if (ModList.get().isLoaded("echoindex")) {
            registerIndexClientIntegration();
        }
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || LensHudOverlay.currentTargetStack().isEmpty()) {
            return;
        }
        if (event.getKey() == GLFW.GLFW_KEY_R) {
            LensClientActions.openIndexRecipes(LensHudOverlay.currentTargetStack());
        } else if (event.getKey() == GLFW.GLFW_KEY_U) {
            LensClientActions.openIndexUses(LensHudOverlay.currentTargetStack());
        } else if (event.getKey() == GLFW.GLFW_KEY_T) {
            LensClientActions.trackInIndex(LensHudOverlay.currentTargetStack());
        }
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        LensHudOverlay.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(true));
    }

    private static void registerIndexClientIntegration() {
        try {
            Class.forName("com.knoxhack.echolens.client.integration.LensIndexClientIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            EchoLens.LOGGER.warn("ECHO: Lens Index client integration could not be registered.", exception);
        }
    }

    @EventBusSubscriber(modid = EchoLens.MODID, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.registerCategory(KEY_CATEGORY);
            event.register(DEEP_SCAN_KEY);
        }
    }
}
