package com.knoxhack.echoterminal.client.mission;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalRenderCache;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionPresentation;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRequirement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionReward;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoterminal.api.mission.TerminalMissionVisuals;
import com.knoxhack.echoterminal.client.screen.TerminalClientOptions;
import com.knoxhack.echoterminal.player.TerminalPlayerData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class TerminalMissionBrowser {
    private static final int PHASE_ROW_HEIGHT = 28;
    private static final int MISSION_ROW_HEIGHT = 36;
    private static final int ACTION_BAR_HEIGHT = 92;
    private static final int TREE_FOCUS_EXTRA = 8;
    private static final int STATE_REFRESH_TICKS = 10;
    private static final int WIDTH_BUCKET_SIZE = 80;
    private static final int SPLIT_LAYOUT_MIN_WIDTH = 900;
    private static final String ACTIONS_SECTION_TITLE = "ACTIONS";
    private static final String EMPTY_REQUIREMENTS_COPY =
            "No checklist is needed for this record. Follow the next step above.";
    private static final String MET_REQUIREMENTS_COPY =
            "Requirements are clear. Use the available actions below.";

    private final TerminalMissionProvider provider;
    private final Identifier tabId;
    private final int stateRefreshTicks;
    private final List<Hitbox> hitboxes = new ArrayList<>();
    private final Set<String> expandedPhases = new LinkedHashSet<>();
    private final Set<String> collapsedPhases = new LinkedHashSet<>();
    private final Set<String> warnedProviderSurfaces = new LinkedHashSet<>();

    private Identifier selectedMissionId;
    private Identifier lastDetailMissionId;
    private TreeFocusMode pendingTreeFocus = TreeFocusMode.NONE;
    private int treeScroll;
    private int detailScroll;
    private int lastTreeX;
    private int lastTreeY;
    private int lastTreeW;
    private int lastTreeH;
    private int lastTreeContentH;
    private int lastDetailX;
    private int lastDetailY;
    private int lastDetailW;
    private int lastDetailH;
    private int lastDetailContentH;
    private boolean lastDetailScrollable;
    private CacheKey cachedStateKey;
    private CacheKey staleServedKey;
    private MissionRenderState cachedState;
    private long cachedStateFrame = -1L;

    public TerminalMissionBrowser(TerminalMissionProvider provider, Identifier tabId, boolean showExpandControls) {
        this(provider, tabId, showExpandControls, STATE_REFRESH_TICKS);
    }

    public TerminalMissionBrowser(
            TerminalMissionProvider provider, Identifier tabId, boolean showExpandControls, int stateRefreshTicks) {
        this.provider = provider;
        this.tabId = tabId;
        this.stateRefreshTicks = Math.max(1, stateRefreshTicks);
    }

    public void onSelected(TerminalRenderContext context) {
        expandedPhases.clear();
        collapsedPhases.clear();
        treeScroll = 0;
        detailScroll = 0;
        selectedMissionId = null;
        lastDetailMissionId = null;
        invalidateStateCache();
        pendingTreeFocus = TreeFocusMode.ALIGN_SELECTED_TOP;
    }

    public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        hitboxes.clear();
        MissionRenderState state = buildState(context);
        normalizeSelection(state);
        syncDetailScrollWithSelection();
        TerminalMissionChapter chapter = chapter();
        int x = context.contentX();
        int y = context.contentY();
        int w = context.contentWidth();
        int h = context.contentHeight();
        if (state.allRecords().isEmpty()) {
            Identifier hero = context.theme().tokens().assets().loading() == null
                    ? TerminalVisualAssets.MISSIONS_VISUAL_HERO
                    : context.theme().tokens().assets().loading();
            y = TerminalUi.imageHero(context, graphics, hero,
                    x, y, w, Math.min(60, Math.max(44, h / 5)), chapter.accentColor());
            TerminalUi.emptyState(context, graphics, x, y, w,
                    chapter.title(), "No mission records are available from this chapter yet.", chapter.accentColor());
            return;
        }

        MissionRecord selected = selectedRecord(state);
        boolean splitLayout = w >= SPLIT_LAYOUT_MIN_WIDTH;
        if (splitLayout) {
            int gap = 12;
            int leftW = Math.max(340, Math.min(460, w * 40 / 100));
            int detailX = x + leftW + gap;
            int detailW = Math.max(420, w - leftW - gap);
            drawRoadmapPane(context, graphics, state, x, y, leftW, h, mouseX, mouseY);
            drawDetailPane(context, graphics, selected, detailX, y, detailW, h, mouseX, mouseY, true);
        } else {
            int treeH = stackedTreeHeight(context, state, w, h);
            drawRoadmapPane(context, graphics, state, x, y, w, treeH, mouseX, mouseY);
            drawDetailPane(context, graphics, selected, x, y + treeH + 10, w,
                    Math.max(180, detailBodyHeight(context, selected, w, false) + actionBarHeight() + 18),
                    mouseX, mouseY, false);
        }
    }

    public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        for (Hitbox hitbox : List.copyOf(hitboxes)) {
            if (TerminalUi.inside(mouseX, mouseY, hitbox.x(), hitbox.y(), hitbox.w(), hitbox.h())) {
                if (hitbox.enabled()) {
                    hitbox.action().run();
                } else {
                    context.playRejectedSound();
                }
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
        if (event == null) {
            return false;
        }
        return handleKey(context, event.key());
    }

    private boolean handleKey(TerminalRenderContext context, int key) {
        MissionRenderState state = buildState(context);
        normalizeSelection(state);
        if (key == GLFW.GLFW_KEY_UP) {
            return selectRelative(state, -1);
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            return selectRelative(state, 1);
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) {
            return activateSelectedAction(context, state);
        }
        return false;
    }

    public boolean charTyped(TerminalRenderContext context, CharacterEvent event) {
        return false;
    }

    public boolean mouseScrolled(TerminalRenderContext context, double mouseX, double mouseY, double delta) {
        int amount = (int) Math.round(delta * 18.0D);
        if (TerminalUi.inside(mouseX, mouseY, lastTreeX, lastTreeY, lastTreeW, lastTreeH)) {
            treeScroll = TerminalUi.clampScroll(treeScroll - amount, lastTreeContentH, lastTreeH);
            return true;
        }
        if (lastDetailScrollable && TerminalUi.inside(mouseX, mouseY, lastDetailX, lastDetailY, lastDetailW, lastDetailH)) {
            detailScroll = TerminalUi.clampScroll(detailScroll - amount, lastDetailContentH, lastDetailH);
            return true;
        }
        return false;
    }

    public int contentHeight(TerminalRenderContext context) {
        int w = context.contentWidth();
        MissionRenderState state = buildState(context);
        normalizeSelection(state);
        if (w >= SPLIT_LAYOUT_MIN_WIDTH) {
            return context.contentHeight();
        }
        MissionRecord selected = selectedRecord(state);
        return Math.max(context.contentHeight(), stackedTreeHeight(context, state, w, context.contentHeight())
                + detailBodyHeight(context, selected, w, false) + actionBarHeight() + 38);
    }

    private int densityStep() {
        return TerminalClientOptions.interfaceDensity().compactness();
    }

    private int phaseRowHeight() {
        return Math.max(20, PHASE_ROW_HEIGHT - densityStep());
    }

    private int missionRowHeight() {
        return Math.max(30, MISSION_ROW_HEIGHT - densityStep() * 2);
    }

    private int actionBarHeight() {
        return Math.max(78, ACTION_BAR_HEIGHT - densityStep() * 6);
    }

    public boolean hasCachedStateForTests() {
        return cachedState != null;
    }

    public int visibleMissionCountForTests(TerminalRenderContext context) {
        return buildState(context, false).visibleRecords().size();
    }

    public int allMissionCountForTests(TerminalRenderContext context) {
        return buildState(context, false).allRecords().size();
    }

    public boolean keyCodeForTests(TerminalRenderContext context, int key) {
        return handleKey(context, key);
    }

    public int treePaneHeightForTests(TerminalRenderContext context, int width) {
        return treePaneHeight(context, buildState(context, false), width);
    }

    public List<String> phaseDebugRowsForTests(TerminalRenderContext context) {
        MissionRenderState state = buildState(context, false);
        return state.allPhases().stream()
                .map(phase -> phase.label() + "|" + phase.stateLabel() + "|" + phase.contextTitle())
                .toList();
    }

    public boolean phaseExpandedForTests(TerminalRenderContext context, String label) {
        MissionRenderState state = buildState(context, false);
        return state.allPhases().stream()
                .filter(phase -> phase.label().equals(label))
                .findFirst()
                .map(this::isPhaseExpanded)
                .orElse(false);
    }

    public Identifier focusMissionIdForTests(TerminalRenderContext context) {
        MissionRenderState state = buildState(context, false);
        MissionRecord focus = state.focusRecord();
        return focus == null ? null : focus.id();
    }

    public Identifier selectedMissionIdForTests(TerminalRenderContext context) {
        MissionRenderState state = buildState(context, false);
        normalizeSelection(state);
        return selectedMissionId;
    }

    public int detailHeaderHeightForTests(TerminalRenderContext context, Identifier missionId) {
        MissionRenderState state = buildState(context, false);
        MissionRecord record = state.allRecords().stream()
                .filter(candidate -> candidate.id().equals(missionId))
                .findFirst()
                .orElse(null);
        return record == null ? 0 : briefingHeaderHeight(record);
    }

    public boolean selectMissionForTests(TerminalRenderContext context, Identifier missionId) {
        MissionRenderState state = buildState(context, false);
        if (state.visibleRecords().stream().noneMatch(record -> record.id().equals(missionId))) {
            return false;
        }
        selectMission(missionId, false);
        normalizeSelection(state);
        MissionRecord selected = selectedRecord(state);
        return selected != null && selected.id().equals(missionId);
    }

    public boolean missionReadOnlyForTests(TerminalRenderContext context, Identifier missionId) {
        MissionRenderState state = buildState(context, false);
        return state.allRecords().stream()
                .filter(record -> record.id().equals(missionId))
                .findFirst()
                .map(MissionRecord::phaseLocked)
                .orElse(false);
    }

    public int enabledActionCountForTests(TerminalRenderContext context, Identifier missionId) {
        MissionRenderState state = buildState(context, false);
        return state.allRecords().stream()
                .filter(record -> record.id().equals(missionId))
                .findFirst()
                .map(record -> record.phaseLocked()
                        ? 0
                        : (int) record.snapshot().actions().stream().filter(TerminalMissionAction::enabled).count())
                .orElse(0);
    }

    public boolean activateMissionActionForTests(TerminalRenderContext context, Identifier missionId) {
        MissionRenderState state = buildState(context, false);
        selectMission(missionId, false);
        return activateSelectedAction(context, state);
    }

    public int applyTreeFocusForTests(TerminalRenderContext context, int viewportHeight) {
        MissionRenderState state = buildState(context, false);
        normalizeSelection(state);
        lastTreeH = Math.max(1, viewportHeight);
        lastTreeContentH = treeRowsHeight(context, state);
        focusTreeOnSelection(state);
        treeScroll = TerminalUi.clampScroll(treeScroll, lastTreeContentH, lastTreeH);
        return treeScroll;
    }

    public int treeScrollForTests() {
        return treeScroll;
    }

    public int treeMaxScrollForTests(TerminalRenderContext context, int viewportHeight) {
        MissionRenderState state = buildState(context, false);
        return Math.max(0, treeRowsHeight(context, state) - Math.max(1, viewportHeight));
    }

    public int selectedRowOffsetForTests(TerminalRenderContext context) {
        MissionRenderState state = buildState(context, false);
        normalizeSelection(state);
        return selectedRowOffset(state);
    }

    public String stickyActionsTitleForTests() {
        return ACTIONS_SECTION_TITLE;
    }

    public String emptyRequirementsCopyForTests() {
        return EMPTY_REQUIREMENTS_COPY;
    }

    public String metRequirementsCopyForTests() {
        return MET_REQUIREMENTS_COPY;
    }

    private MissionRenderState buildState(TerminalRenderContext context) {
        return buildState(context, true);
    }

    private MissionRenderState buildState(TerminalRenderContext context, boolean allowStale) {
        long frameId = TerminalRenderCache.current().frameId();
        CacheKey key = cacheKey(context);
        if (cachedState != null && key.equals(cachedStateKey)) {
            return cachedState;
        }
        if (cachedState != null && allowStale && !key.equals(staleServedKey)) {
            staleServedKey = key;
            return cachedState;
        }
        MissionRenderState state = buildFreshState(context);
        cachedState = state;
        cachedStateFrame = frameId;
        cachedStateKey = key;
        staleServedKey = null;
        return state;
    }

    private MissionRenderState buildFreshState(TerminalRenderContext context) {
        List<TerminalMissionDefinition> definitions = safeMissions(context).stream()
                .filter(definition -> definition != null)
                .sorted(Comparator
                        .comparingInt(TerminalMissionDefinition::phaseOrder)
                        .thenComparingInt(TerminalMissionDefinition::missionOrder)
                        .thenComparing(mission -> mission.id().toString()))
                .toList();
        List<MissionRecord> rawRecords = new ArrayList<>();
        for (TerminalMissionDefinition definition : definitions) {
            TerminalMissionSnapshot snapshot = safeSnapshot(context, definition);
            TerminalMissionPresentation presentation = safePresentation(context, definition, snapshot);
            TerminalMissionVisuals visuals = safeVisuals(context, definition, snapshot);
            TerminalMissionRole role = safeRole(context, definition, snapshot);
            rawRecords.add(new MissionRecord(definition, snapshot, presentation, visuals, role));
        }
        PhaseModel rawPhaseModel = buildPhaseModel(rawRecords);
        List<MissionRecord> records = rawRecords.stream()
                .map(record -> record.withPhase(rawPhaseModel.phase(record.phaseKey())))
                .toList();
        PhaseModel phaseModel = buildPhaseModel(records);
        List<MissionRecord> visible = records;
        List<PhaseGroup> visiblePhases = visiblePhases(phaseModel, visible);
        MissionRecord focus = focusRecord(visible);
        int completed = 0;
        for (MissionRecord record : records) {
            if (isDone(record.snapshot().status())) {
                completed++;
            }
        }
        return new MissionRenderState(records, visible, phaseModel.phases(), visiblePhases, focus, completed);
    }

    private CacheKey cacheKey(TerminalRenderContext context) {
        net.minecraft.world.entity.player.Player player = context == null ? null : context.player();
        UUID playerId = player == null ? new UUID(0L, 0L) : player.getUUID();
        long gameTime = player == null || player.level() == null ? 0L : player.level().getGameTime();
        int refreshTick = (int) Math.max(0L, gameTime / stateRefreshTicks);
        int widthBucket = context == null ? 0 : Math.max(0, context.contentWidth() / WIDTH_BUCKET_SIZE);
        return new CacheKey(providerName(), tabId, playerId, widthBucket, refreshTick);
    }

    private TerminalMissionChapter chapter() {
        try {
            TerminalMissionChapter chapter = provider == null ? null : provider.chapter();
            return chapter == null ? fallbackChapter() : chapter;
        } catch (RuntimeException exception) {
            warnProviderFailure("chapter", exception);
            return fallbackChapter();
        }
    }

    private TerminalMissionChapter fallbackChapter() {
        Identifier id = tabId == null ? Identifier.fromNamespaceAndPath("echoterminal", "unknown_missions") : tabId;
        return new TerminalMissionChapter(id, "Mission Records", "Mission provider unavailable.", Integer.MAX_VALUE,
                0xFF66D9FF, true);
    }

    private List<TerminalMissionDefinition> safeMissions(TerminalRenderContext context) {
        try {
            List<TerminalMissionDefinition> missions = provider == null
                    ? List.of()
                    : provider.missions(context == null ? null : context.player());
            return missions == null ? List.of() : missions;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission list", exception);
            return List.of();
        }
    }

    private TerminalMissionSnapshot safeSnapshot(TerminalRenderContext context, TerminalMissionDefinition definition) {
        try {
            TerminalMissionSnapshot snapshot = provider.snapshot(context == null ? null : context.player(), definition.id());
            return snapshot == null ? fallbackSnapshot(definition) : snapshot;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission snapshot", exception);
            return fallbackSnapshot(definition);
        }
    }

    private TerminalMissionSnapshot fallbackSnapshot(TerminalMissionDefinition definition) {
        return new TerminalMissionSnapshot(
                definition.id(),
                TerminalMissionStatus.LOCKED,
                0.0F,
                "LOCKED",
                "Mission provider unavailable.",
                "Open What Now? diagnostics or reload this chapter later.",
                List.of());
    }

    private TerminalMissionPresentation safePresentation(TerminalRenderContext context,
            TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionPresentation presentation = provider.presentation(
                    context == null ? null : context.player(), definition, snapshot);
            return presentation == null
                    ? TerminalMissionPresentation.fallback(definition, snapshot)
                    : presentation;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission presentation", exception);
            return TerminalMissionPresentation.fallback(definition, snapshot);
        }
    }

    private TerminalMissionVisuals safeVisuals(TerminalRenderContext context,
            TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionVisuals visuals = provider.visuals(context == null ? null : context.player(), definition, snapshot);
            return visuals == null ? TerminalMissionVisuals.fallback(definition, snapshot) : visuals;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission visuals", exception);
            return TerminalMissionVisuals.fallback(definition, snapshot);
        }
    }

    private TerminalMissionRole safeRole(TerminalRenderContext context,
            TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionRole role = provider.role(context == null ? null : context.player(), definition, snapshot);
            return role == null ? TerminalMissionRole.fallback(definition, snapshot) : role;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission role", exception);
            return TerminalMissionRole.fallback(definition, snapshot);
        }
    }

    private void warnProviderFailure(String surface, RuntimeException exception) {
        if (warnedProviderSurfaces.add(surface)) {
            EchoTerminal.LOGGER.warn("Terminal mission provider {} failed while building {}; rendering fallback.",
                    providerName(), surface, exception);
        }
    }

    private String providerName() {
        return provider == null ? "<null>" : provider.getClass().getName();
    }

    private void drawRoadmapPane(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int h, int mouseX, int mouseY) {
        TerminalMissionChapter chapter = chapter();
        TerminalUi.cinematicPanel(context, graphics, x, y, w, h,
                TerminalUi.chapterAccent(context, chapter.accentColor()));
        int total = state.allRecords().size();
        int pct = total > 0 ? Math.round(state.completedCount() * 100.0F / total) : 0;
        String countLine = "ECHO-7 PROTOCOL // " + pct + "% COMPLETE";
        int countW = Math.min(180, Math.max(120, w / 3));
        TerminalUi.line(context, graphics, "ROADMAP", x + 12, y + 10,
                Math.max(48, w - countW - 28), chapter.accentColor());
        TerminalUi.line(context, graphics, countLine, x + w - countW - 12, y + 10, countW, TerminalUi.MUTED);
        TerminalUi.divider(graphics, x + 12, y + 27, w - 24, chapter.accentColor());
        int innerX = x + 12;
        int innerW = w - 24;
        int listY = y + 36;

        lastTreeX = innerX;
        lastTreeY = listY;
        lastTreeW = innerW - 4;
        lastTreeH = Math.max(68, h - (listY - y) - 12);
        lastTreeContentH = treeRowsHeight(context, state);
        focusTreeOnSelection(state);
        treeScroll = TerminalUi.clampScroll(treeScroll, lastTreeContentH, lastTreeH);
        boolean scissor = lastTreeContentH > lastTreeH;
        if (scissor) {
            graphics.enableScissor(innerX, listY, innerX + innerW - 4, listY + lastTreeH);
        }
        drawTreeRows(context, graphics, state, innerX, listY - (scissor ? treeScroll : 0),
                innerW, listY, lastTreeH, mouseX, mouseY);
        if (scissor) {
            graphics.disableScissor();
            TerminalUi.scrollbar(graphics, innerX + innerW - 5, listY, lastTreeH,
                    treeScroll, Math.max(0, lastTreeContentH - lastTreeH), chapter.accentColor());
        }
    }

    private void drawTreeRows(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int viewportY, int viewportH, int mouseX, int mouseY) {
        if (state.visibleRecords().isEmpty()) {
            TerminalUi.emptyState(context, graphics, x + 2, y + 4, Math.max(80, w - 12),
                    "No Mission Records", "No mission records are available from this chapter yet.", TerminalUi.MUTED);
            return;
        }
        int cy = y;
        int phaseH = phaseRowHeight();
        int missionH = missionRowHeight();
        cy = drawGuidedLanes(context, graphics, state, x, cy, w, viewportY, viewportH, mouseX, mouseY);
        cy += 6;
        for (PhaseGroup phase : state.visiblePhases()) {
            List<MissionRecord> phaseAll = recordsForPhase(state.allRecords(), phase.id());
            int complete = completedCount(phaseAll);
            int total = phaseAll.size();
            boolean expanded = isPhaseExpanded(phase);
            int phaseColor = phase.locked() ? TerminalUi.MUTED : phase.complete() ? TerminalUi.GREEN : chapter().accentColor();
            boolean hover = TerminalUi.inside(mouseX, mouseY, x, cy, w - 8, phaseH);
            String progressCount = complete + "/" + total;
            String phaseLabel = phase.label().isBlank() ? phase.contextTitle() : phase.label();
            TerminalUi.phaseAccordionRow(context, graphics, x, cy, w - 8, phaseH,
                    String.format(Locale.ROOT, "%02d", phase.displayIndex() + 1),
                    phaseLabel, progressCount, expanded, hover, phaseColor);
            if (visible(cy, phaseH, viewportY, viewportH)) {
                PhaseGroup hitPhase = phase;
                addHitbox(x, cy, w - 8, phaseH, true, () -> togglePhase(hitPhase));
            }
            cy += phaseH + 2;
            if (!expanded) {
                continue;
            }
            for (MissionRecord record : phase.records()) {
                boolean selected = record.id().equals(selectedMissionId);
                if (selected) {
                    drawMissionRow(context, graphics, record, x + 8, cy, w - 18, viewportY, viewportH, mouseX, mouseY);
                } else {
                    drawSubduedMissionRow(context, graphics, record, x + 8, cy, w - 18, viewportY, viewportH, mouseX, mouseY);
                }
                cy += missionH;
            }
            cy += 2;
        }
    }

    private int drawGuidedLanes(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int viewportY, int viewportH, int mouseX, int mouseY) {
        int cy = y;
        if (state.focusRecord() != null) {
            TerminalUi.missionLaneHeader(context, graphics, x, cy, w - 8,
                    "CURRENT OBJECTIVE", "", chapter().accentColor());
            cy += 20;
            drawMissionRow(context, graphics, state.focusRecord(), x + 8, cy, w - 18, viewportY, viewportH, mouseX, mouseY);
            cy += missionRowHeight();
        }
        List<MissionRecord> ready = state.visibleRecords().stream()
                .filter(record -> !record.phaseLocked())
                .filter(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)
                .filter(record -> state.focusRecord() == null || !record.id().equals(state.focusRecord().id()))
                .limit(2)
                .toList();
        if (!ready.isEmpty()) {
            TerminalUi.missionLaneHeader(context, graphics, x, cy, w - 8,
                    "READY REWARDS", String.valueOf(ready.size()), TerminalUi.GREEN);
            cy += 20;
            for (MissionRecord record : ready) {
                drawMissionRow(context, graphics, record, x + 8, cy, w - 18, viewportY, viewportH, mouseX, mouseY);
                cy += missionRowHeight();
            }
        }
        return cy;
    }

    private int drawLane(TerminalRenderContext context, GuiGraphicsExtractor graphics, String title,
            List<MissionRecord> records, List<MissionRecord> fallback, int x, int y, int w,
            int viewportY, int viewportH, int mouseX, int mouseY, int color) {
        List<MissionRecord> rows = records.isEmpty() ? fallback : records;
        int cy = TerminalUi.missionLaneHeader(context, graphics, x, y, w - 8, title,
                rows.isEmpty() ? "0" : String.valueOf(rows.size()), color);
        int missionH = missionRowHeight();
        for (MissionRecord record : rows) {
            drawMissionRow(context, graphics, record, x + 8, cy, w - 18, viewportY, viewportH, mouseX, mouseY);
            cy += missionH;
        }
        return cy + 2;
    }

    private void drawMissionRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int rowX, int y, int rowW, int viewportY, int viewportH, int mouseX, int mouseY) {
        TerminalMissionSnapshot snapshot = record.snapshot();
        boolean selected = record.id().equals(selectedMissionId);
        boolean locked = record.phaseLocked();
        int color = locked ? TerminalUi.MUTED : statusColor(snapshot.status());
        String statusLabel = locked ? "LOCKED" : compactStatusLabel(snapshot);
        int chipW = Math.max(54, Math.min(Math.max(58, rowW / 3),
                Math.min(98, TerminalUi.statusBadgeWidth(context, statusLabel))));
        int chipX = rowX + rowW - chipW - 6;
        int textX = rowX + 30;
        int titleW = Math.max(34, chipX - textX - 8);
        int progressW = Math.max(36, chipX - textX);
        int missionH = missionRowHeight();
        int rowH = missionH - 4;
        boolean hovered = TerminalUi.inside(mouseX, mouseY, rowX, y, rowW, rowH);
        TerminalRenderContext recordContext = context.withChapterTheme(record.definition().id().getNamespace(),
                chapter().title(), record.definition().id().getNamespace());
        TerminalUi.selectableRow(recordContext, graphics, rowX, y, rowW, rowH,
                selected, hovered, color);
        if (selected) {
            graphics.fill(rowX, y, rowX + 3, y + rowH, color);
            graphics.outline(rowX, y, rowW, rowH, color);
        } else if (!locked
                && (snapshot.status() == TerminalMissionStatus.UNLOCKED
                        || snapshot.status() == TerminalMissionStatus.CLAIMABLE)) {
            graphics.fill(rowX, y, rowX + 2, y + rowH, color);
        }
        int iconSize = Math.min(20, Math.max(16, missionH - 10));
        int iconY = y + Math.max(3, (rowH - iconSize) / 2);
        TerminalIcon semanticIcon = semanticIconForStatus(snapshot.status(), locked);
        TerminalUi.iconBadge(recordContext, graphics, semanticIcon, rowX + 5, iconY, iconSize, color,
                selected || hovered);
        String rolePrefix = record.role() == TerminalMissionRole.OPTIONAL ? "OPT " : "";
        TerminalUi.line(context, graphics, rolePrefix + record.definition().missionOrder() + ". " + record.presentation().shortTitle(),
                textX, y + 3, titleW, locked ? selected ? TerminalUi.TEXT : TerminalUi.MUTED
                        : missionTitleColor(snapshot.status(), selected, color));
        TerminalUi.missionStatusPill(context, graphics, statusLabel, chipX, y + 3, chipW);
        TerminalUi.progress(recordContext, graphics, textX, y + Math.max(17, missionH - 10), progressW, 4,
                snapshot.progress(), color);
        if (visible(y, missionH, viewportY, viewportH)) {
            addHitbox(rowX, y, rowW, rowH, true, () -> selectMission(record.id(), false));
        }
    }

    private void drawSubduedMissionRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int rowX, int y, int rowW, int viewportY, int viewportH, int mouseX, int mouseY) {
        TerminalMissionSnapshot snapshot = record.snapshot();
        boolean selected = record.id().equals(selectedMissionId);
        boolean locked = record.phaseLocked();
        int color = locked ? TerminalUi.MUTED : statusColor(snapshot.status());
        String statusLabel = locked ? "LOCKED" : compactStatusLabel(snapshot);
        int missionH = missionRowHeight();
        int rowH = missionH - 4;
        boolean hovered = TerminalUi.inside(mouseX, mouseY, rowX, y, rowW, rowH);
        TerminalIcon semanticIcon = semanticIconForStatus(snapshot.status(), locked);
        TerminalUi.subduedMissionRow(context, graphics, rowX, y, rowW, rowH,
                semanticIcon, record.presentation().shortTitle(), statusLabel,
                snapshot.progress(), color, selected, hovered);
        if (visible(y, missionH, viewportY, viewportH)) {
            addHitbox(rowX, y, rowW, rowH, !locked, () -> selectMission(record.id(), false));
        }
    }

    private static TerminalIcon semanticIconForStatus(TerminalMissionStatus status, boolean locked) {
        if (locked) {
            return TerminalIcon.LOCK;
        }
        return switch (status) {
            case COMPLETED, CLAIMED -> TerminalIcon.CHECK;
            case CLAIMABLE -> TerminalIcon.CHECK;
            case UNLOCKED -> TerminalIcon.TARGET;
            case VIEW_ONLY -> TerminalIcon.CLOCK;
            case LOCKED -> TerminalIcon.LOCK;
        };
    }

    private void drawDetailPane(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int h, int mouseX, int mouseY, boolean scrollable) {
        boolean largeCard = scrollable;
        TerminalUi.cinematicPanel(context, graphics, x, y, w - 4, h,
                TerminalUi.chapterAccent(context, chapter().accentColor()));
        if (record == null) {
            TerminalUi.emptyState(context, graphics, x + 10, y + 12, w - 24,
                    "Select Mission", "Choose a mission record from the command queue.", TerminalUi.MUTED);
            return;
        }
        if (largeCard) {
            drawLargeMissionCard(context, graphics, record, x, y, w, h, mouseX, mouseY);
            return;
        }
        int bodyX = x + 12;
        int bodyW = w - 30;
        int bodyY = y + 12;
        int actionH = Math.min(actionBarHeight(), Math.max(78, h / 5));
        int actionY = y + h - actionH - 10;
        int bodyH = Math.max(64, actionY - bodyY - 8);
        lastDetailX = bodyX;
        lastDetailY = bodyY;
        lastDetailW = bodyW;
        lastDetailH = bodyH;
        lastDetailScrollable = scrollable;
        lastDetailContentH = detailBodyHeight(context, record, bodyW, false);
        detailScroll = TerminalUi.clampScroll(detailScroll, lastDetailContentH, lastDetailH);
        boolean scissor = scrollable && lastDetailContentH > lastDetailH;
        if (scissor) {
            graphics.enableScissor(bodyX, bodyY, bodyX + bodyW - 4, bodyY + bodyH);
        }
        int cy = drawDetailBody(context, graphics, record, bodyX, bodyY - (scissor ? detailScroll : 0), bodyW, mouseX, mouseY, false);
        lastDetailContentH = Math.max(lastDetailContentH, cy - (bodyY - (scissor ? detailScroll : 0)));
        if (scissor) {
            graphics.disableScissor();
            TerminalUi.scrollbar(graphics, x + w - 9, bodyY, bodyH,
                    detailScroll, Math.max(0, lastDetailContentH - bodyH), chapter().accentColor());
        }
        drawStickyActions(context, graphics, record, bodyX, actionY, bodyW, actionH, mouseX, mouseY);
    }

    private int drawDetailBody(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY, boolean largeCard) {
        int cy = drawBriefingHeader(context, graphics, record, x, y, w, mouseX, mouseY) + 8;
        cy = drawNextStepCallout(context, graphics, record, x, cy, w) + 2;
        TerminalUi.sectionHeader(context, graphics, "REQUIREMENTS", "", x, cy, w - 4, chapter().accentColor());
        cy += 20;
        List<TerminalMissionRequirement> unmet = unmetRequirements(record);
        if (record.definition().requirements().isEmpty()) {
            TerminalUi.line(context, graphics, EMPTY_REQUIREMENTS_COPY,
                    x + 4, cy, w - 12, TerminalUi.MUTED);
            cy += 14;
        } else if (unmet.isEmpty()) {
            TerminalUi.line(context, graphics, MET_REQUIREMENTS_COPY,
                    x + 4, cy, w - 12, TerminalUi.GREEN);
            cy += 14;
        } else {
            for (TerminalMissionRequirement requirement : unmet) {
                cy = drawRequirementRow(context, graphics, requirement, x + 2, cy, w - 10, mouseX, mouseY);
            }
            int hidden = record.definition().requirements().size() - unmet.size();
            if (hidden > 0) {
                TerminalUi.line(context, graphics, hidden + " completed requirement(s) hidden.",
                        x + 4, cy, w - 12, TerminalUi.MUTED);
                cy += 14;
            }
        }
        cy += 4;
        cy = drawRewards(context, graphics, record, x, cy, w, mouseX, mouseY);
        if (!largeCard) {
            TerminalUi.sectionHeader(context, graphics, "FIELD GUIDE", "", x, cy, w - 4, chapter().accentColor());
            cy += 20;
            String guide = record.definition().fieldGuide().isBlank()
                    ? record.definition().briefing()
                    : record.definition().fieldGuide();
            cy = TerminalUi.wrap(context, graphics, previewText(guide, "Field guide signal unavailable.", record.phaseLocked()),
                    x + 2, cy, w - 12, record.phaseLocked() ? TerminalUi.MUTED : TerminalUi.TEXT) + 9;
            if (!record.presentation().relatedIntelKey().isBlank()) {
                TerminalUi.sectionHeader(context, graphics, "RELATED INTEL", "", x, cy, w - 4, chapter().accentColor());
                cy += 20;
                cy = TerminalUi.wrap(context, graphics, intelLabel(record.presentation().relatedIntelKey()),
                        x + 2, cy, w - 12, TerminalUi.MUTED) + 8;
            }
            if (!record.definition().prerequisites().isEmpty()) {
                TerminalUi.sectionHeader(context, graphics, "PREREQUISITES", "", x, cy, w - 4, chapter().accentColor());
                cy += 20;
                for (String prerequisite : record.definition().prerequisites()) {
                    cy = TerminalUi.wrap(context, graphics, "- " + prerequisite, x + 2, cy, w - 12, TerminalUi.MUTED) + 2;
                }
            }
        }
        return cy + 8;
    }

    private void drawLargeMissionCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean locked = record.phaseLocked();
        int color = locked ? TerminalUi.MUTED : statusColor(record.snapshot().status());
        int padX = x + 14;
        int padW = w - 32;
        int cy = y + 14;
        int imageH = Math.min(160, Math.max(140, h / 4));
        TerminalRenderContext recordContext = context.withChapterTheme(record.definition().id().getNamespace(),
                chapter().title(), record.definition().id().getNamespace());
        Identifier art = record.visuals().categoryArt();
        if (art != null) {
            TerminalUi.imagePanel(recordContext, graphics, art, padX, cy, padW, imageH, color, 0.48F, true, TerminalUi.ImageFit.COVER);
        } else {
            TerminalUi.flatHudPanel(recordContext, graphics, padX, cy, padW, imageH, color);
        }
        int overlayY = cy + imageH - 44;
        graphics.fill(padX, overlayY, padX + padW, cy + imageH, 0x8802070C);
        String phaseChip = "PHASE " + (record.definition().phaseOrder() + 1);
        TerminalUi.miniStatusPill(context, graphics, phaseChip, padX + 10, overlayY + 8, 68, color, false);
        String primaryStatus = locked ? "PREVIEW" : compactStatusLabel(record.snapshot());
        String secondaryStatus = locked ? "LOCKED" : roleChipLabel(record.role());
        int pillW = Math.max(82, Math.min(120, padW / 4));
        TerminalUi.missionStatusPill(context, graphics, primaryStatus, padX + padW - pillW - 10, overlayY + 8, pillW);
        if (!secondaryStatus.isBlank()) {
            TerminalUi.miniStatusPill(context, graphics, secondaryStatus,
                    padX + padW - pillW - 10, overlayY + 26, pillW, color, false);
        }
        cy += imageH + 12;
        String title = record.presentation().shortTitle().toUpperCase(Locale.ROOT);
        TerminalUi.line(context, graphics, title, padX, cy, padW, TerminalUi.TEXT);
        cy += 16;
        String objective = previewText(record.presentation().objectiveSummary(), record.definition().briefing(), locked);
        cy = TerminalUi.wrap(context, graphics, objective, padX, cy, padW,
                locked ? TerminalUi.MUTED : TerminalUi.TEXT) + 10;
        cy = drawNextStepCallout(context, graphics, record, padX, cy, padW) + 10;
        TerminalUi.sectionHeader(context, graphics, "REQUIREMENTS", "", padX, cy, padW, chapter().accentColor());
        cy += 22;
        List<TerminalMissionRequirement> unmet = unmetRequirements(record);
        if (record.definition().requirements().isEmpty()) {
            TerminalUi.line(context, graphics, EMPTY_REQUIREMENTS_COPY, padX, cy, padW, TerminalUi.MUTED);
            cy += 14;
        } else if (unmet.isEmpty()) {
            TerminalUi.line(context, graphics, MET_REQUIREMENTS_COPY, padX, cy, padW, TerminalUi.GREEN);
            cy += 14;
        } else {
            for (TerminalMissionRequirement requirement : unmet) {
                int rowH = requirementHeight(context, requirement, padW);
                TerminalUi.flatHudPanel(context, graphics, padX, cy, padW, rowH - 4, color);
                TerminalUi.itemSlot(context, graphics, requirement.icon(), padX + 6, cy + 6, color,
                        TerminalUi.inside(mouseX, mouseY, padX + 6, cy + 6, 20, 20));
                int chipW = Math.max(74, Math.min(92, padW / 4));
                int chipX = padX + padW - chipW - 10;
                int textW = Math.max(38, chipX - (padX + 32) - 8);
                TerminalUi.line(context, graphics, requirement.label(), padX + 32, cy + 6, textW, color);
                String progress = requirement.need() > 0 ? requirement.have() + "/" + requirement.need() : "";
                TerminalUi.missionStatusPill(context, graphics, requirement.satisfied() ? "DONE" : "NEEDED",
                        chipX, cy + 6, chipW);
                TerminalUi.wrap(context, graphics, requirement.detail().isBlank() ? progress : requirement.detail(),
                        padX + 32, cy + 20, Math.max(40, textW), TerminalUi.MUTED);
                cy += rowH;
            }
            int hidden = record.definition().requirements().size() - unmet.size();
            if (hidden > 0) {
                TerminalUi.line(context, graphics, hidden + " completed requirement(s) hidden.",
                        padX, cy, padW, TerminalUi.MUTED);
                cy += 14;
            }
        }
        cy += 8;
        cy = drawRewards(context, graphics, record, padX, cy, padW, mouseX, mouseY);
        int actionH = Math.min(actionBarHeight(), Math.max(78, h / 5));
        int actionY = y + h - actionH - 10;
        if (cy + 24 < actionY) {
            cy += 8;
            String summary = record.phaseLocked()
                    ? "Preview only. Finish the blocker above to unlock actions."
                    : commandSummary(record.snapshot(), record.presentation());
            TerminalUi.line(context, graphics, ACTIONS_SECTION_TITLE, padX, cy, padW, chapter().accentColor());
            cy += 14;
            cy = TerminalUi.wrap(context, graphics, summary, padX, cy, padW, TerminalUi.TEXT) + 10;
        }
        drawStickyActions(context, graphics, record, padX, actionY, padW, actionH, mouseX, mouseY);
    }

    private int drawBriefingHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        boolean locked = record.phaseLocked();
        int color = locked ? TerminalUi.MUTED : statusColor(record.snapshot().status());
        int height = briefingHeaderHeight(record);
        boolean visualHeader = TerminalClientOptions.useVisualAssets();
        String detail = locked
                ? record.phaseLabel() + " / " + emptyFallback(record.presentation().routeHint(), record.definition().category())
                : tagLine(record.definition(), record.presentation(), record.role());
        TerminalRenderContext recordContext = context.withChapterTheme(record.definition().id().getNamespace(),
                chapter().title(), record.definition().id().getNamespace());
        Identifier banner = TerminalUi.chapterBanner(recordContext);
        return TerminalUi.v2HeroHeader(recordContext, graphics,
                banner == null ? record.visuals().categoryArt() : banner,
                TerminalUi.themedMissionIcon(recordContext, record.definition().id(), record.definition().category()),
                TerminalIcon.DEFAULT,
                x, y, w - 4, height,
                record.presentation().shortTitle().toUpperCase(Locale.ROOT),
                detail,
                previewText(record.presentation().objectiveSummary(), record.definition().briefing(), locked),
                locked ? "PREVIEW" : compactStatusLabel(record.snapshot()),
                locked ? "LOCKED" : roleChipLabel(record.role()),
                record.snapshot().progress(), color, locked ? TerminalUi.MUTED : color, visualHeader);
    }

    private int drawNextStepCallout(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w) {
        String hint = record.presentation().nextStep().isBlank()
                ? "This record is visible for planning."
                : record.presentation().nextStep();
        if (record.phaseLocked()) {
            hint = record.phaseUnlockHint();
        }
        return TerminalUi.callout(context, graphics, x, y, w - 4,
                record.phaseLocked() ? "LOCKED PHASE" : "NEXT STEP", hint,
                record.phaseLocked() ? TerminalUi.MUTED : actionHintColor(record.snapshot()));
    }

    private int drawRequirementRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            TerminalMissionRequirement requirement, int x, int y, int w, int mouseX, int mouseY) {
        int color = requirement.satisfied() ? TerminalUi.GREEN : TerminalUi.AMBER;
        int rowH = requirementHeight(context, requirement, w);
        TerminalUi.flatHudPanel(context, graphics, x, y, w, rowH - 4, color);
        TerminalUi.itemSlot(context, graphics, requirement.icon(), x + 6, y + 6, color,
                TerminalUi.inside(mouseX, mouseY, x + 6, y + 6, 20, 20));
        int chipW = Math.max(74, Math.min(92, w / 4));
        int chipX = x + w - chipW - 10;
        int textW = Math.max(38, chipX - (x + 32) - 8);
        TerminalUi.line(context, graphics, requirement.label(), x + 32, y + 6, textW, color);
        String progress = requirement.need() > 0 ? requirement.have() + "/" + requirement.need() : "";
        TerminalUi.missionStatusPill(context, graphics, requirement.satisfied() ? "DONE" : "NEEDED",
                chipX, y + 6, chipW);
        TerminalUi.wrap(context, graphics, requirement.detail().isBlank() ? progress : requirement.detail(),
                x + 32, y + 20, Math.max(40, textW), TerminalUi.MUTED);
        return y + rowH;
    }

    private int drawRewards(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        List<ItemStack> stacks = record.definition().rewards().stream()
                .map(TerminalMissionReward::stack)
                .filter(stack -> !stack.isEmpty())
                .toList();
        boolean hasTextReward = record.definition().rewards().stream()
                .anyMatch(reward -> reward.stack().isEmpty() && !reward.label().isBlank());
        if (stacks.isEmpty() && !hasTextReward) {
            return y;
        }
        TerminalUi.sectionHeader(context, graphics, "REWARDS", "", x, y, w - 4, chapter().accentColor());
        int cy = y + 20;
        cy = TerminalUi.itemGrid(context, graphics, stacks, x + 2, cy, w - 12,
                chapter().accentColor(), mouseX, mouseY) + 3;
        for (TerminalMissionReward reward : record.definition().rewards()) {
            if (!reward.stack().isEmpty()) {
                continue;
            }
            cy = TerminalUi.wrap(context, graphics, reward.label() + ": " + reward.detail(),
                    x + 2, cy, w - 12, TerminalUi.MUTED) + 2;
        }
        return cy + 8;
    }

    private void drawStickyActions(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int h, int mouseX, int mouseY) {
        String summary = record.phaseLocked()
                ? "Preview only. Finish the blocker above to unlock actions."
                : commandSummary(record.snapshot(), record.presentation());
        TerminalUi.flatHudPanel(context, graphics, x, y, w - 4, h,
                TerminalUi.chapterAccent(context, chapter().accentColor()));
        TerminalUi.line(context, graphics, ACTIONS_SECTION_TITLE, x + 8, y + 8, w - 20, chapter().accentColor());
        int summaryBottom = TerminalUi.wrap(context, graphics, summary, x + 8, y + 21, w - 20, TerminalUi.TEXT);
        int buttonY = Math.min(y + h - 28, Math.max(y + 42, summaryBottom + 6));
        if (record.phaseLocked()) {
            if (buttonY + 22 < y + h) {
                TerminalUi.line(context, graphics, record.phaseUnlockHint(), x + 8, buttonY, w - 20, TerminalUi.MUTED);
            }
            return;
        }
        List<TerminalMissionAction> actions = record.snapshot().actions();
        boolean tracking = TerminalPlayerData.get(context.player()).isTracking(tabId, record.definition().id());
        List<CommandButton> buttons = new ArrayList<>();
        for (TerminalMissionAction action : actions) {
            TerminalMissionAction hitAction = action;
            buttons.add(new CommandButton(
                    action.label(),
                    action.enabled(),
                    action.enabled() ? "" : action.disabledReason(),
                    actionIcon(context, action),
                    () -> sendMissionAction(context, record.definition().id(), hitAction.id())));
        }
        buttons.add(new CommandButton(
                tracking ? "UNTRACK" : "TRACK",
                true,
                "",
                TerminalUi.themedActionIcon(context, "track", TerminalVisualAssets.ICON_ACTION_VIEW),
                () -> sendTrackingAction(context, record.definition().id(), tracking)));
        int buttonH = 22;
        int gap = buttons.size() > 3 ? 6 : 8;
        int gaps = Math.max(0, buttons.size() - 1) * gap;
        int buttonW = buttons.size() == 1
                ? Math.min(220, w - 20)
                : Math.min(150, Math.max(72, (w - 16 - gaps) / buttons.size()));
        int bx = x + 8;
        for (CommandButton button : buttons) {
            boolean hover = button.enabled() && TerminalUi.inside(mouseX, mouseY, bx, buttonY, buttonW, buttonH);
            if (button.enabled()) {
                TerminalUi.primaryCommandButton(context, graphics, bx, buttonY, buttonW, buttonH, button.label(),
                        button.icon(), chapter().accentColor(), hover);
            } else {
                TerminalUi.disabledCommandButton(context, graphics, bx, buttonY, buttonW, buttonH,
                        button.label(), button.icon());
            }
            addHitbox(bx, buttonY, buttonW, buttonH, button.enabled(), button.action());
            bx += buttonW + gap;
        }
        String reason = firstDisabledReason(actions);
        if (reason.isBlank() && actions.isEmpty()) {
            reason = "No mission command is available; tracking still pins this record to the Command Deck.";
        }
        if (!reason.isBlank() && buttonY + 39 < y + h) {
            TerminalUi.line(context, graphics, reason, x + 8, buttonY + 29, w - 20, 0xFFC2D4DC);
        }
    }

    private int treePaneHeight(TerminalRenderContext context, MissionRenderState state, int width) {
        return 20 + 22 + treeRowsHeight(context, state);
    }

    private int stackedTreeHeight(TerminalRenderContext context, MissionRenderState state, int width, int viewportHeight) {
        int preferred = Math.max(190, viewportHeight * (width >= 620 ? 52 : 46) / 100);
        int maximum = Math.max(180, viewportHeight - 128);
        return Math.min(treePaneHeight(context, state, width), Math.min(preferred, maximum));
    }

    private int treeRowsHeight(TerminalRenderContext context, MissionRenderState state) {
        int height = 0;
        int phaseH = phaseRowHeight();
        int missionH = missionRowHeight();
        if (!laneMain(state).isEmpty()) {
            height += 20 + laneMain(state).size() * missionH;
        }
        if (!laneReady(state).isEmpty()) {
            height += 20 + laneReady(state).size() * missionH;
        }
        height += 6;
        for (PhaseGroup phase : state.visiblePhases()) {
            height += phaseH + 2;
            if (isPhaseExpanded(phase)) {
                height += phase.records().size() * missionH + 2;
            }
        }
        return height + 6;
    }

    private int detailBodyHeight(TerminalRenderContext context, MissionRecord record, int width, boolean largeCard) {
        if (record == null) {
            return 60;
        }
        if (largeCard) {
            int imageH = 160;
            int height = imageH + 14 + 16 + 14 + 40 + 22;
            if (record.definition().requirements().isEmpty()) {
                height += 14;
            } else {
                List<TerminalMissionRequirement> unmet = unmetRequirements(record);
                if (unmet.isEmpty()) {
                    height += 14;
                }
                for (TerminalMissionRequirement requirement : unmet) {
                    height += requirementHeight(context, requirement, width);
                }
                if (!unmet.isEmpty() && unmet.size() < record.definition().requirements().size()) {
                    height += 14;
                }
            }
            int stackRewards = (int) record.definition().rewards().stream()
                    .filter(reward -> !reward.stack().isEmpty())
                    .count();
            int textRewards = (int) record.definition().rewards().stream()
                    .filter(reward -> reward.stack().isEmpty() && !reward.label().isBlank())
                    .count();
            if (stackRewards > 0 || textRewards > 0) {
                height += 20 + TerminalUi.itemGridHeight(stackRewards, width - 12) + 12;
                height += textRewards * 14;
            }
            height += 22 + 14 + 10;
            return height;
        }
        int height = briefingHeaderHeight(record) + 8;
        height += nextStepCalloutHeight(context, record, width);
        height += 20;
        if (record.definition().requirements().isEmpty()) {
            height += 14;
        } else {
            List<TerminalMissionRequirement> unmet = unmetRequirements(record);
            if (unmet.isEmpty()) {
                height += 14;
            }
            for (TerminalMissionRequirement requirement : unmet) {
                height += requirementHeight(context, requirement, width);
            }
            if (!unmet.isEmpty() && unmet.size() < record.definition().requirements().size()) {
                height += 14;
            }
        }
        String guide = record.definition().fieldGuide().isBlank()
                ? record.definition().briefing()
                : record.definition().fieldGuide();
        height += 20 + TerminalUi.wrappedHeight(context, guide, width - 12) + 18;
        int stackRewards = (int) record.definition().rewards().stream()
                .filter(reward -> !reward.stack().isEmpty())
                .count();
        int textRewards = (int) record.definition().rewards().stream()
                .filter(reward -> reward.stack().isEmpty() && !reward.label().isBlank())
                .count();
        if (stackRewards > 0 || textRewards > 0) {
            height += 20 + TerminalUi.itemGridHeight(stackRewards, width - 12) + 12;
            height += textRewards * 14;
        }
        if (!record.presentation().relatedIntelKey().isBlank()) {
            height += 20 + TerminalUi.wrappedHeight(context, record.presentation().relatedIntelKey(), width - 12) + 8;
        }
        if (!record.definition().prerequisites().isEmpty()) {
            height += 20;
            for (String prerequisite : record.definition().prerequisites()) {
                height += TerminalUi.wrappedHeight(context, "- " + prerequisite, width - 12) + 2;
            }
        }
        return height + 8;
    }

    private int briefingHeaderHeight(MissionRecord record) {
        boolean visualHeader = TerminalClientOptions.useVisualAssets();
        return Math.max(92, (visualHeader ? 104 : 98) - densityStep() * 5);
    }

    private int nextStepCalloutHeight(TerminalRenderContext context, MissionRecord record, int width) {
        String hint = record.presentation().nextStep().isBlank()
                ? "This record is visible for planning."
                : record.presentation().nextStep();
        if (record.phaseLocked()) {
            hint = record.phaseUnlockHint();
        }
        return Math.max(40, 27 + TerminalUi.wrappedHeight(context, hint, Math.max(40, width - 24))) + 5;
    }

    private int requirementHeight(TerminalRenderContext context, TerminalMissionRequirement requirement, int width) {
        String detail = requirement.detail();
        if (requirement.need() > 0 && detail.isBlank()) {
            detail = requirement.have() + "/" + requirement.need();
        }
        int chipW = Math.max(74, Math.min(92, width / 4));
        int detailH = TerminalUi.wrappedHeight(context, detail, Math.max(40, width - chipW - 54));
        return Math.max(38, 24 + detailH) + 5;
    }

    private static List<TerminalMissionRequirement> unmetRequirements(MissionRecord record) {
        if (record == null) {
            return List.of();
        }
        return record.definition().requirements().stream()
                .filter(requirement -> !requirement.satisfied())
                .toList();
    }

    private List<MissionRecord> laneMain(MissionRenderState state) {
        return state.focusRecord() == null ? List.of() : List.of(state.focusRecord());
    }

    private List<MissionRecord> laneReady(MissionRenderState state) {
        return state.visibleRecords().stream()
                .filter(record -> !record.phaseLocked())
                .filter(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)
                .filter(record -> state.focusRecord() == null || !record.id().equals(state.focusRecord().id()))
                .limit(3)
                .toList();
    }

    private List<MissionRecord> laneOptional(MissionRenderState state) {
        return state.visibleRecords().stream()
                .filter(record -> !record.phaseLocked())
                .filter(record -> record.role() == TerminalMissionRole.OPTIONAL)
                .filter(record -> !isDone(record.snapshot().status()))
                .filter(record -> state.focusRecord() == null || !record.id().equals(state.focusRecord().id()))
                .limit(3)
                .toList();
    }

    private void normalizeSelection(MissionRenderState state) {
        if (state.visibleRecords().isEmpty()) {
            selectedMissionId = null;
            return;
        }
        if (selectedMissionId != null && state.visibleRecords().stream().anyMatch(record -> record.id().equals(selectedMissionId))) {
            return;
        }
        MissionRecord focus = state.focusRecord() == null
                || state.visibleRecords().stream().noneMatch(record -> record.id().equals(state.focusRecord().id()))
                        ? state.visibleRecords().get(0)
                        : state.focusRecord();
        selectedMissionId = focus.id();
    }

    private boolean selectRelative(MissionRenderState state, int offset) {
        List<MissionRecord> rows = navigationRecords(state);
        if (rows.isEmpty()) {
            return false;
        }
        int index = 0;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).id().equals(selectedMissionId)) {
                index = i;
                break;
            }
        }
        selectMission(rows.get(Math.floorMod(index + offset, rows.size())).id(), true);
        return true;
    }

    private boolean activateSelectedAction(TerminalRenderContext context, MissionRenderState state) {
        MissionRecord selected = selectedRecord(state);
        if (selected == null || selected.phaseLocked()) {
            return false;
        }
        for (TerminalMissionAction action : selected.snapshot().actions()) {
            if (action.enabled()) {
                sendMissionAction(context, selected.definition().id(), action.id());
                return true;
            }
        }
        return false;
    }

    private void sendMissionAction(TerminalRenderContext context, Identifier missionId, String actionId) {
        context.sendAction(tabId, TerminalMissionActions.MISSION_ACTION,
                TerminalMissionActions.payload(chapter().id(), missionId, actionId));
        invalidateStateCache();
    }

    private void sendTrackingAction(TerminalRenderContext context, Identifier missionId, boolean clear) {
        context.sendAction(tabId, TerminalMissionActions.TRACK_MISSION,
                TerminalMissionActions.trackingPayload(tabId, chapter().id(), missionId, clear));
        invalidateStateCache();
    }

    private List<MissionRecord> navigationRecords(MissionRenderState state) {
        List<MissionRecord> rows = new ArrayList<>();
        for (PhaseGroup phase : state.visiblePhases()) {
            if (isPhaseExpanded(phase)) {
                rows.addAll(phase.records());
            }
        }
        return rows.isEmpty() ? state.visibleRecords() : rows;
    }

    private MissionRecord selectedRecord(MissionRenderState state) {
        if (selectedMissionId == null) {
            return null;
        }
        return state.allRecords().stream()
                .filter(record -> state.visibleRecords().stream().anyMatch(visible -> visible.id().equals(record.id())))
                .filter(record -> record.id().equals(selectedMissionId))
                .findFirst()
                .orElse(null);
    }

    private MissionRecord focusRecord(List<MissionRecord> records) {
        List<MissionRecord> unlocked = records.stream()
                .filter(record -> !record.phaseLocked())
                .toList();
        return unlocked.stream()
                .filter(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)
                .findFirst()
                .or(() -> unlocked.stream()
                        .filter(record -> record.role() == TerminalMissionRole.MAIN)
                        .filter(record -> record.snapshot().status() == TerminalMissionStatus.UNLOCKED)
                        .findFirst())
                .or(() -> unlocked.stream()
                        .filter(record -> record.role() == TerminalMissionRole.MAIN)
                        .filter(record -> !isDone(record.snapshot().status()))
                        .findFirst())
                .or(() -> unlocked.stream()
                        .filter(record -> !isDone(record.snapshot().status()))
                        .findFirst())
                .orElse(records.isEmpty() ? null : records.get(0));
    }

    private void selectMission(Identifier missionId, boolean focusTree) {
        if (missionId == null) {
            return;
        }
        if (!missionId.equals(selectedMissionId)) {
            selectedMissionId = missionId;
            detailScroll = 0;
            lastDetailMissionId = missionId;
            invalidateStateCache();
        }
        if (focusTree) {
            pendingTreeFocus = TreeFocusMode.ENSURE_SELECTED_VISIBLE;
        }
    }

    private void syncDetailScrollWithSelection() {
        if (selectedMissionId == null) {
            lastDetailMissionId = null;
            detailScroll = 0;
            return;
        }
        if (!selectedMissionId.equals(lastDetailMissionId)) {
            detailScroll = 0;
            lastDetailMissionId = selectedMissionId;
        }
    }

    private void focusTreeOnSelection(MissionRenderState state) {
        if (pendingTreeFocus == TreeFocusMode.NONE || selectedMissionId == null || state.visibleRecords().isEmpty()) {
            return;
        }
        int rowY = selectedRowOffset(state);
        if (rowY < 0) {
            pendingTreeFocus = TreeFocusMode.NONE;
            return;
        }
        if (pendingTreeFocus == TreeFocusMode.ALIGN_SELECTED_TOP) {
            treeScroll = TerminalUi.clampScroll(rowY, lastTreeContentH, lastTreeH);
        } else if (rowY < treeScroll) {
            treeScroll = Math.max(0, rowY - TREE_FOCUS_EXTRA);
        } else if (rowY + missionRowHeight() > treeScroll + lastTreeH) {
            treeScroll = Math.max(0, rowY + missionRowHeight() - lastTreeH + TREE_FOCUS_EXTRA);
        }
        pendingTreeFocus = TreeFocusMode.NONE;
    }

    private int selectedRowOffset(MissionRenderState state) {
        int cy = 0;
        int phaseH = phaseRowHeight();
        int missionH = missionRowHeight();
        cy += treeRowsHeightForGuidedLanes(state) + 6;
        for (PhaseGroup phase : state.visiblePhases()) {
            cy += phaseH + 2;
            if (!isPhaseExpanded(phase)) {
                continue;
            }
            for (MissionRecord record : phase.records()) {
                if (record.id().equals(selectedMissionId)) {
                    return cy;
                }
                cy += missionH;
            }
            cy += 2;
        }
        return -1;
    }

    private int treeRowsHeightForGuidedLanes(MissionRenderState state) {
        int missionH = missionRowHeight();
        int height = 0;
        if (!laneMain(state).isEmpty()) {
            height += 20 + laneMain(state).size() * missionH;
        }
        if (!laneReady(state).isEmpty()) {
            height += 20 + laneReady(state).size() * missionH;
        }
        return height;
    }

    private boolean isPhaseExpanded(PhaseGroup phase) {
        if (collapsedPhases.contains(phase.id())) {
            return false;
        }
        if (expandedPhases.contains(phase.id())) {
            return true;
        }
        if (phase.records().stream().anyMatch(record -> record.id().equals(selectedMissionId))) {
            return true;
        }
        if (phase.locked()) {
            return false;
        }
        if (phase.records().stream().anyMatch(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)) {
            return true;
        }
        return !phase.complete();
    }

    private void togglePhase(PhaseGroup phase) {
        if (isPhaseExpanded(phase)) {
            expandedPhases.remove(phase.id());
            collapsedPhases.add(phase.id());
        } else {
            collapsedPhases.remove(phase.id());
            expandedPhases.add(phase.id());
        }
        invalidateStateCache();
    }

    private PhaseModel buildPhaseModel(List<MissionRecord> records) {
        Map<String, List<MissionRecord>> grouped = new LinkedHashMap<>();
        for (MissionRecord record : records) {
            grouped.computeIfAbsent(record.phaseKey(), ignored -> new ArrayList<>()).add(record);
        }
        List<PhaseGroup> sorted = new ArrayList<>();
        for (List<MissionRecord> group : grouped.values()) {
            MissionRecord first = group.get(0);
            sorted.add(new PhaseGroup(
                    first.phaseKey(),
                    "",
                    first.definition().phaseTitle(),
                    first.definition().phaseOrder(),
                    -1,
                    false,
                    phaseComplete(group),
                    "",
                    group));
        }
        sorted.sort(Comparator.comparingInt(PhaseGroup::order).thenComparing(PhaseGroup::id));
        List<PhaseGroup> phases = new ArrayList<>();
        Map<String, PhaseGroup> byId = new LinkedHashMap<>();
        boolean unlocked = true;
        PhaseGroup blocking = null;
        for (int i = 0; i < sorted.size(); i++) {
            PhaseGroup seed = sorted.get(i);
            boolean locked = !unlocked;
            String label = String.format(Locale.ROOT, "Phase %02d", i);
            String hint = locked && blocking != null
                    ? "Complete " + blocking.label() + " main objectives to unlock"
                    : "";
            PhaseGroup phase = new PhaseGroup(seed.id(), label, seed.contextTitle(), seed.order(), i,
                    locked, seed.complete(), hint, seed.records());
            phases.add(phase);
            byId.put(phase.id(), phase);
            if (!locked && !phase.complete()) {
                unlocked = false;
                blocking = phase;
            }
        }
        return new PhaseModel(List.copyOf(phases), Map.copyOf(byId));
    }

    private List<PhaseGroup> visiblePhases(PhaseModel phaseModel, List<MissionRecord> visibleRecords) {
        Map<String, List<MissionRecord>> grouped = new LinkedHashMap<>();
        for (MissionRecord record : visibleRecords) {
            grouped.computeIfAbsent(record.phaseKey(), ignored -> new ArrayList<>()).add(record);
        }
        List<PhaseGroup> visible = new ArrayList<>();
        for (PhaseGroup phase : phaseModel.phases()) {
            List<MissionRecord> records = grouped.get(phase.id());
            if (records != null && !records.isEmpty()) {
                visible.add(phase.withRecords(records));
            }
        }
        return List.copyOf(visible);
    }

    private List<MissionRecord> recordsForPhase(List<MissionRecord> records, String phaseId) {
        return records.stream()
                .filter(record -> record.phaseKey().equals(phaseId))
                .toList();
    }

    private int completedCount(List<MissionRecord> records) {
        int count = 0;
        for (MissionRecord record : records) {
            if (isDone(record.snapshot().status())) {
                count++;
            }
        }
        return count;
    }

    private static boolean phaseComplete(List<MissionRecord> records) {
        for (MissionRecord record : records) {
            if (record.role() == TerminalMissionRole.MAIN && !isGateComplete(record.snapshot().status())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isGateComplete(TerminalMissionStatus status) {
        return status == TerminalMissionStatus.COMPLETED
                || status == TerminalMissionStatus.CLAIMED
                || status == TerminalMissionStatus.CLAIMABLE;
    }

    private static String phaseKey(TerminalMissionDefinition definition) {
        return definition.phaseOrder() + "::" + definition.phaseId();
    }

    private static int statusColor(TerminalMissionStatus status) {
        return switch (status) {
            case COMPLETED, CLAIMED, CLAIMABLE -> TerminalUi.GREEN;
            case UNLOCKED -> TerminalUi.AMBER;
            case LOCKED, VIEW_ONLY -> TerminalUi.MUTED;
        };
    }

    private static int missionTitleColor(TerminalMissionStatus status, boolean selected, int statusColor) {
        if (selected) {
            return TerminalUi.TEXT;
        }
        return switch (status) {
            case CLAIMABLE, UNLOCKED -> statusColor;
            case COMPLETED, CLAIMED -> TerminalUi.GREEN;
            case LOCKED, VIEW_ONLY -> TerminalUi.MUTED;
        };
    }

    private static String compactStatusLabel(TerminalMissionSnapshot snapshot) {
        return switch (snapshot.status()) {
            case UNLOCKED -> "ACTIVE";
            case CLAIMABLE -> "READY TO CLAIM";
            case COMPLETED, CLAIMED -> "DONE";
            case VIEW_ONLY -> "REFERENCE";
            case LOCKED -> "LOCKED";
        };
    }

    private static int actionHintColor(TerminalMissionSnapshot snapshot) {
        return switch (snapshot.status()) {
            case COMPLETED, CLAIMABLE, CLAIMED -> TerminalUi.GREEN;
            case UNLOCKED -> TerminalUi.AMBER;
            case LOCKED, VIEW_ONLY -> TerminalUi.MUTED;
        };
    }

    private static String commandSummary(TerminalMissionSnapshot snapshot, TerminalMissionPresentation presentation) {
        return switch (snapshot.status()) {
            case CLAIMABLE -> "Reward cache is ready. Claim it here before moving on.";
            case COMPLETED, CLAIMED -> "Protocol complete. Any pending cache remains available here.";
            case UNLOCKED -> presentation.nextStep();
            case VIEW_ONLY -> "Reference record. Track it if you want it pinned to the Command Deck.";
            case LOCKED -> snapshot.unlockReason().isBlank() ? presentation.nextStep() : snapshot.unlockReason();
        };
    }

    private static String previewText(String value, String fallback, boolean locked) {
        String text = emptyFallback(value, fallback);
        return locked ? mysticCipher(text) : text;
    }

    private static String mysticCipher(String text) {
        String alphabet = "AZURETHOMNIVKSLY";
        StringBuilder cipher = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                cipher.append(alphabet.charAt((c - 'A' + i) % alphabet.length()));
            } else if (c >= 'a' && c <= 'z') {
                cipher.append(Character.toLowerCase(alphabet.charAt((c - 'a' + i) % alphabet.length())));
            } else if (c >= '0' && c <= '9') {
                cipher.append((char) ('0' + (c - '0' + i) % 10));
            } else {
                cipher.append(c);
            }
        }
        return cipher.toString();
    }

    private static String tagLine(TerminalMissionDefinition mission,
            TerminalMissionPresentation presentation, TerminalMissionRole role) {
        List<String> tags = new ArrayList<>();
        if (role == TerminalMissionRole.OPTIONAL) {
            tags.add("Optional");
        } else if (role == TerminalMissionRole.REFERENCE) {
            tags.add("Reference");
        }
        if (!presentation.routeHint().isBlank()) {
            tags.add(presentation.routeHint());
        }
        tags.addAll(presentation.tags());
        if (tags.isEmpty()) {
            tags.add(emptyFallback(mission.category(), "Mission"));
            tags.add(emptyFallback(mission.difficulty(), "Standard"));
        }
        return String.join(" / ", tags);
    }

    private static String roleLabel(TerminalMissionRole role) {
        return switch (role) {
            case MAIN -> "Main progression";
            case OPTIONAL -> "Optional / nonblocking";
            case REFERENCE -> "Reference";
        };
    }

    private static String roleChipLabel(TerminalMissionRole role) {
        return switch (role) {
            case MAIN -> "MAIN";
            case OPTIONAL -> "OPTIONAL";
            case REFERENCE -> "REFERENCE";
        };
    }

    private static String firstDisabledReason(List<TerminalMissionAction> actions) {
        for (TerminalMissionAction action : actions) {
            if (!action.enabled() && !action.disabledReason().isBlank()) {
                return action.label() + ": " + action.disabledReason();
            }
        }
        return "";
    }

    private static Identifier actionIcon(TerminalRenderContext context, TerminalMissionAction action) {
        String value = ((action == null ? "" : action.id()) + " " + (action == null ? "" : action.label()))
                .toLowerCase(Locale.ROOT);
        if (value.contains("claim") || value.contains("reward")) {
            return TerminalUi.themedActionIcon(context, "claim", TerminalVisualAssets.ICON_ACTION_CLAIM);
        }
        if (value.contains("turn") || value.contains("submit") || value.contains("finish")) {
            return TerminalUi.themedActionIcon(context, "turn_in", TerminalVisualAssets.ICON_ACTION_TURN_IN);
        }
        if (value.contains("scan")) {
            return TerminalUi.themedActionIcon(context, "scan", TerminalVisualAssets.ICON_ACTION_SCAN);
        }
        if (value.contains("open")) {
            return TerminalUi.themedActionIcon(context, "open", TerminalVisualAssets.ICON_ACTION_OPEN_ROADMAP);
        }
        return TerminalUi.themedActionIcon(context, "view", TerminalVisualAssets.ICON_ACTION_VIEW);
    }

    private static String intelLabel(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String cleaned = key;
        int colon = cleaned.indexOf(':');
        if (colon >= 0 && colon + 1 < cleaned.length()) {
            cleaned = cleaned.substring(colon + 1);
        }
        if (cleaned.startsWith("ashfall_")) {
            cleaned = cleaned.substring("ashfall_".length());
        }
        String[] words = cleaned.replace('_', ' ').split(" ");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                label.append(word.substring(1));
            }
        }
        return label.length() == 0 ? key : label.toString();
    }

    private static boolean isDone(TerminalMissionStatus status) {
        return status == TerminalMissionStatus.COMPLETED || status == TerminalMissionStatus.CLAIMED;
    }

    private static boolean visible(int y, int h, int viewportY, int viewportH) {
        return y + h >= viewportY && y <= viewportY + viewportH;
    }

    private static String emptyFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void addHitbox(int x, int y, int w, int h, boolean enabled, Runnable action) {
        hitboxes.add(new Hitbox(x, y, w, h, enabled, action));
    }

    private void invalidateStateCache() {
        cachedState = null;
        cachedStateKey = null;
        staleServedKey = null;
        cachedStateFrame = -1L;
    }

    private record CacheKey(
            String providerName,
            Identifier tabId,
            UUID playerId,
            int widthBucket,
            int refreshTick) {
    }

    private record MissionRenderState(
            List<MissionRecord> allRecords,
            List<MissionRecord> visibleRecords,
            List<PhaseGroup> allPhases,
            List<PhaseGroup> visiblePhases,
            MissionRecord focusRecord,
            int completedCount) {
    }

    private record MissionRecord(
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot,
            TerminalMissionPresentation presentation,
            TerminalMissionVisuals visuals,
            TerminalMissionRole role,
            String phaseKey,
            String phaseLabel,
            String phaseContext,
            boolean phaseLocked,
            String phaseUnlockHint) {
        MissionRecord(
                TerminalMissionDefinition definition,
                TerminalMissionSnapshot snapshot,
                TerminalMissionPresentation presentation,
            TerminalMissionVisuals visuals,
            TerminalMissionRole role) {
            this(definition, snapshot, presentation, visuals, role,
                    TerminalMissionBrowser.phaseKey(definition), "", definition.phaseTitle(), false, "");
        }

        Identifier id() {
            return definition.id();
        }

        MissionRecord withPhase(PhaseGroup phase) {
            if (phase == null) {
                return this;
            }
            return new MissionRecord(definition, snapshot, presentation, visuals, role,
                    phase.id(), phase.label(), phase.contextTitle(), phase.locked(), phase.unlockHint());
        }
    }

    private record PhaseGroup(
            String id,
            String label,
            String contextTitle,
            int order,
            int displayIndex,
            boolean locked,
            boolean complete,
            String unlockHint,
            List<MissionRecord> records) {
        PhaseGroup withRecords(List<MissionRecord> records) {
            return new PhaseGroup(id, label, contextTitle, order, displayIndex, locked, complete, unlockHint, records);
        }

        String stateLabel() {
            if (locked) {
                return "LOCKED";
            }
            return complete ? "COMPLETE" : "ACTIVE";
        }
    }

    private record PhaseModel(List<PhaseGroup> phases, Map<String, PhaseGroup> byId) {
        PhaseGroup phase(String id) {
            return byId.get(id);
        }
    }

    private record Hitbox(int x, int y, int w, int h, boolean enabled, Runnable action) {
    }

    private record CommandButton(String label, boolean enabled, String disabledReason, Identifier icon, Runnable action) {
    }

    private enum TreeFocusMode {
        NONE,
        ALIGN_SELECTED_TOP,
        ENSURE_SELECTED_VISIBLE
    }

}
