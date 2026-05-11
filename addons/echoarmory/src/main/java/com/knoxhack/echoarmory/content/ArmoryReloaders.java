package com.knoxhack.echoarmory.content;

import com.knoxhack.echoarmory.EchoArmory;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class ArmoryReloaders {
   private ArmoryReloaders() {
   }

   public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
      event.addListener(Identifier.fromNamespaceAndPath(EchoArmory.MODID, "content"), new ArmoryJsonReloadListener());
   }
}
