package com.knoxhack.echoweathercore;

import com.knoxhack.echoweathercore.client.config.WeatherCoreClientConfig;
import com.knoxhack.echoweathercore.command.WeatherCoreCommands;
import com.knoxhack.echoweathercore.config.WeatherCoreConfig;
import com.knoxhack.echoweathercore.data.WeatherDataReloadListener;
import com.knoxhack.echoweathercore.registry.WeatherCoreBlockEntities;
import com.knoxhack.echoweathercore.registry.WeatherCoreBlocks;
import com.knoxhack.echoweathercore.registry.WeatherCoreCreativeTabs;
import com.knoxhack.echoweathercore.registry.WeatherCoreItems;
import com.knoxhack.echoweathercore.registry.WeatherCoreMenus;
import com.knoxhack.echoweathercore.server.WeatherScheduler;
import com.knoxhack.echoweathercore.server.WeatherStateManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

@Mod(EchoWeatherCore.MODID)
public class EchoWeatherCore {
    public static final String MODID = "echoweathercore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoWeatherCore(IEventBus modEventBus, ModContainer modContainer) {
        WeatherCoreItems.register(modEventBus);
        WeatherCoreBlocks.register(modEventBus);
        WeatherCoreBlockEntities.register(modEventBus);
        WeatherCoreMenus.register(modEventBus);
        WeatherCoreCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modContainer.registerConfig(ModConfig.Type.SERVER, WeatherCoreConfig.SERVER_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, WeatherCoreClientConfig.CLIENT_SPEC);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onLevelTick);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO: WeatherCore online. Atmospheric hazard framework initializing.");
        event.enqueueWork(() -> registerOptionalIntegrations());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        WeatherCoreCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    private void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(id("weather_profiles"), WeatherDataReloadListener.INSTANCE);
    }

    private void onServerStarting(ServerStartingEvent event) {
        WeatherStateManager.getInstance().onServerStarting(event.getServer());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        WeatherStateManager.getInstance().onServerStopping();
    }

    private void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        WeatherScheduler.tick(event.getLevel());
        WeatherStateManager.getInstance().tickLevel(event.getLevel());
    }

    private static void registerOptionalIntegrations() {
        tryOptional("com.knoxhack.echoweathercore.integration.terminal.WeatherCoreTerminalIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.holomap.WeatherCoreHoloMapIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.lens.WeatherCoreLensIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.powergrid.WeatherCorePowerGridIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.soundcore.WeatherCoreSoundCoreIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.worldcore.WeatherCoreWorldCoreIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.mission.WeatherCoreMissionIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.nexus.WeatherCoreNexusIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.tutorial.WeatherCoreTutorialIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.drone.WeatherCoreDroneIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.faction.WeatherCoreFactionIntegration");
        tryOptional("com.knoxhack.echoweathercore.integration.runtimeguard.WeatherCoreRuntimeGuardIntegration");
    }

    private static void tryOptional(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Method m = clazz.getMethod("register");
            m.invoke(null);
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Optional integration {} not present.", className);
        } catch (ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("Optional integration {} could not be registered.", className, e);
        }
    }

    public static net.minecraft.resources.Identifier id(String path) {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(MODID, path);
    }
}
