package com.knoxhack.echoarmory;

import com.knoxhack.echoarmory.content.ArmoryReloaders;
import com.knoxhack.echoarmory.event.ArmoryEvents;
import com.knoxhack.echoarmory.integration.ArmoryCoreIntegration;
import com.knoxhack.echoarmory.integration.ArmoryIndexProvider;
import com.knoxhack.echoarmory.registry.ModBlockEntities;
import com.knoxhack.echoarmory.registry.ModBlocks;
import com.knoxhack.echoarmory.registry.ModCreativeTabs;
import com.knoxhack.echoarmory.registry.ModDataComponents;
import com.knoxhack.echoarmory.registry.ModGameTests;
import com.knoxhack.echoarmory.registry.ModItems;
import com.knoxhack.echoarmory.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoArmory.MODID)
public class EchoArmory {
   public static final String MODID = "echoarmory";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoArmory(IEventBus modEventBus, ModContainer modContainer) {
      ModDataComponents.register(modEventBus);
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModItems.register(modEventBus);
      ModMenus.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      ModGameTests.register(modEventBus);
      modEventBus.addListener(ModGameTests::registerTests);
      modEventBus.addListener(this::commonSetup);
      NeoForge.EVENT_BUS.addListener(ArmoryReloaders::addServerReloadListeners);
      NeoForge.EVENT_BUS.addListener(ArmoryEvents::onPlayerTick);
      NeoForge.EVENT_BUS.addListener(ArmoryEvents::onLivingDamage);
      NeoForge.EVENT_BUS.addListener(ArmoryEvents::onItemCrafted);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("ECHO Armory online. Modular survival is now mission-ready.");
      event.enqueueWork(() -> {
         ArmoryCoreIntegration.registerAddonChapter();
         ArmoryIndexProvider.register();
         if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalIntegration();
         }
      });
   }

   private static void registerTerminalIntegration() {
      try {
         Class.forName("com.knoxhack.echoarmory.integration.ArmoryTerminalCommonIntegration")
            .getMethod("register")
            .invoke(null);
      } catch (ReflectiveOperationException exception) {
         LOGGER.warn("ECHO Armory terminal integration could not be registered.", exception);
      }
   }
}
