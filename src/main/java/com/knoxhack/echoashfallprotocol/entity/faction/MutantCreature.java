package com.knoxhack.echoashfallprotocol.entity.faction;

import com.knoxhack.echoashfallprotocol.faction.AshfallFactionBridge;
import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * Mutant Creature - Faction mob for the Mutant Front.
 * Aggressive mutated humanoid creature.
 * Hostile to all non-mutant players.
 * Applies radiation/poison effects on attack.
 * Drops mutation-related materials.
 */
public class MutantCreature extends Monster {
    
    public MutantCreature(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.0F));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        // Attack all players (mutants are aggressive)
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    
    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        if (super.doHurtTarget(level, target)) {
            // Apply poison/radiation effect on hit
            if (target instanceof Player player) {
                int reputation = AshfallFactionBridge.reputation(player, ReputationData.Faction.MUTANTS);
                
                // Even allied players get poisoned (mutants can't control their mutations)
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                
                // Extra damage for enemies of mutants
                if (reputation < -30) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, wasRecentlyHit);
        
        // Drop mutation-related materials
        this.spawnAtLocation(level, ModItems.MUTATED_TISSUE.get());
        
        if (this.random.nextFloat() < 0.4f) {
            this.spawnAtLocation(level, ModItems.MUTATED_TISSUE.get());
        }
        
        if (this.random.nextFloat() < 0.25f) {
            this.spawnAtLocation(level, ModItems.SCRAP_CIRCUIT.get());
        }
        
        // Chance to drop rare items
        if (this.random.nextFloat() < 0.15f) {
            this.spawnAtLocation(level, ModItems.SCRAP_CIRCUIT.get());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5)
                .add(Attributes.ARMOR, 2.0);
    }
    
}
