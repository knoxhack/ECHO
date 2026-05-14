package com.knoxhack.echomultiblockcore.content;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class MultiblockReloaders {
    private MultiblockReloaders() {
    }

    public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(EchoMultiblockCore.id("definitions"), new MultiblockJsonReloadListener());
        event.addListener(EchoMultiblockCore.id("automation_recipes"), new AutomationRecipeJsonReloadListener());
        event.addListener(EchoMultiblockCore.id("upgrades"), new MultiblockUpgradeJsonReloadListener());
        event.addListener(EchoMultiblockCore.id("progression"), new MultiblockProgressionJsonReloadListener());
    }
}
