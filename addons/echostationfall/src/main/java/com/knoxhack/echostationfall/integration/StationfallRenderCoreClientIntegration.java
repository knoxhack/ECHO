package com.knoxhack.echostationfall.integration;

import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echorendercore.client.EchoRenderCoreMobFamilyRenderer;
import com.knoxhack.echostationfall.EchoStationfall;
import com.knoxhack.echostationfall.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class StationfallRenderCoreClientIntegration {
    private StationfallRenderCoreClientIntegration() {
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOLLOW_CREWMAN.get(), renderer("hollow_crewman", EchoMobFamily.STATION_SUIT, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.EVA_STALKER.get(), renderer("eva_stalker", EchoMobFamily.STATION_SUIT, 1.08F, 0.56F));
        event.registerEntityRenderer(ModEntities.MEDICAL_HUSK.get(), renderer("medical_husk", EchoMobFamily.STATION_SUIT, 1.0F, 0.48F));
        event.registerEntityRenderer(ModEntities.HYDROPONIC_GROWTH.get(), renderer("hydroponic_growth", EchoMobFamily.HUMANOID, 0.86F, 0.42F));
        event.registerEntityRenderer(ModEntities.MAINTENANCE_DRONE.get(), renderer("maintenance_drone", EchoMobFamily.DRONE, 0.86F, 0.32F));
        event.registerEntityRenderer(ModEntities.SCREAMING_SIGNAL.get(), renderer("screaming_signal", EchoMobFamily.WRAITH, 0.9F, 0.25F));
        event.registerEntityRenderer(ModEntities.STATION_MIMIC.get(), renderer("station_mimic", EchoMobFamily.HUMANOID, 0.92F, 0.44F));
        event.registerEntityRenderer(ModEntities.SUIT_WITHOUT_BODY.get(), renderer("suit_without_body", EchoMobFamily.STATION_SUIT, 1.12F, 0.58F));
        event.registerEntityRenderer(ModEntities.STATION_MOTHER.get(), renderer("station_mother", EchoMobFamily.HEAVY_BOSS, 1.35F, 0.8F));
    }

    private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
            float scale, float shadow) {
        return context -> new EchoRenderCoreMobFamilyRenderer<>(context, EchoStationfall.MODID, entityName, family, scale, shadow);
    }
}
