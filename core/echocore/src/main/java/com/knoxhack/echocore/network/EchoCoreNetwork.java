package com.knoxhack.echocore.network;

import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.api.EchoFactionDataService;
import com.knoxhack.echocore.api.EchoCoreServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Network payloads owned by ECHO Core.
 */
public final class EchoCoreNetwork {
    private static final String VERSION = "1";

    private EchoCoreNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION).optional();
        registrar.playToClient(
                EchoFactionSyncPacket.TYPE,
                EchoFactionSyncPacket.CODEC,
                (packet, ctx) -> ctx.enqueueWork(() -> handleFactionSync(packet, ctx.player()))
        );
        registrar.playToClient(
                DiscoveryToastPacket.TYPE,
                DiscoveryToastPacket.CODEC,
                (packet, ctx) -> ctx.enqueueWork(() -> handleDiscoveryToast(packet))
        );
    }

    private static void handleFactionSync(EchoFactionSyncPacket packet, Player player) {
        EchoFactionDataService.importRoot(player, packet.factionRoot());
    }

    private static void handleDiscoveryToast(DiscoveryToastPacket packet) {
        try {
            Class<?> hud = Class.forName("com.knoxhack.echoterminal.client.discovery.DiscoveryToastHud");
            hud.getMethod("push", DiscoveryToastPacket.class).invoke(null, packet);
        } catch (ReflectiveOperationException ignored) {
            EchoCore.LOGGER.debug("Discovery toast received without a terminal HUD consumer.");
        }
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EchoCoreServices.syncFactionDataToClient(player);
            EchoCoreServices.syncDiscoveryDataToClient(player);
        }
    }
}
