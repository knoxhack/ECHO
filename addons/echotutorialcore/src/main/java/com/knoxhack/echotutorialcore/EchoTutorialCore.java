package com.knoxhack.echotutorialcore;

import com.knoxhack.echotutorialcore.command.TutorialCommands;
import com.knoxhack.echotutorialcore.config.TutorialConfig;
import com.knoxhack.echotutorialcore.data.ModAttachments;
import com.knoxhack.echotutorialcore.data.TutorialDataReloadListener;
import com.knoxhack.echotutorialcore.integration.TutorialIntegrations;
import com.knoxhack.echotutorialcore.network.TutorialNetworking;
import com.knoxhack.echotutorialcore.server.TutorialHintManager;
import com.knoxhack.echotutorialcore.server.TutorialProgressManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(EchoTutorialCore.MODID)
public class EchoTutorialCore {
    public static final String MODID = "echotutorialcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoTutorialCore(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        TutorialNetworking.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::registerServerReloadListener);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);

        modContainer.registerConfig(ModConfig.Type.COMMON, TutorialConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, TutorialConfig.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO: TutorialCore online. Ashfall deep, but not confusing.");
        event.enqueueWork(() -> {
            TutorialIntegrations.registerOptionalIntegrations();
        });
    }

    private void registerServerReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(net.minecraft.resources.Identifier.fromNamespaceAndPath(MODID, "tutorial_data"), new TutorialDataReloadListener());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        TutorialCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        // Evaluate contextual hints periodically (every 5 seconds approx)
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 100 == 0) {
            for (var player : event.getServer().getPlayerList().getPlayers()) {
                TutorialHintManager.evaluateHints(player);
            }
        }
    }

}
