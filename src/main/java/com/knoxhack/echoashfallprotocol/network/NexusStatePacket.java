package com.knoxhack.echoashfallprotocol.network;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.world.NexusWorldData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Network packet to sync Nexus world state from server to clients.
 */
public record NexusStatePacket(
    String worldState,
    long choiceTime,
    String playerName,
    int nexusX,
    int nexusY,
    int nexusZ
) implements CustomPacketPayload {
    
    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "nexus_state");
    
    public static final StreamCodec<FriendlyByteBuf, NexusStatePacket> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeUtf(packet.worldState);
            buf.writeLong(packet.choiceTime);
            buf.writeUtf(packet.playerName);
            buf.writeInt(packet.nexusX);
            buf.writeInt(packet.nexusY);
            buf.writeInt(packet.nexusZ);
        },
        buf -> new NexusStatePacket(
            buf.readUtf(),
            buf.readLong(),
            buf.readUtf(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt()
        )
    );
    
    public static final Type<NexusStatePacket> TYPE = new Type<>(ID);
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static NexusStatePacket fromWorldData(NexusWorldData data) {
        return new NexusStatePacket(
            data.getState().name(),
            data.getChoiceTime(),
            data.getPlayerName(),
            data.getNexusPos().getX(),
            data.getNexusPos().getY(),
            data.getNexusPos().getZ()
        );
    }
    
    public NexusWorldData.WorldState getState() {
        try {
            return NexusWorldData.WorldState.valueOf(worldState);
        } catch (IllegalArgumentException e) {
            return NexusWorldData.WorldState.NORMAL;
        }
    }
}
