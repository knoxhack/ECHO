package com.knoxhack.echoterminal.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;

public final class TerminalArchiveRegistry {
    private static final Map<Identifier, TerminalArchiveEntry> ENTRIES = new ConcurrentHashMap<>();

    private TerminalArchiveRegistry() {
    }

    public static void register(TerminalArchiveEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Terminal archive entry is required.");
        }
        ENTRIES.put(entry.id(), entry);
    }

    public static List<TerminalArchiveEntry> entries() {
        List<TerminalArchiveEntry> entries = new ArrayList<>(ENTRIES.values());
        entries.sort(Comparator.comparing(TerminalArchiveEntry::group).thenComparing(TerminalArchiveEntry::title));
        return List.copyOf(entries);
    }
}
