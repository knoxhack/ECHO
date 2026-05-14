package com.knoxhack.echoindex.client;

import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoindex.Config;
import com.knoxhack.echoindex.EchoIndexClient;
import com.knoxhack.echoindex.network.IndexActionPacket;
import com.knoxhack.echoindex.service.ClientIndexState;
import com.knoxhack.echoindex.service.IndexRecipePlan;
import com.knoxhack.echoindex.service.IndexRecipePlanner;
import com.knoxhack.echoindex.service.IndexRecipeQueryClientState;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class IndexCatalogScreen extends Screen {
    private static final int BG = IndexRecipeUi.BG;
    private static final int PANEL = IndexRecipeUi.PANEL;
    private static final int ROW = IndexRecipeUi.ROW;
    private static final int CYAN = IndexRecipeUi.CYAN;
    private static final int TEXT = IndexRecipeUi.TEXT;
    private static final int MUTED = IndexRecipeUi.MUTED;
    private static final int WARN = IndexRecipeUi.WARN;
    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 24;
    private static final int INNER_PAD = 10;
    private static final int SPLIT_DETAIL_MIN_WIDTH = 460;
    private static final int SPLIT_DETAIL_MIN_HEIGHT = 220;

    private String search = "";
    private boolean searchFocused;
    private String categoryFilter = "";
    private boolean bookmarkedOnly;
    private int scroll;
    private int horizontalScroll;
    private ItemStack selectedItem = ItemStack.EMPTY;
    private IndexRecipeUi.ViewMode detailMode = IndexRecipeUi.ViewMode.RECIPES;
    private Identifier detailCategory;
    private int detailSelected;
    private Config.GridDensity gridDensity = Config.GridDensity.NORMAL;
    private boolean gridDensityOverridden;
    private final List<IndexRecipeUi.SlotHit> slotHits = new ArrayList<>();
    private final List<Hitbox> hitboxes = new ArrayList<>();
    private ItemStack hoveredStack = ItemStack.EMPTY;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int lastGridX;
    private int lastGridY;
    private int lastGridW;
    private int lastGridH;

    private List<ItemStack> cachedGridItems = List.of();
    private String cachedGridQuery = null;
    private long cachedGridRevision = -1;

    private List<IndexRecipeView> cachedDetailViews = List.of();
    private String cachedDetailKey = null;

    public IndexCatalogScreen() {
        super(Component.translatable("screen.echoindex.catalog"));
    }

    @Override
    protected void init() {
        EchoNetClientActions.sendServerboundAction(new IndexActionPacket(IndexActionPacket.Action.REQUEST_SYNC, null));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        hitboxes.clear();
        slotHits.clear();
        hoveredStack = ItemStack.EMPTY;
        layout();
        Font font = Minecraft.getInstance().font;

        graphics.fill(0, 0, width, height, 0xDD02070A);
        drawPanelChrome(graphics);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + HEADER_HEIGHT, 0x33163843);
        graphics.text(font, "ECHO: INDEX", panelX + 10, panelY + 9, CYAN, false);

        button(graphics, font, panelX + panelW - 92, panelY + 5, 18, 16, densityLabel(), true);
        hitboxes.add(new Hitbox(panelX + panelW - 92, panelY + 5, 18, 16, (b, m) -> cycleGridDensity()));
        button(graphics, font, panelX + panelW - 70, panelY + 5, 18, 16, "R", true);
        hitboxes.add(new Hitbox(panelX + panelW - 70, panelY + 5, 18, 16, (b, m) -> {
            IndexService.INSTANCE.rebuildRecipes(Minecraft.getInstance().player, "catalog refresh button");
            requestServerSync(true);
        }));
        button(graphics, font, panelX + panelW - 48, panelY + 5, 18, 16, "?", true);
        hitboxes.add(new Hitbox(panelX + panelW - 48, panelY + 5, 18, 16, (b, m) ->
                Minecraft.getInstance().setScreen(new IndexDiagnosticsScreen())));
        button(graphics, font, panelX + panelW - 26, panelY + 5, 18, 16, "X", true);
        hitboxes.add(new Hitbox(panelX + panelW - 26, panelY + 5, 18, 16, (b, m) ->
                Minecraft.getInstance().setScreen(null)));

        int searchX = panelX + INNER_PAD;
        int searchY = panelY + HEADER_HEIGHT + 6;
        int searchW = panelW - INNER_PAD * 2;
        drawSearch(graphics, font, searchX, searchY, searchW);
        categoryFilter = "";
        bookmarkedOnly = false;
        int bodyY = searchY + 25;
        int bodyH = Math.max(48, panelH - (bodyY - panelY) - FOOTER_HEIGHT - 4);

        if (selectedItem.isEmpty()) {
            drawGrid(graphics, font, mouseX, mouseY, searchX, bodyY, searchW, bodyH);
        } else if (searchW >= SPLIT_DETAIL_MIN_WIDTH && bodyH >= SPLIT_DETAIL_MIN_HEIGHT) {
            int gridW = Math.min(180, Math.max(120, searchW / 3));
            drawGrid(graphics, font, mouseX, mouseY, searchX, bodyY, gridW, bodyH);
            drawDetail(graphics, font, mouseX, mouseY, searchX + gridW + 8, bodyY, searchW - gridW - 8, bodyH);
        } else {
            drawDetail(graphics, font, mouseX, mouseY, searchX, bodyY, searchW, bodyH);
        }
        drawFooter(graphics, font, searchX, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        layout();
        for (Hitbox hitbox : List.copyOf(hitboxes)) {
            if (inside(event.x(), event.y(), hitbox.x(), hitbox.y(), hitbox.w(), hitbox.h())) {
                hitbox.action().click(event.button(), event.modifiers());
                return true;
            }
        }
        if (searchFocused) {
            searchFocused = false;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        layout();
        if (!inside(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        boolean overGrid = inside(mouseX, mouseY, lastGridX, lastGridY, lastGridW, lastGridH);
        if (!selectedItem.isEmpty() && !overGrid) {
            for (IndexRecipeUi.SlotHit hit : List.copyOf(slotHits)) {
                if (hit.choiceCyclable() && inside(mouseX, mouseY, hit.x(), hit.y(), hit.w(), hit.h())) {
                    IndexRecipeUi.cycleChoice(hit, scrollY > 0 ? 1 : -1);
                    return true;
                }
            }
            int max = Math.max(0, detailViews().size() - 1);
            detailSelected = clamp(detailSelected - (int) Math.round(scrollY), 0, max);
        } else {
            if (Math.abs(scrollX) > 0.0D || shiftDown()) {
                double horizontalDelta = Math.abs(scrollX) > 0.0D ? scrollX : scrollY;
                horizontalScroll = Math.max(0, horizontalScroll - (int) Math.round(horizontalDelta * 26.0D));
            } else {
                scroll = Math.max(0, scroll - (int) Math.round(scrollY * 26.0D));
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchFocused) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) {
                    search = search.substring(0, search.offsetByCodePoints(search.length(), -1));
                    resetGridScroll();
                }
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ENTER) {
                List<ItemStack> items = gridItems();
                if (!items.isEmpty()) {
                    selectItem(items.getFirst(), IndexRecipeUi.ViewMode.RECIPES);
                }
                searchFocused = false;
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (EchoIndexClient.SHOW_RECIPE_KEY.matches(event) && !selectedItem.isEmpty()) {
            detailMode = IndexRecipeUi.ViewMode.RECIPES;
            detailCategory = null;
            detailSelected = 0;
            return true;
        }
        if (EchoIndexClient.SHOW_USAGE_KEY.matches(event) && !selectedItem.isEmpty()) {
            detailMode = IndexRecipeUi.ViewMode.USES;
            detailCategory = null;
            detailSelected = 0;
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_B) {
            if (!hoveredStack.isEmpty()) {
                toggleBookmark(IndexService.itemId(hoveredStack.getItem()));
                return true;
            } else if (!selectedItem.isEmpty()) {
                toggleBookmark(IndexService.itemId(selectedItem.getItem()));
                return true;
            }
        }
        if (!selectedItem.isEmpty()) {
            if (event.key() == GLFW.GLFW_KEY_LEFT && detailSelected > 0) {
                detailSelected--;
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_RIGHT && detailSelected + 1 < detailViews().size()) {
                detailSelected++;
                return true;
            }
        } else {
            if (event.key() == GLFW.GLFW_KEY_LEFT) {
                horizontalScroll = Math.max(0, horizontalScroll - gridStep());
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_RIGHT) {
                horizontalScroll += gridStep();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!searchFocused || event == null || !event.isAllowedChatCharacter()) {
            return super.charTyped(event);
        }
        if (search.length() < 80) {
            search += event.codepointAsString();
            resetGridScroll();
        }
        return true;
    }

    private void layout() {
        panelW = Math.min(1200, Math.max(360, width - 40));
        panelH = Math.min(800, Math.max(300, height - 40));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
    }

    private void drawPanelChrome(GuiGraphicsExtractor graphics) {
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        if (cinematicStyle()) {
            renderCoreFrame(graphics, panelX, panelY, panelW, panelH);
        } else {
            graphics.outline(panelX, panelY, panelW, panelH, 0x8846DFF4);
            graphics.fill(panelX, panelY, panelX + Math.max(38, panelW / 5), panelY + 2, CYAN);
            graphics.fill(panelX, panelY + panelH - 2, panelX + Math.max(24, panelW / 7), panelY + panelH, CYAN);
        }
    }

    private void drawSearch(GuiGraphicsExtractor graphics, Font font, int x, int y, int w) {
        graphics.fill(x, y, x + w, y + 19, 0xDD05090E);
        graphics.outline(x, y, w, 19, searchFocused ? CYAN : 0x6638DFF4);
        String label = search.isBlank() && !searchFocused ? "Search ECHO: Index..." : search + (searchFocused ? "_" : "");
        graphics.text(font, trim(font, label, w - 18), x + 6, y + 6, search.isBlank() ? MUTED : TEXT, false);
        hitboxes.add(new Hitbox(x, y, w, 19, (b, m) -> searchFocused = true));
    }

    private int drawFilterButtons(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
            int mouseX, int mouseY) {
        ChipCursor cursor = new ChipCursor(x, y);
        cursor = filterChip(graphics, font, x, cursor, width, 34, "All",
                categoryFilter.isBlank() && !bookmarkedOnly, mouseX, mouseY,
                (b, m) -> clearFilters(false));
        cursor = filterChip(graphics, font, x, cursor, width, 48, "Blocks", "$blocks".equals(categoryFilter),
                mouseX, mouseY, (b, m) -> setFilter("$blocks"));
        cursor = filterChip(graphics, font, x, cursor, width, 62, "Machines", "$machines".equals(categoryFilter),
                mouseX, mouseY, (b, m) -> setFilter("$machines"));
        cursor = filterChip(graphics, font, x, cursor, width, 42, "Tools", "$tools".equals(categoryFilter),
                mouseX, mouseY, (b, m) -> setFilter("$tools"));
        cursor = filterChip(graphics, font, x, cursor, width, 56, "Combat", "$combat".equals(categoryFilter),
                mouseX, mouseY, (b, m) -> setFilter("$combat"));
        cursor = filterChip(graphics, font, x, cursor, width, 46, "ECHO", "$echo".equals(categoryFilter),
                mouseX, mouseY, (b, m) -> setFilter("$echo"));
        cursor = filterChip(graphics, font, x, cursor, width, 26, "*", bookmarkedOnly, mouseX, mouseY,
                (b, m) -> {
                    bookmarkedOnly = !bookmarkedOnly;
                    resetGridScroll();
                });
        cursor = filterChip(graphics, font, x, cursor, width, 48, "Clear", false, mouseX, mouseY,
                (b, m) -> clearFilters(true));
        return cursor.y() + 17;
    }

    private ChipCursor filterChip(GuiGraphicsExtractor graphics, Font font, int rowX, ChipCursor cursor, int rowW,
            int preferredW, String label, boolean selected, int mouseX, int mouseY, ClickAction action) {
        int w = Math.min(Math.max(24, rowW), Math.max(preferredW, font.width(label) + 12));
        int cx = cursor.x();
        int cy = cursor.y();
        if (cx > rowX && cx + w > rowX + rowW) {
            cx = rowX;
            cy += 19;
        }
        chip(graphics, font, cx, cy, w, label, selected, mouseX, mouseY);
        hitboxes.add(new Hitbox(cx, cy, w, 17, action));
        return new ChipCursor(cx + w + 4, cy);
    }

    private int drawActiveTokens(GuiGraphicsExtractor graphics, Font font, int x, int y, int w) {
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

    private void drawGrid(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            int gridX, int gridY, int gridW, int gridH) {
        lastGridX = gridX;
        lastGridY = gridY;
        lastGridW = gridW;
        lastGridH = gridH;
        List<ItemStack> items = gridItems();
        int step = gridStep();
        int slot = slotSize();
        int columns = Math.max(1, Math.max(Config.OVERLAY_MAX_COLUMNS.get(), gridW / step));
        int rows = (items.size() + columns - 1) / columns;
        int contentW = columns * step;
        int contentH = rows * step;
        int maxVerticalScroll = Math.max(0, contentH - gridH);
        int maxHorizontalScroll = Math.max(0, contentW - gridW);
        scroll = clamp(scroll, 0, maxVerticalScroll);
        horizontalScroll = clamp(horizontalScroll, 0, maxHorizontalScroll);
        graphics.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        int startY = gridY - scroll;
        int firstRow = Math.max(0, scroll / step);
        int lastRow = Math.min(Math.max(0, rows - 1), (scroll + gridH) / step + 1);
        int firstColumn = Math.max(0, horizontalScroll / step);
        int lastColumn = Math.min(columns - 1, (horizontalScroll + gridW) / step + 1);
        for (int row = firstRow; row <= lastRow; row++) {
            int y = startY + row * step;
            if (y < gridY - step || y > gridY + gridH) {
                continue;
            }
            for (int column = firstColumn; column <= lastColumn; column++) {
                int index = row * columns + column;
                if (index >= items.size()) {
                    break;
                }
                ItemStack stack = items.get(index);
                int x = gridX - horizontalScroll + column * step;
                itemSlot(graphics, font, stack, x, y, slot, mouseX, mouseY);
                Identifier itemId = IndexService.itemId(stack.getItem());
                if (ClientIndexState.isBookmarked(itemId)) {
                    graphics.fill(x + slot - 4, y + 1, x + slot - 1, y + 4, CYAN);
                }
                if (hoveredStack == stack) {
                    hitboxes.add(new Hitbox(x, y, slot, slot, (b, m) -> {
                        if (b == 2) {
                            toggleBookmark(IndexService.itemId(stack.getItem()));
                        } else {
                            selectItem(stack, IndexRecipeUi.ViewMode.RECIPES);
                        }
                    }));
                }
            }
        }
        graphics.disableScissor();
        drawGridScrollbars(graphics, gridX, gridY, gridW, gridH, contentW, contentH);
        if (items.isEmpty()) {
            graphics.text(font, "No indexed items match.", gridX, gridY + 8, MUTED, false);
        }
    }

    private void drawDetail(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            int x, int y, int w, int h) {
        List<IndexRecipeView> views = detailViews();
        detailSelected = clamp(detailSelected, 0, Math.max(0, views.size() - 1));
        graphics.fill(x, y, x + w, y + h, 0x99071117);

        boolean canBack = !selectedItem.isEmpty()
                && (panelW < SPLIT_DETAIL_MIN_WIDTH || panelH < SPLIT_DETAIL_MIN_HEIGHT);
        button(graphics, font, x, y, 22, 17, "<", canBack);
        if (canBack) {
            hitboxes.add(new Hitbox(x, y, 22, 17, (b, m) -> closeDetail()));
        }
        button(graphics, font, x + 24, y, 54, 17, "Results", true);
        hitboxes.add(new Hitbox(x + 24, y, 54, 17, (b, m) -> closeDetail()));
        button(graphics, font, x + w - 46, y, 46, 17, "Open", true);
        hitboxes.add(new Hitbox(x + w - 46, y, 46, 17, (b, m) -> openRecipeScreen()));

        if (w >= 200) {
            graphics.item(selectedItem, x + 86, y + 1);
            graphics.text(font, trim(font, selectedItem.getHoverName().getString(), Math.max(36, w - 140)),
                    x + 106, y + 5,
                    ClientIndexState.isBookmarked(IndexService.itemId(selectedItem.getItem())) ? WARN : TEXT, false);
        }

        int chipY = y + 22;
        modeChip(graphics, font, x, chipY, Math.max(42, (w - 8) / 3), IndexRecipeUi.ViewMode.RECIPES, mouseX, mouseY);
        modeChip(graphics, font, x + (w - 8) / 3 + 4, chipY, Math.max(42, (w - 8) / 3), IndexRecipeUi.ViewMode.USES, mouseX, mouseY);
        modeChip(graphics, font, x + ((w - 8) / 3 + 4) * 2, chipY,
                w - ((w - 8) / 3 + 4) * 2, IndexRecipeUi.ViewMode.SOURCES, mouseX, mouseY);

        int railY = chipY + 21;
        boolean showCategories = h >= 220;
        if (showCategories) {
            drawCategoryChips(graphics, font, x, railY, w, views, mouseX, mouseY);
            railY += 22;
        }
        int traceH = drawTraceRail(graphics, font, x, railY, w, mouseX, mouseY);
        int cardY = railY + traceH;
        int reservedBottom = 43;
        int cardH = Math.max(70, h - (cardY - y) - reservedBottom);
        IndexRecipeUi.recordCardSelection(detailMode, detailSelected, views.size());
        if (views.isEmpty()) {
            graphics.fill(x + 2, cardY, x + w - 2, cardY + Math.min(64, cardH), 0xAA071014);
            graphics.textWithWordWrap(font, Component.literal(
                    IndexRecipeUi.emptyMessage(Minecraft.getInstance().player, selectedItem.getItem(), detailMode)),
                    x + 12, cardY + 14, w - 24, MUTED);
        } else {
            IndexRecipeView recipe = views.get(detailSelected);
            IndexRecipeUi.drawRecipeCard(graphics, font, recipe, x + 2, cardY, w - 4, cardH,
                    selectedItem, mouseX, mouseY, slotHits);
            int actionY = cardY + cardH + 4;
            drawRecipeActions(graphics, font, recipe, x + 8, actionY, w - 16, mouseX, mouseY);
            for (IndexRecipeUi.SlotHit hit : slotHits) {
                hitboxes.add(new Hitbox(hit.x(), hit.y(), hit.w(), hit.h(), (b, m) -> {
                    if ((m & GLFW.GLFW_MOD_SHIFT) != 0 || b == 2) {
                        toggleBookmark(IndexService.itemId(hit.stack().getItem()));
                    } else if (b == 1 && hit.choiceCyclable()) {
                        IndexRecipeUi.cycleChoice(hit, 1);
                    } else {
                        selectItem(hit.stack(), modeForSlot(hit, b));
                    }
                }));
            }
        }
        String page = (detailSelected + 1) + " / " + views.size();
        button(graphics, font, x + Math.max(0, w / 2 - 48), y + h - 19, 24, 16, "<", detailSelected > 0);
        hitboxes.add(new Hitbox(x + Math.max(0, w / 2 - 48), y + h - 19, 24, 16,
                (b, m) -> detailSelected = Math.max(0, detailSelected - 1)));
        graphics.centeredText(font, page, x + w / 2, y + h - 15, MUTED);
        button(graphics, font, x + Math.min(w - 24, w / 2 + 24), y + h - 19, 24, 16, ">", detailSelected + 1 < views.size());
        hitboxes.add(new Hitbox(x + Math.min(w - 24, w / 2 + 24), y + h - 19, 24, 16,
                (b, m) -> detailSelected = Math.min(views.size() - 1, detailSelected + 1)));
    }

    private void drawRecipeActions(GuiGraphicsExtractor graphics, Font font, IndexRecipeView recipe,
            int x, int y, int w, int mouseX, int mouseY) {
        IndexRecipePlan plan = IndexRecipePlanner.plan(Minecraft.getInstance().player, recipe);
        int pinW = plan.pinned() ? 48 : 34;
        button(graphics, font, x, y, pinW, 16, plan.pinned() ? "Unpin" : "Pin", true);
        hitboxes.add(new Hitbox(x, y, pinW, 16, (b, m) -> sendRecipeAction(
                plan.pinned() ? IndexActionPacket.Action.UNPIN_RECIPE : IndexActionPacket.Action.PIN_RECIPE,
                recipe.id())));
        int tx = x + pinW + 5;
        if (plan.missingCount() > 0 && !selectedItem.isEmpty()) {
            button(graphics, font, tx, y, 44, 16, "Trace", true);
            hitboxes.add(new Hitbox(tx, y, 44, 16, (b, m) -> IndexRecipeTraceState.open(selectedItem, recipe, plan)));
            tx += 49;
        }
        if (plan.canTransfer()) {
            button(graphics, font, tx, y, 54, 16, "Transfer", true);
            hitboxes.add(new Hitbox(tx, y, 54, 16,
                    (b, m) -> sendRecipeAction(IndexActionPacket.Action.TRANSFER_RECIPE, recipe.id())));
        } else {
            String note = IndexRecipeUi.statusDetail(plan, true);
            if (!note.isBlank()) {
                graphics.text(font, trim(font, note, Math.max(30, x + w - tx - 4)), tx, y + 5,
                        IndexRecipeUi.statusColor(plan, true), false);
            }
        }
    }

    private int drawTraceRail(GuiGraphicsExtractor graphics, Font font, int x, int y, int w,
            int mouseX, int mouseY) {
        IndexRecipeTraceState.Trace trace = IndexRecipeTraceState.current();
        if (!traceApplies(trace)) {
            return 0;
        }
        int h = 28;
        graphics.fill(x, y, x + w, y + h - 4, 0x88102630);
        graphics.outline(x, y, w, h - 4, 0x5538DFF4);
        String label = "Path: " + trace.rootStack().getHoverName().getString() + " > missing inputs";
        graphics.text(font, trim(font, label, w - 82), x + 7, y + 8, MUTED, false);
        button(graphics, font, x + w - 74, y + 4, 68, 16, "Root", true);
        hitboxes.add(new Hitbox(x + w - 74, y + 4, 68, 16,
                (b, m) -> selectItem(trace.rootStack(), IndexRecipeUi.ViewMode.RECIPES)));
        return h;
    }

    private boolean traceApplies(IndexRecipeTraceState.Trace trace) {
        if (trace == null || !trace.active() || selectedItem.isEmpty()) {
            return false;
        }
        Identifier focusId = IndexService.itemId(selectedItem.getItem());
        return trace.rootItemId().equals(focusId)
                || trace.entries().stream().anyMatch(entry -> entry.itemId().equals(focusId));
    }

    private void drawCategoryChips(GuiGraphicsExtractor graphics, Font font, int x, int y, int w,
            List<IndexRecipeView> views, int mouseX, int mouseY) {
        chip(graphics, font, x, y, 34, "All", detailCategory == null, mouseX, mouseY);
        hitboxes.add(new Hitbox(x, y, 34, 17, (b, m) -> {
            detailCategory = null;
            detailSelected = 0;
        }));
        Set<Identifier> categories = new LinkedHashSet<>();
        for (IndexRecipeView view : views) {
            categories.add(view.categoryId());
        }
        int cx = x + 38;
        for (Identifier category : categories.stream().limit(6).toList()) {
            int chipW = Math.min(86, Math.max(42, font.width(category.getPath()) + 12));
            if (cx + chipW > x + w) {
                break;
            }
            chip(graphics, font, cx, y, chipW, category.getPath(), category.equals(detailCategory), mouseX, mouseY);
            hitboxes.add(new Hitbox(cx, y, chipW, 17, (b, m) -> {
                detailCategory = category.equals(detailCategory) ? null : category;
                detailSelected = 0;
            }));
            cx += chipW + 4;
        }
    }

    private void drawFooter(GuiGraphicsExtractor graphics, Font font, int x, int mouseX, int mouseY) {
        int footerY = panelY + panelH - 20;
        graphics.fill(panelX + 1, footerY - 3, panelX + panelW - 1, panelY + panelH - 1, 0xAA071017);
        int indexed = selectedItem.isEmpty() ? gridItems().size() : detailViews().size();
        int total = IndexService.INSTANCE.catalogCount(Minecraft.getInstance().player);
        String left = selectedItem.isEmpty() ? "Showing " + indexed + " / " + total : detailMode.label() + ": " + indexed;
        graphics.text(font, left, x, footerY, CYAN, false);
        String footer = "R recipe  U uses  B bookmark";
        graphics.text(font, trim(font, footer, Math.max(60, panelW / 3)), x + Math.min(120, panelW / 3), footerY, MUTED, false);
    }

    private void modeChip(GuiGraphicsExtractor graphics, Font font, int x, int y, int w,
            IndexRecipeUi.ViewMode mode, int mouseX, int mouseY) {
        boolean selected = this.detailMode == mode;
        chip(graphics, font, x, y, w, mode.label(), selected, mouseX, mouseY);
        hitboxes.add(new Hitbox(x, y, w, 17, (b, m) -> {
            detailMode = mode;
            detailCategory = null;
            detailSelected = 0;
        }));
    }

    private void itemSlot(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, int slotSize,
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
            graphics.setTooltipForNextFrame(font, stack, x + size / 2, y + size / 2);
        }
    }

    private int slotSize() {
        return switch (currentGridDensity()) {
            case COMPACT -> 18;
            case LARGE -> 24;
            case NORMAL -> 20;
        };
    }

    private int gridStep() {
        return slotSize() + switch (currentGridDensity()) {
            case COMPACT -> 3;
            case LARGE -> 6;
            case NORMAL -> 4;
        };
    }

    private Config.GridDensity currentGridDensity() {
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

    private String densityLabel() {
        return switch (currentGridDensity()) {
            case COMPACT -> "C";
            case NORMAL -> "N";
            case LARGE -> "L";
        };
    }

    private void cycleGridDensity() {
        gridDensity = switch (currentGridDensity()) {
            case COMPACT -> Config.GridDensity.NORMAL;
            case NORMAL -> Config.GridDensity.LARGE;
            case LARGE -> Config.GridDensity.COMPACT;
        };
        gridDensityOverridden = true;
        resetGridScroll();
    }

    private List<ItemStack> gridItems() {
        String query = effectiveSearch();
        long revision = ClientIndexState.revision();
        if (!query.equals(cachedGridQuery) || revision != cachedGridRevision) {
            cachedGridQuery = query;
            cachedGridRevision = revision;
            Player player = Minecraft.getInstance().player;
            cachedGridItems = player == null ? List.of() : List.copyOf(IndexService.INSTANCE.filteredItemsUnbounded(player, query));
        }
        return cachedGridItems;
    }

    private List<IndexRecipeView> detailViews() {
        String key = detailCacheKey();
        if (!key.equals(cachedDetailKey)) {
            cachedDetailKey = key;
            cachedDetailViews = computeDetailViews();
        }
        return cachedDetailViews;
    }

    private String detailCacheKey() {
        return selectedItem + ":" + detailMode + ":" + detailCategory + ":" + ClientIndexState.revision()
                + ":" + IndexRecipeQueryClientState.revision();
    }

    private List<IndexRecipeView> computeDetailViews() {
        if (selectedItem.isEmpty()) {
            return List.of();
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        List<IndexRecipeView> views = IndexRecipeUi.viewsFor(player, selectedItem.getItem(), detailMode);
        if (detailCategory != null) {
            views = views.stream()
                    .filter(v -> v.categoryId().equals(detailCategory))
                    .toList();
        }
        return views;
    }

    private void setFilter(String filter) {
        String next = filter == null ? "" : filter;
        categoryFilter = next.equals(categoryFilter) ? "" : next;
        searchFocused = false;
        resetGridScroll();
    }

    private void clearFilters(boolean includeSearch) {
        categoryFilter = "";
        bookmarkedOnly = false;
        if (includeSearch) {
            search = "";
        }
        searchFocused = false;
        resetGridScroll();
    }

    private void resetGridScroll() {
        scroll = 0;
        horizontalScroll = 0;
    }

    private void drawGridScrollbars(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int contentW, int contentH) {
        int maxVerticalScroll = Math.max(0, contentH - h);
        int maxHorizontalScroll = Math.max(0, contentW - w);
        if (maxVerticalScroll > 0) {
            int trackX = x + w - 5;
            int thumbH = Math.max(14, h * h / Math.max(h, contentH));
            int thumbY = y + (h - thumbH) * scroll / maxVerticalScroll;
            graphics.fill(trackX, y, trackX + 4, y + h, 0xAA071017);
            graphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xCC66E8FF);
        }
        if (maxHorizontalScroll > 0) {
            int trackY = y + h - 5;
            int thumbW = Math.max(14, w * w / Math.max(w, contentW));
            int thumbX = x + (w - thumbW) * horizontalScroll / maxHorizontalScroll;
            graphics.fill(x, trackY, x + w, trackY + 4, 0xAA071017);
            graphics.fill(thumbX, trackY, thumbX + thumbW, trackY + 4, 0xCC66E8FF);
        }
    }

    private String effectiveSearch() {
        return search.isBlank() ? "" : search.trim();
    }

    private void requestServerSync(boolean force) {
        EchoNetClientActions.sendServerboundAction(new IndexActionPacket(IndexActionPacket.Action.REQUEST_SYNC, null));
    }

    private void toggleBookmark(Identifier id) {
        boolean currently = ClientIndexState.isBookmarked(id);
        EchoNetClientActions.sendServerboundAction(new IndexActionPacket(
                currently ? IndexActionPacket.Action.UNBOOKMARK : IndexActionPacket.Action.BOOKMARK,
                id));
    }

    private void sendRecipeAction(IndexActionPacket.Action action, Identifier recipeId) {
        if (recipeId != null) {
            EchoNetClientActions.sendServerboundAction(new IndexActionPacket(action, recipeId));
        }
    }

    private void selectItem(ItemStack stack, IndexRecipeUi.ViewMode mode) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        selectedItem = stack.copy();
        detailMode = mode == null ? IndexRecipeUi.ViewMode.RECIPES : mode;
        detailCategory = null;
        detailSelected = 0;
        searchFocused = false;
    }

    private void closeDetail() {
        selectedItem = ItemStack.EMPTY;
        detailCategory = null;
        detailSelected = 0;
    }

    private void openRecipeScreen() {
        if (selectedItem.isEmpty()) {
            return;
        }
        Minecraft.getInstance().setScreen(new IndexRecipeScreen(selectedItem, screenMode(detailMode)));
    }

    private static IndexRecipeScreen.Mode screenMode(IndexRecipeUi.ViewMode mode) {
        return switch (mode) {
            case USES -> IndexRecipeScreen.Mode.USES;
            case SOURCES -> IndexRecipeScreen.Mode.SOURCES;
            case RECIPES -> IndexRecipeScreen.Mode.RECIPES;
        };
    }

    private static IndexRecipeUi.ViewMode modeForSlot(IndexRecipeUi.SlotHit hit, int button) {
        if (button == 1) {
            return IndexRecipeUi.ViewMode.USES;
        }
        return hit.role() == IndexSlotRole.OUTPUT ? IndexRecipeUi.ViewMode.RECIPES : IndexRecipeUi.ViewMode.USES;
    }

    private static void button(GuiGraphicsExtractor graphics, Font font, int x, int y, int w, int h, String label, boolean active) {
        graphics.fill(x, y, x + w, y + h, active ? 0xFF123241 : PANEL);
        graphics.outline(x, y, w, h, active ? CYAN : 0x55244352);
        graphics.centeredText(font, label, x + w / 2, y + 5, active ? TEXT : MUTED);
    }

    private static void chip(GuiGraphicsExtractor graphics, Font font, int x, int y, int w, String label,
            boolean selected, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, 17);
        graphics.fill(x, y, x + w, y + 17, selected ? 0xFF123241 : hover ? 0xCC102630 : PANEL);
        graphics.outline(x, y, w, 17, selected ? CYAN : 0x4438DFF4);
        graphics.centeredText(font, trim(font, label, w - 8), x + w / 2, y + 5, selected ? TEXT : MUTED);
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

    private static int clamp(int value, int min, int max) {
        return IndexRecipeUi.clamp(value, min, max);
    }

    private static String trim(Font font, String text, int width) {
        return IndexRecipeUi.trim(font, text, width);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return IndexRecipeUi.inside(mx, my, x, y, w, h);
    }

    private static boolean shiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @FunctionalInterface
    private interface ClickAction {
        void click(int button, int modifiers);
    }

    private record Hitbox(int x, int y, int w, int h, ClickAction action) {
    }

    private record ChipCursor(int x, int y) {
    }
}
