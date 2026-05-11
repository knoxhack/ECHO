package com.knoxhack.echologisticsnetwork.client;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.entity.CourierDroneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class CourierDroneRenderer extends MobRenderer<CourierDroneEntity, CourierDroneRenderState, CourierDroneModel> {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(EchoLogisticsNetwork.MODID, "textures/entity/courier_drone.png");

   public CourierDroneRenderer(EntityRendererProvider.Context context) {
      super(context, new CourierDroneModel(context.bakeLayer(CourierDroneModel.LAYER_LOCATION)), 0.35F);
   }

   @Override
   public CourierDroneRenderState createRenderState() {
      return new CourierDroneRenderState();
   }

   @Override
   public void extractRenderState(CourierDroneEntity entity, CourierDroneRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.hoverOffset = entity.isAlive() ? 0.0F : -0.2F;
   }

   @Override
   public Identifier getTextureLocation(CourierDroneRenderState state) {
      return TEXTURE;
   }
}
