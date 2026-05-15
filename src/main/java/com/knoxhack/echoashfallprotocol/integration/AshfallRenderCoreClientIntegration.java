package com.knoxhack.echoashfallprotocol.integration;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echorendercore.client.EchoRenderCoreMobFamilyRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class AshfallRenderCoreClientIntegration {
    private AshfallRenderCoreClientIntegration() {
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RAD_ZOMBIE.get(), renderer("rad_zombie", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.SCAVENGER_BANDIT.get(), renderer("scavenger_bandit", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.IRRADIATED_WOLF.get(), renderer("irradiated_wolf", EchoMobFamily.QUADRUPED, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.ECHO_DRONE.get(), renderer("echo_drone", EchoMobFamily.DRONE, 1.0F, 0.4F));
        event.registerEntityRenderer(ModEntities.SCOUT_DRONE.get(), renderer("scout_drone", EchoMobFamily.DRONE, 1.0F, 0.4F));
        event.registerEntityRenderer(ModEntities.GLOWING_GHOUL.get(), renderer("glowing_ghoul", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.ASH_WRAITH.get(), renderer("ash_wraith", EchoMobFamily.WRAITH, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.TOXIC_SLIME.get(), renderer("toxic_slime", EchoMobFamily.SLIME, 1.0F, 0.35F));
        event.registerEntityRenderer(ModEntities.GRIDBOUND_HUSK.get(), renderer("gridbound_husk", EchoMobFamily.HUMANOID, 1.0F, 0.55F));
        event.registerEntityRenderer(ModEntities.RELAY_WARDEN.get(), renderer("relay_warden", EchoMobFamily.HEAVY_BOSS, 1.0F, 0.85F));
        event.registerEntityRenderer(ModEntities.SIGNAL_LEECH.get(), renderer("signal_leech", EchoMobFamily.CRAWLER, 1.0F, 0.35F));
        event.registerEntityRenderer(ModEntities.NEXUS_NULLIFIER.get(), renderer("nexus_nullifier", EchoMobFamily.HUMANOID, 1.0F, 0.55F));
        event.registerEntityRenderer(ModEntities.CITY_STALKER.get(), renderer("city_stalker", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.RUST_WALKER.get(), renderer("rust_walker", EchoMobFamily.HEAVY_BOSS, 1.0F, 0.7F));
        event.registerEntityRenderer(ModEntities.STEAM_WRAITH.get(), renderer("steam_wraith", EchoMobFamily.WRAITH, 1.0F, 0.4F));
        event.registerEntityRenderer(ModEntities.MUTATED_CRAWLER.get(), renderer("mutated_crawler", EchoMobFamily.CRAWLER, 1.0F, 0.3F));
        event.registerEntityRenderer(ModEntities.ECHO_COMPANION_DRONE.get(), renderer("echo_companion_drone", EchoMobFamily.DRONE, 1.0F, 0.4F));
        event.registerEntityRenderer(ModEntities.WILD_DOG.get(), renderer("wild_dog", EchoMobFamily.QUADRUPED, 1.0F, 0.45F));
        event.registerEntityRenderer(ModEntities.FERAL_HUMAN.get(), renderer("feral_human", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.CRASH_SURVIVOR.get(), renderer("crash_survivor", EchoMobFamily.SURVIVOR_NPC, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.FACTION_NPC.get(), renderer("faction_npc", EchoMobFamily.SURVIVOR_NPC, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.WARDEN_BOSS.get(), renderer("warden_boss", EchoMobFamily.HEAVY_BOSS, 1.0F, 1.0F));
        event.registerEntityRenderer(ModEntities.WASTELAND_SENTINEL.get(), renderer("wasteland_sentinel", EchoMobFamily.HEAVY_BOSS, 1.0F, 0.9F));
        event.registerEntityRenderer(ModEntities.CRASH_ZONE_COLOSSUS.get(), renderer("crash_zone_colossus", EchoMobFamily.HEAVY_BOSS, 1.24F, 1.12F));
        event.registerEntityRenderer(ModEntities.CRYOGENIC_OVERSEER.get(), renderer("cryogenic_overseer", EchoMobFamily.HEAVY_BOSS, 1.04F, 0.9F));
        event.registerEntityRenderer(ModEntities.INDUSTRIAL_JUGGERNAUT.get(), renderer("industrial_juggernaut", EchoMobFamily.HEAVY_BOSS, 1.16F, 1.04F));
        event.registerEntityRenderer(ModEntities.NEXUS_SCAR_AVATAR.get(), renderer("nexus_scar_avatar", EchoMobFamily.HEAVY_BOSS, 1.18F, 1.08F));
        event.registerEntityRenderer(ModEntities.RADIATION_BEHEMOTH.get(), renderer("radiation_behemoth", EchoMobFamily.HEAVY_BOSS, 1.12F, 1.0F));
        event.registerEntityRenderer(ModEntities.CITY_RUIN_STALKER.get(), renderer("city_ruin_stalker", EchoMobFamily.HEAVY_BOSS, 0.92F, 0.68F));
        event.registerEntityRenderer(ModEntities.PLAINS_WARLORD.get(), renderer("plains_warlord", EchoMobFamily.HEAVY_BOSS, 1.02F, 0.88F));
        event.registerEntityRenderer(ModEntities.TOXIC_HIVE_MATRIARCH.get(), renderer("toxic_hive_matriarch", EchoMobFamily.HEAVY_BOSS, 1.05F, 0.92F));
        event.registerEntityRenderer(ModEntities.CORRUPTION_BLOOM.get(), renderer("corruption_bloom", EchoMobFamily.HEAVY_BOSS, 1.04F, 0.86F));
        event.registerEntityRenderer(ModEntities.SEVERANCE_ENGINE.get(), renderer("severance_engine", EchoMobFamily.HEAVY_BOSS, 1.14F, 0.86F));
        event.registerEntityRenderer(ModEntities.MIRROR_COMMAND.get(), renderer("mirror_command", EchoMobFamily.HEAVY_BOSS, 1.08F, 0.86F));
    }

    private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
            float scale, float shadow) {
        return context -> new EchoRenderCoreMobFamilyRenderer<>(context, EchoAshfallProtocol.MODID, entityName, family, scale, shadow);
    }
}
