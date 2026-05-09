package com.knoxhack.echoconvoyprotocol.client;

import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class ConvoyVehicleRenderer extends EntityRenderer<ConvoyVehicleEntity, ConvoyVehicleRenderState> {
   private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/block/iron_block.png");
   private final ConvoyVehicleModel model;

   public ConvoyVehicleRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.model = new ConvoyVehicleModel(context.bakeLayer(ConvoyVehicleModel.LAYER_LOCATION));
      this.shadowRadius = 0.85F;
   }

   @Override
   public ConvoyVehicleRenderState createRenderState() {
      return new ConvoyVehicleRenderState();
   }

   @Override
   public void extractRenderState(ConvoyVehicleEntity entity, ConvoyVehicleRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.yRot = entity.getYRot(partialTick);
      state.kind = entity.kind().ordinal();
      state.damageRatio = entity.kind().maxDamage() == 0 ? 0.0F : entity.damage() / (float)entity.kind().maxDamage();
      state.fuelRatio = entity.kind().maxFuel() == 0 ? 0.0F : entity.fuel() / (float)entity.kind().maxFuel();
   }

   @Override
   public void submit(ConvoyVehicleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
      poseStack.scale(-1.0F, -1.0F, 1.0F);
      poseStack.translate(0.0F, -1.501F, 0.0F);
      collector.submitModel(model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }
}
