package com.knoxhack.echocore;

import com.knoxhack.echocore.client.model.EchoMobModelLayers;
import com.knoxhack.echocore.client.model.EchoMobModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = EchoCore.MODID, value = Dist.CLIENT)
public final class EchoCoreClient {
    private EchoCoreClient() {
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EchoMobModelLayers.HUMANOID, EchoMobModels::createHumanoidLayer);
        event.registerLayerDefinition(EchoMobModelLayers.SURVIVOR_NPC, EchoMobModels::createSurvivorNpcLayer);
        event.registerLayerDefinition(EchoMobModelLayers.STATION_SUIT, EchoMobModels::createStationSuitLayer);
        event.registerLayerDefinition(EchoMobModelLayers.WRAITH, EchoMobModels::createWraithLayer);
        event.registerLayerDefinition(EchoMobModelLayers.DRONE, EchoMobModels::createDroneLayer);
        event.registerLayerDefinition(EchoMobModelLayers.QUADRUPED, EchoMobModels::createQuadrupedLayer);
        event.registerLayerDefinition(EchoMobModelLayers.CRAWLER, EchoMobModels::createCrawlerLayer);
        event.registerLayerDefinition(EchoMobModelLayers.SLIME, EchoMobModels::createSlimeLayer);
        event.registerLayerDefinition(EchoMobModelLayers.HEAVY_BOSS, EchoMobModels::createHeavyBossLayer);
        event.registerLayerDefinition(EchoMobModelLayers.INDUSTRIAL_CONSTRUCT, EchoMobModels::createIndustrialConstructLayer);
        event.registerLayerDefinition(EchoMobModelLayers.ROCKET, EchoMobModels::createRocketLayer);
    }
}
