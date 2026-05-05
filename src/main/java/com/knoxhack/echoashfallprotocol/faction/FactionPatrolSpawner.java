package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

/**
 * Spawns faction patrols near villages and in contested territories.
 * Patrols attack enemy faction mobs and players with hostile reputation.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class FactionPatrolSpawner {
    
    // Tracked active patrols
    private static final Map<UUID, FactionPatrol> activePatrols = new HashMap<>();
    
    // Spawn check interval
    private static int spawnCheckCounter = 0;
    private static final int SPAWN_CHECK_INTERVAL = 6000; // 5 minutes
    
    // Maximum patrols per faction per player area
    private static final int MAX_PATROLS_PER_FACTION = 2;
    
    // Patrol radius around villages
    private static final int PATROL_RADIUS = 80;
    
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        // Clean up dead patrols
        cleanupDeadPatrols();
        
        // Spawn check
        spawnCheckCounter++;
        if (spawnCheckCounter >= SPAWN_CHECK_INTERVAL) {
            spawnCheckCounter = 0;
            checkAndSpawnPatrols(serverLevel);
        }
        
        // Update patrol behavior
        tickPatrols(serverLevel);
    }
    
    /**
     * Check for patrol spawning opportunities
     */
    private static void checkAndSpawnPatrols(ServerLevel level) {
        Random random = new Random();
        
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) continue;
            
            FactionTerritory territory = player.getData(ModAttachments.FACTION_TERRITORY.get());
            FactionDiplomacy diplomacy = player.getData(ModAttachments.FACTION_DIPLOMACY.get());
            // Check each faction for patrol spawning
            for (ReputationData.Faction faction : ReputationData.Faction.values()) {
                int currentPatrols = countFactionPatrols(faction, player.blockPosition());
                if (currentPatrols >= MAX_PATROLS_PER_FACTION) continue;
                
                // Get player's reputation with this faction
                int rep = AshfallFactionBridge.reputation(player, faction);
                
                // Spawn chance based on reputation and territory control
                double spawnChance = calculateSpawnChance(faction, rep, diplomacy, territory, player, level);
                
                if (random.nextDouble() < spawnChance) {
                    // Find spawn location near a faction village
                    FactionTerritory.VillageControl village = territory.getNearestVillage(player.blockPosition(), faction);
                    if (village != null && village.center.distSqr(player.blockPosition()) < 40000) { // Within 200 blocks
                        BlockPos spawnPos = findValidSpawnPos(level, village.center, PATROL_RADIUS);
                        if (spawnPos != null) {
                            spawnPatrol(level, faction, spawnPos, player, village);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Calculate spawn chance based on multiple factors
     */
    private static double calculateSpawnChance(ReputationData.Faction faction, int reputation, 
                                               FactionDiplomacy diplomacy, FactionTerritory territory,
                                               ServerPlayer player, ServerLevel level) {
        double baseChance = 0.06;
        
        // Reputation modifier
        if (reputation >= ReputationData.FRIENDLY_THRESHOLD) {
            baseChance += 0.10;
        } else if (reputation < 0) {
            baseChance -= 0.03;
        }

        FactionTerritory.VillageControl localSafehouse = territory.getNearestVillage(player.blockPosition(), faction);
        if (localSafehouse != null && localSafehouse.center.distSqr(player.blockPosition()) < 40000) {
            baseChance += 0.05;
        }
        
        // Territory control modifier
        String biomeKey = player.level().getBiome(player.blockPosition()).toString();
        int influence = territory.getBiomeInfluence(faction, biomeKey);
        baseChance += influence / 700.0;
        
        // War state modifier - more patrols during conflicts
        for (ReputationData.Faction other : ReputationData.Faction.values()) {
            if (other == faction) continue;
            FactionDiplomacy.DiplomaticState state = diplomacy.getState(faction, other);
            if (state == FactionDiplomacy.DiplomaticState.OPEN_WAR) {
                baseChance += 0.12;
            } else if (state == FactionDiplomacy.DiplomaticState.SKIRMISH) {
                baseChance += 0.06;
            }
        }
        
        return Math.min(0.30, Math.max(0, baseChance));
    }
    
    /**
     * Spawn a faction patrol at the given location
     */
    private static void spawnPatrol(ServerLevel level, ReputationData.Faction faction, 
                                  BlockPos pos, ServerPlayer player,
                                  FactionTerritory.VillageControl homeVillage) {
        Random random = new Random();
        int patrolSize = 2 + random.nextInt(3); // 2-4 members
        
        List<Mob> patrolMembers = new ArrayList<>();
        
        for (int i = 0; i < patrolSize; i++) {
            Mob member = spawnPatrolMember(level, faction, pos);
            if (member != null) {
                patrolMembers.add(member);
                
                // Configure AI based on faction
                configurePatrolAI(member, faction, player, homeVillage);
            }
        }
        
        if (!patrolMembers.isEmpty()) {
            FactionPatrol patrol = new FactionPatrol(faction, patrolMembers, homeVillage, player.getUUID());
            activePatrols.put(patrol.getId(), patrol);
            
            // Notify player if friendly
            if (AshfallFactionBridge.reputation(player, faction) >= 25) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "\u00A7a[ECHO-7]\u00A7r " + faction.getDisplayName() + " patrol spotted nearby."
                ));
            }
        }
    }
    
    /**
     * Spawn a single patrol member
     */
    private static Mob spawnPatrolMember(ServerLevel level, ReputationData.Faction faction, BlockPos pos) {
        EntityType<? extends Mob> entityType = getPatrolEntityType(faction);
        if (entityType == null) return null;
        
        Mob mob = entityType.create(level, EntitySpawnReason.EVENT);
        if (mob != null) {
            // Random offset from spawn point
            Random random = new Random();
            double offsetX = (random.nextDouble() - 0.5) * 10;
            double offsetZ = (random.nextDouble() - 0.5) * 10;
            mob.setPos(pos.getX() + offsetX, pos.getY(), pos.getZ() + offsetZ);
            level.addFreshEntity(mob);
        }
        return mob;
    }
    
    /**
     * Get entity type for patrol members based on faction
     */
    private static EntityType<? extends Mob> getPatrolEntityType(ReputationData.Faction faction) {
        return switch (faction) {
            case REMNANTS -> ModEntities.REMNANT_SOLDIER.get();
            case SALVAGERS -> ModEntities.SALVAGER_TRADER.get();
            case MUTANTS -> ModEntities.MUTANT_CREATURE.get();
        };
    }
    
    /**
     * Configure patrol AI based on faction and player reputation
     */
    private static void configurePatrolAI(Mob member, ReputationData.Faction faction, 
                                         ServerPlayer player, FactionTerritory.VillageControl homeVillage) {
        int rep = AshfallFactionBridge.reputation(player, faction);
        
        // Always target enemy faction mobs
        member.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(member, Monster.class, false) {
            @Override
            public boolean canUse() {
                // Only target mobs from hostile factions
                if (this.target != null && this.target instanceof Mob) {
                    ReputationData.Faction targetFaction = identifyMobFaction((Mob) this.target);
                    if (targetFaction != null && targetFaction != faction) {
                        FactionDiplomacy diplomacy = player.getData(ModAttachments.FACTION_DIPLOMACY.get());
                        FactionDiplomacy.DiplomaticState state = diplomacy.getState(faction, targetFaction);
                        return state.isConflict() || state == FactionDiplomacy.DiplomaticState.COLD_WAR;
                    }
                }
                return super.canUse();
            }
        });
        
        // Target players only once their faction tier is actually hostile.
        if (rep <= ReputationData.HOSTILE_THRESHOLD) {
            member.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(member, Player.class, false) {
                @Override
                public boolean canUse() {
                    if (this.target instanceof Player targetPlayer) {
                        // Check if this player is the owner or has bad reputation
                        return AshfallFactionBridge.reputation(targetPlayer, faction) <= ReputationData.HOSTILE_THRESHOLD;
                    }
                    return false;
                }
            });
        }
    }
    
    /**
     * Identify faction of a mob
     */
    private static ReputationData.Faction identifyMobFaction(net.minecraft.world.entity.LivingEntity mob) {
        String entityId = mob.getType().getDescriptionId();
        
        if (entityId.contains("military") || entityId.contains("soldier") || entityId.contains("remnant") ||
            entityId.contains("guard") || entityId.contains("pillager")) {
            return ReputationData.Faction.REMNANTS;
        } else if (entityId.contains("scavenger") || entityId.contains("bandit") || entityId.contains("salvager") ||
                   entityId.contains("raider") || entityId.contains("vindicator")) {
            return ReputationData.Faction.SALVAGERS;
        } else if (entityId.contains("mutant") || entityId.contains("feral") || entityId.contains("ghoul") ||
                   entityId.contains("infected") || entityId.contains("zombie")) {
            return ReputationData.Faction.MUTANTS;
        }
        return null;
    }
    
    /**
     * Find valid spawn position
     */
    private static BlockPos findValidSpawnPos(ServerLevel level, BlockPos center, int radius) {
        Random random = new Random();
        
        for (int attempts = 0; attempts < 10; attempts++) {
            int offsetX = random.nextInt(radius * 2) - radius;
            int offsetZ = random.nextInt(radius * 2) - radius;
            BlockPos pos = center.offset(offsetX, 0, offsetZ);
            
            // Find ground level
            for (int y = 10; y > -10; y--) {
                BlockPos check = pos.offset(0, y, 0);
                if (!level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                    return check.above();
                }
            }
        }
        return null;
    }
    
    /**
     * Tick all active patrols
     */
    private static void tickPatrols(ServerLevel level) {
        for (FactionPatrol patrol : activePatrols.values()) {
            patrol.tick(level);
        }
    }
    
    /**
     * Remove dead/invalid patrols
     */
    private static void cleanupDeadPatrols() {
        Iterator<Map.Entry<UUID, FactionPatrol>> iterator = activePatrols.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FactionPatrol> entry = iterator.next();
            if (entry.getValue().isInvalid()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Count active patrols for a faction near a position
     */
    private static int countFactionPatrols(ReputationData.Faction faction, BlockPos pos) {
        int count = 0;
        for (FactionPatrol patrol : activePatrols.values()) {
            if (patrol.getFaction() == faction && patrol.isNear(pos)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Represents a faction patrol group
     */
    private static class FactionPatrol {
        private final UUID id;
        private final ReputationData.Faction faction;
        private final List<Mob> members;
        private final FactionTerritory.VillageControl homeVillage;
        private final UUID ownerUUID;
        private int patrolTicks = 0;
        private BlockPos currentTargetPos;
        
        FactionPatrol(ReputationData.Faction faction, List<Mob> members, 
                     FactionTerritory.VillageControl homeVillage, UUID ownerUUID) {
            this.id = UUID.randomUUID();
            this.faction = faction;
            this.members = members;
            this.homeVillage = homeVillage;
            this.ownerUUID = ownerUUID;
            this.currentTargetPos = homeVillage.center;
        }
        
        void tick(ServerLevel level) {
            patrolTicks++;
            
            // Every 30 seconds, pick new patrol target
            if (patrolTicks % 600 == 0) {
                pickNewPatrolTarget();
            }
            
            // Clean up dead members
            members.removeIf(m -> !m.isAlive());
            
            // Move members toward patrol target if they don't have a combat target
            for (Mob member : members) {
                if (member.isAlive() && member.getTarget() == null && currentTargetPos != null) {
                    // Simple movement toward target
                    if (member.distanceToSqr(currentTargetPos.getX(), currentTargetPos.getY(), currentTargetPos.getZ()) > 100) {
                        member.getNavigation().moveTo(currentTargetPos.getX(), currentTargetPos.getY(), currentTargetPos.getZ(), 1.0);
                    }
                }
            }
        }
        
        void pickNewPatrolTarget() {
            // Random point within patrol radius of home village
            Random random = new Random();
            int offsetX = random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS;
            int offsetZ = random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS;
            currentTargetPos = homeVillage.center.offset(offsetX, 0, offsetZ);
        }
        
        boolean isInvalid() {
            return members.isEmpty();
        }
        
        boolean isNear(BlockPos pos) {
            return homeVillage.center.distSqr(pos) < 250000; // Within 500 blocks
        }
        
        UUID getId() { return id; }
        ReputationData.Faction getFaction() { return faction; }
    }
}
