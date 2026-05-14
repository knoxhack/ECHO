package com.knoxhack.echomultiblockcore.client;

import com.knoxhack.echomultiblockcore.api.MultiblockState;
import com.knoxhack.echomultiblockcore.menu.MultiblockControllerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MultiblockControllerScreen extends AbstractContainerScreen<MultiblockControllerMenu> {
    private static final int PANEL = 0xEE071018;
    private static final int CYAN = 0xFF66E8FF;
    private static final int GREEN = 0xFF8AF6B6;
    private static final int AMBER = 0xFFFFD166;
    private static final int RED = 0xFFFF8FA3;

    public MultiblockControllerScreen(MultiblockControllerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MultiblockControllerMenu.GUI_WIDTH, MultiblockControllerMenu.GUI_HEIGHT);
        this.titleLabelX = 16;
        this.titleLabelY = 12;
        this.inventoryLabelY = 10000;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL);
        graphics.outline(x, y, imageWidth, imageHeight, CYAN);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 42, 0xDD101D24);
        drawBars(graphics, x, y);
        drawButtons(graphics, x, y, mouseX, mouseY);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, Component.literal("ECHO MULTIBLOCK // CONTROLLER"), titleLabelX, titleLabelY, CYAN, true);
        graphics.text(font, Component.literal("State " + stateName()
                + " | Integrity " + menu.integrity() + "%"
                + " | Completion " + menu.completion() + "%"), 16, 30, stateColor(), false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = leftPos;
        int y = topPos;
        if (click(event, x + 16, y + 156, 72, 18, MultiblockControllerMenu.BUTTON_VALIDATE, true)) return true;
        if (click(event, x + 96, y + 156, 72, 18, MultiblockControllerMenu.BUTTON_START, true)) return true;
        if (click(event, x + 176, y + 156, 60, 18, MultiblockControllerMenu.BUTTON_RETRY, menu.blocked())) return true;
        if (click(event, x + 244, y + 156, 72, 18, MultiblockControllerMenu.BUTTON_AUTOBUILD, true)) return true;
        if (click(event, x + 16, y + 182, 72, 18, MultiblockControllerMenu.BUTTON_PAUSE, menu.queueSize() > 0)) return true;
        if (click(event, x + 96, y + 182, 72, 18, MultiblockControllerMenu.BUTTON_RESUME, menu.queueSize() > 0)) return true;
        if (click(event, x + 176, y + 182, 60, 18, MultiblockControllerMenu.BUTTON_CLEAR, menu.queueSize() > 0)) return true;
        if (click(event, x + 244, y + 182, 72, 18, MultiblockControllerMenu.BUTTON_REPAIR, menu.integrity() < 100)) return true;
        return super.mouseClicked(event, doubleClick);
    }

    private void drawBars(GuiGraphicsExtractor graphics, int x, int y) {
        drawMetric(graphics, x + 16, y + 58, 138, "Integrity", menu.integrity(), menu.integrity() >= 70 ? GREEN : AMBER);
        drawMetric(graphics, x + 176, y + 58, 138, "Completion", menu.completion(), menu.completion() >= 100 ? GREEN : CYAN);
        graphics.text(font, Component.literal("Robots " + menu.robots() + " | Queue " + menu.queueSize()
                + " | Upgrades " + menu.upgrades()), x + 16, y + 96, 0xFFD8F6FF, false);
        graphics.text(font, Component.literal("Capabilities " + (menu.capabilityOk() ? "READY" : "BLOCKED")
                + " | Damage groups " + menu.damageGroups()
                + " | Repair actions " + menu.repairActions()), x + 16, y + 112,
                menu.capabilityOk() ? GREEN : RED, false);
        graphics.text(font, Component.literal(menu.blocked()
                ? "Blocked tasks need operator attention."
                : "Queue ready. Use recipes, repairs, or auto-builder actions."), x + 16, y + 130,
                menu.blocked() ? AMBER : 0xFF8CA7B5, false);
        graphics.text(font, Component.translatable("screen.echomultiblockcore.controller.progression",
                        menu.progressionTier(), menu.featuredRecipes()), x + 176, y + 96,
                menu.progressionTier() > 0 ? GREEN : 0xFF8CA7B5, false);
    }

    private void drawMetric(GuiGraphicsExtractor graphics, int x, int y, int width, String label, int value, int color) {
        int fill = Math.max(0, Math.min(width, Math.round(width * Math.max(0, Math.min(100, value)) / 100.0F)));
        graphics.text(font, Component.literal(label + " " + value + "%"), x, y - 12, color, false);
        graphics.fill(x, y, x + width, y + 8, 0xFF12202A);
        graphics.fill(x, y, x + fill, y + 8, color);
        graphics.outline(x, y, width, 8, 0xFF2E4B58);
    }

    private void drawButtons(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        drawButton(graphics, x + 16, y + 156, 72, 18, "VALIDATE", mouseX, mouseY, true);
        drawButton(graphics, x + 96, y + 156, 72, 18, "START", mouseX, mouseY, true);
        drawButton(graphics, x + 176, y + 156, 60, 18, "RETRY", mouseX, mouseY, menu.blocked());
        drawButton(graphics, x + 244, y + 156, 72, 18, "BUILD", mouseX, mouseY, true);
        drawButton(graphics, x + 16, y + 182, 72, 18, "PAUSE", mouseX, mouseY, menu.queueSize() > 0);
        drawButton(graphics, x + 96, y + 182, 72, 18, "RESUME", mouseX, mouseY, menu.queueSize() > 0);
        drawButton(graphics, x + 176, y + 182, 60, 18, "CLEAR", mouseX, mouseY, menu.queueSize() > 0);
        drawButton(graphics, x + 244, y + 182, 72, 18, "REPAIR", mouseX, mouseY, menu.integrity() < 100);
    }

    private boolean click(MouseButtonEvent event, int x, int y, int w, int h, int id, boolean enabled) {
        if (!enabled || event.button() != 0 || !inside(event.x(), event.y(), x, y, w, h)) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
        return true;
    }

    private void drawButton(GuiGraphicsExtractor graphics, int x, int y, int w, int h, String label, int mouseX, int mouseY, boolean enabled) {
        int color = enabled ? (inside(mouseX, mouseY, x, y, w, h) ? CYAN : 0xFF3B6974) : 0xFF273036;
        graphics.fill(x, y, x + w, y + h, enabled ? 0xAA122530 : 0x77101418);
        graphics.outline(x, y, w, h, color);
        graphics.text(font, Component.literal(label), x + Math.max(3, (w - font.width(label)) / 2), y + 5,
                enabled ? 0xFFE9FBFF : 0xFF66777D, false);
    }

    private boolean inside(double px, double py, int x, int y, int w, int h) {
        return px >= x && py >= y && px < x + w && py < y + h;
    }

    private String stateName() {
        MultiblockState[] values = MultiblockState.values();
        int ordinal = menu.stateOrdinal();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal].name() : "UNKNOWN";
    }

    private int stateColor() {
        return switch (stateName()) {
            case "FORMED", "ACTIVE" -> GREEN;
            case "DAMAGED", "JAMMED", "OVERLOADED" -> AMBER;
            case "OFFLINE" -> RED;
            default -> CYAN;
        };
    }
}
