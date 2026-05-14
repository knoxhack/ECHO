package com.knoxhack.echologisticsnetwork.client;

import com.knoxhack.echologisticsnetwork.block.entity.LogisticsBlockEntity;
import com.knoxhack.echologisticsnetwork.menu.LogisticsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class LogisticsScreen extends AbstractContainerScreen<LogisticsMenu> {
   private static final int PANEL = 0xEE08121A;
   private static final int CYAN = 0xFF66E8FF;
   private static final int GREEN = 0xFF8AF6B6;
   private static final int AMBER = 0xFFFFD166;
   private static final int RED = 0xFFFF8FA3;

   public LogisticsScreen(LogisticsMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, LogisticsMenu.GUI_WIDTH, LogisticsMenu.GUI_HEIGHT);
      this.titleLabelX = 16;
      this.titleLabelY = 12;
      this.inventoryLabelX = 97;
      this.inventoryLabelY = 137;
   }

   @Override
   public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      int x = leftPos;
      int y = topPos;
      graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL);
      graphics.outline(x, y, imageWidth, imageHeight, CYAN);
      graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 42, 0xDD101D24);
      for (Slot slot : menu.slots) {
         drawSlot(graphics, x + slot.x, y + slot.y);
      }
      drawDashboard(graphics, x, y);
      drawButton(graphics, x + 16, y + 58, 64, 18, "SCAN", mouseX, mouseY, true);
      drawButton(graphics, x + 16, y + 82, 64, 18, menu.canDispatch() ? "DISPATCH" : "MISSING", mouseX, mouseY, menu.canDispatch());
      drawButton(graphics, x + 16, y + 106, 64, 18, menu.kind() == com.knoxhack.echologisticsnetwork.block.LogisticsBlock.LogisticsKind.FACTION_TRADE_DEPOT ? "OFFER" : "CARD", mouseX, mouseY, true);
      drawButton(graphics, x + 16, y + 130, 64, 18, "CANCEL", mouseX, mouseY, menu.activeDeliveries() > 0);
      drawButton(graphics, x + 16, y + 190, 64, 18, "RELAY", mouseX, mouseY, menu.rewardCount() > 0 && menu.relayOnline());
      drawButton(graphics, x + 16, y + 214, 64, 18, "OFFERS", mouseX, mouseY, menu.depotOnline());
      drawButton(graphics, x + 16, y + 238, 64, 18, "DEPOT", mouseX, mouseY, menu.depotOnline() && menu.depotOffers() > 0 && menu.depotCooldown() <= 0);
      super.extractContents(graphics, mouseX, mouseY, partialTick);
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      graphics.text(this.font, Component.literal(fit("ECHO LOGISTICS // " + menu.kind().displayName(), 260)), titleLabelX, titleLabelY, CYAN, true);
      graphics.text(this.font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xD8F6FF, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int x = leftPos;
      int y = topPos;
      if (clickButton(event, x + 16, y + 58, 64, 18, LogisticsMenu.BUTTON_SCAN, true)) return true;
      if (clickButton(event, x + 16, y + 82, 64, 18, LogisticsMenu.BUTTON_REQUEST_LOADOUT, menu.canDispatch())) return true;
      if (clickButton(event, x + 16, y + 106, 64, 18, LogisticsMenu.BUTTON_CYCLE_LOADOUT, true)) return true;
      if (clickButton(event, x + 16, y + 130, 64, 18, LogisticsMenu.BUTTON_CANCEL_DELIVERIES, menu.activeDeliveries() > 0)) return true;
      if (clickButton(event, x + 16, y + 190, 64, 18, LogisticsMenu.BUTTON_CLAIM_RELAY, menu.rewardCount() > 0 && menu.relayOnline())) return true;
      if (clickButton(event, x + 16, y + 214, 64, 18, LogisticsMenu.BUTTON_REFRESH_OFFERS, menu.depotOnline())) return true;
      if (clickButton(event, x + 16, y + 238, 64, 18, LogisticsMenu.BUTTON_DEPOT_EXCHANGE, menu.depotOnline() && menu.depotOffers() > 0 && menu.depotCooldown() <= 0)) return true;
      return super.mouseClicked(event, doubleClick);
   }

   private boolean clickButton(MouseButtonEvent event, int x, int y, int w, int h, int id, boolean enabled) {
      if (!enabled || event.button() != 0 || !inside(event.x(), event.y(), x, y, w, h)) {
         return false;
      }
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.gameMode != null) {
         minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
      }
      return true;
   }

   private void drawDashboard(GuiGraphicsExtractor graphics, int x, int y) {
      int sx = x + 96;
      int sy = y + 48;
      graphics.text(font, Component.literal("Blocks " + menu.blockCount() + " | Endpoints " + menu.endpointCount()), sx, sy, GREEN, false);
      graphics.text(font, Component.literal(fit("Dock " + online(menu.dockOnline()) + " | Relay " + online(menu.relayOnline()) + " | Depot " + online(menu.depotOnline()), 112)),
         sx + 142, sy, menu.dockOnline() ? CYAN : AMBER, false);
      graphics.text(font, Component.literal("Categories " + menu.stockRows()), sx, sy + 12, GREEN, false);
      graphics.text(font, Component.literal("Low " + menu.missingRows()), sx + 86, sy + 12, menu.missingRows() > 0 ? AMBER : GREEN, false);
      graphics.text(font, Component.literal("Kits " + menu.readyRows()), sx + 148, sy + 12, GREEN, false);
      graphics.text(font, Component.literal("Drones " + menu.activeDeliveries()), sx + 226, sy + 12, menu.activeDeliveries() > 0 ? CYAN : 0xFF8CA7B5, false);
      graphics.text(font, Component.literal(fit("Selected: " + endpointName() + " | "
            + (menu.selectedReady() ? "READY" : "MISSING " + menu.selectedMissing())
            + " | Delivery " + menu.firstDeliveryStatus()
            + (menu.activeDeliveries() > 0 ? " ETA " + menu.firstDeliveryEta() + "t" : ""), 318)),
         x + 16, y + 30, menu.selectedReady() ? GREEN : AMBER, false);
      graphics.text(font, Component.literal("Relay rewards " + menu.rewardCount()), x + 16, y + 166, menu.rewardCount() > 0 ? AMBER : 0xFF8CA7B5, false);
      graphics.text(font, Component.literal("Depot offers " + menu.depotOffers()), x + 16, y + 178, menu.depotOffers() > 0 ? CYAN : 0xFF8CA7B5, false);
      graphics.text(font, Component.literal("Control cooldown " + menu.cooldown() + "t"), sx, y + 238, menu.cooldown() > 0 ? RED : 0xFF8CA7B5, false);
      graphics.text(font, Component.literal("Depot cooldown " + menu.depotCooldown() + "t"), sx + 126, y + 238, menu.depotCooldown() > 0 ? AMBER : 0xFF8CA7B5, false);
   }

   private void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
      graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF2A3B44);
      graphics.fill(x, y, x + 16, y + 16, 0xFF0B151C);
   }

   private void drawButton(GuiGraphicsExtractor graphics, int x, int y, int w, int h, String label, int mouseX, int mouseY, boolean enabled) {
      int color = enabled ? (inside(mouseX, mouseY, x, y, w, h) ? CYAN : 0xFF3B6974) : 0xFF273036;
      graphics.fill(x, y, x + w, y + h, enabled ? 0xAA122530 : 0x77101418);
      graphics.outline(x, y, w, h, color);
      graphics.text(font, Component.literal(label), x + Math.max(3, (w - font.width(label)) / 2), y + 5, enabled ? 0xFFE9FBFF : 0xFF66777D, false);
   }

   private String online(boolean value) {
      return value ? "ONLINE" : "OFFLINE";
   }

   private String endpointName() {
      com.knoxhack.echologisticsnetwork.block.LogisticsBlock.LogisticsKind kind = menu.selectedEndpointKind();
      return kind == null ? "No endpoint" : kind.displayName();
   }

   private boolean inside(double px, double py, int x, int y, int w, int h) {
      return px >= x && py >= y && px < x + w && py < y + h;
   }

   private String fit(String text, int maxWidth) {
      if (font.width(text) <= maxWidth) {
         return text;
      }
      String suffix = "...";
      return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width(suffix))) + suffix;
   }
}
