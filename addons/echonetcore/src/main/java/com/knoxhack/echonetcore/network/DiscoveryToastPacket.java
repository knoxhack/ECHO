package com.knoxhack.echonetcore.network;

import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.api.network.EchoDiscoveryToast;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DiscoveryToastPacket(EchoDiscoveryToast toast) implements CustomPacketPayload {
    private static final int MAX_ID = 160;
    private static final int MAX_TEXT = 512;

    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoCore.MODID, "discovery_toast");
    public static final Type<DiscoveryToastPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, DiscoveryToastPacket> CODEC =
            StreamCodec.of(DiscoveryToastPacket::write, DiscoveryToastPacket::read);

    public DiscoveryToastPacket {
        toast = toast == null ? new EchoDiscoveryToast(null, "", "", "", "", "", 0) : toast;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(FriendlyByteBuf buffer, DiscoveryToastPacket packet) {
        EchoDiscoveryToast toast = packet.toast;
        buffer.writeUtf(toast.featureId().toString(), MAX_ID);
        buffer.writeUtf(toast.category(), MAX_TEXT);
        buffer.writeUtf(toast.title(), MAX_TEXT);
        buffer.writeUtf(toast.subtitle(), MAX_TEXT);
        buffer.writeUtf(toast.iconArt(), MAX_TEXT);
        buffer.writeUtf(toast.heroArt(), MAX_TEXT);
        buffer.writeInt(toast.accentColor());
    }

    private static DiscoveryToastPacket read(FriendlyByteBuf buffer) {
        return new DiscoveryToastPacket(new EchoDiscoveryToast(
                Identifier.tryParse(buffer.readUtf(MAX_ID)),
                buffer.readUtf(MAX_TEXT),
                buffer.readUtf(MAX_TEXT),
                buffer.readUtf(MAX_TEXT),
                buffer.readUtf(MAX_TEXT),
                buffer.readUtf(MAX_TEXT),
                buffer.readInt()));
    }
}
