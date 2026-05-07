package com.knoxhack.echoorbitalremnants.registry;

import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.entity.AbandonedCaptainEntity;
import com.knoxhack.echoorbitalremnants.entity.CorruptedDockingAiEntity;
import com.knoxhack.echoorbitalremnants.entity.BrokenAstronautEntity;
import com.knoxhack.echoorbitalremnants.entity.EchoDefenseDroneEntity;
import com.knoxhack.echoorbitalremnants.entity.EmergencyRocketEntity;
import com.knoxhack.echoorbitalremnants.entity.EchoZeroEntity;
import com.knoxhack.echoorbitalremnants.entity.EuropaCryoWardenEntity;
import com.knoxhack.echoorbitalremnants.entity.LunarNexusHuskEntity;
import com.knoxhack.echoorbitalremnants.entity.NexusHuskEntity;
import com.knoxhack.echoorbitalremnants.entity.SaturnRelaySentinelEntity;
import com.knoxhack.echoorbitalremnants.entity.TitanMethaneStalkerEntity;
import com.knoxhack.echoorbitalremnants.entity.VacuumWraithEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(EchoOrbitalRemnants.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EmergencyRocketEntity>> EMERGENCY_ROCKET_VEHICLE =
            ENTITIES.registerEntityType("emergency_rocket_vehicle", EmergencyRocketEntity::new, MobCategory.MISC,
                    builder -> builder.sized(1.4F, 3.5F).clientTrackingRange(12));

    public static final DeferredHolder<EntityType<?>, EntityType<EchoDefenseDroneEntity>> ECHO_DEFENSE_DRONE =
            ENTITIES.registerEntityType("echo_defense_drone", EchoDefenseDroneEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 0.6F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<VacuumWraithEntity>> VACUUM_WRAITH =
            ENTITIES.registerEntityType("vacuum_wraith", VacuumWraithEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.5F, 0.8F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<BrokenAstronautEntity>> BROKEN_ASTRONAUT =
            ENTITIES.registerEntityType("broken_astronaut", BrokenAstronautEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<NexusHuskEntity>> NEXUS_HUSK =
            ENTITIES.registerEntityType("nexus_husk", NexusHuskEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<CorruptedDockingAiEntity>> CORRUPTED_DOCKING_AI =
            ENTITIES.registerEntityType("corrupted_docking_ai", CorruptedDockingAiEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.9F, 0.9F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<LunarNexusHuskEntity>> LUNAR_NEXUS_HUSK =
            ENTITIES.registerEntityType("lunar_nexus_husk", LunarNexusHuskEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.7F, 2.2F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<AbandonedCaptainEntity>> ABANDONED_CAPTAIN =
            ENTITIES.registerEntityType("abandoned_captain", AbandonedCaptainEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.75F, 2.1F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<EchoZeroEntity>> ECHO_ZERO =
            ENTITIES.registerEntityType("echo_zero", EchoZeroEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.9F, 2.4F).clientTrackingRange(12));

    public static final DeferredHolder<EntityType<?>, EntityType<EuropaCryoWardenEntity>> EUROPA_CRYO_WARDEN =
            ENTITIES.registerEntityType("europa_cryo_warden", EuropaCryoWardenEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.7F, 1.0F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<SaturnRelaySentinelEntity>> SATURN_RELAY_SENTINEL =
            ENTITIES.registerEntityType("saturn_relay_sentinel", SaturnRelaySentinelEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.85F, 1.15F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<TitanMethaneStalkerEntity>> TITAN_METHANE_STALKER =
            ENTITIES.registerEntityType("titan_methane_stalker", TitanMethaneStalkerEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.7F, 2.05F).clientTrackingRange(10));

    private ModEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ECHO_DEFENSE_DRONE.get(), EchoDefenseDroneEntity.createAttributes().build());
        event.put(VACUUM_WRAITH.get(), VacuumWraithEntity.createAttributes().build());
        event.put(BROKEN_ASTRONAUT.get(), BrokenAstronautEntity.createAttributes().build());
        event.put(NEXUS_HUSK.get(), NexusHuskEntity.createAttributes().build());
        event.put(CORRUPTED_DOCKING_AI.get(), CorruptedDockingAiEntity.createAttributes().build());
        event.put(LUNAR_NEXUS_HUSK.get(), LunarNexusHuskEntity.createAttributes().build());
        event.put(ABANDONED_CAPTAIN.get(), AbandonedCaptainEntity.createAttributes().build());
        event.put(ECHO_ZERO.get(), EchoZeroEntity.createAttributes().build());
        event.put(EUROPA_CRYO_WARDEN.get(), EuropaCryoWardenEntity.createAttributes().build());
        event.put(SATURN_RELAY_SENTINEL.get(), SaturnRelaySentinelEntity.createAttributes().build());
        event.put(TITAN_METHANE_STALKER.get(), TitanMethaneStalkerEntity.createAttributes().build());
    }
}
