package com.knoxhack.echoplayercore;

import com.knoxhack.echoplayercore.command.PlayerCoreCommands;
import com.knoxhack.echoplayercore.config.PlayerCoreConfig;
import com.knoxhack.echoplayercore.integration.PlayerCoreIntegrations;
import com.knoxhack.echoplayercore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoPlayerCore.MODID)
public final class EchoPlayerCore {
    public static final String MODID = "echoplayercore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoPlayerCore(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, PlayerCoreConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(PlayerCoreCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(PlayerCoreCommands::onPlayerDeath);
        NeoForge.EVENT_BUS.addListener(PlayerCoreCommands::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(PlayerCoreCommands::onPlayerRespawn);
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        PlayerCoreCommands.registerEchoSubcommands();
        PlayerCoreIntegrations.logIntegrationStatus();
        LOGGER.info("ECHO PlayerCore initialized.");
    }
}
