package com.knoxhack.echorendercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public final class OverlayRenderLayer {
   private static final int OVERLAY_COLOR = 0xAAFFFFFF;

   private OverlayRenderLayer() {
   }

   public static <S extends EntityRenderState> void submit(EntityModel<S> model, S state, PoseStack poseStack,
         SubmitNodeCollector collector, Identifier texture, int order) {
      submit(model, state, poseStack, collector, texture, order, OVERLAY_COLOR);
   }

   public static <S extends EntityRenderState> void submit(EntityModel<S> model, S state, PoseStack poseStack,
         SubmitNodeCollector collector, Identifier texture, int order, int color) {
      if (texture == null || state.isInvisible) {
         return;
      }
      OrderedSubmitNodeCollector ordered = collector.order(order);
      ordered.submitModel(model, state, poseStack, RenderTypes.eyes(texture), state.lightCoords, OverlayTexture.NO_OVERLAY,
         color == 0 ? OVERLAY_COLOR : color, null, state.outlineColor, null);
   }
}
