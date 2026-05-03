package com.knoxhack.echocore.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Public integration point for ECHO addon chapters.
 */
public final class EchoAddonRegistry {
    private static final Map<String, EchoAddonChapter> CHAPTERS = new ConcurrentHashMap<>();

    private EchoAddonRegistry() {
    }

    public static void register(EchoAddonChapter chapter) {
        if (chapter == null || chapter.id() == null || chapter.id().isBlank()) {
            throw new IllegalArgumentException("ECHO addon chapter id is required.");
        }
        String id = chapter.id().toLowerCase(Locale.ROOT);
        if (!id.equals(chapter.id())) {
            throw new IllegalArgumentException("ECHO addon chapter id must be lowercase: " + chapter.id());
        }
        CHAPTERS.put(id, chapter);
    }

    public static boolean isRegistered(String id) {
        return id != null && CHAPTERS.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public static List<EchoAddonChapter> chapters() {
        List<EchoAddonChapter> chapters = new ArrayList<>(CHAPTERS.values());
        chapters.sort(Comparator.comparing(EchoAddonChapter::id));
        return List.copyOf(chapters);
    }

    public static void clearForTests() {
        CHAPTERS.clear();
    }
}
