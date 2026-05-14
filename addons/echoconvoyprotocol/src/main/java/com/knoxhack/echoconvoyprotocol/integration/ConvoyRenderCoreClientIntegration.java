package com.knoxhack.echoconvoyprotocol.integration;

import com.knoxhack.echoconvoyprotocol.client.ConvoyRenderCoreVehicleRenderer;
import com.knoxhack.echoconvoyprotocol.registry.ModEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class ConvoyRenderCoreClientIntegration {
   private ConvoyRenderCoreClientIntegration() {
   }

   public static void registerVehicleRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.SCRAP_BIKE.get(), ConvoyRenderCoreVehicleRenderer::new);
      event.registerEntityRenderer(ModEntities.WASTELAND_ROVER.get(), ConvoyRenderCoreVehicleRenderer::new);
      event.registerEntityRenderer(ModEntities.CARGO_CRAWLER.get(), ConvoyRenderCoreVehicleRenderer::new);
      event.registerEntityRenderer(ModEntities.ARMORED_RELAY_TRUCK.get(), ConvoyRenderCoreVehicleRenderer::new);
   }
}
