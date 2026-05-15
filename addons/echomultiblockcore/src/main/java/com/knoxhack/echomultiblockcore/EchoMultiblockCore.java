package com.knoxhack.echomultiblockcore;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echomultiblockcore.api.MultiblockIntegrationServices;
import com.knoxhack.echomultiblockcore.command.MultiblockCommands;
import com.knoxhack.echomultiblockcore.content.MultiblockReloaders;
import com.knoxhack.echomultiblockcore.integration.MultiblockMapDataProvider;
import com.knoxhack.echomultiblockcore.integration.MultiblockIndexProvider;
import com.knoxhack.echomultiblockcore.integration.MultiblockMissionCoreIntegration;
import com.knoxhack.echomultiblockcore.network.ModNetwork;
import com.knoxhack.echomultiblockcore.registry.ModBlockEntities;
import com.knoxhack.echomultiblockcore.registry.ModBlocks;
import com.knoxhack.echomultiblockcore.registry.ModCreativeTabs;
import com.knoxhack.echomultiblockcore.registry.ModDataComponents;
import com.knoxhack.echomultiblockcore.registry.ModGameTests;
import com.knoxhack.echomultiblockcore.registry.ModItems;
import com.knoxhack.echomultiblockcore.registry.ModMenus;
import com.knoxhack.echomultiblockcore.runtime.MultiblockRuntimeEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoMultiblockCore.MODID)
public final class EchoMultiblockCore {
    public static final String MODID = "echomultiblockcore";
    public static final String CHAPTER_ID = "multiblock_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoMultiblockCore(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModGameTests.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Config.registerEchoConfig();

        modEventBus.addListener(ModNetwork::registerPayloads);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(MultiblockReloaders::addServerReloadListeners);
        NeoForge.EVENT_BUS.addListener(MultiblockCommands::register);
        NeoForge.EVENT_BUS.addListener(MultiblockRuntimeEvents::onServerTick);
        NeoForge.EVENT_BUS.addListener(MultiblockRuntimeEvents::onPlayerLoggedIn);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerAddonChapter();
            MultiblockIntegrationServices.registerDefaultProviders();
            EchoCoreServices.registerMapDataProvider(MultiblockMapDataProvider.INSTANCE);
            EchoCoreServices.registerIndexRecipeProvider(MultiblockIndexProvider.INSTANCE);
            if (ModList.get().isLoaded("echomissioncore")) {
                MultiblockMissionCoreIntegration.register();
            }
            if (ModList.get().isLoaded("echoterminal")) {
                registerTerminalBridge();
            }
        });
        LOGGER.info("ECHO MultiblockCore online. Facility runtime awaiting controllers.");
    }

    private static void registerTerminalBridge() {
        try {
            Class.forName("com.knoxhack.echomultiblockcore.integration.terminal.MultiblockTerminalBridge")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("ECHO MultiblockCore terminal bridge could not be registered.", exception);
        }
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
                return "ECHO: MultiblockCore";
            }

            @Override
            public String summary() {
                return "Shared multiblock, blueprint, robotic automation, and facility runtime framework.";
            }

            @Override
            public String statusLine(Player player) {
                return "MultiblockCore: " + com.knoxhack.echomultiblockcore.content.MultiblockContent.definitions().size()
                        + " definition(s), controllers online through world caches.";
            }
        });
    }
}
