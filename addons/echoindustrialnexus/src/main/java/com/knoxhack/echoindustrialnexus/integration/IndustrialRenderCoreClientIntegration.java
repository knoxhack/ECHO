package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echoindustrialnexus.client.IndustrialRenderCoreMachineRenderer;
import com.knoxhack.echoindustrialnexus.client.IndustrialFurnaceModel;
import com.knoxhack.echoindustrialnexus.client.IndustrialRenderCoreFurnaceRenderer;
import com.knoxhack.echoindustrialnexus.registry.ModBlockEntities;
import com.knoxhack.echoindustrialnexus.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class IndustrialRenderCoreClientIntegration {
   private IndustrialRenderCoreClientIntegration() {
   }

   public static void registerMachineRenderer(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlockEntities.INDUSTRIAL_MACHINE.get(), IndustrialRenderCoreMachineRenderer::new);
   }

   public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)ModEntities.FURNACE_WARDEN.get(),
         context -> new IndustrialRenderCoreFurnaceRenderer(context, IndustrialFurnaceModel.WARDEN_LAYER_LOCATION,
            "furnace_warden", 0xFFFF7A28, 1.08F, 0.9F));
      event.registerEntityRenderer((EntityType)ModEntities.FURNACE_DRONE.get(),
         context -> new IndustrialRenderCoreFurnaceRenderer(context, IndustrialFurnaceModel.DRONE_LAYER_LOCATION,
            "furnace_drone", 0xFFFF9C3D, 0.82F, 0.5F));
   }
}
