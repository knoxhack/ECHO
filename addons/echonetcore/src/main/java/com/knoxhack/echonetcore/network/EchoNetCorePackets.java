package com.knoxhack.echonetcore.network;

import com.knoxhack.echocore.api.EchoFactionDataService;
import com.knoxhack.echocore.api.network.EchoDiscoveryToast;
import com.knoxhack.echocore.api.network.EchoPacketDirection;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.EchoNetCore;
import com.knoxhack.echonetcore.api.EchoClientSyncRegistry;
import com.knoxhack.echonetcore.api.EchoDebugCommandRegistry;
import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import com.knoxhack.echonetcore.config.EchoNetCoreConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class EchoNetCorePackets {
    private EchoNetCorePackets() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, EchoFactionSyncPacket.TYPE, EchoFactionSyncPacket.CODEC,
                EchoNetCorePackets::handleFactionSync);
        EchoNetPayloads.clientboundSync(registrar, DiscoveryToastPacket.TYPE, DiscoveryToastPacket.CODEC,
                EchoNetCorePackets::handleDiscoveryToast);
        EchoNetPayloads.clientboundSync(registrar, EchoSyncPayload.TYPE, EchoSyncPayload.CODEC,
                (packet, player, context) -> EchoClientSyncRegistry.dispatch(packet));
        EchoNetPayloads.debugServerbound(registrar, EchoDebugCommandPacket.TYPE, EchoDebugCommandPacket.CODEC,
                EchoRateLimitPolicy.of(EchoNetCoreConfig.DEBUG_ACTION_RATE_LIMIT_TICKS.get(), "debug_command"),
                EchoNetCorePackets::handleDebugCommand);
    }

    private static void handleFactionSync(EchoFactionSyncPacket packet, Player player,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        if (player != null) {
            EchoFactionDataService.importRoot(player, packet.factionRoot());
        }
    }

    private static void handleDiscoveryToast(DiscoveryToastPacket packet, Player player,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            return;
        }
        try {
            Class<?> hud = Class.forName("com.knoxhack.echoterminal.client.discovery.DiscoveryToastHud");
            hud.getMethod("push", EchoDiscoveryToast.class).invoke(null, packet.toast());
        } catch (ReflectiveOperationException ignored) {
            EchoNetCore.LOGGER.debug("Discovery toast received without a terminal HUD consumer.");
        }
    }

    private static void handleDebugCommand(EchoDebugCommandPacket packet, ServerPlayer player,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        if (!EchoNetCoreConfig.ENABLE_DEBUG_PACKETS.get()) {
            EchoNetDebug.emit(packet.type().id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.DEBUG_DEV,
                    player.getScoreboardName(), false, "debug-disabled");
            return;
        }
        if (!player.createCommandSourceStack().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            EchoNetDebug.emit(packet.type().id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.DEBUG_DEV,
                    player.getScoreboardName(), false, "permission-denied");
            return;
        }
        if (!EchoDebugCommandRegistry.handle(player, packet.commandId(), packet.payload())) {
            EchoNetDebug.emit(packet.type().id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.DEBUG_DEV,
                    player.getScoreboardName(), false, "unknown-command");
        }
    }
}
