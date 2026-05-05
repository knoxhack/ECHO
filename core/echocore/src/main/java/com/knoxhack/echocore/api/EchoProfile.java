package com.knoxhack.echocore.api;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record EchoProfile(
        String callsign,
        EchoDifficultyProfile difficulty,
        String nexusPath,
        Set<String> completedArcs,
        Set<String> discoveredRecords,
        Set<String> dismissedTutorials) {
    public EchoProfile {
        callsign = callsign == null || callsign.isBlank() ? "ECHO Operator" : callsign;
        difficulty = difficulty == null ? EchoDifficultyProfile.GUIDED : difficulty;
        nexusPath = clean(nexusPath);
        completedArcs = cleanSet(completedArcs);
        discoveredRecords = cleanSet(discoveredRecords);
        dismissedTutorials = cleanSet(dismissedTutorials);
    }

    public static EchoProfile empty() {
        return new EchoProfile("ECHO Operator", EchoDifficultyProfile.GUIDED, "", Set.of(), Set.of(), Set.of());
    }

    public EchoProfile withCallsign(String value) {
        return new EchoProfile(value, difficulty, nexusPath, completedArcs, discoveredRecords, dismissedTutorials);
    }

    public EchoProfile withDifficulty(EchoDifficultyProfile value) {
        return new EchoProfile(callsign, value, nexusPath, completedArcs, discoveredRecords, dismissedTutorials);
    }

    public EchoProfile withNexusPath(String value) {
        return new EchoProfile(callsign, difficulty, value, completedArcs, discoveredRecords, dismissedTutorials);
    }

    public EchoProfile completeArc(String arcId) {
        return new EchoProfile(callsign, difficulty, nexusPath, with(completedArcs, arcId), discoveredRecords, dismissedTutorials);
    }

    public EchoProfile discoverRecord(String recordId) {
        return new EchoProfile(callsign, difficulty, nexusPath, completedArcs, with(discoveredRecords, recordId), dismissedTutorials);
    }

    public EchoProfile dismissTutorial(String tutorialId) {
        return new EchoProfile(callsign, difficulty, nexusPath, completedArcs, discoveredRecords, with(dismissedTutorials, tutorialId));
    }

    public boolean hasNexusPath() {
        return !nexusPath.isBlank();
    }

    public boolean hasCompletedArc(String arcId) {
        return completedArcs.contains(clean(arcId));
    }

    public boolean hasDiscoveredRecord(String recordId) {
        return discoveredRecords.contains(clean(recordId));
    }

    public boolean hasDismissedTutorial(String tutorialId) {
        return dismissedTutorials.contains(clean(tutorialId));
    }

    private static Set<String> with(Set<String> values, String value) {
        LinkedHashSet<String> next = new LinkedHashSet<>(cleanSet(values));
        String clean = clean(value);
        if (!clean.isBlank()) {
            next.add(clean);
        }
        return Collections.unmodifiableSet(next);
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

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
