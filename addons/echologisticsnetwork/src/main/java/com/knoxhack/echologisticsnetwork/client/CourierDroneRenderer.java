package com.knoxhack.echologisticsnetwork.client;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Vex;

public class CourierDroneRenderer extends VexRenderer {
   private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/illager/vex.png");

   public CourierDroneRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   @Override
   public Identifier getTextureLocation(VexRenderState state) {
      return TEXTURE;
   }

   @Override
   protected int getModelTint(VexRenderState state) {
      return 0xFF66E8FF;
   }

   @Override
   protected void scale(VexRenderState state, PoseStack poseStack) {
      poseStack.scale(0.72F, 0.72F, 0.72F);
   }

   @Override
   public void extractRenderState(Vex entity, VexRenderState state, float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      this.shadowRadius = 0.25F;
   }
}
