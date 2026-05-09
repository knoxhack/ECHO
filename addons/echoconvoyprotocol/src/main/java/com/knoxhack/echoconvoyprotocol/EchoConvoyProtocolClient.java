package com.knoxhack.echoconvoyprotocol;

import com.knoxhack.echoconvoyprotocol.client.ConvoyVehicleModel;
import com.knoxhack.echoconvoyprotocol.client.ConvoyVehicleRenderer;
import com.knoxhack.echoconvoyprotocol.client.ConvoyStationScreen;
import com.knoxhack.echoconvoyprotocol.registry.ModEntities;
import com.knoxhack.echoconvoyprotocol.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = EchoConvoyProtocol.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoConvoyProtocol.MODID, value = Dist.CLIENT)
public class EchoConvoyProtocolClient {
   public EchoConvoyProtocolClient() {
      if (ModList.get().isLoaded("echoterminal")) {
         registerTerminalClientIntegration();
      }
   }

   @SubscribeEvent
   static void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(ModMenus.CONVOY_STATION.get(), ConvoyStationScreen::new);
   }

   @SubscribeEvent
   static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
      event.registerLayerDefinition(ConvoyVehicleModel.LAYER_LOCATION, ConvoyVehicleModel::createBodyLayer);
   }

   @SubscribeEvent
   static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.SCRAP_BIKE.get(), ConvoyVehicleRenderer::new);
      event.registerEntityRenderer(ModEntities.WASTELAND_ROVER.get(), ConvoyVehicleRenderer::new);
      event.registerEntityRenderer(ModEntities.CARGO_CRAWLER.get(), ConvoyVehicleRenderer::new);
      event.registerEntityRenderer(ModEntities.ARMORED_RELAY_TRUCK.get(), ConvoyVehicleRenderer::new);
   }

   private static void registerTerminalClientIntegration() {
      try {
         Class.forName("com.knoxhack.echoconvoyprotocol.integration.ConvoyTerminalClientIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         EchoConvoyProtocol.LOGGER.warn("ECHO Convoy Protocol terminal client integration could not be registered.", exception);
      }
   }
}
