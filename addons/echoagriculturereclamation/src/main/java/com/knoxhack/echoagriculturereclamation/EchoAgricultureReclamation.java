package com.knoxhack.echoagriculturereclamation;

import com.knoxhack.echoagriculturereclamation.content.ReclamationReloaders;
import com.knoxhack.echoagriculturereclamation.integration.ReclamationCoreIntegration;
import com.knoxhack.echoagriculturereclamation.registry.ModBlockEntities;
import com.knoxhack.echoagriculturereclamation.registry.ModBlocks;
import com.knoxhack.echoagriculturereclamation.registry.ModCreativeTabs;
import com.knoxhack.echoagriculturereclamation.registry.ModDataComponents;
import com.knoxhack.echoagriculturereclamation.registry.ModEntities;
import com.knoxhack.echoagriculturereclamation.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.slf4j.Logger;

@Mod(EchoAgricultureReclamation.MODID)
public class EchoAgricultureReclamation {
   public static final String MODID = "echoagriculturereclamation";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoAgricultureReclamation(IEventBus modEventBus) {
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModEntities.register(modEventBus);
      ModDataComponents.register(modEventBus);
      ModItems.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      registerGameTests(modEventBus);
      modEventBus.addListener(ModEntities::registerAttributes);
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

   private static void registerGameTests(IEventBus modEventBus) {
      if (!gameTestsEnabled()) {
         return;
      }
      try {
         Class<?> tests = Class.forName("com.knoxhack.echoagriculturereclamation.test.ModGameTests");
         tests.getMethod("register", IEventBus.class).invoke(null, modEventBus);
         modEventBus.addListener((RegisterGameTestsEvent event) -> invokeGameTestRegistration(tests, event));
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException("Unable to register Agriculture Reclamation GameTests.", exception);
      }
   }

   private static boolean gameTestsEnabled() {
      String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
      if (namespaces.isBlank()) {
         return false;
      }
      for (String namespace : namespaces.split(",")) {
         String trimmed = namespace.trim();
         if (trimmed.equals("*") || trimmed.equalsIgnoreCase("all") || trimmed.equals(EchoAgricultureReclamation.MODID)) {
            return true;
         }
      }
      return false;
   }

   private static void invokeGameTestRegistration(Class<?> tests, RegisterGameTestsEvent event) {
      try {
         tests.getMethod("registerTests", RegisterGameTestsEvent.class).invoke(null, event);
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException("Unable to register Agriculture Reclamation GameTests.", exception);
      }
   }
}
