package com.knoxhack.echocore.api;

import com.knoxhack.echocore.EchoCore;
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
        String chapterId = safeId(chapter, "register");
        if (chapterId == null || chapterId.isBlank()) {
            EchoCore.LOGGER.warn("ECHO addon chapter {} did not expose a valid id; ignoring chapter.",
                    providerName(chapter));
            return;
        }
        String id = chapterId.toLowerCase(Locale.ROOT);
        if (!id.equals(chapterId)) {
            throw new IllegalArgumentException("ECHO addon chapter id must be lowercase: " + chapterId);
        }
        CHAPTERS.put(id, chapter);
    }

    public static boolean isRegistered(String id) {
        return id != null && CHAPTERS.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public static List<EchoAddonChapter> chapters() {
        List<EchoAddonChapter> chapters = new ArrayList<>(CHAPTERS.values());
        chapters.removeIf(chapter -> safeId(chapter, "list") == null);
        chapters.sort(Comparator.comparing(chapter -> {
            String id = safeId(chapter, "sort");
            return id == null ? "" : id;
        }));
        return List.copyOf(chapters);
    }

    public static void clearForTests() {
        CHAPTERS.clear();
    }

    private static String safeId(EchoAddonChapter chapter, String surface) {
        if (chapter == null) {
            return null;
        }
        try {
            String id = chapter.id();
            return id == null || id.isBlank() ? null : id;
        } catch (RuntimeException exception) {
            EchoCore.LOGGER.warn("ECHO addon chapter {} failed during {}; ignoring chapter output.",
                    providerName(chapter), surface, exception);
            return null;
        }
    }

    private static String providerName(Object provider) {
        return provider == null ? "<null>" : provider.getClass().getName();
    }
}
