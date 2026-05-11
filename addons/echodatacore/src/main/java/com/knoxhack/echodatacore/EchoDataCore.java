package com.knoxhack.echodatacore;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echodatacore.command.DataCoreCommands;
import com.knoxhack.echodatacore.integration.DataCoreWorldCoreConsumer;
import com.knoxhack.echodatacore.network.ModNetwork;
import com.knoxhack.echodatacore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoDataCore.MODID)
public class EchoDataCore {
    public static final String MODID = "echodatacore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoDataCore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ModNetwork::registerPayloads);
        modEventBus.addListener(this::commonSetup);
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Config.registerEchoConfig();

        NeoForge.EVENT_BUS.addListener(DataCoreCommands::register);
        NeoForge.EVENT_BUS.addListener(DataCoreDataService.INSTANCE::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(DataCoreDataService.INSTANCE::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(DataCoreDataService.INSTANCE::onPlayerTick);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EchoCoreServices.registerDataService(DataCoreDataService.INSTANCE);
            DataCoreBuiltinKeys.register();
            DataCoreWorldCoreConsumer.register();
            LOGGER.info("ECHO: DataCore registered. {}", EchoCoreServices.platformProviderSummary());
        });
    }
}
