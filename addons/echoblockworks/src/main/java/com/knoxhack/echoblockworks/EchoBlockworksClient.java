package com.knoxhack.echoblockworks;

import com.knoxhack.echoblockworks.client.BlockworksTableScreen;
import com.knoxhack.echoblockworks.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = EchoBlockworks.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoBlockworks.MODID, value = Dist.CLIENT)
public class EchoBlockworksClient {
   public EchoBlockworksClient() {
      if (ModList.get().isLoaded("echorendercore")) {
         registerRenderCoreStaticSurfaces();
      }
   }

   @SubscribeEvent
   static void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(ModMenus.BLOCKWORKS_TABLE.get(), BlockworksTableScreen::new);
   }

   private static void registerRenderCoreStaticSurfaces() {
      try {
         Class.forName("com.knoxhack.echoblockworks.integration.BlockworksRenderCoreClientIntegration")
            .getMethod("registerStaticSurfaces")
            .invoke(null);
      } catch (ReflectiveOperationException | LinkageError exception) {
         EchoBlockworks.LOGGER.warn("ECHO Blockworks RenderCore static surface integration could not be registered.", exception);
      }
   }
}
