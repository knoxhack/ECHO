package com.knoxhack.echocore.api;

import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small shared service locator for optional ECHO integrations.
 */
public final class EchoServiceRegistry {
    private static final Map<Class<?>, Object> SERVICES = new ConcurrentHashMap<>();

    private EchoServiceRegistry() {
    }

    public static <T> void register(Class<T> serviceType, T service) {
        if (serviceType == null || service == null) {
            throw new IllegalArgumentException("Service type and implementation are required.");
        }
        SERVICES.put(serviceType, serviceType.cast(service));
    }

    public static <T> Optional<T> find(Class<T> serviceType) {
        Object service = SERVICES.get(serviceType);
        return service == null ? Optional.empty() : Optional.of(serviceType.cast(service));
    }

    public static <T> T getOrDefault(Class<T> serviceType, T fallback) {
        return find(serviceType).orElse(fallback);
    }

    public static void clearForTests() {
        SERVICES.clear();
    }

    public static void withClearedForTests(Runnable body) {
        Map<Class<?>, Object> previous = Map.copyOf(SERVICES);
        SERVICES.clear();
        try {
            body.run();
        } finally {
            SERVICES.clear();
            SERVICES.putAll(previous);
        }
    }
}
