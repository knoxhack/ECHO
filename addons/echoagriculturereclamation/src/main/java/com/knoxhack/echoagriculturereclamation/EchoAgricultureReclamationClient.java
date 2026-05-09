package com.knoxhack.echoagriculturereclamation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(value = EchoAgricultureReclamation.MODID, dist = Dist.CLIENT)
public class EchoAgricultureReclamationClient {
   public EchoAgricultureReclamationClient() {
      if (ModList.get().isLoaded("echoterminal")) {
         registerTerminalClientIntegration();
      }
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
