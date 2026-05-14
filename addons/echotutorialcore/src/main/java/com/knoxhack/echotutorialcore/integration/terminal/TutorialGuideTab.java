package com.knoxhack.echotutorialcore.integration.terminal;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import com.knoxhack.echotutorialcore.api.TutorialCategory;
import com.knoxhack.echotutorialcore.api.card.TutorialCard;
import com.knoxhack.echotutorialcore.data.TutorialCoreRegistries;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalUi;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class TutorialGuideTab implements TerminalTab {
    private static final int ACCENT = 0xFF92F7A6;
    private final TerminalTabDescriptor descriptor =
            new TerminalTabDescriptor(TutorialTerminalClientIntegration.TAB_ID, "GUIDE", 45, ACCENT);
    private final TerminalTabChrome chrome =
            TerminalTabChrome.of("ECHO Guide", TerminalTabChrome.GROUP_FIELD, "GD",
                    "Tutorial cards, hints, and guidance", 45);

    private Identifier selectedCardId = null;

    @Override
    public TerminalTabDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public TerminalTabChrome chrome() {
        return chrome;
    }

    @Override
    public void onSelected(TerminalRenderContext context) {
        selectedCardId = null;
    }

    @Override
    public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY,
            float partialTick) {
        int x = context.contentX() + 12;
        int y = context.contentY() + 10 - context.scrollY();
        int w = context.contentWidth() - 24;

        // Header
        y = TerminalUi.sectionHeader(context, graphics, "ECHO GUIDE", "TUTORIALCORE", x, y, w, ACCENT);
        y += 4;

        // Guide mode panel
        int panelH = 36;
        TerminalUi.flatHudPanel(context, graphics, x, y, w, panelH, ACCENT);
        TerminalUi.line(context, graphics, "Guide Mode:", x + 12, y + 10, 90, TerminalUi.muted(context));
        TerminalUi.line(context, graphics, "NORMAL", x + 94, y + 10, 80, TerminalUi.text(context));
        y += panelH + 10;

        // Categories
        for (TutorialCategory category : TutorialCategory.values()) {
            List<TutorialCard> cards = TutorialCoreRegistries.getCardsByCategory(category);
            if (cards.isEmpty()) continue;

            int sectionH = estimateSectionHeight(cards);
            TerminalUi.flatHudPanel(context, graphics, x, y, w, sectionH, ACCENT);

            int sy = y + 8;
            TerminalUi.line(context, graphics, category.name(), x + 12, sy, w - 24, ACCENT);
            sy += 16;

            for (TutorialCard card : cards) {
                boolean selected = card.id().equals(selectedCardId);
                int rowColor = selected ? TerminalUi.ROW_SELECTED : TerminalUi.ROW;
                graphics.fill(x + 8, sy, x + w - 8, sy + 14, rowColor);
                TerminalUi.line(context, graphics, card.title(), x + 14, sy + 3, w - 28, selected ? ACCENT : TerminalUi.text(context));
                sy += 14;

                if (selected) {
                    sy = renderCardDetail(context, graphics, card, x + 14, sy, w - 28);
                }
            }

            y += sectionH + 10;
        }
    }

    @Override
    public int contentHeight(TerminalRenderContext context) {
        int h = 60; // header + guide mode
        for (TutorialCategory category : TutorialCategory.values()) {
            List<TutorialCard> cards = TutorialCoreRegistries.getCardsByCategory(category);
            if (cards.isEmpty()) continue;
            h += estimateSectionHeight(cards) + 10;
        }
        return h + 20;
    }

    @Override
    public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        int x = context.contentX() + 12;
        int y = context.contentY() + 10 - context.scrollY();
        int w = context.contentWidth() - 24;
        y += 50; // skip header + guide mode panel

        for (TutorialCategory category : TutorialCategory.values()) {
            List<TutorialCard> cards = TutorialCoreRegistries.getCardsByCategory(category);
            if (cards.isEmpty()) continue;

            int sectionH = estimateSectionHeight(cards);
            int sy = y + 24; // after category title

            for (TutorialCard card : cards) {
                if (TerminalUi.inside(mouseX, mouseY, x + 8, sy, w - 16, 14)) {
                    if (card.id().equals(selectedCardId)) {
                        selectedCardId = null;
                    } else {
                        selectedCardId = card.id();
                        // Also send a chat fallback so the player sees the card content
                        if (context.player() != null) {
                            context.player().sendSystemMessage(Component.literal("[ECHO Guide] " + card.title()));
                            for (String line : card.body()) {
                                context.player().sendSystemMessage(Component.literal("  " + line));
                            }
                        }
                    }
                    context.playCommandSound();
                    return true;
                }
                sy += 14;
                if (card.id().equals(selectedCardId)) {
                    sy += detailHeight(card, w - 28);
                }
            }
            y += sectionH + 10;
        }
        return false;
    }

    private static int estimateSectionHeight(List<TutorialCard> cards) {
        int h = 28; // title padding
        for (TutorialCard card : cards) {
            h += 14; // title row
        }
        return Math.max(h, 40);
    }

    private static int renderCardDetail(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            TutorialCard card, int x, int y, int maxW) {
        int startY = y;
        for (String paragraph : card.body()) {
            y = TerminalUi.wrap(context, graphics, paragraph, x, y, maxW, TerminalUi.muted(context));
            y += 4;
        }
        if (!card.steps().isEmpty()) {
            y = TerminalUi.wrap(context, graphics, "Steps:", x, y, maxW, TerminalUi.accent(context));
            y += 2;
            for (String step : card.steps()) {
                y = TerminalUi.wrap(context, graphics, "  " + step, x, y, maxW, TerminalUi.text(context));
                y += 2;
            }
        }
        if (!card.commonMistakes().isEmpty()) {
            y = TerminalUi.wrap(context, graphics, "Common Mistakes:", x, y, maxW, TerminalUi.warning(context));
            y += 2;
            for (String mistake : card.commonMistakes()) {
                y = TerminalUi.wrap(context, graphics, "  " + mistake, x, y, maxW, TerminalUi.text(context));
                y += 2;
            }
        }
        return y - startY + 8;
    }

    private static int detailHeight(TutorialCard card, int maxW) {
        // Approximate height for layout estimation
        int h = card.body().size() * 16;
        h += card.steps().isEmpty() ? 0 : 16 + card.steps().size() * 14;
        h += card.commonMistakes().isEmpty() ? 0 : 16 + card.commonMistakes().size() * 14;
        return h + 8;
    }
}
