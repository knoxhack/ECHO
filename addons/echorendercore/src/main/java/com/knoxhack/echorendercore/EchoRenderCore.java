package com.knoxhack.echorendercore;

import com.knoxhack.echorendercore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(EchoRenderCore.MODID)
public class EchoRenderCore {
   public static final String MODID = "echorendercore";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EchoRenderCore(IEventBus modEventBus) {
      ModGameTests.register(modEventBus);
      modEventBus.addListener(ModGameTests::registerTests);
      LOGGER.info("ECHO: RenderCore online.");
   }
}
