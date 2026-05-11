package com.knoxhack.echomissioncore.content;

import com.knoxhack.echomissioncore.EchoMissionCore;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class MissionCoreReloaders {
    private MissionCoreReloaders() {
    }

    public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(EchoMissionCore.MODID, "content"), new MissionCoreJsonReloadListener());
    }
}
