package com.knoxhack.echostationfall;

import com.knoxhack.echostationfall.event.ModTooltipEvents;
import com.knoxhack.echostationfall.event.StationfallEvents;
import com.knoxhack.echostationfall.integration.StationfallCoreIntegration;
import com.knoxhack.echostationfall.integration.StationfallTerminalCommonIntegration;
import com.knoxhack.echostationfall.registry.*;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoStationfall.MODID)
public class EchoStationfall {
    public static final String MODID = "echostationfall";
    public static final Logger LOGGER = LogUtils.getLogger();
    public EchoStationfall(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus); ModEntities.register(modEventBus); ModItems.register(modEventBus);
        ModWorldgen.register(modEventBus); ModGameTests.register(modEventBus); ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::commonSetup); modEventBus.addListener(ModEntities::registerAttributes); modEventBus.addListener(ModGameTests::registerTests);
        NeoForge.EVENT_BUS.register(new StationfallEvents()); NeoForge.EVENT_BUS.register(new ModTooltipEvents());
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    private void commonSetup(FMLCommonSetupEvent event) { event.enqueueWork(() -> { StationfallCoreIntegration.register(); if (ModList.get().isLoaded("echoterminal")) StationfallTerminalCommonIntegration.register(); }); }
}
