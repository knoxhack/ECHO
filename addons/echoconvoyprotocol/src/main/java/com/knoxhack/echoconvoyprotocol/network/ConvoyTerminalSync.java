package com.knoxhack.echoconvoyprotocol.network;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ConvoyTerminalSync {
   private ConvoyTerminalSync() {
   }

   public static void send(ServerPlayer player) {
      if (player != null) {
         try {
            PacketDistributor.sendToPlayer(player, ConvoyTerminalStatePacket.from(player));
         } catch (RuntimeException exception) {
            EchoConvoyProtocol.LOGGER.debug("Skipped optional convoy terminal sync for {}: {}", player.getName().getString(), exception.getMessage());
         }
      }
   }
}
