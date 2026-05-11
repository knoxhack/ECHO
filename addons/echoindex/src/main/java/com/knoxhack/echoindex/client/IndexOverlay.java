package com.knoxhack.echoindex.client;

import com.knoxhack.echoindex.Config;
import com.knoxhack.echoindex.EchoIndexClient;
import com.knoxhack.echoindex.network.IndexActionPacket;
import com.knoxhack.echoindex.service.ClientIndexState;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
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
    private static final List<Hitbox> HITBOXES = new ArrayList<>();

    private static String search = "";
    private static boolean searchFocused;
    private static boolean collapsed;
    private static int scroll;
    private static int panelX;
    private static int panelY;
    private static int panelW;
    private static int panelH;
    private static ItemStack hoveredStack = ItemStack.EMPTY;
    private static String categoryFilter = "";
    private static boolean bookmarkedOnly;

    private IndexOverlay() {
    }

    public static void onRender(ScreenEvent.Render.Post event) {
        if (!active(event.getScreen())) {
            HITBOXES.clear();
            hoveredStack = ItemStack.EMPTY;
            return;
        }
        render(event.getScreen(), event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
    }

    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!active(event.getScreen()) || !inside(event.getMouseX(), event.getMouseY(), panelX, panelY, panelW, panelH)) {
            return;
        }
        event.setCanceled(true);
        for (Hitbox hitbox : List.copyOf(HITBOXES)) {
            if (inside(event.getMouseX(), event.getMouseY(), hitbox.x(), hitbox.y(), hitbox.w(), hitbox.h())) {
                hitbox.action().click(event.getButton(), event.getMouseButtonEvent().modifiers());
                return;
            }
        }
        searchFocused = false;
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!active(event.getScreen()) || collapsed || !inside(event.getMouseX(), event.getMouseY(), panelX, panelY, panelW, panelH)) {
            return;
        }
        event.setCanceled(true);
        scroll = Math.max(0, scroll - (int) Math.round(event.getScrollDeltaY() * 26.0D));
    }

    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!active(event.getScreen())) {
            return;
        }
        KeyEvent keyEvent = event.getKeyEvent();
        if (EchoIndexClient.BOOKMARK_KEY.matches(keyEvent) && !hoveredStack.isEmpty()) {
            toggleBookmark(IndexService.itemId(hoveredStack.getItem()));
            event.setCanceled(true);
            return;
        }
        if (EchoIndexClient.SHOW_RECIPE_KEY.matches(keyEvent) && !hoveredStack.isEmpty()) {
            Minecraft.getInstance().setScreen(new IndexRecipeScreen(hoveredStack, IndexRecipeScreen.Mode.RECIPES));
            event.setCanceled(true);
            return;
        }
        if (EchoIndexClient.SHOW_USAGE_KEY.matches(keyEvent) && !hoveredStack.isEmpty()) {
            Minecraft.getInstance().setScreen(new IndexRecipeScreen(hoveredStack, IndexRecipeScreen.Mode.USES));
            event.setCanceled(true);
            return;
        }
        if (!searchFocused) {
            return;
        }
        if (keyEvent.key() == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.offsetByCodePoints(search.length(), -1));
            scroll = 0;
            event.setCanceled(true);
        } else if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (!search.isEmpty()) {
                search = "";
                scroll = 0;
            } else {
                searchFocused = false;
            }
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
            scroll = 0;
            event.setCanceled(true);
        }
    }

    private static void render(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        HITBOXES.clear();
        hoveredStack = ItemStack.EMPTY;
        layout(screen);
        Font font = Minecraft.getInstance().font;
        if (collapsed) {
            graphics.fill(panelX, panelY, panelX + 22, panelY + 86, BG);
            graphics.outline(panelX, panelY, 22, 86, CYAN);
            graphics.text(font, "IDX", panelX + 3, panelY + 8, CYAN, false);
            HITBOXES.add(new Hitbox(panelX, panelY, 22, 86, (button, modifiers) -> collapsed = false));
            return;
        }

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 25, 0x22163843);
        graphics.outline(panelX, panelY, panelW, panelH, 0x8846DFF4);
        graphics.fill(panelX, panelY, panelX + Math.max(38, panelW / 5), panelY + 2, CYAN);
        graphics.text(font, "ECHO: INDEX", panelX + 10, panelY + 9, CYAN, false);
        button(graphics, font, panelX + panelW - 24, panelY + 5, 16, 16, "-", true);
        HITBOXES.add(new Hitbox(panelX + panelW - 24, panelY + 5, 16, 16, (button, modifiers) -> collapsed = true));

        int searchX = panelX + 10;
        int searchY = panelY + 32;
        int searchW = panelW - 20;
        graphics.fill(searchX, searchY, searchX + searchW, searchY + 19, 0xDD05090E);
        graphics.outline(searchX, searchY, searchW, 19, searchFocused ? CYAN : 0x6638DFF4);
        String label = search.isBlank() && !searchFocused ? "Search ECHO: Index..." : search + (searchFocused ? "_" : "");
        graphics.text(font, trim(font, label, searchW - 18), searchX + 6, searchY + 6, search.isBlank() ? MUTED : TEXT, false);
        HITBOXES.add(new Hitbox(searchX, searchY, searchW, 19, (button, modifiers) -> searchFocused = true));

        int modeY = searchY + 25;
        chip(graphics, font, searchX, modeY, 34, "All", categoryFilter.isBlank() && !bookmarkedOnly, mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX, modeY, 34, 17, (button, modifiers) -> clearFilters(false)));
        chip(graphics, font, searchX + 38, modeY, 48, "Blocks", "$blocks".equals(categoryFilter), mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX + 38, modeY, 48, 17, (button, modifiers) -> setFilter("$blocks")));
        chip(graphics, font, searchX + 90, modeY, 62, "Machines", "$machines".equals(categoryFilter), mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX + 90, modeY, 62, 17, (button, modifiers) -> setFilter("$machines")));
        chip(graphics, font, searchX + 156, modeY, 42, "Tools", "$tools".equals(categoryFilter), mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX + 156, modeY, 42, 17, (button, modifiers) -> setFilter("$tools")));

        int filterY = modeY + 19;
        chip(graphics, font, searchX, filterY, 56, "Combat", "$combat".equals(categoryFilter), mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX, filterY, 56, 17, (button, modifiers) -> setFilter("$combat")));
        chip(graphics, font, searchX + 60, filterY, 46, "ECHO", "$echo".equals(categoryFilter), mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX + 60, filterY, 46, 17, (button, modifiers) -> setFilter("$echo")));
        chip(graphics, font, searchX + 110, filterY, 26, "*", bookmarkedOnly, mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX + 110, filterY, 26, 17, (button, modifiers) -> {
            bookmarkedOnly = !bookmarkedOnly;
            scroll = 0;
        }));
        chip(graphics, font, searchX + 140, filterY, 48, "Clear", false, mouseX, mouseY);
        HITBOXES.add(new Hitbox(searchX + 140, filterY, 48, 17, (button, modifiers) -> clearFilters(true)));

        List<ItemStack> items = IndexService.INSTANCE.filteredItems(Minecraft.getInstance().player, effectiveSearch(),
                Config.UI_MAX_RENDERED_ITEMS.get());
        int gridX = panelX + 10;
        int gridY = filterY + 24;
        int gridW = panelW - 22;
        int gridH = panelH - (gridY - panelY) - 30;
        int step = 24;
        int columns = Math.max(1, Math.min(Config.OVERLAY_MAX_COLUMNS.get(), gridW / step));
        int contentH = ((items.size() + columns - 1) / columns) * step;
        scroll = clamp(scroll, 0, Math.max(0, contentH - gridH));
        graphics.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        int startY = gridY - scroll;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            int x = gridX + (i % columns) * step;
            int y = startY + (i / columns) * step;
            if (y < gridY - 24 || y > gridY + gridH) {
                continue;
            }
            itemSlot(graphics, font, stack, x, y, mouseX, mouseY);
            Identifier itemId = IndexService.itemId(stack.getItem());
            if (ClientIndexState.isBookmarked(itemId)) {
                graphics.text(font, "*", x + 15, y - 1, WARN, false);
            }
            HITBOXES.add(new Hitbox(x, y, 20, 20, (button, modifiers) -> {
                if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 || button == 2) {
                    toggleBookmark(itemId);
                } else {
                    Minecraft.getInstance().setScreen(new IndexRecipeScreen(stack,
                            button == 1 ? IndexRecipeScreen.Mode.USES : IndexRecipeScreen.Mode.RECIPES));
                }
            }));
        }
        graphics.disableScissor();

        if (items.isEmpty()) {
            graphics.text(font, "No indexed items match.", gridX, gridY + 8, MUTED, false);
        }
        int footerY = panelY + panelH - 20;
        graphics.fill(panelX + 1, footerY - 3, panelX + panelW - 1, panelY + panelH - 1, 0xAA071017);
        graphics.text(font, "Indexed: " + items.size(), panelX + 10, footerY, CYAN, false);
        String footer = "R: recipe  U: uses  B: bookmark";
        graphics.text(font, trim(font, footer, panelW - 92), panelX + 82, footerY, MUTED, false);
    }

    private static boolean active(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> container) || Minecraft.getInstance().player == null) {
            return false;
        }
        String name = screen.getClass().getName();
        return IndexService.INSTANCE.overlayEnabled(Minecraft.getInstance().player)
                && !IndexService.INSTANCE.excludedScreen(name)
                && container.getImageWidth() > 0;
    }

    private static void layout(Screen screen) {
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) screen;
        int margin = 6;
        int gap = 8;
        int minW = 160;
        int requestedW = Config.OVERLAY_WIDTH.get();
        int maxScreenW = Math.max(minW, screen.width - margin * 2);
        int desiredW = clamp(requestedW, minW, maxScreenW);
        int compactH = clamp(container.getImageHeight(), 160, Math.max(160, screen.height - margin * 2));
        int tallH = Math.min(310, Math.max(160, screen.height - margin * 2));
        panelH = Config.OVERLAY_LAYOUT.get() == Config.OverlayLayout.TALL ? tallH : compactH;
        int desiredY = Config.OVERLAY_LAYOUT.get() == Config.OverlayLayout.TALL
                ? container.getTopPos() + container.getImageHeight() / 2 - panelH / 2
                : container.getTopPos();
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
        panelW = clamp(Math.min(desiredW, Math.max(minW, available)), minW, maxScreenW);
        panelX = useLeft ? container.getLeftPos() - gap - panelW : rightStart;
        panelX = clamp(panelX, margin, Math.max(margin, screen.width - panelW - margin));
    }

    private static void itemSlot(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, 20, 20);
        graphics.fill(x, y, x + 20, y + 20, ROW);
        graphics.outline(x, y, 20, 20, hover ? CYAN : 0x5538DFF4);
        graphics.item(stack, x + 2, y + 2);
        graphics.itemDecorations(font, stack, x + 2, y + 2);
        if (hover) {
            hoveredStack = stack;
            graphics.setTooltipForNextFrame(font, stack, x + 10, y + 10);
        }
    }

    private static void setFilter(String filter) {
        String next = filter == null ? "" : filter;
        categoryFilter = next.equals(categoryFilter) ? "" : next;
        searchFocused = false;
        scroll = 0;
    }

    private static void clearFilters(boolean includeSearch) {
        categoryFilter = "";
        bookmarkedOnly = false;
        if (includeSearch) {
            search = "";
        }
        searchFocused = false;
        scroll = 0;
    }

    private static String effectiveSearch() {
        StringBuilder builder = new StringBuilder();
        if (!search.isBlank()) {
            builder.append(search.trim());
        }
        if (!categoryFilter.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(categoryFilter);
        }
        if (bookmarkedOnly) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append("bookmarked");
        }
        return builder.toString();
    }

    private static void toggleBookmark(Identifier id) {
        boolean currently = ClientIndexState.isBookmarked(id);
        EchoNetClientActions.sendServerboundAction(new IndexActionPacket(
                currently ? IndexActionPacket.Action.UNBOOKMARK : IndexActionPacket.Action.BOOKMARK,
                id));
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

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String trim(Font font, String text, int width) {
        String safe = text == null ? "" : text;
        if (font.width(safe) <= width) {
            return safe;
        }
        String ellipsis = "...";
        while (!safe.isEmpty() && font.width(safe + ellipsis) > width) {
            safe = safe.substring(0, safe.length() - 1);
        }
        return safe + ellipsis;
    }

    private record Hitbox(int x, int y, int w, int h, ClickAction action) {
    }

    @FunctionalInterface
    private interface ClickAction {
        void click(int button, int modifiers);
    }
}
