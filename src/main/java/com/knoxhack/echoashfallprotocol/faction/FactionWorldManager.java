package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

/**
 * Manages faction world events, diplomatic relations, and raid triggers.
 * Runs periodically to update faction states and spawn dynamic events.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class FactionWorldManager {
    
    // Active raid events being tracked
    private static final Map<UUID, FactionRaidEvent> activeRaids = new HashMap<>();
    
    // Pending raids with warning timer
    private static final List<PendingRaid> pendingRaids = new ArrayList<>();
    private static final int RAID_WARNING_TICKS = 6000; // 5 minutes warning
    private static final float SKIRMISH_RAID_CHANCE = 0.08f;
    private static final float WAR_RAID_CHANCE = 0.12f;
    private static final double DUPLICATE_RAID_DISTANCE_SQR = 10000.0D;
    
    // Tick counters
    private static int diplomacyTickCounter = 0;
    private static int raidCheckCounter = 0;
    private static int intelDecayCounter = 0;
    
    // Intervals (in ticks)
    private static final int DIPLOMACY_TICK_INTERVAL = 24000; // 20 minutes
    private static final int RAID_CHECK_INTERVAL = 12000;     // 10 minutes
    private static final int INTEL_DECAY_INTERVAL = 72000;    // 1 hour
    
    /**
     * Represents a raid that has been detected but not yet started.
     * Gives players time to prepare.
     */
    private static class PendingRaid {
        final ReputationData.Faction attacker;
        final ReputationData.Faction defender;
        final net.minecraft.core.BlockPos target;
        final ServerLevel level;
        final int waves;
        final long startTime;
        int warningTicksRemaining;
        boolean warningBroadcast = false;
        boolean imminentBroadcast = false;
        
        PendingRaid(ReputationData.Faction attacker, ReputationData.Faction defender,
                    net.minecraft.core.BlockPos target, ServerLevel level, int waves) {
            this.attacker = attacker;
            this.defender = defender;
            this.target = target;
            this.level = level;
            this.waves = waves;
            this.startTime = level.getGameTime();
            this.warningTicksRemaining = RAID_WARNING_TICKS;
        }
    }
    
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        // Game time available for future use
        
        // Tick pending raids (warning countdown)
        tickPendingRaids();
        
        // Tick active raids
        tickActiveRaids();
        
        // Diplomacy relations update
        diplomacyTickCounter++;
        if (diplomacyTickCounter >= DIPLOMACY_TICK_INTERVAL) {
            diplomacyTickCounter = 0;
            updateDiplomaticRelations(serverLevel);
        }
        
        // Raid spawn check
        raidCheckCounter++;
        if (raidCheckCounter >= RAID_CHECK_INTERVAL) {
            raidCheckCounter = 0;
            checkForRaidTriggers(serverLevel);
        }
        
        // Intel decay/refresh for all players
        intelDecayCounter++;
        if (intelDecayCounter >= INTEL_DECAY_INTERVAL) {
            intelDecayCounter = 0;
            refreshPlayerIntel(serverLevel);
        }
    }
    
    /**
     * Update diplomatic relations for all players
     */
    private static void updateDiplomaticRelations(ServerLevel level) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            FactionDiplomacy diplomacy = player.getData(ModAttachments.FACTION_DIPLOMACY.get());
            diplomacy.tickRelations(level.getGameTime());
            
            // Check for significant state changes
            for (FactionDiplomacy.FactionPair pair : FactionDiplomacy.FactionPair.values()) {
                FactionDiplomacy.DiplomaticState state = diplomacy.getState(pair);
                
                // Notify player of significant diplomatic shifts
                if (state == FactionDiplomacy.DiplomaticState.OPEN_WAR && 
                    diplomacy.getRelation(pair) == -75) { // Just entered war state
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A74[FACTION WAR]\u00A7r " + pair.getFactionA().getDisplayName() + 
                        " and " + pair.getFactionB().getDisplayName() + " are now at open war!"
                    ));
                }
            }
            
            // Save updated data
            FactionDiplomacy.saveAndSync(player, diplomacy);
        }
    }
    
    /**
     * Check if any faction pairs should trigger a raid
     */
    private static void checkForRaidTriggers(ServerLevel level) {
        Random random = new Random();
        
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            FactionDiplomacy diplomacy = player.getData(ModAttachments.FACTION_DIPLOMACY.get());
            
            // Check each faction pair for conflict states
            for (FactionDiplomacy.FactionPair pair : FactionDiplomacy.FactionPair.values()) {
                FactionDiplomacy.DiplomaticState state = diplomacy.getState(pair);
                
                // Raids only trigger during SKIRMISH or OPEN_WAR
                if (state != FactionDiplomacy.DiplomaticState.SKIRMISH && 
                    state != FactionDiplomacy.DiplomaticState.OPEN_WAR) {
                    continue;
                }
                
                float triggerChance = state == FactionDiplomacy.DiplomaticState.OPEN_WAR
                    ? WAR_RAID_CHANCE
                    : SKIRMISH_RAID_CHANCE;
                if (random.nextFloat() < triggerChance) {
                    ReputationData.Faction attacker = pair.getFactionA();
                    ReputationData.Faction defender = pair.getFactionB();

                    com.knoxhack.echoashfallprotocol.faction.FactionTerritory territory = 
                        player.getData(ModAttachments.FACTION_TERRITORY.get());
                    com.knoxhack.echoashfallprotocol.faction.FactionTerritory.VillageControl targetVillage = 
                        territory.getNearestVillage(player.blockPosition(), defender);
                    
                    if (targetVillage != null) {
                        // Check distance - must be within 500 blocks
                        if (targetVillage.center.distSqr(player.blockPosition()) < 250000) {
                            int waves = state == FactionDiplomacy.DiplomaticState.OPEN_WAR ? 4 : 2;
                            if (!hasRaidNear(level, targetVillage.center)) {
                                addPendingRaid(level, attacker, defender, targetVillage.center, waves);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Start a new faction raid event
     */
    public static void startRaid(ServerLevel level, ReputationData.Faction attacker, 
                                  ReputationData.Faction defender, 
                                  net.minecraft.core.BlockPos target, int waves) {
        FactionRaidEvent raid = new FactionRaidEvent(attacker, defender, target, level, waves);
        activeRaids.put(raid.getRaidId(), raid);
        
        // Notify nearby players
        notifyNearbyPlayers(level, target, 
            "\u00A74[RAID ALERT]\u00A7r " + attacker.getDisplayName() + " forces approaching " + 
            defender.getDisplayName() + " position!");
    }
    
    /**
     * Add a pending raid with warning period
     */
    private static void addPendingRaid(ServerLevel level, ReputationData.Faction attacker,
                                       ReputationData.Faction defender,
                                       net.minecraft.core.BlockPos target, int waves) {
        if (hasRaidNear(level, target)) {
            return;
        }
        PendingRaid pending = new PendingRaid(attacker, defender, target, level, waves);
        pendingRaids.add(pending);
        
        // Broadcast initial warning immediately via ECHO-7 and drone
        broadcastRaidWarning(level, pending, false);
        
        // Add intel entry for players
        for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() == level && player.blockPosition().distSqr(target) < 250000) {
                var echoIntel = player.getData(ModAttachments.ECHO_INTEL.get());
                echoIntel.addTacticalIntel(
                    "Incoming Raid Detected",
                    attacker.getDisplayName() + " forces assembling near " + defender.getDisplayName() + " territory.",
                    attacker,
                    com.knoxhack.echoashfallprotocol.echo.EchoIntel.IntelPriority.HIGH
                );
                com.knoxhack.echoashfallprotocol.echo.EchoIntel.saveAndSync(player, echoIntel);
            }
        }
    }
    
    /**
     * Tick pending raids - countdown and broadcast warnings
     */
    private static void tickPendingRaids() {
        Iterator<PendingRaid> iterator = pendingRaids.iterator();
        
        while (iterator.hasNext()) {
            PendingRaid pending = iterator.next();
            pending.warningTicksRemaining--;
            
            // Broadcast warnings at key intervals
            if (!pending.warningBroadcast && pending.warningTicksRemaining <= RAID_WARNING_TICKS - 100) {
                // Initial warning broadcast (5 seconds after detection)
                pending.warningBroadcast = true;
            }
            
            // 1 minute warning (1200 ticks remaining)
            if (!pending.imminentBroadcast && pending.warningTicksRemaining <= 1200) {
                pending.imminentBroadcast = true;
                broadcastRaidWarning(pending.level, pending, true);
            }
            
            // Start the raid when countdown reaches 0
            if (pending.warningTicksRemaining <= 0) {
                iterator.remove();
                startRaid(pending.level, pending.attacker, pending.defender, 
                         pending.target, pending.waves);
            }
        }
    }
    
    /**
     * Broadcast raid warning through ECHO-7 and drone
     */
    private static void broadcastRaidWarning(ServerLevel level, PendingRaid pending, boolean imminent) {
        String eta = imminent ? "1 MINUTE" : "5 MINUTES";
        String severity = imminent ? "\u00A74" : "\u00A7c";
        String prep = raidPrepHint(pending.attacker);
        
        // Broadcast to all players in the dimension
        for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() == level) {
                // Check if player has a drone nearby to relay the message
                boolean hasDrone = hasPlayerDroneNearby(level, player, pending.target);
                
                if (hasDrone || player.blockPosition().distSqr(pending.target) < 100000) {
                    String direction = getDirectionFromPlayer(player.blockPosition(), pending.target);
                    String message = severity + "[RAID WARNING]\u00A7r " + pending.attacker.getDisplayName()
                        + " raid " + direction + " toward " + pending.defender.getDisplayName()
                        + " territory. ETA: " + eta + ". Prep: " + prep;

                    // Player has drone or is within ~300 blocks of target
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
                    
                    // If imminent and player has drone, make the drone speak
                    if (imminent && hasDrone) {
                        speakThroughDrone(level, player, 
                            "RAID IMMINENT! " + pending.attacker.getDisplayName() + " forces approaching "
                                + pending.defender.getDisplayName() + " territory. Prep: " + prep);
                    }
                }
            }
        }
    }
    
    /**
     * Check if player has a drone within range of the target
     */
    private static boolean hasPlayerDroneNearby(ServerLevel level, net.minecraft.server.level.ServerPlayer player,
                                                 net.minecraft.core.BlockPos target) {
        var drones = level.getEntitiesOfClass(
            com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.class,
            new net.minecraft.world.phys.AABB(target).inflate(150),
            drone -> {
                java.util.UUID ownerUUID = drone.getOwnerUUID();
                return ownerUUID != null && ownerUUID.equals(player.getUUID());
            }
        );
        return !drones.isEmpty();
    }
    
    /**
     * Make the player's drone speak a message
     */
    private static void speakThroughDrone(ServerLevel level, net.minecraft.server.level.ServerPlayer player, String message) {
        var drones = level.getEntitiesOfClass(
            com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.class,
            new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(50),
            drone -> {
                java.util.UUID ownerUUID = drone.getOwnerUUID();
                return ownerUUID != null && ownerUUID.equals(player.getUUID());
            }
        );
        
        for (var drone : drones) {
            drone.speak(message, EchoCompanionDrone.MOOD_URGENT, 100, 20);
        }
    }
    
    /**
     * Get rough direction from a player to a target position.
     */
    private static String getDirectionFromPlayer(BlockPos playerPos, net.minecraft.core.BlockPos target) {
        int dx = target.getX() - playerPos.getX();
        int dz = target.getZ() - playerPos.getZ();
        
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? "to the East" : "to the West";
        } else {
            return dz > 0 ? "to the South" : "to the North";
        }
    }

    private static String raidPrepHint(ReputationData.Faction attacker) {
        return switch (attacker) {
            case REMNANTS -> "armor, medicine, cover, and spare filters";
            case SALVAGERS -> "armor, a backup weapon, and inventory space for salvage";
            case MUTANTS -> "filters, RadAway, clean water, and medicine";
        };
    }

    private static boolean hasRaidNear(ServerLevel level, BlockPos target) {
        for (PendingRaid pending : pendingRaids) {
            if (pending.level == level && pending.target.distSqr(target) < DUPLICATE_RAID_DISTANCE_SQR) {
                return true;
            }
        }
        for (FactionRaidEvent raid : activeRaids.values()) {
            if (raid.getTargetLevel() == level
                    && raid.getTargetLocation() != null
                    && raid.getTargetLocation().distSqr(target) < DUPLICATE_RAID_DISTANCE_SQR) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tick all active raid events
     */
    private static void tickActiveRaids() {
        Iterator<Map.Entry<UUID, FactionRaidEvent>> iterator = activeRaids.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, FactionRaidEvent> entry = iterator.next();
            FactionRaidEvent raid = entry.getValue();
            
            raid.tick();
            
            // Remove completed raids
            if (!raid.isActive()) {
                iterator.remove();
                
                // Apply diplomatic consequences
                applyRaidConsequences(raid);
            }
        }
    }
    
    /**
     * Apply diplomatic changes based on raid outcome
     */
    private static void applyRaidConsequences(FactionRaidEvent raid) {
        if (raid.getTargetLocation() == null) return;
        for (ServerPlayer player : raidLevelPlayers(raid)) {
            var reputation = ReputationData.get(player);
            if (raid.getStatus() == FactionRaidEvent.RaidStatus.DEFENDER_VICTORY) {
                reputation.addReputation(raid.getDefendingFaction(), 2);
                AshfallFactionBridge.addReputation(player, raid.getDefendingFaction(), 2);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "\u00A7a[ECHO-7] " + raid.getDefendingFaction().getDisplayName() + " patrol confidence increased near this route."));
            } else if (raid.getStatus() == FactionRaidEvent.RaidStatus.ATTACKER_VICTORY) {
                reputation.addReputation(raid.getAttackingFaction(), 1);
                reputation.addReputation(raid.getDefendingFaction(), -2);
                AshfallFactionBridge.addReputation(player, raid.getAttackingFaction(), 1);
                AshfallFactionBridge.addReputation(player, raid.getDefendingFaction(), -2);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "\u00A7c[ECHO-7] Territory pressure shifted toward " + raid.getAttackingFaction().getDisplayName() + ". Vendor safety may vary."));
            }
            ReputationData.saveAndSync(player, reputation);
        }
    }

    private static List<ServerPlayer> raidLevelPlayers(FactionRaidEvent raid) {
        if (!(raid.getTargetLevel() instanceof ServerLevel level)) {
            return java.util.Collections.emptyList();
        }
        var area = new net.minecraft.world.phys.AABB(raid.getTargetLocation()).inflate(240.0D);
        return level.getPlayers(player -> area.contains(player.getX(), player.getY(), player.getZ()));
    }
    
    /**
     * Notify players near a location
     */
    private static void notifyNearbyPlayers(ServerLevel level, net.minecraft.core.BlockPos pos, String message) {
        double radius = 100.0;
        var aabb = new net.minecraft.world.phys.AABB(pos).inflate(radius);
        
        for (ServerPlayer player : level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()))) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
        }
    }
    
    /**
     * Refresh intel for all players (mark old intel as stale, trigger new collection opportunities)
     */
    private static void refreshPlayerIntel(ServerLevel level) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            var echoIntel = player.getData(ModAttachments.ECHO_INTEL.get());
            
            // Synthesize new insights from existing intel
            String insight = echoIntel.synthesizeInsight();
            if (insight != null && !insight.isEmpty()) {
                // Chance to add as a new intel entry
                if (new Random().nextFloat() < 0.3f) {
                    echoIntel.addIntel(new com.knoxhack.echoashfallprotocol.echo.EchoIntel.IntelEntry(
                        "synthesis_" + System.currentTimeMillis(),
                        com.knoxhack.echoashfallprotocol.echo.EchoIntel.IntelType.TACTICAL,
                        "ECHO Analysis",
                        insight,
                        com.knoxhack.echoashfallprotocol.echo.EchoIntel.IntelPriority.MEDIUM,
                        null
                    ));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A78[ECHO-7]\u00A7r New analysis available in faction terminal."
                    ));
                }
            }
            
            com.knoxhack.echoashfallprotocol.echo.EchoIntel.saveAndSync(player, echoIntel);
        }
    }
    
    /**
     * Triggered when player completes a quest - affects diplomatic relations
     */
    public static void onPlayerQuestComplete(ServerPlayer player, ReputationData.Faction faction, int reputationChange) {
        FactionDiplomacy diplomacy = player.getData(ModAttachments.FACTION_DIPLOMACY.get());
        
        // Helping one faction affects relations with their enemies
        for (ReputationData.Faction other : ReputationData.Faction.values()) {
            if (other == faction) continue;
            
            FactionDiplomacy.FactionPair pair = FactionDiplomacy.FactionPair.fromFactions(faction, other);
            if (pair == null) continue;
            
            int currentRelation = diplomacy.getRelation(pair);
            
            // If factions are hostile, helping one hurts the other
            if (currentRelation < 0) {
                // Slight negative drift for the enemy faction toward the helped faction
                diplomacy.modifyRelation(pair, -2);
            }
        }
        
        FactionDiplomacy.saveAndSync(player, diplomacy);
    }
    
    /**
     * Check if there are any active raids
     */
    public static boolean hasActiveRaids() {
        return !activeRaids.isEmpty();
    }
    
    /**
     * Get count of active raids
     */
    public static int getActiveRaidCount() {
        return activeRaids.size();
    }
}
