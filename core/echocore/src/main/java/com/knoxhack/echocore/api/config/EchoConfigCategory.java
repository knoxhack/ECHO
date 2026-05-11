package com.knoxhack.echocore.api.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EchoConfigCategory(
        String id,
        String title,
        List<EchoConfigEntry> entries) {
    public EchoConfigCategory {
        id = EchoConfigEntry.requireId(id, "config category");
        title = title == null || title.isBlank() ? id : title.strip();
        entries = List.copyOf(entries == null
                ? List.of()
                : entries.stream().filter(entry -> entry != null).toList());
        Map<String, EchoConfigEntry> seen = new LinkedHashMap<>();
        for (EchoConfigEntry entry : entries) {
            EchoConfigEntry previous = seen.putIfAbsent(entry.id(), entry);
            if (previous != null && previous != entry) {
                throw new IllegalArgumentException("Duplicate config entry id in category " + id + ": " + entry.id());
            }
        }
    }

    public EchoConfigCategorySnapshot snapshot(String moduleId, EchoConfigSide side) {
        List<EchoConfigEntrySnapshot> filtered = entries.stream()
                .filter(entry -> side == null || entry.side() == side)
                .map(entry -> entry.snapshot(moduleId, id))
                .toList();
        return new EchoConfigCategorySnapshot(id, title, filtered);
    }
}
