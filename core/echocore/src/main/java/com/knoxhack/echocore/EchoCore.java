package com.knoxhack.echocore;

import com.knoxhack.echocore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(EchoCore.MODID)
public class EchoCore {
    public static final String MODID = "echocore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoCore(IEventBus modEventBus) {
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        LOGGER.info("ECHO: Core API online.");
    }
}
