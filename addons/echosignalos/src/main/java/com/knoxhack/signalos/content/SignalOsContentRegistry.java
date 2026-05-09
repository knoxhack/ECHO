package com.knoxhack.signalos.content;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalChapter;
import com.knoxhack.signalos.api.TerminalDiagnosticProvider;
import com.knoxhack.signalos.api.TerminalMission;
import com.knoxhack.signalos.api.TerminalPage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class SignalOsContentRegistry {
    private static final Map<Identifier, TerminalChapter> JAVA_CHAPTERS = new ConcurrentHashMap<>();
    private static final Map<Identifier, TerminalPage> JAVA_PAGES = new ConcurrentHashMap<>();
    private static final Map<Identifier, TerminalMission> JAVA_MISSIONS = new ConcurrentHashMap<>();
    private static final Map<Identifier, TerminalArchiveRecord> JAVA_ARCHIVES = new ConcurrentHashMap<>();
    private static final Map<Identifier, TerminalDiagnosticProvider> DIAGNOSTIC_PROVIDERS = new ConcurrentHashMap<>();

    private static final Map<Identifier, TerminalChapter> SCRIPT_CHAPTERS = new ConcurrentHashMap<>();
    private static final Map<Identifier, TerminalMission> SCRIPT_MISSIONS = new ConcurrentHashMap<>();
    private static final Map<Identifier, TerminalArchiveRecord> SCRIPT_ARCHIVES = new ConcurrentHashMap<>();

    private static volatile LoadedContent jsonContent = LoadedContent.empty();

    private SignalOsContentRegistry() {
    }

    public static void registerChapter(TerminalChapter chapter) {
        registerUnique(JAVA_CHAPTERS, chapter.id(), chapter, "chapter");
    }

    public static void registerPage(TerminalPage page) {
        registerUnique(JAVA_PAGES, page.id(), page, "page");
    }

    public static void registerMission(TerminalMission mission) {
        registerUnique(JAVA_MISSIONS, mission.id(), mission, "mission");
    }

    public static void registerArchive(TerminalArchiveRecord archive) {
        registerUnique(JAVA_ARCHIVES, archive.id(), archive, "archive");
    }

    public static void registerDiagnostics(TerminalDiagnosticProvider provider) {
        if (provider == null || provider.id() == null) {
            throw new IllegalArgumentException("SignalOS diagnostic provider id is required.");
        }
        registerUnique(DIAGNOSTIC_PROVIDERS, provider.id(), provider, "diagnostic provider");
    }

    public static void registerScriptChapter(TerminalChapter chapter) {
        SCRIPT_CHAPTERS.put(chapter.id(), chapter);
    }

    public static void registerScriptMission(TerminalMission mission) {
        SCRIPT_MISSIONS.put(mission.id(), mission);
    }

    public static void registerScriptArchive(TerminalArchiveRecord archive) {
        SCRIPT_ARCHIVES.put(archive.id(), archive);
    }

    public static void clearScriptContent() {
        SCRIPT_CHAPTERS.clear();
        SCRIPT_MISSIONS.clear();
        SCRIPT_ARCHIVES.clear();
    }

    public static void replaceJsonContent(LoadedContent loaded) {
        jsonContent = loaded == null ? LoadedContent.empty() : loaded;
        SignalOS.LOGGER.info("SignalOS loaded {} JSON chapters, {} JSON missions, and {} JSON archive records.",
                jsonContent.chapters().size(), jsonContent.missions().size(), jsonContent.archives().size());
        LoadReport report = jsonContent.report();
        if (report.hasProblems()) {
            SignalOS.LOGGER.warn(
                    "SignalOS JSON load report: {} file(s) scanned, {} parsed, {} duplicate id(s) ignored, {} parse failure(s), {} unresolved reference(s) skipped.",
                    report.discoveredFiles(), report.parsedFiles(), report.duplicateIds(), report.failedFiles(),
                    report.rejectedReferences());
        }
    }

    public static List<TerminalChapter> chapters() {
        Map<Identifier, TerminalChapter> merged = new LinkedHashMap<>();
        merged.putAll(JAVA_CHAPTERS);
        putMissing(merged, jsonContent.chapters());
        putMissing(merged, SCRIPT_CHAPTERS);
        return merged.values().stream()
                .filter(TerminalChapter::visible)
                .sorted(Comparator
                        .comparingInt((TerminalChapter chapter) -> sectionOrder(chapter.section()))
                        .thenComparingInt(TerminalChapter::order)
                        .thenComparing(chapter -> chapter.id().toString()))
                .toList();
    }

    public static TerminalChapter chapter(Identifier chapterId) {
        if (chapterId == null) {
            return null;
        }
        return chapters().stream()
                .filter(chapter -> chapter.id().equals(chapterId))
                .findFirst()
                .orElse(null);
    }

    static boolean hasNonJsonChapter(Identifier chapterId) {
        return chapterId != null && (JAVA_CHAPTERS.containsKey(chapterId) || SCRIPT_CHAPTERS.containsKey(chapterId));
    }

    public static List<TerminalPage> pagesFor(Identifier chapterId) {
        if (chapterId == null) {
            return List.of();
        }
        Map<String, TerminalPage> merged = new LinkedHashMap<>();
        JAVA_PAGES.values().stream()
                .filter(page -> page.chapterId().equals(chapterId))
                .sorted(Comparator.comparingInt(TerminalPage::order).thenComparing(page -> page.id().toString()))
                .forEach(page -> merged.putIfAbsent(page.type(), page));

        TerminalChapter chapter = chapter(chapterId);
        if (chapter != null) {
            int order = 0;
            for (String pageType : chapter.pages()) {
                String type = cleanPageType(pageType);
                if (!type.isBlank()) {
                    merged.putIfAbsent(type, inferredPage(chapterId, type, order));
                }
                order += 10;
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparingInt(TerminalPage::order).thenComparing(page -> page.id().toString()))
                .toList();
    }

    public static List<TerminalMission> missionsFor(Identifier chapterId) {
        return missions().stream()
                .filter(mission -> mission.chapterId().equals(chapterId))
                .sorted(Comparator.comparingInt(TerminalMission::order).thenComparing(mission -> mission.id().toString()))
                .toList();
    }

    public static List<TerminalMission> missions() {
        Map<Identifier, TerminalMission> merged = new LinkedHashMap<>();
        merged.putAll(JAVA_MISSIONS);
        putMissing(merged, jsonContent.missions());
        putMissing(merged, SCRIPT_MISSIONS);
        return merged.values().stream()
                .sorted(Comparator
                        .comparing((TerminalMission mission) -> mission.chapterId().toString())
                        .thenComparingInt(TerminalMission::order)
                        .thenComparing(mission -> mission.id().toString()))
                .toList();
    }

    public static TerminalMission mission(Identifier missionId) {
        if (missionId == null) {
            return null;
        }
        TerminalMission mission = JAVA_MISSIONS.get(missionId);
        if (mission == null) {
            mission = jsonContent.missions().get(missionId);
        }
        return mission == null ? SCRIPT_MISSIONS.get(missionId) : mission;
    }

    public static List<TerminalArchiveRecord> archivesFor(Identifier chapterId) {
        return archives().stream()
                .filter(record -> record.chapterId().equals(chapterId))
                .sorted(Comparator.comparingInt(TerminalArchiveRecord::order)
                        .thenComparing(TerminalArchiveRecord::title)
                        .thenComparing(record -> record.id().toString()))
                .toList();
    }

    public static TerminalArchiveRecord archive(Identifier archiveId) {
        if (archiveId == null) {
            return null;
        }
        TerminalArchiveRecord archive = JAVA_ARCHIVES.get(archiveId);
        if (archive == null) {
            archive = jsonContent.archives().get(archiveId);
        }
        return archive == null ? SCRIPT_ARCHIVES.get(archiveId) : archive;
    }

    public static List<TerminalArchiveRecord> archives() {
        Map<Identifier, TerminalArchiveRecord> merged = new LinkedHashMap<>();
        merged.putAll(JAVA_ARCHIVES);
        putMissing(merged, jsonContent.archives());
        putMissing(merged, SCRIPT_ARCHIVES);
        return merged.values().stream()
                .sorted(Comparator
                        .comparing((TerminalArchiveRecord record) -> record.chapterId().toString())
                        .thenComparingInt(TerminalArchiveRecord::order)
                        .thenComparing(record -> record.id().toString()))
                .toList();
    }

    public static List<TerminalDiagnosticProvider.Diagnostic> diagnostics(Player player) {
        List<TerminalDiagnosticProvider> providers = new ArrayList<>(DIAGNOSTIC_PROVIDERS.values());
        providers.sort(Comparator.comparingInt(TerminalDiagnosticProvider::order)
                .thenComparing(provider -> provider.id().toString()));
        List<TerminalDiagnosticProvider.Diagnostic> diagnostics = new ArrayList<>();
        for (TerminalDiagnosticProvider provider : providers) {
            try {
                List<TerminalDiagnosticProvider.Diagnostic> provided = provider.diagnostics(player);
                if (provided != null) {
                    diagnostics.addAll(provided.stream().filter(java.util.Objects::nonNull).toList());
                }
            } catch (RuntimeException exception) {
                SignalOS.LOGGER.warn("SignalOS diagnostic provider {} failed.", provider.id(), exception);
            }
        }
        diagnostics.sort(Comparator
                .comparingInt((TerminalDiagnosticProvider.Diagnostic diagnostic) -> severityOrder(diagnostic.severity()))
                .thenComparing(diagnostic -> diagnostic.id().toString()));
        return List.copyOf(diagnostics);
    }

    public static void clearForTests() {
        JAVA_CHAPTERS.clear();
        JAVA_PAGES.clear();
        JAVA_MISSIONS.clear();
        JAVA_ARCHIVES.clear();
        DIAGNOSTIC_PROVIDERS.clear();
        clearScriptContent();
        jsonContent = LoadedContent.empty();
    }

    public static void withClearedForTests(Runnable body) {
        Map<Identifier, TerminalChapter> chapters = Map.copyOf(JAVA_CHAPTERS);
        Map<Identifier, TerminalPage> pages = Map.copyOf(JAVA_PAGES);
        Map<Identifier, TerminalMission> missions = Map.copyOf(JAVA_MISSIONS);
        Map<Identifier, TerminalArchiveRecord> archives = Map.copyOf(JAVA_ARCHIVES);
        Map<Identifier, TerminalDiagnosticProvider> diagnostics = Map.copyOf(DIAGNOSTIC_PROVIDERS);
        Map<Identifier, TerminalChapter> scriptChapters = Map.copyOf(SCRIPT_CHAPTERS);
        Map<Identifier, TerminalMission> scriptMissions = Map.copyOf(SCRIPT_MISSIONS);
        Map<Identifier, TerminalArchiveRecord> scriptArchives = Map.copyOf(SCRIPT_ARCHIVES);
        LoadedContent previousJson = jsonContent;
        clearForTests();
        try {
            body.run();
        } finally {
            JAVA_CHAPTERS.putAll(chapters);
            JAVA_PAGES.putAll(pages);
            JAVA_MISSIONS.putAll(missions);
            JAVA_ARCHIVES.putAll(archives);
            DIAGNOSTIC_PROVIDERS.putAll(diagnostics);
            SCRIPT_CHAPTERS.putAll(scriptChapters);
            SCRIPT_MISSIONS.putAll(scriptMissions);
            SCRIPT_ARCHIVES.putAll(scriptArchives);
            jsonContent = previousJson;
        }
    }

    private static <T> void registerUnique(Map<Identifier, T> target, Identifier id, T value, String kind) {
        T previous = target.putIfAbsent(id, value);
        if (previous != null && previous != value) {
            throw new IllegalArgumentException("Duplicate SignalOS " + kind + " id: " + id);
        }
    }

    private static <T> void putMissing(Map<Identifier, T> target, Map<Identifier, T> source) {
        for (Map.Entry<Identifier, T> entry : source.entrySet()) {
            target.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private static TerminalPage inferredPage(Identifier chapterId, String type, int order) {
        return new TerminalPage(
                Identifier.fromNamespaceAndPath(chapterId.getNamespace(), chapterId.getPath() + "/" + type),
                chapterId,
                pageTitle(type),
                type,
                order);
    }

    private static String cleanPageType(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }

    private static String pageTitle(String type) {
        return switch (type) {
            case "missions" -> "Missions";
            case "archives" -> "Archives";
            case "rewards", "reward_inbox" -> "Rewards";
            case "diagnostics" -> "Diagnostics";
            default -> type;
        };
    }

    private static int sectionOrder(String section) {
        return switch (section == null ? "" : section) {
            case "command" -> 0;
            case "progress" -> 100;
            case "intel" -> 200;
            case "system" -> 300;
            default -> 500;
        };
    }

    private static int severityOrder(TerminalDiagnosticProvider.Severity severity) {
        return switch (severity == null ? TerminalDiagnosticProvider.Severity.INFO : severity) {
            case CRITICAL -> 0;
            case BLOCKED -> 1;
            case WARNING -> 2;
            case INFO -> 3;
        };
    }

    public record LoadedContent(
            Map<Identifier, TerminalChapter> chapters,
            Map<Identifier, TerminalMission> missions,
            Map<Identifier, TerminalArchiveRecord> archives,
            LoadReport report) {
        public LoadedContent {
            chapters = Map.copyOf(chapters == null ? Map.of() : chapters);
            missions = Map.copyOf(missions == null ? Map.of() : missions);
            archives = Map.copyOf(archives == null ? Map.of() : archives);
            report = report == null ? LoadReport.empty() : report;
        }

        public LoadedContent(Map<Identifier, TerminalChapter> chapters,
                Map<Identifier, TerminalMission> missions,
                Map<Identifier, TerminalArchiveRecord> archives) {
            this(chapters, missions, archives, LoadReport.empty());
        }

        public static LoadedContent empty() {
            return new LoadedContent(Map.of(), Map.of(), Map.of(), LoadReport.empty());
        }
    }

    public record LoadReport(
            int discoveredFiles,
            int parsedFiles,
            int duplicateIds,
            int failedFiles,
            int rejectedReferences) {
        public LoadReport {
            discoveredFiles = Math.max(0, discoveredFiles);
            parsedFiles = Math.max(0, parsedFiles);
            duplicateIds = Math.max(0, duplicateIds);
            failedFiles = Math.max(0, failedFiles);
            rejectedReferences = Math.max(0, rejectedReferences);
        }

        public static LoadReport empty() {
            return new LoadReport(0, 0, 0, 0, 0);
        }

        public LoadReport plus(LoadReport other) {
            if (other == null) {
                return this;
            }
            return new LoadReport(
                    discoveredFiles + other.discoveredFiles,
                    parsedFiles + other.parsedFiles,
                    duplicateIds + other.duplicateIds,
                    failedFiles + other.failedFiles,
                    rejectedReferences + other.rejectedReferences);
        }

        public LoadReport withRejectedReferences(int additionalRejectedReferences) {
            return new LoadReport(discoveredFiles, parsedFiles, duplicateIds, failedFiles,
                    rejectedReferences + Math.max(0, additionalRejectedReferences));
        }

        public boolean hasProblems() {
            return duplicateIds > 0 || failedFiles > 0 || rejectedReferences > 0;
        }
    }
}
