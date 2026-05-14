package com.knoxhack.echotutorialcore.network;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncTutorialProgressPacket(String guideMode, java.util.List<String> progressFlags) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncTutorialProgressPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(EchoTutorialCore.MODID, "sync_progress"));

    public static final StreamCodec<ByteBuf, SyncTutorialProgressPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncTutorialProgressPacket::guideMode,
            ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.STRING_UTF8), SyncTutorialProgressPacket::progressFlags,
            SyncTutorialProgressPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
