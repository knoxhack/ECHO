package com.knoxhack.echoashfallprotocol.faction;

import net.minecraft.core.BlockPos;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.*;

/**
 * Tracks faction influence over biomes and chunks.
 * Used for territory control, raid spawning, and quest generation.
 */
public class FactionTerritory implements ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, FactionTerritory> STREAM_CODEC = StreamCodec.of(
        FactionTerritory::writeSync,
        FactionTerritory::readSync
    );
    
    // Influence threshold constants
    public static final int CONTESTED_THRESHOLD = 30;
    public static final int DOMINANT_THRESHOLD = 60;
    public static final int CONTROLLED_THRESHOLD = 80;
    
    public enum TerritoryStatus {
        UNCLAIMED("Unclaimed", 0xFF555555),
        CONTESTED("Contested", 0xFFFFA94D),
        DOMINANT("Dominant", 0xFF8A9BB0),
        CONTROLLED("Controlled", 0xFF42D67E);
        
        private final String displayName;
        private final int mapColor;
        
        TerritoryStatus(String displayName, int mapColor) {
            this.displayName = displayName;
            this.mapColor = mapColor;
        }
        
        public String getDisplayName() { return displayName; }
        public int getMapColor() { return mapColor; }
    }
    
    // Biome influence: faction -> biome -> influence value (0-100)
    private final Map<ReputationData.Faction, Map<String, Integer>> biomeInfluence = new HashMap<>();
    
    // Chunk ownership: chunk pos -> primary faction
    private final Map<String, ReputationData.Faction> chunkOwnership = new HashMap<>();
    
    // Village locations and their controlling factions
    private final List<VillageControl> factionVillages = new ArrayList<>();
    
    public FactionTerritory() {
        // Initialize biome influence maps
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            biomeInfluence.put(faction, new HashMap<>());
        }
    }

    private static void writeSync(RegistryFriendlyByteBuf buf, FactionTerritory data) {
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            Map<String, Integer> influence = data.biomeInfluence.getOrDefault(faction, Collections.emptyMap());
            buf.writeVarInt(influence.size());
            for (Map.Entry<String, Integer> entry : influence.entrySet()) {
                buf.writeUtf(entry.getKey());
                buf.writeVarInt(entry.getValue());
            }
        }

        buf.writeVarInt(data.chunkOwnership.size());
        for (Map.Entry<String, ReputationData.Faction> entry : data.chunkOwnership.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue().name());
        }

        buf.writeVarInt(data.factionVillages.size());
        for (VillageControl village : data.factionVillages) {
            buf.writeBlockPos(village.center);
            buf.writeUtf(village.controllingFaction.name());
            buf.writeUtf(village.name);
            buf.writeLong(village.captureTime);
        }
    }

    private static FactionTerritory readSync(RegistryFriendlyByteBuf buf) {
        FactionTerritory data = new FactionTerritory();
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            Map<String, Integer> influence = new HashMap<>();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                String key = buf.readUtf();
                int value = buf.readVarInt();
                if (!key.isEmpty()) influence.put(key, value);
            }
            data.biomeInfluence.put(faction, influence);
        }

        data.chunkOwnership.clear();
        int chunkCount = buf.readVarInt();
        for (int i = 0; i < chunkCount; i++) {
            String key = buf.readUtf();
            String factionName = buf.readUtf();
            try {
                if (!key.isEmpty()) {
                    data.chunkOwnership.put(key, ReputationData.Faction.valueOf(factionName));
                }
            } catch (IllegalArgumentException ignored) {}
        }

        data.factionVillages.clear();
        int villageCount = buf.readVarInt();
        for (int i = 0; i < villageCount; i++) {
            BlockPos center = buf.readBlockPos();
            String factionName = buf.readUtf();
            String name = buf.readUtf();
            long captureTime = buf.readLong();
            try {
                data.factionVillages.add(new VillageControl(center, ReputationData.Faction.valueOf(factionName), name, captureTime));
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }
    
    /**
     * Get a faction's influence in a specific biome
     */
    public int getBiomeInfluence(ReputationData.Faction faction, String biomeKey) {
        return biomeInfluence.getOrDefault(faction, Collections.emptyMap())
                .getOrDefault(biomeKey, 0);
    }
    
    /**
     * Set a faction's influence in a biome
     */
    public void setBiomeInfluence(ReputationData.Faction faction, String biomeKey, int value) {
        biomeInfluence.computeIfAbsent(faction, k -> new HashMap<>())
                .put(biomeKey, Math.max(0, Math.min(100, value)));
    }
    
    /**
     * Modify biome influence (add/subtract)
     */
    public void modifyBiomeInfluence(ReputationData.Faction faction, String biomeKey, int amount) {
        int current = getBiomeInfluence(faction, biomeKey);
        setBiomeInfluence(faction, biomeKey, current + amount);
    }
    
    /**
     * Get the dominant faction in a biome
     */
    public ReputationData.Faction getDominantFaction(String biomeKey) {
        ReputationData.Faction dominant = null;
        int maxInfluence = 0;
        
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            int influence = getBiomeInfluence(faction, biomeKey);
            if (influence > maxInfluence) {
                maxInfluence = influence;
                dominant = faction;
            }
        }
        return dominant;
    }
    
    /**
     * Get territory status for a biome
     */
    public TerritoryStatus getBiomeStatus(String biomeKey) {
        ReputationData.Faction dominant = getDominantFaction(biomeKey);
        if (dominant == null) return TerritoryStatus.UNCLAIMED;
        
        int influence = getBiomeInfluence(dominant, biomeKey);
        
        if (influence >= CONTROLLED_THRESHOLD) return TerritoryStatus.CONTROLLED;
        if (influence >= DOMINANT_THRESHOLD) return TerritoryStatus.DOMINANT;
        if (influence >= CONTESTED_THRESHOLD) return TerritoryStatus.CONTESTED;
        return TerritoryStatus.UNCLAIMED;
    }
    
    /**
     * Check if a biome is contested (two or more factions have significant influence)
     */
    public boolean isBiomeContested(String biomeKey) {
        int factionsWithInfluence = 0;
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            if (getBiomeInfluence(faction, biomeKey) >= CONTESTED_THRESHOLD) {
                factionsWithInfluence++;
            }
        }
        return factionsWithInfluence >= 2;
    }
    
    /**
     * Get all biomes where a faction has dominance
     */
    public List<String> getFactionBiomes(ReputationData.Faction faction, TerritoryStatus minStatus) {
        List<String> biomes = new ArrayList<>();
        Map<String, Integer> influenceMap = biomeInfluence.getOrDefault(faction, Collections.emptyMap());
        
        for (Map.Entry<String, Integer> entry : influenceMap.entrySet()) {
            int threshold = switch (minStatus) {
                case UNCLAIMED -> 0;
                case CONTESTED -> CONTESTED_THRESHOLD;
                case DOMINANT -> DOMINANT_THRESHOLD;
                case CONTROLLED -> CONTROLLED_THRESHOLD;
            };
            
            if (entry.getValue() >= threshold) {
                biomes.add(entry.getKey());
            }
        }
        return biomes;
    }
    
    /**
     * Set chunk ownership
     */
    public void setChunkOwner(ChunkPos pos, Level level, ReputationData.Faction faction) {
        String key = getChunkKey(pos, level);
        if (faction == null) {
            chunkOwnership.remove(key);
        } else {
            chunkOwnership.put(key, faction);
        }
    }
    
    /**
     * Get chunk owner
     */
    public ReputationData.Faction getChunkOwner(ChunkPos pos, Level level) {
        return chunkOwnership.get(getChunkKey(pos, level));
    }
    
    private String getChunkKey(ChunkPos pos, Level level) {
        return level.dimension().toString() + "_" + pos.x() + "_" + pos.z();
    }
    
    /**
     * Register a faction village
     */
    public void registerVillage(BlockPos center, ReputationData.Faction controllingFaction, String villageName) {
        // Remove existing village at this location
        factionVillages.removeIf(v -> v.center.distSqr(center) < 10000); // ~100 blocks
        
        factionVillages.add(new VillageControl(center, controllingFaction, villageName, System.currentTimeMillis()));
        
        // Boost controlling faction's influence in surrounding biomes
        // This would be expanded with actual biome detection
    }
    
    /**
     * Get villages controlled by a faction
     */
    public List<VillageControl> getFactionVillages(ReputationData.Faction faction) {
        return factionVillages.stream()
                .filter(v -> v.controllingFaction == faction)
                .toList();
    }

    public List<VillageControl> getAllVillages() {
        return new ArrayList<>(factionVillages);
    }
    
    /**
     * Transfer village control (e.g., after successful defense/raid)
     */
    public boolean transferVillageControl(BlockPos villageCenter, ReputationData.Faction newFaction) {
        for (VillageControl village : factionVillages) {
            if (village.center.distSqr(villageCenter) < 10000) {
                ReputationData.Faction oldFaction = village.controllingFaction;
                if (oldFaction == newFaction) return false;
                
                village.controllingFaction = newFaction;
                village.captureTime = System.currentTimeMillis();
                
                // Influence shift would happen here - reduce old faction, boost new
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the closest faction village to a position
     */
    public VillageControl getNearestVillage(BlockPos pos, ReputationData.Faction faction) {
        VillageControl nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (VillageControl village : factionVillages) {
            if (faction != null && village.controllingFaction != faction) continue;
            
            double dist = village.center.distSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = village;
            }
        }
        return nearest;
    }
    
    @Override
    public void serialize(ValueOutput output) {
        // Save biome influence
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            Map<String, Integer> influence = biomeInfluence.getOrDefault(faction, Collections.emptyMap());
            output.putInt("BiomeCount_" + faction.name(), influence.size());
            int idx = 0;
            for (Map.Entry<String, Integer> entry : influence.entrySet()) {
                output.putString("Biome_" + faction.name() + "_" + idx + "_Key", entry.getKey());
                output.putInt("Biome_" + faction.name() + "_" + idx + "_Value", entry.getValue());
                idx++;
            }
        }
        
        // Save chunk ownership
        output.putInt("ChunkCount", chunkOwnership.size());
        int chunkIdx = 0;
        for (Map.Entry<String, ReputationData.Faction> entry : chunkOwnership.entrySet()) {
            output.putString("Chunk_" + chunkIdx + "_Key", entry.getKey());
            output.putString("Chunk_" + chunkIdx + "_Faction", entry.getValue().name());
            chunkIdx++;
        }
        
        // Save villages
        output.putInt("VillageCount", factionVillages.size());
        for (int i = 0; i < factionVillages.size(); i++) {
            VillageControl v = factionVillages.get(i);
            output.putInt("Village_" + i + "_X", v.center.getX());
            output.putInt("Village_" + i + "_Y", v.center.getY());
            output.putInt("Village_" + i + "_Z", v.center.getZ());
            output.putString("Village_" + i + "_Faction", v.controllingFaction.name());
            output.putString("Village_" + i + "_Name", v.name);
            output.putLong("Village_" + i + "_CaptureTime", v.captureTime);
        }
    }
    
    @Override
    public void deserialize(ValueInput input) {
        // Load biome influence
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            Map<String, Integer> influence = new HashMap<>();
            int count = input.getIntOr("BiomeCount_" + faction.name(), 0);
            for (int i = 0; i < count; i++) {
                String key = input.getStringOr("Biome_" + faction.name() + "_" + i + "_Key", "");
                int value = input.getIntOr("Biome_" + faction.name() + "_" + i + "_Value", 0);
                if (!key.isEmpty()) {
                    influence.put(key, value);
                }
            }
            biomeInfluence.put(faction, influence);
        }
        
        // Load chunk ownership
        chunkOwnership.clear();
        int chunkCount = input.getIntOr("ChunkCount", 0);
        for (int i = 0; i < chunkCount; i++) {
            String key = input.getStringOr("Chunk_" + i + "_Key", "");
            String factionName = input.getStringOr("Chunk_" + i + "_Faction", "");
            if (!key.isEmpty() && !factionName.isEmpty()) {
                try {
                    chunkOwnership.put(key, ReputationData.Faction.valueOf(factionName));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        // Load villages
        factionVillages.clear();
        int villageCount = input.getIntOr("VillageCount", 0);
        for (int i = 0; i < villageCount; i++) {
            int x = input.getIntOr("Village_" + i + "_X", 0);
            int y = input.getIntOr("Village_" + i + "_Y", 0);
            int z = input.getIntOr("Village_" + i + "_Z", 0);
            String factionName = input.getStringOr("Village_" + i + "_Faction", "");
            String name = input.getStringOr("Village_" + i + "_Name", "Unknown Village");
            long captureTime = input.getLongOr("Village_" + i + "_CaptureTime", 0L);
            
            try {
                ReputationData.Faction faction = ReputationData.Faction.valueOf(factionName);
                VillageControl v = new VillageControl(new BlockPos(x, y, z), faction, name, captureTime);
                factionVillages.add(v);
            } catch (IllegalArgumentException ignored) {}
        }
    }
    
    /**
     * Record class for village control data
     */
    public static class VillageControl {
        public final BlockPos center;
        public ReputationData.Faction controllingFaction;
        public final String name;
        public long captureTime;
        
        public VillageControl(BlockPos center, ReputationData.Faction faction, String name, long captureTime) {
            this.center = center;
            this.controllingFaction = faction;
            this.name = name;
            this.captureTime = captureTime;
        }
    }

    public static FactionTerritory get(net.minecraft.world.entity.player.Player player) {
        return player.getData(ModAttachments.FACTION_TERRITORY.get());
    }

    public static void saveAndSync(ServerPlayer player, FactionTerritory territory) {
        player.setData(ModAttachments.FACTION_TERRITORY.get(), territory);
        player.syncData(ModAttachments.FACTION_TERRITORY.get());
    }

    public static void syncToClient(ServerPlayer player) {
        player.syncData(ModAttachments.FACTION_TERRITORY.get());
    }
}
