package com.knoxhack.echoashfallprotocol.client;

import com.knoxhack.echoashfallprotocol.Config;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.client.screen.EchoMainMenuScreen;
import com.knoxhack.echoashfallprotocol.client.screen.EchoVanillaScreenTheme;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = EchoAshfallProtocol.MODID, value = Dist.CLIENT)
public final class EchoMainMenuEvents {
    private EchoMainMenuEvents() {
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (Config.ENABLE_ECHO_MAIN_MENU.get() && event.getNewScreen() instanceof TitleScreen) {
            try {
                event.setNewScreen(new EchoMainMenuScreen());
            } catch (RuntimeException ignored) {
                // Leave the vanilla title screen alone if the custom shell cannot be created.
            }
        }
    }

    @SubscribeEvent
    public static void onScreenBackground(ScreenEvent.Render.Background event) {
        try {
            EchoVanillaScreenTheme.renderBackground(event.getScreen(), event.getGuiGraphics(), event.getPartialTick());
        } catch (RuntimeException ignored) {
            // Preserve vanilla screens if the terminal skin cannot be drawn.
        }
    }

    @SubscribeEvent
    public static void onScreenPostRender(ScreenEvent.Render.Post event) {
        try {
            EchoVanillaScreenTheme.renderForeground(event.getScreen(), event.getGuiGraphics(), event.getPartialTick());
        } catch (RuntimeException ignored) {
            // Preserve vanilla screens if the terminal skin cannot be drawn.
        }
    }
}
