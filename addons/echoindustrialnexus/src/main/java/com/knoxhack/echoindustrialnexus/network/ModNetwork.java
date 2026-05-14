package com.knoxhack.echoindustrialnexus.network;

import com.knoxhack.echonetcore.api.EchoNetPayloads;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
   private ModNetwork() {
   }

   public static void registerPayloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = EchoNetPayloads.optional(event);
      EchoNetPayloads.clientboundSync(registrar, IndustrialFactorySnapshotPacket.TYPE,
         IndustrialFactorySnapshotPacket.CODEC,
         (packet, player, context) -> handleClient("handle", packet));
   }

   private static void handleClient(String method, Object packet) {
      try {
         Class.forName("com.knoxhack.echoindustrialnexus.client.IndustrialFactoryClientState")
            .getMethod(method, packet.getClass())
            .invoke(null, packet);
      } catch (ReflectiveOperationException ignored) {
      }
   }
}
