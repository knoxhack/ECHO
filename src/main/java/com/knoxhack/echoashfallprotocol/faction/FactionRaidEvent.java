package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Represents an ongoing raid between two factions.
 * Spawns waves of faction mobs attacking a target location.
 */
public class FactionRaidEvent {
    
    public enum RaidStatus {
        PREPARING("Preparing", 0xFFFFA94D),
        ACTIVE("Active", 0xFFFF3333),
        DEFENDER_VICTORY("Defenders Win", 0xFF42D67E),
        ATTACKER_VICTORY("Attackers Win", 0xFFFF3333),
        EXPIRED("Expired", 0xFF8A9BB0);
        
        private final String displayName;
        private final int color;
        
        RaidStatus(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }
    }
    
    private final UUID raidId;
    private final ReputationData.Faction attackingFaction;
    private final ReputationData.Faction defendingFaction;
    private final BlockPos targetLocation;
    private final Level level;
    
    private RaidStatus status = RaidStatus.PREPARING;
    private int currentWave = 0;
    private int maxWaves;
    private int ticksUntilNextWave;
    private int totalTicks = 0;
    private int attackersKilled = 0;
    private int defendersKilled = 0;
    
    private final List<Mob> activeAttackers = new ArrayList<>();
    private final List<Mob> activeDefenders = new ArrayList<>();
    
    private static final int PREPARE_TICKS = 600; // 30 seconds
    private static final int WAVE_INTERVAL = 400; // 20 seconds between waves
    private static final int RAID_RADIUS = 50;
    
    private final Random random = new Random();
    
    public FactionRaidEvent(ReputationData.Faction attacker, ReputationData.Faction defender,
                            BlockPos target, Level level, int waves) {
        this.raidId = UUID.randomUUID();
        this.attackingFaction = attacker;
        this.defendingFaction = defender;
        this.targetLocation = target;
        this.level = level;
        this.maxWaves = waves;
        this.ticksUntilNextWave = PREPARE_TICKS;
    }
    
    /**
     * Tick the raid event - call every server tick
     */
    public void tick() {
        if (status != RaidStatus.ACTIVE && status != RaidStatus.PREPARING) {
            return;
        }
        
        totalTicks++;
        
        // Clean up dead entities from tracking lists
        int attackersBefore = activeAttackers.size();
        int defendersBefore = activeDefenders.size();
        
        activeAttackers.removeIf(e -> !e.isAlive());
        activeDefenders.removeIf(e -> !e.isAlive());
        
        // Track kills
        attackersKilled += attackersBefore - activeAttackers.size();
        defendersKilled += defendersBefore - activeDefenders.size();
        
        if (status == RaidStatus.PREPARING) {
            ticksUntilNextWave--;
            if (ticksUntilNextWave <= 0) {
                startRaid();
            }
            return;
        }
        
        // Active raid logic
        ticksUntilNextWave--;
        
        // Check for wave completion
        if (activeAttackers.isEmpty() && currentWave > 0) {
            if (currentWave >= maxWaves) {
                endRaid(RaidStatus.DEFENDER_VICTORY);
                return;
            }
            // Start next wave
            spawnNextWave();
        }
        
        // Spawn next wave on interval
        if (ticksUntilNextWave <= 0 && currentWave < maxWaves) {
            spawnNextWave();
        }
        
        // Check for defender defeat
        if (areDefendersDefeated()) {
            endRaid(RaidStatus.ATTACKER_VICTORY);
        }
        
        // Timeout check (30 minutes)
        if (totalTicks > 36000) {
            endRaid(RaidStatus.EXPIRED);
        }
    }
    
    private void startRaid() {
        status = RaidStatus.ACTIVE;
        announceRaidStart();
        spawnNextWave();
    }
    
    private void spawnNextWave() {
        currentWave++;
        ticksUntilNextWave = WAVE_INTERVAL;
        
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        // Calculate spawn positions around the target
        int attackersPerWave = 3 + (currentWave * 2);
        
        for (int i = 0; i < attackersPerWave; i++) {
            double angle = (2 * Math.PI * i) / attackersPerWave;
            int spawnX = targetLocation.getX() + (int)(Math.cos(angle) * RAID_RADIUS);
            int spawnZ = targetLocation.getZ() + (int)(Math.sin(angle) * RAID_RADIUS);
            BlockPos spawnPos = findValidSpawnPos(serverLevel, spawnX, targetLocation.getY(), spawnZ);
            
            if (spawnPos != null) {
                spawnAttacker(serverLevel, spawnPos);
            }
        }
        
        // Spawn defenders if first wave
        if (currentWave == 1) {
            int defenders = 2 + maxWaves;
            for (int i = 0; i < defenders; i++) {
                BlockPos defPos = targetLocation.offset(
                    random.nextInt(10) - 5,
                    0,
                    random.nextInt(10) - 5
                );
                spawnDefender(serverLevel, defPos);
            }
        }
        
        // Notify nearby players
        notifyPlayers("§c[RAID ALERT] Wave " + currentWave + "/" + maxWaves + " incoming!");
    }
    
    private void spawnAttacker(ServerLevel level, BlockPos pos) {
        EntityType<? extends Mob> attackerType = getFactionAttackerType(attackingFaction);
        Mob attacker = attackerType.create(level, EntitySpawnReason.EVENT);
        if (attacker != null) {
            attacker.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            
            // Set AI to attack defenders and players
            attacker.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(attacker, Player.class, true));
            
            level.addFreshEntity(attacker);
            activeAttackers.add(attacker);
        }
    }
    
    private void spawnDefender(ServerLevel level, BlockPos pos) {
        EntityType<? extends Mob> defenderType = getFactionDefenderType(defendingFaction);
        Mob defender = defenderType.create(level, EntitySpawnReason.EVENT);
        if (defender != null) {
            defender.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            
            // Defenders target attackers
            for (Mob attacker : activeAttackers) {
                if (attacker.isAlive()) {
                    defender.setTarget(attacker);
                    break;
                }
            }
            
            level.addFreshEntity(defender);
            activeDefenders.add(defender);
        }
    }
    
    private EntityType<? extends Mob> getFactionAttackerType(ReputationData.Faction faction) {
        // Would map to actual faction mob entities
        return switch (faction) {
            case REMNANTS -> ModEntities.REMNANT_SOLDIER.get();
            case SALVAGERS -> ModEntities.SALVAGER_TRADER.get();
            case MUTANTS -> ModEntities.MUTANT_CREATURE.get();
        };
    }
    
    private EntityType<? extends Mob> getFactionDefenderType(ReputationData.Faction faction) {
        return switch (faction) {
            case REMNANTS -> ModEntities.REMNANT_SOLDIER.get();
            case SALVAGERS -> ModEntities.SALVAGER_TRADER.get();
            case MUTANTS -> ModEntities.MUTANT_CREATURE.get();
        };
    }
    
    private BlockPos findValidSpawnPos(ServerLevel level, int x, int y, int z) {
        // Find ground level
        BlockPos pos = new BlockPos(x, y, z);
        for (int i = 10; i > -10; i--) {
            BlockPos check = pos.offset(0, i, 0);
            if (!level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                return check.above();
            }
        }
        return null;
    }
    
    private boolean areDefendersDefeated() {
        return activeDefenders.isEmpty() && currentWave >= maxWaves / 2;
    }
    
    private void endRaid(RaidStatus result) {
        this.status = result;
        
        // Clear remaining entities
        for (Mob attacker : activeAttackers) {
            if (attacker.isAlive()) {
                attacker.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
        }
        
        String message = switch (result) {
            case DEFENDER_VICTORY -> "§aThe defenders of " + defendingFaction.getDisplayName() + " have repelled the attack!";
            case ATTACKER_VICTORY -> "§cThe " + attackingFaction.getDisplayName() + " have overrun the position!";
            case EXPIRED -> "§7The raid has ended without resolution.";
            default -> "§7The raid has concluded.";
        };
        
        notifyPlayers(message);
        
        // Apply territory changes
        applyRaidOutcome(result);
    }
    
    private void applyRaidOutcome(RaidStatus result) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB notifyArea = new AABB(targetLocation).inflate(RAID_RADIUS * 4);
        for (ServerPlayer player : serverLevel.getPlayers(p -> notifyArea.contains(p.getX(), p.getY(), p.getZ()))) {
            FactionTerritory territory = player.getData(ModAttachments.FACTION_TERRITORY.get());
            FactionDiplomacy diplomacy = player.getData(ModAttachments.FACTION_DIPLOMACY.get());
            String biomeKey = player.level().getBiome(targetLocation).toString();

            if (result == RaidStatus.ATTACKER_VICTORY) {
                territory.transferVillageControl(targetLocation, attackingFaction);
                territory.modifyBiomeInfluence(attackingFaction, biomeKey, 12);
                territory.modifyBiomeInfluence(defendingFaction, biomeKey, -10);
                FactionDiplomacy.FactionPair pair = FactionDiplomacy.FactionPair.fromFactions(attackingFaction, defendingFaction);
                if (pair != null) diplomacy.modifyRelation(pair, -4);
            } else if (result == RaidStatus.DEFENDER_VICTORY) {
                territory.modifyBiomeInfluence(defendingFaction, biomeKey, 8);
                FactionDiplomacy.FactionPair pair = FactionDiplomacy.FactionPair.fromFactions(attackingFaction, defendingFaction);
                if (pair != null) diplomacy.modifyRelation(pair, -2);
                FactionQuestProgression.progress(player, FactionQuest.ObjectiveType.RAID_DEFENSE,
                    defendingFaction.getDisplayName().toLowerCase(), defendingFaction, 1);
                AshfallFactionContractProgression.progressRaidDefense(player, AshfallFactionBridge.coreFactionId(defendingFaction));
            }

            FactionTerritory.saveAndSync(player, territory);
            FactionDiplomacy.saveAndSync(player, diplomacy);

            var intel = player.getData(ModAttachments.ECHO_INTEL.get());
            intel.addTacticalIntel(
                "Raid Resolved",
                attackingFaction.getDisplayName() + " vs " + defendingFaction.getDisplayName() + ": " + result.getDisplayName(),
                result == RaidStatus.ATTACKER_VICTORY ? attackingFaction : defendingFaction,
                result == RaidStatus.EXPIRED ? com.knoxhack.echoashfallprotocol.echo.EchoIntel.IntelPriority.LOW : com.knoxhack.echoashfallprotocol.echo.EchoIntel.IntelPriority.HIGH
            );
            com.knoxhack.echoashfallprotocol.echo.EchoIntel.saveAndSync(player, intel);
        }
    }
    
    private void announceRaidStart() {
        String msg = "§4[FACTION WAR] " + attackingFaction.getDisplayName() + 
                     " are attacking a " + defendingFaction.getDisplayName() + " position!";
        notifyPlayers(msg);
    }
    
    private void notifyPlayers(String message) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        AABB notifyArea = new AABB(targetLocation).inflate(RAID_RADIUS * 2);
        for (ServerPlayer player : serverLevel.getPlayers(p -> notifyArea.contains(p.getX(), p.getY(), p.getZ()))) {
            player.sendSystemMessage(Component.literal(message));
        }
    }
    
    // Getters
    public UUID getRaidId() { return raidId; }
    public RaidStatus getStatus() { return status; }
    public ReputationData.Faction getAttackingFaction() { return attackingFaction; }
    public ReputationData.Faction getDefendingFaction() { return defendingFaction; }
    public BlockPos getTargetLocation() { return targetLocation; }
    public Level getTargetLevel() { return level; }
    public int getCurrentWave() { return currentWave; }
    public int getMaxWaves() { return maxWaves; }
    public boolean isActive() { return status == RaidStatus.ACTIVE || status == RaidStatus.PREPARING; }
}
