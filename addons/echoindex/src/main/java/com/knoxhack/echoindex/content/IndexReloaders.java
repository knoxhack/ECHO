package com.knoxhack.echoindex.content;

import com.knoxhack.echoindex.EchoIndex;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class IndexReloaders {
    private IndexReloaders() {
    }

    public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(EchoIndex.MODID, "content"), new IndexJsonReloadListener());
        event.addListener(Identifier.fromNamespaceAndPath(EchoIndex.MODID, "sources"), new IndexSourceReloadListener());
    }
}
