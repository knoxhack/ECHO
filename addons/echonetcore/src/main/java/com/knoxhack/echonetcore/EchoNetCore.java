package com.knoxhack.echonetcore;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echonetcore.config.EchoNetCoreConfig;
import com.knoxhack.echonetcore.network.EchoNetCorePackets;
import com.knoxhack.echonetcore.service.NetCoreNetworkService;
import com.knoxhack.echonetcore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(EchoNetCore.MODID)
public class EchoNetCore {
    public static final String MODID = "echonetcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoNetCore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(EchoNetCorePackets::register);
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        modContainer.registerConfig(ModConfig.Type.COMMON, EchoNetCoreConfig.SPEC);
        EchoNetCoreConfig.registerEchoConfig();
        EchoCoreServices.registerNetworkService(NetCoreNetworkService.INSTANCE);
        LOGGER.info("ECHO: NetCore packet bridge online.");
    }
}
