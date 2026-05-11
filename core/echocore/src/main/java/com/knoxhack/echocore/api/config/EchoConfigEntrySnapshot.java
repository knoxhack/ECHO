package com.knoxhack.echocore.api.config;

import java.util.List;

public record EchoConfigEntrySnapshot(
        String moduleId,
        String categoryId,
        String entryId,
        String label,
        String description,
        EchoConfigSide side,
        EchoConfigValueKind kind,
        String value,
        String defaultValue,
        String minValue,
        String maxValue,
        List<String> options,
        boolean editable,
        boolean restartRequired,
        boolean newWorldOnly,
        String status) {
    public EchoConfigEntrySnapshot {
        moduleId = cleanId(moduleId);
        categoryId = cleanId(categoryId);
        entryId = cleanId(entryId);
        label = clean(label, entryId);
        description = clean(description, "");
        side = side == null ? EchoConfigSide.COMMON : side;
        kind = kind == null ? EchoConfigValueKind.STRING : kind;
        value = clean(value, "");
        defaultValue = clean(defaultValue, "");
        minValue = clean(minValue, "");
        maxValue = clean(maxValue, "");
        options = List.copyOf(options == null
                ? List.of()
                : options.stream().filter(option -> option != null && !option.isBlank()).map(String::strip).toList());
        status = clean(status, "");
    }

    private static String cleanId(String value) {
        return value == null ? "" : value.strip().toLowerCase(java.util.Locale.ROOT);
    }

    private static String clean(String value, String fallback) {
        String cleaned = value == null ? "" : value.strip();
        return cleaned.isBlank() ? fallback : cleaned;
    }
}
