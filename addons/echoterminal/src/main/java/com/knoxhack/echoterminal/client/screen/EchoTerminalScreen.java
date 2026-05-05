package com.knoxhack.echoterminal.client.screen;

import com.knoxhack.echoterminal.api.TerminalRenderCache;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalLayoutProfile;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    private int collapseToggleX;
    private int collapseToggleY;
    private int collapseToggleW;
    private int collapseToggleH;

    public EchoTerminalScreen(EchoTerminalMenu menu, Inventory playerInventory, Component title) {
        this(menu, playerInventory, title, TerminalScreenTheme.modular());
    }

    public EchoTerminalScreen(EchoTerminalMenu menu, Inventory playerInventory, Component title, TerminalScreenTheme theme) {
        super(menu, playerInventory, title);
        this.theme = theme == null ? TerminalScreenTheme.modular() : theme;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ticks++;
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);

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
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        if (tab != null && tab.keyPressed(contextFor(tab, scrollFor(tab)), event)) {
            return true;
        }

        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_M) {
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
        if ((key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_W) && !tabs.isEmpty()) {
            return selectGroupOffset(tabs, -1);
        }
        if ((key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_S) && !tabs.isEmpty()) {
            return selectGroupOffset(tabs, 1);
        }
        if ((key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) && !tabs.isEmpty()) {
            selectTab(Math.floorMod(activeTab - 1, tabs.size()), tabs);
            return true;
        }
        if ((key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D || key == GLFW.GLFW_KEY_TAB) && !tabs.isEmpty()) {
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

    public boolean handleCharTyped(CharacterEvent event) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
        TerminalTab tab = activeTab < tabs.size() ? tabs.get(activeTab) : null;
        return tab != null && tab.charTyped(contextFor(tab, scrollFor(tab)), event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
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
    public boolean isPauseScreen() {
        return false;
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double deltaY) {
        TerminalRenderCache.beginFrame();
        layout();
        List<TerminalTab> tabs = tabs();
        normalizeActiveTab(tabs);
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
        TerminalUi.appShellBackdrop(graphics, TerminalVisualAssets.TERMINAL_FRAME_BACKDROP,
                panelX, panelY, panelW, panelH, chromeColor(tab),
                TerminalClientOptions.useVisualAssets(), TerminalClientOptions.reduceMotion());
        TerminalUi.topMetaBar(graphics, font, panelX, panelY, panelW,
                theme.title(), status, meta, chromeColor(tab));

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
        TerminalUi.commandStackPanel(graphics, font, groupRailX, groupRailY, groupRailW, groupRailH, accent);
        drawCollapseToggle(graphics, mouseX, mouseY, accent);
        boolean compact = groupRailW < 190;
        int cy = groupRailY + (compact ? 32 : 40);
        int rowH = commandRowHeight();
        int gap = commandRowGap();
        graphics.enableScissor(groupRailX, groupRailY + 28, groupRailX + groupRailW,
                groupRailY + groupRailH - 54);
        for (String group : navigationModel.groups()) {
            boolean active = group.equals(activeGroup);
            int groupColor = navigationModel.groupAccent(group, theme.accentColor());
            int groupH = compact ? 24 : 28;
            boolean groupHover = TerminalUi.inside(mouseX, mouseY, groupRailX + 8, cy, groupRailW - 16, groupH);
            TerminalUi.commandStackGroupButton(graphics, font, groupRailX + 8, cy, groupRailW - 16, groupH,
                    TerminalIcon.fromGroup(group), TerminalVisualAssets.terminalGroupIcon(group),
                    navigationModel.groupLabel(group), active, groupHover, groupColor);
            cy += groupH + (active ? 5 : 3);
            if (active) {
                for (TerminalNavigationModel.IndexedTab entry : navigationModel.tabsInGroup(group)) {
                    TerminalTab tab = entry.tab();
                    boolean selected = entry.index() == activeTab;
                    boolean hover = TerminalUi.inside(mouseX, mouseY, groupRailX + 14, cy, groupRailW - 22, rowH);
                    TerminalUi.commandPageButton(graphics, font, groupRailX + 14, cy, groupRailW - 22, rowH,
                            TerminalIcon.fromTitle(tab.chrome().shortTitle()),
                            TerminalVisualAssets.terminalPageIcon(tab.chrome().shortTitle()), tab.chrome().shortTitle(),
                            compact ? "" : tab.chrome().summary(), selected, hover, tab.descriptor().accentColor());
                    cy += rowH + gap;
                }
                cy += 5;
            }
        }
        graphics.disableScissor();
        TerminalUi.diagnosticRail(graphics, font, groupRailX + 10, groupRailY + groupRailH - 47,
                groupRailW - 20, 36, Minecraft.getInstance().player != null, accent);
    }

    private void drawCollapsedCommandStack(GuiGraphicsExtractor graphics, List<TerminalTab> tabs,
            int mouseX, int mouseY, String activeGroup, int accent) {
        TerminalUi.cinematicPanel(graphics, groupRailX, groupRailY, groupRailW, groupRailH, accent);
        drawCollapseToggle(graphics, mouseX, mouseY, accent);
        int buttonW = Math.max(30, groupRailW - 16);
        int cy = groupRailY + 34;
        int rowH = 34;
        for (String group : navigationModel.groups()) {
            boolean active = group.equals(activeGroup);
            int groupColor = navigationModel.groupAccent(group, theme.accentColor());
            boolean hover = TerminalUi.inside(mouseX, mouseY, groupRailX + 8, cy, buttonW, rowH);
            TerminalUi.iconRailButton(graphics, font, groupRailX + 8, cy, buttonW, rowH,
                    TerminalIcon.fromGroup(group), TerminalVisualAssets.terminalGroupIcon(group), "",
                    active, hover, groupColor);
            cy += rowH + 6;
        }
        if (!tabs.isEmpty()) {
            TerminalTab tab = tabs.get(activeTab);
            int pageY = Math.min(groupRailY + groupRailH - 82, cy + 8);
            TerminalUi.hybridIconBadge(graphics, TerminalVisualAssets.terminalPageIcon(tab.chrome().shortTitle()),
                    TerminalIcon.fromTitle(tab.chrome().shortTitle()),
                    groupRailX + Math.max(8, (groupRailW - 28) / 2), pageY, 28,
                    tab.descriptor().accentColor(), true);
            graphics.fill(groupRailX + 10, groupRailY + groupRailH - 26,
                    groupRailX + groupRailW - 10, groupRailY + groupRailH - 22, 0x552E8E9D);
            graphics.fill(groupRailX + 10, groupRailY + groupRailH - 26,
                    groupRailX + groupRailW - 18, groupRailY + groupRailH - 22, TerminalUi.opaque(accent));
        }
    }

    private void drawCollapseToggle(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int accent) {
        boolean hovered = TerminalUi.inside(mouseX, mouseY, collapseToggleX, collapseToggleY, collapseToggleW, collapseToggleH);
        int bg = hovered ? 0xCC10313C : 0x99071117;
        graphics.fill(collapseToggleX, collapseToggleY, collapseToggleX + collapseToggleW,
                collapseToggleY + collapseToggleH, bg);
        graphics.outline(collapseToggleX, collapseToggleY, collapseToggleW, collapseToggleH,
                TerminalUi.opaque(accent));
        String label = commandStackCollapsed ? "OPEN" : "MIN";
        graphics.centeredText(font, label, collapseToggleX + collapseToggleW / 2,
                collapseToggleY + Math.max(4, (collapseToggleH - 8) / 2), TerminalUi.TEXT);
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
        List<TerminalNavigationModel.IndexedTab> groupTabs = navigationModel.tabsInGroup(activeGroup);
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
        TerminalUi.cinematicContentFrame(graphics, contentX, contentY, contentW, contentH, bodyAccent);
        if (tabs.isEmpty()) {
            graphics.text(font, Component.literal("No terminal channels are online."), contentX + 10, contentY + 10, theme.mutedColor(), false);
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
        TerminalUi.scrollbar(graphics, contentX + contentW - 5, contentY + 5, contentH - 10,
                scroll, maxScroll(tab), tab.descriptor().accentColor());
    }

    private void drawFooter(GuiGraphicsExtractor graphics, List<TerminalTab> tabs) {
        String footer = layoutProfile == TerminalLayoutProfile.COMPACT_STACK
                ? "ESC Close   LEFT/RIGHT Tabs   UP/DOWN Groups   WHEEL Scroll"
                : "ESC Close   M Close   LEFT/RIGHT Tabs   UP/DOWN Groups   PAGE Scroll   WHEEL Scroll";
        String label = "";
        if (!tabs.isEmpty()) {
            TerminalTab tab = tabs.get(activeTab);
            label = "ECHO-7 LINK  |  " + navigationModel.groupLabel(tab.chrome().group())
                    + " / " + tab.chrome().shortTitle();
        }
        int color = tabs.isEmpty() ? theme.accentColor() : chromeColor(tabs.get(activeTab));
        TerminalUi.bottomShortcutBar(graphics, font, panelX, panelY + panelH - 30, panelW,
                footer, label.isBlank() ? "Esc Back" : label, color);
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
            return true;
        }
        if (commandStackCollapsed) {
            return handleCollapsedCommandStackClick(tabs, mouseX, mouseY);
        }
        String activeGroup = navigationModel.activeGroup(activeTab);
        boolean compact = groupRailW < 190;
        int cy = groupRailY + (compact ? 32 : 40);
        int rowH = commandRowHeight();
        int gap = commandRowGap();
        for (String group : navigationModel.groups()) {
            boolean active = group.equals(activeGroup);
            int groupH = compact ? 24 : 28;
            if (TerminalUi.inside(mouseX, mouseY, groupRailX + 8, cy, groupRailW - 16, groupH)) {
                selectTab(navigationModel.firstTabInGroup(group), tabs);
                return true;
            }
            cy += groupH + (active ? 5 : 3);
            if (active) {
                for (TerminalNavigationModel.IndexedTab entry : navigationModel.tabsInGroup(group)) {
                    if (TerminalUi.inside(mouseX, mouseY, groupRailX + 14, cy, groupRailW - 22, rowH)) {
                        selectTab(entry.index(), tabs);
                        return true;
                    }
                    cy += rowH + gap;
                }
                cy += 5;
            }
        }
        return false;
    }

    private boolean handleCollapsedCommandStackClick(List<TerminalTab> tabs, double mouseX, double mouseY) {
        int buttonW = Math.max(30, groupRailW - 16);
        int cy = groupRailY + 34;
        int rowH = 34;
        for (String group : navigationModel.groups()) {
            if (TerminalUi.inside(mouseX, mouseY, groupRailX + 8, cy, buttonW, rowH)) {
                selectTab(navigationModel.firstTabInGroup(group), tabs);
                return true;
            }
            cy += rowH + 6;
        }
        return false;
    }

    private boolean handleCompactNavigationClick(List<TerminalTab> tabs, double mouseX, double mouseY) {
        String activeGroup = navigationModel.activeGroup(activeTab);
        int groupW = layoutProfile == TerminalLayoutProfile.COMPACT_STACK ? 100 : 142;
        if (TerminalUi.inside(mouseX, mouseY, navX, navY, groupW, navH)) {
            return selectGroupOffset(tabs, 1);
        }
        List<TerminalNavigationModel.IndexedTab> groupTabs = navigationModel.tabsInGroup(activeGroup);
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
            return 28;
        }
        String activeGroup = navigationModel.activeGroup(activeTab);
        int tabCount = Math.max(1, navigationModel.tabsInGroup(activeGroup).size());
        int groupCount = Math.max(1, navigationModel.groups().size());
        boolean compact = groupRailW < 190;
        int reserved = (compact ? 94 : 118) + groupCount * (compact ? 27 : 34);
        int min = compact ? 22 : 26;
        int max = compact ? 28 : 34;
        int available = Math.max(tabCount * min, groupRailH - reserved);
        return Math.max(min, Math.min(max, (available / tabCount) - 2));
    }

    private int commandRowGap() {
        return commandRowHeight() <= 25 ? 2 : 3;
    }

    private TerminalRenderContext contextFor(TerminalTab tab, int scroll) {
        Minecraft minecraft = Minecraft.getInstance();
        return new TerminalRenderContext(
                minecraft,
                minecraft.player,
                width,
                height,
                contentX + 10,
                contentY + 10 - scroll,
                contentW - 22,
                contentH - 20,
                scroll);
    }

    private void selectTab(int index, List<TerminalTab> tabs) {
        if (tabs.isEmpty()) {
            activeTab = 0;
            return;
        }
        activeTab = Math.max(0, Math.min(index, tabs.size() - 1));
        TerminalTab tab = tabs.get(activeTab);
        rememberedTabId = tab.descriptor().id();
        tab.onSelected(contextFor(tab, scrollFor(tab)));
        clampScroll(tab);
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
        return Math.max(0, contentHeight - (contentH - 16));
    }

    private void layout() {
        int margin = Math.max(8, Math.min(16, Math.min(width, height) / 68));
        panelW = Math.min(theme.panelMaxWidth(), Math.max(340, width - margin * 2));
        panelH = Math.min(theme.panelMaxHeight(), Math.max(280, height - margin * 2));
        layoutProfile = panelW < 660
                ? TerminalLayoutProfile.COMPACT_STACK
                : panelW < 980 ? TerminalLayoutProfile.MEDIUM_CAROUSEL : TerminalLayoutProfile.APP_HUB;
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        commandStackNavigation = true;
        sidebarNavigation = true;
        int footerTop = panelY + panelH - 34;
        int horizontalPad = panelW < 560 ? 12 : 18;
        groupRailX = panelX + horizontalPad;
        groupRailY = panelY + 58;
        if (commandStackCollapsed) {
            groupRailW = panelW >= 760 ? 58 : 50;
        } else if (panelW >= 980) {
            groupRailW = Math.max(232, Math.min(286, panelW / 5));
        } else if (panelW >= 760) {
            groupRailW = Math.max(206, Math.min(238, panelW / 4));
        } else {
            groupRailW = Math.max(108, Math.min(152, panelW / 4));
        }
        groupRailH = Math.max(210, footerTop - groupRailY - 8);
        pageRailX = groupRailX;
        pageRailY = groupRailY;
        pageRailW = groupRailW;
        pageRailH = groupRailH;
        navX = pageRailX;
        navY = pageRailY;
        navW = pageRailW;
        navH = pageRailH;
        collapseToggleH = 18;
        collapseToggleW = commandStackCollapsed ? Math.max(36, groupRailW - 16) : 38;
        collapseToggleX = commandStackCollapsed ? groupRailX + 8 : groupRailX + groupRailW - collapseToggleW - 8;
        collapseToggleY = groupRailY + 8;
        int gap = commandStackCollapsed ? 10 : panelW >= 760 ? 14 : 8;
        contentX = groupRailX + groupRailW + gap;
        contentY = groupRailY;
        contentW = Math.max(panelW < 520 ? 180 : 260, panelX + panelW - contentX - horizontalPad);
        contentH = Math.max(168, footerTop - contentY - 8);
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
