package com.knoxhack.echoashfallprotocol.fasttravel;

import com.knoxhack.echoashfallprotocol.gameplay.AshfallInteractionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.*;

import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.survival.CombatData;

/**
 * Radio Network - Fast travel system using relay stations.
 * Players can teleport between discovered and activated relay stations.
 */
public class RadioNetwork implements ValueIOSerializable {
    
    // Cooldown between fast travels (default 5 minutes = 6000 ticks). Overridable via Config.FAST_TRAVEL_COOLDOWN_TICKS.
    public static final int FAST_TRAVEL_COOLDOWN = 6000;

    private static int cooldownTicks() {
        try {
            return com.knoxhack.echoashfallprotocol.Config.FAST_TRAVEL_COOLDOWN_TICKS.get();
        } catch (Throwable t) {
            return FAST_TRAVEL_COOLDOWN;
        }
    }
    
    // Discovered stations (not necessarily activated)
    private final Set<String> discoveredStations = new HashSet<>();
    
    // Activated stations (available for fast travel)
    private final Set<String> activatedStations = new HashSet<>();
    
    // Last fast travel time (for cooldown)
    private long lastFastTravelTick = 0;
    
    // Fast travel usage count (for achievements)
    private int fastTravelCount = 0;
    
    public RadioNetwork() {
        // Default constructor
    }
    
    /**
     * Discover a station (found but not yet activated)
     */
    public void discoverStation(String stationId) {
        discoveredStations.add(stationId);
    }
    
    /**
     * Activate a station (available for fast travel)
     */
    public void activateStation(String stationId) {
        discoveredStations.add(stationId);
        activatedStations.add(stationId);
    }
    
    /**
     * Check if station is discovered
     */
    public boolean isDiscovered(String stationId) {
        return discoveredStations.contains(stationId);
    }
    
    /**
     * Check if station is activated (available for travel)
     */
    public boolean isActivated(String stationId) {
        return activatedStations.contains(stationId);
    }
    
    /**
     * Get all discovered stations
     */
    public Set<String> getDiscoveredStations() {
        return new HashSet<>(discoveredStations);
    }
    
    /**
     * Get all activated stations
     */
    public Set<String> getActivatedStations() {
        return new HashSet<>(activatedStations);
    }
    
    /**
     * Get count of activated stations
     */
    public int getActivatedCount() {
        return activatedStations.size();
    }
    
    /**
     * Get available destinations from a position
     */
    public List<StationInfo> getAvailableDestinations(BlockPos fromPos) {
        List<StationInfo> destinations = new ArrayList<>();
        
        for (String stationId : activatedStations) {
            StationInfo info = StationRegistry.getStation(stationId);
            if (info != null && !info.getPosition().equals(fromPos)) {
                destinations.add(info);
            }
        }
        
        return destinations;
    }
    
    /**
     * Attempt fast travel to a station
     * @return true if successful
     */
    public boolean fastTravelTo(Player player, String destinationId) {
        // Check cooldown
        long currentTick = player.level().getGameTime();
        int cooldown = cooldownTicks();
        if (currentTick - lastFastTravelTick < cooldown) {
            int remainingSeconds = (int) ((cooldown - (currentTick - lastFastTravelTick)) / 20);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Fast travel on cooldown! " + remainingSeconds + " seconds remaining."));
            return false;
        }
        
        // Check if destination is activated
        if (!activatedStations.contains(destinationId)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Station not activated!"));
            return false;
        }
        
        // Check if player has Power Cell
        if (!hasTravelCell(player)) {
            player.sendSystemMessage(Component.literal(
                "Need a Power Cell or Energy Cell to fast travel!"));
            return false;
        }
        
        // Check if in combat (would need combat tracking system)
        if (isInCombat(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Cannot fast travel while in combat!"));
            return false;
        }
        
        // Check if in radiation zone
        if (isInRadiationZone(player)) {
            player.sendSystemMessage(Component.literal(
                "Cannot fast travel from an active radiation or Nexus hazard zone!"));
            return false;
        }

        StationInfo dest = StationRegistry.getStation(destinationId);
        if (dest != null) {
            BlockPos safePos = findSafeLanding(player.level(), dest.getPosition());
            if (safePos == null) {
                player.sendSystemMessage(Component.literal(
                    "Destination landing zone obstructed. Clear the relay pad and try again."));
                return false;
            }

            consumeTravelCell(player);
            player.teleportTo(safePos.getX() + 0.5D, safePos.getY(), safePos.getZ() + 0.5D);
            
            // Update stats
            lastFastTravelTick = currentTick;
            fastTravelCount++;
            
            player.sendSystemMessage(Component.literal(
                "Fast traveled to " + dest.getName()).withStyle(net.minecraft.ChatFormatting.AQUA));
            
            return true;
        }
        
        return false;
    }
    
    private boolean hasTravelCell(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.POWER_CELL.get()) || stack.is(ModItems.ENERGY_CELL.get())) {
                return true;
            }
        }
        return false;
    }
    
    private void consumeTravelCell(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.POWER_CELL.get())) {
                stack.shrink(1);
                return;
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.ENERGY_CELL.get())) {
                stack.shrink(1);
                return;
            }
        }
    }
    
    private boolean isInCombat(Player player) {
        CombatData combatData = player.getData(ModAttachments.COMBAT_DATA);
        return combatData != null && combatData.isInCombat(player.tickCount);
    }
    
    private boolean isInRadiationZone(Player player) {
        return com.knoxhack.echoashfallprotocol.event.EnvironmentalEventHandler.isInRadiationZone(player)
                || com.knoxhack.echoashfallprotocol.survival.RadiationUtil.isPlayerContaminated(player);
    }

    private BlockPos findSafeLanding(Level level, BlockPos destination) {
        for (int radius = 0; radius <= 4; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos base = destination.offset(dx, 0, dz);
                    for (int dy = 3; dy >= -5; dy--) {
                        BlockPos feet = base.offset(0, dy, 0);
                        BlockPos ground = feet.below();
                        if (level.getBlockState(feet).isAir()
                                && level.getBlockState(feet.above()).isAir()
                                && !level.getBlockState(ground).isAir()
                                && !AshfallInteractionRules.hasFluid(level.getBlockState(ground))) {
                            return feet;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public int getFastTravelCount() {
        return fastTravelCount;
    }
    
    @Override
    public void serialize(ValueOutput output) {
        output.putInt("discoveredCount", discoveredStations.size());
        int i = 0;
        for (String station : discoveredStations) {
            output.putString("discovered_" + i++, station);
        }
        
        output.putInt("activatedCount", activatedStations.size());
        i = 0;
        for (String station : activatedStations) {
            output.putString("activated_" + i++, station);
        }
        
        output.putLong("lastFastTravel", lastFastTravelTick);
        output.putInt("fastTravelCount", fastTravelCount);
    }
    
    @Override
    public void deserialize(ValueInput input) {
        discoveredStations.clear();
        int discoveredCount = input.getIntOr("discoveredCount", 0);
        for (int i = 0; i < discoveredCount; i++) {
            String station = input.getStringOr("discovered_" + i, "");
            if (!station.isEmpty()) discoveredStations.add(station);
        }
        
        activatedStations.clear();
        int activatedCount = input.getIntOr("activatedCount", 0);
        for (int i = 0; i < activatedCount; i++) {
            String station = input.getStringOr("activated_" + i, "");
            if (!station.isEmpty()) activatedStations.add(station);
        }
        
        lastFastTravelTick = input.getLongOr("lastFastTravel", 0);
        fastTravelCount = input.getIntOr("fastTravelCount", 0);
    }
    
    /**
     * Get RadioNetwork data for a player
     */
    public static RadioNetwork get(Player player) {
        return player.getData(com.knoxhack.echoashfallprotocol.registry.ModAttachments.RADIO_NETWORK.get());
    }
    
    /**
     * Station information record
     */
    public static class StationInfo {
        private final String id;
        private final String name;
        private final BlockPos position;
        
        public StationInfo(String id, String name, BlockPos position) {
            this.id = id;
            this.name = name;
            this.position = position;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public BlockPos getPosition() { return position; }
        public double getX() { return position.getX(); }
        public double getY() { return position.getY(); }
        public double getZ() { return position.getZ(); }
    }
}
