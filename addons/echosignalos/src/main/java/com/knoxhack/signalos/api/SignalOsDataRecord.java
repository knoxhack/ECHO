package com.knoxhack.signalos.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * Small text-oriented record that can be loaded from datapacks, provided by an
 * addon, stored on a drive, or persisted as an operator note.
 */
public record SignalOsDataRecord(
        Identifier id,
        String title,
        String type,
        String source,
        String body,
        int order,
        boolean archived) {
    public static final Codec<SignalOsDataRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(record -> record.id().toString()),
            Codec.STRING.optionalFieldOf("title", "").forGetter(SignalOsDataRecord::title),
            Codec.STRING.optionalFieldOf("type", "record").forGetter(SignalOsDataRecord::type),
            Codec.STRING.optionalFieldOf("source", "SignalOS").forGetter(SignalOsDataRecord::source),
            Codec.STRING.optionalFieldOf("body", "").forGetter(SignalOsDataRecord::body),
            Codec.INT.optionalFieldOf("order", 0).forGetter(SignalOsDataRecord::order),
            Codec.BOOL.optionalFieldOf("archived", false).forGetter(SignalOsDataRecord::archived)
    ).apply(instance, SignalOsDataRecord::fromCodec));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignalOsDataRecord> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            record -> record.id().toString(),
            ByteBufCodecs.STRING_UTF8,
            SignalOsDataRecord::title,
            ByteBufCodecs.STRING_UTF8,
            SignalOsDataRecord::type,
            ByteBufCodecs.STRING_UTF8,
            SignalOsDataRecord::source,
            ByteBufCodecs.STRING_UTF8,
            SignalOsDataRecord::body,
            ByteBufCodecs.VAR_INT,
            SignalOsDataRecord::order,
            ByteBufCodecs.BOOL,
            SignalOsDataRecord::archived,
            SignalOsDataRecord::fromCodec);

    public SignalOsDataRecord {
        id = TerminalIds.requireLowercase(id, "SignalOS data record");
        title = title == null || title.isBlank() ? id.getPath() : title.strip();
        type = type == null || type.isBlank() ? "record" : type.strip().toLowerCase(java.util.Locale.ROOT);
        source = source == null || source.isBlank() ? id.getNamespace() : source.strip();
        body = body == null ? "" : body.strip();
    }

    public static SignalOsDataRecord of(String id, String title, String type, String source, String body, int order) {
        return new SignalOsDataRecord(TerminalIds.parse(id, "SignalOS data record"), title, type, source, body, order, false);
    }

    private static SignalOsDataRecord fromCodec(String id, String title, String type, String source, String body,
            int order, boolean archived) {
        return new SignalOsDataRecord(TerminalIds.parse(id, "SignalOS data record"), title, type, source, body,
                order, archived);
    }
}
