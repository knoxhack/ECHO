package com.knoxhack.echocore.api.config;

import com.knoxhack.echocore.EchoCore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class EchoConfigRegistry {
    private static final Map<String, EchoConfigProvider> PROVIDERS = new ConcurrentHashMap<>();
    private static volatile List<EchoConfigProvider> sortedProviders = List.of();

    private EchoConfigRegistry() {
    }

    public static void register(EchoConfigProvider provider) {
        String moduleId = safeModuleId(provider, "register");
        if (moduleId == null || moduleId.isBlank()) {
            EchoCore.LOGGER.warn("ECHO config provider {} did not expose a valid module id; ignoring provider.",
                    providerName(provider));
            return;
        }
        String id = EchoConfigEntry.requireId(moduleId, "config provider module");
        EchoConfigProvider previous = PROVIDERS.putIfAbsent(id, provider);
        if (previous != null && previous != provider) {
            throw new IllegalArgumentException("Duplicate ECHO config provider id: " + id);
        }
        ensureSorted();
    }

    public static List<EchoConfigProvider> providers() {
        return sortedProviders;
    }

    public static List<EchoConfigModule> modules() {
        return sortedProviders.stream()
                .map(EchoConfigRegistry::safeModule)
                .flatMap(Optional::stream)
                .toList();
    }

    public static List<EchoConfigModuleSnapshot> snapshots(EchoConfigSide side) {
        return modules().stream()
                .map(module -> module.snapshot(side))
                .filter(EchoConfigModuleSnapshot::hasEntries)
                .toList();
    }

    public static Optional<EchoConfigModuleSnapshot> snapshot(String moduleId, EchoConfigSide side) {
        return module(moduleId).map(module -> module.snapshot(side));
    }

    public static Optional<EchoConfigModule> module(String moduleId) {
        if (moduleId == null || moduleId.isBlank()) {
            return Optional.empty();
        }
        EchoConfigProvider provider = PROVIDERS.get(moduleId.strip().toLowerCase(Locale.ROOT));
        return safeModule(provider);
    }

    public static EchoConfigApplyResult apply(EchoConfigSide side, String moduleId, String entryId, String value) {
        return module(moduleId)
                .flatMap(module -> module.entry(entryId, side)
                        .map(entry -> entry.apply(module.moduleId(), value)))
                .orElseGet(() -> EchoConfigApplyResult.failure(moduleId, entryId, "Unknown config entry."));
    }

    public static EchoConfigApplyResult reset(EchoConfigSide side, String moduleId, String entryId) {
        return module(moduleId)
                .flatMap(module -> module.entry(entryId, side)
                        .map(entry -> entry.reset(module.moduleId())))
                .orElseGet(() -> EchoConfigApplyResult.failure(moduleId, entryId, "Unknown config entry."));
    }

    public static void ensureSorted() {
        List<EchoConfigProvider> providers = new ArrayList<>(PROVIDERS.values());
        providers.removeIf(provider -> safeModuleId(provider, "sort") == null);
        providers.sort(Comparator.comparing(provider -> {
            String id = safeModuleId(provider, "sort");
            return id == null ? "" : id;
        }));
        sortedProviders = List.copyOf(providers);
    }

    public static void clearForTests() {
        PROVIDERS.clear();
        sortedProviders = List.of();
    }

    public static void withClearedForTests(Runnable runnable) {
        Map<String, EchoConfigProvider> snapshot = Map.copyOf(PROVIDERS);
        List<EchoConfigProvider> sortedSnapshot = sortedProviders;
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

    private static Optional<EchoConfigModule> safeModule(EchoConfigProvider provider) {
        if (provider == null) {
            return Optional.empty();
        }
        try {
            EchoConfigModule module = provider.module();
            return module == null ? Optional.empty() : Optional.of(module);
        } catch (RuntimeException exception) {
            EchoCore.LOGGER.warn("ECHO config provider {} failed while building module snapshot; ignoring provider.",
                    providerName(provider), exception);
            return Optional.empty();
        }
    }

    private static String safeModuleId(EchoConfigProvider provider, String surface) {
        if (provider == null) {
            return null;
        }
        try {
            String id = provider.moduleId();
            return id == null || id.isBlank() ? null : id.strip();
        } catch (RuntimeException exception) {
            EchoCore.LOGGER.warn("ECHO config provider {} failed during {}; ignoring provider output.",
                    providerName(provider), surface, exception);
            return null;
        }
    }

    private static String providerName(Object provider) {
        return provider == null ? "<null>" : provider.getClass().getName();
    }
}
