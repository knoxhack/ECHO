package com.knoxhack.echoindex.integration;

import com.knoxhack.echoterminal.api.recipe.TerminalRecipeRegistry;
import java.util.concurrent.atomic.AtomicBoolean;

public final class IndexTerminalCommonIntegration {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private IndexTerminalCommonIntegration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalRecipeRegistry.register(IndexTerminalRecipeProvider.INSTANCE);
    }
}
