package com.knoxhack.echoindex.client;

import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoindex.Config;
import com.knoxhack.echoindex.EchoIndexClient;
import com.knoxhack.echoindex.network.IndexActionPacket;
import com.knoxhack.echoindex.service.ClientIndexState;
import com.knoxhack.echoindex.service.IndexRecipePlan;
import com.knoxhack.echoindex.service.IndexRecipePlanner;
import com.knoxhack.echoindex.service.IndexRecipeQueryClientState;
import com.knoxhack.echoindex.service.IndexRecipeSnapshot;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

public final class IndexOverlay {
    private static final int BG = 0xE8060D13;
    private static final int PANEL = 0xF00B151D;
    private static final int ROW = 0xAA102630;
    private static final int TEXT = 0xFFE9FBFF;
    private static final int MUTED = 0xFF8CA7B5;
    private static final int CYAN = 0xFF66E8FF;
    private static final int WARN = 0xFFFFD166;
    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 24;
    private static final int INNER_PAD = 10;
    private static final int GRID_SCROLLBAR_GUTTER = 8;
    private static final int SPLIT_DETAIL_MIN_WIDTH = 460;
    private static final int SPLIT_DETAIL_MIN_HEIGHT = 220;
    private static final List<Hitbox> HITBOXES = new ArrayList<>();
    private static final List<IndexRecipeUi.SlotHit> SLOT_HITS = new ArrayList<>();
    private static final Map<String, OverlayScreenState> SCREEN_STATES = new HashMap<>();
    private static final Map<String, PanelBounds> PANEL_BOUNDS = new HashMap<>();
    private static final List<HistoryEntry> DETAIL_HISTORY = new ArrayList<>();
    private static final int MAX_HISTORY = 16;

    private static String search = "";
    private static boolean searchFocused;
    private static boolean collapsed;
    private static int scroll;
    private static int horizontalScroll;
    private static int panelX;
    private static int panelY;
    private static int panelW;
    private static int panelH;
    private static int lastGridX;
    private static int lastGridY;
    private static int lastGridW;
    private static int lastGridH;
    private static ItemStack hoveredStack = ItemStack.EMPTY;
    private static String categoryFilter = "";
    private static boolean bookmarkedOnly;
    private static String activeScreenKey = "";
    private static Config.GridDensity gridDensity = Config.GridDensity.NORMAL;
    private static boolean gridDensityOverridden;
    private static ItemStack detailStack = ItemStack.EMPTY;
    private static IndexRecipeUi.ViewMode detailMode = IndexRecipeUi.ViewMode.RECIPES;
    private static Identifier detailCategory;
    private static int detailSelected;
    private static int historyIndex = -1;
    private static DragMode dragMode = DragMode.NONE;
    private static int dragMouseX;
    private static int dragMouseY;
    private static int dragPanelX;
    private static int dragPanelY;
    private static int dragPanelW;
    private static int dragPanelH;
    private static int dragThumbOffset;
    private static int lastMouseX;
    private static int lastMouseY;
    private static ScrollbarMetrics verticalScrollbar;
    private static ScrollbarMetrics horizontalScrollbar;
    private static long lastSyncRequestMillis;
    private static GridCacheKey gridCacheKey;
    private static List<ItemStack> gridCacheItems = List.of();
    private static DetailBaseCacheKey detailBaseCacheKey;
    private static List<IndexRecipeView> detailBaseCacheViews = List.of();
    private static DetailCacheKey detailCacheKey;
    private static List<IndexRecipeView> detailCacheViews = List.of();
    private static final Map<ModeCountKey, Integer> MODE_COUNT_CACHE = new HashMap<>();

    private IndexOverlay() {
    }

    public static void onRender(ContainerScreenEvent.Render.Foreground event) {
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        if (!active(screen)) {
            saveScreenState();
            HITBOXES.clear();
            SLOT_HITS.clear();
            hoveredStack = ItemStack.EMPTY;
            verticalScrollbar = null;
            horizontalScrollbar = null;
            return;
        }
        syncScreenState(screen);
        event.getGuiGraphics().pose().pushMatrix();
        try {
            event.getGuiGraphics().pose().translate(-screen.getLeftPos(), -screen.getTopPos());
            render(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
        } finally {
            event.getGuiGraphics().pose().popMatrix();
        }
    }

    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!active(event.getScreen()) || !inside(event.getMouseX(), event.getMouseY(), panelX, panelY, panelW, panelH)) {
            return;
        }
        event.setCanceled(true);
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && beginScrollbarDrag(event.getMouseX(), event.getMouseY())) {
            return;
        }
        for (Hitbox hitbox : List.copyOf(HITBOXES)) {
            if (inside(event.getMouseX(), event.getMouseY(), hitbox.x(), hitbox.y(), hitbox.w(), hitbox.h())) {
                hitbox.action().click(event.getButton(), event.getMouseButtonEvent().modifiers());
                saveScreenState();
                return;
            }
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && beginPanelDrag(event.getMouseX(), event.getMouseY())) {
            saveScreenState();
            return;
        }
        searchFocused = false;
        saveScreenState();
    }

    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (!active(event.getScreen()) || dragMode == DragMode.NONE) {
            return;
        }
        event.setCanceled(true);
        int dx = (int) Math.round(event.getMouseX() - dragMouseX);
        int dy = (int) Math.round(event.getMouseY() - dragMouseY);
        if (dragMode == DragMode.MOVE) {
            panelX = clamp(dragPanelX + dx, 4, Math.max(4, event.getScreen().width - panelW - 4));
            panelY = clamp(dragPanelY + dy, 4, Math.max(4, event.getScreen().height - panelH - 4));
            storePanelBounds();
        } else if (dragMode == DragMode.RESIZE) {
            panelW = clamp(dragPanelW + dx, 160, Math.max(160, event.getScreen().width - panelX - 4));
            panelH = clamp(dragPanelH + dy, 180, Math.max(180, event.getScreen().height - panelY - 4));
            storePanelBounds();
        } else if (dragMode == DragMode.VERTICAL_SCROLL || dragMode == DragMode.HORIZONTAL_SCROLL) {
            updateScrollbarDrag(event.getMouseX(), event.getMouseY());
        }
    }

    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (dragMode == DragMode.NONE) {
            return;
        }
        boolean panelDrag = dragMode == DragMode.MOVE || dragMode == DragMode.RESIZE;
        dragMode = DragMode.NONE;
        if (panelDrag) {
            storePanelBounds();
        }
        saveScreenState();
        event.setCanceled(true);
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!active(event.getScreen()) || collapsed || !inside(event.getMouseX(), event.getMouseY(), panelX, panelY, panelW, panelH)) {
            return;
        }
        event.setCanceled(true);
        boolean overGrid = inside(event.getMouseX(), event.getMouseY(), lastGridX, lastGridY, lastGridW, lastGridH);
        if (!detailStack.isEmpty() && !overGrid) {
            for (IndexRecipeUi.SlotHit hit : List.copyOf(SLOT_HITS)) {
                if (hit.choiceCyclable() && inside(event.getMouseX(), event.getMouseY(), hit.x(), hit.y(), hit.w(), hit.h())) {
                    IndexRecipeUi.cycleChoice(hit, event.getScrollDeltaY() > 0 ? 1 : -1);
                    saveScreenState();
                    return;
                }
            }
            int max = Math.max(0, detailViews().size() - 1);
            detailSelected = clamp(detailSelected - (int) Math.round(event.getScrollDeltaY()), 0, max);
        } else {
            if (shiftDown()) {
                horizontalScroll = Math.max(0, horizontalScroll - (int) Math.round(event.getScrollDeltaY() * 26.0D));
            } else {
                scroll = Math.max(0, scroll - (int) Math.round(event.getScrollDeltaY() * 26.0D));
            }
        }
        saveScreenState();
    }

    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!active(event.getScreen())) {
            return;
        }
        KeyEvent keyEvent = event.getKeyEvent();
        if (searchFocused) {
            if (keyEvent.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) {
                    search = search.substring(0, search.offsetByCodePoints(search.length(), -1));
                    resetGridScroll();
                }
            } else if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) {
                if (!search.isEmpty()) {
                    search = "";
                    resetGridScroll();
                } else {
                    searchFocused = false;
                }
            } else if (keyEvent.key() == GLFW.GLFW_KEY_ENTER) {
                List<ItemStack> items = gridItems();
                if (!items.isEmpty()) {
                    openDetail(items.getFirst(), IndexRecipeUi.ViewMode.RECIPES);
                }
                searchFocused = false;
            }
            event.setCanceled(true);
            saveScreenState();
            return;
        }
        if (screenHasFocusedInput(event.getScreen())) {
            return;
        }
        if (EchoIndexClient.SHOW_RECIPE_KEY.matches(keyEvent)) {
            ItemStack hoveredInventoryStack = hoveredInventoryStack(event.getScreen());
            if (!hoveredInventoryStack.isEmpty()) {
                Minecraft.getInstance().setScreen(new IndexRecipeScreen(hoveredInventoryStack, IndexRecipeScreen.Mode.RECIPES));
                event.setCanceled(true);
                return;
            }
        }
        if (EchoIndexClient.SHOW_USAGE_KEY.matches(keyEvent)) {
            ItemStack hoveredInventoryStack = hoveredInventoryStack(event.getScreen());
            if (!hoveredInventoryStack.isEmpty()) {
                Minecraft.getInstance().setScreen(new IndexRecipeScreen(hoveredInventoryStack, IndexRecipeScreen.Mode.USES));
                event.setCanceled(true);
                return;
            }
        }
        if (EchoIndexClient.BOOKMARK_KEY.matches(keyEvent) && !hoveredStack.isEmpty()) {
            toggleBookmark(IndexService.itemId(hoveredStack.getItem()));
            event.setCanceled(true);
            return;
        }
        if (!detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_B) {
            toggleBookmark(IndexService.itemId(detailStack.getItem()));
            event.setCanceled(true);
            return;
        }
        if (!detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_P) {
            toggleFocusedRecipePin();
            event.setCanceled(true);
            return;
        }
        if (!detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_R) {
            setDetailMode(IndexRecipeUi.ViewMode.RECIPES);
            event.setCanceled(true);
            return;
        }
        if (!detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_U) {
            setDetailMode(IndexRecipeUi.ViewMode.USES);
            event.setCanceled(true);
            return;
        }
        if (!detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_S) {
            setDetailMode(IndexRecipeUi.ViewMode.SOURCES);
            event.setCanceled(true);
            return;
        }
        if (EchoIndexClient.SHOW_RECIPE_KEY.matches(keyEvent) && !hoveredStack.isEmpty()) {
            openDetail(hoveredStack, IndexRecipeUi.ViewMode.RECIPES);
            event.setCanceled(true);
            return;
        }
        if (EchoIndexClient.SHOW_USAGE_KEY.matches(keyEvent) && !hoveredStack.isEmpty()) {
            openDetail(hoveredStack, IndexRecipeUi.ViewMode.USES);
            event.setCanceled(true);
            return;
        }
        if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE && !detailStack.isEmpty() && !searchFocused) {
            closeDetail();
            event.setCanceled(true);
            return;
        }
        if (!detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_LEFT) {
            if (historyBack()) {
                saveScreenState();
            } else {
                detailSelected = Math.max(0, detailSelected - 1);
            }
            event.setCanceled(true);
            return;
        }
        if (!detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_RIGHT) {
            if (historyForward()) {
                saveScreenState();
            } else {
                detailSelected = Math.min(Math.max(0, detailViews().size() - 1), detailSelected + 1);
            }
            event.setCanceled(true);
            return;
        }
        if (detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_LEFT) {
            horizontalScroll = Math.max(0, horizontalScroll - gridStep());
            event.setCanceled(true);
            return;
        }
        if (detailStack.isEmpty() && keyEvent.key() == GLFW.GLFW_KEY_RIGHT) {
            horizontalScroll += gridStep();
            event.setCanceled(true);
        }
    }

    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (!active(event.getScreen()) || !searchFocused) {
            return;
        }
        CharacterEvent character = event.getCharacterEvent();
        if (character.isAllowedChatCharacter() && search.length() < 80) {
            search += character.codepointAsString();
            resetGridScroll();
            event.setCanceled(true);
        }
    }

    private static void render(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        HITBOXES.clear();
        SLOT_HITS.clear();
        hoveredStack = ItemStack.EMPTY;
        verticalScrollbar = null;
        horizontalScrollbar = null;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        layout(screen);
        Font font = Minecraft.getInstance().font;
        if (collapsed) {
            graphics.fill(panelX, panelY, panelX + 22, panelY + 86, BG);
            graphics.outline(panelX, panelY, 22, 86, CYAN);
            graphics.text(font, text("screen.echoindex.overlay.collapsed"), panelX + 3, panelY + 8, CYAN, false);
            tooltipIfHovered(graphics, font, mouseX, mouseY, panelX, panelY, 22, 86,
                    tr("screen.echoindex.overlay.tooltip.expand"));
            HITBOXES.add(new Hitbox(panelX, panelY, 22, 86, (button, modifiers) -> collapsed = false));
            return;
        }

        drawPanelChrome(graphics);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + HEADER_HEIGHT, 0x33163843);
        graphics.text(font, text("screen.echoindex.overlay.title"), panelX + 10, panelY + 9, CYAN, false);
        int densityX = panelX + panelW - 68;
        button(graphics, font, densityX, panelY + 5, 18, 16, densityLabel(), true);
        tooltipIfHovered(graphics, font, mouseX, mouseY, densityX, panelY + 5, 18, 16,
                tr("screen.echoindex.overlay.tooltip.density"),
                tr("screen.echoindex.overlay.tooltip.density.current", densityName()));
        HITBOXES.add(new Hitbox(densityX, panelY + 5, 18, 16, (button, modifiers) -> cycleGridDensity()));
        int refreshX = panelX + panelW - 46;
        button(graphics, font, refreshX, panelY + 5, 18, 16, text("screen.echoindex.overlay.button.refresh"), true);
        tooltipIfHovered(graphics, font, mouseX, mouseY, refreshX, panelY + 5, 18, 16,
                tr("screen.echoindex.overlay.tooltip.refresh"));
        HITBOXES.add(new Hitbox(refreshX, panelY + 5, 18, 16,
                (button, modifiers) -> {
                    IndexService.INSTANCE.rebuildRecipes(Minecraft.getInstance().player, "overlay refresh button");
                    requestServerSync(true);
                }));
        int collapseX = panelX + panelW - 24;
        button(graphics, font, collapseX, panelY + 5, 16, 16, text("screen.echoindex.overlay.button.collapse"), true);
        tooltipIfHovered(graphics, font, mouseX, mouseY, collapseX, panelY + 5, 16, 16,
                tr("screen.echoindex.overlay.tooltip.collapse"));
        HITBOXES.add(new Hitbox(collapseX, panelY + 5, 16, 16, (button, modifiers) -> collapsed = true));

        int searchX = panelX + INNER_PAD;
        int searchY = panelY + HEADER_HEIGHT + 6;
        int searchW = panelW - INNER_PAD * 2;
        drawSearch(graphics, font, searchX, searchY, searchW);
        categoryFilter = "";
        bookmarkedOnly = false;
        int bodyY = searchY + 25;
        int bodyH = Math.max(48, panelH - (bodyY - panelY) - FOOTER_HEIGHT - 4);

        if (detailStack.isEmpty()) {
            renderGrid(graphics, font, mouseX, mouseY, searchX, bodyY, searchW, bodyH);
        } else if (searchW >= SPLIT_DETAIL_MIN_WIDTH && bodyH >= SPLIT_DETAIL_MIN_HEIGHT) {
            renderSplitDetail(graphics, font, mouseX, mouseY, searchX, bodyY, searchW, bodyH);
        } else {
            renderDetail(graphics, font, mouseX, mouseY, searchX, bodyY, searchW, bodyH);
        }
        drawFooter(graphics, font, searchX, mouseX, mouseY);
        graphics.outline(panelX + panelW - 12, panelY + panelH - 12, 8, 8,
                dragMode == DragMode.RESIZE ? CYAN : 0x6638DFF4);
        tooltipIfHovered(graphics, font, mouseX, mouseY, panelX + panelW - 14, panelY + panelH - 14, 14, 14,
                tr("screen.echoindex.overlay.tooltip.resize"));
    }

    private static void drawPanelChrome(GuiGraphicsExtractor graphics) {
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        if (cinematicStyle()) {
            renderCoreFrame(graphics, panelX, panelY, panelW, panelH);
        } else {
            graphics.outline(panelX, panelY, panelW, panelH, 0x8846DFF4);
            graphics.fill(panelX, panelY, panelX + Math.max(38, panelW / 5), panelY + 2, CYAN);
            graphics.fill(panelX, panelY + panelH - 2, panelX + Math.max(24, panelW / 7), panelY + panelH, CYAN);
        }
    }

    private static void drawSearch(GuiGraphicsExtractor graphics, Font font, int x, int y, int w) {
        graphics.fill(x, y, x + w, y + 19, 0xDD05090E);
        graphics.outline(x, y, w, 19, searchFocused ? CYAN : 0x6638DFF4);
        String label = search.isBlank() && !searchFocused ? text("screen.echoindex.search") : search + (searchFocused ? "_" : "");
        graphics.text(font, trim(font, label, w - 18), x + 6, y + 6, search.isBlank() ? MUTED : TEXT, false);
        HITBOXES.add(new Hitbox(x, y, w, 19, (button, modifiers) -> searchFocused = true));
    }

    private static int drawFilterButtons(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
            int mouseX, int mouseY) {
        ChipCursor cursor = new ChipCursor(x, y);
        cursor = filterChip(graphics, font, x, cursor, width, 34, "All",
                categoryFilter.isBlank() && !bookmarkedOnly, mouseX, mouseY,
                (button, modifiers) -> clearFilters(false));
        cursor = filterChip(graphics, font, x, cursor, width, 48, "Blocks", "$blocks".equals(categoryFilter),
                mouseX, mouseY, (button, modifiers) -> setFilter("$blocks"));
        cursor = filterChip(graphics, font, x, cursor, width, 62, "Machines", "$machines".equals(categoryFilter),
                mouseX, mouseY, (button, modifiers) -> setFilter("$machines"));
        cursor = filterChip(graphics, font, x, cursor, width, 42, "Tools", "$tools".equals(categoryFilter),
                mouseX, mouseY, (button, modifiers) -> setFilter("$tools"));
        cursor = filterChip(graphics, font, x, cursor, width, 56, "Combat", "$combat".equals(categoryFilter),
                mouseX, mouseY, (button, modifiers) -> setFilter("$combat"));
        cursor = filterChip(graphics, font, x, cursor, width, 46, "ECHO", "$echo".equals(categoryFilter),
                mouseX, mouseY, (button, modifiers) -> setFilter("$echo"));
        cursor = filterChip(graphics, font, x, cursor, width, 26, "*", bookmarkedOnly, mouseX, mouseY,
                (button, modifiers) -> {
                    bookmarkedOnly = !bookmarkedOnly;
                    resetGridScroll();
                });
        cursor = filterChip(graphics, font, x, cursor, width, 48, "Clear", false, mouseX, mouseY,
                (button, modifiers) -> clearFilters(true));
        return cursor.y() + 17;
    }

    private static ChipCursor filterChip(GuiGraphicsExtractor graphics, Font font, int rowX, ChipCursor cursor, int rowW,
            int preferredW, String label, boolean selected, int mouseX, int mouseY, ClickAction action) {
        int w = Math.min(Math.max(24, rowW), Math.max(preferredW, font.width(label) + 12));
        int cx = cursor.x();
        int cy = cursor.y();
        if (cx > rowX && cx + w > rowX + rowW) {
            cx = rowX;
            cy += 19;
        }
        chip(graphics, font, cx, cy, w, label, selected, mouseX, mouseY);
        HITBOXES.add(new Hitbox(cx, cy, w, 17, action));
        return new ChipCursor(cx + w + 4, cy);
    }

    private static int drawActiveTokens(GuiGraphicsExtractor graphics, Font font, int x, int y, int w) {
        List<String> tokens = new ArrayList<>();
        if (!search.isBlank()) {
            tokens.add("\"" + search.trim() + "\"");
        }
        if (!categoryFilter.isBlank()) {
            tokens.add(categoryFilter);
        }
        if (bookmarkedOnly) {
            tokens.add("bookmarked");
        }
        if (tokens.isEmpty()) {
            return 0;
        }
        graphics.text(font, trim(font, "Filters: " + String.join("  ", tokens), w), x, y, MUTED, false);
        return 12;
    }

    private static void renderGrid(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            int gridX, int gridY, int gridW, int gridH) {
        lastGridX = gridX;
        lastGridY = gridY;
        lastGridW = gridW;
        lastGridH = gridH;
        List<ItemStack> items = gridItems();
        GridLayout grid = gridLayout(gridW, gridH, items.size());
        int step = grid.step();
        int slot = grid.slot();
        int columns = grid.columns();
        int rows = (items.size() + columns - 1) / columns;
        int contentH = rows * step;
        int maxVerticalScroll = Math.max(0, contentH - gridH);
        scroll = clamp(scroll, 0, maxVerticalScroll);
        horizontalScroll = 0;
        graphics.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        int startY = gridY - scroll;
        int firstRow = Math.max(0, scroll / step);
        int lastRow = Math.min(Math.max(0, rows - 1), (scroll + gridH) / step + 1);
        for (int row = firstRow; row <= lastRow; row++) {
            int y = startY + row * step;
            if (y < gridY - step || y > gridY + gridH) {
                continue;
            }
            for (int column = 0; column < columns; column++) {
                int i = row * columns + column;
                if (i >= items.size()) {
                    break;
                }
                ItemStack stack = items.get(i);
                int x = gridX + grid.columnOffset(column);
                itemSlot(graphics, font, stack, x, y, slot, mouseX, mouseY);
                Identifier itemId = IndexService.itemId(stack.getItem());
                if (ClientIndexState.isBookmarked(itemId)) {
                    graphics.text(font, "*", x + Math.max(12, slot - 5), y - 1, WARN, false);
                }
                HITBOXES.add(new Hitbox(x, y, slot, slot, (button, modifiers) -> {
                    if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 || button == 2) {
                        toggleBookmark(itemId);
                    } else {
                        openDetail(stack, button == 1 ? IndexRecipeUi.ViewMode.USES : IndexRecipeUi.ViewMode.RECIPES);
                    }
                }));
            }
        }
        graphics.disableScissor();
        drawGridScrollbars(graphics, font, gridX, gridY, gridW, gridH, gridW, contentH);
        if (items.isEmpty()) {
            graphics.text(font, text("screen.echoindex.no_results"), gridX, gridY + 8, MUTED, false);
        }
    }

    private static void renderSplitDetail(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            int x, int y, int w, int h) {
        int gridW = Math.min(118, Math.max(92, w / 3));
        graphics.text(font, "Browse", x, y, MUTED, false);
        renderGrid(graphics, font, mouseX, mouseY, x, y + 12, gridW, Math.max(40, h - 12));
        int detailX = x + gridW + 8;
        renderDetail(graphics, font, mouseX, mouseY, detailX, y, w - gridW - 8, h);
    }

    private static void renderDetail(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            int x, int y, int w, int h) {
        List<IndexRecipeView> allViews = detailViews();
        detailSelected = clamp(detailSelected, 0, Math.max(0, allViews.size() - 1));
        graphics.fill(x, y, x + w, y + h, 0x99071117);
        button(graphics, font, x, y, 22, 17, "<", historyIndex > 0);
        HITBOXES.add(new Hitbox(x, y, 22, 17, (button, modifiers) -> historyBack()));
        button(graphics, font, x + 24, y, 22, 17, ">", historyIndex + 1 < DETAIL_HISTORY.size());
        HITBOXES.add(new Hitbox(x + 24, y, 22, 17, (button, modifiers) -> historyForward()));
        button(graphics, font, x + 48, y, 54, 17, "Results", true);
        HITBOXES.add(new Hitbox(x + 48, y, 54, 17, (button, modifiers) -> closeDetail()));
        button(graphics, font, x + w - 46, y, 46, 17, "Open", true);
        HITBOXES.add(new Hitbox(x + w - 46, y, 46, 17, (button, modifiers) ->
                Minecraft.getInstance().setScreen(new IndexRecipeScreen(detailStack, screenMode(detailMode)))));
        if (w >= 240) {
            graphics.item(detailStack, x + 106, y + 1);
            graphics.text(font, trim(font, detailStack.getHoverName().getString(), Math.max(36, w - 160)), x + 126, y + 5,
                    ClientIndexState.isBookmarked(IndexService.itemId(detailStack.getItem())) ? WARN : TEXT, false);
        }

        int chipY = y + 22;
        int modeGap = 4;
        int modeW = Math.max(42, (w - modeGap * 2) / 3);
        modeChip(graphics, font, x, chipY, modeW, IndexRecipeUi.ViewMode.RECIPES, mouseX, mouseY);
        modeChip(graphics, font, x + modeW + modeGap, chipY, modeW, IndexRecipeUi.ViewMode.USES, mouseX, mouseY);
        modeChip(graphics, font, x + (modeW + modeGap) * 2, chipY,
                Math.max(42, w - (modeW + modeGap) * 2), IndexRecipeUi.ViewMode.SOURCES, mouseX, mouseY);

        boolean compactRails = h < 260 || w < 260;
        int railY = chipY + 21;
        if (!compactRails) {
            int recentH = drawRecent(graphics, font, x, railY, w, mouseX, mouseY);
            railY += recentH;
        }
        boolean showCategories = !compactRails || h >= 220;
        if (showCategories) {
            drawCategoryChips(graphics, font, x, railY, w, allViews, mouseX, mouseY);
            railY += 22;
        }
        IndexRecipeView selectedTraceRecipe = allViews.isEmpty() ? null : allViews.get(detailSelected);
        int traceH = drawTracePath(graphics, font, x, railY, w, selectedTraceRecipe, mouseX, mouseY);
        int cardY = railY + traceH;
        int reservedBottom = 43;
        int cardH = Math.max(70, h - (cardY - y) - reservedBottom);
        IndexRecipeUi.recordCardSelection(detailMode, detailSelected, allViews.size());
        if (allViews.isEmpty()) {
            graphics.fill(x + 2, cardY, x + w - 2, cardY + Math.min(64, cardH), 0xAA071014);
            graphics.textWithWordWrap(font, net.minecraft.network.chat.Component.literal(
                    IndexRecipeUi.emptyMessage(Minecraft.getInstance().player, detailStack.getItem(), detailMode)),
                    x + 12, cardY + 14, w - 24, MUTED);
            return;
        }
        IndexRecipeView recipe = allViews.get(detailSelected);
        IndexRecipeUi.drawRecipeCard(graphics, font, recipe, x + 2, cardY, w - 4, cardH,
                detailStack, mouseX, mouseY, SLOT_HITS);
        int actionY = cardY + cardH + 4;
        drawRecipeActions(graphics, font, recipe, x + 8, actionY, w - 16, mouseX, mouseY, true);
        for (IndexRecipeUi.SlotHit hit : SLOT_HITS) {
            HITBOXES.add(new Hitbox(hit.x(), hit.y(), hit.w(), hit.h(), (button, modifiers) -> {
                if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 || button == 2) {
                    toggleBookmark(IndexService.itemId(hit.stack().getItem()));
                } else if (button == 1 && hit.choiceCyclable()) {
                    IndexRecipeUi.cycleChoice(hit, 1);
                } else {
                    openDetail(hit.stack(), modeForSlot(hit, button));
                }
            }));
        }
        String page = (detailSelected + 1) + " / " + allViews.size();
        button(graphics, font, x + Math.max(0, w / 2 - 48), y + h - 19, 24, 16, "<", detailSelected > 0);
        HITBOXES.add(new Hitbox(x + Math.max(0, w / 2 - 48), y + h - 19, 24, 16,
                (button, modifiers) -> detailSelected = Math.max(0, detailSelected - 1)));
        graphics.centeredText(font, page, x + w / 2, y + h - 15, MUTED);
        button(graphics, font, x + Math.min(w - 24, w / 2 + 24), y + h - 19, 24, 16, ">", detailSelected + 1 < allViews.size());
        HITBOXES.add(new Hitbox(x + Math.min(w - 24, w / 2 + 24), y + h - 19, 24, 16,
                (button, modifiers) -> detailSelected = Math.min(allViews.size() - 1, detailSelected + 1)));
    }

    private static void drawRecipeActions(GuiGraphicsExtractor graphics, Font font, IndexRecipeView recipe,
            int x, int y, int w, int mouseX, int mouseY, boolean allowTransfer) {
        IndexRecipePlan plan = IndexRecipePlanner.plan(Minecraft.getInstance().player, recipe);
        int pinW = plan.pinned() ? 48 : 34;
        button(graphics, font, x, y, pinW, 16, plan.pinned() ? "Unpin" : "Pin", true);
        HITBOXES.add(new Hitbox(x, y, pinW, 16, (button, modifiers) -> sendRecipeAction(
                plan.pinned() ? IndexActionPacket.Action.UNPIN_RECIPE : IndexActionPacket.Action.PIN_RECIPE,
                recipe.id())));
        int tx = x + pinW + 5;
        if (plan.missingCount() > 0 && !detailStack.isEmpty()) {
            button(graphics, font, tx, y, 44, 16, "Trace", true);
            HITBOXES.add(new Hitbox(tx, y, 44, 16,
                    (button, modifiers) -> IndexRecipeTraceState.open(detailStack, recipe, plan)));
            tx += 49;
        }
        if (allowTransfer && plan.canTransfer()) {
            button(graphics, font, tx, y, 54, 16, "Transfer", true);
            HITBOXES.add(new Hitbox(tx, y, 54, 16,
                    (button, modifiers) -> sendRecipeAction(IndexActionPacket.Action.TRANSFER_RECIPE, recipe.id())));
        } else {
            String note = IndexRecipeUi.statusDetail(plan, allowTransfer);
            if (!note.isBlank()) {
                graphics.text(font, trim(font, note, Math.max(30, x + w - tx - 4)), tx, y + 5,
                        IndexRecipeUi.statusColor(plan, allowTransfer), false);
            }
        }
    }

    private static int drawTracePath(GuiGraphicsExtractor graphics, Font font, int x, int y, int w,
            IndexRecipeView selectedRecipe, int mouseX, int mouseY) {
        IndexRecipeTraceState.Trace trace = IndexRecipeTraceState.current();
        if (!traceApplies(trace)) {
            return 0;
        }
        int h = trace.entries().isEmpty() ? 18 : 42;
        graphics.fill(x, y, x + w, y + h, 0x88102630);
        graphics.outline(x, y, w, h, 0x5538DFF4);
        String path = "Path: " + trace.rootStack().getHoverName().getString() + " > missing inputs";
        graphics.text(font, trim(font, path, w - 84), x + 6, y + 6, MUTED, false);
        button(graphics, font, x + w - 76, y + 2, 70, 16,
                selectedRecipe != null && selectedRecipe.id().equals(trace.rootRecipeId()) ? "Tracing" : "Root", true);
        HITBOXES.add(new Hitbox(x + w - 76, y + 2, 70, 16,
                (button, modifiers) -> openRecipe(trace.rootRecipeId())));
        if (trace.entries().isEmpty()) {
            return h + 4;
        }
        int cx = x + 6;
        int cy = y + 22;
        for (IndexRecipeTraceState.TraceEntry entry : trace.entries().stream().limit(4).toList()) {
            int chipW = Math.min(92, Math.max(42, font.width(entry.stack().getHoverName().getString()) + 20));
            if (cx + chipW > x + w - 4) {
                break;
            }
            boolean selected = !detailStack.isEmpty() && IndexService.itemId(detailStack.getItem()).equals(entry.itemId());
            chip(graphics, font, cx, cy, chipW, entry.stack().getHoverName().getString(), selected, mouseX, mouseY);
            if (inside(mouseX, mouseY, cx, cy, chipW, 17)) {
                graphics.setComponentTooltipForNextFrame(font, List.of(
                        entry.stack().getHoverName(),
                        net.minecraft.network.chat.Component.literal(entry.countLabel()),
                        net.minecraft.network.chat.Component.literal(entry.dataLabel())),
                        cx + chipW / 2, cy + 9);
            }
            HITBOXES.add(new Hitbox(cx, cy, chipW, 17,
                    (button, modifiers) -> openDetail(entry.stack(), IndexRecipeUi.ViewMode.RECIPES)));
            cx += chipW + 4;
        }
        return h + 4;
    }

    private static boolean traceApplies(IndexRecipeTraceState.Trace trace) {
        if (trace == null || !trace.active() || detailStack.isEmpty()) {
            return false;
        }
        Identifier detailId = IndexService.itemId(detailStack.getItem());
        if (detailId.equals(trace.rootItemId())) {
            return true;
        }
        return trace.entries().stream().anyMatch(entry -> detailId.equals(entry.itemId()));
    }

    private static IndexRecipeUi.ViewMode modeForSlot(IndexRecipeUi.SlotHit hit, int button) {
        if (button == 1) {
            return IndexRecipeUi.ViewMode.USES;
        }
        IndexSlotRole role = hit.role();
        return role == IndexSlotRole.OUTPUT ? IndexRecipeUi.ViewMode.RECIPES : IndexRecipeUi.ViewMode.USES;
    }

    private static void modeChip(GuiGraphicsExtractor graphics, Font font, int x, int y, int w,
            IndexRecipeUi.ViewMode mode, int mouseX, int mouseY) {
        int count = modeCount(mode);
        chip(graphics, font, x, y, w, modeLabel(mode) + " " + count, detailMode == mode, mouseX, mouseY);
        HITBOXES.add(new Hitbox(x, y, w, 17, (button, modifiers) -> setDetailMode(mode)));
    }

    private static int drawRecent(GuiGraphicsExtractor graphics, Font font, int x, int y, int w, int mouseX, int mouseY) {
        List<HistoryEntry> recent = recentHistory();
        if (recent.isEmpty()) {
            return 0;
        }
        graphics.text(font, "Recent", x, y + 5, MUTED, false);
        int cx = x + 42;
        for (HistoryEntry entry : recent) {
            ItemStack stack = itemStack(entry.itemId());
            if (stack.isEmpty()) {
                continue;
            }
            boolean selected = !detailStack.isEmpty() && IndexService.itemId(detailStack.getItem()).equals(entry.itemId());
            graphics.fill(cx, y, cx + 20, y + 20, selected ? 0xFF123241 : ROW);
            graphics.outline(cx, y, 20, 20, selected ? CYAN : 0x5538DFF4);
            graphics.item(stack, cx + 2, y + 2);
            if (inside(mouseX, mouseY, cx, y, 20, 20)) {
                graphics.setTooltipForNextFrame(font, stack, cx + 10, y + 10);
            }
            HITBOXES.add(new Hitbox(cx, y, 20, 20, (button, modifiers) -> restoreHistory(entry)));
            cx += 23;
            if (cx + 20 > x + w) {
                break;
            }
        }
        return 23;
    }

    private static void drawCategoryChips(GuiGraphicsExtractor graphics, Font font, int x, int y, int w,
            List<IndexRecipeView> views, int mouseX, int mouseY) {
        chip(graphics, font, x, y, 34, "All", detailCategory == null, mouseX, mouseY);
        HITBOXES.add(new Hitbox(x, y, 34, 17, (button, modifiers) -> {
            detailCategory = null;
            detailSelected = 0;
        }));
        Set<Identifier> categories = new LinkedHashSet<>();
        for (IndexRecipeView view : baseDetailViews()) {
            categories.add(view.categoryId());
        }
        int cx = x + 38;
        for (Identifier category : categories.stream().limit(3).toList()) {
            int chipW = Math.min(72, Math.max(42, font.width(category.getPath()) + 12));
            if (cx + chipW > x + w) {
                break;
            }
            chip(graphics, font, cx, y, chipW, category.getPath(), category.equals(detailCategory), mouseX, mouseY);
            HITBOXES.add(new Hitbox(cx, y, chipW, 17, (button, modifiers) -> {
                detailCategory = category.equals(detailCategory) ? null : category;
                detailSelected = 0;
            }));
            cx += chipW + 4;
        }
    }

    private static void drawFooter(GuiGraphicsExtractor graphics, Font font, int x, int mouseX, int mouseY) {
        IndexRecipeSnapshot snapshot = IndexService.INSTANCE.recipeSnapshot(Minecraft.getInstance().player);
        int footerY = panelY + panelH - 20;
        graphics.fill(panelX + 1, footerY - 3, panelX + panelW - 1, panelY + panelH - 1, 0xAA071017);
        String left = detailStack.isEmpty() ? text("screen.echoindex.overlay.footer.index") : modeLabel(detailMode);
        graphics.text(font, left, x, footerY, CYAN, false);
        int footerMetaX = x + Math.min(86, Math.max(56, panelW / 4));
        int drawerEnd = panelW >= 330
                ? drawPinnedDrawer(graphics, font, footerMetaX, footerY - 4, panelX + panelW - 60, mouseX, mouseY)
                : footerMetaX;
        int warnings = snapshot.warnings().size();
        if (warnings > 0 || snapshot.recipesStillLoading() || Config.DEBUG_SHOW_RECIPE_IDS.get()) {
            int bx = panelX + panelW - 54;
            String label = snapshot.recipesStillLoading()
                    ? text("screen.echoindex.overlay.diagnostics.loading_short")
                    : text("screen.echoindex.overlay.diagnostics.warning_short");
            button(graphics, font, bx, footerY - 4, 44, 16, label, warnings == 0);
            if (inside(mouseX, mouseY, bx, footerY - 4, 44, 16)) {
                graphics.setComponentTooltipForNextFrame(font, List.of(
                        tr("screen.echoindex.overlay.tooltip.diagnostics"),
                        tr(snapshot.recipesStillLoading()
                                ? "screen.echoindex.overlay.tooltip.diagnostics.loading"
                                : "screen.echoindex.overlay.tooltip.diagnostics.open")),
                        mouseX, mouseY);
            }
            HITBOXES.add(new Hitbox(bx, footerY - 4, 44, 16,
                    (button, modifiers) -> Minecraft.getInstance().setScreen(new IndexDiagnosticsScreen())));
        } else if (panelX + panelW - drawerEnd > 72) {
            graphics.text(font, trim(font, text("screen.echoindex.overlay.footer.hints"), panelX + panelW - drawerEnd - 12),
                    drawerEnd, footerY, MUTED, false);
        }
    }

    private static int drawPinnedDrawer(GuiGraphicsExtractor graphics, Font font, int x, int y, int maxX,
            int mouseX, int mouseY) {
        Set<Identifier> pinned = ClientIndexState.pinnedRecipes();
        if (pinned.isEmpty() || x + 20 > maxX) {
            return x;
        }
        graphics.text(font, text("screen.echoindex.overlay.footer.pins"), x, y + 5, WARN, false);
        int cx = x + 28;
        IndexRecipeSnapshot snapshot = IndexService.INSTANCE.recipeSnapshot(Minecraft.getInstance().player);
        List<Identifier> orderedPins = pinned.stream()
                .sorted((left, right) -> Integer.compare(pinGroup(snapshot.byId().get(left)), pinGroup(snapshot.byId().get(right))))
                .limit(6)
                .toList();
        for (Identifier id : orderedPins) {
            IndexRecipeView recipe = snapshot.byId().get(id);
            if (recipe == null || cx + 22 > maxX) {
                continue;
            }
            ItemStack icon = IndexRecipeUi.recipeIcon(recipe, ItemStack.EMPTY);
            IndexRecipePlan plan = IndexRecipePlanner.plan(Minecraft.getInstance().player, recipe);
            graphics.fill(cx, y, cx + 20, y + 20, ROW);
            graphics.outline(cx, y, 20, 20, IndexRecipeUi.statusColor(plan, true));
            graphics.item(icon, cx + 2, y + 2);
            if (plan.missingCount() > 0) {
                graphics.text(font, "!", cx + 14, y + 10, WARN, false);
            } else if (plan.sourceCard()) {
                graphics.text(font, "S", cx + 14, y + 10, MUTED, false);
            }
            if (inside(mouseX, mouseY, cx, y, 20, 20)) {
                graphics.setComponentTooltipForNextFrame(font, List.of(icon.getHoverName(),
                        Component.literal(IndexRecipeUi.statusLabel(plan, true)),
                        pinTooltip(plan)),
                        cx + 10, y + 10);
            }
            HITBOXES.add(new Hitbox(cx, y, 20, 20, (button, modifiers) -> openRecipe(recipe.id())));
            cx += 23;
        }
        return cx + 4;
    }

    private static int pinGroup(IndexRecipeView recipe) {
        if (recipe == null) {
            return 4;
        }
        IndexRecipePlan plan = IndexRecipePlanner.plan(Minecraft.getInstance().player, recipe);
        if (plan.canTransfer()) {
            return 0;
        }
        if (plan.missingCount() > 0) {
            return 1;
        }
        if (plan.sourceCard()) {
            return 3;
        }
        return 2;
    }

    private static Component pinTooltip(IndexRecipePlan plan) {
        if (plan == null) {
            return tr("screen.echoindex.overlay.tooltip.pin.unavailable");
        }
        if (plan.canTransfer()) {
            return tr("screen.echoindex.overlay.tooltip.pin.ready");
        }
        if (plan.missingCount() > 0) {
            return tr("screen.echoindex.overlay.tooltip.pin.missing", plan.missingCount());
        }
        if (plan.sourceCard()) {
            return tr("screen.echoindex.overlay.tooltip.pin.source");
        }
        return plan.transferBlocker().isBlank()
                ? tr("screen.echoindex.overlay.tooltip.pin.plan_only")
                : Component.literal(plan.transferBlocker());
    }

    private static boolean active(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> container) || Minecraft.getInstance().player == null) {
            return false;
        }
        String name = screen.getClass().getName();
        return IndexService.INSTANCE.overlayEnabled(Minecraft.getInstance().player)
                && !IndexService.INSTANCE.excludedScreen(name)
                && !name.contains("IndexDiagnosticsScreen")
                && container.getImageWidth() > 0;
    }

    private static boolean screenHasFocusedInput(Screen screen) {
        GuiEventListener focused = screen.getFocused();
        return focused != null && focused.isFocused();
    }

    private static ItemStack hoveredInventoryStack(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) {
            return ItemStack.EMPTY;
        }
        int left = container.getLeftPos();
        int top = container.getTopPos();
        for (Slot slot : container.getMenu().slots) {
            if (slot == null || !slot.isActive() || !slot.hasItem()) {
                continue;
            }
            if (inside(lastMouseX, lastMouseY, left + slot.x, top + slot.y, 16, 16)) {
                return slot.getItem().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static void layout(Screen screen) {
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) screen;
        int margin = 6;
        int gap = 8;
        int minW = 160;
        int requestedW = Config.OVERLAY_WIDTH.get();
        Config.OverlayLayout overlayLayout = Config.OVERLAY_LAYOUT.get();
        if (overlayLayout == Config.OverlayLayout.COMPACT && requestedW <= 238) {
            overlayLayout = Config.OverlayLayout.JEI;
            requestedW = 300;
        }
        int maxScreenW = Math.max(minW, screen.width - margin * 2);
        int desiredW = clamp(requestedW, minW, maxScreenW);
        int availableH = Math.max(180, screen.height - margin * 2);
        int compactH = clamp(Math.max(container.getImageHeight(), 220), 160, availableH);
        int tallH = Math.min(Math.max(340, availableH * 4 / 5), availableH);
        panelH = switch (overlayLayout) {
            case COMPACT -> compactH;
            case TALL -> tallH;
            case JEI -> availableH;
        };
        int desiredY = switch (overlayLayout) {
            case COMPACT -> container.getTopPos();
            case TALL -> container.getTopPos() + container.getImageHeight() / 2 - panelH / 2;
            case JEI -> margin;
        };
        panelY = clamp(desiredY, margin, Math.max(margin, screen.height - panelH - margin));

        int rightStart = container.getLeftPos() + container.getImageWidth() + gap;
        int rightSpace = screen.width - margin - rightStart;
        int leftSpace = container.getLeftPos() - gap - margin;
        boolean preferLeft = Config.OVERLAY_SIDE.get() == Config.OverlaySide.LEFT;
        boolean useLeft;
        if (preferLeft && leftSpace >= minW) {
            useLeft = true;
        } else if (!preferLeft && rightSpace >= minW) {
            useLeft = false;
        } else if (rightSpace >= minW) {
            useLeft = false;
        } else if (leftSpace >= minW) {
            useLeft = true;
        } else {
            useLeft = preferLeft;
        }
        int available = useLeft ? leftSpace : rightSpace;
        int targetW = desiredW;
        if (!detailStack.isEmpty()) {
            targetW = Math.max(targetW, Math.min(500, Math.max(minW, available)));
        }
        panelW = clamp(Math.min(targetW, Math.max(minW, available)), minW, maxScreenW);
        panelX = useLeft ? container.getLeftPos() - gap - panelW : rightStart;
        panelX = clamp(panelX, margin, Math.max(margin, screen.width - panelW - margin));
        PanelBounds saved = PANEL_BOUNDS.get(activeScreenKey);
        if (saved != null) {
            panelW = clamp(saved.w(), minW, maxScreenW);
            panelH = clamp(saved.h(), 180, availableH);
            panelX = clamp(saved.x(), margin, Math.max(margin, screen.width - panelW - margin));
            panelY = clamp(saved.y(), margin, Math.max(margin, screen.height - panelH - margin));
        }
    }

    private static boolean beginPanelDrag(double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, panelX + panelW - 14, panelY + panelH - 14, 14, 14)) {
            dragMode = DragMode.RESIZE;
        } else if (inside(mouseX, mouseY, panelX, panelY, panelW, 25)) {
            dragMode = DragMode.MOVE;
        } else {
            dragMode = DragMode.NONE;
            return false;
        }
        dragMouseX = (int) Math.round(mouseX);
        dragMouseY = (int) Math.round(mouseY);
        dragPanelX = panelX;
        dragPanelY = panelY;
        dragPanelW = panelW;
        dragPanelH = panelH;
        return true;
    }

    private static void storePanelBounds() {
        if (!activeScreenKey.isBlank()) {
            PANEL_BOUNDS.put(activeScreenKey, new PanelBounds(panelX, panelY, panelW, panelH));
        }
    }

    private static void itemSlot(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        itemSlot(graphics, font, stack, x, y, 20, mouseX, mouseY);
    }

    private static void itemSlot(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, int slotSize,
            int mouseX, int mouseY) {
        int size = Math.max(18, slotSize);
        int inset = Math.max(1, (size - 16) / 2);
        boolean hover = inside(mouseX, mouseY, x, y, size, size);
        graphics.fill(x, y, x + size, y + size, ROW);
        graphics.outline(x, y, size, size, hover ? CYAN : 0x5538DFF4);
        graphics.item(stack, x + inset, y + inset);
        graphics.itemDecorations(font, stack, x + inset, y + inset);
        if (hover) {
            hoveredStack = stack;
            graphics.setTooltipForNextFrame(font, itemTooltip(stack), stack.getTooltipImage(), mouseX, mouseY);
        }
    }

    private static int slotSize() {
        return switch (currentGridDensity()) {
            case COMPACT -> 18;
            case LARGE -> 24;
            case NORMAL -> 20;
        };
    }

    private static int gridStep() {
        return slotSize() + switch (currentGridDensity()) {
            case COMPACT -> 3;
            case LARGE -> 6;
            case NORMAL -> 4;
        };
    }

    private static GridLayout gridLayout(int gridW, int gridH, int itemCount) {
        GridLayout full = gridLayout(gridW, false);
        int rows = (itemCount + full.columns() - 1) / full.columns();
        if (rows * full.step() <= gridH) {
            return full;
        }
        return gridLayout(gridW, true);
    }

    private static GridLayout gridLayout(int gridW, boolean reserveScrollbar) {
        int slot = slotSize();
        int step = gridStep();
        int gap = Math.max(1, step - slot);
        int usableW = Math.max(slot, gridW - (reserveScrollbar ? GRID_SCROLLBAR_GUTTER : 0));
        int maxColumns = Math.max(1, Config.OVERLAY_MAX_COLUMNS.get());
        int fitColumns = Math.max(1, (usableW + gap) / step);
        int columns = Math.max(1, Math.min(maxColumns, fitColumns));
        return new GridLayout(slot, step, columns, usableW);
    }

    private static Config.GridDensity currentGridDensity() {
        if (gridDensityOverridden) {
            return gridDensity;
        }
        try {
            gridDensity = Config.OVERLAY_GRID_DENSITY.get();
        } catch (RuntimeException exception) {
            gridDensity = Config.GridDensity.NORMAL;
        }
        return gridDensity;
    }

    private static String densityLabel() {
        return switch (currentGridDensity()) {
            case COMPACT -> "C";
            case NORMAL -> "N";
            case LARGE -> "L";
        };
    }

    private static String densityName() {
        return switch (currentGridDensity()) {
            case COMPACT -> text("screen.echoindex.overlay.density.compact");
            case NORMAL -> text("screen.echoindex.overlay.density.normal");
            case LARGE -> text("screen.echoindex.overlay.density.large");
        };
    }

    private static void cycleGridDensity() {
        gridDensity = switch (currentGridDensity()) {
            case COMPACT -> Config.GridDensity.NORMAL;
            case NORMAL -> Config.GridDensity.LARGE;
            case LARGE -> Config.GridDensity.COMPACT;
        };
        gridDensityOverridden = true;
        resetGridScroll();
        gridCacheKey = null;
        saveScreenState();
    }

    private static List<ItemStack> gridItems() {
        String query = effectiveSearch();
        GridCacheKey key = new GridCacheKey(activeScreenKey, query, ClientIndexState.revision());
        if (!key.equals(gridCacheKey)) {
            gridCacheKey = key;
            gridCacheItems = List.copyOf(IndexService.INSTANCE.filteredItemsUnbounded(Minecraft.getInstance().player, query));
        }
        return gridCacheItems;
    }

    private static List<IndexRecipeView> baseDetailViews() {
        if (detailStack.isEmpty()) {
            return List.of();
        }
        DetailBaseCacheKey key = new DetailBaseCacheKey(activeScreenKey, IndexService.itemId(detailStack.getItem()),
                detailMode, recipeSnapshotGeneration(), ClientIndexState.revision(), IndexRecipeQueryClientState.revision());
        if (!key.equals(detailBaseCacheKey)) {
            detailBaseCacheKey = key;
            detailBaseCacheViews = List.copyOf(
                    IndexRecipeUi.viewsFor(Minecraft.getInstance().player, detailStack.getItem(), detailMode));
            detailCacheKey = null;
        }
        return detailBaseCacheViews;
    }

    private static List<IndexRecipeView> detailViews() {
        List<IndexRecipeView> views = baseDetailViews();
        DetailCacheKey key = new DetailCacheKey(detailBaseCacheKey, detailCategory);
        if (!key.equals(detailCacheKey)) {
            detailCacheKey = key;
            detailCacheViews = detailCategory == null ? views
                    : views.stream().filter(view -> detailCategory.equals(view.categoryId())).toList();
        }
        return detailCacheViews;
    }

    private static int modeCount(IndexRecipeUi.ViewMode mode) {
        if (detailStack.isEmpty()) {
            return 0;
        }
        ModeCountKey key = new ModeCountKey(activeScreenKey, IndexService.itemId(detailStack.getItem()), mode,
                recipeSnapshotGeneration(), ClientIndexState.revision(), IndexRecipeQueryClientState.revision());
        Integer cached = MODE_COUNT_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        int count = IndexRecipeUi.viewsFor(Minecraft.getInstance().player, detailStack.getItem(), mode).size();
        if (MODE_COUNT_CACHE.size() > 64) {
            MODE_COUNT_CACHE.clear();
        }
        MODE_COUNT_CACHE.put(key, count);
        return count;
    }

    private static long recipeSnapshotGeneration() {
        return IndexService.INSTANCE.recipeSnapshot(Minecraft.getInstance().player).generation();
    }

    private static void openDetail(ItemStack stack, IndexRecipeUi.ViewMode mode) {
        detailStack = stack == null ? ItemStack.EMPTY : stack.copy();
        detailMode = mode == null ? IndexRecipeUi.ViewMode.RECIPES : mode;
        detailCategory = null;
        detailSelected = 0;
        searchFocused = false;
        resetGridScroll();
        pushHistory(detailStack, detailMode);
        saveScreenState();
    }

    private static void closeDetail() {
        detailStack = ItemStack.EMPTY;
        detailCategory = null;
        detailSelected = 0;
        saveScreenState();
    }

    private static void openRecipe(Identifier recipeId) {
        IndexRecipeView recipe = IndexService.INSTANCE.recipeSnapshot(Minecraft.getInstance().player).byId().get(recipeId);
        if (recipe == null) {
            recipe = IndexRecipeQueryClientState.recipe(recipeId).orElse(null);
        }
        if (recipe == null) {
            return;
        }
        ItemStack stack = IndexRecipeUi.recipeIcon(recipe, ItemStack.EMPTY);
        if (stack.isEmpty()) {
            return;
        }
        IndexRecipeUi.ViewMode mode = IndexRecipeUi.sourceCard(recipe)
                ? IndexRecipeUi.ViewMode.SOURCES
                : IndexRecipeUi.ViewMode.RECIPES;
        openDetail(stack, mode);
        detailCategory = recipe.categoryId();
        List<IndexRecipeView> views = detailViews();
        for (int i = 0; i < views.size(); i++) {
            if (views.get(i).id().equals(recipe.id())) {
                detailSelected = i;
                break;
            }
        }
    }

    private static void toggleFocusedRecipePin() {
        List<IndexRecipeView> views = detailViews();
        if (views.isEmpty()) {
            return;
        }
        IndexRecipeView recipe = views.get(clamp(detailSelected, 0, views.size() - 1));
        boolean pinned = ClientIndexState.isRecipePinned(recipe.id());
        sendRecipeAction(pinned ? IndexActionPacket.Action.UNPIN_RECIPE : IndexActionPacket.Action.PIN_RECIPE,
                recipe.id());
    }

    private static void setDetailMode(IndexRecipeUi.ViewMode mode) {
        detailMode = mode == null ? IndexRecipeUi.ViewMode.RECIPES : mode;
        detailCategory = null;
        detailSelected = 0;
        if (!detailStack.isEmpty()) {
            pushHistory(detailStack, detailMode);
        }
        saveScreenState();
    }

    private static void pushHistory(ItemStack stack, IndexRecipeUi.ViewMode mode) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        HistoryEntry entry = new HistoryEntry(IndexService.itemId(stack.getItem()), mode);
        if (historyIndex >= 0 && historyIndex < DETAIL_HISTORY.size() && DETAIL_HISTORY.get(historyIndex).equals(entry)) {
            return;
        }
        while (DETAIL_HISTORY.size() > historyIndex + 1) {
            DETAIL_HISTORY.removeLast();
        }
        DETAIL_HISTORY.add(entry);
        while (DETAIL_HISTORY.size() > MAX_HISTORY) {
            DETAIL_HISTORY.removeFirst();
        }
        historyIndex = DETAIL_HISTORY.size() - 1;
    }

    private static boolean historyBack() {
        if (historyIndex <= 0) {
            return false;
        }
        historyIndex--;
        restoreHistory(DETAIL_HISTORY.get(historyIndex));
        return true;
    }

    private static boolean historyForward() {
        if (historyIndex + 1 >= DETAIL_HISTORY.size()) {
            return false;
        }
        historyIndex++;
        restoreHistory(DETAIL_HISTORY.get(historyIndex));
        return true;
    }

    private static void restoreHistory(HistoryEntry entry) {
        ItemStack stack = itemStack(entry.itemId());
        if (stack.isEmpty()) {
            return;
        }
        detailStack = stack;
        detailMode = entry.mode();
        detailCategory = null;
        detailSelected = 0;
        searchFocused = false;
        for (int i = 0; i < DETAIL_HISTORY.size(); i++) {
            if (DETAIL_HISTORY.get(i).equals(entry)) {
                historyIndex = i;
                break;
            }
        }
        saveScreenState();
    }

    private static List<HistoryEntry> recentHistory() {
        List<HistoryEntry> recent = new ArrayList<>();
        for (int i = DETAIL_HISTORY.size() - 1; i >= 0 && recent.size() < 5; i--) {
            HistoryEntry entry = DETAIL_HISTORY.get(i);
            if (recent.stream().noneMatch(existing -> existing.itemId().equals(entry.itemId()))) {
                recent.add(entry);
            }
        }
        return recent;
    }

    private static IndexRecipeScreen.Mode screenMode(IndexRecipeUi.ViewMode mode) {
        return switch (mode) {
            case USES -> IndexRecipeScreen.Mode.USES;
            case SOURCES -> IndexRecipeScreen.Mode.SOURCES;
            case RECIPES -> IndexRecipeScreen.Mode.RECIPES;
        };
    }

    private static void syncScreenState(Screen screen) {
        String key = screen.getClass().getName();
        if (key.equals(activeScreenKey)) {
            return;
        }
        saveScreenState();
        activeScreenKey = key;
        requestServerSync(false);
        OverlayScreenState state = SCREEN_STATES.get(key);
        if (state == null) {
            collapsed = false;
            gridDensity = Config.OVERLAY_GRID_DENSITY.get();
            gridDensityOverridden = false;
            detailStack = ItemStack.EMPTY;
            detailMode = IndexRecipeUi.ViewMode.RECIPES;
            detailCategory = null;
            detailSelected = 0;
            return;
        }
        collapsed = state.collapsed();
        gridDensity = state.gridDensity();
        gridDensityOverridden = true;
        detailMode = state.mode();
        detailCategory = state.category();
        detailSelected = state.selected();
        detailStack = itemStack(state.itemId());
    }

    private static void requestServerSync(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastSyncRequestMillis < 5000L) {
            return;
        }
        lastSyncRequestMillis = now;
        EchoNetClientActions.sendServerboundAction(new IndexActionPacket(IndexActionPacket.Action.REQUEST_SYNC, null));
    }

    private static void saveScreenState() {
        if (activeScreenKey.isBlank()) {
            return;
        }
        SCREEN_STATES.put(activeScreenKey, new OverlayScreenState(collapsed,
                detailStack.isEmpty() ? null : IndexService.itemId(detailStack.getItem()),
                detailMode, detailCategory, detailSelected, currentGridDensity()));
    }

    private static ItemStack itemStack(Identifier id) {
        if (id == null) {
            return ItemStack.EMPTY;
        }
        return BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    private static void setFilter(String filter) {
        String next = filter == null ? "" : filter;
        categoryFilter = next.equals(categoryFilter) ? "" : next;
        searchFocused = false;
        resetGridScroll();
    }

    private static void clearFilters(boolean includeSearch) {
        categoryFilter = "";
        bookmarkedOnly = false;
        if (includeSearch) {
            search = "";
        }
        searchFocused = false;
        resetGridScroll();
    }

    private static String effectiveSearch() {
        return search.isBlank() ? "" : search.trim();
    }

    private static void toggleBookmark(Identifier id) {
        boolean currently = ClientIndexState.isBookmarked(id);
        EchoNetClientActions.sendServerboundAction(new IndexActionPacket(
                currently ? IndexActionPacket.Action.UNBOOKMARK : IndexActionPacket.Action.BOOKMARK,
                id));
    }

    private static void sendRecipeAction(IndexActionPacket.Action action, Identifier recipeId) {
        if (recipeId != null) {
            EchoNetClientActions.sendServerboundAction(new IndexActionPacket(action, recipeId));
        }
    }

    private static void resetGridScroll() {
        scroll = 0;
        horizontalScroll = 0;
    }

    private static void drawGridScrollbars(GuiGraphicsExtractor graphics, Font font, int x, int y, int w, int h, int contentW, int contentH) {
        verticalScrollbar = null;
        horizontalScrollbar = null;
        int maxVerticalScroll = Math.max(0, contentH - h);
        int maxHorizontalScroll = Math.max(0, contentW - w);
        if (maxVerticalScroll > 0) {
            int trackX = x + w - 7;
            int trackW = 6;
            int thumbH = Math.max(14, h * h / Math.max(h, contentH));
            int thumbY = y + (h - thumbH) * scroll / maxVerticalScroll;
            graphics.fill(trackX, y, trackX + trackW, y + h, 0xAA071017);
            graphics.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xCC66E8FF);
            verticalScrollbar = new ScrollbarMetrics(trackX, y, trackW, h, trackX, thumbY, trackW, thumbH,
                    maxVerticalScroll, true);
            tooltipIfHovered(graphics, font, lastMouseX, lastMouseY, trackX, y, trackW, h,
                    tr("screen.echoindex.overlay.tooltip.scroll_vertical"));
        }
        if (maxHorizontalScroll > 0) {
            int trackY = y + h - 7;
            int trackH = 6;
            int thumbW = Math.max(14, w * w / Math.max(w, contentW));
            int thumbX = x + (w - thumbW) * horizontalScroll / maxHorizontalScroll;
            graphics.fill(x, trackY, x + w, trackY + trackH, 0xAA071017);
            graphics.fill(thumbX, trackY, thumbX + thumbW, trackY + trackH, 0xCC66E8FF);
            horizontalScrollbar = new ScrollbarMetrics(x, trackY, w, trackH, thumbX, trackY, thumbW, trackH,
                    maxHorizontalScroll, false);
            tooltipIfHovered(graphics, font, lastMouseX, lastMouseY, x, trackY, w, trackH,
                    tr("screen.echoindex.overlay.tooltip.scroll_horizontal"));
        }
    }

    private static List<Component> itemTooltip(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        Item.TooltipContext context = minecraft.level == null ? Item.TooltipContext.EMPTY : Item.TooltipContext.of(minecraft.level);
        List<Component> tooltip = new ArrayList<>(stack.getTooltipLines(context, minecraft.player, TooltipFlag.NORMAL));
        tooltip.add(tr("screen.echoindex.overlay.tooltip.item_actions"));
        return tooltip;
    }

    private static boolean beginScrollbarDrag(double mouseX, double mouseY) {
        if (verticalScrollbar != null && verticalScrollbar.insideTrack(mouseX, mouseY)) {
            dragMode = DragMode.VERTICAL_SCROLL;
            dragThumbOffset = verticalScrollbar.insideThumb(mouseX, mouseY)
                    ? (int) Math.round(mouseY) - verticalScrollbar.thumbY()
                    : verticalScrollbar.thumbH() / 2;
            updateScrollbarDrag(mouseX, mouseY);
            return true;
        }
        if (horizontalScrollbar != null && horizontalScrollbar.insideTrack(mouseX, mouseY)) {
            dragMode = DragMode.HORIZONTAL_SCROLL;
            dragThumbOffset = horizontalScrollbar.insideThumb(mouseX, mouseY)
                    ? (int) Math.round(mouseX) - horizontalScrollbar.thumbX()
                    : horizontalScrollbar.thumbW() / 2;
            updateScrollbarDrag(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private static void updateScrollbarDrag(double mouseX, double mouseY) {
        ScrollbarMetrics metrics = dragMode == DragMode.VERTICAL_SCROLL ? verticalScrollbar : horizontalScrollbar;
        if (metrics == null) {
            return;
        }
        int trackStart = metrics.vertical() ? metrics.trackY() : metrics.trackX();
        int trackSize = metrics.vertical() ? metrics.trackH() : metrics.trackW();
        int thumbSize = metrics.vertical() ? metrics.thumbH() : metrics.thumbW();
        int trackRange = Math.max(1, trackSize - thumbSize);
        int mouse = (int) Math.round(metrics.vertical() ? mouseY : mouseX);
        int thumbStart = clamp(mouse - dragThumbOffset, trackStart, trackStart + trackRange);
        int nextScroll = (int) Math.round((thumbStart - trackStart) * (double) metrics.maxScroll() / trackRange);
        if (metrics.vertical()) {
            scroll = clamp(nextScroll, 0, metrics.maxScroll());
        } else {
            horizontalScroll = clamp(nextScroll, 0, metrics.maxScroll());
        }
    }

    private static void button(GuiGraphicsExtractor graphics, Font font, int x, int y, int w, int h, String label, boolean active) {
        graphics.fill(x, y, x + w, y + h, active ? 0xFF123241 : PANEL);
        graphics.outline(x, y, w, h, active ? CYAN : 0x55244352);
        graphics.centeredText(font, label, x + w / 2, y + 5, active ? TEXT : MUTED);
    }

    private static void renderCoreFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        try {
            Class.forName("com.knoxhack.echoindex.integration.IndexRenderCoreScreenIntegration")
                    .getMethod("drawOverlayFrame", GuiGraphicsExtractor.class, int.class, int.class, int.class, int.class)
                    .invoke(null, graphics, x, y, width, height);
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }

    private static boolean cinematicStyle() {
        try {
            return Config.UI_CINEMATIC_STYLE.get();
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private static void chip(GuiGraphicsExtractor graphics, Font font, int x, int y, int w, String label,
            boolean selected, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, 17);
        graphics.fill(x, y, x + w, y + 17, selected ? 0xFF123241 : hover ? 0xCC102630 : PANEL);
        graphics.outline(x, y, w, 17, selected ? CYAN : 0x4438DFF4);
        graphics.centeredText(font, trim(font, label, w - 8), x + w / 2, y + 5, selected ? TEXT : MUTED);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private static boolean shiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String trim(Font font, String text, int width) {
        return IndexRecipeUi.trim(font, text, width);
    }

    private static Component tr(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static String text(String key, Object... args) {
        return tr(key, args).getString();
    }

    private static String modeLabel(IndexRecipeUi.ViewMode mode) {
        return switch (mode) {
            case USES -> text("screen.echoindex.uses");
            case SOURCES -> text("screen.echoindex.sources");
            case RECIPES -> text("screen.echoindex.recipes");
        };
    }

    private static void tooltipIfHovered(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            int x, int y, int w, int h, Component... lines) {
        if (lines.length > 0 && inside(mouseX, mouseY, x, y, w, h)) {
            graphics.setComponentTooltipForNextFrame(font, List.of(lines), mouseX, mouseY);
        }
    }

    private record GridCacheKey(String screenKey, String query, long clientRevision) {
    }

    private record DetailBaseCacheKey(String screenKey, Identifier itemId, IndexRecipeUi.ViewMode mode,
            long recipeSnapshotGeneration, long clientRevision, long queryRevision) {
    }

    private record DetailCacheKey(DetailBaseCacheKey base, Identifier category) {
    }

    private record ModeCountKey(String screenKey, Identifier itemId, IndexRecipeUi.ViewMode mode,
            long recipeSnapshotGeneration, long clientRevision, long queryRevision) {
    }

    private record ChipCursor(int x, int y) {
    }

    private record Hitbox(int x, int y, int w, int h, ClickAction action) {
    }

    private record PanelBounds(int x, int y, int w, int h) {
    }

    private record GridLayout(int slot, int step, int columns, int usableW) {
        int columnOffset(int column) {
            if (columns <= 1) {
                return 0;
            }
            int span = Math.max(0, usableW - slot);
            return (int) Math.round(column * (double) span / (columns - 1));
        }
    }

    private record ScrollbarMetrics(int trackX, int trackY, int trackW, int trackH,
            int thumbX, int thumbY, int thumbW, int thumbH, int maxScroll, boolean vertical) {
        boolean insideTrack(double mouseX, double mouseY) {
            return inside(mouseX, mouseY, trackX, trackY, trackW, trackH);
        }

        boolean insideThumb(double mouseX, double mouseY) {
            return inside(mouseX, mouseY, thumbX, thumbY, thumbW, thumbH);
        }
    }

    private record OverlayScreenState(boolean collapsed, Identifier itemId, IndexRecipeUi.ViewMode mode,
            Identifier category, int selected, Config.GridDensity gridDensity) {
    }

    private record HistoryEntry(Identifier itemId, IndexRecipeUi.ViewMode mode) {
    }

    @FunctionalInterface
    private interface ClickAction {
        void click(int button, int modifiers);
    }

    private enum DragMode {
        NONE,
        MOVE,
        RESIZE,
        VERTICAL_SCROLL,
        HORIZONTAL_SCROLL
    }
}
