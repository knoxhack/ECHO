package com.knoxhack.echoashfallprotocol.network;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client request to analyze one schematic fragment in an open Research Lab.
 */
public record ResearchAnalyzeFragmentPacket(String schematicType) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "research_analyze_fragment");

    public static final StreamCodec<FriendlyByteBuf, ResearchAnalyzeFragmentPacket> CODEC = StreamCodec.of(
        (buf, packet) -> buf.writeUtf(packet.schematicType),
        buf -> new ResearchAnalyzeFragmentPacket(buf.readUtf())
    );

    public static final Type<ResearchAnalyzeFragmentPacket> TYPE = new Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
