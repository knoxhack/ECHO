package com.knoxhack.echoterminal.network;

import com.knoxhack.echocore.api.config.EchoConfigApplyResult;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.serverboundAction(registrar, TerminalActionPacket.TYPE, TerminalActionPacket.CODEC,
                EchoRateLimitPolicy.NONE, ModNetwork::handleTerminalAction);
        EchoNetPayloads.serverboundAction(registrar, TerminalConfigActionPacket.TYPE, TerminalConfigActionPacket.CODEC,
                EchoRateLimitPolicy.of(4, "terminal_config_action"), ModNetwork::handleConfigAction);
        EchoNetPayloads.clientboundSync(registrar, TerminalConfigSyncPacket.TYPE, TerminalConfigSyncPacket.CODEC,
                (packet, player, context) -> TerminalConfigClientState.apply(packet));
    }

    private static void handleTerminalAction(TerminalActionPacket packet, ServerPlayer player, IPayloadContext context) {
        if (!TerminalActionRegistry.handle(player, packet.tabId(), packet.actionId(), packet.payload())) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Terminal action signal unknown. Reopen the tab and try again."), true);
        }
    }

    private static void handleConfigAction(TerminalConfigActionPacket packet, ServerPlayer player, IPayloadContext context) {
        if (packet.side() != EchoConfigSide.COMMON) {
            sendConfigSnapshot(player, "Client config is local to this terminal.");
            return;
        }
        switch (packet.action()) {
            case REQUEST -> sendConfigSnapshot(player, "");
            case SET -> {
                if (!canEditServerConfig(player)) {
                    sendConfigSnapshot(player, "Operator access is required to change server config.");
                    return;
                }
                EchoConfigApplyResult result = EchoConfigRegistry.apply(EchoConfigSide.COMMON,
                        packet.moduleId(), packet.entryId(), packet.value());
                sendConfigSnapshot(player, result.message());
            }
            case RESET -> {
                if (!canEditServerConfig(player)) {
                    sendConfigSnapshot(player, "Operator access is required to reset server config.");
                    return;
                }
                EchoConfigApplyResult result = EchoConfigRegistry.reset(EchoConfigSide.COMMON,
                        packet.moduleId(), packet.entryId());
                sendConfigSnapshot(player, result.message());
            }
        }
    }

    private static void sendConfigSnapshot(ServerPlayer player, String status) {
        EchoNetSend.toPlayer(player,
                new TerminalConfigSyncPacket(EchoConfigRegistry.snapshots(EchoConfigSide.COMMON), status),
                EchoPacketKind.CLIENTBOUND_SYNC);
    }

    private static boolean canEditServerConfig(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        if (player.createCommandSourceStack().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return true;
        }
        return isSingleplayerOwner(player);
    }

    private static boolean isSingleplayerOwner(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
        try {
            Method method = server.getClass().getMethod("isSingleplayerOwner", com.mojang.authlib.GameProfile.class);
            Object result = method.invoke(server, player.getGameProfile());
            return Boolean.TRUE.equals(result);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }
}
