package com.knoxhack.echoindustrialnexus.client;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echorendercore.client.RenderCoreEntityVisuals;
import com.knoxhack.echorendercore.client.RenderCoreVisualLayer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class IndustrialRenderCoreFurnaceRenderer extends IndustrialFurnaceRenderer {
   private final Identifier profileId;
   private final Identifier fallbackTexture;

   public IndustrialRenderCoreFurnaceRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer,
         String entityName, int tint, float scale, float shadow) {
      super(context, layer, entityName, tint, scale, shadow);
      this.profileId = Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, "echo_mobs/" + entityName);
      this.fallbackTexture = Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, "textures/entity/" + entityName + ".png");
      addLayer(new RenderCoreVisualLayer<>(this));
   }

   @Override
   public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      RenderCoreEntityVisuals.attach(entity, state, profileId, fallbackTexture, partialTicks);
   }
}
