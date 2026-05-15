package com.knoxhack.echoblockworks;

import com.knoxhack.echoblockworks.integration.BlockworksCoreIntegration;
import com.knoxhack.echoblockworks.integration.BlockworksIndexProvider;
import com.knoxhack.echoblockworks.integration.BlockworksMissionCoreIntegration;
import com.knoxhack.echoblockworks.registry.ModBlockEntities;
import com.knoxhack.echoblockworks.registry.ModBlocks;
import com.knoxhack.echoblockworks.registry.ModCreativeTabs;
import com.knoxhack.echoblockworks.registry.ModGameTests;
import com.knoxhack.echoblockworks.registry.ModItems;
import com.knoxhack.echoblockworks.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(EchoBlockworks.MODID)
public class EchoBlockworks {
   public static final String MODID = "echoblockworks";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoBlockworks(IEventBus modEventBus, ModContainer modContainer) {
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModItems.register(modEventBus);
      ModMenus.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      ModGameTests.register(modEventBus);

      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(ModGameTests::registerTests);
      modContainer.registerConfig(Type.COMMON, Config.SPEC);
      Config.registerEchoConfig();
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(MODID, path);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         BlockworksCoreIntegration.registerAddonChapter();
         if (ModList.get().isLoaded("echomissioncore")) {
            BlockworksMissionCoreIntegration.register();
         }
         if (ModList.get().isLoaded("echoindex")) {
            BlockworksIndexProvider.register();
         }
      });
      LOGGER.info("ECHO Blockworks catalog online.");
   }
}
