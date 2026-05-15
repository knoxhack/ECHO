package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echorendercore.client.EchoRenderCoreMobFamilyRenderer;
import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.client.RenderCoreEmergencyRocketRenderer;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class OrbitalRenderCoreClientIntegration {
    private OrbitalRenderCoreClientIntegration() {
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.EMERGENCY_ROCKET_VEHICLE.get(), RenderCoreEmergencyRocketRenderer::new);
        event.registerEntityRenderer(ModEntities.ECHO_DEFENSE_DRONE.get(), renderer("echo_defense_drone", EchoMobFamily.DRONE, 1.0F, 0.34F));
        event.registerEntityRenderer(ModEntities.VACUUM_WRAITH.get(), renderer("vacuum_wraith", EchoMobFamily.WRAITH, 1.15F, 0.25F));
        event.registerEntityRenderer(ModEntities.CORRUPTED_DOCKING_AI.get(), renderer("corrupted_docking_ai", EchoMobFamily.DRONE, 1.35F, 0.44F));
        event.registerEntityRenderer(ModEntities.BROKEN_ASTRONAUT.get(), renderer("broken_astronaut", EchoMobFamily.STATION_SUIT, 1.0F, 0.52F));
        event.registerEntityRenderer(ModEntities.NEXUS_HUSK.get(), renderer("nexus_husk", EchoMobFamily.HUMANOID, 1.05F, 0.56F));
        event.registerEntityRenderer(ModEntities.LUNAR_NEXUS_HUSK.get(), renderer("lunar_nexus_husk", EchoMobFamily.STATION_SUIT, 1.22F, 0.68F));
        event.registerEntityRenderer(ModEntities.ABANDONED_CAPTAIN.get(), renderer("abandoned_captain", EchoMobFamily.STATION_SUIT, 1.18F, 0.72F));
        event.registerEntityRenderer(ModEntities.ECHO_ZERO.get(), renderer("echo_zero", EchoMobFamily.HEAVY_BOSS, 1.35F, 0.9F));
        event.registerEntityRenderer(ModEntities.EUROPA_CRYO_WARDEN.get(), renderer("europa_cryo_warden", EchoMobFamily.DRONE, 1.45F, 0.58F));
        event.registerEntityRenderer(ModEntities.SATURN_RELAY_SENTINEL.get(), renderer("saturn_relay_sentinel", EchoMobFamily.DRONE, 1.55F, 0.6F));
        event.registerEntityRenderer(ModEntities.TITAN_METHANE_STALKER.get(), renderer("titan_methane_stalker", EchoMobFamily.HUMANOID, 1.18F, 0.66F));
        event.registerEntityRenderer(ModEntities.ORBITAL_FACTION_NPC.get(), renderer("orbital_faction_npc", EchoMobFamily.SURVIVOR_NPC, 1.0F, 0.5F));
    }

    private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
            float scale, float shadow) {
        return context -> new EchoRenderCoreMobFamilyRenderer<>(context, EchoOrbitalRemnants.MODID, entityName, family, scale, shadow);
    }
}
