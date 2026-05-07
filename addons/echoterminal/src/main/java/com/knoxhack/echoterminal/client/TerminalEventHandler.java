package com.knoxhack.echoterminal.client;

import com.knoxhack.echoterminal.client.screen.EchoTerminalScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class TerminalEventHandler {
    @SubscribeEvent
    public void onMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() instanceof EchoTerminalScreen screen
                && screen.handleMouseScroll(event.getMouseX(), event.getMouseY(), event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }
}
