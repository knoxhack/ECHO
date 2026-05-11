package com.knoxhack.echoworldcore.content;

import com.knoxhack.echoworldcore.EchoWorldCore;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class WorldCoreReloaders {
    private WorldCoreReloaders() {
    }

    public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(EchoWorldCore.MODID, "world_definitions"),
                new WorldCoreJsonReloadListener());
    }
}
