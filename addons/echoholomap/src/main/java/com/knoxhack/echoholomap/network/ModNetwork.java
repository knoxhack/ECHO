package com.knoxhack.echoholomap.network;

import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, HoloMapSnapshotPacket.TYPE, HoloMapSnapshotPacket.CODEC,
                ModNetwork::handleSnapshot);
        EchoNetPayloads.clientboundSync(registrar, HoloMapTileBatchPacket.TYPE, HoloMapTileBatchPacket.CODEC,
                ModNetwork::handleTileBatch);
        EchoNetPayloads.serverboundAction(registrar, HoloMapTileRequestPacket.TYPE, HoloMapTileRequestPacket.CODEC,
                EchoRateLimitPolicy.of(4, "holomap_tiles"), ModNetwork::handleTileRequest);
    }

    private static void handleSnapshot(HoloMapSnapshotPacket packet,
            net.minecraft.world.entity.player.Player player, IPayloadContext context) {
        HoloMapClientState.apply(packet);
    }

    private static void handleTileBatch(HoloMapTileBatchPacket packet,
            net.minecraft.world.entity.player.Player player, IPayloadContext context) {
        HoloMapTerrainClientState.apply(packet);
    }

    private static void handleTileRequest(HoloMapTileRequestPacket packet,
            net.minecraft.server.level.ServerPlayer player, IPayloadContext context) {
        EchoNetSend.toPlayer(player, HoloMapTileBatchPacket.from(player, packet), EchoPacketKind.CLIENTBOUND_SYNC);
    }
}
