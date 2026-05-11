package com.knoxhack.echomissioncore;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echomissioncore.command.MissionCoreCommands;
import com.knoxhack.echomissioncore.content.MissionCoreReloaders;
import com.knoxhack.echomissioncore.integration.MissionCoreTerminalIntegration;
import com.knoxhack.echomissioncore.integration.MissionCoreWorldCoreConsumer;
import com.knoxhack.echomissioncore.registry.ModAttachments;
import com.knoxhack.echomissioncore.service.MissionCoreService;
import com.knoxhack.echomissioncore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoMissionCore.MODID)
public final class EchoMissionCore {
    public static final String MODID = "echomissioncore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoMissionCore(IEventBus modEventBus) {
        ModAttachments.register(modEventBus);
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(MissionCoreCommands::register);
        NeoForge.EVENT_BUS.addListener(MissionCoreReloaders::addServerReloadListeners);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EchoCoreServices.registerMissionService(MissionCoreService.INSTANCE);
            MissionCoreService.INSTANCE.registerBuiltInContent();
            MissionCoreWorldCoreConsumer.register();
            if (ModList.get().isLoaded("echoterminal")) {
                MissionCoreTerminalIntegration.register();
            }
            LOGGER.info("ECHO: MissionCore online with {} missions.",
                    MissionCoreService.INSTANCE.missionDefinitions().size());
        });
    }
}
