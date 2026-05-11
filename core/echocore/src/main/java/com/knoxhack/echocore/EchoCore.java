package com.knoxhack.echocore;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.registry.ModAttachments;
import com.knoxhack.echocore.test.ModGameTests;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

@Mod(EchoCore.MODID)
public class EchoCore {
    public static final String MODID = "echocore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoCore(IEventBus modEventBus) {
        ModAttachments.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(EchoCore::onPlayerLogin);
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        LOGGER.info("ECHO: Core API online.");
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EchoCoreServices.syncFactionDataToClient(player);
            EchoCoreServices.syncDiscoveryDataToClient(player);
        }
    }
}
