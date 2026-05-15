package com.knoxhack.echologisticsnetwork;

import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echocore.client.model.EchoMobFamilyRenderer;
import com.knoxhack.echologisticsnetwork.client.LogisticsScreen;
import com.knoxhack.echologisticsnetwork.registry.ModEntities;
import com.knoxhack.echologisticsnetwork.registry.ModMenus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = EchoLogisticsNetwork.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoLogisticsNetwork.MODID, value = Dist.CLIENT)
public class EchoLogisticsNetworkClient {
   public EchoLogisticsNetworkClient() {
      if (ModList.get().isLoaded("echoterminal")) {
         registerTerminalClientIntegration();
      }
   }

   @SubscribeEvent
   static void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(ModMenus.LOGISTICS.get(), LogisticsScreen::new);
   }

   @SubscribeEvent
   static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      if (ModList.get().isLoaded("echorendercore") && registerRenderCoreEntityRenderers(event)) {
         return;
      }
      event.registerEntityRenderer(ModEntities.COURIER_DRONE.get(),
         renderer("courier_drone", EchoMobFamily.DRONE, 1.0F, 0.35F));
   }

   private static void registerTerminalClientIntegration() {
      try {
         Class.forName("com.knoxhack.echologisticsnetwork.integration.LogisticsTerminalClientIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         EchoLogisticsNetwork.LOGGER.warn("ECHO Logistics Network terminal client integration could not be registered.", exception);
      }
   }

   private static boolean registerRenderCoreEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      try {
         Class.forName("com.knoxhack.echologisticsnetwork.integration.LogisticsRenderCoreClientIntegration")
            .getMethod("registerEntityRenderers", EntityRenderersEvent.RegisterRenderers.class)
            .invoke(null, event);
         return true;
      } catch (ReflectiveOperationException | LinkageError exception) {
         EchoLogisticsNetwork.LOGGER.warn("ECHO Logistics Network RenderCore entity renderer integration unavailable; using courier drone fallback renderer.", exception);
         return false;
      }
   }

   private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
         float scale, float shadow) {
      return context -> new EchoMobFamilyRenderer<>(context, EchoLogisticsNetwork.MODID, entityName, family, scale, shadow);
   }
}
