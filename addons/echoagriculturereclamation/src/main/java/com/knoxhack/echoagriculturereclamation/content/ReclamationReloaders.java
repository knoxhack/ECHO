package com.knoxhack.echoagriculturereclamation.content;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class ReclamationReloaders {
   private ReclamationReloaders() {
   }

   public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
      event.addListener(Identifier.fromNamespaceAndPath(EchoAgricultureReclamation.MODID, "content"), new ReclamationJsonReloadListener());
   }
}
