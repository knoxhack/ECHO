package com.knoxhack.echoruntimeguard.api;

import com.knoxhack.echoruntimeguard.runtime.BlockEntitySleepService;
import com.knoxhack.echoruntimeguard.runtime.EntityAiGuardService;
import com.knoxhack.echoruntimeguard.runtime.IntegrationThrottleService;
import com.knoxhack.echoruntimeguard.runtime.MultiblockValidationScheduler;
import com.knoxhack.echoruntimeguard.runtime.NetworkBudgetService;
import com.knoxhack.echoruntimeguard.runtime.ParticleBudgetService;
import com.knoxhack.echoruntimeguard.runtime.PerformanceBudgetService;
import com.knoxhack.echoruntimeguard.runtime.RuntimeModeService;
import com.knoxhack.echoruntimeguard.runtime.RuntimeProfilerService;
import com.knoxhack.echoruntimeguard.runtime.SmartTickService;

public final class RuntimeGuardServices {
    private RuntimeGuardServices() {
    }

    public static RuntimeModeService modes() {
        return RuntimeModeService.INSTANCE;
    }

    public static RuntimeProfilerService metrics() {
        return RuntimeProfilerService.INSTANCE;
    }

    public static PerformanceBudgetService budgets() {
        return PerformanceBudgetService.INSTANCE;
    }

    public static SmartTickService smartTicks() {
        return SmartTickService.INSTANCE;
    }

    public static BlockEntitySleepService blockEntitySleep() {
        return BlockEntitySleepService.INSTANCE;
    }

    public static ParticleBudgetService particles() {
        return ParticleBudgetService.INSTANCE;
    }

    public static MultiblockValidationScheduler multiblocks() {
        return MultiblockValidationScheduler.INSTANCE;
    }

    public static NetworkBudgetService network() {
        return NetworkBudgetService.INSTANCE;
    }

    public static IntegrationThrottleService integrations() {
        return IntegrationThrottleService.INSTANCE;
    }

    public static EntityAiGuardService entities() {
        return EntityAiGuardService.INSTANCE;
    }
}
