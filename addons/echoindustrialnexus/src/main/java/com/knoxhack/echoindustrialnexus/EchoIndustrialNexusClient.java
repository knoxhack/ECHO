package com.knoxhack.echoindustrialnexus;

import com.knoxhack.echoindustrialnexus.client.IndustrialFurnaceModel;
import com.knoxhack.echoindustrialnexus.client.IndustrialFurnaceRenderer;
import com.knoxhack.echoindustrialnexus.client.IndustrialMachineScreen;
import com.knoxhack.echoindustrialnexus.client.IndustrialMultiblockControllerScreen;
import com.knoxhack.echoindustrialnexus.registry.ModEntities;
import com.knoxhack.echoindustrialnexus.registry.ModMenus;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = "echoindustrialnexus", dist = Dist.CLIENT)
@EventBusSubscriber(modid = "echoindustrialnexus", value = Dist.CLIENT)
public class EchoIndustrialNexusClient {
   public EchoIndustrialNexusClient() {
      if (ModList.get().isLoaded("echoterminal")) {
         registerTerminalClientIntegration();
      }
   }

   @SubscribeEvent
   static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
      event.registerLayerDefinition(IndustrialFurnaceModel.WARDEN_LAYER_LOCATION, IndustrialFurnaceModel::createWardenLayer);
      event.registerLayerDefinition(IndustrialFurnaceModel.DRONE_LAYER_LOCATION, IndustrialFurnaceModel::createDroneLayer);
   }

   @SubscribeEvent
   static void registerEntityRenderers(RegisterRenderers event) {
      boolean renderCoreLoaded = ModList.get().isLoaded("echorendercore");
      boolean renderCoreEntities = renderCoreLoaded && registerRenderCoreEntityRenderers(event);
      if (!renderCoreEntities) {
         registerFallbackEntityRenderers(event);
      }
      if (ModList.get().isLoaded("echorendercore")) {
         registerRenderCoreMachineRenderer(event);
      }
   }

   private static void registerFallbackEntityRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)ModEntities.FURNACE_WARDEN.get(),
         context -> new IndustrialFurnaceRenderer(context, IndustrialFurnaceModel.WARDEN_LAYER_LOCATION, "furnace_warden", 0xFFFF7A28, 1.08F, 0.9F));
      event.registerEntityRenderer((EntityType)ModEntities.FURNACE_DRONE.get(),
         context -> new IndustrialFurnaceRenderer(context, IndustrialFurnaceModel.DRONE_LAYER_LOCATION, "furnace_drone", 0xFFFF9C3D, 0.82F, 0.5F));
   }

   @SubscribeEvent
   static void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(ModMenus.INDUSTRIAL_MACHINE.get(), IndustrialMachineScreen::new);
      event.register(ModMenus.INDUSTRIAL_MULTIBLOCK_CONTROLLER.get(), IndustrialMultiblockControllerScreen::new);
   }

   private static void registerTerminalClientIntegration() {
      try {
         Class.forName("com.knoxhack.echoindustrialnexus.integration.IndustrialTerminalClientIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         EchoIndustrialNexus.LOGGER.warn("ECHO Industrial Nexus terminal client integration could not be registered.", exception);
      }
   }

   private static void registerRenderCoreMachineRenderer(RegisterRenderers event) {
      try {
         Class.forName("com.knoxhack.echoindustrialnexus.integration.IndustrialRenderCoreClientIntegration")
            .getMethod("registerMachineRenderer", EntityRenderersEvent.RegisterRenderers.class)
            .invoke(null, event);
      } catch (ReflectiveOperationException exception) {
         EchoIndustrialNexus.LOGGER.warn("ECHO Industrial Nexus RenderCore client integration could not be registered.", exception);
      }
   }

   private static boolean registerRenderCoreEntityRenderers(RegisterRenderers event) {
      try {
         Class.forName("com.knoxhack.echoindustrialnexus.integration.IndustrialRenderCoreClientIntegration")
            .getMethod("registerEntityRenderers", EntityRenderersEvent.RegisterRenderers.class)
            .invoke(null, event);
         return true;
      } catch (ReflectiveOperationException | LinkageError exception) {
         EchoIndustrialNexus.LOGGER.warn("ECHO Industrial Nexus RenderCore entity renderer integration unavailable; using furnace fallback renderers.", exception);
         return false;
      }
   }
}
