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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class MainSurvivalQuestProvider implements TerminalMissionProvider {
    public static final MainSurvivalQuestProvider INSTANCE = new MainSurvivalQuestProvider();
    public static final Identifier CHAPTER_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "main_survival_route");
    public static final Identifier TAB_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "main_survival_route");
    private static final int ACCENT = 0xFF92F7A6;

    private MainSurvivalQuestProvider() {
    }

    @Override
    public TerminalMissionChapter chapter() {
        return new TerminalMissionChapter(
                CHAPTER_ID,
                "Survival Route",
                "One authored field route that mirrors vanilla survival, ECHO chapter progress, and remaining registered mission signals.",
                45,
                ACCENT,
                true);
    }

    @Override
    public List<TerminalMissionDefinition> missions(Player player) {
        return records(player).stream()
                .map(MainSurvivalQuestProvider::definition)
                .toList();
    }

    @Override
    public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
        SourceRecord record = record(player, missionId);
        if (record == null) {
            return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                    "MISSING", "Survival route signal not found.",
                    "Reopen the terminal after installed ECHO chapters finish registering.", List.of());
        }
        TerminalMissionSnapshot child = record.snapshot();
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
        SourceRecord record = record(player, definition == null ? null : definition.id());
        if (record == null) {
            return TerminalMissionPresentation.fallback(definition, snapshot);
        }
        TerminalMissionPresentation child = record.presentation();
        List<String> tags = new ArrayList<>(child.tags());
        tags.add("Source: " + record.chapter().title());
        return new TerminalMissionPresentation(
                child.shortTitle(),
                child.objectiveSummary(),
                guideHint(record, record.snapshot()),
                "Source: " + record.chapter().title(),
                child.statusTone(),
                tags,
                child.relatedIntelKey());
    }

    @Override
    public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        SourceRecord record = record(player, definition == null ? null : definition.id());
        return record == null ? TerminalMissionRole.REFERENCE : record.role();
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

    private static SourceRecord record(Player player, Identifier missionId) {
        if (missionId == null) {
            return null;
        }
        return records(player).stream()
                .filter(record -> missionId.equals(record.definition().id()))
                .findFirst()
                .orElse(null);
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
                selected.add(candidate.withPhase(RoutePhase.OTHER_SIGNALS));
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

    private static List<SourceCandidate> candidates(Player player) {
        List<SourceCandidate> candidates = new ArrayList<>();
        for (TerminalMissionProvider provider : TerminalMissionRegistry.providers()) {
            if (provider == null || provider == INSTANCE) {
                continue;
            }
            TerminalMissionChapter chapter = safeChapter(provider);
            if (chapter == null || CHAPTER_ID.equals(chapter.id())) {
                continue;
            }
            for (TerminalMissionDefinition definition : safeMissions(provider, player)) {
                if (definition == null) {
                    continue;
                }
                TerminalMissionSnapshot snapshot = safeSnapshot(provider, player, definition);
                TerminalMissionRole role = safeRole(provider, player, definition, snapshot);
                TerminalMissionPresentation presentation = safePresentation(provider, player, definition, snapshot);
                candidates.add(new SourceCandidate(provider, chapter, definition, snapshot, presentation, role, null));
            }
        }
        return candidates;
    }

    private static RoutePhase authoredPhase(SourceCandidate candidate) {
        TerminalMissionChapter chapter = candidate.chapter();
        TerminalMissionDefinition definition = candidate.definition();
        String chapterId = chapter.id().toString();
        String namespace = definition.id().getNamespace();
        String path = definition.id().getPath().toLowerCase(Locale.ROOT);
        String phase = definition.phaseTitle().toLowerCase(Locale.ROOT);
        String title = definition.title().toLowerCase(Locale.ROOT);

        if (VanillaJourneyProvider.CHAPTER_ID.equals(chapter.id())) {
            return vanillaPhase(path);
        }
        if (chapterId.contains("echoashfallprotocol:ashfall_protocol")
                || "echoashfallprotocol".equals(namespace)) {
            if (definition.phaseOrder() <= 0) {
                return RoutePhase.SURVIVE;
            }
            if (definition.phaseOrder() <= 2) {
                return RoutePhase.STABILIZE;
            }
            return phase.contains("nexus") || title.contains("nexus") || title.contains("ending")
                    ? RoutePhase.CONTAINMENT
                    : RoutePhase.ROUTE;
        }
        if (chapterId.contains("echoorbitalremnants") || namespace.equals("echoorbitalremnants")) {
            return path.contains("deep_space") || path.contains("echo_zero") || path.contains("final")
                    || path.contains("seal")
                            ? RoutePhase.CONTAINMENT
                            : RoutePhase.ROUTE;
        }
        if (chapterId.contains("echostationfall") || chapterId.endsWith(":stationfall")
                || namespace.equals("echostationfall")) {
            return RoutePhase.ROUTE;
        }
        if (chapterId.contains("echoindustrialnexus") || namespace.equals("echoindustrialnexus")) {
            return RoutePhase.INFRASTRUCTURE;
        }
        if (chapterId.contains("echonexusprotocol") || namespace.equals("echonexusprotocol")
                || chapterId.contains("echoblackboxprotocol") || namespace.equals("echoblackboxprotocol")) {
            return RoutePhase.CONTAINMENT;
        }
        return null;
    }

    private static RoutePhase vanillaPhase(String path) {
        return switch (path) {
            case "story/root", "story/mine_stone", "story/upgrade_tools", "story/smelt_iron", "story/iron_tools",
                    "adventure/kill_a_mob", "adventure/sleep_in_bed", "husbandry/plant_seed" -> RoutePhase.SURVIVE;
            case "husbandry/breed_an_animal", "husbandry/tame_an_animal", "adventure/trade",
                    "adventure/shoot_arrow", "story/lava_bucket", "story/mine_diamond", "story/enchant_item" ->
                    RoutePhase.STABILIZE;
            case "story/enter_the_nether", "story/follow_ender_eye", "story/enter_the_end" -> RoutePhase.ROUTE;
            default -> path.startsWith("nether/") || path.startsWith("end/") ? RoutePhase.ROUTE : null;
        };
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
        SURVIVE("survive", "Survive", 0),
        STABILIZE("stabilize", "Stabilize", 1),
        ROUTE("route", "Route", 2),
        INFRASTRUCTURE("infrastructure", "Infrastructure", 3),
        CONTAINMENT("containment", "Containment", 4),
        OTHER_SIGNALS("other_signals", "Other Signals", 5);

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
}
