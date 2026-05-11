package com.knoxhack.echoconvoyprotocol;

import com.knoxhack.echoconvoyprotocol.client.ConvoyVehicleModel;
import com.knoxhack.echoconvoyprotocol.client.ConvoyVehicleRenderer;
import com.knoxhack.echoconvoyprotocol.client.ConvoyStationScreen;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleKind;
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
      for (ConvoyVehicleKind kind : ConvoyVehicleKind.values()) {
         event.registerLayerDefinition(ConvoyVehicleModel.layerLocation(kind), () -> ConvoyVehicleModel.createBodyLayer(kind));
      }
   }

   @SubscribeEvent
   static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.SCRAP_BIKE.get(), ConvoyVehicleRenderer::new);
      if (!registerRenderCoreWastelandRover(event)) {
         event.registerEntityRenderer(ModEntities.WASTELAND_ROVER.get(), ConvoyVehicleRenderer::new);
      }
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

   private static boolean registerRenderCoreWastelandRover(EntityRenderersEvent.RegisterRenderers event) {
      if (!ModList.get().isLoaded("echorendercore")) {
         return false;
      }
      try {
         Class.forName("com.knoxhack.echoconvoyprotocol.integration.ConvoyRenderCoreClientIntegration")
            .getMethod("registerWastelandRoverRenderer", EntityRenderersEvent.RegisterRenderers.class)
            .invoke(null, event);
         return true;
      } catch (ReflectiveOperationException exception) {
         EchoConvoyProtocol.LOGGER.warn("ECHO Convoy Protocol RenderCore rover integration could not be registered; using fallback renderer.", exception);
         return false;
      }
   }
}
