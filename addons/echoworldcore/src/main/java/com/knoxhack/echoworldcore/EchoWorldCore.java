package com.knoxhack.echoworldcore;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoworldcore.content.WorldCoreReloaders;
import com.knoxhack.echoworldcore.integration.WorldCoreDiscoveryProvider;
import com.knoxhack.echoworldcore.integration.WorldCoreIndexProvider;
import com.knoxhack.echoworldcore.registry.WorldCoreBuiltins;
import com.knoxhack.echoworldcore.service.WorldRegionService;
import com.knoxhack.echoworldcore.test.ModGameTests;
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

@Mod(EchoWorldCore.MODID)
public final class EchoWorldCore {
    public static final String MODID = "echoworldcore";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String CHAPTER_ID = "world_core";

    public EchoWorldCore(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Config.registerEchoConfig();
        ModGameTests.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModGameTests::registerTests);
        NeoForge.EVENT_BUS.addListener(WorldCoreReloaders::addServerReloadListeners);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WorldRegionService service = WorldRegionService.INSTANCE;
            WorldCoreBuiltins.register(service);
            EchoCoreServices.registerWorldRegionService(service);
            EchoCoreServices.registerDiscoveryProvider(new WorldCoreDiscoveryProvider(service));
            EchoCoreServices.registerIndexRecipeProvider(WorldCoreIndexProvider.INSTANCE);
            registerAddonChapter();
            if (ModList.get().isLoaded("echoterminal")) {
                registerTerminalIntegration();
            }
            LOGGER.info("ECHO WorldCore initialized with {} region definitions and {} hazard definitions.",
                    service.regionDefinitions().size(), service.hazardDefinitions().size());
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
                return "ECHO: WorldCore";
            }

            @Override
            public String summary() {
                return "Shared world regions, hazards, markers, structure discovery, and runtime world events.";
            }

            @Override
            public String statusLine(Player player) {
                WorldRegionService service = WorldRegionService.INSTANCE;
                if (player == null) {
                    return "WorldCore: " + service.regionDefinitions().size() + " shared region definitions online.";
                }
                return "WorldCore: " + service.activeRegions(player).size() + " active region(s), "
                        + service.markers(player).size() + " known marker(s).";
            }
        });
    }

    private static void registerTerminalIntegration() {
        try {
            Class.forName("com.knoxhack.echoworldcore.integration.WorldCoreTerminalIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("WorldCore terminal integration could not be registered.", exception);
        }
    }
}
