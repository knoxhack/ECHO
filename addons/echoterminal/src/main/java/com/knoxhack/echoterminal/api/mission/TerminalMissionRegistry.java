package com.knoxhack.echoterminal.api.mission;

import com.knoxhack.echoterminal.api.TerminalApiIds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;

public final class TerminalMissionRegistry {
    private static final Map<Identifier, TerminalMissionProvider> PROVIDERS = new ConcurrentHashMap<>();
    private static volatile List<TerminalMissionProvider> sortedProviders = List.of();

    private TerminalMissionRegistry() {
    }

    public static void register(TerminalMissionProvider provider) {
        if (provider == null || provider.chapter() == null) {
            throw new IllegalArgumentException("Terminal mission provider and chapter are required.");
        }
        Identifier id = TerminalApiIds.requireLowercase(provider.chapter().id(), "Terminal mission provider");
        TerminalMissionProvider previous = PROVIDERS.putIfAbsent(id, provider);
        if (previous != null && previous != provider) {
            throw new IllegalArgumentException("Duplicate terminal mission provider id: " + id);
        }
        ensureSorted();
    }

    public static Optional<TerminalMissionProvider> provider(Identifier chapterId) {
        return Optional.ofNullable(PROVIDERS.get(chapterId));
    }

    public static List<TerminalMissionProvider> providers() {
        return sortedProviders;
    }

    public static void ensureSorted() {
        List<TerminalMissionProvider> providers = new ArrayList<>(PROVIDERS.values());
        providers.sort(Comparator
                .comparingInt((TerminalMissionProvider provider) -> provider.chapter().order())
                .thenComparing(provider -> provider.chapter().id().toString()));
        sortedProviders = List.copyOf(providers);
    }

    public static void clearForTests() {
        PROVIDERS.clear();
        sortedProviders = List.of();
    }

    public static void withClearedForTests(Runnable runnable) {
        Map<Identifier, TerminalMissionProvider> snapshot = Map.copyOf(PROVIDERS);
        List<TerminalMissionProvider> sortedSnapshot = sortedProviders;
        PROVIDERS.clear();
        sortedProviders = List.of();
        try {
            runnable.run();
        } finally {
            PROVIDERS.clear();
            PROVIDERS.putAll(snapshot);
            sortedProviders = sortedSnapshot;
        }
    }
}
