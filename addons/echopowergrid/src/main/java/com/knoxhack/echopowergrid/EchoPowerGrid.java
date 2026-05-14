package com.knoxhack.echopowergrid;

import com.knoxhack.echopowergrid.commands.EchoPowerCommands;
import com.knoxhack.echopowergrid.config.PowerGridConfig;
import com.knoxhack.echopowergrid.grid.PowerNetworkManager;
import com.knoxhack.echopowergrid.integration.PowerGridCoreIntegration;
import com.knoxhack.echopowergrid.registry.ModBlockEntities;
import com.knoxhack.echopowergrid.registry.ModBlocks;
import com.knoxhack.echopowergrid.registry.ModCapabilities;
import com.knoxhack.echopowergrid.registry.ModCreativeTabs;
import com.knoxhack.echopowergrid.registry.ModItems;
import com.knoxhack.echopowergrid.registry.ModMenus;
import com.knoxhack.echopowergrid.test.PowerGridGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod("echopowergrid")
public class EchoPowerGrid {
    public static final String MODID = "echopowergrid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoPowerGrid(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(PowerGridGameTests::registerTests);
        PowerGridGameTests.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        modContainer.registerConfig(ModConfig.Type.COMMON, PowerGridConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO PowerGrid online. Restore the grid. Power the signal.");
        event.enqueueWork(() -> {
            PowerGridCoreIntegration.registerAddonChapter();
            registerOptionalIntegrations();
        });
    }

    private void onServerTick(ServerTickEvent.Post event) {
        PowerNetworkManager.tickAll(event.getServer());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        EchoPowerCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    private static void registerOptionalIntegrations() {
        if (ModList.get().isLoaded("echoterminal")) {
            tryInvoke("com.knoxhack.echopowergrid.integration.terminal.PowerGridTerminalIntegration");
        }
        if (ModList.get().isLoaded("echolens")) {
            tryInvoke("com.knoxhack.echopowergrid.integration.lens.PowerGridLensIntegration");
        }
        if (ModList.get().isLoaded("echoruntimeguard")) {
            tryInvoke("com.knoxhack.echopowergrid.integration.runtimeguard.PowerGridRuntimeGuardIntegration");
        }
        if (ModList.get().isLoaded("echomultiblockcore")) {
            tryInvoke("com.knoxhack.echopowergrid.integration.multiblock.PowerGridMultiblockIntegration");
        }
        if (ModList.get().isLoaded("echoindustrialnexus")) {
            tryInvoke("com.knoxhack.echopowergrid.integration.industrial.PowerGridIndustrialIntegration");
        }
    }

    private static void tryInvoke(String className) {
        try {
            Class.forName(className).getMethod("register").invoke(null);
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Optional integration {} not present.", className);
        } catch (ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("Optional integration {} could not be registered.", className, e);
        }
    }
}
