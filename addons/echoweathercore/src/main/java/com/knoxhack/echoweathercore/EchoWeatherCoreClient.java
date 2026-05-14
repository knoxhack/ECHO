package com.knoxhack.echoweathercore;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(value = EchoWeatherCore.MODID, dist = Dist.CLIENT)
public class EchoWeatherCoreClient {
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoWeatherCoreClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        LOGGER.info("ECHO: WeatherCore client setup complete.");
    }
}
