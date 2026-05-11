package com.knoxhack.echodatacore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class DataCoreWorldData extends SavedData {
    public static final Codec<DataCoreWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("version", DataCoreDataService.CURRENT_VERSION).forGetter(data -> data.version),
            Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).optionalFieldOf("worldValues", Map.of()).forGetter(data -> data.worldValues),
            Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).optionalFieldOf("teamValues", Map.of()).forGetter(data -> data.teamValues),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("migrations", Map.of()).forGetter(data -> data.migrations)
    ).apply(instance, (version, worldValues, teamValues, migrations) -> {
        DataCoreWorldData data = new DataCoreWorldData();
        data.version = Math.max(0, version);
        data.worldValues.putAll(copyMap(worldValues));
        data.teamValues.putAll(copyMap(teamValues));
        data.migrations.putAll(migrations);
        return data;
    }));

    public static final SavedDataType<DataCoreWorldData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(EchoDataCore.MODID, "data_world"), DataCoreWorldData::new, CODEC);

    private int version = DataCoreDataService.CURRENT_VERSION;
    private final Map<String, CompoundTag> worldValues = new LinkedHashMap<>();
    private final Map<String, CompoundTag> teamValues = new LinkedHashMap<>();
    private final Map<String, Integer> migrations = new LinkedHashMap<>();

    public static DataCoreWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int version() {
        return version;
    }

    public void ensureVersion() {
        if (version < DataCoreDataService.CURRENT_VERSION) {
            version = DataCoreDataService.CURRENT_VERSION;
            setDirty();
        }
    }

    public CompoundTag worldValue(String key) {
        CompoundTag value = worldValues.get(key);
        return value == null ? null : value.copy();
    }

    public boolean putWorldValue(String key, CompoundTag value) {
        CompoundTag safe = value == null ? new CompoundTag() : value.copy();
        CompoundTag previous = worldValues.get(key);
        if (safe.equals(previous)) {
            return false;
        }
        worldValues.put(key, safe);
        setDirty();
        return true;
    }

    public boolean removeWorldValue(String key) {
        if (worldValues.remove(key) == null) {
            return false;
        }
        setDirty();
        return true;
    }

    public CompoundTag teamValue(Identifier teamId, String key) {
        CompoundTag value = teamValues.get(teamKey(teamId, key));
        return value == null ? null : value.copy();
    }

    public boolean putTeamValue(Identifier teamId, String key, CompoundTag value) {
        String fullKey = teamKey(teamId, key);
        CompoundTag safe = value == null ? new CompoundTag() : value.copy();
        CompoundTag previous = teamValues.get(fullKey);
        if (safe.equals(previous)) {
            return false;
        }
        teamValues.put(fullKey, safe);
        setDirty();
        return true;
    }

    public boolean removeTeamValue(Identifier teamId, String key) {
        if (teamValues.remove(teamKey(teamId, key)) == null) {
            return false;
        }
        setDirty();
        return true;
    }

    public Map<String, CompoundTag> worldSnapshot() {
        return copyMap(worldValues);
    }

    public Map<String, CompoundTag> teamSnapshot(Identifier teamId) {
        String prefix = teamId.toString() + "|";
        Map<String, CompoundTag> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, CompoundTag> entry : teamValues.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                snapshot.put(entry.getKey().substring(prefix.length()), entry.getValue().copy());
            }
        }
        return snapshot;
    }

    private static String teamKey(Identifier teamId, String key) {
        return (teamId == null ? "echodatacore:unknown" : teamId.toString()) + "|" + key;
    }

    private static Map<String, CompoundTag> copyMap(Map<String, CompoundTag> source) {
        Map<String, CompoundTag> copy = new LinkedHashMap<>();
        for (Map.Entry<String, CompoundTag> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return copy;
    }
}
