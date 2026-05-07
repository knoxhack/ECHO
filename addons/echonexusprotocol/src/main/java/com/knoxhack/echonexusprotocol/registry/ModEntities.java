package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.entity.ArchiveSeekerEntity;
import com.knoxhack.echonexusprotocol.entity.CoreSoldierEntity;
import com.knoxhack.echonexusprotocol.entity.CorruptionWardenEntity;
import com.knoxhack.echonexusprotocol.entity.DataWraithEntity;
import com.knoxhack.echonexusprotocol.entity.NexusGuardianEntity;
import com.knoxhack.echonexusprotocol.entity.NexusHuskEntity;
import com.knoxhack.echonexusprotocol.entity.NexusMobEntity;
import com.knoxhack.echonexusprotocol.entity.StaticCrawlerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Entities;

public final class ModEntities {
   public static final Entities ENTITIES = DeferredRegister.createEntities("echonexusprotocol");
   public static final DeferredHolder<EntityType<?>, EntityType<NexusHuskEntity>> NEXUS_HUSK = ENTITIES.registerEntityType("nexus_husk", NexusHuskEntity::new, MobCategory.MONSTER, builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(10));
   public static final DeferredHolder<EntityType<?>, EntityType<DataWraithEntity>> DATA_WRAITH = ENTITIES.registerEntityType("data_wraith", DataWraithEntity::new, MobCategory.MONSTER, builder -> builder.sized(0.55F, 1.3F).clientTrackingRange(10));
   public static final DeferredHolder<EntityType<?>, EntityType<StaticCrawlerEntity>> STATIC_CRAWLER = ENTITIES.registerEntityType("static_crawler", StaticCrawlerEntity::new, MobCategory.MONSTER, builder -> builder.sized(0.7F, 0.7F).clientTrackingRange(10));
   public static final DeferredHolder<EntityType<?>, EntityType<CoreSoldierEntity>> CORE_SOLDIER = ENTITIES.registerEntityType("core_soldier", CoreSoldierEntity::new, MobCategory.MONSTER, builder -> builder.sized(0.7F, 2.05F).clientTrackingRange(10));
   public static final DeferredHolder<EntityType<?>, EntityType<ArchiveSeekerEntity>> ARCHIVE_SEEKER = ENTITIES.registerEntityType("archive_seeker", ArchiveSeekerEntity::new, MobCategory.MONSTER, builder -> builder.sized(0.6F, 2.4F).clientTrackingRange(10));
   public static final DeferredHolder<EntityType<?>, EntityType<CorruptionWardenEntity>> CORRUPTION_WARDEN = ENTITIES.registerEntityType("corruption_warden", CorruptionWardenEntity::new, MobCategory.MONSTER, builder -> builder.sized(0.9F, 2.6F).clientTrackingRange(10));
   public static final DeferredHolder<EntityType<?>, EntityType<NexusGuardianEntity>> NEXUS_GUARDIAN = ENTITIES.registerEntityType("nexus_guardian", NexusGuardianEntity::new, MobCategory.MONSTER, builder -> builder.sized(1.35F, 3.2F).clientTrackingRange(10));
   private ModEntities() {}
   public static void register(IEventBus eventBus) { ENTITIES.register(eventBus); }
   public static void registerAttributes(EntityAttributeCreationEvent event) { event.put(NEXUS_HUSK.get(), NexusMobEntity.createAttributes().build()); event.put(DATA_WRAITH.get(), NexusMobEntity.createAttributes().add(Attributes.MAX_HEALTH, 24.0).build()); event.put(STATIC_CRAWLER.get(), NexusMobEntity.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.42).build()); event.put(CORE_SOLDIER.get(), NexusMobEntity.createAttributes().add(Attributes.ARMOR, 8.0).build()); event.put(ARCHIVE_SEEKER.get(), NexusMobEntity.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.32).build()); event.put(CORRUPTION_WARDEN.get(), NexusMobEntity.createAttributes().add(Attributes.MAX_HEALTH, 110.0).add(Attributes.ATTACK_DAMAGE, 9.0).build()); event.put(NEXUS_GUARDIAN.get(), NexusMobEntity.createAttributes().add(Attributes.MAX_HEALTH, 260.0).add(Attributes.ATTACK_DAMAGE, 13.0).add(Attributes.ARMOR, 12.0).build()); }
   public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) { registerMonsterSpawn(event, NEXUS_HUSK); registerMonsterSpawn(event, DATA_WRAITH); registerMonsterSpawn(event, STATIC_CRAWLER); registerMonsterSpawn(event, CORE_SOLDIER); registerMonsterSpawn(event, ARCHIVE_SEEKER); registerBossSpawn(event, CORRUPTION_WARDEN); registerBossSpawn(event, NEXUS_GUARDIAN); }
   private static <T extends Monster> void registerMonsterSpawn(RegisterSpawnPlacementsEvent event, DeferredHolder<EntityType<?>, EntityType<T>> entity) { event.register(entity.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> Monster.checkMonsterSpawnRules(type, level, reason, pos, random), Operation.REPLACE); }
   private static <T extends Monster> void registerBossSpawn(RegisterSpawnPlacementsEvent event, DeferredHolder<EntityType<?>, EntityType<T>> entity) { event.register(entity.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> reason != net.minecraft.world.entity.EntitySpawnReason.NATURAL && reason != net.minecraft.world.entity.EntitySpawnReason.CHUNK_GENERATION && Monster.checkMonsterSpawnRules(type, level, reason, pos, random), Operation.REPLACE); }
}
