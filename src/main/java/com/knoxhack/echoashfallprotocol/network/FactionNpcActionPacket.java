package com.knoxhack.echoashfallprotocol.network;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FactionNpcActionPacket(int entityId, String actionId, String targetId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "faction_npc_action");

    public static final StreamCodec<FriendlyByteBuf, FactionNpcActionPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.entityId);
                buf.writeUtf(packet.actionId == null ? "" : packet.actionId);
                buf.writeUtf(packet.targetId == null ? "" : packet.targetId);
            },
            buf -> new FactionNpcActionPacket(buf.readVarInt(), buf.readUtf(), buf.readUtf())
    );

    public static final Type<FactionNpcActionPacket> TYPE = new Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
