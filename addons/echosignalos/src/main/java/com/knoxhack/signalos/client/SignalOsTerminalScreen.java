package com.knoxhack.signalos.client;

import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalChapter;
import com.knoxhack.signalos.api.TerminalDiagnosticProvider;
import com.knoxhack.signalos.api.TerminalMission;
import com.knoxhack.signalos.api.TerminalPage;
import com.knoxhack.signalos.content.SignalOsContentRegistry;
import com.knoxhack.signalos.menu.SignalOsTerminalMenu;
import com.knoxhack.signalos.network.SignalOsActionPacket;
import com.knoxhack.signalos.service.SignalOsBuiltinActions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

public class SignalOsTerminalScreen extends AbstractContainerScreen<SignalOsTerminalMenu> {
    private static final int BG = 0xFF03080D;
    private static final int PANEL = 0xF0071017;
    private static final int PANEL_ALT = 0xE80B1720;
    private static final int ROW = 0x94112430;
    private static final int ROW_HOVER = 0xD0152B38;
    private static final int TEXT = 0xFFE9FBFF;
    private static final int MUTED = 0xFF8CA7B5;
    private static final int CYAN = 0xFF66E8FF;
    private static final int GREEN = 0xFF91F7A5;
    private static final int WARN = 0xFFFFD166;
    private static final int RED = 0xFFFF8FA3;

    private final List<HitBox> hitBoxes = new ArrayList<>();
    private Identifier selectedChapterId;
    private String selectedPage = "missions";
    private Identifier selectedMissionId;
    private Identifier selectedArchiveId;
    private int ticks;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int sidebarX;
    private int sidebarY;
    private int sidebarW;
    private int sidebarH;
    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;

    public SignalOsTerminalScreen(SignalOsTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ticks++;
        hitBoxes.clear();
        layout();
        List<TerminalChapter> chapters = SignalOsContentRegistry.chapters();
        normalizeSelection(chapters);

        graphics.fill(0, 0, width, height, BG);
        drawFrame(graphics, chapters);
        drawSidebar(graphics, chapters, mouseX, mouseY);
        drawPageTabs(graphics, activeChapter(chapters), mouseX, mouseY);
        drawPage(graphics, activeChapter(chapters), mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || SignalOSClient.OPEN_TERMINAL_KEY.matches(event)) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_LEFT || event.key() == GLFW.GLFW_KEY_RIGHT) {
            return cyclePage(event.key() == GLFW.GLFW_KEY_RIGHT ? 1 : -1);
        }
        if (event.key() == GLFW.GLFW_KEY_UP || event.key() == GLFW.GLFW_KEY_DOWN) {
            return cycleChapter(event.key() == GLFW.GLFW_KEY_DOWN ? 1 : -1);
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (HitBox box : List.copyOf(hitBoxes)) {
            if (!box.inside(event.x(), event.y())) {
                continue;
            }
            switch (box.kind()) {
                case CHAPTER -> {
                    selectedChapterId = box.id();
                    selectedMissionId = null;
                    selectedArchiveId = null;
                    playClick();
                    return true;
                }
                case PAGE -> {
                    selectedPage = box.payload();
                    selectedMissionId = null;
                    selectedArchiveId = null;
                    playClick();
                    return true;
                }
                case MISSION -> {
                    selectedMissionId = box.id();
                    playClick();
                    return true;
                }
                case ARCHIVE -> {
                    selectedArchiveId = box.id();
                    TerminalArchiveRecord record = SignalOsContentRegistry.archive(box.id());
                    if (record != null && !record.locked() && !SignalOsClientState.isArchiveRead(record.id())) {
                        send(SignalOsBuiltinActions.PAGE_ARCHIVES, SignalOsBuiltinActions.MARK_ARCHIVE_READ,
                                box.id().toString());
                    } else {
                        playClick();
                    }
                    return true;
                }
                case ACTION -> {
                    send(box.pageId(), box.id(), box.payload());
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void layout() {
        int margin = width < 320 ? 4 : 8;
        panelX = margin;
        panelY = margin;
        panelW = Math.max(1, width - margin * 2);
        panelH = Math.max(1, height - margin * 2);
        sidebarX = panelX + 6;
        sidebarY = panelY + 28;

        if (panelW < 260) {
            sidebarW = Math.max(1, panelW - 12);
            sidebarH = Math.min(78, Math.max(48, panelH / 3));
            contentX = sidebarX;
            contentY = sidebarY + sidebarH + 6;
            contentW = sidebarW;
            contentH = Math.max(1, panelY + panelH - 6 - contentY);
            return;
        }

        sidebarW = Math.min(panelW / 3, panelW < 360 ? 92 : 126);
        sidebarH = Math.max(1, panelH - 38);
        contentX = sidebarX + sidebarW + 6;
        contentY = sidebarY;
        contentW = Math.max(1, panelX + panelW - 6 - contentX);
        contentH = sidebarH;
    }

    private void drawFrame(GuiGraphicsExtractor graphics, List<TerminalChapter> chapters) {
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 21, 0xFF0B1A22);
        graphics.outline(panelX, panelY, panelW, panelH, 0x884DBAF4);
        graphics.fill(panelX, panelY, panelX + Math.max(34, panelW / 5), panelY + 2, CYAN);
        graphics.fill(panelX, panelY + panelH - 2, panelX + Math.max(28, panelW / 7), panelY + panelH, CYAN);
        graphics.text(font, "SIGNALOS", panelX + 8, panelY + 7, TEXT, false);
        String meta = Minecraft.getInstance().player == null ? "LINK OFFLINE" : "LINK ONLINE";
        TerminalChapter chapter = activeChapter(chapters);
        if (chapter != null) {
            meta += "  |  " + chapter.title().toUpperCase(Locale.ROOT);
        }
        int pendingRewards = SignalOsClientState.pendingRewardCount();
        if (pendingRewards > 0) {
            meta += "  |  " + pendingRewards + " REWARD(S)";
        }
        graphics.text(font, trim(meta, panelW - 86), panelX + 80, panelY + 7, MUTED, false);
    }

    private void drawSidebar(GuiGraphicsExtractor graphics, List<TerminalChapter> chapters, int mouseX, int mouseY) {
        graphics.fill(sidebarX, sidebarY, sidebarX + sidebarW, sidebarY + sidebarH, PANEL_ALT);
        graphics.outline(sidebarX, sidebarY, sidebarW, sidebarH, 0x5538DFF4);
        graphics.text(font, "CHAPTERS", sidebarX + 7, sidebarY + 7, CYAN, false);
        int y = sidebarY + 20;
        String lastSection = "";
        for (TerminalChapter chapter : chapters) {
            if (y + 24 > sidebarY + sidebarH - 4) {
                break;
            }
            if (!chapter.section().equals(lastSection) && sidebarW > 96) {
                lastSection = chapter.section();
                graphics.text(font, lastSection.toUpperCase(Locale.ROOT), sidebarX + 7, y, MUTED, false);
                y += 11;
            }
            boolean selected = chapter.id().equals(selectedChapterId);
            boolean hovered = inside(mouseX, mouseY, sidebarX + 5, y, sidebarW - 10, 22);
            int accent = chapter.accentColor();
            graphics.fill(sidebarX + 5, y, sidebarX + sidebarW - 5, y + 22, selected ? 0xD0152C38 : hovered ? ROW_HOVER : ROW);
            graphics.outline(sidebarX + 5, y, sidebarW - 10, 22, selected ? accent : 0x3338DFF4);
            graphics.fill(sidebarX + 5, y, sidebarX + 8, y + 22, selected ? accent : 0x7738DFF4);
            graphics.text(font, trim(chapter.title(), sidebarW - 22), sidebarX + 12, y + 7,
                    selected ? TEXT : MUTED, false);
            hitBoxes.add(new HitBox(HitKind.CHAPTER, chapter.id(), null, "", sidebarX + 5, y, sidebarW - 10, 22));
            y += 25;
        }
    }

    private void drawPageTabs(GuiGraphicsExtractor graphics, TerminalChapter chapter, int mouseX, int mouseY) {
        if (chapter == null) {
            return;
        }
        int x = contentX;
        int y = contentY;
        int gap = 4;
        List<TerminalPage> pages = SignalOsContentRegistry.pagesFor(chapter.id());
        int pageCount = Math.max(1, pages.size());
        int tabW = Math.max(44, Math.min(78, (contentW - gap * (pageCount - 1)) / pageCount));
        for (TerminalPage page : pages) {
            if (x + tabW > contentX + contentW) {
                break;
            }
            boolean selected = page.type().equals(selectedPage);
            boolean hovered = inside(mouseX, mouseY, x, y, tabW, 18);
            graphics.fill(x, y, x + tabW, y + 18, selected ? 0xD0152B38 : hovered ? ROW_HOVER : ROW);
            graphics.outline(x, y, tabW, 18, selected ? chapter.accentColor() : 0x3338DFF4);
            graphics.fill(x, y + 16, x + tabW, y + 18, selected ? chapter.accentColor() : 0x5538DFF4);
            graphics.centeredText(font, trim(page.title().toUpperCase(Locale.ROOT), tabW - 8), x + tabW / 2, y + 6,
                    selected ? TEXT : MUTED);
            hitBoxes.add(new HitBox(HitKind.PAGE, page.id(), null, page.type(), x, y, tabW, 18));
            x += tabW + gap;
        }
    }

    private void drawPage(GuiGraphicsExtractor graphics, TerminalChapter chapter, int mouseX, int mouseY) {
        int bodyY = contentY + 24;
        int bodyH = Math.max(1, contentH - 24);
        graphics.fill(contentX, bodyY, contentX + contentW, bodyY + bodyH, PANEL_ALT);
        graphics.outline(contentX, bodyY, contentW, bodyH, 0x4438DFF4);
        if (chapter == null) {
            graphics.text(font, "NO SIGNALOS CONTENT REGISTERED", contentX + 9, bodyY + 9, WARN, false);
            return;
        }
        switch (selectedPage) {
            case "missions" -> drawMissions(graphics, chapter, bodyY, bodyH, mouseX, mouseY);
            case "archives" -> drawArchives(graphics, chapter, bodyY, bodyH, mouseX, mouseY);
            case "rewards", "reward_inbox" -> drawRewards(graphics, bodyY, bodyH, mouseX, mouseY);
            case "diagnostics" -> drawDiagnostics(graphics, bodyY, bodyH, mouseX, mouseY);
            default -> drawUnknownPage(graphics, chapter, bodyY, bodyH);
        }
    }

    private void drawMissions(GuiGraphicsExtractor graphics, TerminalChapter chapter, int bodyY, int bodyH,
            int mouseX, int mouseY) {
        List<TerminalMission> missions = SignalOsContentRegistry.missionsFor(chapter.id());
        if (missions.isEmpty()) {
            sectionHeader(graphics, "MISSIONS", "No mission uplink records for this chapter.", contentX + 9, bodyY + 8,
                    contentW - 18, chapter.accentColor());
            return;
        }
        if (selectedMissionId == null || missions.stream().noneMatch(mission -> mission.id().equals(selectedMissionId))) {
            selectedMissionId = missions.getFirst().id();
        }
        boolean split = contentW >= 245;
        int listW = split ? Math.min(138, contentW / 2) : contentW - 18;
        int listX = contentX + 8;
        int listY = bodyY + 8;
        sectionHeader(graphics, "MISSIONS", missions.size() + " active", listX, listY, listW, chapter.accentColor());
        int y = listY + 23;
        for (TerminalMission mission : missions) {
            if (y + 27 > bodyY + bodyH - 6) {
                break;
            }
            boolean selected = mission.id().equals(selectedMissionId);
            boolean hovered = inside(mouseX, mouseY, listX, y, listW, 25);
            graphics.fill(listX, y, listX + listW, y + 25, selected ? 0xD0152B38 : hovered ? ROW_HOVER : ROW);
            graphics.outline(listX, y, listW, 25, selected ? chapter.accentColor() : 0x3338DFF4);
            graphics.fill(listX, y, listX + 3, y + 25, selected ? chapter.accentColor() : 0x5538DFF4);
            ItemStack icon = itemStack(mission.iconItem(), Items.PAPER);
            graphics.item(icon, listX + 7, y + 5);
            graphics.text(font, trim(mission.title(), listW - 32), listX + 28, y + 5, selected ? TEXT : MUTED, false);
            graphics.text(font, trim(missionStateLabel(mission), listW - 32), listX + 28, y + 15,
                    missionStateColor(mission), false);
            hitBoxes.add(new HitBox(HitKind.MISSION, mission.id(), null, "", listX, y, listW, 25));
            y += 28;
        }

        TerminalMission mission = SignalOsContentRegistry.mission(selectedMissionId);
        int detailX = split ? listX + listW + 8 : listX;
        int detailY = split ? bodyY + 8 : Math.min(bodyY + bodyH - 90, y + 6);
        int detailW = split ? contentX + contentW - detailX - 8 : listW;
        int detailH = Math.max(50, bodyY + bodyH - detailY - 8);
        drawMissionDetail(graphics, mission, detailX, detailY, detailW, detailH, chapter.accentColor(), mouseX, mouseY);
    }

    private void drawMissionDetail(GuiGraphicsExtractor graphics, TerminalMission mission, int x, int y, int w, int h,
            int accent, int mouseX, int mouseY) {
        graphics.fill(x, y, x + w, y + h, 0xAA071017);
        graphics.outline(x, y, w, h, 0x5538DFF4);
        if (mission == null) {
            graphics.text(font, "SELECT A MISSION", x + 8, y + 8, MUTED, false);
            return;
        }
        graphics.fill(x, y, x + w, y + 2, accent);
        graphics.text(font, trim(mission.title().toUpperCase(Locale.ROOT), w - 16), x + 8, y + 8, TEXT, false);
        int cursor = y + 22;
        graphics.text(font, "Status: " + missionStateLabel(mission), x + 8, cursor, missionStateColor(mission), false);
        cursor += 12;
        cursor = drawWrapped(graphics, mission.description(), x + 8, cursor, w - 16, MUTED, 3) + 5;
        for (String objective : mission.objectives()) {
            if (cursor + 10 > y + h - 25) {
                break;
            }
            graphics.fill(x + 8, cursor + 3, x + 13, cursor + 8, accent);
            graphics.text(font, trim(objective, w - 28), x + 18, cursor + 2, TEXT, false);
            cursor += 12;
        }
        if (!mission.rewards().isEmpty() && cursor + 12 < y + h - 25) {
            String rewardLine = mission.rewards().stream()
                    .map(reward -> reward.displayLabel() + " x" + reward.count())
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
            graphics.text(font, trim("Rewards: " + rewardLine, w - 16), x + 8, cursor + 3, WARN, false);
        }
        if (mission.rewardClaim()) {
            int buttonW = Math.min(104, w - 16);
            int buttonX = x + w - buttonW - 8;
            int buttonY = y + h - 20;
            boolean completed = SignalOsClientState.isMissionCompleted(mission.id());
            boolean claimed = SignalOsClientState.isMissionClaimed(mission.id());
            boolean hovered = inside(mouseX, mouseY, buttonX, buttonY, buttonW, 14);
            int border = claimed ? GREEN : completed ? accent : 0x5538DFF4;
            String label = claimed ? "CLAIMED" : completed ? "CLAIM REWARD" : "LOCKED";
            graphics.fill(buttonX, buttonY, buttonX + buttonW, buttonY + 14,
                    hovered && completed && !claimed ? 0xFF183743 : 0xFF112430);
            graphics.outline(buttonX, buttonY, buttonW, 14, border);
            graphics.centeredText(font, label, buttonX + buttonW / 2, buttonY + 4,
                    completed || claimed ? TEXT : MUTED);
            if (completed && !claimed) {
                hitBoxes.add(new HitBox(HitKind.ACTION, SignalOsBuiltinActions.CLAIM_MISSION,
                        SignalOsBuiltinActions.PAGE_MISSIONS, mission.id().toString(), buttonX, buttonY, buttonW, 14));
            }
        }
    }

    private void drawArchives(GuiGraphicsExtractor graphics, TerminalChapter chapter, int bodyY, int bodyH,
            int mouseX, int mouseY) {
        List<TerminalArchiveRecord> records = SignalOsContentRegistry.archivesFor(chapter.id());
        if (records.isEmpty()) {
            sectionHeader(graphics, "ARCHIVES", "No archive records for this chapter.", contentX + 9, bodyY + 8,
                    contentW - 18, chapter.accentColor());
            return;
        }
        if (selectedArchiveId == null || records.stream().noneMatch(record -> record.id().equals(selectedArchiveId))) {
            selectedArchiveId = records.getFirst().id();
        }
        boolean split = contentW >= 245;
        int listW = split ? Math.min(138, contentW / 2) : contentW - 18;
        int listX = contentX + 8;
        int listY = bodyY + 8;
        sectionHeader(graphics, "ARCHIVES", records.size() + " records", listX, listY, listW, chapter.accentColor());
        int y = listY + 23;
        for (TerminalArchiveRecord record : records) {
            if (y + 27 > bodyY + bodyH - 6) {
                break;
            }
            boolean selected = record.id().equals(selectedArchiveId);
            boolean read = SignalOsClientState.isArchiveRead(record.id());
            boolean hovered = inside(mouseX, mouseY, listX, y, listW, 25);
            graphics.fill(listX, y, listX + listW, y + 25, selected ? 0xD0152B38 : hovered ? ROW_HOVER : ROW);
            graphics.outline(listX, y, listW, 25, selected ? chapter.accentColor() : 0x3338DFF4);
            graphics.fill(listX, y, listX + 3, y + 25, read ? GREEN : chapter.accentColor());
            graphics.text(font, trim(record.title(), listW - 16), listX + 9, y + 5, selected ? TEXT : MUTED, false);
            graphics.text(font, trim(archiveStateLabel(record), listW - 16), listX + 9, y + 15,
                    record.locked() ? RED : read ? GREEN : 0xFF5D7784, false);
            hitBoxes.add(new HitBox(HitKind.ARCHIVE, record.id(), null, "", listX, y, listW, 25));
            y += 28;
        }

        TerminalArchiveRecord record = records.stream()
                .filter(candidate -> candidate.id().equals(selectedArchiveId))
                .findFirst()
                .orElse(records.getFirst());
        int detailX = split ? listX + listW + 8 : listX;
        int detailY = split ? bodyY + 8 : Math.min(bodyY + bodyH - 90, y + 6);
        int detailW = split ? contentX + contentW - detailX - 8 : listW;
        int detailH = Math.max(50, bodyY + bodyH - detailY - 8);
        graphics.fill(detailX, detailY, detailX + detailW, detailY + detailH, 0xAA071017);
        graphics.outline(detailX, detailY, detailW, detailH, 0x5538DFF4);
        graphics.fill(detailX, detailY, detailX + detailW, detailY + 2, chapter.accentColor());
        graphics.text(font, trim(record.title().toUpperCase(Locale.ROOT), detailW - 16), detailX + 8, detailY + 8,
                record.locked() ? RED : TEXT, false);
        int cursor = detailY + 23;
        if (record.locked()) {
            graphics.text(font, "RECORD LOCKED", detailX + 8, cursor, RED, false);
            return;
        }
        graphics.text(font, "Status: " + archiveStateLabel(record), detailX + 8, cursor,
                SignalOsClientState.isArchiveRead(record.id()) ? GREEN : MUTED, false);
        cursor += 13;
        for (String line : record.lines()) {
            if (cursor > detailY + detailH - 12) {
                break;
            }
            cursor = drawWrapped(graphics, line, detailX + 8, cursor, detailW - 16, MUTED, 3) + 4;
        }
    }

    private void drawRewards(GuiGraphicsExtractor graphics, int bodyY, int bodyH, int mouseX, int mouseY) {
        int pending = SignalOsClientState.pendingRewardCount();
        sectionHeader(graphics, "REWARD INBOX", pending + " pending item(s)", contentX + 9, bodyY + 8,
                contentW - 18, CYAN);
        int x = contentX + 12;
        int y = bodyY + 38;
        drawWrapped(graphics,
                pending == 0
                        ? "No stored rewards are waiting in the linked SignalOS terminal."
                        : "Stored mission rewards are available. Claiming pulls every cached stack into your inventory.",
                x, y, contentW - 24, MUTED, 5);
        int buttonW = Math.min(122, contentW - 24);
        int buttonY = bodyY + bodyH - 24;
        boolean hovered = inside(mouseX, mouseY, x, buttonY, buttonW, 16);
        graphics.fill(x, buttonY, x + buttonW, buttonY + 16, hovered && pending > 0 ? 0xFF183743 : 0xFF112430);
        graphics.outline(x, buttonY, buttonW, 16, pending > 0 ? GREEN : 0x5538DFF4);
        graphics.centeredText(font, pending > 0 ? "CLAIM ALL" : "INBOX EMPTY", x + buttonW / 2, buttonY + 5,
                pending > 0 ? TEXT : MUTED);
        if (pending > 0) {
            hitBoxes.add(new HitBox(HitKind.ACTION, SignalOsBuiltinActions.CLAIM_REWARDS,
                    SignalOsBuiltinActions.PAGE_REWARDS, "", x, buttonY, buttonW, 16));
        }
    }

    private void drawDiagnostics(GuiGraphicsExtractor graphics, int bodyY, int bodyH, int mouseX, int mouseY) {
        List<TerminalDiagnosticProvider.Diagnostic> diagnostics =
                SignalOsContentRegistry.diagnostics(Minecraft.getInstance().player);
        sectionHeader(graphics, "DIAGNOSTICS", diagnostics.size() + " system report(s)", contentX + 9, bodyY + 8,
                contentW - 18, CYAN);
        int y = bodyY + 34;
        if (diagnostics.isEmpty()) {
            graphics.text(font, "No diagnostic providers registered.", contentX + 12, y, MUTED, false);
            return;
        }
        for (TerminalDiagnosticProvider.Diagnostic diagnostic : diagnostics) {
            if (y + 32 > bodyY + bodyH - 6) {
                break;
            }
            int color = severityColor(diagnostic.severity());
            graphics.fill(contentX + 8, y, contentX + contentW - 8, y + 30, ROW);
            graphics.outline(contentX + 8, y, contentW - 16, 30, 0x3338DFF4);
            graphics.fill(contentX + 8, y, contentX + 11, y + 30, color);
            graphics.text(font, trim(severityLabel(diagnostic.severity()) + " | " + diagnostic.title(),
                    contentW - 30), contentX + 17, y + 6, TEXT, false);
            graphics.text(font, trim(diagnostic.detail(), contentW - 30), contentX + 17, y + 18, MUTED, false);
            y += 33;
        }
    }

    private void drawUnknownPage(GuiGraphicsExtractor graphics, TerminalChapter chapter, int bodyY, int bodyH) {
        sectionHeader(graphics, selectedPage.toUpperCase(Locale.ROOT), "Unsupported page type", contentX + 9,
                bodyY + 8, contentW - 18, chapter.accentColor());
        drawWrapped(graphics, "This page is registered as content metadata, but SignalOS has no built-in renderer for this page type.",
                contentX + 12, bodyY + 36, contentW - 24, MUTED, 4);
    }

    private void sectionHeader(GuiGraphicsExtractor graphics, String title, String subtitle, int x, int y, int w,
            int accent) {
        graphics.fill(x, y, x + w, y + 18, 0xAA071017);
        graphics.outline(x, y, w, 18, 0x3338DFF4);
        graphics.fill(x, y + 16, x + Math.max(28, w / 3), y + 18, accent);
        if (w < 118) {
            graphics.text(font, trim(title, w - 12), x + 6, y + 5, TEXT, false);
            return;
        }
        graphics.text(font, trim(title, w / 2 - 10), x + 6, y + 5, TEXT, false);
        graphics.text(font, trim(subtitle, w / 2 - 12), x + w / 2, y + 5, MUTED, false);
    }

    private int drawWrapped(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color,
            int maxLines) {
        if (text == null || text.isBlank() || maxWidth <= 0 || maxLines <= 0) {
            return y;
        }
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int drawn = 0;
        int cursor = y;
        for (String word : words) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (font.width(next) > maxWidth && !line.isEmpty()) {
                graphics.text(font, trim(line.toString(), maxWidth), x, cursor, color, false);
                cursor += 10;
                drawn++;
                line.setLength(0);
                if (drawn >= maxLines) {
                    return cursor;
                }
            }
            if (!line.isEmpty()) {
                line.append(' ');
            }
            line.append(word);
        }
        if (!line.isEmpty() && drawn < maxLines) {
            graphics.text(font, trim(line.toString(), maxWidth), x, cursor, color, false);
            cursor += 10;
        }
        return cursor;
    }

    private boolean cyclePage(int direction) {
        List<TerminalChapter> chapters = SignalOsContentRegistry.chapters();
        TerminalChapter chapter = activeChapter(chapters);
        if (chapter == null) {
            return false;
        }
        List<TerminalPage> pages = SignalOsContentRegistry.pagesFor(chapter.id());
        if (pages.isEmpty()) {
            return false;
        }
        List<String> pageTypes = pages.stream().map(TerminalPage::type).toList();
        int index = pageTypes.indexOf(selectedPage);
        if (index < 0) {
            index = direction > 0 ? -1 : 0;
        }
        selectedPage = pageTypes.get(Math.floorMod(index + direction, pageTypes.size()));
        selectedMissionId = null;
        selectedArchiveId = null;
        playClick();
        return true;
    }

    private boolean cycleChapter(int direction) {
        List<TerminalChapter> chapters = SignalOsContentRegistry.chapters();
        if (chapters.isEmpty()) {
            return false;
        }
        normalizeSelection(chapters);
        int index = 0;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).id().equals(selectedChapterId)) {
                index = i;
                break;
            }
        }
        selectedChapterId = chapters.get(Math.floorMod(index + direction, chapters.size())).id();
        selectedMissionId = null;
        selectedArchiveId = null;
        normalizeSelection(chapters);
        playClick();
        return true;
    }

    private void normalizeSelection(List<TerminalChapter> chapters) {
        if (chapters.isEmpty()) {
            selectedChapterId = null;
            return;
        }
        if (selectedChapterId == null || chapters.stream().noneMatch(chapter -> chapter.id().equals(selectedChapterId))) {
            selectedChapterId = chapters.getFirst().id();
        }
        TerminalChapter chapter = activeChapter(chapters);
        if (chapter != null) {
            List<TerminalPage> pages = SignalOsContentRegistry.pagesFor(chapter.id());
            if (!pages.isEmpty() && pages.stream().noneMatch(page -> page.type().equals(selectedPage))) {
                selectedPage = pages.getFirst().type();
            }
        }
    }

    private TerminalChapter activeChapter(List<TerminalChapter> chapters) {
        if (chapters == null || chapters.isEmpty()) {
            return null;
        }
        return chapters.stream()
                .filter(chapter -> chapter.id().equals(selectedChapterId))
                .findFirst()
                .orElse(chapters.getFirst());
    }

    private void send(Identifier pageId, Identifier actionId, String payload) {
        playClick();
        ClientPacketDistributor.sendToServer(new SignalOsActionPacket(pageId, actionId, payload == null ? "" : payload));
    }

    private void playClick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private ItemStack itemStack(Identifier id, Item fallback) {
        Item item = id == null ? fallback : BuiltInRegistries.ITEM.getOptional(id).orElse(fallback);
        return new ItemStack(item == null ? Items.PAPER : item);
    }

    private int severityColor(TerminalDiagnosticProvider.Severity severity) {
        return switch (severity == null ? TerminalDiagnosticProvider.Severity.INFO : severity) {
            case CRITICAL -> RED;
            case BLOCKED -> WARN;
            case WARNING -> 0xFFFFB454;
            case INFO -> CYAN;
        };
    }

    private String severityLabel(TerminalDiagnosticProvider.Severity severity) {
        return (severity == null ? TerminalDiagnosticProvider.Severity.INFO : severity).name();
    }

    private String missionStateLabel(TerminalMission mission) {
        if (mission == null) {
            return "UNKNOWN";
        }
        if (SignalOsClientState.isMissionClaimed(mission.id())) {
            return "CLAIMED";
        }
        if (SignalOsClientState.isMissionCompleted(mission.id()) && mission.rewardClaim()
                && !mission.rewards().isEmpty()) {
            return "READY TO CLAIM";
        }
        if (SignalOsClientState.isMissionCompleted(mission.id())) {
            return "COMPLETE";
        }
        return "IN PROGRESS";
    }

    private int missionStateColor(TerminalMission mission) {
        if (mission == null) {
            return MUTED;
        }
        if (SignalOsClientState.isMissionClaimed(mission.id())) {
            return GREEN;
        }
        if (SignalOsClientState.isMissionCompleted(mission.id())) {
            return WARN;
        }
        return 0xFF5D7784;
    }

    private String archiveStateLabel(TerminalArchiveRecord record) {
        if (record == null) {
            return "UNKNOWN";
        }
        if (record.locked()) {
            return "LOCKED";
        }
        String status = record.status() == null || record.status().isBlank() ? "OPEN" : record.status();
        return (SignalOsClientState.isArchiveRead(record.id()) ? "READ" : "UNREAD") + " | " + status;
    }

    private String trim(String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int allowed = Math.max(1, maxWidth - font.width(ellipsis));
        String value = text;
        while (!value.isEmpty() && font.width(value) > allowed) {
            value = value.substring(0, value.length() - 1);
        }
        return value + ellipsis;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private record HitBox(HitKind kind, Identifier id, Identifier pageId, String payload, int x, int y, int w, int h) {
        boolean inside(double mouseX, double mouseY) {
            return SignalOsTerminalScreen.inside(mouseX, mouseY, x, y, w, h);
        }
    }

    private enum HitKind {
        CHAPTER,
        PAGE,
        MISSION,
        ARCHIVE,
        ACTION
    }
}
