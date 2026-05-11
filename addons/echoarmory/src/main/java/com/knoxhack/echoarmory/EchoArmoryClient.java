package com.knoxhack.echoarmory;

import com.knoxhack.echoarmory.client.ArmoryStationScreen;
import com.knoxhack.echoarmory.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = EchoArmory.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoArmory.MODID, value = Dist.CLIENT)
public class EchoArmoryClient {
   public EchoArmoryClient() {
      if (ModList.get().isLoaded("echoterminal")) {
         registerTerminalClientIntegration();
      }
   }

   @SubscribeEvent
   static void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(ModMenus.ARMORY_STATION.get(), ArmoryStationScreen::new);
   }

   private static void registerTerminalClientIntegration() {
      try {
         Class.forName("com.knoxhack.echoarmory.integration.ArmoryTerminalClientIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         EchoArmory.LOGGER.warn("ECHO Armory terminal client integration could not be registered.", exception);
      }
   }
}
