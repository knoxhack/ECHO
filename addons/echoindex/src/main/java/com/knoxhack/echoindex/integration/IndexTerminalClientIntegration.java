package com.knoxhack.echoindex.integration;

import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echoindex.EchoIndex;
import com.knoxhack.echoindex.IndexIds;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echoterminal.api.TerminalNavigationProfile;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import org.lwjgl.glfw.GLFW;

public final class IndexTerminalClientIntegration {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final int ACCENT = 0xFF66E8FF;
    public static final Identifier TAB_ID = EchoIndex.id("terminal/index");

    private IndexTerminalClientIntegration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalTab tab = new IndexArchiveTab();
        TerminalTabRegistry.register(tab);
        TerminalNavigationProfiles.register(tab.descriptor().id(), TerminalNavigationProfile.intel(146));
        TerminalRecipeRegistry.register(IndexTerminalRecipeProvider.INSTANCE);
    }

    private static final class IndexArchiveTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor = new TerminalTabDescriptor(TAB_ID, "INDEX", 146, ACCENT);
        private final TerminalTabChrome chrome = TerminalTabChrome.of(
                "Index", TerminalTabChrome.GROUP_FIELD, "IX", "Shared codex and recipe archive", 146);
        private final List<Hit> hits = new ArrayList<>();
        private Identifier selectedCategory = IndexIds.CATEGORY_TUTORIALS;
        private Identifier selectedEntry = IndexIds.ENTRY_OVERVIEW;
        private String query = "";

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                float partialTick) {
            hits.clear();
            List<IndexCategory> categories = IndexService.INSTANCE.categories(context.player());
            List<IndexEntry> entries = visibleEntries(context);
            ensureSelection(categories, entries);

            int x = context.contentX() + 12;
            int y = context.contentY() + 10 - context.scrollY();
            int w = context.contentWidth() - 24;
            y = TerminalUi.sectionHeader(context, graphics, "ECHO: INDEX", "TERMINAL // CODEX", x, y, w, ACCENT);

            int searchH = 32;
            TerminalUi.flatHudPanel(context, graphics, x, y, w, searchH, ACCENT);
            TerminalUi.line(context, graphics, query.isBlank() ? "Search ECHO: Index..." : query,
                    x + 12, y + 11, w - 270, query.isBlank() ? TerminalUi.muted(context) : TerminalUi.text(context));
            chip(context, graphics, x + w - 248, y + 8, 78, "ALL", true, mouseX, mouseY, HitKind.FILTER, null);
            chip(context, graphics, x + w - 164, y + 8, 72, entries.size() + " ENTRIES", false, mouseX, mouseY,
                    HitKind.NONE, null);
            chip(context, graphics, x + w - 86, y + 8, 66,
                    TerminalRecipeRegistry.snapshot(context.player()).recipes().size() + " REC", false,
                    mouseX, mouseY, HitKind.NONE, null);

            int bodyY = y + searchH + 10;
            int bodyH = Math.max(260, context.contentHeight() - 78);
            int categoryW = Math.min(170, Math.max(132, w / 5));
            int entryW = Math.min(230, Math.max(176, w / 4));
            int detailW = Math.max(240, w - categoryW - entryW - 24);

            drawCategories(context, graphics, categories, x, bodyY, categoryW, bodyH, mouseX, mouseY);
            drawEntries(context, graphics, entries, x + categoryW + 10, bodyY, entryW, bodyH, mouseX, mouseY);
            drawDetail(context, graphics, selected(entries), x + categoryW + entryW + 20, bodyY, detailW, bodyH,
                    mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            for (Hit hit : hits) {
                if (!TerminalUi.inside(mouseX, mouseY, hit.x(), hit.y(), hit.w(), hit.h())) {
                    continue;
                }
                if (hit.kind() == HitKind.CATEGORY && hit.id() != null) {
                    selectedCategory = hit.id();
                    selectedEntry = null;
                    context.playCommandSound();
                    return true;
                }
                if (hit.kind() == HitKind.ENTRY && hit.id() != null) {
                    selectedEntry = hit.id();
                    context.playCommandSound();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
            int key = event.key();
            if (key == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
                query = query.substring(0, query.length() - 1);
                selectedEntry = null;
                return true;
            }
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) {
                if (!query.isBlank()) {
                    query = "";
                    selectedEntry = null;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean charTyped(TerminalRenderContext context, CharacterEvent event) {
            if (!event.isAllowedChatCharacter() || query.length() >= 48) {
                return false;
            }
            query += event.codepointAsString();
            selectedEntry = null;
            return true;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return 520;
        }

        private void drawCategories(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                List<IndexCategory> categories, int x, int y, int w, int h, int mouseX, int mouseY) {
            TerminalUi.flatHudPanel(context, graphics, x, y, w, h, ACCENT);
            TerminalUi.line(context, graphics, "CATEGORIES", x + 10, y + 10, w - 20, TerminalUi.accent(context));
            int cy = y + 30;
            for (IndexCategory category : categories.stream().limit(12).toList()) {
                boolean selected = category.id().equals(selectedCategory);
                boolean hover = TerminalUi.inside(mouseX, mouseY, x + 8, cy, w - 16, 30);
                graphics.fill(x + 8, cy, x + w - 8, cy + 30, selected ? 0xFF123241 : hover ? 0xAA102630 : 0x66071117);
                graphics.outline(x + 8, cy, w - 16, 30, selected ? ACCENT : 0x4438DFF4);
                graphics.item(category.icon(), x + 14, cy + 7);
                TerminalUi.line(context, graphics, title(category.titleKey()), x + 36, cy + 6, w - 46,
                        selected ? TerminalUi.text(context) : TerminalUi.muted(context));
                hits.add(new Hit(x + 8, cy, w - 16, 30, HitKind.CATEGORY, category.id()));
                cy += 35;
                if (cy > y + h - 28) {
                    break;
                }
            }
        }

        private void drawEntries(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                List<IndexEntry> entries, int x, int y, int w, int h, int mouseX, int mouseY) {
            TerminalUi.flatHudPanel(context, graphics, x, y, w, h, ACCENT);
            TerminalUi.line(context, graphics, "ENTRIES", x + 10, y + 10, w - 20, TerminalUi.accent(context));
            int cy = y + 30;
            for (IndexEntry entry : entries.stream().limit(11).toList()) {
                boolean selected = entry.id().equals(selectedEntry);
                boolean hover = TerminalUi.inside(mouseX, mouseY, x + 8, cy, w - 16, 36);
                graphics.fill(x + 8, cy, x + w - 8, cy + 36, selected ? 0xFF123241 : hover ? 0xAA102630 : 0x66071117);
                graphics.outline(x + 8, cy, w - 16, 36, selected ? ACCENT : 0x4438DFF4);
                graphics.item(entry.icon(), x + 14, cy + 10);
                TerminalUi.line(context, graphics, title(entry.titleKey()), x + 36, cy + 6, w - 46,
                        selected ? TerminalUi.text(context) : TerminalUi.text(context));
                TerminalUi.line(context, graphics, summary(entry.summaryKey()), x + 36, cy + 19, w - 46,
                        TerminalUi.muted(context));
                hits.add(new Hit(x + 8, cy, w - 16, 36, HitKind.ENTRY, entry.id()));
                cy += 40;
                if (cy > y + h - 34) {
                    break;
                }
            }
        }

        private void drawDetail(TerminalRenderContext context, GuiGraphicsExtractor graphics, IndexEntry entry,
                int x, int y, int w, int h, int mouseX, int mouseY) {
            TerminalUi.flatHudPanel(context, graphics, x, y, w, h, ACCENT);
            if (entry == null) {
                TerminalUi.wrap(context, graphics, "No Index entry matches the current filters.",
                        x + 16, y + 26, w - 32, TerminalUi.muted(context));
                return;
            }
            graphics.item(entry.icon(), x + 16, y + 18);
            TerminalUi.line(context, graphics, title(entry.titleKey()), x + 42, y + 14, w - 58,
                    TerminalUi.accent(context));
            TerminalUi.line(context, graphics, entry.sourceModId().toUpperCase(Locale.ROOT),
                    x + 42, y + 28, w - 58, TerminalUi.muted(context));
            int cy = y + 56;
            cy = drawRenderCorePreview(context, graphics, entry, x + 14, cy, w - 28, mouseX, mouseY);
            cy = TerminalUi.sectionHeader(context, graphics, "SUMMARY", entry.defaultState().name(),
                    x + 14, cy, w - 28, ACCENT);
            TerminalUi.wrap(context, graphics, summary(entry.summaryKey()), x + 14, cy, w - 28, TerminalUi.text(context));
            cy += Math.max(34, TerminalUi.wrappedHeight(context, summary(entry.summaryKey()), w - 28)) + 12;
            cy = TerminalUi.sectionHeader(context, graphics, "ARCHIVE", entry.categoryId().toString(),
                    x + 14, cy, w - 28, ACCENT);
            TerminalUi.wrap(context, graphics, body(entry.bodyKey()), x + 14, cy, w - 28, TerminalUi.text(context));
            cy += Math.max(54, TerminalUi.wrappedHeight(context, body(entry.bodyKey()), w - 28)) + 12;
            TerminalUi.sectionHeader(context, graphics, "LINKS",
                    entry.relatedEntries().size() + " related / " + entry.linkedItems().size() + " items",
                    x + 14, cy, w - 28, ACCENT);
        }

        private List<IndexEntry> visibleEntries(TerminalRenderContext context) {
            String lower = query.toLowerCase(Locale.ROOT);
            return IndexService.INSTANCE.entries(context.player()).stream()
                    .filter(entry -> selectedCategory == null || selectedCategory.equals(entry.categoryId()) || !query.isBlank())
                    .filter(entry -> lower.isBlank() || matches(entry, lower))
                    .sorted(Comparator.comparingInt(IndexEntry::sortOrder).thenComparing(entry -> entry.id().toString()))
                    .toList();
        }

        private void ensureSelection(List<IndexCategory> categories, List<IndexEntry> entries) {
            if (selectedCategory == null && !categories.isEmpty()) {
                selectedCategory = categories.getFirst().id();
            }
            if (selectedEntry == null || entries.stream().noneMatch(entry -> entry.id().equals(selectedEntry))) {
                selectedEntry = entries.isEmpty() ? null : entries.getFirst().id();
            }
        }

        private IndexEntry selected(List<IndexEntry> entries) {
            return entries.stream().filter(entry -> entry.id().equals(selectedEntry)).findFirst()
                    .orElse(entries.isEmpty() ? null : entries.getFirst());
        }

        private boolean matches(IndexEntry entry, String lower) {
            return entry.id().toString().toLowerCase(Locale.ROOT).contains(lower)
                    || title(entry.titleKey()).toLowerCase(Locale.ROOT).contains(lower)
                    || summary(entry.summaryKey()).toLowerCase(Locale.ROOT).contains(lower)
                    || entry.tags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(lower));
        }

        private void chip(TerminalRenderContext context, GuiGraphicsExtractor graphics, int x, int y, int w,
                String label, boolean active, int mouseX, int mouseY, HitKind kind, Identifier id) {
            boolean hover = TerminalUi.inside(mouseX, mouseY, x, y, w, 16);
            TerminalUi.compactButton(context, graphics, x, y, w, label, active ? ACCENT : TerminalUi.muted(context),
                    kind != HitKind.NONE, hover);
            if (kind != HitKind.NONE) {
                hits.add(new Hit(x, y, w, 16, kind, id));
            }
        }

        private String title(String key) {
            return Component.translatable(key).getString();
        }

        private String summary(String key) {
            return Component.translatable(key).getString();
        }

        private String body(String key) {
            return Component.translatable(key).getString();
        }

        private int drawRenderCorePreview(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                IndexEntry entry, int x, int y, int w, int mouseX, int mouseY) {
            if (!ModList.get().isLoaded("echorendercore")) {
                return y;
            }
            try {
                Object result = Class.forName("com.knoxhack.echoindex.integration.IndexRenderCorePreviewBridge")
                        .getMethod("drawPreview", TerminalRenderContext.class, GuiGraphicsExtractor.class,
                                IndexEntry.class, int.class, int.class, int.class, int.class, int.class)
                        .invoke(null, context, graphics, entry, x, y, w, mouseX, mouseY);
                return result instanceof Integer nextY ? nextY : y;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                EchoIndex.LOGGER.debug("RenderCore Index preview unavailable.", exception);
                return y;
            }
        }
    }

    private enum HitKind {
        NONE,
        FILTER,
        CATEGORY,
        ENTRY
    }

    private record Hit(int x, int y, int w, int h, HitKind kind, Identifier id) {
    }
}
