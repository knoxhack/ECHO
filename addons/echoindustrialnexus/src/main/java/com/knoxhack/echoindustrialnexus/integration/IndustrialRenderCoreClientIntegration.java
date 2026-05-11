package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echoindustrialnexus.client.IndustrialRenderCoreMachineRenderer;
import com.knoxhack.echoindustrialnexus.registry.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class IndustrialRenderCoreClientIntegration {
   private IndustrialRenderCoreClientIntegration() {
   }

   public static void registerMachineRenderer(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlockEntities.INDUSTRIAL_MACHINE.get(), IndustrialRenderCoreMachineRenderer::new);
   }
}
