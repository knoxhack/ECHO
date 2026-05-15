package com.knoxhack.echoindex;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoindex.content.IndexReloaders;
import com.knoxhack.echoindex.integration.IndexMissionCoreIntegration;
import com.knoxhack.echoindex.network.ModNetwork;
import com.knoxhack.echoindex.service.BuiltinIndexProvider;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echoindex.service.IndexSourceRecipeProvider;
import com.knoxhack.echoindex.service.VanillaIndexRecipeProvider;
import com.knoxhack.echoindex.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoIndex.MODID)
public class EchoIndex {
    public static final String MODID = "echoindex";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoIndex(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Config.registerEchoConfig();
        ModGameTests.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModNetwork::registerPayloads);
        modEventBus.addListener(ModGameTests::registerTests);
        NeoForge.EVENT_BUS.addListener(IndexReloaders::addServerReloadListeners);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO: Index is assembling the shared archive.");
        event.enqueueWork(() -> {
            EchoAddonRegistry.register(new EchoAddonChapter() {
                @Override
                public String id() {
                    return "index";
                }

                @Override
                public String modId() {
                    return MODID;
                }

                @Override
                public String displayName() {
                    return "ECHO: Index";
                }

                @Override
                public String summary() {
                    return "Shared item, recipe, usage, and archive browser.";
                }

                @Override
                public String statusLine(net.minecraft.world.entity.player.Player player) {
                    return "Index online.";
                }
            });
            EchoCoreServices.registerIndexService(IndexService.INSTANCE);
            EchoCoreServices.registerIndexProvider(BuiltinIndexProvider.INSTANCE);
            EchoCoreServices.registerIndexRecipeProvider(VanillaIndexRecipeProvider.INSTANCE);
            EchoCoreServices.registerIndexRecipeProvider(IndexSourceRecipeProvider.INSTANCE);
            if (ModList.get().isLoaded("echomissioncore")) {
                IndexMissionCoreIntegration.register();
            }
            if (ModList.get().isLoaded("echoterminal")) {
                registerTerminalIntegration();
            }
        });
    }

    private static void registerTerminalIntegration() {
        try {
            Class.forName("com.knoxhack.echoindex.integration.IndexTerminalCommonIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("ECHO: Index terminal integration could not be registered.", exception);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
