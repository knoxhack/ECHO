package com.knoxhack.echorecovery.client.screen;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.config.RecoveryConfig;
import com.knoxhack.echorecovery.menu.GraveMenu;
import com.knoxhack.echorecovery.net.RecoverAllPacket;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GraveScreen extends AbstractContainerScreen<GraveMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(EchoRecovery.MODID, "textures/gui/grave.png");
    private static final int PANEL_COLOR = 0xEE1A1A1A;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int TITLE_COLOR = 0xFFCCCCCC;
    private static final int TEXT_COLOR = 0xFFAAAAAA;
    private static final int BUTTON_BG = 0xFF333333;
    private static final int BUTTON_HOVER = 0xFF555555;
    private static final int BUTTON_TEXT = 0xFF00E5FF;
    private static final int WARN_COLOR = 0xFFFFAA00;

    public GraveScreen(GraveMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 222);
        this.inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("screen.echorecovery.grave.recover_all"), button -> {
            if (menu.getGrave() != null) {
                EchoNetClientActions.trySendServerboundAction(new RecoverAllPacket(menu.getPos()));
            }
        }).bounds(leftPos + imageWidth - 72, topPos + 4, 64, 14).build());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL_COLOR);
        graphics.fill(x, y, x + imageWidth, y + 1, BORDER_COLOR);
        graphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, BORDER_COLOR);
        graphics.fill(x, y, x + 1, y + imageHeight, BORDER_COLOR);
        graphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, BORDER_COLOR);

        graphics.text(this.font, this.title, x + 8, y + 6, TITLE_COLOR, false);

        graphics.text(this.font, this.playerInventoryTitle, x + 8, y + this.inventoryLabelY, TEXT_COLOR, false);

        var grave = menu.getGrave();
        if (grave != null) {
            int count = grave.itemCount();
            int max = grave.getContainerSize();
            String info = count + "/" + max;
            int infoWidth = this.font.width(info);
            graphics.text(this.font, info, x + imageWidth - 78 - infoWidth, y + 6, TEXT_COLOR, false);

            int expirationMinutes = RecoveryConfig.GRAVE_EXPIRATION_MINUTES.get();
            if (expirationMinutes > 0) {
                long elapsed = (System.currentTimeMillis() - grave.createdAt()) / 60000L;
                long remaining = expirationMinutes - elapsed;
                if (remaining > 0) {
                    String timeText = "Expires in " + remaining + "m";
                    graphics.text(this.font, timeText, x + 8, y + imageHeight - 14, TEXT_COLOR, false);
                } else {
                    graphics.text(this.font, "Expired", x + 8, y + imageHeight - 14, WARN_COLOR, false);
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }
}
