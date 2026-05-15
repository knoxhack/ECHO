package com.knoxhack.echocore.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class EchoMobModelFactory {
    private EchoMobModelFactory() {
    }

    public static EntityModel<EchoMobRenderState> create(EntityRendererProvider.Context context, EchoMobFamily family) {
        return switch (family) {
            case HUMANOID -> new EchoMobModels.EchoHumanoidModel(context.bakeLayer(EchoMobModelLayers.HUMANOID));
            case SURVIVOR_NPC -> new EchoMobModels.EchoSurvivorNpcModel(context.bakeLayer(EchoMobModelLayers.SURVIVOR_NPC));
            case STATION_SUIT -> new EchoMobModels.EchoStationSuitModel(context.bakeLayer(EchoMobModelLayers.STATION_SUIT));
            case WRAITH -> new EchoMobModels.EchoWraithModel(context.bakeLayer(EchoMobModelLayers.WRAITH));
            case DRONE -> new EchoMobModels.EchoDroneModel(context.bakeLayer(EchoMobModelLayers.DRONE));
            case QUADRUPED -> new EchoMobModels.EchoQuadrupedModel(context.bakeLayer(EchoMobModelLayers.QUADRUPED));
            case CRAWLER -> new EchoMobModels.EchoCrawlerModel(context.bakeLayer(EchoMobModelLayers.CRAWLER));
            case SLIME -> new EchoMobModels.EchoSlimeModel(context.bakeLayer(EchoMobModelLayers.SLIME));
            case HEAVY_BOSS -> new EchoMobModels.EchoHeavyBossModel(context.bakeLayer(EchoMobModelLayers.HEAVY_BOSS));
            case INDUSTRIAL_CONSTRUCT -> new EchoMobModels.EchoIndustrialConstructModel(context.bakeLayer(EchoMobModelLayers.INDUSTRIAL_CONSTRUCT));
            case ROCKET -> new EchoMobModels.EchoRocketModel(context.bakeLayer(EchoMobModelLayers.ROCKET));
        };
    }
}
