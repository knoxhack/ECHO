package com.knoxhack.echologisticsnetwork.integration;

import com.knoxhack.echologisticsnetwork.client.RenderCoreCourierDroneRenderer;
import com.knoxhack.echologisticsnetwork.registry.ModEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class LogisticsRenderCoreClientIntegration {
   private LogisticsRenderCoreClientIntegration() {
   }

   public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.COURIER_DRONE.get(), RenderCoreCourierDroneRenderer::new);
   }
}
