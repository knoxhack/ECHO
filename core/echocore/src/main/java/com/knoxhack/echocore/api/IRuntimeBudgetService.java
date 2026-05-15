package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Map;

/**
 * Optional runtime budget / performance diagnostics provider.
 * The fallback is intentionally inert so mods can run without RuntimeGuard.
 */
public interface IRuntimeBudgetService {

    default boolean available() {
        return false;
    }

    default double currentMs(String category) {
        return 0.0;
    }

    default double budgetMs(String category) {
        return Double.MAX_VALUE;
    }

    default boolean isOverBudget(String category) {
        return false;
    }

    default Map<String, Double> snapshot() {
        return Map.of();
    }

    default List<String> categories() {
        return List.of();
    }
}
