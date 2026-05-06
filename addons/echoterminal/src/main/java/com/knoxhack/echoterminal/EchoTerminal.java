package com.knoxhack.echoterminal;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import com.knoxhack.echoterminal.mission.MainSurvivalQuestProvider;
import com.knoxhack.echoterminal.mission.VanillaJourneyProvider;
import com.knoxhack.echoterminal.network.ModNetwork;
import com.knoxhack.echoterminal.registry.ModBlockEntities;
import com.knoxhack.echoterminal.registry.ModBlocks;
import com.knoxhack.echoterminal.registry.ModAttachments;
import com.knoxhack.echoterminal.registry.ModCreativeTabs;
import com.knoxhack.echoterminal.registry.ModMenus;
import com.knoxhack.echoterminal.service.EchoTerminalCoreServices;
import com.knoxhack.echoterminal.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(EchoTerminal.MODID)
public class EchoTerminal {
    public static final String MODID = "echoterminal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoTerminal(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModGameTests.register(modEventBus);

        modEventBus.addListener(ModNetwork::registerPayloads);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EchoTerminalCoreServices.register();
            TerminalMissionRegistry.register(MainSurvivalQuestProvider.INSTANCE);
            TerminalMissionRegistry.register(VanillaJourneyProvider.INSTANCE);
            TerminalMissionActions.registerForTab(MainSurvivalQuestProvider.TAB_ID);
            TerminalMissionActions.registerForTab(VanillaJourneyProvider.TAB_ID);
            LOGGER.info("ECHO platform providers after Terminal setup: {}",
                    EchoCoreServices.platformProviderSummary());
        });
        LOGGER.info("ECHO: Terminal modular shell online.");
    }
}
