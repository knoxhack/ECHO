package com.knoxhack.echoterminal.api;

import com.knoxhack.echoterminal.EchoTerminal;
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
        if (tabId == null || actionId == null) {
            return false;
        }
        Optional<TerminalActionHandler> handler = Optional.ofNullable(HANDLERS.get(new Key(tabId, actionId)));
        if (handler.isEmpty()) {
            return false;
        }
        try {
            handler.get().handle(player, payload);
            return true;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal action {}:{} failed; ignoring action.",
                    tabId, actionId, exception);
            return false;
        }
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
