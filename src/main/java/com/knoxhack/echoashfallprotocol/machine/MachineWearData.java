package com.knoxhack.echoashfallprotocol.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks machine wear data per-block in the world.
 * Machines accumulate wear over use and can jam at high wear levels.
 */
public class MachineWearData {
    
    public static final int MAX_WEAR = 1000;
    public static final int JAM_THRESHOLD = 800; // 80% wear = chance to jam
    public static final int BROKEN_THRESHOLD = 950; // 95% wear = high failure rate
    
    // Per-world storage using dimension-based map
    private static final Map<String, Map<String, Integer>> worldMachineWear = new HashMap<>();
    private static final Map<String, Map<String, Boolean>> worldMachineJammed = new HashMap<>();
    
    private final String worldKey;
    private final Map<String, Integer> machineWear;
    private final Map<String, Boolean> machineJammed;
    
    public MachineWearData(Level level) {
        this.worldKey = level.dimension().toString();
        this.machineWear = worldMachineWear.computeIfAbsent(worldKey, k -> new HashMap<>());
        this.machineJammed = worldMachineJammed.computeIfAbsent(worldKey, k -> new HashMap<>());
    }
    
    public int getWear(BlockPos pos) {
        String key = getKey(pos);
        return machineWear.getOrDefault(key, 0);
    }
    
    public void addWear(BlockPos pos, int amount, RandomSource random) {
        String key = getKey(pos);
        int current = machineWear.getOrDefault(key, 0);
        int newWear = Math.min(MAX_WEAR, current + amount);
        machineWear.put(key, newWear);
        
        // Check for jam at threshold
        if (newWear >= JAM_THRESHOLD && !machineJammed.getOrDefault(key, false)) {
            if (random.nextFloat() < 0.3f) { // 30% chance to jam when crossing threshold
                machineJammed.put(key, true);
            }
        }
    }
    
    public void repair(BlockPos pos, int amount) {
        String key = getKey(pos);
        int current = machineWear.getOrDefault(key, 0);
        int newWear = Math.max(0, current - amount);
        machineWear.put(key, newWear);
        machineJammed.put(key, false); // Repair clears jam
    }
    
    public boolean isJammed(BlockPos pos) {
        String key = getKey(pos);
        return machineJammed.getOrDefault(key, false);
    }
    
    public void setJammed(BlockPos pos, boolean jammed) {
        String key = getKey(pos);
        machineJammed.put(key, jammed);
    }
    
    public float getWearPercent(BlockPos pos) {
        return (float) getWear(pos) / MAX_WEAR;
    }
    
    public boolean shouldShowSmoke(BlockPos pos) {
        int wear = getWear(pos);
        return wear > JAM_THRESHOLD;
    }
    
    public boolean checkJamChance(BlockPos pos, RandomSource random) {
        int wear = getWear(pos);
        if (wear < JAM_THRESHOLD) return false;
        
        // Chance to jam based on wear level
        float jamChance = (wear - JAM_THRESHOLD) / (float)(MAX_WEAR - JAM_THRESHOLD);
        jamChance *= 0.5f; // Max 50% chance per tick at 100% wear
        
        if (random.nextFloat() < jamChance) {
            setJammed(pos, true);
            return true;
        }
        return false;
    }
    
    private String getKey(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
    
    public String getWearStatus(BlockPos pos) {
        int wear = getWear(pos);
        if (wear < 300) return "Good";
        if (wear < 600) return "Worn";
        if (wear < JAM_THRESHOLD) return "Degraded";
        if (wear < BROKEN_THRESHOLD) return "Critical";
        return "Failing";
    }
}
