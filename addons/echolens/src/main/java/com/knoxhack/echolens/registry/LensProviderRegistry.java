package com.knoxhack.echolens.registry;

import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.LensInfoProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.Identifier;

public final class LensProviderRegistry {
    private static final Map<Identifier, LensInfoProvider> PROVIDERS_BY_ID = new ConcurrentHashMap<>();
    private static final List<LensInfoProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private LensProviderRegistry() {
    }

    public static void register(LensInfoProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Lens provider is required.");
        }
        Identifier id = provider.id();
        if (id == null) {
            throw new IllegalArgumentException("Lens provider id is required: " + provider.getClass().getName());
        }
        LensInfoProvider existing = PROVIDERS_BY_ID.putIfAbsent(id, provider);
        if (existing != null && existing != provider) {
            throw new IllegalArgumentException("Duplicate Lens provider id: " + id);
        }
        if (!PROVIDERS.contains(provider)) {
            PROVIDERS.add(provider);
            sort();
            EchoLens.LOGGER.debug("Registered Lens provider {} ({})", id, provider.getClass().getName());
        }
    }

    public static List<LensInfoProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    public static int count() {
        return PROVIDERS.size();
    }

    public static boolean hasProvider(Identifier id) {
        return PROVIDERS_BY_ID.containsKey(id);
    }

    public static void clearForTests() {
        PROVIDERS_BY_ID.clear();
        PROVIDERS.clear();
    }

    public static void withClearedForTests(Runnable body) {
        Map<Identifier, LensInfoProvider> ids = Map.copyOf(PROVIDERS_BY_ID);
        List<LensInfoProvider> providers = List.copyOf(PROVIDERS);
        PROVIDERS_BY_ID.clear();
        PROVIDERS.clear();
        try {
            body.run();
        } finally {
            PROVIDERS_BY_ID.clear();
            PROVIDERS_BY_ID.putAll(ids);
            PROVIDERS.clear();
            PROVIDERS.addAll(providers);
        }
    }

    private static void sort() {
        List<LensInfoProvider> sorted = new ArrayList<>(PROVIDERS);
        sorted.sort(Comparator.comparingInt(LensInfoProvider::priority)
                .thenComparing(provider -> provider.id().toString()));
        PROVIDERS.clear();
        PROVIDERS.addAll(sorted);
    }
}
