package com.knoxhack.signalos;

import com.knoxhack.signalos.content.SignalOsBuiltinContent;
import com.knoxhack.signalos.content.SignalOsServerReloaders;
import com.knoxhack.signalos.registry.ModBlockEntities;
import com.knoxhack.signalos.registry.ModBlocks;
import com.knoxhack.signalos.registry.ModCreativeTabs;
import com.knoxhack.signalos.registry.ModDataComponents;
import com.knoxhack.signalos.registry.ModGameTests;
import com.knoxhack.signalos.registry.ModMenus;
import com.knoxhack.signalos.integration.SignalOsMissionCoreIntegration;
import com.knoxhack.signalos.service.SignalOsBuiltinActions;
import com.knoxhack.signalos.service.SignalOsTerminalServices;
import com.knoxhack.signalos.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(SignalOS.MODID)
public class SignalOS {
    public static final String MODID = "signalos";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SignalOS(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModGameTests.register(modEventBus);

        modEventBus.addListener(ModNetwork::registerPayloads);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(SignalOsServerReloaders::addServerReloadListeners);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SignalOsBuiltinActions.register();
            SignalOsBuiltinContent.register();
            SignalOsTerminalServices.registerEchoCoreServices();
            SignalOsMissionCoreIntegration.register();
        });
        LOGGER.info("SignalOS computer OS online.");
    }
}
