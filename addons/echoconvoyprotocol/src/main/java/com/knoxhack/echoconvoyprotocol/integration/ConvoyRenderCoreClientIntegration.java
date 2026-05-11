package com.knoxhack.echoconvoyprotocol.integration;

import com.knoxhack.echoconvoyprotocol.client.ConvoyRenderCoreVehicleRenderer;
import com.knoxhack.echoconvoyprotocol.registry.ModEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class ConvoyRenderCoreClientIntegration {
   private ConvoyRenderCoreClientIntegration() {
   }

   public static void registerWastelandRoverRenderer(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.WASTELAND_ROVER.get(), ConvoyRenderCoreVehicleRenderer::new);
   }
}
