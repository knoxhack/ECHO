package com.knoxhack.signalos.api;

import com.knoxhack.signalos.SignalOS;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class TerminalActionRegistry {
    private static final Map<Key, TerminalActionHandler> HANDLERS = new ConcurrentHashMap<>();

    private TerminalActionRegistry() {
    }

    public static void register(Identifier pageId, Identifier actionId, TerminalActionHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("SignalOS terminal action handler is required.");
        }
        HANDLERS.put(new Key(
                TerminalIds.requireLowercase(pageId, "SignalOS action page"),
                TerminalIds.requireLowercase(actionId, "SignalOS action")),
                handler);
    }

    public static boolean handle(ServerPlayer player, Identifier pageId, Identifier actionId, String payload) {
        TerminalActionHandler handler = HANDLERS.get(new Key(pageId, actionId));
        if (handler == null) {
            return false;
        }
        try {
            handler.handle(player, payload == null ? "" : payload);
            return true;
        } catch (RuntimeException exception) {
            SignalOS.LOGGER.warn("SignalOS action {}:{} failed.", pageId, actionId, exception);
            return false;
        }
    }

    public static void clearForTests() {
        HANDLERS.clear();
    }

    public static void withClearedForTests(Runnable body) {
        Map<Key, TerminalActionHandler> snapshot = Map.copyOf(HANDLERS);
        HANDLERS.clear();
        try {
            body.run();
        } finally {
            HANDLERS.clear();
            HANDLERS.putAll(snapshot);
        }
    }

    private record Key(Identifier pageId, Identifier actionId) {
    }
}
