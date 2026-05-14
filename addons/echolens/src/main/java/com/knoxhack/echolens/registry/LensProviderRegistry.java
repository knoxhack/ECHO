package com.knoxhack.echolens.registry;

import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.LensInfoProvider;
import com.knoxhack.echolens.api.LensProviderDiagnostic;
import com.knoxhack.echolens.api.ServerLensProvider;
import com.knoxhack.echolens.config.LensConfig;
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

    public static void registerAll(Iterable<? extends LensInfoProvider> providers) {
        if (providers == null) {
            throw new IllegalArgumentException("Lens providers are required.");
        }
        for (LensInfoProvider provider : providers) {
            register(provider);
        }
    }

    public static List<LensInfoProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    public static List<ServerLensProvider> serverProviders() {
        return PROVIDERS.stream()
                .filter(ServerLensProvider.class::isInstance)
                .map(ServerLensProvider.class::cast)
                .toList();
    }

    public static List<LensProviderDiagnostic> diagnostics() {
        return PROVIDERS.stream()
                .map(provider -> new LensProviderDiagnostic(
                        provider.id(),
                        provider.getClass().getName(),
                        provider.priority(),
                        provider.category(),
                        true,
                        categoryEnabled(provider.category())))
                .toList();
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

    private static boolean categoryEnabled(com.knoxhack.echolens.api.LensDataCategory category) {
        return switch (category == null ? com.knoxhack.echolens.api.LensDataCategory.IDENTITY : category) {
            case IDENTITY -> LensConfig.bool(LensConfig.SHOW_IDENTITY, true);
            case BLOCK -> LensConfig.bool(LensConfig.SHOW_BLOCK, true);
            case ENTITY -> LensConfig.bool(LensConfig.SHOW_ENTITY, true);
            case FLUID -> LensConfig.bool(LensConfig.SHOW_FLUID, true);
            case MACHINE -> LensConfig.bool(LensConfig.SHOW_MACHINE, true);
            case INVENTORY -> LensConfig.bool(LensConfig.SHOW_INVENTORY, true);
            case INTEGRATION -> LensConfig.bool(LensConfig.SHOW_INTEGRATION, true);
            case HINTS -> LensConfig.bool(LensConfig.BEGINNER_HINTS, true);
            case ACTIONS -> LensConfig.bool(LensConfig.SHOW_ACTIONS, true);
        };
    }
}
