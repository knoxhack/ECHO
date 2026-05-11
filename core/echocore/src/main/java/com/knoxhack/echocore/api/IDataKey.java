package com.knoxhack.echocore.api;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.resources.Identifier;

/**
 * Typed shared data key registered by an ECHO addon.
 */
public interface IDataKey<T> {
    Identifier id();

    DataScope scope();

    DataValueKind kind();

    Codec<T> codec();

    T defaultValue();

    boolean synced();

    static IDataKey<Boolean> flag(Identifier id, DataScope scope, boolean defaultValue, boolean synced) {
        return of(id, scope, DataValueKind.FLAG, Codec.BOOL, defaultValue, synced);
    }

    static IDataKey<Long> counter(Identifier id, DataScope scope, long defaultValue, boolean synced) {
        return of(id, scope, DataValueKind.COUNTER, Codec.LONG, defaultValue, synced);
    }

    static IDataKey<String> string(Identifier id, DataScope scope, String defaultValue, boolean synced) {
        return of(id, scope, DataValueKind.STRING, Codec.STRING, defaultValue == null ? "" : defaultValue, synced);
    }

    static IDataKey<String> enumName(Identifier id, DataScope scope, String defaultValue, boolean synced) {
        return of(id, scope, DataValueKind.ENUM, Codec.STRING, defaultValue == null ? "" : defaultValue, synced);
    }

    static <T> IDataKey<T> record(Identifier id, DataScope scope, Codec<T> codec, T defaultValue, boolean synced) {
        return of(id, scope, DataValueKind.RECORD, codec, defaultValue, synced);
    }

    static <T> IDataKey<T> of(
            Identifier id,
            DataScope scope,
            DataValueKind kind,
            Codec<T> codec,
            T defaultValue,
            boolean synced) {
        return new SimpleDataKey<>(
                Objects.requireNonNull(id, "Data key id is required."),
                Objects.requireNonNull(scope, "Data key scope is required."),
                Objects.requireNonNull(kind, "Data key kind is required."),
                Objects.requireNonNull(codec, "Data key codec is required."),
                defaultValue,
                synced);
    }

    record SimpleDataKey<T>(
            Identifier id,
            DataScope scope,
            DataValueKind kind,
            Codec<T> codec,
            T defaultValue,
            boolean synced) implements IDataKey<T> {
    }
}
