package com.knoxhack.echoterminal.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class TerminalActionRegistry {
    private static final Map<Key, TerminalActionHandler> HANDLERS = new ConcurrentHashMap<>();

    private TerminalActionRegistry() {
    }

    public static void register(Identifier tabId, Identifier actionId, TerminalActionHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Terminal action handler is required.");
        }
        HANDLERS.put(new Key(
                TerminalApiIds.requireLowercase(tabId, "Terminal action tab"),
                TerminalApiIds.requireLowercase(actionId, "Terminal action")), handler);
    }

    public static boolean handle(ServerPlayer player, Identifier tabId, Identifier actionId, String payload) {
        Optional<TerminalActionHandler> handler = Optional.ofNullable(HANDLERS.get(new Key(tabId, actionId)));
        handler.ifPresent(value -> value.handle(player, payload));
        return handler.isPresent();
    }

    public static void clearForTests() {
        HANDLERS.clear();
    }

    public static void withClearedForTests(Runnable runnable) {
        Map<Key, TerminalActionHandler> snapshot = Map.copyOf(HANDLERS);
        HANDLERS.clear();
        try {
            runnable.run();
        } finally {
            HANDLERS.clear();
            HANDLERS.putAll(snapshot);
        }
    }

    private record Key(Identifier tabId, Identifier actionId) {
    }
}
