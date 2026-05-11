package com.knoxhack.echoindustrialnexus.client;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class IndustrialFurnaceRenderer extends MobRenderer<Zombie, ZombieRenderState, IndustrialFurnaceModel> {
   private final Identifier texture;
   private final int tint;
   private final float scale;
   private final float shadow;

   public IndustrialFurnaceRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, String textureName, int tint, float scale, float shadow) {
      super(context, new IndustrialFurnaceModel(context.bakeLayer(layer)), shadow);
      this.texture = Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, "textures/entity/" + textureName + ".png");
      this.tint = tint;
      this.scale = scale;
      this.shadow = shadow;
   }

   @Override
   public ZombieRenderState createRenderState() {
      return new ZombieRenderState();
   }

   @Override
   public Identifier getTextureLocation(ZombieRenderState state) {
      return this.texture;
   }

   @Override
   protected int getModelTint(ZombieRenderState state) {
      return this.tint;
   }

   @Override
   protected void scale(ZombieRenderState state, PoseStack poseStack) {
      poseStack.scale(this.scale, this.scale, this.scale);
   }

   @Override
   public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      this.shadowRadius = this.shadow;
   }
}
