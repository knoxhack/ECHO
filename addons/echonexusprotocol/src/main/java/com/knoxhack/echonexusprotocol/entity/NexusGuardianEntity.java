package com.knoxhack.echonexusprotocol.entity;

import com.knoxhack.echonexusprotocol.registry.ModSounds;
import com.knoxhack.echonexusprotocol.world.NexusWorldData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class NexusGuardianEntity extends NexusMobEntity {
   private int lastSoundPhase;

   public NexusGuardianEntity(EntityType<? extends NexusGuardianEntity> type, Level level) { super(type, level); }
   public int phase() { float ratio = this.getHealth() / Math.max(1.0F, this.getMaxHealth()); if (ratio > 0.75F) return 1; if (ratio > 0.5F) return 2; return ratio > 0.25F ? 3 : 4; }
   public void tick() { super.tick(); if (this.level() instanceof ServerLevel serverLevel) { int phase = this.phase(); if (phase != this.lastSoundPhase) { this.lastSoundPhase = phase; serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.7D, this.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.02D); serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.1D, this.getZ(), 18 + phase * 8, 1.5D, 1.0D, 1.5D, 0.12D); serverLevel.playSound(null, this.blockPosition(), ModSounds.GUARDIAN_PHASE.get(), SoundSource.HOSTILE, 0.9F, 0.75F + phase * 0.08F); NexusWorldData.get(serverLevel).startAnomalyStorm(this.chunkPosition(), serverLevel.getGameTime()); } if (this.tickCount % Math.max(35, 110 - phase * 18) == 0) { NexusWorldData data = NexusWorldData.get(serverLevel); data.addFieldValue(this.chunkPosition(), -phase); data.addCorruptionPressure(this.chunkPosition(), phase * 2); if (phase >= 3) data.markRealityTearActive(this.chunkPosition()); serverLevel.sendParticles(phase >= 3 ? ParticleTypes.PORTAL : ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.2D, this.getZ(), 10 + phase * 4, 0.8D + phase * 0.2D, 0.7D, 0.8D + phase * 0.2D, 0.08D); for (Player player : serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(7.0D + phase))) { player.hurtServer(serverLevel, this.damageSources().magic(), this.scaledBossDamage(2.5F + phase)); if (phase >= 2) { player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 0)); } if (phase >= 3) { player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0)); } if (phase >= 4) { player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0)); } } } } }
}
