package com.knoxhack.echoconvoyprotocol.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
   private ModNetwork() {
   }

   public static void registerPayloads(RegisterPayloadHandlersEvent event) {
      event.registrar("1")
         .playToClient(ConvoyTerminalStatePacket.TYPE, ConvoyTerminalStatePacket.CODEC, ModNetwork::handleTerminalState);
   }

   private static void handleTerminalState(ConvoyTerminalStatePacket packet, IPayloadContext context) {
      context.enqueueWork(() -> ConvoyTerminalClientState.apply(packet));
   }
}
