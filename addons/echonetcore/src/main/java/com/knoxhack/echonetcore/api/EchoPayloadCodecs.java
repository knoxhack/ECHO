package com.knoxhack.echonetcore.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public final class EchoPayloadCodecs {
    public static final int ID = 160;
    public static final int SMALL_TEXT = 512;
    public static final int ACTION_PAYLOAD = 4096;

    private EchoPayloadCodecs() {
    }

    public static void writeIdentifier(FriendlyByteBuf buffer, Identifier id) {
        writeUtf(buffer, id == null ? "" : id.toString(), ID);
    }

    public static Identifier readIdentifier(FriendlyByteBuf buffer) {
        Identifier id = Identifier.tryParse(readUtf(buffer, ID));
        return id == null ? Identifier.fromNamespaceAndPath("echonetcore", "unknown") : id;
    }

    public static void writeUtf(FriendlyByteBuf buffer, String value, int maxLength) {
        buffer.writeUtf(value == null ? "" : value, Math.max(1, maxLength));
    }

    public static String readUtf(FriendlyByteBuf buffer, int maxLength) {
        return buffer.readUtf(Math.max(1, maxLength)).trim();
    }
}
