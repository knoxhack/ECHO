package com.knoxhack.echopowergrid.grid;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.api.EchoEnergyStorage;
import com.knoxhack.echopowergrid.api.EchoGridState;
import com.knoxhack.echopowergrid.api.EchoPowerNetwork;
import com.knoxhack.echopowergrid.api.EchoPowerNodeType;
import com.knoxhack.echopowergrid.api.EchoPowerQuality;
import com.knoxhack.echopowergrid.block.entity.GeneratorBlockEntity;
import com.knoxhack.echopowergrid.block.entity.PowerConsumerBlockEntity;
import com.knoxhack.echopowergrid.block.entity.BatteryBlockEntity;
import com.knoxhack.echopowergrid.config.PowerGridConfig;
import com.knoxhack.echopowergrid.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PowerNetworkManager {
    private static final Map<ResourceKey<Level>, PowerNetworkManager> MANAGERS = new HashMap<>();

    private final ServerLevel level;
    private final Map<BlockPos, EchoPowerNetwork> posToNetwork = new HashMap<>();
    private final Map<UUID, EchoPowerNetwork> networks = new HashMap<>();
    private final Set<BlockPos> dirtyPositions = new HashSet<>();
    private final Deque<EchoPowerNetwork> rebuildQueue = new ArrayDeque<>();
    private int tickCounter = 0;

    private PowerNetworkManager(ServerLevel level) {
        this.level = level;
    }

    public static PowerNetworkManager get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return MANAGERS.computeIfAbsent(serverLevel.dimension(), k -> new PowerNetworkManager(serverLevel));
        }
        // Client fallback - return a no-op manager
        return new PowerNetworkManager(null) {
            @Override
            public void tick() {}
        };
    }

    public static void tickAll(MinecraftServer server) {
        if (!PowerGridConfig.ENABLED.get()) return;
        for (ServerLevel level : server.getAllLevels()) {
            get(level).tick();
        }
    }

    public static void clearAll() {
        MANAGERS.clear();
    }

    public void tick() {
        if (level == null) return;
        tickCounter++;

        // Rebuild dirty networks in batches
        int batchSize = PowerGridConfig.NETWORK_REBUILD_BATCH_SIZE.get();
        if (!dirtyPositions.isEmpty()) {
            rebuildDirtyNetworks(batchSize);
        }

        // Process rebuild queue
        int processed = 0;
        while (!rebuildQueue.isEmpty() && processed < batchSize) {
            EchoPowerNetwork net = rebuildQueue.poll();
            if (net != null && networks.containsKey(net.networkId)) {
                rebuildNetwork(net);
                processed++;
            }
        }

        // Update networks on interval
        int interval = PowerGridConfig.NETWORK_UPDATE_INTERVAL_TICKS.get();
        if (tickCounter % interval == 0) {
            updateNetworks();
        }
    }

    public EchoPowerNetwork getNetworkAt(BlockPos pos) {
        return posToNetwork.get(pos);
    }

    public int getNetworkCount() {
        return networks.size();
    }

    public Optional<EchoEnergyStorage> getEnergyStorageAt(BlockPos pos) {
        if (level == null) return Optional.empty();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EchoEnergyStorage storage) {
            return Optional.of(storage);
        }
        return Optional.empty();
    }

    public boolean requestPower(BlockPos pos, long epPerTick) {
        EchoPowerNetwork network = posToNetwork.get(pos);
        if (network == null) return false;
        long available = network.totalGeneration + network.totalStored;
        return available >= epPerTick;
    }

    public void markDirty(BlockPos pos) {
        dirtyPositions.add(pos);
        EchoPowerNetwork net = posToNetwork.get(pos);
        if (net != null) {
            net.dirty = true;
        }
    }

    public void onBlockPlaced(BlockPos pos) {
        markDirty(pos);
        for (BlockPos neighbor : getNeighbors(pos)) {
            markDirty(neighbor);
        }
    }

    public void onBlockRemoved(BlockPos pos) {
        EchoPowerNetwork net = posToNetwork.remove(pos);
        if (net != null) {
            net.removeNode(pos);
            net.dirty = true;
            rebuildQueue.add(net);
        }
        for (BlockPos neighbor : getNeighbors(pos)) {
            markDirty(neighbor);
        }
    }

    private void rebuildDirtyNetworks(int batchSize) {
        int processed = 0;
        Iterator<BlockPos> it = dirtyPositions.iterator();
        while (it.hasNext() && processed < batchSize * 4) {
            BlockPos pos = it.next();
            it.remove();
            processed++;

            EchoPowerNetwork existing = posToNetwork.get(pos);
            if (existing != null) {
                existing.dirty = true;
                rebuildQueue.add(existing);
            } else if (isPowerNode(pos)) {
                // New node - find or create network
                EchoPowerNetwork neighborNet = findNeighborNetwork(pos);
                if (neighborNet != null) {
                    neighborNet.addNode(pos);
                    posToNetwork.put(pos, neighborNet);
                    neighborNet.dirty = true;
                    rebuildQueue.add(neighborNet);
                } else {
                    EchoPowerNetwork net = new EchoPowerNetwork(UUID.randomUUID(), level.dimension());
                    net.addNode(pos);
                    networks.put(net.networkId, net);
                    posToNetwork.put(pos, net);
                    rebuildQueue.add(net);
                }
            }
        }
    }

    private void rebuildNetwork(EchoPowerNetwork network) {
        if (network.isEmpty()) {
            networks.remove(network.networkId);
            return;
        }

        // BFS to find all connected nodes
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.addAll(network.getNodes());
        for (BlockPos p : network.getNodes()) {
            visited.add(p);
        }

        int maxSize = PowerGridConfig.MAX_NETWORK_SIZE.get();
        int scanLimit = PowerGridConfig.MAX_CABLE_SCAN_PER_TICK.get();
        int scanned = 0;

        while (!queue.isEmpty() && scanned < scanLimit) {
            BlockPos current = queue.poll();
            scanned++;

            for (BlockPos neighbor : getNeighbors(current)) {
                if (visited.contains(neighbor)) continue;
                if (!isPowerNode(neighbor)) continue;
                if (visited.size() >= maxSize) break;

                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        // Update network membership
        for (BlockPos pos : network.getNodes()) {
            if (!visited.contains(pos)) {
                posToNetwork.remove(pos);
                network.removeNode(pos);
            }
        }
        for (BlockPos pos : visited) {
            posToNetwork.put(pos, network);
        }
        network.clearNodes();
        network.addAllNodes(visited);
        network.dirty = false;
    }

    private void updateNetworks() {
        List<EchoPowerNetwork> toUpdate = new ArrayList<>(networks.values());
        int maxUpdates = PowerGridConfig.GRID_UPDATES_PER_TICK.get();
        int updated = 0;

        for (EchoPowerNetwork network : toUpdate) {
            if (updated >= maxUpdates) break;
            if (network.isEmpty() || network.dirty) continue;
            updated++;

            long totalGen = 0;
            long totalDemand = 0;
            long totalStored = 0;
            long totalCapacity = 0;
            long minTransfer = Long.MAX_VALUE;
            EchoPowerQuality quality = EchoPowerQuality.STABLE;

            List<BatteryBlockEntity> batteries = new ArrayList<>();
            List<PowerConsumerBlockEntity> consumers = new ArrayList<>();
            List<GeneratorBlockEntity> generators = new ArrayList<>();

            for (BlockPos pos : network.getNodes()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof GeneratorBlockEntity gen) {
                    totalGen += gen.getGenerationPerTick();
                    if (gen.getPowerQuality() == EchoPowerQuality.DIRTY) quality = EchoPowerQuality.DIRTY;
                    generators.add(gen);
                } else if (be instanceof BatteryBlockEntity bat) {
                    totalStored += bat.getEnergyStored();
                    totalCapacity += bat.getMaxEnergyStored();
                    batteries.add(bat);
                } else if (be instanceof PowerConsumerBlockEntity con) {
                    totalDemand += con.getDemandPerTick();
                    consumers.add(con);
                }

                BlockState state = level.getBlockState(pos);
                long transfer = ModBlocks.getTransferLimit(state);
                if (transfer > 0 && transfer < minTransfer) {
                    minTransfer = transfer;
                }
            }

            network.totalGeneration = totalGen;
            network.totalDemand = totalDemand;
            network.totalStored = totalStored;
            network.totalCapacity = totalCapacity;
            network.transferLimit = minTransfer == Long.MAX_VALUE ? Long.MAX_VALUE : minTransfer;
            network.quality = quality;

            int updateTicks = Math.max(1, PowerGridConfig.NETWORK_UPDATE_INTERVAL_TICKS.get());
            long demandBudget = saturatedMultiply(totalDemand, updateTicks);
            long storageWindow = storageReceiveWindow(batteries, updateTicks);
            long generatorWindow = generatorAvailableWindow(generators, updateTicks);
            long requestedFromGenerators = Math.min(generatorWindow, saturatedAdd(demandBudget, storageWindow));
            long generatedBudget = extractGeneratorBudget(generators, requestedFromGenerators, updateTicks);
            long suppliedBudget = generatedBudget;

            if (suppliedBudget < demandBudget && !batteries.isEmpty()) {
                long deficit = demandBudget - suppliedBudget;
                long extracted = extractBatteryBudget(batteries, deficit, updateTicks);
                suppliedBudget = saturatedAdd(suppliedBudget, extracted);
            }

            boolean overload = PowerGridConfig.ENABLE_OVERLOAD.get() && totalDemand > network.transferLimit && network.transferLimit > 0;
            boolean brownout = PowerGridConfig.ENABLE_BROWNOUT.get() && demandBudget > 0 && suppliedBudget < demandBudget;

            if (overload) {
                network.state = EchoGridState.OVERLOADED;
            } else if (brownout) {
                network.state = EchoGridState.BROWNOUT;
            } else if (generatedBudget > demandBudget && storageWindow > 0) {
                network.state = EchoGridState.CHARGING;
            } else if (generatedBudget < demandBudget && suppliedBudget >= demandBudget && totalStored > 0) {
                network.state = EchoGridState.DISCHARGING;
            } else {
                network.state = EchoGridState.STABLE;
            }

            if (!consumers.isEmpty()) {
                double ratio = brownout && demandBudget > 0 ? (double) suppliedBudget / (double) demandBudget : 1.0;
                for (PowerConsumerBlockEntity con : consumers) {
                    long demand = con.getDemandPerTick();
                    long supplied = (long) Math.floor(demand * ratio);
                    con.setPowerReceived(supplied, updateTicks);
                }
            }

            long excess = generatedBudget > demandBudget ? generatedBudget - demandBudget : 0;
            if (excess > 0 && !batteries.isEmpty()) {
                receiveBatteryBudget(batteries, excess, updateTicks);
            }
        }
    }

    private static long generatorAvailableWindow(List<GeneratorBlockEntity> generators, int ticks) {
        long available = 0;
        for (GeneratorBlockEntity gen : generators) {
            available = saturatedAdd(available, gen.getAvailableEnergyForNetwork(ticks));
        }
        return available;
    }

    private static long extractGeneratorBudget(List<GeneratorBlockEntity> generators, long requested, int ticks) {
        long remaining = requested;
        long extractedTotal = 0;
        for (GeneratorBlockEntity gen : generators) {
            if (remaining <= 0) break;
            long available = gen.getAvailableEnergyForNetwork(ticks);
            long extracted = gen.extractEnergyForNetwork(Math.min(remaining, available));
            remaining -= extracted;
            extractedTotal = saturatedAdd(extractedTotal, extracted);
        }
        return extractedTotal;
    }

    private static long storageReceiveWindow(List<BatteryBlockEntity> batteries, int ticks) {
        long capacity = 0;
        for (BatteryBlockEntity bat : batteries) {
            long free = Math.max(0, bat.getMaxEnergyStored() - bat.getEnergyStored());
            capacity = saturatedAdd(capacity, Math.min(free, saturatedMultiply(bat.getMaxInput(), ticks)));
        }
        return capacity;
    }

    private static long receiveBatteryBudget(List<BatteryBlockEntity> batteries, long amount, int ticks) {
        long remaining = amount;
        long receivedTotal = 0;
        for (BatteryBlockEntity bat : batteries) {
            for (int tick = 0; tick < ticks && remaining > 0; tick++) {
                long received = bat.receiveEnergy(Math.min(remaining, bat.getMaxInput()), false);
                if (received <= 0) break;
                remaining -= received;
                receivedTotal = saturatedAdd(receivedTotal, received);
            }
            if (remaining <= 0) break;
        }
        return receivedTotal;
    }

    private static long extractBatteryBudget(List<BatteryBlockEntity> batteries, long amount, int ticks) {
        long remaining = amount;
        long extractedTotal = 0;
        for (BatteryBlockEntity bat : batteries) {
            for (int tick = 0; tick < ticks && remaining > 0; tick++) {
                long extracted = bat.extractEnergy(Math.min(remaining, bat.getMaxOutput()), false);
                if (extracted <= 0) break;
                remaining -= extracted;
                extractedTotal = saturatedAdd(extractedTotal, extracted);
            }
            if (remaining <= 0) break;
        }
        return extractedTotal;
    }

    private static long saturatedMultiply(long value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Long.MAX_VALUE / multiplier) return Long.MAX_VALUE;
        return value * multiplier;
    }

    private static long saturatedAdd(long left, long right) {
        if (left >= Long.MAX_VALUE - right) return Long.MAX_VALUE;
        return left + right;
    }

    private EchoPowerNetwork findNeighborNetwork(BlockPos pos) {
        for (BlockPos neighbor : getNeighbors(pos)) {
            EchoPowerNetwork net = posToNetwork.get(neighbor);
            if (net != null) return net;
        }
        return null;
    }

    private boolean isPowerNode(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return ModBlocks.isPowerNode(state);
    }

    private static BlockPos[] getNeighbors(BlockPos pos) {
        return new BlockPos[]{
            pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()
        };
    }
}
