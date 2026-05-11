package com.knoxhack.echocore.api;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

/**
 * Common read/write surface for player, world, and team shared data.
 */
public interface IDataView {
    DataScope scope();

    <T> T get(IDataKey<T> key);

    <T> boolean set(IDataKey<T> key, T value);

    boolean clear(IDataKey<?> key);

    boolean has(IDataKey<?> key);

    CompoundTag record(Identifier id);

    boolean putRecord(Identifier id, CompoundTag value);

    Map<Identifier, String> debugSnapshot();

    default boolean flag(Identifier id) {
        return get(IDataKey.flag(id, scope(), false, true));
    }

    default boolean setFlag(Identifier id, boolean value) {
        return set(IDataKey.flag(id, scope(), false, true), value);
    }

    default long counter(Identifier id) {
        return get(IDataKey.counter(id, scope(), 0L, true));
    }

    default long addCounter(Identifier id, long delta) {
        long next = Math.max(0L, counter(id) + delta);
        set(IDataKey.counter(id, scope(), 0L, true), next);
        return next;
    }

    default String string(Identifier id) {
        return get(IDataKey.string(id, scope(), "", true));
    }

    default boolean setString(Identifier id, String value) {
        return set(IDataKey.string(id, scope(), "", true), value == null ? "" : value);
    }

    default String enumName(Identifier id) {
        return get(IDataKey.enumName(id, scope(), "", true));
    }

    default boolean setEnumName(Identifier id, String value) {
        return set(IDataKey.enumName(id, scope(), "", true), value == null ? "" : value);
    }
}
