package com.knoxhack.echologisticsnetwork.client;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.entity.CourierDroneEntity;
import com.knoxhack.echorendercore.client.RenderCoreEntityVisuals;
import com.knoxhack.echorendercore.client.RenderCoreVisualLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RenderCoreCourierDroneRenderer extends CourierDroneRenderer {
   private static final Identifier PROFILE = Identifier.fromNamespaceAndPath(EchoLogisticsNetwork.MODID, "echo_mobs/courier_drone");
   private static final Identifier FALLBACK_TEXTURE = Identifier.fromNamespaceAndPath(EchoLogisticsNetwork.MODID,
      "textures/entity/courier_drone.png");

   public RenderCoreCourierDroneRenderer(EntityRendererProvider.Context context) {
      super(context);
      addLayer(new RenderCoreVisualLayer<>(this));
   }

   @Override
   public void extractRenderState(CourierDroneEntity entity, CourierDroneRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      RenderCoreEntityVisuals.attach(entity, state, PROFILE, FALLBACK_TEXTURE, partialTick);
   }
}
