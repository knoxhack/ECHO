package com.knoxhack.echoterminal.api;

import java.util.Locale;

/**
 * Navigation metadata layered over terminal tab rendering.
 */
public record TerminalNavigationProfile(
        TerminalNavigationSection section,
        String chapterId,
        String chapterTitle,
        String chapterIcon,
        int order) {
    public TerminalNavigationProfile {
        section = section == null ? TerminalNavigationSection.CHAPTERS : section;
        chapterId = clean(chapterId);
        chapterTitle = clean(chapterTitle);
        chapterIcon = clean(chapterIcon).toUpperCase(Locale.ROOT);
    }

    public static TerminalNavigationProfile terminal(int order) {
        return section(TerminalNavigationSection.TERMINAL, order);
    }

    public static TerminalNavigationProfile core(int order) {
        return section(TerminalNavigationSection.CORE, order);
    }

    public static TerminalNavigationProfile chaptersHub(int order) {
        return section(TerminalNavigationSection.CHAPTERS, order);
    }

    public static TerminalNavigationProfile section(TerminalNavigationSection section, int order) {
        return new TerminalNavigationProfile(section, "", "", "", order);
    }

    public static TerminalNavigationProfile chapter(String chapterId, String chapterTitle, String chapterIcon, int order) {
        return new TerminalNavigationProfile(
                TerminalNavigationSection.CHAPTERS, chapterId, chapterTitle, chapterIcon, order);
    }

    public boolean hasChapter() {
        return !chapterId.isBlank();
    }

    private static String clean(String value) {
        return value == null ? "" : value.strip();
    }
}
