package com.knoxhack.echoindex.network;

import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import com.knoxhack.echoindex.service.IndexDiscoveryStore;
import com.knoxhack.echoindex.service.IndexService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, IndexStateSyncPacket.TYPE, IndexStateSyncPacket.CODEC,
                (packet, player, context) -> IndexDiscoveryStore.INSTANCE.applyClientSync(packet.state()));
        EchoNetPayloads.serverboundAction(registrar, IndexActionPacket.TYPE, IndexActionPacket.CODEC,
                EchoRateLimitPolicy.of(6, "echoindex_action"), ModNetwork::handleAction);
    }

    private static void handleAction(IndexActionPacket packet, ServerPlayer player,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        switch (packet.action()) {
            case REQUEST_SYNC -> IndexSync.send(player);
            case MARK_READ -> {
                if (IndexService.INSTANCE.entry(player, packet.targetId()).isPresent()) {
                    IndexDiscoveryStore.INSTANCE.markRead(player, packet.targetId());
                }
                IndexSync.send(player);
            }
            case BOOKMARK -> {
                IndexDiscoveryStore.INSTANCE.setBookmarked(player, packet.targetId(), true);
                IndexSync.send(player);
            }
            case UNBOOKMARK -> {
                IndexDiscoveryStore.INSTANCE.setBookmarked(player, packet.targetId(), false);
                IndexSync.send(player);
            }
        }
    }
}
