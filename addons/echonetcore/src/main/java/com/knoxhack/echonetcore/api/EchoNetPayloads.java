package com.knoxhack.echonetcore.api;

import com.knoxhack.echocore.api.network.EchoPacketDirection;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.network.EchoNetDebug;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class EchoNetPayloads {
    public static final String VERSION = "1";

    private EchoNetPayloads() {
    }

    public static PayloadRegistrar optional(RegisterPayloadHandlersEvent event) {
        return optional(event, VERSION);
    }

    public static PayloadRegistrar optional(RegisterPayloadHandlersEvent event, String version) {
        return event.registrar(version == null || version.isBlank() ? VERSION : version).optional();
    }

    public static <T extends CustomPacketPayload> PayloadRegistrar clientboundSync(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            ClientboundHandler<T> handler) {
        return clientbound(registrar, type, codec, EchoPacketKind.CLIENTBOUND_SYNC, handler);
    }

    public static <T extends CustomPacketPayload> PayloadRegistrar clientboundSync(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        registrar.playToClient(type, codec);
        return registrar;
    }

    public static <T extends CustomPacketPayload> PayloadRegistrar optionalClientbound(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            ClientboundHandler<T> handler) {
        return clientbound(registrar, type, codec, EchoPacketKind.OPTIONAL_ADDON, handler);
    }

    public static <T extends CustomPacketPayload> PayloadRegistrar serverboundAction(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            EchoRateLimitPolicy rateLimit,
            ServerboundHandler<T> handler) {
        registrar.playToServer(type, codec, (packet, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                EchoNetDebug.emit(type.id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.SERVERBOUND_ACTION,
                        "", false, "non-server-player");
                return;
            }
            EchoRateLimitPolicy policy = rateLimit == null ? EchoRateLimitPolicy.NONE : rateLimit;
            if (!EchoRateLimiter.tryAcquire(player, type.id(), policy)) {
                EchoNetDebug.emit(type.id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.SERVERBOUND_ACTION,
                        player.getScoreboardName(), false, "rate-limited");
                return;
            }
            try {
                EchoNetDebug.emit(type.id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.SERVERBOUND_ACTION,
                        player.getScoreboardName(), true, "received");
                handler.handle(packet, player, context);
            } catch (RuntimeException exception) {
                EchoNetDebug.warnHandlerFailure(type.id(), player, exception);
            }
        }));
        return registrar;
    }

    public static <T extends CustomPacketPayload> PayloadRegistrar debugServerbound(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            EchoRateLimitPolicy rateLimit,
            ServerboundHandler<T> handler) {
        registrar.playToServer(type, codec, (packet, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                EchoNetDebug.emit(type.id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.DEBUG_DEV,
                        "", false, "non-server-player");
                return;
            }
            EchoRateLimitPolicy policy = rateLimit == null ? EchoRateLimitPolicy.NONE : rateLimit;
            if (!EchoRateLimiter.tryAcquire(player, type.id(), policy)) {
                EchoNetDebug.emit(type.id(), EchoPacketDirection.SERVERBOUND, EchoPacketKind.DEBUG_DEV,
                        player.getScoreboardName(), false, "rate-limited");
                return;
            }
            try {
                handler.handle(packet, player, context);
            } catch (RuntimeException exception) {
                EchoNetDebug.warnHandlerFailure(type.id(), player, exception);
            }
        }));
        return registrar;
    }

    private static <T extends CustomPacketPayload> PayloadRegistrar clientbound(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            EchoPacketKind kind,
            ClientboundHandler<T> handler) {
        registrar.playToClient(type, codec, (packet, context) -> context.enqueueWork(() -> {
            Player player = context.player();
            EchoNetDebug.emit(type.id(), EchoPacketDirection.CLIENTBOUND, kind,
                    player == null ? "" : player.getScoreboardName(), true, "received");
            handler.handle(packet, player, context);
        }));
        return registrar;
    }

    @FunctionalInterface
    public interface ClientboundHandler<T extends CustomPacketPayload> {
        void handle(T packet, Player player, IPayloadContext context);
    }

    @FunctionalInterface
    public interface ServerboundHandler<T extends CustomPacketPayload> {
        void handle(T packet, ServerPlayer player, IPayloadContext context);
    }
}
