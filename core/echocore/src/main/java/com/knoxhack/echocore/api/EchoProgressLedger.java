package com.knoxhack.echocore.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record EchoProgressLedger(
        Set<String> milestones,
        Map<String, String> flags,
        Set<String> activeObjectives) {
    public EchoProgressLedger {
        milestones = cleanMilestoneSet(milestones);
        flags = cleanMap(flags);
        activeObjectives = cleanSet(activeObjectives);
    }

    public static EchoProgressLedger empty() {
        return new EchoProgressLedger(Set.of(), Map.of(), Set.of());
    }

    public EchoProgressLedger withMilestone(String milestoneId) {
        LinkedHashSet<String> next = new LinkedHashSet<>(milestones);
        String clean = EchoHandoffs.canonicalMilestone(milestoneId);
        if (!clean.isBlank()) {
            next.add(clean);
        }
        return new EchoProgressLedger(next, flags, activeObjectives);
    }

    public EchoProgressLedger withFlag(String key, String value) {
        LinkedHashMap<String, String> next = new LinkedHashMap<>(flags);
        String cleanKey = clean(key);
        if (!cleanKey.isBlank()) {
            next.put(cleanKey, clean(value));
        }
        return new EchoProgressLedger(milestones, next, activeObjectives);
    }

    public EchoProgressLedger withActiveObjective(String objectiveId) {
        LinkedHashSet<String> next = new LinkedHashSet<>(activeObjectives);
        String clean = clean(objectiveId);
        if (!clean.isBlank()) {
            next.add(clean);
        }
        return new EchoProgressLedger(milestones, flags, next);
    }

    public boolean hasMilestone(String milestoneId) {
        String requested = EchoHandoffs.canonicalMilestone(milestoneId);
        if (requested.isBlank()) {
            return false;
        }
        if (milestones.contains(requested)) {
            return true;
        }
        for (String milestone : milestones) {
            if (EchoHandoffs.matches(milestone, requested)) {
                return true;
            }
        }
        return false;
    }

    public String flag(String key) {
        return flags.getOrDefault(clean(key), "");
    }

    private static Set<String> cleanMilestoneSet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalizedValues = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = EchoHandoffs.canonicalMilestone(value);
            if (!normalized.isBlank()) {
                normalizedValues.add(normalized);
            }
        }
        LinkedHashSet<String> sorted = new LinkedHashSet<>(normalizedValues.stream().sorted().toList());
        return Collections.unmodifiableSet(sorted);
    }

    private static Set<String> cleanSet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalizedValues = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = clean(value);
            if (!normalized.isBlank()) {
                normalizedValues.add(normalized);
            }
        }
        LinkedHashSet<String> sorted = new LinkedHashSet<>(normalizedValues.stream().sorted().toList());
        return Collections.unmodifiableSet(sorted);
    }

    private static Map<String, String> cleanMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> clean = new LinkedHashMap<>();
        values.entrySet().stream()
                .sorted(Comparator.comparing(entry -> clean(entry.getKey())))
                .forEach(entry -> {
            String key = clean(entry.getKey());
            if (!key.isBlank()) {
                clean.put(key, clean(entry.getValue()));
            }
        });
        return Collections.unmodifiableMap(clean);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
