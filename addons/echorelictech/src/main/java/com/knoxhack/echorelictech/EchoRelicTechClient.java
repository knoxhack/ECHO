package com.knoxhack.echorelictech;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(value = EchoRelicTech.MODID, dist = Dist.CLIENT)
public class EchoRelicTechClient {
    public EchoRelicTechClient(IEventBus modEventBus) {
        if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalClientIntegration();
        }
    }

    private static void registerTerminalClientIntegration() {
        try {
            Class.forName("com.knoxhack.echorelictech.integration.terminal.RelicTechTerminalClientIntegration")
                .getMethod("register")
                .invoke(null);
        } catch (ReflectiveOperationException | LinkageError e) {
            EchoRelicTech.LOGGER.warn("RelicTech Terminal client integration could not be registered.", e);
        }
    }
}
