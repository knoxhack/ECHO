package com.knoxhack.echologisticsnetwork.content;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class LogisticsReloaders {
   private LogisticsReloaders() {
   }

   public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
      event.addListener(Identifier.fromNamespaceAndPath(EchoLogisticsNetwork.MODID, "content"), new LogisticsJsonReloadListener());
   }
}
