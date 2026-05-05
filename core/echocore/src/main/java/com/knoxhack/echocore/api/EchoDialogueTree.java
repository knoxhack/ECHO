package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Objects;

/**
 * Portable dialogue copy that can be consumed by NPC screens or terminal views.
 */
public record EchoDialogueTree(String greeting, List<String> topics, String farewell) {
    public static final EchoDialogueTree EMPTY = new EchoDialogueTree("", List.of(), "");

    public EchoDialogueTree {
        greeting = clean(greeting);
        List<String> safeTopics = topics == null ? List.of() : topics;
        topics = List.copyOf(safeTopics.stream()
                .map(EchoDialogueTree::clean)
                .filter(topic -> !topic.isBlank())
                .toList());
        farewell = clean(farewell);
    }

    private static String clean(String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }
}
