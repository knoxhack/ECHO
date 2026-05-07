package com.knoxhack.echoterminal.api;

import java.util.Locale;
import java.util.List;

/**
 * Top-level command areas for the terminal shell.
 */
public enum TerminalNavigationSection {
    TERMINAL("Terminal", 0),
    CORE("Core", 100),
    CHAPTERS("Chapters", 200);

    private final String label;
    private final int order;

    TerminalNavigationSection(String label, int order) {
        this.label = label;
        this.order = order;
    }

    public String key() {
        return name();
    }

    public String label() {
        return label;
    }

    public int order() {
        return order;
    }

    public static List<TerminalNavigationSection> storyFirstOrder() {
        return List.of(CHAPTERS, TERMINAL, CORE);
    }

    public static TerminalNavigationSection fromKey(String key) {
        String normalized = key == null ? "" : key.strip().toUpperCase(Locale.ROOT);
        for (TerminalNavigationSection section : values()) {
            if (section.name().equals(normalized)) {
                return section;
            }
        }
        return CHAPTERS;
    }
}
