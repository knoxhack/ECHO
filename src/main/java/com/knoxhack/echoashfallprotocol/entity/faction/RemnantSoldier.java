package com.knoxhack.echoashfallprotocol.entity.faction;

import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Remnant Soldier - Faction mob for the Remnant Collective.
 * Hostile to players with negative Remnant reputation.
 * Neutral/friendly to players with positive reputation.
 * Drops military gear and tech components.
 */
public class RemnantSoldier extends Monster {
    
    public RemnantSoldier(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.0F));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        // Only attack players with negative reputation
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                // Check if there's a valid target with negative reputation
                if (!super.canUse()) return false;
                if (this.target == null) return false;
                
                // Check reputation - only attack if negative reputation with Remnants
                ReputationData factionData = this.target.getData(ModAttachments.REPUTATION_DATA.get());
                int reputation = factionData.getReputation(ReputationData.Faction.REMNANTS);
                return reputation < 0; // Only attack players with negative reputation
            }
        });
    }
    
    /**
     * Determine if this soldier should attack a player based on reputation
     */
    private boolean shouldAttackPlayer(Player player) {
        // Get player's reputation with Remnant Collective
        ReputationData factionData = player.getData(ModAttachments.REPUTATION_DATA.get());
        int reputation = factionData.getReputation(ReputationData.Faction.REMNANTS);
        
        // Attack if reputation is negative (suspicious or worse)
        return reputation < 0;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, wasRecentlyHit);
        
        // Drop military gear and tech components
        // Use spawnAtLocation with ItemLike (the item type) - drops 1 item
        int alloyCount = 1 + this.random.nextInt(2);
        for (int i = 0; i < alloyCount; i++) {
            this.spawnAtLocation(level, ModItems.DENSE_ALLOY_CHUNK.get());
        }
        
        if (this.random.nextFloat() < 0.3f) {
            this.spawnAtLocation(level, ModItems.CIRCUIT_BOARD.get());
        }
        
        if (this.random.nextFloat() < 0.2f) {
            this.spawnAtLocation(level, ModItems.ENERGY_CELL.get());
        }
        
        // Chance to drop schematic fragment
        if (this.random.nextFloat() < 0.15f) {
            this.spawnAtLocation(level, ModItems.SCHEMATIC_FRAGMENT.get());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0);
    }
}
