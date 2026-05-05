package com.knoxhack.echoashfallprotocol.entity;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.boss.BiomeBossEntity;
import com.knoxhack.echoashfallprotocol.entity.boss.NexusFinalBossEntity;
import com.knoxhack.echoashfallprotocol.entity.boss.NexusFinalBossEntity;
import com.knoxhack.echoashfallprotocol.entity.boss.WardenBossEntity;
import com.knoxhack.echoashfallprotocol.entity.faction.FactionNpcEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity registry for ECHO: ASHFALL PROTOCOL custom mobs.
 */
public class ModEntities {
    public static final DeferredRegister.Entities ENTITIES =
            DeferredRegister.createEntities(EchoAshfallProtocol.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RadZombie>> RAD_ZOMBIE =
            ENTITIES.registerEntityType("rad_zombie", RadZombie::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<ScavengerBandit>> SCAVENGER_BANDIT =
            ENTITIES.registerEntityType("scavenger_bandit", ScavengerBandit::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<IrradiatedWolf>> IRRADIATED_WOLF =
            ENTITIES.registerEntityType("irradiated_wolf", IrradiatedWolf::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 0.85F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<EchoDrone>> ECHO_DRONE =
            ENTITIES.registerEntityType("echo_drone", EchoDrone::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.5F, 0.5F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<ScoutDrone>> SCOUT_DRONE =
            ENTITIES.registerEntityType("scout_drone", ScoutDrone::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).clientTrackingRange(64).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<EchoCompanionDrone>> ECHO_COMPANION_DRONE =
            ENTITIES.registerEntityType("echo_companion_drone", EchoCompanionDrone::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).clientTrackingRange(64).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<GlowingGhoul>> GLOWING_GHOUL =
            ENTITIES.registerEntityType("glowing_ghoul", GlowingGhoul::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(64).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<AshWraith>> ASH_WRAITH =
            ENTITIES.registerEntityType("ash_wraith", AshWraith::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.9F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<ToxicSlime>> TOXIC_SLIME =
            ENTITIES.registerEntityType("toxic_slime", ToxicSlime::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.8F, 0.8F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<CityStalker>> CITY_STALKER =
            ENTITIES.registerEntityType("city_stalker", CityStalker::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.8F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<RustWalker>> RUST_WALKER =
            ENTITIES.registerEntityType("rust_walker", RustWalker::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.0F, 2.2F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<SteamWraith>> STEAM_WRAITH =
            ENTITIES.registerEntityType("steam_wraith", SteamWraith::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.9F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<MutatedCrawler>> MUTATED_CRAWLER =
            ENTITIES.registerEntityType("mutated_crawler", MutatedCrawler::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.5F, 0.7F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<WildDog>> WILD_DOG =
            ENTITIES.registerEntityType("wild_dog", WildDog::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.65F, 0.9F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<FeralHuman>> FERAL_HUMAN =
            ENTITIES.registerEntityType("feral_human", FeralHuman::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<CrashSurvivor>> CRASH_SURVIVOR =
            ENTITIES.registerEntityType("crash_survivor", CrashSurvivor::new, MobCategory.CREATURE,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(64));

    public static final DeferredHolder<EntityType<?>, EntityType<FactionNpcEntity>> FACTION_NPC =
            ENTITIES.registerEntityType("faction_npc", FactionNpcEntity::new, MobCategory.CREATURE,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(64));

    // Nexus Warfront pressure mobs
    public static final DeferredHolder<EntityType<?>, EntityType<NexusPressureMobEntity>> GRIDBOUND_HUSK =
            ENTITIES.registerEntityType("gridbound_husk", NexusPressureMobEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(80));

    public static final DeferredHolder<EntityType<?>, EntityType<NexusPressureMobEntity>> RELAY_WARDEN =
            ENTITIES.registerEntityType("relay_warden", NexusPressureMobEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.9F, 2.4F).clientTrackingRange(88).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<NexusPressureMobEntity>> SIGNAL_LEECH =
            ENTITIES.registerEntityType("signal_leech", NexusPressureMobEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.55F, 1.45F).clientTrackingRange(80));

    public static final DeferredHolder<EntityType<?>, EntityType<NexusPressureMobEntity>> NEXUS_NULLIFIER =
            ENTITIES.registerEntityType("nexus_nullifier", NexusPressureMobEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.8F, 2.3F).clientTrackingRange(96).fireImmune());

    // Boss Entities
    public static final DeferredHolder<EntityType<?>, EntityType<WardenBossEntity>> WARDEN_BOSS =
            ENTITIES.registerEntityType("warden_boss", WardenBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.5F, 3.0F).clientTrackingRange(128).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> WASTELAND_SENTINEL =
            ENTITIES.registerEntityType("wasteland_sentinel", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.0F, 2.5F).clientTrackingRange(96));

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> CRASH_ZONE_COLOSSUS =
            ENTITIES.registerEntityType("crash_zone_colossus", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.2F, 2.8F).clientTrackingRange(96).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> CRYOGENIC_OVERSEER =
            ENTITIES.registerEntityType("cryogenic_overseer", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.0F, 2.6F).clientTrackingRange(96).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> INDUSTRIAL_JUGGERNAUT =
            ENTITIES.registerEntityType("industrial_juggernaut", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.3F, 2.9F).clientTrackingRange(96).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> NEXUS_SCAR_AVATAR =
            ENTITIES.registerEntityType("nexus_scar_avatar", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.1F, 2.8F).clientTrackingRange(112).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> RADIATION_BEHEMOTH =
            ENTITIES.registerEntityType("radiation_behemoth", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.3F, 3.0F).clientTrackingRange(96).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> CITY_RUIN_STALKER =
            ENTITIES.registerEntityType("city_ruin_stalker", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.9F, 2.4F).clientTrackingRange(96));

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> PLAINS_WARLORD =
            ENTITIES.registerEntityType("plains_warlord", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.0F, 2.5F).clientTrackingRange(96));

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeBossEntity>> TOXIC_HIVE_MATRIARCH =
            ENTITIES.registerEntityType("toxic_hive_matriarch", BiomeBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.2F, 2.5F).clientTrackingRange(96));

    public static final DeferredHolder<EntityType<?>, EntityType<NexusFinalBossEntity>> CORRUPTION_BLOOM =
            ENTITIES.registerEntityType("corruption_bloom", NexusFinalBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.35F, 2.9F).clientTrackingRange(128).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<NexusFinalBossEntity>> SEVERANCE_ENGINE =
            ENTITIES.registerEntityType("severance_engine", NexusFinalBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.45F, 3.0F).clientTrackingRange(128).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<NexusFinalBossEntity>> MIRROR_COMMAND =
            ENTITIES.registerEntityType("mirror_command", NexusFinalBossEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.25F, 2.85F).clientTrackingRange(128).fireImmune());

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RAD_ZOMBIE.get(), RadZombie.createAttributes().build());
        event.put(SCAVENGER_BANDIT.get(), ScavengerBandit.createAttributes().build());
        event.put(IRRADIATED_WOLF.get(), IrradiatedWolf.createAttributes().build());
        event.put(ECHO_DRONE.get(), EchoDrone.createAttributes().build());
        event.put(SCOUT_DRONE.get(), ScoutDrone.createAttributes().build());
        event.put(ECHO_COMPANION_DRONE.get(), EchoCompanionDrone.createAttributes().build());
        event.put(GLOWING_GHOUL.get(), GlowingGhoul.createAttributes().build());
        event.put(ASH_WRAITH.get(), AshWraith.createAttributes().build());
        event.put(TOXIC_SLIME.get(), ToxicSlime.createAttributes().build());
        event.put(GRIDBOUND_HUSK.get(), NexusPressureMobEntity.createAttributes().build());
        event.put(RELAY_WARDEN.get(), NexusPressureMobEntity.createAttributes().build());
        event.put(SIGNAL_LEECH.get(), NexusPressureMobEntity.createAttributes().build());
        event.put(NEXUS_NULLIFIER.get(), NexusPressureMobEntity.createAttributes().build());
        event.put(CITY_STALKER.get(), CityStalker.createAttributes().build());
        event.put(RUST_WALKER.get(), RustWalker.createAttributes().build());
        event.put(STEAM_WRAITH.get(), SteamWraith.createAttributes().build());
        event.put(MUTATED_CRAWLER.get(), MutatedCrawler.createAttributes().build());
        event.put(WILD_DOG.get(), WildDog.createAttributes().build());
        event.put(FERAL_HUMAN.get(), FeralHuman.createAttributes().build());
        event.put(CRASH_SURVIVOR.get(), CrashSurvivor.createAttributes().build());
        event.put(FACTION_NPC.get(), FactionNpcEntity.createAttributes().build());
        
        // Boss Entities
        event.put(WARDEN_BOSS.get(), WardenBossEntity.createAttributes().build());
        event.put(WASTELAND_SENTINEL.get(), BiomeBossEntity.createAttributes().build());
        event.put(CRASH_ZONE_COLOSSUS.get(), BiomeBossEntity.createAttributes().build());
        event.put(CRYOGENIC_OVERSEER.get(), BiomeBossEntity.createAttributes().build());
        event.put(INDUSTRIAL_JUGGERNAUT.get(), BiomeBossEntity.createAttributes().build());
        event.put(NEXUS_SCAR_AVATAR.get(), BiomeBossEntity.createAttributes().build());
        event.put(RADIATION_BEHEMOTH.get(), BiomeBossEntity.createAttributes().build());
        event.put(CITY_RUIN_STALKER.get(), BiomeBossEntity.createAttributes().build());
        event.put(PLAINS_WARLORD.get(), BiomeBossEntity.createAttributes().build());
        event.put(TOXIC_HIVE_MATRIARCH.get(), BiomeBossEntity.createAttributes().build());
        event.put(CORRUPTION_BLOOM.get(), NexusFinalBossEntity.createAttributes().build());
        event.put(SEVERANCE_ENGINE.get(), NexusFinalBossEntity.createAttributes().build());
        event.put(MIRROR_COMMAND.get(), NexusFinalBossEntity.createAttributes().build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        registerMonsterSpawn(event, RAD_ZOMBIE);
        registerMonsterSpawn(event, SCAVENGER_BANDIT);
        registerMonsterSpawn(event, IRRADIATED_WOLF);
        registerMonsterSpawn(event, ECHO_DRONE);
        registerMonsterSpawn(event, GLOWING_GHOUL);
        registerMonsterSpawn(event, ASH_WRAITH);
        registerMonsterSpawn(event, TOXIC_SLIME);
        registerMonsterSpawn(event, GRIDBOUND_HUSK);
        registerMonsterSpawn(event, RELAY_WARDEN);
        registerMonsterSpawn(event, SIGNAL_LEECH);
        registerMonsterSpawn(event, NEXUS_NULLIFIER);
        registerMonsterSpawn(event, CITY_STALKER);
        registerMonsterSpawn(event, RUST_WALKER);
        registerMonsterSpawn(event, STEAM_WRAITH);
        registerMonsterSpawn(event, MUTATED_CRAWLER);
        registerMonsterSpawn(event, WILD_DOG);
        registerMonsterSpawn(event, FERAL_HUMAN);
        registerGroundMobSpawn(event, FACTION_NPC);
        registerGroundMobSpawn(event, WARDEN_BOSS);
        registerGroundMobSpawn(event, WASTELAND_SENTINEL);
        registerGroundMobSpawn(event, CRASH_ZONE_COLOSSUS);
        registerGroundMobSpawn(event, CRYOGENIC_OVERSEER);
        registerGroundMobSpawn(event, INDUSTRIAL_JUGGERNAUT);
        registerGroundMobSpawn(event, NEXUS_SCAR_AVATAR);
        registerGroundMobSpawn(event, RADIATION_BEHEMOTH);
        registerGroundMobSpawn(event, CITY_RUIN_STALKER);
        registerGroundMobSpawn(event, PLAINS_WARLORD);
        registerGroundMobSpawn(event, TOXIC_HIVE_MATRIARCH);
        registerGroundMobSpawn(event, CORRUPTION_BLOOM);
        registerGroundMobSpawn(event, SEVERANCE_ENGINE);
        registerGroundMobSpawn(event, MIRROR_COMMAND);

        registerGroundMobSpawn(event, CRASH_SURVIVOR);
        registerNoRestrictionSpawn(event, SCOUT_DRONE);
        registerNoRestrictionSpawn(event, ECHO_COMPANION_DRONE);
    }

    private static <T extends Monster> void registerMonsterSpawn(
            RegisterSpawnPlacementsEvent event,
            DeferredHolder<EntityType<?>, EntityType<T>> entity
    ) {
        event.register(
                entity.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    private static <T extends Mob> void registerGroundMobSpawn(
            RegisterSpawnPlacementsEvent event,
            DeferredHolder<EntityType<?>, EntityType<T>> entity
    ) {
        event.register(
                entity.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Mob.checkMobSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    private static <T extends Mob> void registerNoRestrictionSpawn(
            RegisterSpawnPlacementsEvent event,
            DeferredHolder<EntityType<?>, EntityType<T>> entity
    ) {
        event.register(
                entity.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }
}
