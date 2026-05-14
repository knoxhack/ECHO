package com.knoxhack.echorendercore;

import com.knoxhack.echorendercore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(EchoRenderCore.MODID)
public class EchoRenderCore {
   public static final String MODID = "echorendercore";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoRenderCore(IEventBus modEventBus, ModContainer modContainer) {
      modContainer.registerConfig(ModConfig.Type.CLIENT, RenderCoreConfig.CLIENT_SPEC);
      try {
         ModGameTests.register(modEventBus);
         modEventBus.addListener(ModGameTests::registerTests);
      } catch (NoClassDefFoundError error) {
         LOGGER.debug("RenderCore GameTest hooks are unavailable in this packaged runtime.");
      }
      LOGGER.info("ECHO: RenderCore online.");
   }
}
