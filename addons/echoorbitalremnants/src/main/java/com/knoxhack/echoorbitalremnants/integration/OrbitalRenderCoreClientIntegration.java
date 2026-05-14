package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.client.OrbitalRenderCoreVexRenderer;
import com.knoxhack.echoorbitalremnants.client.OrbitalRenderCoreZombieRenderer;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class OrbitalRenderCoreClientIntegration {
    private OrbitalRenderCoreClientIntegration() {
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerVex(event, ModEntities.ECHO_DEFENSE_DRONE.get(), "echo_defense_drone", 0xFF82E9FF, 1.0F, 0.34F);
        registerVex(event, ModEntities.VACUUM_WRAITH.get(), "vacuum_wraith", 0xFFD8E2FF, 1.15F, 0.25F);
        registerVex(event, ModEntities.CORRUPTED_DOCKING_AI.get(), "corrupted_docking_ai", 0xFFFF6868, 1.35F, 0.44F);
        registerZombie(event, ModEntities.BROKEN_ASTRONAUT.get(), "broken_astronaut", 0xFFBFD0D6, 1.0F, 0.52F);
        registerZombie(event, ModEntities.NEXUS_HUSK.get(), "nexus_husk", 0xFFD48BFF, 1.05F, 0.56F);
        registerZombie(event, ModEntities.LUNAR_NEXUS_HUSK.get(), "lunar_nexus_husk", 0xFFE09CFF, 1.22F, 0.68F);
        registerZombie(event, ModEntities.ABANDONED_CAPTAIN.get(), "abandoned_captain", 0xFF6D7B88, 1.18F, 0.72F);
        registerZombie(event, ModEntities.ECHO_ZERO.get(), "echo_zero", 0xFFFF5AF7, 1.35F, 0.9F);
        registerVex(event, ModEntities.EUROPA_CRYO_WARDEN.get(), "europa_cryo_warden", 0xFF7FE8FF, 1.45F, 0.58F);
        registerVex(event, ModEntities.SATURN_RELAY_SENTINEL.get(), "saturn_relay_sentinel", 0xFFFFE2B8, 1.55F, 0.6F);
        registerZombie(event, ModEntities.TITAN_METHANE_STALKER.get(), "titan_methane_stalker", 0xFFE58A45, 1.18F, 0.66F);
    }

    private static void registerZombie(EntityRenderersEvent.RegisterRenderers event,
          net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.monster.zombie.Zombie> type,
          String name, int tint, float scale, float shadow) {
        event.registerEntityRenderer(type, context -> new OrbitalRenderCoreZombieRenderer(
              context, entityTexture(name), profile(name), tint, scale, shadow));
    }

    private static void registerVex(EntityRenderersEvent.RegisterRenderers event,
          net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.monster.Vex> type,
          String name, int tint, float scale, float shadow) {
        event.registerEntityRenderer(type, context -> new OrbitalRenderCoreVexRenderer(
              context, entityTexture(name), profile(name), tint, scale, shadow));
    }

    private static Identifier entityTexture(String name) {
        return Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, "textures/entity/" + name + ".png");
    }

    private static Identifier profile(String name) {
        return Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, "echo_mobs/" + name);
    }
}
