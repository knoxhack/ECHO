package com.knoxhack.echonetcore.network;

import com.knoxhack.echocore.api.network.EchoPacketDebugEvent;
import com.knoxhack.echocore.api.network.EchoPacketDirection;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echocore.api.network.PacketDebugHook;
import com.knoxhack.echocore.api.network.PacketDebugHooks;
import com.knoxhack.echonetcore.EchoNetCore;
import com.knoxhack.echonetcore.config.EchoNetCoreConfig;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class EchoNetDebug {
    public static final PacketHub HOOKS = new PacketHub();

    private EchoNetDebug() {
    }

    public static void emit(Identifier payloadId, EchoPacketDirection direction, EchoPacketKind kind,
            String playerName, boolean accepted, String detail) {
        EchoPacketDebugEvent event = new EchoPacketDebugEvent(payloadId, direction, kind, playerName, accepted, detail);
        if (shouldDispatchHooks(accepted)) {
            HOOKS.emit(event);
        }
        if (accepted) {
            if (EchoNetCoreConfig.DEBUG_PACKET_LOGGING.get()) {
                EchoNetCore.LOGGER.info("ECHO packet {} {} {} player={} detail={}",
                        direction, kind, payloadId, event.playerName(), event.detail());
            }
        } else if (EchoNetCoreConfig.LOG_DROPPED_PACKETS.get()) {
            EchoNetCore.LOGGER.info("Dropped ECHO packet {} {} {} player={} detail={}",
                    direction, kind, payloadId, event.playerName(), event.detail());
        }
    }

    private static boolean shouldDispatchHooks(boolean accepted) {
        if (EchoNetCoreConfig.ENABLE_DEBUG_PACKETS.get()) {
            return true;
        }
        return accepted ? EchoNetCoreConfig.DEBUG_PACKET_LOGGING.get() : EchoNetCoreConfig.LOG_DROPPED_PACKETS.get();
    }

    public static void warnHandlerFailure(Identifier payloadId, ServerPlayer player, RuntimeException exception) {
        EchoNetCore.LOGGER.warn("ECHO packet handler {} failed for {}; ignoring packet.",
                payloadId, player == null ? "<null>" : player.getScoreboardName(), exception);
    }

    public static final class PacketHub implements PacketDebugHooks {
        private final List<PacketDebugHook> hooks = new CopyOnWriteArrayList<>();

        @Override
        public void add(PacketDebugHook hook) {
            if (hook != null && !hooks.contains(hook)) {
                hooks.add(hook);
            }
        }

        @Override
        public void remove(PacketDebugHook hook) {
            hooks.remove(hook);
        }

        @Override
        public void emit(EchoPacketDebugEvent event) {
            if (event == null) {
                return;
            }
            for (PacketDebugHook hook : hooks) {
                hook.onPacket(event);
            }
        }
    }
}
