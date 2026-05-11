package com.knoxhack.echoconvoyprotocol.network;

import com.knoxhack.echonetcore.api.EchoNetPayloads;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
   private ModNetwork() {
   }

   public static void registerPayloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = EchoNetPayloads.optional(event);
      EchoNetPayloads.clientboundSync(registrar, ConvoyTerminalStatePacket.TYPE, ConvoyTerminalStatePacket.CODEC,
         ModNetwork::handleTerminalState);
   }

   private static void handleTerminalState(ConvoyTerminalStatePacket packet,
         net.minecraft.world.entity.player.Player player, IPayloadContext context) {
      ConvoyTerminalClientState.apply(packet);
   }
}
