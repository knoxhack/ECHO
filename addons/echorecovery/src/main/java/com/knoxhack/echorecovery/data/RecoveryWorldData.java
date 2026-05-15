package com.knoxhack.echorecovery.data;

import com.knoxhack.echorecovery.EchoRecovery;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class RecoveryWorldData extends SavedData {
    public static final Codec<RecoveryWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        GraveEntry.CODEC.listOf().optionalFieldOf("graves", List.of()).forGetter(RecoveryWorldData::graveList),
        DeathRecord.CODEC.listOf().optionalFieldOf("history", List.of()).forGetter(RecoveryWorldData::historyList)
    ).apply(instance, RecoveryWorldData::fromCodec));

    public static final SavedDataType<RecoveryWorldData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(EchoRecovery.MODID, "recovery_world_data"),
        RecoveryWorldData::new,
        CODEC
    );

    private final Map<UUID, List<GraveEntry>> playerGraves = new LinkedHashMap<>();
    private final Map<UUID, List<DeathRecord>> deathHistory = new LinkedHashMap<>();

    public RecoveryWorldData() {}

    private RecoveryWorldData(List<GraveEntry> graves, List<DeathRecord> history) {
        for (GraveEntry g : graves) {
            playerGraves.computeIfAbsent(g.ownerId(), k -> new ArrayList<>()).add(g);
        }
        for (DeathRecord r : history) {
            deathHistory.computeIfAbsent(r.playerId(), k -> new ArrayList<>()).add(r);
        }
    }

    private static RecoveryWorldData fromCodec(List<GraveEntry> graves, List<DeathRecord> history) {
        return new RecoveryWorldData(graves, history);
    }

    public List<GraveEntry> graveList() {
        List<GraveEntry> all = new ArrayList<>();
        for (List<GraveEntry> list : playerGraves.values()) {
            all.addAll(list);
        }
        return List.copyOf(all);
    }

    public List<DeathRecord> historyList() {
        List<DeathRecord> all = new ArrayList<>();
        for (List<DeathRecord> list : deathHistory.values()) {
            all.addAll(list);
        }
        return List.copyOf(all);
    }

    public static RecoveryWorldData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public List<GraveEntry> getActiveGraves(UUID playerId) {
        return playerGraves.getOrDefault(playerId, List.of());
    }

    public void addGrave(UUID playerId, GraveEntry grave) {
        playerGraves.computeIfAbsent(playerId, k -> new ArrayList<>()).add(grave);
        setDirty();
    }

    public void removeGrave(UUID playerId, BlockPos pos) {
        List<GraveEntry> graves = playerGraves.get(playerId);
        if (graves != null) {
            graves.removeIf(g -> g.pos().equals(pos));
            if (graves.isEmpty()) {
                playerGraves.remove(playerId);
            }
            setDirty();
        }
    }

    public void addDeathRecord(UUID playerId, DeathRecord record) {
        deathHistory.computeIfAbsent(playerId, k -> new ArrayList<>()).add(record);
        setDirty();
    }

    public List<DeathRecord> getDeathHistory(UUID playerId) {
        return deathHistory.getOrDefault(playerId, List.of());
    }

    public record GraveEntry(UUID graveId, UUID ownerId, String ownerName, BlockPos pos,
                             String dimension, long createdAt, long expiresAt, String deathCause,
                             String deathMessage, int graveType, int xpStored,
                             boolean recovered, boolean expired) {
        public static final Codec<GraveEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("graveId").forGetter(g -> g.graveId().toString()),
            Codec.STRING.fieldOf("ownerId").forGetter(g -> g.ownerId().toString()),
            Codec.STRING.optionalFieldOf("ownerName", "").forGetter(GraveEntry::ownerName),
            BlockPos.CODEC.fieldOf("pos").forGetter(GraveEntry::pos),
            Codec.STRING.optionalFieldOf("dimension", "minecraft:overworld").forGetter(GraveEntry::dimension),
            Codec.LONG.optionalFieldOf("createdAt", 0L).forGetter(GraveEntry::createdAt),
            Codec.LONG.optionalFieldOf("expiresAt", 0L).forGetter(GraveEntry::expiresAt),
            Codec.STRING.optionalFieldOf("deathCause", "").forGetter(GraveEntry::deathCause),
            Codec.STRING.optionalFieldOf("deathMessage", "").forGetter(GraveEntry::deathMessage),
            Codec.INT.optionalFieldOf("graveType", 0).forGetter(GraveEntry::graveType),
            Codec.INT.optionalFieldOf("xpStored", 0).forGetter(GraveEntry::xpStored),
            Codec.BOOL.optionalFieldOf("recovered", false).forGetter(GraveEntry::recovered),
            Codec.BOOL.optionalFieldOf("expired", false).forGetter(GraveEntry::expired)
        ).apply(instance, GraveEntry::fromCodec));

        public static GraveEntry fromCodec(String graveIdStr, String ownerIdStr, String ownerName,
                                           BlockPos pos, String dimension, long createdAt, long expiresAt,
                                           String deathCause, String deathMessage, int graveType,
                                           int xpStored, boolean recovered, boolean expired) {
            return new GraveEntry(UUID.fromString(graveIdStr), UUID.fromString(ownerIdStr), ownerName,
                pos, dimension, createdAt, expiresAt, deathCause, deathMessage, graveType, xpStored, recovered, expired);
        }
    }

    public record DeathRecord(UUID playerId, long time, String cause, String dimension,
                              BlockPos pos, boolean recovered, boolean expired) {
        public static final Codec<DeathRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("playerId").forGetter(r -> r.playerId().toString()),
            Codec.LONG.optionalFieldOf("time", 0L).forGetter(DeathRecord::time),
            Codec.STRING.optionalFieldOf("cause", "").forGetter(DeathRecord::cause),
            Codec.STRING.optionalFieldOf("dimension", "").forGetter(DeathRecord::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(DeathRecord::pos),
            Codec.BOOL.optionalFieldOf("recovered", false).forGetter(DeathRecord::recovered),
            Codec.BOOL.optionalFieldOf("expired", false).forGetter(DeathRecord::expired)
        ).apply(instance, DeathRecord::fromCodec));

        public static DeathRecord fromCodec(String playerIdStr, long time, String cause,
                                            String dimension, BlockPos pos, boolean recovered, boolean expired) {
            return new DeathRecord(UUID.fromString(playerIdStr), time, cause, dimension, pos, recovered, expired);
        }
    }
}
