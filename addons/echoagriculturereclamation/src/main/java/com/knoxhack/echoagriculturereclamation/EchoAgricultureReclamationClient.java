package com.knoxhack.echoagriculturereclamation;

import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echocore.client.model.EchoMobFamilyRenderer;
import com.knoxhack.echoagriculturereclamation.client.HydroponicTrayRenderer;
import com.knoxhack.echoagriculturereclamation.registry.ModBlockEntities;
import com.knoxhack.echoagriculturereclamation.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = EchoAgricultureReclamation.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoAgricultureReclamation.MODID, value = Dist.CLIENT)
public class EchoAgricultureReclamationClient {
   public EchoAgricultureReclamationClient() {
      if (ModList.get().isLoaded("echoterminal")) {
         registerTerminalClientIntegration();
      }
   }

   @SubscribeEvent
   static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlockEntities.HYDROPONIC_TRAY.get(), HydroponicTrayRenderer::new);
      if (ModList.get().isLoaded("echorendercore") && registerRenderCoreEntityRenderers(event)) {
         return;
      }
      event.registerEntityRenderer(ModEntities.POLLINATOR_DRONE.get(),
         renderer("pollinator_drone", EchoMobFamily.DRONE, 0.78F, 0.28F));
   }

   private static void registerTerminalClientIntegration() {
      try {
         Class.forName("com.knoxhack.echoagriculturereclamation.integration.ReclamationTerminalClientIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException | LinkageError exception) {
         EchoAgricultureReclamation.LOGGER.warn("ECHO Agriculture Reclamation terminal client integration could not be registered.", exception);
      }
   }

   private static boolean registerRenderCoreEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      try {
         Class.forName("com.knoxhack.echoagriculturereclamation.integration.ReclamationRenderCoreClientIntegration")
            .getMethod("registerEntityRenderers", EntityRenderersEvent.RegisterRenderers.class)
            .invoke(null, event);
         return true;
      } catch (ReflectiveOperationException | LinkageError exception) {
         EchoAgricultureReclamation.LOGGER.warn("ECHO Agriculture Reclamation RenderCore entity renderer integration unavailable; using generated fallback renderers.", exception);
         return false;
      }
   }

   private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
         float scale, float shadow) {
      return context -> new EchoMobFamilyRenderer<>(context, EchoAgricultureReclamation.MODID, entityName, family, scale, shadow);
   }
}
