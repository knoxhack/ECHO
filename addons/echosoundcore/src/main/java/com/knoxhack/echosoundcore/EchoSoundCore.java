package com.knoxhack.echosoundcore;

import com.knoxhack.echosoundcore.client.config.SoundCoreConfig;
import com.knoxhack.echosoundcore.command.SoundCoreCommands;
import com.knoxhack.echosoundcore.data.SoundCoreDataReloadListener;
import com.knoxhack.echosoundcore.integration.SoundCoreCoreIntegration;
import com.knoxhack.echosoundcore.registry.SoundCoreSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(EchoSoundCore.MODID)
public class EchoSoundCore {
    public static final String MODID = "echosoundcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoSoundCore(IEventBus modEventBus, ModContainer modContainer) {
        SoundCoreSounds.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modContainer.registerConfig(ModConfig.Type.CLIENT, SoundCoreConfig.CLIENT_SPEC);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO: SoundCore online. Adaptive audio framework initializing.");
        event.enqueueWork(() -> {
            SoundCoreCoreIntegration.registerAddonChapter();
            registerOptionalIntegrations();
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        SoundCoreCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    private void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(id("audio_profiles"), SoundCoreDataReloadListener.INSTANCE);
    }

    private static void registerOptionalIntegrations() {
        tryOptional("com.knoxhack.echosoundcore.integration.terminal.SoundCoreTerminalIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.mission.SoundCoreMissionIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.lens.SoundCoreLensIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.holomap.SoundCoreHoloMapIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.powergrid.SoundCorePowerGridIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.signaloos.SoundCoreSignalOSIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.worldcore.SoundCoreWorldCoreIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.nexus.SoundCoreNexusIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.blackbox.SoundCoreBlackboxIntegration");
        tryOptional("com.knoxhack.echosoundcore.integration.stationfall.SoundCoreStationfallIntegration");
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
