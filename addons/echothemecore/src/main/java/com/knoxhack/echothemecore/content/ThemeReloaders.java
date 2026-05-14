package com.knoxhack.echothemecore.content;

import com.knoxhack.echothemecore.EchoThemeCore;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class ThemeReloaders {
    private ThemeReloaders() {
    }

    public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(EchoThemeCore.id("themes"), new ThemeJsonReloadListener());
    }
}
