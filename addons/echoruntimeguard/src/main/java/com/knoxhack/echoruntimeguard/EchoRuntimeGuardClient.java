package com.knoxhack.echoruntimeguard;

import com.knoxhack.echoruntimeguard.client.ClientFpsMonitor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = EchoRuntimeGuard.MODID, dist = Dist.CLIENT)
public final class EchoRuntimeGuardClient {
    public EchoRuntimeGuardClient() {
        NeoForge.EVENT_BUS.addListener(ClientFpsMonitor::onClientTick);
        if (ModList.get().isLoaded("echoterminal")) {
            registerTerminalIntegration();
        }
    }

    private static void registerTerminalIntegration() {
        try {
            Class.forName("com.knoxhack.echoruntimeguard.integration.RuntimeGuardTerminalClientIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            EchoRuntimeGuard.LOGGER.warn("RuntimeGuard Terminal page could not be registered.", exception);
        }
    }
}
