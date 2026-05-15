package com.knoxhack.echorecovery;

import com.knoxhack.echorecovery.command.GravesCommand;
import com.knoxhack.echorecovery.config.RecoveryConfig;
import com.knoxhack.echorecovery.data.RecoveryWorldData;
import com.knoxhack.echorecovery.grave.DeathHandler;
import com.knoxhack.echorecovery.integration.RecoveryIntegrationDispatcher;
import com.knoxhack.echorecovery.net.RecoveryPackets;
import com.knoxhack.echorecovery.registry.ModBlockEntities;
import com.knoxhack.echorecovery.registry.ModBlocks;
import com.knoxhack.echorecovery.registry.ModCreativeTabs;
import com.knoxhack.echorecovery.registry.ModDataComponents;
import com.knoxhack.echorecovery.registry.ModItems;
import com.knoxhack.echorecovery.registry.ModMenus;
import com.knoxhack.echorecovery.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(EchoRecovery.MODID)
public class EchoRecovery {
    public static final String MODID = "echorecovery";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static boolean ashfallLoaded = false;

    public EchoRecovery(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RecoveryConfig.SPEC);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
        RecoveryPackets.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        DeathHandler.register();
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        ashfallLoaded = ModList.get().isLoaded("echoashfallprotocol");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO Recovery online. Standalone recovery enabled.");
        event.enqueueWork(() -> {
            RecoveryIntegrationDispatcher.registerCommon();
        });
    }

    private void registerCommands(RegisterCommandsEvent event) {
        GravesCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    private void onServerStarted(ServerStartedEvent event) {
        RecoveryWorldData.getOrCreate(event.getServer().overworld());
    }

    private void onServerStopping(ServerStoppingEvent event) {
    }

    public static boolean isAshfallLoaded() {
        return ashfallLoaded;
    }

    public static String displayName() {
        return ashfallLoaded ? "Field Recovery" : "Graves";
    }
}
