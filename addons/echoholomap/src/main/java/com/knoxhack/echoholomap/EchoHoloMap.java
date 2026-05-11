package com.knoxhack.echoholomap;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoholomap.command.HoloMapCommands;
import com.knoxhack.echoholomap.map.HoloMapService;
import com.knoxhack.echoholomap.map.HoloMapTerrainScanner;
import com.knoxhack.echoholomap.network.ModNetwork;
import com.knoxhack.echoholomap.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoHoloMap.MODID)
public final class EchoHoloMap {
    public static final String MODID = "echoholomap";
    public static final String CHAPTER_ID = "holomap";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoHoloMap(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModNetwork::registerPayloads);
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        Config.registerEchoConfig();
        NeoForge.EVENT_BUS.addListener(HoloMapCommands::register);
        NeoForge.EVENT_BUS.addListener(HoloMapTerrainScanner::onPlayerTick);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            HoloMapService.INSTANCE.registerBuiltins();
            EchoCoreServices.registerMapMarkerService(HoloMapService.INSTANCE);
            registerAddonChapter();
            if (ModList.get().isLoaded("echoterminal")) {
                registerTerminalIntegration();
            }
            LOGGER.info("ECHO: HoloMap online. {}", EchoCoreServices.platformProviderSummary());
        });
    }

    private static void registerAddonChapter() {
        if (EchoAddonRegistry.isRegistered(CHAPTER_ID)) {
            return;
        }
        EchoAddonRegistry.register(new EchoAddonChapter() {
            @Override
            public String id() {
                return CHAPTER_ID;
            }

            @Override
            public String modId() {
                return MODID;
            }

            @Override
            public String displayName() {
                return "ECHO: HoloMap";
            }

            @Override
            public String summary() {
                return "Terminal-integrated world telemetry, route, scan, and marker command map.";
            }

            @Override
            public String statusLine(Player player) {
                int layers = EchoCoreServices.mapLayers(player).size();
                int markers = EchoCoreServices.mapMarkers(player).size();
                return "HoloMap: " + layers + " layer(s), " + markers + " marker(s), "
                        + EchoCoreServices.mapMarkerService().providerCount() + " provider(s).";
            }
        });
    }

    private static void registerTerminalIntegration() {
        try {
            Class.forName("com.knoxhack.echoholomap.integration.HoloMapTerminalCommonIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("ECHO HoloMap terminal integration could not be registered.", exception);
        }
    }
}
