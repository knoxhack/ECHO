package com.knoxhack.echoindustrialnexus;

import com.knoxhack.echoindustrialnexus.client.IndustrialZombieRenderer;
import com.knoxhack.echoindustrialnexus.client.IndustrialMachineScreen;
import com.knoxhack.echoindustrialnexus.registry.ModEntities;
import com.knoxhack.echoindustrialnexus.registry.ModMenus;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
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
   static void registerEntityRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)ModEntities.FURNACE_WARDEN.get(), context -> new IndustrialZombieRenderer(context, -34248, 1.55F));
      event.registerEntityRenderer((EntityType)ModEntities.FURNACE_DRONE.get(), context -> new IndustrialZombieRenderer(context, -26368, 0.9F));
   }

   @SubscribeEvent
   static void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(ModMenus.INDUSTRIAL_MACHINE.get(), IndustrialMachineScreen::new);
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
}
