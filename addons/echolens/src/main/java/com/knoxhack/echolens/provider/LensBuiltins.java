package com.knoxhack.echolens.provider;

import com.knoxhack.echolens.registry.LensProviderRegistry;

public final class LensBuiltins {
    private static boolean registered;

    private LensBuiltins() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        LensProviderRegistry.register(TargetIdentityProvider.INSTANCE);
        LensProviderRegistry.register(BlockStatsProvider.INSTANCE);
        LensProviderRegistry.register(FluidStateProvider.INSTANCE);
        LensProviderRegistry.register(EntityStatsProvider.INSTANCE);
        LensProviderRegistry.register(MachineStatusProvider.INSTANCE);
        LensProviderRegistry.register(SafeInventoryProvider.INSTANCE);
        LensProviderRegistry.register(IntegrationStatusProvider.INSTANCE);
        LensProviderRegistry.register(BeginnerHintProvider.INSTANCE);
        registered = true;
    }

    public static synchronized void resetForTests() {
        registered = false;
        LensProviderRegistry.clearForTests();
    }
}
