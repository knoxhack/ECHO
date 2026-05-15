package com.knoxhack.echocore.api;

/**
 * Safe fallback used when ECHO: RuntimeGuard is not installed.
 */
public final class NoOpRuntimeBudgetService implements IRuntimeBudgetService {
    public static final NoOpRuntimeBudgetService INSTANCE = new NoOpRuntimeBudgetService();

    private NoOpRuntimeBudgetService() {
    }
}
