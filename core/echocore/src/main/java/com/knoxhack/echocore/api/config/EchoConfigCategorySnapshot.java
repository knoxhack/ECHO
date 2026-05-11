package com.knoxhack.echocore.api.config;

import java.util.List;

public record EchoConfigCategorySnapshot(
        String categoryId,
        String title,
        List<EchoConfigEntrySnapshot> entries) {
    public EchoConfigCategorySnapshot {
        categoryId = categoryId == null ? "" : categoryId.strip().toLowerCase(java.util.Locale.ROOT);
        title = title == null || title.isBlank() ? categoryId : title.strip();
        entries = List.copyOf(entries == null
                ? List.of()
                : entries.stream().filter(entry -> entry != null).toList());
    }
}
