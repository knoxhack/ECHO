package com.knoxhack.signalos.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Persistent data component carried by SignalOS data drive items.
 */
public record SignalOsDriveData(String label, List<SignalOsDataRecord> records) {
    public static final SignalOsDriveData EMPTY = new SignalOsDriveData("Blank Drive", List.of());

    public static final Codec<SignalOsDriveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("label", "Blank Drive").forGetter(SignalOsDriveData::label),
            SignalOsDataRecord.CODEC.listOf().optionalFieldOf("records", List.of()).forGetter(SignalOsDriveData::records)
    ).apply(instance, SignalOsDriveData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignalOsDriveData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SignalOsDriveData::label,
            SignalOsDataRecord.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SignalOsDriveData::records,
            SignalOsDriveData::new);

    public SignalOsDriveData {
        label = label == null || label.isBlank() ? "Blank Drive" : label.strip();
        records = List.copyOf(records == null ? List.of() : records);
    }

    public SignalOsDriveData withRecord(SignalOsDataRecord record) {
        if (record == null) {
            return this;
        }
        java.util.ArrayList<SignalOsDataRecord> next = new java.util.ArrayList<>();
        boolean replaced = false;
        for (SignalOsDataRecord existing : records) {
            if (existing.id().equals(record.id())) {
                next.add(record);
                replaced = true;
            } else {
                next.add(existing);
            }
        }
        if (!replaced) {
            next.add(record);
        }
        return new SignalOsDriveData(label, next);
    }
}
