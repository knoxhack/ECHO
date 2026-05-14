package com.knoxhack.echoterminal;

import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.client.BuiltinTerminalTabs;
import com.knoxhack.echoterminal.client.TerminalEventHandler;
import com.knoxhack.echoterminal.client.discovery.DiscoveryToastHud;
import com.knoxhack.echoterminal.client.mission.TerminalMissionHudController;
import com.knoxhack.echoterminal.client.screen.EchoTerminalScreens;
import com.knoxhack.echoterminal.client.screen.TerminalClientConfigIntegration;
import com.knoxhack.echoterminal.client.screen.TerminalClientOptions;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import com.knoxhack.echoterminal.registry.ModMenus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = EchoTerminal.MODID, dist = Dist.CLIENT)
public class EchoTerminalClient {
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "terminal"));
    public static final KeyMapping OPEN_TERMINAL_KEY = new KeyMapping(
            "key.echoterminal.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KEY_CATEGORY);

    public EchoTerminalClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        TerminalClientOptions.load();
        TerminalClientConfigIntegration.register();
        BuiltinTerminalTabs.register();
        NeoForge.EVENT_BUS.addListener(EchoTerminalClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(EchoTerminalClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(EchoTerminalClient::onRenderGui);
        NeoForge.EVENT_BUS.register(new TerminalEventHandler());
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
            minecraft.setScreen(EchoTerminalScreens.create(
                    new EchoTerminalMenu(0, minecraft.player.getInventory()),
                    minecraft.player.getInventory(),
                    Component.translatable("container.echoterminal.echo_terminal")));
        } else if (EchoTerminalScreens.isManagedTerminalScreen(minecraft.screen)) {
            minecraft.setScreen(null);
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        TerminalMissionHudController.tick();
        DiscoveryToastHud.tick();
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        TerminalMissionHudController.render(event.getGuiGraphics(),
                event.getPartialTick().getGameTimeDeltaPartialTick(true));
        DiscoveryToastHud.render(event.getGuiGraphics(),
                event.getPartialTick().getGameTimeDeltaPartialTick(true));
    }

    private static void registerRenderCoreScreenIntegration() {
        try {
            Class.forName("com.knoxhack.echoterminal.integration.TerminalRenderCoreClientIntegration")
                    .getMethod("registerScreenVisuals")
                    .invoke(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            EchoTerminal.LOGGER.warn("ECHO Terminal RenderCore screen integration could not be registered.", exception);
        }
    }

    private static void registerRenderCoreBlockRenderer(EntityRenderersEvent.RegisterRenderers event) {
        if (!ModList.get().isLoaded("echorendercore")) {
            return;
        }
        try {
            Class.forName("com.knoxhack.echoterminal.integration.TerminalRenderCoreClientIntegration")
                    .getMethod("registerBlockRenderer", EntityRenderersEvent.RegisterRenderers.class)
                    .invoke(null, event);
        } catch (ReflectiveOperationException | LinkageError exception) {
            EchoTerminal.LOGGER.warn("ECHO Terminal RenderCore block integration could not be registered.", exception);
        }
    }

    @EventBusSubscriber(modid = EchoTerminal.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.registerCategory(KEY_CATEGORY);
            event.register(OPEN_TERMINAL_KEY);
        }

        @SubscribeEvent
        static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            TerminalTabRegistry.ensureSorted();
            event.register(ModMenus.ECHO_TERMINAL.get(), EchoTerminalScreens::create);
        }

        @SubscribeEvent
        static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            registerRenderCoreBlockRenderer(event);
        }
    }
}
