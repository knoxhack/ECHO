package com.knoxhack.echocore.network;

import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.api.EchoDiscoveryEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DiscoveryToastPacket(
        Identifier featureId,
        String category,
        String title,
        String subtitle,
        String iconArt,
        String heroArt,
        int accentColor) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoCore.MODID, "discovery_toast");
    public static final Type<DiscoveryToastPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, DiscoveryToastPacket> CODEC = StreamCodec.of(
            DiscoveryToastPacket::write,
            DiscoveryToastPacket::read);

    public DiscoveryToastPacket(EchoDiscoveryEntry entry) {
        this(entry.id(),
                entry.category().displayName(),
                entry.revealedTitle(),
                "Added to Discovery Grid",
                art(entry.iconArt()),
                art(entry.heroArt()),
                entry.accentColor());
    }

    public DiscoveryToastPacket {
        featureId = featureId == null ? Identifier.fromNamespaceAndPath(EchoCore.MODID, "unknown") : featureId;
        category = safe(category);
        title = safe(title);
        subtitle = safe(subtitle);
        iconArt = safe(iconArt);
        heroArt = safe(heroArt);
        accentColor = accentColor == 0 ? 0xFF66E8FF : accentColor;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(FriendlyByteBuf buf, DiscoveryToastPacket packet) {
        buf.writeUtf(packet.featureId.toString());
        buf.writeUtf(packet.category);
        buf.writeUtf(packet.title);
        buf.writeUtf(packet.subtitle);
        buf.writeUtf(packet.iconArt);
        buf.writeUtf(packet.heroArt);
        buf.writeInt(packet.accentColor);
    }

    private static DiscoveryToastPacket read(FriendlyByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readUtf());
        return new DiscoveryToastPacket(
                id,
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt());
    }

    private static String art(Identifier id) {
        return id == null ? "" : id.toString();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
