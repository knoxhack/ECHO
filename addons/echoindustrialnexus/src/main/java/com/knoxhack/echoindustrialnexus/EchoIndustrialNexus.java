package com.knoxhack.echoindustrialnexus;

import com.knoxhack.echoindustrialnexus.integration.IndustrialCoreIntegration;
import com.knoxhack.echoindustrialnexus.registry.ModBlockEntities;
import com.knoxhack.echoindustrialnexus.registry.ModBlocks;
import com.knoxhack.echoindustrialnexus.registry.ModCapabilities;
import com.knoxhack.echoindustrialnexus.registry.ModCreativeTabs;
import com.knoxhack.echoindustrialnexus.registry.ModEntities;
import com.knoxhack.echoindustrialnexus.registry.ModFluids;
import com.knoxhack.echoindustrialnexus.registry.ModItems;
import com.knoxhack.echoindustrialnexus.registry.ModMenus;
import com.knoxhack.echoindustrialnexus.registry.ModRecipes;
import com.knoxhack.echoindustrialnexus.registry.ModSounds;
import com.knoxhack.echoindustrialnexus.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod("echoindustrialnexus")
public class EchoIndustrialNexus {
   public static final String MODID = "echoindustrialnexus";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoIndustrialNexus(IEventBus modEventBus, ModContainer modContainer) {
      ModBlocks.register(modEventBus);
      ModFluids.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModEntities.register(modEventBus);
      ModItems.register(modEventBus);
      ModMenus.register(modEventBus);
      ModRecipes.register(modEventBus);
      ModSounds.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      ModGameTests.register(modEventBus);
      modEventBus.addListener(ModEntities::registerAttributes);
      modEventBus.addListener(ModCapabilities::register);
      modEventBus.addListener(ModGameTests::registerTests);
      modEventBus.addListener(this::commonSetup);
      modContainer.registerConfig(Type.COMMON, Config.SPEC);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("ECHO Industrial Nexus online. Where survival becomes infrastructure.");
      event.enqueueWork(() -> {
         IndustrialCoreIntegration.registerAddonChapter();
         if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalIntegration();
         }
      });
   }

   private static void registerTerminalIntegration() {
      try {
         Class.forName("com.knoxhack.echoindustrialnexus.integration.IndustrialTerminalCommonIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         LOGGER.warn("ECHO Industrial Nexus terminal integration could not be registered.", exception);
      }
   }
}
