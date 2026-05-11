package com.knoxhack.echocore.api.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record EchoConfigModule(
        String moduleId,
        String displayName,
        List<EchoConfigCategory> categories) {
    public EchoConfigModule {
        moduleId = EchoConfigEntry.requireId(moduleId, "config module");
        displayName = displayName == null || displayName.isBlank() ? moduleId : displayName.strip();
        categories = List.copyOf(categories == null
                ? List.of()
                : categories.stream().filter(category -> category != null).toList());
        Map<String, EchoConfigCategory> seenCategories = new LinkedHashMap<>();
        Map<String, EchoConfigEntry> seenEntries = new LinkedHashMap<>();
        for (EchoConfigCategory category : categories) {
            EchoConfigCategory previous = seenCategories.putIfAbsent(category.id(), category);
            if (previous != null && previous != category) {
                throw new IllegalArgumentException("Duplicate config category id in module " + moduleId + ": " + category.id());
            }
            for (EchoConfigEntry entry : category.entries()) {
                EchoConfigEntry previousEntry = seenEntries.putIfAbsent(entry.id(), entry);
                if (previousEntry != null && previousEntry != entry) {
                    throw new IllegalArgumentException("Duplicate config entry id in module " + moduleId + ": " + entry.id());
                }
            }
        }
    }

    public EchoConfigModuleSnapshot snapshot(EchoConfigSide side) {
        List<EchoConfigCategorySnapshot> filtered = categories.stream()
                .map(category -> category.snapshot(moduleId, side))
                .filter(category -> !category.entries().isEmpty())
                .toList();
        return new EchoConfigModuleSnapshot(moduleId, displayName, filtered);
    }

    public Optional<EchoConfigEntry> entry(String entryId, EchoConfigSide side) {
        if (entryId == null || entryId.isBlank()) {
            return Optional.empty();
        }
        String id = entryId.strip().toLowerCase(java.util.Locale.ROOT);
        return categories.stream()
                .flatMap(category -> category.entries().stream())
                .filter(entry -> entry.id().equals(id) && (side == null || entry.side() == side))
                .findFirst();
    }
}
