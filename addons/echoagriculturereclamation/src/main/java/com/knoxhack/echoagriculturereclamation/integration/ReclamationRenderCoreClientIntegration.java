package com.knoxhack.echoagriculturereclamation.integration;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.registry.ModEntities;
import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echorendercore.client.EchoRenderCoreMobFamilyRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class ReclamationRenderCoreClientIntegration {
   private ReclamationRenderCoreClientIntegration() {
   }

   public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.POLLINATOR_DRONE.get(),
         renderer("pollinator_drone", EchoMobFamily.DRONE, 0.78F, 0.28F));
   }

   private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
         float scale, float shadow) {
      return context -> new EchoRenderCoreMobFamilyRenderer<>(context, EchoAgricultureReclamation.MODID, entityName, family, scale, shadow);
   }
}
