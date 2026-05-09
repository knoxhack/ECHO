package com.knoxhack.signalos.content;

import com.knoxhack.signalos.SignalOS;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class SignalOsServerReloaders {
    private SignalOsServerReloaders() {
    }

    public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(SignalOS.MODID, "content"), new SignalOsJsonContentLoader());
    }
}
