package com.knoxhack.echonexusprotocol;

import com.knoxhack.echonexusprotocol.command.NexusCommandHandler;
import com.knoxhack.echonexusprotocol.event.NexusArmorEvents;
import com.knoxhack.echonexusprotocol.event.NexusWorldEvents;
import com.knoxhack.echonexusprotocol.integration.NexusCoreIntegration;
import com.knoxhack.echonexusprotocol.integration.NexusTerminalCommonIntegration;
import com.knoxhack.echonexusprotocol.registry.ModAttachments;
import com.knoxhack.echonexusprotocol.registry.ModBlockEntities;
import com.knoxhack.echonexusprotocol.registry.ModBlocks;
import com.knoxhack.echonexusprotocol.registry.ModCreativeTabs;
import com.knoxhack.echonexusprotocol.registry.ModEnergyCapabilities;
import com.knoxhack.echonexusprotocol.registry.ModEntities;
import com.knoxhack.echonexusprotocol.registry.ModItems;
import com.knoxhack.echonexusprotocol.registry.ModMenus;
import com.knoxhack.echonexusprotocol.registry.ModRecipes;
import com.knoxhack.echonexusprotocol.registry.ModSounds;
import com.knoxhack.echonexusprotocol.registry.ModWorldgen;
import com.knoxhack.echonexusprotocol.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod("echonexusprotocol")
public class EchoNexusProtocol {
   public static final String MODID = "echonexusprotocol";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoNexusProtocol(IEventBus modEventBus, ModContainer modContainer) {
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModEntities.register(modEventBus);
      ModItems.register(modEventBus);
      ModRecipes.register(modEventBus);
      ModMenus.register(modEventBus);
      ModSounds.register(modEventBus);
      ModWorldgen.register(modEventBus);
      ModAttachments.register(modEventBus);
      ModGameTests.register(modEventBus);
      ModCreativeTabs.register(modEventBus);
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(ModEntities::registerAttributes);
      modEventBus.addListener(ModEntities::registerSpawnPlacements);
      modEventBus.addListener(ModEnergyCapabilities::register);
      modEventBus.addListener(ModGameTests::registerTests);
      NeoForge.EVENT_BUS.register(new NexusWorldEvents());
      NeoForge.EVENT_BUS.register(new NexusArmorEvents());
      NeoForge.EVENT_BUS.register(new NexusCommandHandler());
      modContainer.registerConfig(Type.COMMON, Config.SPEC);
      Config.registerEchoConfig();
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("ECHO-7 Nexus systems initialized. Reality field telemetry online.");
      event.enqueueWork(() -> {
         NexusCoreIntegration.register();
         registerTerminalCommonIntegration();
      });
   }

   private static void registerTerminalCommonIntegration() {
      if (!ModList.get().isLoaded("echoterminal")) {
         return;
      }

      try {
         NexusTerminalCommonIntegration.register();
      } catch (LinkageError error) {
         LOGGER.warn("ECHO-7 Nexus Terminal common integration skipped because echoterminal APIs were unavailable.", error);
      }
   }
}
