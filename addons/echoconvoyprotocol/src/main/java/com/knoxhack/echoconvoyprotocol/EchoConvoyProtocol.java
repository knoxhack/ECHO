package com.knoxhack.echoconvoyprotocol;

import com.knoxhack.echoconvoyprotocol.content.ConvoyReloaders;
import com.knoxhack.echoconvoyprotocol.integration.ConvoyCoreIntegration;
import com.knoxhack.echoconvoyprotocol.integration.ConvoyIndexProvider;
import com.knoxhack.echoconvoyprotocol.network.ModNetwork;
import com.knoxhack.echoconvoyprotocol.registry.ModBlocks;
import com.knoxhack.echoconvoyprotocol.registry.ModBlockEntities;
import com.knoxhack.echoconvoyprotocol.registry.ModCreativeTabs;
import com.knoxhack.echoconvoyprotocol.registry.ModEntities;
import com.knoxhack.echoconvoyprotocol.registry.ModItems;
import com.knoxhack.echoconvoyprotocol.registry.ModMenus;
import com.knoxhack.echoconvoyprotocol.registry.ModRecipes;
import com.knoxhack.echoconvoyprotocol.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoConvoyProtocol.MODID)
public class EchoConvoyProtocol {
   public static final String MODID = "echoconvoyprotocol";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoConvoyProtocol(IEventBus modEventBus, ModContainer modContainer) {
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModEntities.register(modEventBus);
      ModItems.register(modEventBus);
      ModMenus.register(modEventBus);
      ModRecipes.register(modEventBus);
      ModGameTests.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(ModNetwork::registerPayloads);
      modEventBus.addListener(ModGameTests::registerTests);
      NeoForge.EVENT_BUS.addListener(ConvoyReloaders::addServerReloadListeners);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("ECHO Convoy Protocol initialized. Road crews are improvising.");
      event.enqueueWork(() -> {
         ConvoyCoreIntegration.registerAddonChapter();
         ConvoyIndexProvider.register();
         if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalIntegration();
         }
      });
   }

   private static void registerTerminalIntegration() {
      try {
         Class.forName("com.knoxhack.echoconvoyprotocol.integration.ConvoyTerminalCommonIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         LOGGER.warn("ECHO Convoy Protocol terminal integration could not be registered.", exception);
      }
   }
}
