package com.knoxhack.echoagriculturereclamation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.resources.Identifier;

public class PollinatorDroneRenderer extends VexRenderer {
   private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/illager/vex.png");

   public PollinatorDroneRenderer(EntityRendererProvider.Context context) {
      super(context);
      shadowRadius = 0.28F;
   }

   @Override
   public Identifier getTextureLocation(VexRenderState state) {
      return TEXTURE;
   }

   @Override
   protected int getModelTint(VexRenderState state) {
      return 0xFFFFD35A;
   }

   @Override
   protected void scale(VexRenderState state, PoseStack poseStack) {
      poseStack.scale(0.78F, 0.78F, 0.78F);
   }
}
