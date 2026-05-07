package com.knoxhack.echoblackboxprotocol.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class TintedZombieRenderer extends ZombieRenderer {
   private final Identifier texture;
   private final int tint;
   private final float scale;
   private final float shadow;

   public TintedZombieRenderer(EntityRendererProvider.Context context, Identifier texture, int tint, float scale, float shadow) {
      super(context);
      this.texture = texture;
      this.tint = tint;
      this.scale = scale;
      this.shadow = shadow;
   }

   @Override
   public Identifier getTextureLocation(ZombieRenderState state) {
      return texture;
   }

   @Override
   protected int getModelTint(ZombieRenderState state) {
      return tint;
   }

   @Override
   protected void scale(ZombieRenderState state, PoseStack poseStack) {
      poseStack.scale(scale, scale, scale);
   }

   @Override
   public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      this.shadowRadius = shadow;
   }
}
