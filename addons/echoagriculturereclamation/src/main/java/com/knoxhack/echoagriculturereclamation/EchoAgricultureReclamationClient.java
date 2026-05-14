package com.knoxhack.echoagriculturereclamation;

import com.knoxhack.echoagriculturereclamation.client.HydroponicTrayRenderer;
import com.knoxhack.echoagriculturereclamation.client.PollinatorDroneRenderer;
import com.knoxhack.echoagriculturereclamation.registry.ModBlockEntities;
import com.knoxhack.echoagriculturereclamation.registry.ModEntities;
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
      event.registerEntityRenderer(ModEntities.POLLINATOR_DRONE.get(), PollinatorDroneRenderer::new);
   }

   private static void registerTerminalClientIntegration() {
      try {
         Class.forName("com.knoxhack.echoagriculturereclamation.integration.ReclamationTerminalClientIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         EchoAgricultureReclamation.LOGGER.warn("ECHO Agriculture Reclamation terminal client integration could not be registered.", exception);
      }
   }
}
