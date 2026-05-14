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
    public static final int MAX_PLAYER_RECORDS = 64;

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

    public SignalOsDriveData withLabel(String nextLabel) {
        return new SignalOsDriveData(nextLabel, records);
    }

    public SignalOsDriveData withRecord(SignalOsDataRecord record) {
        return withRecord(record, Integer.MAX_VALUE);
    }

    public SignalOsDriveData withRecord(SignalOsDataRecord record, int maxRecords) {
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
        trim(next, maxRecords);
        return new SignalOsDriveData(label, next);
    }

    public SignalOsDriveData withoutRecord(net.minecraft.resources.Identifier recordId) {
        if (recordId == null || records.isEmpty()) {
            return this;
        }
        java.util.ArrayList<SignalOsDataRecord> next = new java.util.ArrayList<>();
        for (SignalOsDataRecord record : records) {
            if (!record.id().equals(recordId)) {
                next.add(record);
            }
        }
        return new SignalOsDriveData(label, next);
    }

    public SignalOsDriveData clearRecords() {
        return new SignalOsDriveData(label, List.of());
    }

    public SignalOsDriveData merge(SignalOsDriveData template, int maxRecords) {
        if (template == null || template.records().isEmpty()) {
            return this;
        }
        SignalOsDriveData next = this;
        for (SignalOsDataRecord record : template.records()) {
            next = next.withRecord(record, maxRecords);
        }
        return next;
    }

    private static void trim(java.util.ArrayList<SignalOsDataRecord> records, int maxRecords) {
        int safeMax = Math.max(0, maxRecords);
        while (records.size() > safeMax) {
            records.removeFirst();
        }
    }
}
