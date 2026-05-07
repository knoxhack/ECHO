package com.knoxhack.echoterminal.mission;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionPresentation;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

public final class MainSurvivalQuestProvider implements TerminalMissionProvider {
    public static final MainSurvivalQuestProvider INSTANCE = new MainSurvivalQuestProvider();
    public static final Identifier CHAPTER_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "main_survival_route");
    public static final Identifier TAB_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "main_survival_route");
    private static final int ACCENT = 0xFF92F7A6;
    private static final int CACHE_REFRESH_TICKS = 40;
    private static final int MAX_ROUTE_RECORDS = 250;
    private static final Identifier OVERFLOW_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "main_survival_route_overflow");
    private static final UUID NULL_PLAYER_ID = new UUID(0L, 0L);
    private final Map<RouteCacheKey, RouteSnapshot> routeCache = new HashMap<>();

    private MainSurvivalQuestProvider() {
    }

    @Override
    public TerminalMissionChapter chapter() {
        return new TerminalMissionChapter(
                CHAPTER_ID,
                "Survival Route",
                "One authored field route for installed ECHO chapter progress and remaining registered addon mission signals.",
                45,
                ACCENT,
                true);
    }

    @Override
    public List<TerminalMissionDefinition> missions(Player player) {
        return routeSnapshot(player).definitions();
    }

    @Override
    public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
        if (OVERFLOW_ID.equals(missionId)) {
            RouteSnapshot snapshot = routeSnapshot(player);
            return overflowSnapshot(snapshot.overflowCount());
        }
        SourceRecord record = routeSnapshot(player).sourceById().get(missionId);
        if (record == null) {
            return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                    "MISSING", "Survival route signal not found.",
                    "Reopen the terminal after installed ECHO chapters finish registering.", List.of());
        }
        TerminalMissionSnapshot child = safeSnapshot(record.provider(), player, record.definition());
        return new TerminalMissionSnapshot(
                missionId,
                child.status(),
                child.progress(),
                child.statusLabel(),
                child.unlockReason(),
                guideHint(record, child),
                List.of());
    }

    @Override
    public TerminalMissionPresentation presentation(
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        if (definition != null && OVERFLOW_ID.equals(definition.id())) {
            return new TerminalMissionPresentation(
                    "More Signals Available",
                    "Additional installed mission signals are hidden to keep the Survival Route responsive.",
                    "Open the owning chapter tabs for the remaining detailed records.",
                    "Performance guard",
                    "reference",
                    List.of("Survival Route", "Overflow", "Performance Guard"),
                    null);
        }
        SourceRecord record = routeSnapshot(player).sourceById().get(definition == null ? null : definition.id());
        if (record == null) {
            return TerminalMissionPresentation.fallback(definition, snapshot);
        }
        TerminalMissionSnapshot childSnapshot = usableSnapshot(snapshot, record);
        TerminalMissionPresentation child = safePresentation(record.provider(), player, record.definition(), childSnapshot);
        List<String> tags = new ArrayList<>(child.tags());
        tags.add("Source: " + record.chapter().title());
        return new TerminalMissionPresentation(
                child.shortTitle(),
                child.objectiveSummary(),
                guideHint(record, childSnapshot),
                "Source: " + record.chapter().title(),
                child.statusTone(),
                tags,
                child.relatedIntelKey());
    }

    @Override
    public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        if (definition != null && OVERFLOW_ID.equals(definition.id())) {
            return TerminalMissionRole.REFERENCE;
        }
        SourceRecord record = routeSnapshot(player).sourceById().get(definition == null ? null : definition.id());
        return record == null
                ? TerminalMissionRole.REFERENCE
                : safeRole(record.provider(), player, record.definition(), usableSnapshot(snapshot, record));
    }

    public static int maxRouteRecordsForTests() {
        return MAX_ROUTE_RECORDS;
    }

    public void clearCacheForTests() {
        routeCache.clear();
    }

    private static TerminalMissionDefinition definition(SourceRecord record) {
        TerminalMissionDefinition child = record.definition();
        RoutePhase phase = record.phase();
        return new TerminalMissionDefinition(
                child.id(),
                CHAPTER_ID,
                phase.id(),
                phase.title(),
                phase.order(),
                record.order(),
                child.title(),
                child.briefing(),
                child.fieldGuide(),
                record.chapter().title(),
                child.difficulty(),
                child.icon(),
                child.prerequisites(),
                child.requirements(),
                child.rewards());
    }

    private RouteSnapshot routeSnapshot(Player player) {
        RouteCacheKey key = cacheKey(player);
        RouteSnapshot cached = routeCache.get(key);
        if (cached != null) {
            return cached;
        }
        RouteSnapshot snapshot = buildRouteSnapshot(player);
        routeCache.clear();
        routeCache.put(key, snapshot);
        return snapshot;
    }

    private RouteCacheKey cacheKey(Player player) {
        UUID playerId = player == null ? NULL_PLAYER_ID : player.getUUID();
        long gameTime = player == null || player.level() == null ? 0L : player.level().getGameTime();
        long refreshBucket = Math.max(0L, gameTime / CACHE_REFRESH_TICKS);
        return new RouteCacheKey(playerId, refreshBucket, providerFingerprint());
    }

    private static String providerFingerprint() {
        StringBuilder builder = new StringBuilder();
        for (TerminalMissionProvider provider : TerminalMissionRegistry.providers()) {
            if (provider != null && provider != INSTANCE) {
                builder.append(provider.getClass().getName())
                        .append('@')
                        .append(System.identityHashCode(provider))
                        .append(';');
            }
        }
        return builder.toString();
    }

    private static RouteSnapshot buildRouteSnapshot(Player player) {
        List<SourceRecord> records = records(player);
        int overflowCount = Math.max(0, records.size() - MAX_ROUTE_RECORDS);
        if (overflowCount > 0) {
            records = List.copyOf(records.subList(0, MAX_ROUTE_RECORDS));
        }
        Map<Identifier, SourceRecord> sourceById = new HashMap<>();
        List<TerminalMissionDefinition> definitions = new ArrayList<>();
        for (SourceRecord record : records) {
            sourceById.put(record.definition().id(), record);
            definitions.add(definition(record));
        }
        if (overflowCount > 0) {
            definitions.add(overflowDefinition(overflowCount));
        }
        return new RouteSnapshot(List.copyOf(records), Map.copyOf(sourceById), List.copyOf(definitions), overflowCount);
    }

    private static List<SourceRecord> records(Player player) {
        List<SourceCandidate> candidates = candidates(player);
        List<SourceCandidate> selected = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (SourceCandidate candidate : candidates) {
            RoutePhase phase = authoredPhase(candidate);
            if (phase != null && seen.add(candidate.key())) {
                selected.add(candidate.withPhase(phase));
            }
        }
        for (SourceCandidate candidate : candidates) {
            if (!seen.contains(candidate.key()) && visibleInOtherSignals(candidate.role())
                    && seen.add(candidate.key())) {
                selected.add(candidate.withPhase(RoutePhase.PHASE_09));
            }
        }
        Map<RoutePhase, Integer> phaseCounts = new EnumMap<>(RoutePhase.class);
        List<SourceRecord> records = new ArrayList<>();
        for (SourceCandidate candidate : selected) {
            int order = phaseCounts.merge(candidate.phase(), 1, Integer::sum);
            records.add(new SourceRecord(candidate.provider(), candidate.chapter(), candidate.definition(),
                    candidate.snapshot(), candidate.presentation(), candidate.role(), candidate.phase(), order));
        }
        return List.copyOf(records);
    }

    private static TerminalMissionDefinition overflowDefinition(int overflowCount) {
        return new TerminalMissionDefinition(
                OVERFLOW_ID,
                CHAPTER_ID,
                RoutePhase.PHASE_09.id(),
                RoutePhase.PHASE_09.title(),
                RoutePhase.PHASE_09.order(),
                MAX_ROUTE_RECORDS + 1,
                "More Signals Available",
                overflowCount + " additional mission records are available in installed chapter tabs.",
                "The aggregate Survival Route is showing priority records to keep the terminal responsive.",
                "Performance Guard",
                "Reference",
                ItemStack.EMPTY,
                List.of(),
                List.of(),
                List.of());
    }

    private static TerminalMissionSnapshot overflowSnapshot(int overflowCount) {
        return new TerminalMissionSnapshot(
                OVERFLOW_ID,
                TerminalMissionStatus.VIEW_ONLY,
                0.0F,
                "BOUNDED",
                overflowCount + " additional installed mission records are hidden from this aggregate view.",
                "Open installed chapter tabs for full mission lists.",
                List.of());
    }

    private static List<SourceCandidate> candidates(Player player) {
        List<SourceCandidate> candidates = new ArrayList<>();
        for (TerminalMissionProvider provider : TerminalMissionRegistry.providers()) {
            if (provider == null || provider == INSTANCE) {
                continue;
            }
            TerminalMissionChapter chapter = safeChapter(provider);
            if (chapter == null || CHAPTER_ID.equals(chapter.id())
                    || VanillaJourneyProvider.CHAPTER_ID.equals(chapter.id())) {
                continue;
            }
            for (TerminalMissionDefinition definition : safeMissions(provider, player)) {
                if (definition == null) {
                    continue;
                }
                TerminalMissionSnapshot snapshot = lightweightSnapshot(definition);
                TerminalMissionRole role = TerminalMissionRole.fallback(definition, snapshot);
                TerminalMissionPresentation presentation = TerminalMissionPresentation.fallback(definition, snapshot);
                candidates.add(new SourceCandidate(provider, chapter, definition, snapshot, presentation, role, null));
            }
        }
        return candidates;
    }

    private static TerminalMissionSnapshot lightweightSnapshot(TerminalMissionDefinition definition) {
        return new TerminalMissionSnapshot(definition.id(), TerminalMissionStatus.LOCKED, 0.0F,
                "ROUTE", "Open this route to evaluate live progress.", "Open the owning chapter for actions.",
                List.of());
    }

    private static RoutePhase authoredPhase(SourceCandidate candidate) {
        TerminalMissionChapter chapter = candidate.chapter();
        TerminalMissionDefinition definition = candidate.definition();
        String chapterId = chapter.id().toString();
        String namespace = definition.id().getNamespace();
        String path = definition.id().getPath().toLowerCase(Locale.ROOT);
        String phase = definition.phaseTitle().toLowerCase(Locale.ROOT);
        String title = definition.title().toLowerCase(Locale.ROOT);

        if (chapterId.contains("echoashfallprotocol:ashfall_protocol")
                || "echoashfallprotocol".equals(namespace)) {
            String signal = path + " " + phase + " " + title;
            if (containsAny(signal, "aftermath", "seal", "survey", "faction", "mastery")) {
                return RoutePhase.PHASE_09;
            }
            if (containsAny(signal, "nexus", "ending", "guardian", "echo-0", "echo_zero", "core")) {
                return RoutePhase.PHASE_08;
            }
            if (containsAny(signal, "biohazard", "deep", "radiation", "cryo", "lab", "vault", "boss")) {
                return RoutePhase.PHASE_07;
            }
            if (containsAny(signal, "grid", "relay", "station", "industrial", "infrastructure")
                    || definition.phaseOrder() == 6) {
                return RoutePhase.PHASE_06;
            }
            return switch (definition.phaseOrder()) {
                case 0 -> RoutePhase.PHASE_00;
                case 1 -> RoutePhase.PHASE_01;
                case 2 -> RoutePhase.PHASE_02;
                case 3 -> RoutePhase.PHASE_03;
                default -> RoutePhase.PHASE_07;
            };
        }
        if (chapterId.contains("echoorbitalremnants") || namespace.equals("echoorbitalremnants")) {
            String signal = path + " " + phase + " " + title;
            if (containsAny(signal, "seal", "survey", "faction", "mastery")) {
                return RoutePhase.PHASE_09;
            }
            if (containsAny(signal, "echo_zero", "echo-0", "final", "core", "guardian")) {
                return RoutePhase.PHASE_08;
            }
            return containsAny(signal, "deep_space", "deep space", "radiation", "cryo", "lab", "vault")
                    ? RoutePhase.PHASE_07
                    : RoutePhase.PHASE_06;
        }
        if (chapterId.contains("echostationfall") || chapterId.endsWith(":stationfall")
                || namespace.equals("echostationfall")) {
            String signal = path + " " + phase + " " + title;
            if (containsAny(signal, "boss", "blackbox", "black box", "ai_core", "ai core", "guardian")) {
                return RoutePhase.PHASE_08;
            }
            if (containsAny(signal, "deep", "radiation", "cryo", "lab", "vault", "reactor")) {
                return RoutePhase.PHASE_07;
            }
            return definition.phaseOrder() <= 0 ? RoutePhase.PHASE_03 : RoutePhase.PHASE_06;
        }
        if (chapterId.contains("echoindustrialnexus") || namespace.equals("echoindustrialnexus")) {
            String signal = path + " " + phase + " " + title;
            if (containsAny(signal, "survived", "radiation", "cryo", "lab", "vault", "boss")) {
                return RoutePhase.PHASE_07;
            }
            return containsAny(signal, "filter", "metal", "grind", "reclaim_power", "power")
                            ? RoutePhase.PHASE_02
                            : RoutePhase.PHASE_06;
        }
        if (chapterId.contains("echonexusprotocol") || namespace.equals("echonexusprotocol")
                || chapterId.contains("echoblackboxprotocol") || namespace.equals("echoblackboxprotocol")) {
            String signal = path + " " + phase + " " + title;
            if (containsAny(signal, "aftermath", "seal", "survey", "faction", "mastery")) {
                return RoutePhase.PHASE_09;
            }
            return containsAny(signal, "ending", "guardian", "echo_zero", "echo-0", "core", "decision")
                            ? RoutePhase.PHASE_08
                            : RoutePhase.PHASE_07;
        }
        return null;
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean visibleInOtherSignals(TerminalMissionRole role) {
        return role == TerminalMissionRole.MAIN || role == TerminalMissionRole.REFERENCE;
    }

    private static String guideHint(SourceRecord record, TerminalMissionSnapshot snapshot) {
        String childHint = snapshot == null ? "" : snapshot.actionHint();
        String source = "Source: " + record.chapter().title()
                + ". Use that chapter tab for rewards and chapter-specific commands.";
        return childHint == null || childHint.isBlank() ? source : childHint + " " + source;
    }

    private static TerminalMissionSnapshot usableSnapshot(TerminalMissionSnapshot snapshot, SourceRecord record) {
        return snapshot != null && record.definition().id().equals(snapshot.missionId()) ? snapshot : record.snapshot();
    }

    private static TerminalMissionChapter safeChapter(TerminalMissionProvider provider) {
        try {
            return provider.chapter();
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.debug("Survival route skipped a provider with failing chapter metadata.", exception);
            return null;
        }
    }

    private static List<TerminalMissionDefinition> safeMissions(TerminalMissionProvider provider, Player player) {
        try {
            List<TerminalMissionDefinition> missions = provider.missions(player);
            return missions == null ? List.of() : missions;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.debug("Survival route skipped a provider with failing mission records.", exception);
            return List.of();
        }
    }

    private static TerminalMissionSnapshot safeSnapshot(
            TerminalMissionProvider provider,
            Player player,
            TerminalMissionDefinition definition) {
        try {
            TerminalMissionSnapshot snapshot = provider.snapshot(player, definition.id());
            return snapshot == null
                    ? new TerminalMissionSnapshot(definition.id(), TerminalMissionStatus.LOCKED, 0.0F,
                            "LOCKED", "Mission provider returned no snapshot.", "Open the owning chapter.", List.of())
                    : snapshot;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.debug("Survival route locked a mission with failing snapshot metadata.", exception);
            return new TerminalMissionSnapshot(definition.id(), TerminalMissionStatus.LOCKED, 0.0F,
                    "LOCKED", "Mission provider snapshot failed.", "Open the owning chapter after the chapter reloads.",
                    List.of());
        }
    }

    private static TerminalMissionPresentation safePresentation(
            TerminalMissionProvider provider,
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionPresentation presentation = provider.presentation(player, definition, snapshot);
            return presentation == null ? TerminalMissionPresentation.fallback(definition, snapshot) : presentation;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.debug("Survival route used fallback presentation for a mission.", exception);
            return TerminalMissionPresentation.fallback(definition, snapshot);
        }
    }

    private static TerminalMissionRole safeRole(
            TerminalMissionProvider provider,
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionRole role = provider.role(player, definition, snapshot);
            return role == null ? TerminalMissionRole.fallback(definition, snapshot) : role;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.debug("Survival route used fallback role for a mission.", exception);
            return TerminalMissionRole.fallback(definition, snapshot);
        }
    }

    private enum RoutePhase {
        PHASE_00("phase_00", "Phase 00", 0),
        PHASE_01("phase_01", "Phase 01", 1),
        PHASE_02("phase_02", "Phase 02", 2),
        PHASE_03("phase_03", "Phase 03", 3),
        PHASE_04("phase_04", "Phase 04", 4),
        PHASE_05("phase_05", "Phase 05", 5),
        PHASE_06("phase_06", "Phase 06", 6),
        PHASE_07("phase_07", "Phase 07", 7),
        PHASE_08("phase_08", "Phase 08", 8),
        PHASE_09("phase_09", "Phase 09", 9);

        private final String id;
        private final String title;
        private final int order;

        RoutePhase(String id, String title, int order) {
            this.id = id;
            this.title = title;
            this.order = order;
        }

        String id() {
            return id;
        }

        String title() {
            return title;
        }

        int order() {
            return order;
        }
    }

    private record SourceCandidate(
            TerminalMissionProvider provider,
            TerminalMissionChapter chapter,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot,
            TerminalMissionPresentation presentation,
            TerminalMissionRole role,
            RoutePhase phase) {
        String key() {
            return chapter.id() + "|" + definition.id();
        }

        SourceCandidate withPhase(RoutePhase phase) {
            return new SourceCandidate(provider, chapter, definition, snapshot, presentation, role, phase);
        }
    }

    private record SourceRecord(
            TerminalMissionProvider provider,
            TerminalMissionChapter chapter,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot,
            TerminalMissionPresentation presentation,
            TerminalMissionRole role,
            RoutePhase phase,
            int order) {
    }

    private record RouteCacheKey(UUID playerId, long refreshBucket, String providerFingerprint) {
    }

    private record RouteSnapshot(
            List<SourceRecord> records,
            Map<Identifier, SourceRecord> sourceById,
            List<TerminalMissionDefinition> definitions,
            int overflowCount) {
    }
}
