package com.knoxhack.echoterminal.client.screen;

import com.knoxhack.echoterminal.EchoTerminalClient;
import com.knoxhack.echoterminal.api.TerminalRenderCache;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalThemedSounds;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalLayoutProfile;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.theme.TerminalThemeContext;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class EchoTerminalScreen extends AbstractContainerScreen<EchoTerminalMenu> {
    private static final Identifier OVERVIEW_TAB =
            Identifier.fromNamespaceAndPath("echoterminal", "overview");
    private static Identifier rememberedTabId;

    private final TerminalScreenTheme theme;
    private final Map<Identifier, Integer> tabScroll = new HashMap<>();

    private List<TerminalTab> cachedTabs = List.of();
    private TerminalNavigationModel navigationModel = TerminalNavigationModel.of(List.of());
    private int activeTab;
    private boolean initialTabSelected;
    private int ticks;
    private TerminalLayoutProfile layoutProfile = TerminalLayoutProfile.MEDIUM_CAROUSEL;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;
    private int navX;
    private int navY;
    private int navW;
    private int navH;
    private int groupRailX;
    private int groupRailY;
    private int groupRailW;
    private int groupRailH;
    private int pageRailX;
    private int pageRailY;
    private int pageRailW;
    private int pageRailH;
    private boolean commandStackNavigation;
    private boolean sidebarNavigation;
    private boolean commandStackCollapsed;
    private int commandStackScroll;
    private String commandStackScrollGroup = "";
    private int collapseToggleX;
    private int collapseToggleY;
    private int collapseToggleW;
    private int collapseToggleH;

    public record LayoutMetrics(
            int panelX,
            int panelY,
            int panelW,
            int panelH,
            int contentX,
            int contentY,
            int contentW,
            int contentH,
            int renderContentX,
            int renderContentY,
            int renderContentW,
            int renderContentH,
            int groupRailX,
            int groupRailY,
            int groupRailW,
            int groupRailH,
            int collapseToggleX,
            int collapseToggleY,
            int collapseToggleW,
            int collapseToggleH,
            int shellHeaderH,
            int shellFooterH,
            TerminalLayoutProfile layoutProfile) {
    }

    public EchoTerminalScreen(EchoTerminalMenu menu, Inventory playerInventory, Component title) {
        this(menu, playerInventory, title, TerminalScreenTheme.modular());
    }

    public EchoTerminalScreen(EchoTerminalMenu menu, Inventory playerInventory, Component title, TerminalScreenTheme theme) {
        super(menu, playerInventory, title);
        this.theme = theme == null ? TerminalScreenTheme.modular() : theme;
    }

    public static LayoutMetrics layoutMetricsForTests(
            int screenWidth,
            int screenHeight,
            TerminalScreenTheme theme,
            TerminalClientOptions.InterfaceDensity density,
            TerminalClientOptions.TerminalZoom zoom,
            boolean commandStackCollapsed) {
        TerminalScreenTheme resolvedTheme = theme == null ? TerminalScreenTheme.modular() : theme;
        return computeLayoutMetrics(screenWidth, screenHeight, resolvedTheme.panelMaxWidth(),
                resolvedTheme.panelMaxHeight(), density, zoom, commandStackCollapsed);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ticks++;
        TerminalUi.applyThemeGlobals(TerminalClientOptions.currentTheme());
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        clampCommandStackScroll();

        drawChrome(graphics, tabs, mouseX, mouseY);
        drawBody(graphics, tabs, mouseX, mouseY, partialTick);
        drawFooter(graphics, tabs);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        clampCommandStackScroll();
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        if (tab != null && tab.keyPressed(contextFor(tab, scrollFor(tab)), event)) {
            return true;
        }

        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || EchoTerminalClient.OPEN_TERMINAL_KEY.matches(event)) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (tab != null && key == GLFW.GLFW_KEY_PAGE_UP) {
            setScroll(tab, scrollFor(tab) - Math.max(36, contentH - 34));
            return true;
        }
        if (tab != null && key == GLFW.GLFW_KEY_PAGE_DOWN) {
            setScroll(tab, scrollFor(tab) + Math.max(36, contentH - 34));
            return true;
        }
        if (key == GLFW.GLFW_KEY_UP && !tabs.isEmpty()) {
            return selectGroupOffset(tabs, -1);
        }
        if (key == GLFW.GLFW_KEY_DOWN && !tabs.isEmpty()) {
            return selectGroupOffset(tabs, 1);
        }
        if (key == GLFW.GLFW_KEY_LEFT && !tabs.isEmpty()) {
            selectTab(Math.floorMod(activeTab - 1, tabs.size()), tabs);
            return true;
        }
        if ((key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_TAB) && !tabs.isEmpty()) {
            selectTab(Math.floorMod(activeTab + 1, tabs.size()), tabs);
            return true;
        }
        if (key == GLFW.GLFW_KEY_HOME && !tabs.isEmpty()) {
            selectTab(0, tabs);
            return true;
        }
        if (key == GLFW.GLFW_KEY_END && !tabs.isEmpty()) {
            selectTab(tabs.size() - 1, tabs);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        clampCommandStackScroll();
        if (handleNavigationClick(tabs, event.x(), event.y())) {
            return true;
        }

        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        if (tab != null && TerminalUi.inside(event.x(), event.y(), contentX, contentY, contentW, contentH)
                && tab.mouseClicked(contextFor(tab, scrollFor(tab)), event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        clampCommandStackScroll();
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        if (tab != null && tab.mouseDragged(contextFor(tab, scrollFor(tab)),
                event.x(), event.y(), event.button(), dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        clampCommandStackScroll();
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        if (tab != null && tab.mouseReleased(contextFor(tab, scrollFor(tab)),
                event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double deltaY) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        if (handleCommandStackMouseScroll(tabs, mouseX, mouseY, deltaY)) {
            return true;
        }
        clampCommandStackScroll();
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        if (tab == null || !TerminalUi.inside(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
            return false;
        }
        int current = scrollFor(tab);
        TerminalRenderContext context = contextFor(tab, current);
        if (tab.mouseScrolled(context, mouseX, mouseY, deltaY)) {
            clampScroll(tab);
            return true;
        }
        int next = current - (int) Math.round(deltaY * 18.0D);
        setScroll(tab, next);
        return true;
    }

    public boolean handleCharTyped(CharacterEvent event) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        clampCommandStackScroll();
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        return tab != null && tab.charTyped(contextFor(tab, scrollFor(tab)), event);
    }

    private List<TerminalTab> tabs() {
        List<TerminalTab> current = TerminalTabRegistry.tabs();
        if (current != cachedTabs) {
            cachedTabs = current;
            navigationModel = TerminalNavigationModel.of(current);
        }
        return navigationModel.tabs();
    }

    private void drawChrome(GuiGraphicsExtractor graphics, List<TerminalTab> tabs, int mouseX, int mouseY) {
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        TerminalTabChrome chrome = tab == null ? null : tab.chrome();
        String status = theme.statusProvider().statusLine(Minecraft.getInstance());
        String meta = Minecraft.getInstance().player == null ? "LINK OFFLINE" : "LINK ONLINE";
        if (chrome != null && !chrome.summary().isBlank()) {
            meta += "  |  " + chrome.summary();
        }
        TerminalRenderContext chromeContext = contextFor(tab, tab == null ? 0 : scrollFor(tab));
        TerminalUi.appShellBackdrop(chromeContext, graphics, panelX, panelY, panelW, panelH, chromeColor(tab));
        TerminalUi.topMetaBar(chromeContext, graphics, font, panelX, panelY, panelW,
                shellHeaderHeight(), theme.title(), status, meta, chromeColor(tab));

        drawSidebarNavigation(graphics, tabs, mouseX, mouseY);
    }

    private void drawSidebarNavigation(GuiGraphicsExtractor graphics, List<TerminalTab> tabs, int mouseX, int mouseY) {
        drawCommandStackNavigation(graphics, tabs, mouseX, mouseY);
    }

    private void drawCommandStackNavigation(GuiGraphicsExtractor graphics, List<TerminalTab> tabs, int mouseX, int mouseY) {
        String activeGroup = navigationModel.activeGroup(activeTab);
        int accent = navigationModel.groupAccent(activeGroup, theme.accentColor());
        if (commandStackCollapsed) {
            drawCollapsedCommandStack(graphics, tabs, mouseX, mouseY, activeGroup, accent);
            return;
        }
        clampCommandStackScroll();
        TerminalRenderContext renderContext = tabs.isEmpty() ? contextFor(null, 0) : contextFor(tabs.get(activeTab), 0);
        TerminalUi.commandStackPanel(renderContext, graphics, font, groupRailX, groupRailY, groupRailW, groupRailH, accent);
        drawCollapseToggle(renderContext, graphics, mouseX, mouseY, accent);
        boolean compact = groupRailW < 190;
        int cy = commandStackContentY() - commandStackScroll;
        int rowH = commandRowHeight();
        int gap = commandRowGap();
        int viewportTop = commandStackViewportTop();
        int viewportH = commandStackViewportHeight();
        boolean railHovered = commandStackViewportContains(mouseX, mouseY);
        int groupInset = railInset(8);
        int groupTrim = railTrim(16);
        graphics.enableScissor(groupRailX, viewportTop, groupRailX + groupRailW,
                commandStackViewportBottom());
        for (String group : navigationModel.groups()) {
            boolean active = group.equals(activeGroup);
            int groupColor = navigationModel.groupAccent(group, theme.accentColor());
            int groupH = commandGroupHeight(compact);
            boolean groupHover = railHovered
                    && TerminalUi.inside(mouseX, mouseY, groupRailX + groupInset, cy, groupRailW - groupTrim, groupH);
            TerminalUi.commandStackGroupButton(renderContext, graphics, font, groupRailX + groupInset, cy,
                    groupRailW - groupTrim, groupH,
                    TerminalIcon.fromGroup(group), TerminalUi.themedGroupIcon(renderContext, group),
                    navigationModel.groupLabel(group), active, groupHover, groupColor);
            cy += groupH + zoomed(active ? 5 : 3, active ? 3 : 2);
            if (active) {
                cy = drawExpandedGroupPages(graphics, group, mouseX, mouseY, cy, rowH, gap, compact, renderContext);
                cy += zoomed(5, 3);
            }
        }
        graphics.disableScissor();
        TerminalUi.scrollbar(renderContext, graphics, groupRailX + groupRailW - zoomed(7, 5), viewportTop, viewportH,
                commandStackScroll, maxCommandStackScroll(), accent, railHovered);
        int diagnosticH = diagnosticRailHeight();
        TerminalUi.diagnosticRail(renderContext, graphics, font, groupRailX + railInset(10),
                groupRailY + groupRailH - diagnosticH - zoomed(11, 8), groupRailW - railTrim(20), diagnosticH,
                Minecraft.getInstance().player != null, accent);
    }

    private int drawExpandedGroupPages(GuiGraphicsExtractor graphics, String group, int mouseX, int mouseY,
            int cy, int rowH, int gap, boolean compact, TerminalRenderContext renderContext) {
        for (TerminalNavigationModel.IndexedTab entry : navigationModel.directTabsInGroup(group)) {
            cy = drawNavigationPage(graphics, entry, mouseX, mouseY, cy, rowH, gap, compact, 14, 22);
        }
        String activeChapter = navigationModel.activeChapterId(activeTab);
        for (TerminalNavigationModel.ChapterGroup chapter : navigationModel.chaptersInGroup(group)) {
            boolean selectedChapter = chapter.id().equals(activeChapter);
            int chapterInset = railInset(14);
            int chapterTrim = railTrim(22);
            boolean chapterHover = commandStackViewportContains(mouseX, mouseY)
                    && TerminalUi.inside(mouseX, mouseY, groupRailX + chapterInset, cy,
                            groupRailW - chapterTrim, rowH);
            String label = compact ? chapter.iconLabel() : chapter.title();
            String summary = chapterRailSummary(chapter, compact);
            TerminalIcon chapterIcon = TerminalIcon.fromTitle(chapter.title());
            if (chapterIcon == TerminalIcon.DEFAULT) {
                chapterIcon = TerminalIcon.ADDONS;
            }
            TerminalRenderContext chapterContext = renderContext.withChapterTheme(chapter.id(), chapter.title(), chapter.id());
            TerminalUi.commandPageButton(chapterContext, graphics, font, groupRailX + chapterInset, cy,
                    groupRailW - chapterTrim, rowH,
                    chapterIcon, TerminalUi.themedIcon(chapterContext, com.knoxhack.echoterminal.api.theme.TerminalIconKey.chapter(chapter.id()),
                            TerminalUi.themedPageIcon(renderContext, chapter.title())), label, summary,
                    selectedChapter, chapterHover, chapter.accent());
            cy += rowH + gap;
            if (selectedChapter) {
                int childRailX = groupRailX + railInset(18);
                int childRailH = chapter.tabs().size() * (rowH + gap) - gap;
                if (childRailH > 0) {
                    TerminalUi.navigationSpine(chapterContext, graphics, childRailX, cy, childRailH, chapter.accent());
                }
                for (TerminalNavigationModel.IndexedTab entry : chapter.tabs()) {
                    cy = drawNavigationPage(graphics, entry, mouseX, mouseY, cy, rowH, gap, compact, 24, 34);
                }
            }
        }
        return cy;
    }

    private static String chapterRailSummary(TerminalNavigationModel.ChapterGroup chapter, boolean compact) {
        if (compact || chapter == null) {
            return "";
        }
        String title = chapter.title() == null ? "" : chapter.title();
        if (title.startsWith("Chapter ")) {
            return "Story chapter";
        }
        if (title.startsWith("Optional:")) {
            return "Side route";
        }
        int count = chapter.tabs().size();
        return count == 1 ? "Linked page" : count + " linked pages";
    }

    private int drawNavigationPage(GuiGraphicsExtractor graphics, TerminalNavigationModel.IndexedTab entry,
            int mouseX, int mouseY, int cy, int rowH, int gap, boolean compact, int inset, int widthTrim) {
        TerminalTab tab = entry.tab();
        boolean selected = entry.index() == activeTab;
        int scaledInset = railInset(inset);
        int scaledTrim = railTrim(widthTrim);
        boolean hover = commandStackViewportContains(mouseX, mouseY)
                && TerminalUi.inside(mouseX, mouseY, groupRailX + scaledInset, cy, groupRailW - scaledTrim, rowH);
        TerminalRenderContext renderContext = contextFor(tab, scrollFor(tab));
        TerminalUi.commandPageButton(renderContext, graphics, font, groupRailX + scaledInset, cy,
                groupRailW - scaledTrim, rowH,
                TerminalIcon.fromTitle(tab.chrome().shortTitle()),
                TerminalUi.themedPageIcon(renderContext, tab.chrome().shortTitle()), tab.chrome().shortTitle(),
                compact ? "" : tab.chrome().summary(), selected, hover, tab.descriptor().accentColor());
        return cy + rowH + gap;
    }

    private void drawCollapsedCommandStack(GuiGraphicsExtractor graphics, List<TerminalTab> tabs,
            int mouseX, int mouseY, String activeGroup, int accent) {
        TerminalRenderContext renderContext = tabs.isEmpty() ? contextFor(null, 0) : contextFor(tabs.get(activeTab), 0);
        TerminalUi.cinematicPanel(renderContext, graphics, groupRailX, groupRailY, groupRailW, groupRailH, accent);
        drawCollapseToggle(renderContext, graphics, mouseX, mouseY, accent);
        int buttonW = Math.max(zoomed(30, 24), groupRailW - railTrim(16));
        int cy = groupRailY + zoomed(34, 28);
        int rowH = collapsedGroupRowHeight();
        for (String group : navigationModel.groups()) {
            boolean active = group.equals(activeGroup);
            int groupColor = navigationModel.groupAccent(group, theme.accentColor());
            int groupInset = railInset(8);
            boolean hover = TerminalUi.inside(mouseX, mouseY, groupRailX + groupInset, cy, buttonW, rowH);
            TerminalUi.iconRailButton(renderContext, graphics, font, groupRailX + groupInset, cy, buttonW, rowH,
                    TerminalIcon.fromGroup(group), TerminalUi.themedGroupIcon(renderContext, group), "",
                    active, hover, groupColor);
            cy += rowH + zoomed(6, 4);
        }
        if (!tabs.isEmpty()) {
            TerminalTab tab = tabs.get(activeTab);
            TerminalRenderContext tabContext = contextFor(tab, scrollFor(tab));
            int pageY = Math.min(groupRailY + groupRailH - zoomed(82, 68), cy + zoomed(8, 6));
            int badgeSize = zoomed(28, 22);
            TerminalUi.hybridIconBadge(tabContext, graphics, TerminalUi.themedPageIcon(tabContext, tab.chrome().shortTitle()),
                    TerminalIcon.fromTitle(tab.chrome().shortTitle()),
                    groupRailX + Math.max(railInset(8), (groupRailW - badgeSize) / 2), pageY, badgeSize,
                    tab.descriptor().accentColor(), true);
            TerminalUi.collapsedRailStatus(tabContext, graphics, groupRailX + railInset(10),
                    groupRailY + groupRailH - zoomed(26, 22), groupRailW - railTrim(20), 0.82F, accent);
        }
    }

    private void drawCollapseToggle(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int mouseX, int mouseY, int accent) {
        boolean hovered = TerminalUi.inside(mouseX, mouseY, collapseToggleX, collapseToggleY, collapseToggleW, collapseToggleH);
        TerminalUi.collapseToggle(context, graphics, collapseToggleX, collapseToggleY, collapseToggleW,
                collapseToggleH, commandStackCollapsed, hovered, accent);
    }

    private void drawCompactTopNavigation(GuiGraphicsExtractor graphics, List<TerminalTab> tabs, int mouseX, int mouseY) {
        if (tabs.isEmpty()) {
            return;
        }
        String activeGroup = navigationModel.activeGroup(activeTab);
        int groupW = layoutProfile == TerminalLayoutProfile.COMPACT_STACK ? 100 : 142;
        boolean groupHover = TerminalUi.inside(mouseX, mouseY, navX, navY, groupW, navH);
        TerminalUi.categoryChip(graphics, font, navX, navY, groupW, navH,
                navigationModel.groupLabel(activeGroup), true, groupHover,
                navigationModel.groupAccent(activeGroup, theme.accentColor()));
        List<TerminalNavigationModel.IndexedTab> groupTabs = navigationModel.visibleTabsInGroup(activeGroup, activeTab);
        int x = navX + groupW + 4;
        int available = Math.max(48, navW - groupW - 4);
        int chipW = groupTabs.isEmpty() ? 0 : Math.max(72, Math.min(160,
                (available - Math.max(0, groupTabs.size() - 1) * 4) / groupTabs.size()));
        for (TerminalNavigationModel.IndexedTab entry : groupTabs) {
            TerminalTab tab = entry.tab();
            boolean active = entry.index() == activeTab;
            boolean hover = TerminalUi.inside(mouseX, mouseY, x, navY, chipW, navH);
            TerminalUi.pageTab(graphics, font, x, navY, chipW, navH,
                    tab.chrome().shortTitle(), active, hover, tab.descriptor().accentColor());
            x += chipW + 4;
        }
    }

    private void drawBody(GuiGraphicsExtractor graphics, List<TerminalTab> tabs, int mouseX, int mouseY, float partialTick) {
        int bodyAccent = tabs.isEmpty() ? theme.accentColor() : chromeColor(tabs.get(activeTab));
        boolean contentHovered = TerminalUi.inside(mouseX, mouseY, contentX, contentY, contentW, contentH);
        TerminalRenderContext bodyContext = tabs.isEmpty() ? contextFor(null, 0) : contextFor(tabs.get(activeTab), 0);
        TerminalUi.contentFrame(bodyContext, graphics, contentX, contentY, contentW, contentH, bodyAccent, contentHovered);
        if (tabs.isEmpty()) {
            graphics.text(font, Component.literal("No terminal views are online."), contentX + 10, contentY + 10, theme.mutedColor(), false);
            return;
        }
        if (Minecraft.getInstance().player == null) {
            graphics.text(font, Component.literal("LINK OFFLINE"), contentX + 10, contentY + 10, TerminalUi.RED, true);
            graphics.text(font, Component.literal("Operator link unavailable. Reopen after joining a world."),
                    contentX + 10, contentY + 26, theme.mutedColor(), false);
            return;
        }

        TerminalTab tab = tabs.get(activeTab);
        clampScroll(tab);
        int scroll = scrollFor(tab);
        TerminalRenderContext context = contextFor(tab, scroll);
        graphics.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
        tab.render(context, graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();
        TerminalUi.scrollbar(context, graphics, contentX + contentW - zoomed(7, 5),
                contentY + zoomed(8, 6), contentH - zoomed(16, 12),
                scroll, maxScroll(tab), tab.descriptor().accentColor(), contentHovered);
    }

    private void drawFooter(GuiGraphicsExtractor graphics, List<TerminalTab> tabs) {
        String footer = panelW < 560
                ? "ESC Close   Arrows Nav   Wheel Scroll"
                : layoutProfile == TerminalLayoutProfile.COMPACT_STACK
                        ? "ESC/Open Key Close   Arrows Navigate   Enter Command   Wheel Scroll"
                        : "ESC/Open Key Close   Arrows Navigate   Enter Command   Page/Wheel Scroll";
        String label = "";
        if (!tabs.isEmpty()) {
            label = "ECHO-7 LINK  |  " + navigationModel.activePathLabel(activeTab);
        }
        int color = tabs.isEmpty() ? theme.accentColor() : chromeColor(tabs.get(activeTab));
        TerminalRenderContext renderContext = tabs.isEmpty() ? contextFor(null, 0) : contextFor(tabs.get(activeTab), 0);
        int footerH = shellFooterHeight();
        TerminalUi.bottomShortcutBar(renderContext, graphics, font, panelX, panelY + panelH - footerH, panelW,
                footerH, footer, label.isBlank() ? "Esc Back" : label, color);
    }

    private boolean handleNavigationClick(List<TerminalTab> tabs, double mouseX, double mouseY) {
        if (tabs.isEmpty()) {
            return false;
        }
        return handleSidebarNavigationClick(tabs, mouseX, mouseY);
    }

    private boolean handleSidebarNavigationClick(List<TerminalTab> tabs, double mouseX, double mouseY) {
        return handleCommandStackNavigationClick(tabs, mouseX, mouseY);
    }

    private boolean handleCommandStackNavigationClick(List<TerminalTab> tabs, double mouseX, double mouseY) {
        if (TerminalUi.inside(mouseX, mouseY, collapseToggleX, collapseToggleY, collapseToggleW, collapseToggleH)) {
            commandStackCollapsed = !commandStackCollapsed;
            commandStackScroll = 0;
            playUiSound(0.85F);
            return true;
        }
        if (commandStackCollapsed) {
            return handleCollapsedCommandStackClick(tabs, mouseX, mouseY);
        }
        clampCommandStackScroll();
        if (!commandStackViewportContains(mouseX, mouseY)) {
            return false;
        }
        String activeGroup = navigationModel.activeGroup(activeTab);
        boolean compact = groupRailW < 190;
        int cy = commandStackContentY() - commandStackScroll;
        int rowH = commandRowHeight();
        int gap = commandRowGap();
        int groupInset = railInset(8);
        int groupTrim = railTrim(16);
        for (String group : navigationModel.groups()) {
            boolean active = group.equals(activeGroup);
            int groupH = commandGroupHeight(compact);
            if (TerminalUi.inside(mouseX, mouseY, groupRailX + groupInset, cy, groupRailW - groupTrim, groupH)) {
                selectTab(navigationModel.firstTabInGroup(group), tabs);
                return true;
            }
            cy += groupH + zoomed(active ? 5 : 3, active ? 3 : 2);
            if (active) {
                int nextY = handleExpandedGroupClick(tabs, group, mouseX, mouseY, cy, rowH, gap);
                if (nextY < 0) {
                    return true;
                }
                cy = nextY;
                cy += zoomed(5, 3);
            }
        }
        return false;
    }

    private int handleExpandedGroupClick(List<TerminalTab> tabs, String group, double mouseX, double mouseY,
            int cy, int rowH, int gap) {
        for (TerminalNavigationModel.IndexedTab entry : navigationModel.directTabsInGroup(group)) {
            if (TerminalUi.inside(mouseX, mouseY, groupRailX + railInset(14), cy,
                    groupRailW - railTrim(22), rowH)) {
                selectTab(entry.index(), tabs);
                return -1;
            }
            cy += rowH + gap;
        }
        String activeChapter = navigationModel.activeChapterId(activeTab);
        for (TerminalNavigationModel.ChapterGroup chapter : navigationModel.chaptersInGroup(group)) {
            if (TerminalUi.inside(mouseX, mouseY, groupRailX + railInset(14), cy,
                    groupRailW - railTrim(22), rowH)) {
                selectTab(navigationModel.firstTabInChapter(chapter), tabs);
                return -1;
            }
            cy += rowH + gap;
            if (chapter.id().equals(activeChapter)) {
                for (TerminalNavigationModel.IndexedTab entry : chapter.tabs()) {
                    if (TerminalUi.inside(mouseX, mouseY, groupRailX + railInset(22), cy,
                            groupRailW - railTrim(30), rowH)) {
                        selectTab(entry.index(), tabs);
                        return -1;
                    }
                    cy += rowH + gap;
                }
            }
        }
        return cy;
    }

    private boolean handleCollapsedCommandStackClick(List<TerminalTab> tabs, double mouseX, double mouseY) {
        int buttonW = Math.max(zoomed(30, 24), groupRailW - railTrim(16));
        int cy = groupRailY + zoomed(34, 28);
        int rowH = collapsedGroupRowHeight();
        for (String group : navigationModel.groups()) {
            if (TerminalUi.inside(mouseX, mouseY, groupRailX + railInset(8), cy, buttonW, rowH)) {
                selectTab(navigationModel.firstTabInGroup(group), tabs);
                return true;
            }
            cy += rowH + zoomed(6, 4);
        }
        return false;
    }

    private boolean handleCompactNavigationClick(List<TerminalTab> tabs, double mouseX, double mouseY) {
        String activeGroup = navigationModel.activeGroup(activeTab);
        int groupW = layoutProfile == TerminalLayoutProfile.COMPACT_STACK ? 100 : 142;
        if (TerminalUi.inside(mouseX, mouseY, navX, navY, groupW, navH)) {
            return selectGroupOffset(tabs, 1);
        }
        List<TerminalNavigationModel.IndexedTab> groupTabs = navigationModel.visibleTabsInGroup(activeGroup, activeTab);
        int x = navX + groupW + 4;
        int available = Math.max(48, navW - groupW - 4);
        int chipW = groupTabs.isEmpty() ? 0 : Math.max(72, Math.min(160,
                (available - Math.max(0, groupTabs.size() - 1) * 4) / groupTabs.size()));
        for (TerminalNavigationModel.IndexedTab entry : groupTabs) {
            if (TerminalUi.inside(mouseX, mouseY, x, navY, chipW, navH)) {
                selectTab(entry.index(), tabs);
                return true;
            }
            x += chipW + 4;
        }
        return false;
    }

    private int commandRowHeight() {
        if (commandStackCollapsed) {
            return zoomed(Math.max(24, 28 - densityStep() * 2), 20);
        }
        String activeGroup = navigationModel.activeGroup(activeTab);
        int tabCount = Math.max(1, navigationModel.visibleRowCount(activeGroup, activeTab));
        int groupCount = Math.max(1, navigationModel.groups().size());
        boolean compact = groupRailW < 190;
        int reserved = zoomed(compact ? 88 : 106, compact ? 72 : 86)
                + groupCount * (commandGroupHeight(compact) + zoomed(5, 3));
        int min = zoomed(Math.max(compact ? 20 : 23, (compact ? 22 : 26) - densityStep()),
                compact ? 18 : 20);
        int max = zoomed(Math.max(compact ? 24 : 28, (compact ? 28 : 34) - densityStep() * 3),
                compact ? 20 : 23);
        int available = Math.max(tabCount * min, groupRailH - reserved);
        return Math.max(min, Math.min(max, (available / tabCount) - zoomed(2, 1)));
    }

    private int commandRowGap() {
        return commandRowHeight() <= zoomed(25, 21) ? zoomed(2, 2) : zoomed(3, 2);
    }

    private boolean handleCommandStackMouseScroll(List<TerminalTab> tabs, double mouseX, double mouseY, double deltaY) {
        if (tabs.isEmpty() || commandStackCollapsed || !commandStackViewportContains(mouseX, mouseY)) {
            return false;
        }
        int maxScroll = maxCommandStackScroll();
        if (maxScroll <= 0) {
            return false;
        }
        setCommandStackScroll(commandStackScroll - (int) Math.round(deltaY * commandStackScrollStep()));
        return true;
    }

    private void syncCommandStackScrollGroup() {
        String activeGroup = navigationModel.activeGroup(activeTab);
        if (!activeGroup.equals(commandStackScrollGroup)) {
            commandStackScrollGroup = activeGroup;
            commandStackScroll = 0;
        }
    }

    private void setCommandStackScroll(int value) {
        syncCommandStackScrollGroup();
        commandStackScroll = Math.max(0, Math.min(value, maxCommandStackScroll()));
    }

    private void clampCommandStackScroll() {
        syncCommandStackScrollGroup();
        if (commandStackCollapsed) {
            commandStackScroll = 0;
            return;
        }
        commandStackScroll = Math.max(0, Math.min(commandStackScroll, maxCommandStackScroll()));
    }

    private int maxCommandStackScroll() {
        if (commandStackCollapsed) {
            return 0;
        }
        return Math.max(0, commandStackContentHeight() - commandStackViewportHeight());
    }

    private int commandStackContentHeight() {
        boolean compact = groupRailW < 190;
        int cy = commandStackContentY();
        int rowH = commandRowHeight();
        int gap = commandRowGap();
        String activeGroup = navigationModel.activeGroup(activeTab);
        for (String group : navigationModel.groups()) {
            boolean active = group.equals(activeGroup);
            int groupH = commandGroupHeight(compact);
            cy += groupH + zoomed(active ? 5 : 3, active ? 3 : 2);
            if (active) {
                cy = advanceExpandedGroup(group, cy, rowH, gap);
                cy += zoomed(5, 3);
            }
        }
        return Math.max(0, cy - commandStackViewportTop());
    }

    private int advanceExpandedGroup(String group, int cy, int rowH, int gap) {
        cy += navigationModel.directTabsInGroup(group).size() * (rowH + gap);
        String activeChapter = navigationModel.activeChapterId(activeTab);
        for (TerminalNavigationModel.ChapterGroup chapter : navigationModel.chaptersInGroup(group)) {
            cy += rowH + gap;
            if (chapter.id().equals(activeChapter)) {
                cy += chapter.tabs().size() * (rowH + gap);
            }
        }
        return cy;
    }

    private int commandStackContentY() {
        return groupRailY + (groupRailW < 190
                ? zoomed(Math.max(30, 32 - densityStep()), 26)
                : zoomed(Math.max(34, 40 - densityStep() * 3), 30));
    }

    private int commandStackViewportTop() {
        return groupRailY + zoomed(Math.max(26, 28 - densityStep()), 22);
    }

    private int commandStackViewportBottom() {
        return groupRailY + groupRailH - commandStackFooterReserve();
    }

    private int commandStackViewportHeight() {
        return Math.max(0, commandStackViewportBottom() - commandStackViewportTop());
    }

    private boolean commandStackViewportContains(double mouseX, double mouseY) {
        return !commandStackCollapsed
                && TerminalUi.inside(mouseX, mouseY, groupRailX, commandStackViewportTop(),
                        groupRailW, commandStackViewportHeight());
    }

    private TerminalClientOptions.InterfaceDensity interfaceDensity() {
        return TerminalClientOptions.interfaceDensity();
    }

    private int densityStep() {
        return interfaceDensity().compactness();
    }

    private TerminalClientOptions.TerminalZoom terminalZoom() {
        return TerminalClientOptions.terminalZoom();
    }

    private double terminalZoomScale() {
        return terminalZoom().scale();
    }

    private int zoomed(int value) {
        return (int) Math.round(value * terminalZoomScale());
    }

    private int zoomed(int value, int minimum) {
        return Math.max(minimum, zoomed(value));
    }

    private double zoomed(double value, double minimum) {
        return Math.max(minimum, value * terminalZoomScale());
    }

    private static int contentZoomed(int value, int minimum, TerminalClientOptions.TerminalZoom zoom) {
        double scale = zoom == null ? 1.0D : zoom.scale();
        return Math.max(minimum, (int) Math.round(value * scale));
    }

    private static int shellSized(int value, int minimum) {
        return Math.max(minimum, value);
    }

    private int railInset(int value) {
        return zoomed(value, Math.max(5, (int) Math.floor(value * 0.72D)));
    }

    private int railTrim(int value) {
        return zoomed(value, Math.max(10, (int) Math.floor(value * 0.72D)));
    }

    private int shellHeaderHeight() {
        return shellHeaderHeight(densityStep());
    }

    private int shellFooterHeight() {
        return shellFooterHeight(densityStep());
    }

    private static int shellHeaderHeight(int density) {
        return shellSized(Math.max(42, 52 - density * 4), 36);
    }

    private static int shellFooterHeight(int density) {
        return shellSized(Math.max(24, 30 - density * 2), 22);
    }

    private int commandGroupHeight(boolean compact) {
        return zoomed(Math.max(compact ? 22 : 24, (compact ? 24 : 28) - densityStep() * 2),
                compact ? 19 : 20);
    }

    private int collapsedGroupRowHeight() {
        int base = groupRailW <= 52 ? 32 : 34;
        return zoomed(Math.max(28, base - densityStep() * 2), 24);
    }

    private int diagnosticRailHeight() {
        return zoomed(Math.max(30, 36 - densityStep() * 2), 26);
    }

    private int commandStackFooterReserve() {
        return diagnosticRailHeight() + zoomed(16, 12);
    }

    private double commandStackScrollStep() {
        return zoomed(Math.max(14.0D, 18.0D - densityStep() * 2.0D), 12.0D);
    }

    private TerminalRenderContext contextFor(TerminalTab tab, int scroll) {
        Minecraft minecraft = Minecraft.getInstance();
        Identifier tabId = tab == null ? null : tab.descriptor().id();
        String group = tab == null ? navigationModel.activeGroup(activeTab) : tab.chrome().group();
        String chapterId = navigationModel.activeChapterId(activeTab);
        String chapterTitle = navigationModel.activePathLabel(activeTab);
        String namespace = tabId == null || "echoterminal".equals(tabId.getNamespace()) ? chapterId : tabId.getNamespace();
        TerminalThemeContext themeContext = new TerminalThemeContext(
                tabId,
                group,
                chapterId,
                chapterTitle,
                namespace,
                ticks,
                TerminalClientOptions.useVisualAssets(),
                TerminalClientOptions.reduceMotion());
        int contentPadX = zoomed(10, 8);
        int contentPadY = zoomed(10, 8);
        int contentTrimW = zoomed(22, 16);
        int contentTrimH = zoomed(20, 16);
        return new TerminalRenderContext(
                minecraft,
                minecraft.player,
                width,
                height,
                contentX + contentPadX,
                contentY + contentPadY - scroll,
                Math.max(80, contentW - contentTrimW),
                Math.max(80, contentH - contentTrimH),
                scroll,
                this::selectTabById,
                this::hasTab,
                TerminalClientOptions.currentTheme(),
                themeContext);
    }

    private boolean hasTab(Identifier tabId) {
        if (tabId == null) {
            return false;
        }
        return tabs().stream().anyMatch(tab -> tab.descriptor().id().equals(tabId));
    }

    private void selectTabById(Identifier tabId) {
        List<TerminalTab> tabs = tabs();
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).descriptor().id().equals(tabId)) {
                selectTab(i, tabs);
                return;
            }
        }
    }

    private void selectTab(int index, List<TerminalTab> tabs) {
        if (tabs.isEmpty()) {
            activeTab = 0;
            return;
        }
        int previousTab = activeTab;
        activeTab = Math.max(0, Math.min(index, tabs.size() - 1));
        TerminalTab tab = tabs.get(activeTab);
        rememberedTabId = tab.descriptor().id();
        tab.onSelected(contextFor(tab, scrollFor(tab)));
        clampScroll(tab);
        clampCommandStackScroll();
        if (initialTabSelected && previousTab != activeTab) {
            playUiSound(1.15F);
        }
    }

    private void playUiSound(float pitch) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(TerminalThemedSounds.click(), pitch, 0.35F));
    }

    private boolean selectGroupOffset(List<TerminalTab> tabs, int offset) {
        List<String> groups = navigationModel.groups();
        if (groups.isEmpty()) {
            return false;
        }
        int index = Math.max(0, groups.indexOf(navigationModel.activeGroup(activeTab)));
        String group = groups.get(Math.floorMod(index + offset, groups.size()));
        selectTab(navigationModel.firstTabInGroup(group), tabs);
        return true;
    }

    private void normalizeActiveTab(List<TerminalTab> tabs) {
        if (tabs.isEmpty()) {
            activeTab = 0;
            return;
        }
        if (!initialTabSelected) {
            initialTabSelected = true;
            Identifier target = rememberedTabId == null ? OVERVIEW_TAB : rememberedTabId;
            int index = findTab(tabs, target);
            if (index < 0 && target != OVERVIEW_TAB) {
                index = findTab(tabs, OVERVIEW_TAB);
            }
            selectTab(index < 0 ? 0 : index, tabs);
            return;
        }
        if (activeTab >= tabs.size()) {
            activeTab = tabs.size() - 1;
        }
        clampCommandStackScroll();
    }

    private int findTab(List<TerminalTab> tabs, Identifier id) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).descriptor().id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private int chromeColor(TerminalTab tab) {
        return tab == null ? theme.accentColor() : tab.descriptor().accentColor();
    }

    private int scrollFor(TerminalTab tab) {
        return tabScroll.getOrDefault(tab.descriptor().id(), 0);
    }

    private void setScroll(TerminalTab tab, int value) {
        int next = Math.max(0, Math.min(value, maxScroll(tab)));
        tabScroll.put(tab.descriptor().id(), next);
    }

    private void clampScroll(TerminalTab tab) {
        setScroll(tab, scrollFor(tab));
    }

    private int maxScroll(TerminalTab tab) {
        int contentHeight = Math.max(0, tab.contentHeight(contextFor(tab, 0)));
        int contentTrimH = zoomed(20, 16);
        int viewportH = Math.max(80, contentH - contentTrimH);
        return Math.max(0, contentHeight - viewportH);
    }

    private void layout() {
        LayoutMetrics metrics = computeLayoutMetrics(width, height, theme.panelMaxWidth(), theme.panelMaxHeight(),
                interfaceDensity(), terminalZoom(), commandStackCollapsed);
        panelX = metrics.panelX();
        panelY = metrics.panelY();
        panelW = metrics.panelW();
        panelH = metrics.panelH();
        layoutProfile = metrics.layoutProfile();
        commandStackNavigation = true;
        sidebarNavigation = true;
        groupRailX = metrics.groupRailX();
        groupRailY = metrics.groupRailY();
        groupRailW = metrics.groupRailW();
        groupRailH = metrics.groupRailH();
        pageRailX = groupRailX;
        pageRailY = groupRailY;
        pageRailW = groupRailW;
        pageRailH = groupRailH;
        navX = pageRailX;
        navY = pageRailY;
        navW = pageRailW;
        navH = pageRailH;
        collapseToggleX = metrics.collapseToggleX();
        collapseToggleY = metrics.collapseToggleY();
        collapseToggleW = metrics.collapseToggleW();
        collapseToggleH = metrics.collapseToggleH();
        contentX = metrics.contentX();
        contentY = metrics.contentY();
        contentW = metrics.contentW();
        contentH = metrics.contentH();
        clampActiveContentScroll();
        clampCommandStackScroll();
    }

    private static LayoutMetrics computeLayoutMetrics(
            int screenWidth,
            int screenHeight,
            int panelMaxWidth,
            int panelMaxHeight,
            TerminalClientOptions.InterfaceDensity densityOption,
            TerminalClientOptions.TerminalZoom zoom,
            boolean commandStackCollapsed) {
        TerminalClientOptions.InterfaceDensity resolvedDensity = densityOption == null
                ? TerminalClientOptions.InterfaceDensity.BALANCED
                : densityOption;
        TerminalClientOptions.TerminalZoom resolvedZoom = zoom == null
                ? TerminalClientOptions.TerminalZoom.ZOOM_100
                : zoom;
        int density = resolvedDensity.compactness();
        int minDimension = Math.min(screenWidth, screenHeight);
        int baseMargin = Math.max(8, Math.min(16, minDimension / 68));
        int margin = Math.min(Math.max(baseMargin + density * 8, baseMargin),
                Math.max(10, minDimension / 7));
        int minPanelW = shellSized(340, 300);
        int minPanelH = shellSized(280, 240);
        int usableW = Math.max(minPanelW, screenWidth - margin * 2);
        int usableH = Math.max(minPanelH, screenHeight - margin * 2);
        int maxPanelW = Math.max(minPanelW, shellSized(Math.max(360, panelMaxWidth - density * 36), minPanelW));
        int maxPanelH = Math.max(minPanelH, shellSized(Math.max(270, panelMaxHeight - density * 24), minPanelH));
        int panelW = Math.min(maxPanelW, usableW);
        int panelH = Math.min(maxPanelH, usableH);
        TerminalLayoutProfile layoutProfile = panelW < 660
                ? TerminalLayoutProfile.COMPACT_STACK
                : panelW < 980 ? TerminalLayoutProfile.MEDIUM_CAROUSEL : TerminalLayoutProfile.APP_HUB;
        int panelX = (screenWidth - panelW) / 2;
        int panelY = (screenHeight - panelH) / 2;
        int shellHeaderH = shellHeaderHeight(density);
        int shellFooterH = shellFooterHeight(density);
        int footerTop = panelY + panelH - shellFooterH - shellSized(4, 3);
        int horizontalPad = shellSized(panelW < 560 ? 12 : 18 + density, 8);
        int groupRailX = panelX + horizontalPad;
        int groupRailY = panelY + shellHeaderH + shellSized(6, 4);
        int groupRailW;
        if (commandStackCollapsed) {
            groupRailW = panelW >= 760 ? shellSized(58, 46) : shellSized(50, 42);
        } else if (panelW >= 980) {
            groupRailW = Math.max(shellSized(224, 188),
                    Math.min(shellSized(276, 224), panelW / 5 - density * 4));
        } else if (panelW >= 760) {
            groupRailW = Math.max(shellSized(198, 168),
                    Math.min(shellSized(230, 190), panelW / 4 - density * 3));
        } else {
            groupRailW = Math.max(shellSized(108, 92), Math.min(shellSized(152, 124), panelW / 4));
        }
        int groupRailH = Math.max(shellSized(210, 168), footerTop - groupRailY - shellSized(8, 6));
        int collapseToggleH = contentZoomed(18, 14, resolvedZoom);
        int collapseToggleW = commandStackCollapsed
                ? Math.max(contentZoomed(30, 24, resolvedZoom), groupRailW - contentZoomed(18, 14, resolvedZoom))
                : contentZoomed(26, 22, resolvedZoom);
        int collapseToggleX = commandStackCollapsed
                ? groupRailX + contentZoomed(9, 7, resolvedZoom)
                : groupRailX + groupRailW - collapseToggleW - contentZoomed(8, 6, resolvedZoom);
        int collapseToggleY = groupRailY + contentZoomed(8, 6, resolvedZoom);
        int gap = commandStackCollapsed
                ? shellSized(Math.max(8, 10 - density), 6)
                : panelW >= 760 ? shellSized(Math.max(10, 14 - density * 2), 8) : shellSized(8, 6);
        int contentX = groupRailX + groupRailW + gap;
        int contentY = groupRailY;
        int contentW = Math.max(shellSized(panelW < 520 ? 180 : 260, panelW < 520 ? 150 : 210),
                panelX + panelW - contentX - horizontalPad);
        int contentH = Math.max(shellSized(168, 136), footerTop - contentY - 8);
        int contentPadX = contentZoomed(10, 8, resolvedZoom);
        int contentPadY = contentZoomed(10, 8, resolvedZoom);
        int contentTrimW = contentZoomed(22, 16, resolvedZoom);
        int contentTrimH = contentZoomed(20, 16, resolvedZoom);
        return new LayoutMetrics(
                panelX,
                panelY,
                panelW,
                panelH,
                contentX,
                contentY,
                contentW,
                contentH,
                contentX + contentPadX,
                contentY + contentPadY,
                Math.max(80, contentW - contentTrimW),
                Math.max(80, contentH - contentTrimH),
                groupRailX,
                groupRailY,
                groupRailW,
                groupRailH,
                collapseToggleX,
                collapseToggleY,
                collapseToggleW,
                collapseToggleH,
                shellHeaderH,
                shellFooterH,
                layoutProfile);
    }

    private void clampActiveContentScroll() {
        List<TerminalTab> tabs = navigationModel.tabs();
        if (activeTab >= 0 && activeTab < tabs.size()) {
            clampScroll(tabs.get(activeTab));
        }
    }

    private String trim(String text, int maxWidth) {
        return TerminalUi.trim(font, text, maxWidth);
    }

    private static int pulseColor(int tick, int low, int high, int period) {
        float phase = (float) ((Math.sin((tick % period) / (double) period * Math.PI * 2.0D) + 1.0D) * 0.5D);
        int la = (low >>> 24) & 0xFF;
        int ha = (high >>> 24) & 0xFF;
        int alpha = Math.round(la + (ha - la) * phase);
        return (alpha << 24) | (high & 0x00FFFFFF);
    }

}
