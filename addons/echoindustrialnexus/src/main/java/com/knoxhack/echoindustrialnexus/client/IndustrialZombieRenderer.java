package com.knoxhack.echoindustrialnexus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class IndustrialZombieRenderer extends ZombieRenderer {
   private static final Identifier ZOMBIE_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/zombie/zombie.png");
   private final int tint;
   private final float scale;

   public IndustrialZombieRenderer(Context context, int tint, float scale) {
      super(context);
      this.tint = tint;
      this.scale = scale;
   }

   public Identifier getTextureLocation(ZombieRenderState state) {
      return ZOMBIE_TEXTURE;
   }

   protected int getModelTint(ZombieRenderState state) {
      return this.tint;
   }

   protected void scale(ZombieRenderState state, PoseStack poseStack) {
      poseStack.scale(this.scale, this.scale, this.scale);
   }

   public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      this.shadowRadius = 0.9F;
   }
}
