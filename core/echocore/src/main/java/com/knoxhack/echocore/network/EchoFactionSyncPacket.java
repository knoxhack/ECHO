package com.knoxhack.echocore.network;

import com.knoxhack.echocore.EchoCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Mirrors server-owned Echo Core faction persistent data to the logical client.
 */
public record EchoFactionSyncPacket(CompoundTag factionRoot) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoCore.MODID, "faction_sync");
    public static final Type<EchoFactionSyncPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, EchoFactionSyncPacket> CODEC = StreamCodec.of(
            EchoFactionSyncPacket::write,
            EchoFactionSyncPacket::read
    );

    public EchoFactionSyncPacket {
        factionRoot = factionRoot == null ? new CompoundTag() : factionRoot.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(FriendlyByteBuf buf, EchoFactionSyncPacket packet) {
        buf.writeNbt(packet.factionRoot);
    }

    private static EchoFactionSyncPacket read(FriendlyByteBuf buf) {
        return new EchoFactionSyncPacket(buf.readNbt());
    }
}
