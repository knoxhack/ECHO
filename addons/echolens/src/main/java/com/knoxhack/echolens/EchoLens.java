package com.knoxhack.echolens;

import com.knoxhack.echolens.config.LensConfig;
import com.knoxhack.echolens.integration.LensCoreIntegration;
import com.knoxhack.echolens.integration.LensMissionCoreIntegration;
import com.knoxhack.echolens.network.ModNetwork;
import com.knoxhack.echolens.provider.LensBuiltins;
import com.knoxhack.echolens.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(EchoLens.MODID)
public class EchoLens {
    public static final String MODID = "echolens";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoLens(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, LensConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, LensConfig.CLIENT_SPEC);
        LensConfig.registerEchoConfig();
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModNetwork::registerPayloads);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LensBuiltins.register();
            LensCoreIntegration.register();
            LensMissionCoreIntegration.register();
            if (ModList.get().isLoaded("echoterminal")) {
                registerTerminalIntegration();
            }
            LOGGER.info("ECHO: Lens scanner HUD online with {} providers.",
                    com.knoxhack.echolens.registry.LensProviderRegistry.count());
            LOGGER.info("ECHO: Lens server-assisted Deep Scan online with {} server providers.",
                    com.knoxhack.echolens.registry.LensProviderRegistry.serverProviders().size());
        });
    }

    private static void registerTerminalIntegration() {
        try {
            Class.forName("com.knoxhack.echolens.integration.LensTerminalCommonIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("ECHO: Lens terminal integration could not be registered.", exception);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
