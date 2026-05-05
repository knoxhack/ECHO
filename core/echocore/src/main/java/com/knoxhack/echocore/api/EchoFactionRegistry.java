package com.knoxhack.echocore.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.resources.Identifier;

/**
 * Process-wide registry for addon-provided faction definitions.
 */
public final class EchoFactionRegistry {
    private static final Map<Identifier, EchoFactionDefinition> DEFINITIONS = new LinkedHashMap<>();

    private EchoFactionRegistry() {
    }

    public static synchronized EchoFactionDefinition register(EchoFactionDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        EchoFactionDefinition existing = DEFINITIONS.get(definition.id());
        if (existing != null) {
            if (!existing.equals(definition)) {
                throw new IllegalStateException("Faction already registered with different data: " + definition.id());
            }
            return existing;
        }
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static synchronized Optional<EchoFactionDefinition> definition(Identifier id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static synchronized Optional<EchoFactionDefinition> definition(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            return definition(Identifier.parse(id));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public static synchronized List<EchoFactionDefinition> definitions() {
        return DEFINITIONS.values().stream()
                .sorted(Comparator.comparing(EchoFactionDefinition::modId)
                        .thenComparing(EchoFactionDefinition::displayName))
                .toList();
    }

    public static synchronized List<EchoFactionDefinition> byModId(String modId) {
        String namespace = modId == null ? "" : modId.trim();
        List<EchoFactionDefinition> matches = new ArrayList<>();
        for (EchoFactionDefinition definition : DEFINITIONS.values()) {
            if (definition.modId().equals(namespace)) {
                matches.add(definition);
            }
        }
        matches.sort(Comparator.comparing(EchoFactionDefinition::displayName));
        return List.copyOf(matches);
    }

    public static synchronized void clearForTests() {
        DEFINITIONS.clear();
    }

    public static synchronized void withClearedForTests(Runnable runnable) {
        Map<Identifier, EchoFactionDefinition> snapshot = new LinkedHashMap<>(DEFINITIONS);
        try {
            DEFINITIONS.clear();
            runnable.run();
        } finally {
            DEFINITIONS.clear();
            DEFINITIONS.putAll(snapshot);
        }
    }
}
