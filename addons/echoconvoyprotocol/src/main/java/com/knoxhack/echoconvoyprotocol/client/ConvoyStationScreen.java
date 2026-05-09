package com.knoxhack.echoconvoyprotocol.client;

import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyStationBlockEntity;
import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyStationBlockEntity.StationStatus;
import com.knoxhack.echoconvoyprotocol.menu.ConvoyStationMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ConvoyStationScreen extends AbstractContainerScreen<ConvoyStationMenu> {
   public ConvoyStationScreen(ConvoyStationMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, ConvoyStationMenu.GUI_WIDTH, ConvoyStationMenu.GUI_HEIGHT);
      this.titleLabelX = 16;
      this.titleLabelY = 13;
      this.inventoryLabelX = 96;
      this.inventoryLabelY = 137;
   }

   @Override
   protected void init() {
      super.init();
      addRenderableWidget(Button.builder(Component.literal("SCAN"), button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ConvoyStationMenu.BUTTON_SCAN))
         .bounds(leftPos + 22, topPos + 108, 58, 18)
         .build());
      addRenderableWidget(Button.builder(Component.literal("UNLOAD"), button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ConvoyStationMenu.BUTTON_UNLOAD))
         .bounds(leftPos + 22, topPos + 130, 58, 18)
         .build());
   }

   @Override
   public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      int x = leftPos;
      int y = topPos;
      graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xEE101511);
      graphics.fill(x, y, x + imageWidth, y + 2, 0xFF92D66B);
      graphics.fill(x, y + 38, x + imageWidth, y + 40, 0xFF2A3A2B);
      graphics.fill(x + 18, y + 52, x + imageWidth - 18, y + 132, 0xAA07100B);
      for (Slot slot : menu.slots) {
         drawSlot(graphics, x + slot.x, y + slot.y);
      }
      graphics.fill(x + 118, y + 69, x + 230, y + 77, 0xFF34413A);
      graphics.fill(x + 118, y + 69, x + 118 + progressPixels(112), y + 77, 0xFF92D66B);
      graphics.fill(x + 118, y + 88, x + 230, y + 96, 0xFF34413A);
      graphics.fill(x + 118, y + 88, x + 118 + energyPixels(112), y + 96, 0xFF66E8FF);
      super.extractContents(graphics, mouseX, mouseY, partialTick);
      graphics.text(font, Component.literal("Vehicles nearby: " + menu.nearbyVehicles()), x + 118, y + 106, 0xD8F6FF, false);
      graphics.text(font, Component.literal(StationStatus.byId(menu.statusId()).label()), x + 118, y + 120, 0xFFD166, false);
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      graphics.text(font, Component.literal("ECHO " + menu.kind().displayName()), titleLabelX, titleLabelY, 0x92D66B, true);
      graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xD8F6FF, false);
   }

   private void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
      graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF34413A);
      graphics.fill(x, y, x + 16, y + 16, 0xFF0B120D);
   }

   private int progressPixels(int width) {
      int max = menu.maxProgress();
      return max <= 0 ? 0 : Math.min(width, menu.progress() * width / max);
   }

   private int energyPixels(int width) {
      return Math.min(width, menu.energy() * width / Math.max(1, menu.maxEnergy()));
   }
}
