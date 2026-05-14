package com.knoxhack.echopowergrid.integration.runtimeguard;

import com.knoxhack.echopowergrid.EchoPowerGrid;

public final class PowerGridRuntimeGuardIntegration {
    private PowerGridRuntimeGuardIntegration() {}

    public static void register() {
        EchoPowerGrid.LOGGER.info("ECHO PowerGrid RuntimeGuard integration registered.");
        // Future: hook into SmartTickService for grid update budgets
    }
}
