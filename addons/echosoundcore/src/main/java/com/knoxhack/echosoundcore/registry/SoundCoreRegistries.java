package com.knoxhack.echosoundcore.registry;

import net.neoforged.bus.api.IEventBus;

public final class SoundCoreRegistries {
    private SoundCoreRegistries() {}

    public static void register(IEventBus eventBus) {
        SoundCoreSounds.register(eventBus);
    }
}
