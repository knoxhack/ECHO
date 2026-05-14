package com.knoxhack.signalos.service;

import com.knoxhack.signalos.api.SignalOsDataRecord;
import com.knoxhack.signalos.api.SignalOsPeripheralProvider;
import com.knoxhack.signalos.block.entity.SignalOsServerRackBlockEntity;
import com.knoxhack.signalos.block.entity.SignalOsTerminalBlockEntity;
import com.knoxhack.signalos.content.SignalOsContentRegistry;
import com.knoxhack.signalos.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class SignalOsComputerNetworkService {
    private static final int BASE_RADIUS = 24;
    private static final int MAX_RADIUS = 32;
    private static final int SNAPSHOT_CACHE_TICKS = 20;
    private static final int SNAPSHOT_CACHE_LIMIT = 128;
    private static final Map<NetworkCacheKey, CachedNetworkSnapshot> SNAPSHOT_CACHE = new LinkedHashMap<>(64, 0.75F, true);

    private SignalOsComputerNetworkService() {
    }

    public static NetworkSnapshot snapshot(Player player) {
        SignalOsTerminalBlockEntity anchor = SignalOsTerminalServices.findOwnedTerminal(player, true);
        if (player == null || anchor == null || player.level() == null) {
            return NetworkSnapshot.offline();
        }
        Level level = player.level();
        BlockPos anchorPos = anchor.getBlockPos();
        int baseAccessTier = level.getBlockState(anchorPos).is(ModBlocks.WORKSTATION.get()) ? 2 : 1;
        NetworkCacheKey cacheKey = NetworkCacheKey.of(player, level, anchorPos, baseAccessTier);
        long now = level.getGameTime();
        synchronized (SNAPSHOT_CACHE) {
            CachedNetworkSnapshot cached = SNAPSHOT_CACHE.get(cacheKey);
            if (cached != null && now - cached.createdTick() <= SNAPSHOT_CACHE_TICKS) {
                return cached.snapshot();
            }
        }
        Scan scan = scan(level, anchorPos, MAX_RADIUS);
        int radius = Math.min(MAX_RADIUS, BASE_RADIUS + scan.relays() * 8);
        if (radius != MAX_RADIUS) {
            scan = scan(level, anchorPos, radius);
        }
        int accessTier = baseAccessTier;
        if (scan.serverRacks() > 0) {
            accessTier++;
        }
        String networkId = networkId(player, level, anchorPos);
        List<SignalOsDataRecord> records = networkRecords(player, scan.rackRecords());
        List<SignalOsPeripheralProvider.Peripheral> peripherals = new ArrayList<>();
        peripherals.add(new SignalOsPeripheralProvider.Peripheral(
                Identifier.fromNamespaceAndPath("signalos", "network/access"),
                "access",
                baseAccessTier > 1 ? "Workstation" : "Terminal",
                "ONLINE",
                anchorPos,
                accessTier));
        peripherals.addAll(SignalOsContentRegistry.peripherals(player));
        NetworkSnapshot snapshot = new NetworkSnapshot(networkId, true, accessTier, radius, anchorPos.toShortString(),
                anchor.ownerName(), scan.terminals(), scan.workstations(), scan.serverRacks(), scan.relays(),
                records, peripherals);
        synchronized (SNAPSHOT_CACHE) {
            SNAPSHOT_CACHE.put(cacheKey, new CachedNetworkSnapshot(now, snapshot));
            while (SNAPSHOT_CACHE.size() > SNAPSHOT_CACHE_LIMIT) {
                SNAPSHOT_CACHE.remove(SNAPSHOT_CACHE.keySet().iterator().next());
            }
        }
        return snapshot;
    }

    public static void invalidateCache() {
        synchronized (SNAPSHOT_CACHE) {
            SNAPSHOT_CACHE.clear();
        }
    }

    public static List<SignalOsDataRecord> networkRecords(Player player) {
        return snapshot(player).records();
    }

    private static List<SignalOsDataRecord> networkRecords(Player player, List<SignalOsDataRecord> rackRecords) {
        Map<Identifier, SignalOsDataRecord> records = new LinkedHashMap<>();
        for (SignalOsDataRecord record : SignalOsContentRegistry.dataRecords(player)) {
            records.putIfAbsent(record.id(), record);
        }
        for (SignalOsDataRecord record : SignalOsPlayerData.notes(player)) {
            records.put(record.id(), record);
        }
        for (SignalOsDataRecord record : rackRecords) {
            records.putIfAbsent(record.id(), record);
        }
        return records.values().stream()
                .sorted(Comparator.comparingInt(SignalOsDataRecord::order)
                        .thenComparing(record -> record.id().toString()))
                .toList();
    }

    private static Scan scan(Level level, BlockPos anchor, int radius) {
        int terminals = 0;
        int workstations = 0;
        int serverRacks = 0;
        int relays = 0;
        List<SignalOsDataRecord> rackRecords = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(anchor.offset(-radius, -16, -radius), anchor.offset(radius, 16, radius))) {
            Block block = level.getBlockState(pos).getBlock();
            if (block == ModBlocks.TERMINAL.get()) {
                terminals++;
            } else if (block == ModBlocks.WORKSTATION.get()) {
                workstations++;
            } else if (block == ModBlocks.NETWORK_RELAY.get()) {
                relays++;
            } else if (block == ModBlocks.SERVER_RACK.get()) {
                serverRacks++;
                if (level.getBlockEntity(pos) instanceof SignalOsServerRackBlockEntity rack) {
                    rackRecords.addAll(rack.driveRecords());
                }
            }
        }
        return new Scan(terminals, workstations, serverRacks, relays, rackRecords);
    }

    private static String networkId(Player player, Level level, BlockPos anchorPos) {
        String owner = player == null ? "offline" : player.getUUID().toString().substring(0, 8);
        String dimension = level.dimension().identifier().toString().replace(':', '_').replace('/', '_');
        return (dimension + "-" + anchorPos.getX() + "_" + anchorPos.getY() + "_" + anchorPos.getZ() + "-" + owner)
                .toLowerCase(java.util.Locale.ROOT);
    }

    private record Scan(int terminals, int workstations, int serverRacks, int relays,
            List<SignalOsDataRecord> rackRecords) {
    }

    private record NetworkCacheKey(UUID playerId, String dimension, BlockPos anchor, int baseAccessTier) {
        private static NetworkCacheKey of(Player player, Level level, BlockPos anchor, int baseAccessTier) {
            return new NetworkCacheKey(player.getUUID(), level.dimension().identifier().toString(),
                    anchor.immutable(), baseAccessTier);
        }
    }

    private record CachedNetworkSnapshot(long createdTick, NetworkSnapshot snapshot) {
        private CachedNetworkSnapshot {
            snapshot = snapshot == null ? NetworkSnapshot.offline() : snapshot;
        }
    }

    public record NetworkSnapshot(
            String networkId,
            boolean online,
            int accessTier,
            int radius,
            String anchor,
            String owner,
            int terminals,
            int workstations,
            int serverRacks,
            int relays,
            List<SignalOsDataRecord> records,
            List<SignalOsPeripheralProvider.Peripheral> peripherals) {
        public NetworkSnapshot {
            networkId = networkId == null || networkId.isBlank() ? "offline" : networkId;
            accessTier = Math.max(0, accessTier);
            radius = Math.max(0, radius);
            anchor = anchor == null ? "" : anchor;
            owner = owner == null ? "" : owner;
            terminals = Math.max(0, terminals);
            workstations = Math.max(0, workstations);
            serverRacks = Math.max(0, serverRacks);
            relays = Math.max(0, relays);
            records = List.copyOf(records == null ? List.of() : records);
            peripherals = List.copyOf(peripherals == null ? List.of() : peripherals);
        }

        public static NetworkSnapshot offline() {
            return new NetworkSnapshot("offline", false, 0, 0, "", "", 0, 0, 0, 0, List.of(), List.of());
        }

        public int deviceCount() {
            return terminals + workstations + serverRacks + relays;
        }
    }
}
