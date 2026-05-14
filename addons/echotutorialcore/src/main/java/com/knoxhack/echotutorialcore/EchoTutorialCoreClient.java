package com.knoxhack.echotutorialcore;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = EchoTutorialCore.MODID, dist = Dist.CLIENT)
public class EchoTutorialCoreClient {
    public EchoTutorialCoreClient(IEventBus modEventBus) {
        // Client-only UI setup (screens, overlays) can be registered here.
        // Networking payloads are registered from the common side via TutorialNetworking.
    }
}
