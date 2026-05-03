package com.knoxhack.echoashfallprotocol.network;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client request to commit the shared Nexus Core choice.
 */
public record NexusChoicePacket(String choice) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "nexus_choice");

    public static final StreamCodec<FriendlyByteBuf, NexusChoicePacket> CODEC = StreamCodec.of(
        (buf, packet) -> buf.writeUtf(packet.choice),
        buf -> new NexusChoicePacket(buf.readUtf())
    );

    public static final Type<NexusChoicePacket> TYPE = new Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
