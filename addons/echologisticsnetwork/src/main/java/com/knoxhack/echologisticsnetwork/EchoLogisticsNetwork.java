package com.knoxhack.echologisticsnetwork;

import com.knoxhack.echologisticsnetwork.content.LogisticsReloaders;
import com.knoxhack.echologisticsnetwork.integration.LogisticsCoreIntegration;
import com.knoxhack.echologisticsnetwork.integration.LogisticsMissionCoreIntegration;
import com.knoxhack.echologisticsnetwork.registry.ModBlockEntities;
import com.knoxhack.echologisticsnetwork.registry.ModBlocks;
import com.knoxhack.echologisticsnetwork.registry.ModCreativeTabs;
import com.knoxhack.echologisticsnetwork.registry.ModDataComponents;
import com.knoxhack.echologisticsnetwork.registry.ModEntities;
import com.knoxhack.echologisticsnetwork.registry.ModGameTests;
import com.knoxhack.echologisticsnetwork.registry.ModItems;
import com.knoxhack.echologisticsnetwork.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoLogisticsNetwork.MODID)
public class EchoLogisticsNetwork {
   public static final String MODID = "echologisticsnetwork";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoLogisticsNetwork(IEventBus modEventBus, ModContainer modContainer) {
      modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
      Config.registerEchoConfig();
      ModDataComponents.register(modEventBus);
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModItems.register(modEventBus);
      ModMenus.register(modEventBus);
      ModEntities.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      ModGameTests.register(modEventBus);
      modEventBus.addListener(ModEntities::registerAttributes);
      modEventBus.addListener(ModGameTests::registerTests);
      modEventBus.addListener(this::commonSetup);
      NeoForge.EVENT_BUS.addListener(LogisticsReloaders::addServerReloadListeners);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("ECHO Logistics Network online. Supply chaos is now a routing problem.");
      event.enqueueWork(() -> {
         LogisticsCoreIntegration.registerAddonChapter();
         LogisticsMissionCoreIntegration.register();
         if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalIntegration();
         }
      });
   }

   private static void registerTerminalIntegration() {
      try {
         Class.forName("com.knoxhack.echologisticsnetwork.integration.LogisticsTerminalCommonIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         LOGGER.warn("ECHO Logistics Network terminal integration could not be registered.", exception);
      }
   }
}
