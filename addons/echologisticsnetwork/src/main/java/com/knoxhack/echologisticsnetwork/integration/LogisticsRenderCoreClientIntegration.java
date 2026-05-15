package com.knoxhack.echologisticsnetwork.integration;

import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echorendercore.client.EchoRenderCoreMobFamilyRenderer;
import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class LogisticsRenderCoreClientIntegration {
   private LogisticsRenderCoreClientIntegration() {
   }

   public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.COURIER_DRONE.get(),
         renderer("courier_drone", EchoMobFamily.DRONE, 1.0F, 0.35F));
   }

   private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
         float scale, float shadow) {
      return context -> new EchoRenderCoreMobFamilyRenderer<>(context, EchoLogisticsNetwork.MODID, entityName, family, scale, shadow);
   }
}
