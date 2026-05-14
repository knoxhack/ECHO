package com.knoxhack.echoconvoyprotocol.content;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class ConvoyReloaders {
   private ConvoyReloaders() {
   }

   public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
      event.addListener(Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "routes"), new ConvoyJsonReloadListener());
      event.addListener(Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "incidents"), new ConvoyIncidentJsonReloadListener());
   }
}
