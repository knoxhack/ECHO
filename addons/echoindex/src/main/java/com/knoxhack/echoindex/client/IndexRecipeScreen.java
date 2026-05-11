package com.knoxhack.echoindex.client;

import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoindex.Config;
import com.knoxhack.echoindex.EchoIndexClient;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.knoxhack.echoindex.network.IndexActionPacket;
import com.knoxhack.echoindex.service.IndexService;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class IndexRecipeScreen extends Screen {
    private static final int BG = 0xF2060D13;
    private static final int PANEL = 0xF00B151D;
    private static final int ROW = 0xAA102630;
    private static final int CYAN = 0xFF66E8FF;
    private static final int TEXT = 0xFFE9FBFF;
    private static final int MUTED = 0xFF8CA7B5;
    private static final int WARN = 0xFFFFD166;

    private final ItemStack focusStack;
    private Mode mode;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int selected;
    private int firstVisible;

    public IndexRecipeScreen(ItemStack focusStack, Mode mode) {
        super(Component.translatable("screen.echoindex.recipes"));
        this.focusStack = focusStack == null ? ItemStack.EMPTY : focusStack.copy();
        this.mode = mode == null ? Mode.RECIPES : mode;
    }

    @Override
    protected void init() {
        EchoNetClientActions.sendServerboundAction(new IndexActionPacket(IndexActionPacket.Action.REQUEST_SYNC, null));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        layout();
        graphics.fill(0, 0, width, height, 0xDD02070A);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 3, CYAN);
        graphics.fill(panelX + 14, panelY + 30, panelX + panelW - 14, panelY + 31, 0x6632BFD7);

        Font font = Minecraft.getInstance().font;
        List<IndexRecipeView> recipes = recipes(focusStack.getItem());
        selected = clamp(selected, 0, Math.max(0, recipes.size() - 1));
        firstVisible = clamp(firstVisible, 0, Math.max(0, recipes.size() - visibleRows()));
        if (selected < firstVisible) {
            firstVisible = selected;
        } else if (selected >= firstVisible + visibleRows()) {
            firstVisible = Math.max(0, selected - visibleRows() + 1);
        }
        graphics.text(font, titleLine(), panelX + 14, panelY + 11, CYAN, true);
        graphics.text(font, focusStack.getHoverName(), panelX + 14, panelY + 23, TEXT, false);
        graphics.item(focusStack, panelX + panelW - 34, panelY + 10);

        drawModeButton(graphics, font, panelX + 14, panelY + 42, Mode.RECIPES, mouseX, mouseY);
        drawModeButton(graphics, font, panelX + 98, panelY + 42, Mode.USES, mouseX, mouseY);
        drawCloseHint(graphics, font);

        if (recipes.isEmpty()) {
            drawEmpty(graphics, font);
            return;
        }
        drawRecipeList(graphics, font, recipes, mouseX, mouseY);
        drawRecipeDetails(graphics, font, recipes.get(selected), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        layout();
        if (inside(event.x(), event.y(), panelX + 14, panelY + 42, 76, 18)) {
            mode = Mode.RECIPES;
            selected = 0;
            return true;
        }
        if (inside(event.x(), event.y(), panelX + 98, panelY + 42, 76, 18)) {
            mode = Mode.USES;
            selected = 0;
            return true;
        }
        List<IndexRecipeView> recipes = recipes(focusStack.getItem());
        int listX = panelX + 14;
        int listY = panelY + 72;
        int rowH = 24;
        int visible = Math.min(recipes.size() - firstVisible, visibleRows());
        for (int i = 0; i < visible; i++) {
            if (inside(event.x(), event.y(), listX, listY + i * rowH, listWidth(), 21)) {
                selected = firstVisible + i;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        layout();
        if (!inside(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        List<IndexRecipeView> recipes = recipes(focusStack.getItem());
        int maxFirst = Math.max(0, recipes.size() - visibleRows());
        if (scrollY < 0 && firstVisible < maxFirst) {
            firstVisible++;
            selected = clamp(selected, firstVisible, Math.max(firstVisible, firstVisible + visibleRows() - 1));
            return true;
        }
        if (scrollY > 0 && firstVisible > 0) {
            firstVisible--;
            selected = clamp(selected, firstVisible, Math.max(firstVisible, firstVisible + visibleRows() - 1));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        List<IndexRecipeView> recipes = recipes(focusStack.getItem());
        if (EchoIndexClient.SHOW_RECIPE_KEY.matches(event)) {
            mode = Mode.RECIPES;
            selected = 0;
            firstVisible = 0;
            return true;
        }
        if (EchoIndexClient.SHOW_USAGE_KEY.matches(event)) {
            mode = Mode.USES;
            selected = 0;
            firstVisible = 0;
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_UP && selected > 0) {
            selected--;
            if (selected < firstVisible) {
                firstVisible = selected;
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_DOWN && selected + 1 < recipes.size()) {
            selected++;
            if (selected >= firstVisible + visibleRows()) {
                firstVisible++;
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_PAGE_UP) {
            selected = Math.max(0, selected - visibleRows());
            firstVisible = Math.max(0, firstVisible - visibleRows());
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_PAGE_DOWN) {
            selected = Math.min(Math.max(0, recipes.size() - 1), selected + visibleRows());
            firstVisible = Math.min(Math.max(0, recipes.size() - visibleRows()), firstVisible + visibleRows());
            return true;
        }
        return super.keyPressed(event);
    }

    private void layout() {
        panelW = Math.min(500, Math.max(320, width - 44));
        panelH = Math.min(310, Math.max(232, height - 36));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
    }

    private void drawModeButton(GuiGraphicsExtractor graphics, Font font, int x, int y, Mode target, int mouseX, int mouseY) {
        boolean active = mode == target;
        boolean hover = inside(mouseX, mouseY, x, y, 76, 18);
        graphics.fill(x, y, x + 76, y + 18, active ? 0xFF123241 : hover ? 0xCC102630 : PANEL);
        graphics.fill(x, y + 16, x + 76, y + 18, active ? CYAN : 0xFF2F5B68);
        graphics.centeredText(font, target.label(), x + 38, y + 5, active ? TEXT : MUTED);
    }

    private void drawCloseHint(GuiGraphicsExtractor graphics, Font font) {
        graphics.text(font, "ESC", panelX + panelW - 36, panelY + panelH - 18, MUTED, false);
    }

    private void drawEmpty(GuiGraphicsExtractor graphics, Font font) {
        int x = panelX + 18;
        int y = panelY + 86;
        graphics.fill(x, y, panelX + panelW - 18, y + 58, 0xAA071014);
        graphics.text(font, "No " + mode.label().toLowerCase() + " indexed for this item.", x + 12, y + 15, WARN, false);
        String message = mode == Mode.USES
                ? "No recipe, machine, catalyst, or source rule currently consumes this item."
                : "No crafting or source card is currently indexed for this item.";
        graphics.textWithWordWrap(font, Component.literal(message),
                x + 12, y + 30, panelW - 60, MUTED);
    }

    private void drawRecipeList(GuiGraphicsExtractor graphics, Font font, List<IndexRecipeView> recipes, int mouseX, int mouseY) {
        int x = panelX + 14;
        int y = panelY + 72;
        int rowH = 24;
        int visible = Math.min(recipes.size() - firstVisible, visibleRows());
        String range = recipes.isEmpty() ? "0 / 0"
                : (firstVisible + 1) + "-" + (firstVisible + visible) + " / " + recipes.size();
        graphics.text(font, "Matches " + range, x, y - 12, MUTED, false);
        for (int i = 0; i < visible; i++) {
            int index = firstVisible + i;
            IndexRecipeView recipe = recipes.get(index);
            boolean active = index == selected;
            boolean hover = inside(mouseX, mouseY, x, y + i * rowH, listWidth(), 21);
            int rowY = y + i * rowH;
            graphics.fill(x, rowY, x + listWidth(), rowY + 21, active ? 0xFF123241 : hover ? 0xCC102630 : ROW);
            graphics.fill(x, rowY + 19, x + listWidth(), rowY + 21, active ? CYAN : 0x552F5B68);
            graphics.item(recipeIcon(recipe), x + 3, rowY + 2);
            graphics.text(font, trim(font, recipe.title(), listWidth() - 28), x + 24, rowY + 6, active ? TEXT : 0xFFD8F6FF, false);
        }
    }

    private void drawRecipeDetails(GuiGraphicsExtractor graphics, Font font, IndexRecipeView recipe, int mouseX, int mouseY) {
        int x = panelX + 24 + listWidth();
        int y = panelY + 72;
        int w = panelW - listWidth() - 42;
        graphics.fill(x, y, x + w, panelY + panelH - 30, 0xAA071014);
        graphics.text(font, trim(font, recipe.title(), w - 18), x + 9, y + 9, CYAN, true);
        graphics.text(font, recipe.categoryId().getPath(), x + 9, y + 22, MUTED, false);
        if (recipe.processTicks() > 0) {
            graphics.text(font, recipe.processTicks() + " ticks", x + w - 68, y + 22, MUTED, false);
        }
        graphics.text(font, trim(font, "Source: " + recipe.sourceModId(), w - 18), x + 9, y + 34, MUTED, false);
        int slotX = x + 10;
        int slotY = y + 54;
        int slot = 0;
        for (IndexRecipeSlot recipeSlot : recipe.slots()) {
            drawSlotGroup(graphics, font, recipeSlot, slotX, slotY + slot * 27, w - 20, mouseX, mouseY);
            slot++;
            if (slot > 5) {
                break;
            }
        }
        int noteY = Math.min(panelY + panelH - 62, slotY + slot * 27 + 4);
        for (String note : recipe.notes().stream().limit(2).toList()) {
            graphics.textWithWordWrap(font, Component.literal(note), x + 10, noteY, w - 20, MUTED);
            noteY += 20;
        }
        if (Config.DEBUG_SHOW_RECIPE_IDS.get()) {
            graphics.text(font, trim(font, recipe.id().toString(), w - 20), x + 10,
                    panelY + panelH - 42, 0xFF6C7E84, false);
        }
    }

    private void drawSlotGroup(GuiGraphicsExtractor graphics, Font font, IndexRecipeSlot slot, int x, int y, int width,
            int mouseX, int mouseY) {
        int labelColor = switch (slot.role()) {
            case OUTPUT -> 0xFFA8F7C5;
            case MACHINE -> WARN;
            case CATALYST -> 0xFFE09CFF;
            case INPUT -> CYAN;
            default -> MUTED;
        };
        graphics.text(font, slotLabel(slot), x, y + 6, labelColor, false);
        int itemX = x + 58;
        for (ItemStack stack : slot.stacks().stream().limit(Math.max(1, (width - 62) / 20)).toList()) {
            graphics.fill(itemX, y, itemX + 20, y + 20, ROW);
            graphics.outline(itemX, y, 20, 20, 0x5538DFF4);
            graphics.item(stack, itemX + 2, y + 2);
            if (inside(mouseX, mouseY, itemX, y, 20, 20)) {
                graphics.setTooltipForNextFrame(font, stack, itemX + 10, y + 10);
            }
            itemX += 22;
        }
    }

    private List<IndexRecipeView> recipes(Item item) {
        Player player = Minecraft.getInstance().player;
        return mode == Mode.USES
                ? IndexService.INSTANCE.usesFor(player, item)
                : IndexService.INSTANCE.recipesFor(player, item);
    }

    private ItemStack recipeIcon(IndexRecipeView recipe) {
        for (IndexRecipeSlot slot : recipe.slots()) {
            if (slot.role() == IndexSlotRole.OUTPUT && !slot.stacks().isEmpty() && !slot.stacks().getFirst().isEmpty()) {
                return slot.stacks().getFirst();
            }
        }
        return recipe.machine().isEmpty() ? focusStack : recipe.machine();
    }

    private String titleLine() {
        return mode == Mode.USES ? "ECHO: INDEX | USES" : "ECHO: INDEX | RECIPES";
    }

    private String slotLabel(IndexRecipeSlot slot) {
        if (!slot.label().isBlank()) {
            return slot.label();
        }
        return switch (slot.role()) {
            case OUTPUT -> "Output";
            case MACHINE -> "Machine";
            case CATALYST -> "Catalyst";
            case INPUT -> "Input";
            default -> "Info";
        };
    }

    private int listWidth() {
        return Math.max(142, Math.min(190, panelW / 2 - 24));
    }

    private int visibleRows() {
        return Math.max(1, (panelH - 108) / 24);
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

    public enum Mode {
        RECIPES("Recipes"),
        USES("Uses");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }
}
