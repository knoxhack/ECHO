package com.knoxhack.echoholomap.network;

import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import com.knoxhack.echoholomap.world.HoloMapWaypointSavedData;
import net.minecraft.server.permissions.Permissions;
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
        EchoNetPayloads.clientboundSync(registrar, HoloMapWaypointSyncPacket.TYPE, HoloMapWaypointSyncPacket.CODEC,
                ModNetwork::handleWaypointSync);
        EchoNetPayloads.serverboundAction(registrar, HoloMapTileRequestPacket.TYPE, HoloMapTileRequestPacket.CODEC,
                EchoRateLimitPolicy.of(4, "holomap_tiles"), ModNetwork::handleTileRequest);
        EchoNetPayloads.serverboundAction(registrar, HoloMapWaypointActionPacket.TYPE, HoloMapWaypointActionPacket.CODEC,
                EchoRateLimitPolicy.of(6, "holomap_waypoints"), ModNetwork::handleWaypointAction);
        EchoNetPayloads.serverboundAction(registrar, HoloMapSyncRequestPacket.TYPE, HoloMapSyncRequestPacket.CODEC,
                EchoRateLimitPolicy.of(4, "holomap_sync"), ModNetwork::handleSyncRequest);
    }

    private static void handleSnapshot(HoloMapSnapshotPacket packet,
            net.minecraft.world.entity.player.Player player, IPayloadContext context) {
        HoloMapClientState.apply(packet);
    }

    private static void handleTileBatch(HoloMapTileBatchPacket packet,
            net.minecraft.world.entity.player.Player player, IPayloadContext context) {
        HoloMapTerrainClientState.apply(packet);
    }

    private static void handleWaypointSync(HoloMapWaypointSyncPacket packet,
            net.minecraft.world.entity.player.Player player, IPayloadContext context) {
        HoloMapWaypointClientState.apply(packet);
    }

    private static void handleTileRequest(HoloMapTileRequestPacket packet,
            net.minecraft.server.level.ServerPlayer player, IPayloadContext context) {
        EchoNetSend.toPlayer(player, HoloMapTileBatchPacket.from(player, packet), EchoPacketKind.CLIENTBOUND_SYNC);
    }

    private static void handleWaypointAction(HoloMapWaypointActionPacket packet,
            net.minecraft.server.level.ServerPlayer player, IPayloadContext context) {
        if (packet == null || player.level().getServer() == null) {
            return;
        }
        HoloMapWaypointSavedData data = HoloMapWaypointSavedData.get(player.level().getServer());
        boolean mayEditShared = player.createCommandSourceStack()
                .permissions()
                .hasPermission(Permissions.COMMANDS_GAMEMASTER);
        switch (packet.action()) {
            case UPSERT -> data.upsert(player, packet.waypoint(), mayEditShared);
            case DELETE -> data.delete(player, packet.waypointId(), mayEditShared);
            case REQUEST_SYNC -> {
            }
        }
        EchoNetSend.toPlayer(player, HoloMapWaypointSyncPacket.from(player), EchoPacketKind.CLIENTBOUND_SYNC);
    }

    private static void handleSyncRequest(HoloMapSyncRequestPacket packet,
            net.minecraft.server.level.ServerPlayer player, IPayloadContext context) {
        HoloMapSync.send(player);
    }
}
