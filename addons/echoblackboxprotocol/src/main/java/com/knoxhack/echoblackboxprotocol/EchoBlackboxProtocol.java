package com.knoxhack.echoblackboxprotocol;

import com.knoxhack.echoblackboxprotocol.integration.BlackboxCoreIntegration;
import com.knoxhack.echoblackboxprotocol.integration.BlackboxTerminalCommonIntegration;
import com.knoxhack.echoblackboxprotocol.registry.ModBlockEntities;
import com.knoxhack.echoblackboxprotocol.registry.ModBlocks;
import com.knoxhack.echoblackboxprotocol.registry.ModCreativeTabs;
import com.knoxhack.echoblackboxprotocol.registry.ModEntities;
import com.knoxhack.echoblackboxprotocol.registry.ModItems;
import com.knoxhack.echoblackboxprotocol.registry.ModMenus;
import com.knoxhack.echoblackboxprotocol.registry.ModRecipes;
import com.knoxhack.echoblackboxprotocol.registry.ModWorldgen;
import com.knoxhack.echoblackboxprotocol.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod("echoblackboxprotocol")
public class EchoBlackboxProtocol {
   public static final String MODID = "echoblackboxprotocol";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoBlackboxProtocol(IEventBus modEventBus, ModContainer modContainer) {
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModItems.register(modEventBus);
      ModEntities.register(modEventBus);
      ModMenus.register(modEventBus);
      ModRecipes.register(modEventBus);
      ModWorldgen.register(modEventBus);
      ModGameTests.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(ModEntities::registerAttributes);
      modEventBus.addListener(ModGameTests::registerTests);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("ECHO-7 blackbox protocol initialized. Truth Engine cold-start accepted.");
      event.enqueueWork(() -> {
         BlackboxCoreIntegration.registerAddonChapter();
         if (ModList.get().isLoaded("echoterminal")) {
            BlackboxTerminalCommonIntegration.register();
         }
      });
   }
}
