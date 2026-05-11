package com.knoxhack.echodatacore;

import com.knoxhack.echocore.api.DataChangeMessage;
import com.knoxhack.echocore.api.DataScope;
import com.knoxhack.echocore.api.DataValueKind;
import com.knoxhack.echocore.api.EchoDataBus;
import com.knoxhack.echocore.api.IDataKey;
import com.knoxhack.echocore.api.IDataService;
import com.knoxhack.echocore.api.IDataSyncBridge;
import com.knoxhack.echocore.api.IPlayerDataView;
import com.knoxhack.echocore.api.ITeamDataView;
import com.knoxhack.echocore.api.IWorldDataView;
import com.knoxhack.echodatacore.legacy.DataCoreLegacyAdapters;
import com.knoxhack.echodatacore.network.DataCoreSyncPacket;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class DataCoreDataService implements IDataService {
    public static final int CURRENT_VERSION = 1;
    public static final String PLAYER_ROOT = "echodatacore";
    private static final String VALUES = "values";
    private static final String VALUE = "value";
    private static final String KIND = "kind";
    private static final String UPDATED = "updatedGameTime";
    private static final Identifier UNKNOWN_ID = Identifier.fromNamespaceAndPath(EchoDataCore.MODID, "unknown");

    public static final DataCoreDataService INSTANCE = new DataCoreDataService();

    private final Map<Identifier, IDataKey<?>> keys = new ConcurrentHashMap<>();
    private final Map<UUID, LinkedHashSet<Identifier>> dirtyPlayerKeys = new ConcurrentHashMap<>();
    private final Map<String, Map<Identifier, CompoundTag>> clientPlayerValues = new ConcurrentHashMap<>();
    private final Map<String, Map<Identifier, CompoundTag>> clientWorldValues = new ConcurrentHashMap<>();
    private final Map<String, Map<Identifier, CompoundTag>> clientTeamValues = new ConcurrentHashMap<>();
    private final DataCoreSyncBridge syncBridge = new DataCoreSyncBridge();
    private volatile long revision;

    private DataCoreDataService() {
    }

    @Override
    public <T> IDataKey<T> registerKey(IDataKey<T> key) {
        if (key == null) {
            throw new IllegalArgumentException("Data key is required.");
        }
        IDataKey<?> existing = keys.putIfAbsent(key.id(), key);
        if (existing == null) {
            return key;
        }
        if (existing.kind() != key.kind() || existing.scope() != key.scope()) {
            EchoDataCore.LOGGER.warn("DataCore key {} already registered as {}/{}; keeping first definition.",
                    key.id(), existing.scope(), existing.kind());
        }
        @SuppressWarnings("unchecked")
        IDataKey<T> typed = (IDataKey<T>) existing;
        return typed;
    }

    @Override
    public Optional<IDataKey<?>> key(Identifier id) {
        return Optional.ofNullable(id == null ? null : keys.get(id));
    }

    @Override
    public List<IDataKey<?>> registeredKeys() {
        return keys.values().stream()
                .sorted(Comparator.comparing(dataKey -> dataKey.id().toString()))
                .toList();
    }

    @Override
    public IPlayerDataView player(Player player) {
        return new PlayerView(player);
    }

    @Override
    public IWorldDataView world(Level level) {
        return new WorldView(level);
    }

    @Override
    public ITeamDataView team(Level level, Identifier teamId) {
        return new TeamView(level, teamId == null ? UNKNOWN_ID : teamId);
    }

    @Override
    public IDataSyncBridge syncBridge() {
        return syncBridge;
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        migratePlayer(player);
        player(player).set(DataCoreBuiltinKeys.TERMINAL_PROBE, "online");
        player(player).set(DataCoreBuiltinKeys.PLAYER_SCHEMA_VERSION, (long) CURRENT_VERSION);
        syncBridge.requestFullSync(player);
    }

    public void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData().getCompoundOrEmpty(PLAYER_ROOT);
        if (!original.isEmpty()) {
            event.getEntity().getPersistentData().put(PLAYER_ROOT, original.copy());
        }
    }

    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }
        int interval = Math.max(1, Config.SYNC_INTERVAL_TICKS.get());
        if (player.tickCount % interval == 0) {
            syncBridge.flushDirty(player, false);
        }
    }

    public void applyClientSync(DataCoreSyncPacket packet) {
        Map<String, Map<Identifier, CompoundTag>> target = switch (packet.scope()) {
            case PLAYER -> clientPlayerValues;
            case WORLD -> clientWorldValues;
            case TEAM -> clientTeamValues;
        };
        Map<Identifier, CompoundTag> ownerValues = target.computeIfAbsent(packet.ownerId(), ignored -> new ConcurrentHashMap<>());
        if (packet.fullSnapshot()) {
            ownerValues.clear();
        }
        for (DataCoreSyncPacket.Entry entry : packet.entries()) {
            ownerValues.put(entry.keyId(), entry.data().copy());
            EchoDataBus.publish(new DataChangeMessage(packet.scope(), packet.ownerId(), entry.keyId(),
                    entry.kind(), packet.revision(), packet.fullSnapshot()));
        }
    }

    public int debugDirtyPlayerKeyCount(UUID playerId) {
        LinkedHashSet<Identifier> dirty = dirtyPlayerKeys.get(playerId);
        return dirty == null ? 0 : dirty.size();
    }

    private void migratePlayer(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompoundOrEmpty(PLAYER_ROOT);
        int version = root.getIntOr("version", 0);
        if (version >= CURRENT_VERSION) {
            return;
        }
        root.putInt("version", CURRENT_VERSION);
        CompoundTag migrations = root.getCompoundOrEmpty("migrations");
        migrations.putInt(EchoDataCore.MODID, CURRENT_VERSION);
        root.put("migrations", migrations);
        player.getPersistentData().put(PLAYER_ROOT, root);
    }

    private CompoundTag clientValue(DataScope scope, String ownerId, Identifier keyId) {
        Map<String, Map<Identifier, CompoundTag>> source = switch (scope) {
            case PLAYER -> clientPlayerValues;
            case WORLD -> clientWorldValues;
            case TEAM -> clientTeamValues;
        };
        Map<Identifier, CompoundTag> values = source.get(ownerId);
        if (values == null) {
            return null;
        }
        CompoundTag entry = values.get(keyId);
        return entry == null ? null : entry.copy();
    }

    private static CompoundTag playerRoot(Player player) {
        return player == null ? new CompoundTag() : player.getPersistentData().getCompoundOrEmpty(PLAYER_ROOT);
    }

    private static CompoundTag valuesRoot(CompoundTag root) {
        return root.getCompoundOrEmpty(VALUES);
    }

    private static long gameTime(Player player) {
        return player == null || player.level() == null ? 0L : player.level().getGameTime();
    }

    private static long gameTime(Level level) {
        return level == null ? 0L : level.getGameTime();
    }

    private static <T> CompoundTag entryFor(IDataKey<T> key, T value, long gameTime) {
        CompoundTag entry = new CompoundTag();
        entry.putString(KIND, key.kind().name());
        entry.putLong(UPDATED, Math.max(0L, gameTime));
        T safeValue = value == null ? key.defaultValue() : value;
        try {
            entry.store(VALUE, key.codec(), NbtOps.INSTANCE, safeValue);
        } catch (RuntimeException exception) {
            EchoDataCore.LOGGER.warn("DataCore failed to encode key {}; storing default.", key.id(), exception);
            entry.store(VALUE, key.codec(), NbtOps.INSTANCE, key.defaultValue());
        }
        return entry;
    }

    private static <T> T decode(IDataKey<T> key, CompoundTag entry) {
        if (entry == null || !entry.contains(VALUE)) {
            return key.defaultValue();
        }
        try {
            Optional<T> decoded = entry.read(VALUE, key.codec(), NbtOps.INSTANCE);
            return decoded.orElse(key.defaultValue());
        } catch (RuntimeException exception) {
            EchoDataCore.LOGGER.warn("DataCore failed to decode key {}; using default.", key.id(), exception);
            return key.defaultValue();
        }
    }

    private static boolean sameStoredValue(CompoundTag existing, CompoundTag replacement) {
        if (existing == null || replacement == null) {
            return false;
        }
        Tag existingValue = existing.get(VALUE);
        Tag replacementValue = replacement.get(VALUE);
        return Objects.equals(existing.getStringOr(KIND, ""), replacement.getStringOr(KIND, ""))
                && Objects.equals(existingValue, replacementValue);
    }

    private static Map<Identifier, String> debugEntries(Map<String, CompoundTag> entries) {
        Map<Identifier, String> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, CompoundTag> entry : entries.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id != null) {
                snapshot.put(id, debugValue(entry.getValue()));
            }
        }
        return snapshot;
    }

    private static String debugValue(CompoundTag entry) {
        if (entry == null) {
            return "";
        }
        Tag value = entry.get(VALUE);
        return value == null ? entry.toString() : value.toString();
    }

    private static DataValueKind kindOf(IDataKey<?> key, CompoundTag entry) {
        if (key != null) {
            return key.kind();
        }
        try {
            return DataValueKind.valueOf(entry.getStringOr(KIND, DataValueKind.RECORD.name()));
        } catch (RuntimeException ignored) {
            return DataValueKind.RECORD;
        }
    }

    private static Identifier safeIdentifier(String value) {
        Identifier id = Identifier.tryParse(value == null ? "" : value);
        return id == null ? UNKNOWN_ID : id;
    }

    private final class PlayerView implements IPlayerDataView {
        private final Player player;

        private PlayerView(Player player) {
            this.player = player;
        }

        @Override
        public UUID playerId() {
            return player == null ? new UUID(0L, 0L) : player.getUUID();
        }

        @Override
        public <T> T get(IDataKey<T> key) {
            if (key == null) {
                return null;
            }
            registerKey(key);
            if (player == null) {
                return key.defaultValue();
            }
            if (player.level().isClientSide()) {
                return decode(key, clientValue(DataScope.PLAYER, player.getUUID().toString(), key.id()));
            }
            Optional<CompoundTag> legacy = DataCoreLegacyAdapters.read(player, key);
            if (legacy.isPresent()) {
                return decode(key, legacy.get());
            }
            return decode(key, valuesRoot(playerRoot(player)).getCompoundOrEmpty(key.id().toString()));
        }

        @Override
        public <T> boolean set(IDataKey<T> key, T value) {
            if (key == null || player == null || player.level().isClientSide()) {
                return false;
            }
            IDataKey<T> registered = registerKey(key);
            CompoundTag root = playerRoot(player);
            CompoundTag values = valuesRoot(root);
            CompoundTag replacement = entryFor(registered, value, gameTime(player));
            CompoundTag existing = values.getCompoundOrEmpty(registered.id().toString());
            if (sameStoredValue(existing, replacement)) {
                return false;
            }
            values.put(registered.id().toString(), replacement);
            root.put(VALUES, values);
            root.putInt("version", CURRENT_VERSION);
            player.getPersistentData().put(PLAYER_ROOT, root);
            dirtyPlayerKeys.computeIfAbsent(player.getUUID(), ignored -> new LinkedHashSet<>()).add(registered.id());
            syncBridge.markDirty(DataScope.PLAYER, player.getUUID().toString(), registered.id());
            return true;
        }

        @Override
        public boolean clear(IDataKey<?> key) {
            if (key == null || player == null || player.level().isClientSide()) {
                return false;
            }
            CompoundTag root = playerRoot(player);
            CompoundTag values = valuesRoot(root);
            if (!values.contains(key.id().toString())) {
                return false;
            }
            values.remove(key.id().toString());
            root.put(VALUES, values);
            player.getPersistentData().put(PLAYER_ROOT, root);
            dirtyPlayerKeys.computeIfAbsent(player.getUUID(), ignored -> new LinkedHashSet<>()).add(key.id());
            syncBridge.markDirty(DataScope.PLAYER, player.getUUID().toString(), key.id());
            return true;
        }

        @Override
        public boolean has(IDataKey<?> key) {
            if (key == null || player == null) {
                return false;
            }
            if (player.level().isClientSide()) {
                return clientValue(DataScope.PLAYER, player.getUUID().toString(), key.id()) != null;
            }
            return DataCoreLegacyAdapters.read(player, key).isPresent()
                    || valuesRoot(playerRoot(player)).contains(key.id().toString());
        }

        @Override
        public CompoundTag record(Identifier id) {
            return get(IDataKey.record(id, DataScope.PLAYER, CompoundTag.CODEC, new CompoundTag(), true)).copy();
        }

        @Override
        public boolean putRecord(Identifier id, CompoundTag value) {
            return set(IDataKey.record(id, DataScope.PLAYER, CompoundTag.CODEC, new CompoundTag(), true),
                    value == null ? new CompoundTag() : value.copy());
        }

        @Override
        public Map<Identifier, String> debugSnapshot() {
            Map<Identifier, String> snapshot = new LinkedHashMap<>();
            if (player == null) {
                return snapshot;
            }
            if (!player.level().isClientSide()) {
                snapshot.putAll(DataCoreLegacyAdapters.snapshot(player));
                snapshot.putAll(debugEntries(compoundMap(valuesRoot(playerRoot(player)))));
                return snapshot;
            }
            Map<Identifier, CompoundTag> client = clientPlayerValues.getOrDefault(player.getUUID().toString(), Map.of());
            for (Map.Entry<Identifier, CompoundTag> entry : client.entrySet()) {
                snapshot.put(entry.getKey(), debugValue(entry.getValue()));
            }
            return snapshot;
        }
    }

    private final class WorldView implements IWorldDataView {
        private final Level level;

        private WorldView(Level level) {
            this.level = level;
        }

        @Override
        public Identifier dimensionId() {
            return level == null ? UNKNOWN_ID : level.dimension().identifier();
        }

        @Override
        public <T> T get(IDataKey<T> key) {
            if (key == null) {
                return null;
            }
            registerKey(key);
            if (level instanceof ServerLevel serverLevel) {
                DataCoreWorldData data = DataCoreWorldData.get(serverLevel);
                data.ensureVersion();
                return decode(key, data.worldValue(key.id().toString()));
            }
            return decode(key, clientValue(DataScope.WORLD, dimensionId().toString(), key.id()));
        }

        @Override
        public <T> boolean set(IDataKey<T> key, T value) {
            if (key == null || !(level instanceof ServerLevel serverLevel)) {
                return false;
            }
            IDataKey<T> registered = registerKey(key);
            CompoundTag entry = entryFor(registered, value, gameTime(level));
            boolean changed = DataCoreWorldData.get(serverLevel).putWorldValue(registered.id().toString(), entry);
            if (changed) {
                syncBridge.markDirty(DataScope.WORLD, dimensionId().toString(), registered.id());
            }
            return changed;
        }

        @Override
        public boolean clear(IDataKey<?> key) {
            if (key == null || !(level instanceof ServerLevel serverLevel)) {
                return false;
            }
            boolean changed = DataCoreWorldData.get(serverLevel).removeWorldValue(key.id().toString());
            if (changed) {
                syncBridge.markDirty(DataScope.WORLD, dimensionId().toString(), key.id());
            }
            return changed;
        }

        @Override
        public boolean has(IDataKey<?> key) {
            if (key == null) {
                return false;
            }
            if (level instanceof ServerLevel serverLevel) {
                return DataCoreWorldData.get(serverLevel).worldValue(key.id().toString()) != null;
            }
            return clientValue(DataScope.WORLD, dimensionId().toString(), key.id()) != null;
        }

        @Override
        public CompoundTag record(Identifier id) {
            return get(IDataKey.record(id, DataScope.WORLD, CompoundTag.CODEC, new CompoundTag(), true)).copy();
        }

        @Override
        public boolean putRecord(Identifier id, CompoundTag value) {
            return set(IDataKey.record(id, DataScope.WORLD, CompoundTag.CODEC, new CompoundTag(), true),
                    value == null ? new CompoundTag() : value.copy());
        }

        @Override
        public Map<Identifier, String> debugSnapshot() {
            if (level instanceof ServerLevel serverLevel) {
                return debugEntries(DataCoreWorldData.get(serverLevel).worldSnapshot());
            }
            Map<Identifier, String> snapshot = new LinkedHashMap<>();
            Map<Identifier, CompoundTag> client = clientWorldValues.getOrDefault(dimensionId().toString(), Map.of());
            for (Map.Entry<Identifier, CompoundTag> entry : client.entrySet()) {
                snapshot.put(entry.getKey(), debugValue(entry.getValue()));
            }
            return snapshot;
        }
    }

    private final class TeamView implements ITeamDataView {
        private final Level level;
        private final Identifier teamId;

        private TeamView(Level level, Identifier teamId) {
            this.level = level;
            this.teamId = teamId;
        }

        @Override
        public Identifier teamId() {
            return teamId;
        }

        @Override
        public <T> T get(IDataKey<T> key) {
            if (key == null) {
                return null;
            }
            registerKey(key);
            if (level instanceof ServerLevel serverLevel) {
                return decode(key, DataCoreWorldData.get(serverLevel).teamValue(teamId, key.id().toString()));
            }
            return decode(key, clientValue(DataScope.TEAM, teamId.toString(), key.id()));
        }

        @Override
        public <T> boolean set(IDataKey<T> key, T value) {
            if (key == null || !(level instanceof ServerLevel serverLevel)) {
                return false;
            }
            IDataKey<T> registered = registerKey(key);
            CompoundTag entry = entryFor(registered, value, gameTime(level));
            boolean changed = DataCoreWorldData.get(serverLevel).putTeamValue(teamId, registered.id().toString(), entry);
            if (changed) {
                syncBridge.markDirty(DataScope.TEAM, teamId.toString(), registered.id());
            }
            return changed;
        }

        @Override
        public boolean clear(IDataKey<?> key) {
            if (key == null || !(level instanceof ServerLevel serverLevel)) {
                return false;
            }
            boolean changed = DataCoreWorldData.get(serverLevel).removeTeamValue(teamId, key.id().toString());
            if (changed) {
                syncBridge.markDirty(DataScope.TEAM, teamId.toString(), key.id());
            }
            return changed;
        }

        @Override
        public boolean has(IDataKey<?> key) {
            if (key == null) {
                return false;
            }
            if (level instanceof ServerLevel serverLevel) {
                return DataCoreWorldData.get(serverLevel).teamValue(teamId, key.id().toString()) != null;
            }
            return clientValue(DataScope.TEAM, teamId.toString(), key.id()) != null;
        }

        @Override
        public CompoundTag record(Identifier id) {
            return get(IDataKey.record(id, DataScope.TEAM, CompoundTag.CODEC, new CompoundTag(), true)).copy();
        }

        @Override
        public boolean putRecord(Identifier id, CompoundTag value) {
            return set(IDataKey.record(id, DataScope.TEAM, CompoundTag.CODEC, new CompoundTag(), true),
                    value == null ? new CompoundTag() : value.copy());
        }

        @Override
        public Map<Identifier, String> debugSnapshot() {
            if (level instanceof ServerLevel serverLevel) {
                return debugEntries(DataCoreWorldData.get(serverLevel).teamSnapshot(teamId));
            }
            Map<Identifier, String> snapshot = new LinkedHashMap<>();
            Map<Identifier, CompoundTag> client = clientTeamValues.getOrDefault(teamId.toString(), Map.of());
            for (Map.Entry<Identifier, CompoundTag> entry : client.entrySet()) {
                snapshot.put(entry.getKey(), debugValue(entry.getValue()));
            }
            return snapshot;
        }
    }

    private final class DataCoreSyncBridge implements IDataSyncBridge {
        @Override
        public void requestFullSync(ServerPlayer player) {
            flushDirty(player, true);
        }

        @Override
        public void markDirty(DataScope scope, String ownerId, Identifier keyId) {
            revision++;
            EchoDataBus.publish(new DataChangeMessage(scope, ownerId, keyId,
                    key(keyId).map(IDataKey::kind).orElse(DataValueKind.RECORD), revision, false));
        }

        @Override
        public long revision() {
            return revision;
        }

        private void flushDirty(ServerPlayer player, boolean fullSnapshot) {
            if (player == null) {
                return;
            }
            List<DataCoreSyncPacket.Entry> playerEntries = fullSnapshot
                    ? syncedEntries(valuesRoot(playerRoot(player)), null)
                    : dirtyEntries(player);
            if (fullSnapshot || !playerEntries.isEmpty()) {
                send(player, new DataCoreSyncPacket(DataScope.PLAYER, player.getUUID().toString(),
                        fullSnapshot, revision, playerEntries));
            }
            if (fullSnapshot && player.level() instanceof ServerLevel serverLevel) {
                send(player, new DataCoreSyncPacket(DataScope.WORLD, serverLevel.dimension().identifier().toString(),
                        true, revision, syncedEntries(DataCoreWorldData.get(serverLevel).worldSnapshot(), DataScope.WORLD)));
            }
        }

        private List<DataCoreSyncPacket.Entry> dirtyEntries(ServerPlayer player) {
            LinkedHashSet<Identifier> dirty = dirtyPlayerKeys.get(player.getUUID());
            if (dirty == null || dirty.isEmpty()) {
                return List.of();
            }
            int max = Math.max(1, Config.MAX_SYNC_KEYS_PER_BATCH.get());
            List<Identifier> selected = new ArrayList<>();
            for (Identifier id : List.copyOf(dirty)) {
                selected.add(id);
                if (selected.size() >= max) {
                    break;
                }
            }
            dirty.removeAll(selected);
            if (dirty.isEmpty()) {
                dirtyPlayerKeys.remove(player.getUUID());
            }
            CompoundTag values = valuesRoot(playerRoot(player));
            List<DataCoreSyncPacket.Entry> entries = new ArrayList<>();
            for (Identifier id : selected) {
                IDataKey<?> key = keys.get(id);
                if (key == null || !key.synced()) {
                    continue;
                }
                CompoundTag entry = values.getCompoundOrEmpty(id.toString());
                if (!entry.isEmpty()) {
                    entries.add(new DataCoreSyncPacket.Entry(id, kindOf(key, entry), entry));
                }
            }
            return entries;
        }

        private List<DataCoreSyncPacket.Entry> syncedEntries(CompoundTag values, DataScope scope) {
            return syncedEntries(compoundMap(values), scope);
        }

        private List<DataCoreSyncPacket.Entry> syncedEntries(Map<String, CompoundTag> values, DataScope scope) {
            List<DataCoreSyncPacket.Entry> entries = new ArrayList<>();
            for (Map.Entry<String, CompoundTag> entry : values.entrySet()) {
                Identifier id = safeIdentifier(entry.getKey());
                IDataKey<?> key = keys.get(id);
                if (key != null && key.synced() && (scope == null || key.scope() == scope)) {
                    entries.add(new DataCoreSyncPacket.Entry(id, key.kind(), entry.getValue()));
                }
            }
            return entries;
        }

        private void send(ServerPlayer player, DataCoreSyncPacket packet) {
            EchoNetSend.toPlayer(player, packet);
        }
    }

    private static Map<String, CompoundTag> compoundMap(CompoundTag values) {
        Map<String, CompoundTag> map = new LinkedHashMap<>();
        for (String key : values.keySet()) {
            CompoundTag entry = values.getCompoundOrEmpty(key);
            if (!entry.isEmpty()) {
                map.put(key, entry.copy());
            }
        }
        return map;
    }
}

