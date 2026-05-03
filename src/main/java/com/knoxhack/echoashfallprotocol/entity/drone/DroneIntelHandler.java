package com.knoxhack.echoashfallprotocol.entity.drone;

import com.knoxhack.echoashfallprotocol.echo.EchoIntel;
import com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone;
import com.knoxhack.echoashfallprotocol.faction.FactionDiplomacy;
import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Handles ECHO drone intelligence gathering capabilities.
 * Integrates with the drone's patrol and combat behaviors to collect faction intel.
 */
public class DroneIntelHandler {
    
    private final Random random = new Random();
    private int tickCounter = 0;
    
    // Intel collection cooldowns per drone
    private int reconCooldown = 0;
    private int interceptCooldown = 0;
    
    /**
     * Called each tick when the drone is in SCOUT mode
     */
    public void tickScoutMode(EchoCompanionDrone drone, ServerLevel level) {
        if (reconCooldown > 0) {
            reconCooldown--;
            return;
        }
        
        tickCounter++;
        if (tickCounter < 100) return; // Check every 5 seconds
        tickCounter = 0;
        
        Player owner = getOwner(drone, level);
        if (owner == null) return;
        
        BlockPos dronePos = drone.blockPosition();
        
        // Check for nearby faction villages
        var territory = owner.getData(ModAttachments.FACTION_TERRITORY.get());
        var nearestVillage = territory.getNearestVillage(dronePos, null);
        
        if (nearestVillage != null && dronePos.distSqr(nearestVillage.center) < 2500) { // Within 50 blocks
            // Check if we already have intel on this village recently
            var echoIntel = owner.getData(ModAttachments.ECHO_INTEL.get());
            
            if (!hasRecentIntel(echoIntel, nearestVillage.controllingFaction)) {
                // Gather recon intel
                String title = "Village Recon: " + nearestVillage.name;
                String content = String.format(
                    "Location: %d, %d, %d | Faction: %s | Status: %s",
                    nearestVillage.center.getX(),
                    nearestVillage.center.getY(),
                    nearestVillage.center.getZ(),
                    nearestVillage.controllingFaction.getDisplayName(),
                    isVillageUnderThreat(level, nearestVillage) ? "THREAT DETECTED" : "Secure"
                );
                
                echoIntel.addReconIntel(title, content, nearestVillage.controllingFaction, 
                    EchoIntel.IntelPriority.MEDIUM);
                owner.setData(ModAttachments.ECHO_INTEL.get(), echoIntel);
                
                // Notify player
                if (random.nextFloat() < 0.3f) {
                    owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A78[ECHO-7]\u00A7r Scout update: " + nearestVillage.name + " surveyed."
                    ));
                }
                
                reconCooldown = 600; // 30 second cooldown
            }
        }
        
        // Check for faction mobs (tactical intel)
        scanForFactionMobs(drone, level, owner);
    }
    
    /**
     * Called each tick when the drone is in COMBAT mode
     */
    public void tickCombatMode(EchoCompanionDrone drone, ServerLevel level) {
        Player owner = getOwner(drone, level);
        if (owner == null) return;
        
        // Get faction diplomacy to identify enemies
        FactionDiplomacy diplomacy = owner.getData(ModAttachments.FACTION_DIPLOMACY.get());
        
        // Scan for hostile faction mobs
        AABB scanArea = new AABB(drone.blockPosition()).inflate(30);
        var nearbyMobs = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, scanArea);
        
        for (var mob : nearbyMobs) {
            // Identify faction affiliation from entity type/name
            ReputationData.Faction mobFaction = identifyMobFaction(mob);
            if (mobFaction == null) continue;
            
            // Check if this faction is hostile to the player
            for (ReputationData.Faction playerFaction : ReputationData.Faction.values()) {
                if (diplomacy.isAtWar(playerFaction, mobFaction) || 
                    diplomacy.getState(playerFaction, mobFaction).isConflict()) {
                    
                    // Priority target
                    if (drone.getTarget() == null || !drone.getTarget().isAlive()) {
                        // Mark for drone targeting
                        // This would integrate with drone AI
                    }
                }
            }
        }
    }
    
    /**
     * Attempt to intercept faction radio transmissions
     * Called periodically in all modes
     */
    public void tryInterceptTransmission(EchoCompanionDrone drone, ServerLevel level) {
        if (interceptCooldown > 0) {
            interceptCooldown--;
            return;
        }
        
        Player owner = getOwner(drone, level);
        if (owner == null) return;
        
        // 5% chance per check to intercept
        if (random.nextFloat() > 0.05f) return;
        
        // Check for nearby faction activity
        BlockPos dronePos = drone.blockPosition();
        var territory = owner.getData(ModAttachments.FACTION_TERRITORY.get());
        var nearestVillage = territory.getNearestVillage(dronePos, null);
        
        if (nearestVillage == null || dronePos.distSqr(nearestVillage.center) > 10000) return;
        
        // Generate intercepted message based on diplomatic state
        FactionDiplomacy diplomacy = owner.getData(ModAttachments.FACTION_DIPLOMACY.get());
        String transmission = generateTransmission(nearestVillage.controllingFaction, diplomacy, level);
        
        if (transmission != null) {
            var echoIntel = owner.getData(ModAttachments.ECHO_INTEL.get());
            echoIntel.addInterceptedTransmission(
                "Intercepted " + nearestVillage.controllingFaction.getDisplayName() + " Comms",
                transmission,
                nearestVillage.controllingFaction
            );
            owner.setData(ModAttachments.ECHO_INTEL.get(), echoIntel);
            
            // Alert player to critical intel
            owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "\u00A7c[ECHO-7]\u00A7r Intercepted transmission from " + 
                nearestVillage.controllingFaction.getDisplayName() + "!"
            ));
            
            interceptCooldown = 1200; // 1 minute cooldown
        }
    }
    
    /**
     * Update faction dossiers based on player proximity to faction areas
     */
    public void updateDossierFromProximity(EchoCompanionDrone drone, ServerLevel level) {
        Player owner = getOwner(drone, level);
        if (owner == null) return;
        
        BlockPos pos = drone.blockPosition();
        var territory = owner.getData(ModAttachments.FACTION_TERRITORY.get());
        
        // Check biome influence at drone location
        // This would need biome lookup
        String biomeKey = level.getBiome(pos).toString();
        
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            int influence = territory.getBiomeInfluence(faction, biomeKey);
            if (influence >= 30) { // Significant presence
                var echoIntel = owner.getData(ModAttachments.ECHO_INTEL.get());
                int completion = echoIntel.getDossierCompletion(faction);
                
                if (completion < 100 && random.nextFloat() < 0.1f) {
                    echoIntel.updateDossier(faction, "Territory Presence", 
                        "Detected " + influence + "% faction influence in current region.");
                    owner.setData(ModAttachments.ECHO_INTEL.get(), echoIntel);
                }
            }
        }
    }
    
    private Player getOwner(EchoCompanionDrone drone, Level level) {
        if (level.isClientSide()) return null;
        
        // Get owner from drone's owner UUID
        java.util.UUID ownerUUID = drone.getOwnerUUID();
        if (ownerUUID == null) return null;
        
        return ((ServerLevel) level).getServer().getPlayerList().getPlayer(ownerUUID);
    }
    
    private boolean hasRecentIntel(EchoIntel intel, ReputationData.Faction faction) {
        var factionIntel = intel.getFactionIntel(faction);
        long now = System.currentTimeMillis();
        
        return factionIntel.stream()
            .filter(i -> i.type == EchoIntel.IntelType.RECON)
            .anyMatch(i -> (now - i.timestamp) < 300000); // Within 5 minutes
    }
    
    private boolean isVillageUnderThreat(ServerLevel level, com.knoxhack.echoashfallprotocol.faction.FactionTerritory.VillageControl village) {
        // Check for hostile mobs near village
        AABB area = new AABB(village.center).inflate(50);
        var hostileMobs = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, area, 
            e -> e instanceof net.minecraft.world.entity.monster.Monster);
        return hostileMobs.size() > 5;
    }
    
    private void scanForFactionMobs(EchoCompanionDrone drone, ServerLevel level, Player owner) {
        AABB scanArea = new AABB(drone.blockPosition()).inflate(40);
        var mobs = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, scanArea);
        
        int factionMobCount = 0;
        for (var mob : mobs) {
            if (identifyMobFaction(mob) != null) {
                factionMobCount++;
            }
        }
        
        if (factionMobCount >= 3 && random.nextFloat() < 0.2f) {
            var echoIntel = owner.getData(ModAttachments.ECHO_INTEL.get());
            echoIntel.addTacticalIntel("Patrol Sighting", 
                factionMobCount + " faction-aligned units detected in sector.", 
                null, EchoIntel.IntelPriority.LOW);
            owner.setData(ModAttachments.ECHO_INTEL.get(), echoIntel);
        }
    }
    
    private ReputationData.Faction identifyMobFaction(net.minecraft.world.entity.Mob mob) {
        String entityId = mob.getType().getDescriptionId();
        
        if (entityId.contains("military") || entityId.contains("soldier") || entityId.contains("remnant")) {
            return ReputationData.Faction.REMNANTS;
        } else if (entityId.contains("scavenger") || entityId.contains("bandit") || entityId.contains("salvager")) {
            return ReputationData.Faction.SALVAGERS;
        } else if (entityId.contains("mutant") || entityId.contains("feral") || entityId.contains("ghoul")) {
            return ReputationData.Faction.MUTANTS;
        }
        return null;
    }
    
    private String generateTransmission(ReputationData.Faction faction, FactionDiplomacy diplomacy, ServerLevel level) {
        List<String> messages = new java.util.ArrayList<>();
        
        // Messages based on diplomatic state
        for (ReputationData.Faction other : ReputationData.Faction.values()) {
            if (other == faction) continue;
            
            var state = diplomacy.getState(faction, other);
            switch (state) {
                case OPEN_WAR -> messages.add("Hostile forces detected near " + other.getDisplayName() + " sector.");
                case SKIRMISH -> messages.add("Skirmish reported on " + other.getDisplayName() + " border.");
                case TENSION -> messages.add("Increased patrols around " + other.getDisplayName() + " territory.");
                case ALLIANCE -> messages.add("Coordinated operation with " + other.getDisplayName() + " confirmed.");
                default -> {}
            }
        }
        
        // Generic messages
        messages.add("Supply convoy moving at coordinates...");
        messages.add("Requesting backup at outpost seven.");
        messages.add("Unknown contact spotted in sector four.");
        messages.add("Raid preparation complete. Awaiting go signal.");
        
        return messages.isEmpty() ? null : messages.get(random.nextInt(messages.size()));
    }
}
