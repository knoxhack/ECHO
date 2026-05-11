package com.knoxhack.echocore.api.config;

import java.util.List;

public record EchoConfigModuleSnapshot(
        String moduleId,
        String displayName,
        List<EchoConfigCategorySnapshot> categories) {
    public EchoConfigModuleSnapshot {
        moduleId = moduleId == null ? "" : moduleId.strip().toLowerCase(java.util.Locale.ROOT);
        displayName = displayName == null || displayName.isBlank() ? moduleId : displayName.strip();
        categories = List.copyOf(categories == null
                ? List.of()
                : categories.stream().filter(category -> category != null).toList());
    }

    public boolean hasEntries() {
        return categories.stream().anyMatch(category -> !category.entries().isEmpty());
    }
}
