package com.knoxhack.echocore.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Safe fallback used when ECHO: DataCore is not installed.
 */
public final class NoOpDataService implements IDataService {
    public static final NoOpDataService INSTANCE = new NoOpDataService();
    private static final Identifier UNKNOWN_ID = Identifier.fromNamespaceAndPath("echocore", "unknown");

    private final Map<Identifier, IDataKey<?>> keys = new ConcurrentHashMap<>();

    private NoOpDataService() {
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
                .sorted(Comparator.comparing(key -> key.id().toString()))
                .toList();
    }

    @Override
    public IPlayerDataView player(Player player) {
        UUID id = player == null ? new UUID(0L, 0L) : player.getUUID();
        return new NoOpPlayerDataView(id);
    }

    @Override
    public IWorldDataView world(Level level) {
        Identifier id = level == null ? UNKNOWN_ID : level.dimension().identifier();
        return new NoOpWorldDataView(id);
    }

    @Override
    public ITeamDataView team(Level level, Identifier teamId) {
        return new NoOpTeamDataView(teamId == null ? UNKNOWN_ID : teamId);
    }

    @Override
    public IDataSyncBridge syncBridge() {
        return IDataSyncBridge.NOOP;
    }

    public void clearRegisteredKeysForTests() {
        keys.clear();
    }

    private abstract static class NoOpView implements IDataView {
        @Override
        public <T> T get(IDataKey<T> key) {
            return key == null ? null : key.defaultValue();
        }

        @Override
        public <T> boolean set(IDataKey<T> key, T value) {
            return false;
        }

        @Override
        public boolean clear(IDataKey<?> key) {
            return false;
        }

        @Override
        public boolean has(IDataKey<?> key) {
            return false;
        }

        @Override
        public CompoundTag record(Identifier id) {
            return new CompoundTag();
        }

        @Override
        public boolean putRecord(Identifier id, CompoundTag value) {
            return false;
        }

        @Override
        public Map<Identifier, String> debugSnapshot() {
            return Map.of();
        }
    }

    private static final class NoOpPlayerDataView extends NoOpView implements IPlayerDataView {
        private final UUID playerId;

        private NoOpPlayerDataView(UUID playerId) {
            this.playerId = playerId;
        }

        @Override
        public UUID playerId() {
            return playerId;
        }
    }

    private static final class NoOpWorldDataView extends NoOpView implements IWorldDataView {
        private final Identifier dimensionId;

        private NoOpWorldDataView(Identifier dimensionId) {
            this.dimensionId = dimensionId;
        }

        @Override
        public Identifier dimensionId() {
            return dimensionId;
        }
    }

    private static final class NoOpTeamDataView extends NoOpView implements ITeamDataView {
        private final Identifier teamId;

        private NoOpTeamDataView(Identifier teamId) {
            this.teamId = teamId;
        }

        @Override
        public Identifier teamId() {
            return teamId;
        }
    }
}
