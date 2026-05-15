package com.knoxhack.echorecovery.net;

import com.knoxhack.echorecovery.EchoRecovery;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RecoveryPackets {
    public static final Identifier GRAVE_SYNC_ID = Identifier.fromNamespaceAndPath(EchoRecovery.MODID, "grave_sync");
    public static final Identifier COMPASS_SYNC_ID = Identifier.fromNamespaceAndPath(EchoRecovery.MODID, "compass_sync");

    private RecoveryPackets() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RecoveryPackets::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToServer(RecoverAllPacket.TYPE, RecoverAllPacket.CODEC, RecoverAllPacket::handle);
    }

    public record GraveSyncPacket() implements CustomPacketPayload {
        public static final Type<GraveSyncPacket> TYPE = new Type<>(GRAVE_SYNC_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, GraveSyncPacket> CODEC = StreamCodec.unit(new GraveSyncPacket());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
