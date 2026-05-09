package com.knoxhack.echoagriculturereclamation;

import com.knoxhack.echoagriculturereclamation.content.ReclamationReloaders;
import com.knoxhack.echoagriculturereclamation.integration.ReclamationCoreIntegration;
import com.knoxhack.echoagriculturereclamation.registry.ModBlockEntities;
import com.knoxhack.echoagriculturereclamation.registry.ModBlocks;
import com.knoxhack.echoagriculturereclamation.registry.ModCreativeTabs;
import com.knoxhack.echoagriculturereclamation.registry.ModDataComponents;
import com.knoxhack.echoagriculturereclamation.registry.ModItems;
import com.knoxhack.echoagriculturereclamation.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoAgricultureReclamation.MODID)
public class EchoAgricultureReclamation {
   public static final String MODID = "echoagriculturereclamation";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoAgricultureReclamation(IEventBus modEventBus) {
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModDataComponents.register(modEventBus);
      ModItems.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      ModGameTests.register(modEventBus);
      modEventBus.addListener(ModGameTests::registerTests);
      modEventBus.addListener(this::commonSetup);
      NeoForge.EVENT_BUS.addListener(ReclamationReloaders::addServerReloadListeners);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("ECHO Agriculture Reclamation online. Dead worlds do not get the final word.");
      event.enqueueWork(() -> {
         ReclamationCoreIntegration.registerAddonChapter();
         if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalIntegration();
         }
      });
   }

   private static void registerTerminalIntegration() {
      try {
         Class.forName("com.knoxhack.echoagriculturereclamation.integration.ReclamationTerminalCommonIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         LOGGER.warn("ECHO Agriculture Reclamation terminal integration could not be registered.", exception);
      }
   }
}
