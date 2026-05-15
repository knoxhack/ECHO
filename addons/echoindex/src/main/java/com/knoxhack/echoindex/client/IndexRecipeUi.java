package com.knoxhack.echoindex.client;

import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoindex.Config;
import com.knoxhack.echoindex.network.IndexRecipeQueryPacket;
import com.knoxhack.echoindex.service.IndexIngredientNeed;
import com.knoxhack.echoindex.service.IndexRecipeActionState;
import com.knoxhack.echoindex.service.IndexRecipeDisplayMetadata;
import com.knoxhack.echoindex.service.IndexRecipeLayoutType;
import com.knoxhack.echoindex.service.IndexRecipePlan;
import com.knoxhack.echoindex.service.IndexRecipePlanner;
import com.knoxhack.echoindex.service.IndexRecipeQueryClientState;
import com.knoxhack.echoindex.service.IndexRecipeSourceKind;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class IndexRecipeUi {
    public static final int BG = 0xF2060D13;
    public static final int PANEL = 0xF00B151D;
    public static final int ROW = 0xAA102630;
    public static final int CYAN = 0xFF66E8FF;
    public static final int TEXT = 0xFFE9FBFF;
    public static final int MUTED = 0xFF8CA7B5;
    public static final int WARN = 0xFFFFD166;
    public static final int RED = 0xFFFF6B6B;
    public static final int GREEN = 0xFFA8F7C5;

    private static final Map<String, Integer> CHOICE_OFFSETS = new HashMap<>();
    private static Identifier lastHoveredRecipeId;
    private static Identifier lastHoveredItemId;
    private static Identifier lastQueriedItemId;
    private static ViewMode lastViewMode = ViewMode.RECIPES;
    private static int lastSelectedCardIndex;
    private static int lastSelectedCardCount;
    private static String lastQueryCacheState = "none";
    private static Identifier currentRecipeId;
    private static int currentChoiceCell;

    private IndexRecipeUi() {
    }

    public static List<IndexRecipeView> viewsFor(Player player, Item item, ViewMode mode) {
        if (item == null) {
            return List.of();
        }
        recordQueryState(player, item, mode);
        if (clientContext(player)) {
            requestServerViews(item);
            return switch (mode) {
                case USES -> IndexRecipeQueryClientState.usesFor(item);
                case SOURCES -> IndexRecipeQueryClientState.sourcesFor(item);
                case RECIPES -> IndexRecipeQueryClientState.recipesFor(item);
            };
        }
        return switch (mode) {
            case USES -> IndexService.INSTANCE.usesFor(player, item);
            case SOURCES -> IndexService.INSTANCE.recipesFor(player, item).stream()
                    .filter(IndexRecipeUi::sourceCard)
                    .toList();
            case RECIPES -> IndexService.INSTANCE.recipesFor(player, item).stream()
                    .filter(recipe -> !sourceCard(recipe))
                    .toList();
        };
    }

    public static boolean sourceCard(IndexRecipeView recipe) {
        return IndexRecipeSourceKind.isSourceCard(recipe);
    }

    public static ItemStack recipeIcon(IndexRecipeView recipe, ItemStack fallback) {
        if (recipe == null) {
            return fallback == null ? ItemStack.EMPTY : fallback;
        }
        for (IndexRecipeSlot slot : recipe.slots()) {
            if (slot.role() == IndexSlotRole.OUTPUT && !slot.stacks().isEmpty() && !slot.stacks().getFirst().isEmpty()) {
                return slot.stacks().getFirst();
            }
        }
        return recipe.machine().isEmpty() ? fallback : recipe.machine();
    }

    public static List<ItemStack> linkedStacks(IndexRecipeView recipe) {
        List<ItemStack> stacks = new ArrayList<>();
        if (recipe == null) {
            return stacks;
        }
        for (IndexRecipeSlot slot : recipe.slots()) {
            for (ItemStack stack : slot.stacks()) {
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        }
        if (!recipe.machine().isEmpty()) {
            stacks.add(recipe.machine());
        }
        return stacks;
    }

    public static String sourceKindLabel(IndexRecipeView recipe) {
        if (!sourceCard(recipe)) {
            return recipe == null ? "" : recipe.sourceModId();
        }
        return IndexRecipeSourceKind.of(recipe).label();
    }

    public static void drawRecipeCard(GuiGraphicsExtractor graphics, Font font, IndexRecipeView recipe,
            int x, int y, int w, int h, ItemStack fallback, int mouseX, int mouseY, List<SlotHit> slotHits) {
        graphics.fill(x, y, x + w, y + h, 0xAA071014);
        if (recipe == null) {
            graphics.text(font, "No recipe selected.", x + 10, y + 12, MUTED, false);
            return;
        }
        beginRecipeChoiceScope(recipe);
        if (inside(mouseX, mouseY, x, y, w, h)) {
            lastHoveredRecipeId = recipe.id();
        }
        IndexRecipePlan plan = IndexRecipePlanner.plan(Minecraft.getInstance().player, recipe);
        IndexRecipeDisplayMetadata metadata = IndexRecipeQueryClientState.metadata(recipe.id()).orElse(null);
        if (metadata != null && metadata.vanillaLayout()) {
            drawVanillaRecipeCard(graphics, font, recipe, metadata, plan, x, y, w, h, fallback, mouseX, mouseY, slotHits);
            endRecipeChoiceScope();
            return;
        }
        CardMode mode = cardMode(w, h);
        graphics.item(recipeIcon(recipe, fallback), x + 10, y + 10);
        int statusW = statusLaneWidth(plan, mode);
        int titleW = Math.max(42, w - statusW - 48);
        graphics.text(font, trim(font, recipe.title(), titleW), x + 34, y + 9, CYAN, true);
        String meta = sourceCard(recipe) ? sourceKindLabel(recipe) : recipe.categoryId().getPath();
        graphics.text(font, trim(font, meta + " / " + recipe.sourceModId(), titleW), x + 34, y + 22, MUTED, false);
        if (mode != CardMode.COMPACT) {
            drawBadge(graphics, font, x + 34, y + 35, sourceCard(recipe) ? "Source" : "Machine", sourceCard(recipe) ? WARN : CYAN);
        }
        graphics.text(font, trim(font, statusLabel(plan, true), statusW), x + Math.max(42, w - statusW - 6), y + 9,
                statusColor(plan, true), false);
        if (recipe.processTicks() > 0) {
            graphics.text(font, trim(font, recipe.processTicks() + " ticks", statusW), x + Math.max(42, w - statusW - 6),
                    y + 24, MUTED, false);
        } else if (!statusDetail(plan, true).isBlank() && plan.state() != IndexRecipeActionState.MISSING) {
            graphics.text(font, trim(font, statusDetail(plan, true), titleW), x + 34, y + 34, MUTED, false);
        }

        int slotY = y + (mode == CardMode.COMPACT ? 50 : 58);
        int renderedGroups = 0;
        int maxGroups = Math.max(1, (h - 84) / 27);
        for (IndexRecipeSlot slot : recipe.slots()) {
            if (renderedGroups >= maxGroups) {
                break;
            }
            drawSlotGroup(graphics, font, slot, x + 10, slotY + renderedGroups * 27, w - 20, mouseX, mouseY,
                    slotHits, plan);
            renderedGroups++;
        }

        int noteY = Math.min(y + h - 44, slotY + renderedGroups * 27 + 6);
        if (recipe.slots().size() > renderedGroups && noteY <= y + h - 22) {
            graphics.text(font, "+" + (recipe.slots().size() - renderedGroups) + " more rows", x + 10, noteY, MUTED, false);
            noteY += 12;
        }
        if (recipe.processTicks() > 0 && noteY <= y + h - 30) {
            String stats = recipe.processTicks() + " ticks";
            if (!recipe.notes().isEmpty() && recipe.notes().get(0).toLowerCase().contains("power")) {
                stats += "  |  " + recipe.notes().get(0);
            }
            drawBadge(graphics, font, x + 10, noteY, stats, 0xFF5BC0EB);
            noteY += 18;
        }
        int renderedNotes = 0;
        int maxNotes = mode == CardMode.TALL ? 3 : mode == CardMode.STANDARD ? 2 : 1;
        for (String note : recipe.notes().stream().limit(maxNotes).toList()) {
            graphics.textWithWordWrap(font, Component.literal(note), x + 10, noteY, w - 20, MUTED);
            noteY += 16;
            renderedNotes++;
            if (noteY > y + h - 18) {
                break;
            }
        }
        if (recipe.notes().size() > renderedNotes && noteY <= y + h - 18) {
            graphics.text(font, "+" + (recipe.notes().size() - renderedNotes) + " more notes", x + 10, noteY, MUTED, false);
        }
        if (Config.DEBUG_SHOW_RECIPE_IDS.get()) {
            graphics.text(font, trim(font, recipe.id().toString(), w - 20), x + 10, y + h - 14, 0xFF6C7E84, false);
        }
        endRecipeChoiceScope();
    }

    private static void drawVanillaRecipeCard(GuiGraphicsExtractor graphics, Font font, IndexRecipeView recipe,
            IndexRecipeDisplayMetadata metadata, IndexRecipePlan plan, int x, int y, int w, int h,
            ItemStack fallback, int mouseX, int mouseY, List<SlotHit> slotHits) {
        IndexRecipeDisplayMetadata effectiveMetadata = metadata.hasRenderableInputCells()
                ? metadata
                : metadata.withFallbackInputCellsFromSlots(recipe.slots());
        graphics.fill(x, y, x + w, y + h, 0xAA071014);
        CardMode mode = cardMode(w, h);
        ItemStack icon = recipeIcon(recipe, fallback);
        graphics.item(icon, x + 10, y + 10);
        int statusW = statusLaneWidth(plan, mode);
        graphics.text(font, trim(font, recipe.title(), w - statusW - 44), x + 34, y + 9, CYAN, true);
        graphics.text(font, trim(font, layoutLabel(effectiveMetadata.type()) + " / " + recipe.sourceModId(), w - statusW - 44),
                x + 34, y + 22, MUTED, false);
        if (mode != CardMode.COMPACT) {
            drawBadge(graphics, font, x + 34, y + 35, layoutBadge(effectiveMetadata.type()), badgeColor(effectiveMetadata.type()));
        }
        graphics.text(font, trim(font, statusLabel(plan, true), statusW), x + Math.max(44, w - statusW - 4), y + 9,
                statusColor(plan, true), false);
        String detail = recipe.processTicks() > 0 ? recipe.processTicks() + " ticks" : statusDetail(plan, true);
        if (!detail.isBlank() && mode != CardMode.COMPACT) {
            graphics.text(font, trim(font, detail, 108), x + Math.max(44, w - 112), y + 24, MUTED, false);
        }

        int visualY = y + (mode == CardMode.COMPACT ? 44 : 58);
        int visualX = x + Math.max(10, (w - layoutVisualWidth(effectiveMetadata)) / 2);
        int summaryY = switch (effectiveMetadata.type()) {
            case CRAFTING_SHAPED, CRAFTING_SHAPELESS -> drawCraftingLayout(graphics, font, effectiveMetadata,
                    visualX, visualY, mouseX, mouseY, slotHits, plan);
            case COOKING -> drawCookingLayout(graphics, font, recipe, effectiveMetadata, visualX, visualY, mouseX, mouseY, slotHits, plan);
            case STONECUTTING -> drawStonecuttingLayout(graphics, font, effectiveMetadata, visualX, visualY, mouseX, mouseY, slotHits, plan);
            case SMITHING -> drawSmithingLayout(graphics, font, effectiveMetadata, visualX, visualY, mouseX, mouseY, slotHits, plan);
            case GENERIC -> visualY;
        };

        int noteY = drawNeedSummary(graphics, font, plan, x + 10, summaryY + 6, w - 20, y + h - 30,
                mode == CardMode.COMPACT ? 2 : mode == CardMode.STANDARD ? 4 : 6);
        int maxNotesVanilla = mode == CardMode.TALL ? 3 : mode == CardMode.STANDARD ? 2 : 1;
        if (mode != CardMode.COMPACT) {
            for (String note : recipe.notes().stream().limit(maxNotesVanilla).toList()) {
                if (noteY > y + h - 22) {
                    break;
                }
                graphics.textWithWordWrap(font, Component.literal(note), x + 10, noteY, w - 20, MUTED);
                noteY += 16;
            }
        }
        if (Config.DEBUG_SHOW_RECIPE_IDS.get()) {
            graphics.text(font, trim(font, recipe.id().toString(), w - 20), x + 10, y + h - 14, 0xFF6C7E84, false);
        }
    }

    private static int drawCraftingLayout(GuiGraphicsExtractor graphics, Font font, IndexRecipeDisplayMetadata metadata,
            int x, int y, int mouseX, int mouseY, List<SlotHit> slotHits, IndexRecipePlan plan) {
        int cell = 20;
        int grid = 3;
        for (int row = 0; row < grid; row++) {
            for (int col = 0; col < grid; col++) {
                List<ItemStack> choices = recipeCell(metadata, col, row);
                drawRecipeCell(graphics, font, choices, IndexSlotRole.INPUT, plan,
                        x + col * cell, y + row * cell, cell, mouseX, mouseY, slotHits);
            }
        }
        int arrowX = x + grid * cell + 12;
        graphics.text(font, ">", arrowX + 8, y + 25, CYAN, false);
        drawRecipeCell(graphics, font, List.of(output(metadata)), IndexSlotRole.OUTPUT, plan,
                arrowX + 34, y + 20, cell, mouseX, mouseY, slotHits);
        if (!metadata.machine().isEmpty()) {
            graphics.item(metadata.machine(), arrowX + 34, y + 48);
            graphics.text(font, trim(font, metadata.machine().getHoverName().getString(), 90), arrowX + 55, y + 53, MUTED, false);
        }
        return y + 68;
    }

    private static int drawCookingLayout(GuiGraphicsExtractor graphics, Font font, IndexRecipeView recipe,
            IndexRecipeDisplayMetadata metadata, int x, int y, int mouseX, int mouseY, List<SlotHit> slotHits,
            IndexRecipePlan plan) {
        List<ItemStack> input = metadata.cells().isEmpty() ? List.of() : metadata.cells().getFirst();
        List<ItemStack> fuel = firstSlotChoices(recipe, IndexSlotRole.CATALYST);
        drawRecipeCell(graphics, font, input, IndexSlotRole.INPUT, plan, x + 4, y + 6, 20, mouseX, mouseY, slotHits);
        drawRecipeCell(graphics, font, fuel, IndexSlotRole.CATALYST, plan, x + 4, y + 34, 20, mouseX, mouseY, slotHits);
        if (!metadata.machine().isEmpty()) {
            drawRecipeCell(graphics, font, List.of(metadata.machine()), IndexSlotRole.MACHINE, plan,
                    x + 58, y + 20, 20, mouseX, mouseY, slotHits);
        }
        graphics.text(font, ">", x + 94, y + 25, CYAN, false);
        drawRecipeCell(graphics, font, List.of(output(metadata)), IndexSlotRole.OUTPUT, plan,
                x + 126, y + 20, 20, mouseX, mouseY, slotHits);
        graphics.text(font, "Input", x + 28, y + 11, MUTED, false);
        if (!fuel.isEmpty()) {
            graphics.text(font, "Fuel", x + 28, y + 39, MUTED, false);
        }
        return y + 62;
    }

    private static int drawStonecuttingLayout(GuiGraphicsExtractor graphics, Font font, IndexRecipeDisplayMetadata metadata,
            int x, int y, int mouseX, int mouseY, List<SlotHit> slotHits, IndexRecipePlan plan) {
        List<ItemStack> input = metadata.cells().isEmpty() ? List.of() : metadata.cells().getFirst();
        drawRecipeCell(graphics, font, input, IndexSlotRole.INPUT, plan, x + 4, y + 20, 20, mouseX, mouseY, slotHits);
        if (!metadata.machine().isEmpty()) {
            drawRecipeCell(graphics, font, List.of(metadata.machine()), IndexSlotRole.MACHINE, plan,
                    x + 58, y + 20, 20, mouseX, mouseY, slotHits);
        }
        graphics.text(font, ">", x + 94, y + 25, CYAN, false);
        drawRecipeCell(graphics, font, List.of(output(metadata)), IndexSlotRole.OUTPUT, plan,
                x + 126, y + 20, 20, mouseX, mouseY, slotHits);
        return y + 56;
    }

    private static int drawSmithingLayout(GuiGraphicsExtractor graphics, Font font, IndexRecipeDisplayMetadata metadata,
            int x, int y, int mouseX, int mouseY, List<SlotHit> slotHits, IndexRecipePlan plan) {
        for (int i = 0; i < 3; i++) {
            List<ItemStack> choices = i < metadata.cells().size() ? metadata.cells().get(i) : List.of();
            drawRecipeCell(graphics, font, choices, IndexSlotRole.INPUT, plan,
                    x + i * 24, y + 20, 20, mouseX, mouseY, slotHits);
        }
        if (!metadata.machine().isEmpty()) {
            drawRecipeCell(graphics, font, List.of(metadata.machine()), IndexSlotRole.MACHINE, plan,
                    x + 84, y + 20, 20, mouseX, mouseY, slotHits);
        }
        graphics.text(font, ">", x + 118, y + 25, CYAN, false);
        drawRecipeCell(graphics, font, List.of(output(metadata)), IndexSlotRole.OUTPUT, plan,
                x + 150, y + 20, 20, mouseX, mouseY, slotHits);
        return y + 56;
    }

    private static void drawRecipeCell(GuiGraphicsExtractor graphics, Font font, List<ItemStack> choices,
            IndexSlotRole role, IndexRecipePlan plan, int x, int y, int size, int mouseX, int mouseY,
            List<SlotHit> slotHits) {
        boolean hover = inside(mouseX, mouseY, x, y, size, size);
        IndexIngredientNeed need = needForChoices(plan, role, choices);
        int outline = cellOutline(role, need, choices, hover);
        graphics.fill(x, y, x + size, y + size, choices == null || choices.isEmpty() ? 0x66102630 : ROW);
        graphics.outline(x, y, size, size, outline);
        String choiceKey = choiceKey(role, x, y, choices);
        int choiceCount = visibleChoiceCount(choices);
        ItemStack stack = visibleChoice(choices, choiceKey);
        if (!stack.isEmpty()) {
            if (hover) {
                lastHoveredItemId = IndexService.itemId(stack.getItem());
            }
            graphics.item(stack, x + 2, y + 2);
            int required = need == null ? Math.max(1, stack.getCount()) : need.required();
            graphics.itemDecorations(font, stack, x + 2, y + 2);
            if (required > 1) {
                String badge = Integer.toString(required);
                graphics.text(font, badge, x + size - font.width(badge) - 1, y + size - 8, TEXT, true);
            }
            if (choices != null && choices.size() > 1) {
                String badge = "+" + (choices.size() - 1);
                graphics.text(font, badge, x + size - font.width(badge) - 1, y + 1, CYAN, true);
            }
            if (hover) {
                graphics.setComponentTooltipForNextFrame(font, cellTooltip(stack, choices, role, need),
                        x + size / 2, y + size / 2);
            }
            if (slotHits != null) {
                slotHits.add(new SlotHit(x, y, size, size, stack.copy(), role, choiceKey, choiceCount));
            }
        }
    }

    private static int drawNeedSummary(GuiGraphicsExtractor graphics, Font font, IndexRecipePlan plan,
            int x, int y, int width, int bottom, int maxRows) {
        List<NeedRow> rows = aggregateNeeds(plan);
        if (rows.isEmpty() || y > bottom) {
            return y;
        }
        graphics.text(font, "Ingredients", x, y, MUTED, false);
        y += 13;
        int rendered = 0;
        for (NeedRow row : rows) {
            if (y + 18 > bottom || rendered >= maxRows) {
                break;
            }
            int color = row.missing() == 0 ? GREEN : WARN;
            graphics.item(row.stack(), x, y);
            String line = row.stack().getHoverName().getString() + " "
                    + Math.max(0, row.required() - row.missing()) + "/" + row.required();
            graphics.text(font, trim(font, line, width - 24), x + 22, y + 5, color, false);
            y += 19;
            rendered++;
        }
        if (rows.size() > rendered && y + 10 <= bottom) {
            graphics.text(font, "+" + (rows.size() - rendered) + " more", x + 2, y, MUTED, false);
            y += 12;
        }
        return y + 2;
    }

    private static List<NeedRow> aggregateNeeds(IndexRecipePlan plan) {
        if (plan == null || plan.needs().isEmpty()) {
            return List.of();
        }
        Map<String, NeedAccumulator> grouped = new LinkedHashMap<>();
        for (IndexIngredientNeed need : plan.needs()) {
            if (need.selected().isEmpty() || need.role() == IndexSlotRole.OUTPUT || need.role() == IndexSlotRole.MACHINE) {
                continue;
            }
            String key = need.role().name() + ":" + IndexService.itemId(need.selected().getItem());
            grouped.computeIfAbsent(key, ignored -> new NeedAccumulator(need.selected()))
                    .add(need.required(), need.missing());
        }
        return grouped.values().stream()
                .map(NeedAccumulator::row)
                .toList();
    }

    private static List<ItemStack> recipeCell(IndexRecipeDisplayMetadata metadata, int col, int row) {
        int width = Math.max(1, metadata.width());
        int height = Math.max(1, metadata.height());
        if (col >= width || row >= height) {
            return List.of();
        }
        int index = row * width + col;
        return index >= 0 && index < metadata.cells().size() ? metadata.cells().get(index) : List.of();
    }

    private static List<ItemStack> firstSlotChoices(IndexRecipeView recipe, IndexSlotRole role) {
        for (IndexRecipeSlot slot : recipe.slots()) {
            if (slot.role() == role) {
                return slot.stacks();
            }
        }
        return List.of();
    }

    private static ItemStack output(IndexRecipeDisplayMetadata metadata) {
        return metadata.output().isEmpty() ? ItemStack.EMPTY : metadata.output();
    }

    private static ItemStack firstStack(List<ItemStack> stacks) {
        if (stacks == null) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack visibleChoice(List<ItemStack> choices, String choiceKey) {
        if (choices == null || choices.isEmpty()) {
            return ItemStack.EMPTY;
        }
        List<ItemStack> visible = choices.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .toList();
        if (visible.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (visible.size() == 1) {
            return visible.getFirst();
        }
        Integer manualOffset = CHOICE_OFFSETS.get(choiceKey);
        if (manualOffset != null) {
            return visible.get(Math.floorMod(manualOffset, visible.size()));
        }
        long frame = System.currentTimeMillis() / 1200L;
        return visible.get((int) Math.floorMod(frame, visible.size()));
    }

    private static int visibleChoiceCount(List<ItemStack> choices) {
        if (choices == null || choices.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : choices) {
            if (stack != null && !stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static String choiceKey(IndexSlotRole role, int x, int y, List<ItemStack> choices) {
        StringBuilder builder = new StringBuilder(role.name());
        if (currentRecipeId != null) {
            builder.append(':').append(currentRecipeId).append(':').append(currentChoiceCell++);
        } else {
            builder.append(':').append(x).append(':').append(y);
        }
        if (choices != null) {
            int added = 0;
            for (ItemStack stack : choices) {
                if (stack != null && !stack.isEmpty()) {
                    builder.append(':').append(IndexService.itemId(stack.getItem()));
                    added++;
                    if (added >= 8) {
                        break;
                    }
                }
            }
        }
        return builder.toString();
    }

    private static void beginRecipeChoiceScope(IndexRecipeView recipe) {
        currentRecipeId = recipe == null ? null : recipe.id();
        currentChoiceCell = 0;
    }

    private static void endRecipeChoiceScope() {
        currentRecipeId = null;
        currentChoiceCell = 0;
    }

    private static IndexIngredientNeed needForChoices(IndexRecipePlan plan, IndexSlotRole role, List<ItemStack> choices) {
        if (plan == null || role == null || choices == null || choices.isEmpty()) {
            return null;
        }
        for (IndexIngredientNeed need : plan.needs()) {
            if (need.role() != role || need.selected().isEmpty()) {
                continue;
            }
            for (ItemStack choice : choices) {
                if (choice != null && !choice.isEmpty() && choice.is(need.selected().getItem())) {
                    return need;
                }
            }
        }
        return null;
    }

    private static int cellOutline(IndexSlotRole role, IndexIngredientNeed need, List<ItemStack> choices, boolean hover) {
        if (hover) {
            return CYAN;
        }
        if (need != null) {
            return need.satisfied() ? GREEN : RED;
        }
        if (choices == null || choices.isEmpty() || firstStack(choices).isEmpty()) {
            return 0x33445A63;
        }
        return switch (role) {
            case OUTPUT -> CYAN;
            case MACHINE -> WARN;
            case CATALYST -> 0xFFE09CFF;
            case INPUT -> CYAN;
            default -> MUTED;
        };
    }

    private static List<Component> cellTooltip(ItemStack stack, List<ItemStack> choices,
            IndexSlotRole role, IndexIngredientNeed need) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(stack.getHoverName());
        tooltip.add(Component.literal(roleLabel(role)));
        if (Config.DEBUG_SHOW_RECIPE_IDS.get()) {
            tooltip.add(Component.literal(IndexService.itemId(stack.getItem()).toString()));
        }
        if (need != null) {
            int available = availableCount(stack.getItem());
            tooltip.add(Component.literal("Available: " + available));
            tooltip.add(Component.literal("Required: " + need.required()));
            tooltip.add(Component.literal("Missing: " + Math.max(0, need.required() - available)));
        }
        if (choices != null && choices.size() > 1) {
            tooltip.add(Component.literal("Choices:"));
            int shown = 0;
            for (ItemStack choice : choices) {
                if (choice == null || choice.isEmpty()) {
                    continue;
                }
                tooltip.add(Component.literal("- " + choice.getHoverName().getString()));
                shown++;
                if (shown >= 8) {
                    break;
                }
            }
            int remaining = choices.size() - shown;
            if (remaining > 0) {
                tooltip.add(Component.literal("+" + remaining + " more"));
            }
        }
        return tooltip;
    }

    public static void cycleChoice(SlotHit hit, int direction) {
        if (hit == null || !hit.choiceCyclable()) {
            return;
        }
        int current = CHOICE_OFFSETS.getOrDefault(hit.choiceKey(), 0);
        CHOICE_OFFSETS.put(hit.choiceKey(), current + (direction == 0 ? 1 : direction));
    }

    public static void recordCardSelection(ViewMode mode, int selected, int count) {
        lastViewMode = mode == null ? ViewMode.RECIPES : mode;
        lastSelectedCardIndex = Math.max(0, selected);
        lastSelectedCardCount = Math.max(0, count);
    }

    public static Identifier lastHoveredRecipeId() {
        return lastHoveredRecipeId;
    }

    public static Identifier lastHoveredItemId() {
        return lastHoveredItemId;
    }

    public static Identifier lastQueriedItemId() {
        return lastQueriedItemId;
    }

    public static ViewMode lastViewMode() {
        return lastViewMode;
    }

    public static String selectedCardLabel() {
        if (lastSelectedCardCount <= 0) {
            return "0 / 0";
        }
        return (Math.min(lastSelectedCardIndex, lastSelectedCardCount - 1) + 1) + " / " + lastSelectedCardCount;
    }

    public static String lastQueryCacheState() {
        return lastQueryCacheState;
    }

    private static CardMode cardMode(int width, int height) {
        if (height < 128 || width < 210) {
            return CardMode.COMPACT;
        }
        return height < 204 || width < 270 ? CardMode.STANDARD : CardMode.TALL;
    }

    private static int statusLaneWidth(IndexRecipePlan plan, CardMode mode) {
        int base = mode == CardMode.COMPACT ? 70 : 92;
        if (plan != null && plan.missingCount() >= 10) {
            return base + 10;
        }
        return base;
    }

    private static int layoutVisualWidth(IndexRecipeDisplayMetadata metadata) {
        return switch (metadata.type()) {
            case CRAFTING_SHAPED, CRAFTING_SHAPELESS -> metadata.machine().isEmpty() ? 130 : 186;
            case COOKING, STONECUTTING -> 166;
            case SMITHING -> 176;
            case GENERIC -> 120;
        };
    }

    public static String statusLabel(IndexRecipePlan plan, boolean allowTransfer) {
        if (plan == null) {
            return "Plan Only";
        }
        if (plan.sourceCard()) {
            return "Plan Only";
        }
        if (allowTransfer && plan.canTransfer()) {
            return "Ready";
        }
        if (plan.missingCount() > 0) {
            return "Missing " + plan.missingCount();
        }
        if (!plan.transferBlocker().isBlank() && plan.craftingRecipe()) {
            return "Blocked";
        }
        return plan.state().label();
    }

    public static int statusColor(IndexRecipePlan plan, boolean allowTransfer) {
        if (plan == null) {
            return MUTED;
        }
        if (allowTransfer && plan.canTransfer()) {
            return GREEN;
        }
        if (plan.missingCount() > 0) {
            return WARN;
        }
        return switch (plan.state()) {
            case READY -> GREEN;
            case MISSING -> WARN;
            case PLAN_ONLY -> MUTED;
        };
    }

    public static String statusDetail(IndexRecipePlan plan, boolean allowTransfer) {
        if (plan == null) {
            return "";
        }
        if (allowTransfer && plan.canTransfer()) {
            return "Ready to transfer";
        }
        if (plan.missingCount() > 0) {
            return "Missing " + plan.missingCount();
        }
        if (!plan.transferBlocker().isBlank()) {
            return plan.transferBlocker();
        }
        if (plan.sourceCard()) {
            return "Plan only";
        }
        return plan.state() == IndexRecipeActionState.PLAN_ONLY ? "Plan only" : "";
    }

    private static int availableCount(Item item) {
        Player player = Minecraft.getInstance().player;
        if (item == null || player == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static String layoutLabel(IndexRecipeLayoutType type) {
        return switch (type) {
            case CRAFTING_SHAPED -> "Shaped Crafting";
            case CRAFTING_SHAPELESS -> "Shapeless Crafting";
            case COOKING -> "Cooking";
            case STONECUTTING -> "Stonecutting";
            case SMITHING -> "Smithing";
            case GENERIC -> "Recipe";
        };
    }

    private static String layoutBadge(IndexRecipeLayoutType type) {
        return switch (type) {
            case CRAFTING_SHAPED -> "Shaped";
            case CRAFTING_SHAPELESS -> "Shapeless";
            case COOKING -> "Cooking";
            case STONECUTTING -> "Stonecutting";
            case SMITHING -> "Smithing";
            case GENERIC -> "Machine";
        };
    }

    private static int badgeColor(IndexRecipeLayoutType type) {
        return switch (type) {
            case CRAFTING_SHAPED, CRAFTING_SHAPELESS -> CYAN;
            case COOKING -> 0xFFFFB86B;
            case STONECUTTING -> 0xFFD6E6EE;
            case SMITHING -> 0xFFE09CFF;
            case GENERIC -> WARN;
        };
    }

    private static void drawBadge(GuiGraphicsExtractor graphics, Font font, int x, int y, String label, int color) {
        int w = Math.max(32, font.width(label) + 10);
        graphics.fill(x, y, x + w, y + 13, 0x66102630);
        graphics.outline(x, y, w, 13, color);
        graphics.centeredText(font, label, x + w / 2, y + 4, color);
    }

    private static String roleLabel(IndexSlotRole role) {
        return switch (role) {
            case OUTPUT -> "Output";
            case MACHINE -> "Machine";
            case CATALYST -> "Catalyst";
            case INPUT -> "Input";
            default -> "Info";
        };
    }

    private enum CardMode {
        COMPACT,
        STANDARD,
        TALL
    }

    private static final class NeedAccumulator {
        private final ItemStack stack;
        private int required;
        private int missing;

        private NeedAccumulator(ItemStack stack) {
            this.stack = stack.copy();
        }

        private void add(int required, int missing) {
            this.required += Math.max(0, required);
            this.missing += Math.max(0, missing);
        }

        private NeedRow row() {
            return new NeedRow(stack, required, Math.min(required, missing));
        }
    }

    private record NeedRow(ItemStack stack, int required, int missing) {
    }

    public static void drawSlotGroup(GuiGraphicsExtractor graphics, Font font, IndexRecipeSlot slot, int x, int y,
            int width, int mouseX, int mouseY, List<SlotHit> slotHits) {
        drawSlotGroup(graphics, font, slot, x, y, width, mouseX, mouseY, slotHits, null);
    }

    public static void drawSlotGroup(GuiGraphicsExtractor graphics, Font font, IndexRecipeSlot slot, int x, int y,
            int width, int mouseX, int mouseY, List<SlotHit> slotHits, IndexRecipePlan plan) {
        int labelColor = switch (slot.role()) {
            case OUTPUT -> GREEN;
            case MACHINE -> WARN;
            case CATALYST -> 0xFFE09CFF;
            case INPUT -> CYAN;
            default -> MUTED;
        };
        IndexIngredientNeed need = needFor(plan, slot);
        if (need != null) {
            labelColor = need.satisfied() ? GREEN : WARN;
        }
        String label = need == null ? slotLabel(slot)
                : slotLabel(slot) + " " + Math.min(need.available(), need.required()) + "/" + need.required();
        graphics.text(font, trim(font, label, 56), x, y + 6, labelColor, false);
        int itemX = x + 58;
        int max = Math.max(1, (width - 62) / 20);
        List<ItemStack> visibleStacks = slot.stacks().stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .limit(max)
                .toList();
        if (visibleStacks.isEmpty()) {
            int textW = Math.max(28, width - 62);
            graphics.fill(itemX, y + 1, itemX + textW, y + 19, 0x66102630);
            graphics.outline(itemX, y + 1, textW, 18, labelColor);
            graphics.text(font, trim(font, slotLabel(slot), textW - 8), itemX + 4, y + 6, labelColor, false);
            return;
        }
        for (ItemStack stack : visibleStacks) {
            graphics.fill(itemX, y, itemX + 20, y + 20, ROW);
            graphics.outline(itemX, y, 20, 20, inside(mouseX, mouseY, itemX, y, 20, 20) ? CYAN : 0x5538DFF4);
            graphics.item(stack, itemX + 2, y + 2);
            graphics.itemDecorations(font, stack, itemX + 2, y + 2);
            if (inside(mouseX, mouseY, itemX, y, 20, 20)) {
                graphics.setTooltipForNextFrame(font, stack, itemX + 10, y + 10);
            }
            if (slotHits != null && !stack.isEmpty()) {
                slotHits.add(new SlotHit(itemX, y, 20, 20, stack.copy(), slot.role(),
                        choiceKey(slot.role(), itemX, y, slot.stacks()), visibleChoiceCount(slot.stacks())));
            }
            itemX += 22;
        }
    }

    private static IndexIngredientNeed needFor(IndexRecipePlan plan, IndexRecipeSlot slot) {
        if (plan == null || slot == null || slot.stacks().isEmpty()) {
            return null;
        }
        for (IndexIngredientNeed need : plan.needs()) {
            if (need.role() != slot.role() || need.selected().isEmpty()) {
                continue;
            }
            for (ItemStack stack : slot.stacks()) {
                if (!stack.isEmpty() && stack.is(need.selected().getItem())) {
                    return need;
                }
            }
        }
        return null;
    }

    public static String emptyMessage(Player player, Item item, ViewMode mode) {
        if (clientContext(player)) {
            requestServerViews(item);
            var result = IndexRecipeQueryClientState.result(item);
            if (result.isEmpty()) {
                return "Loading recipe data from server.";
            }
            String warning = result.get().warning();
            if (!warning.isBlank()) {
                return warning;
            }
            return switch (mode) {
                case USES -> "No uses for this item.";
                case SOURCES -> IndexRecipeQueryClientState.health().sourceFactCount() > 0
                        ? "No source cards for this item."
                        : "No source cards for this item. Loot and worldgen facts are still unavailable.";
                case RECIPES -> "No recipes for this item.";
            };
        }
        var snapshot = IndexService.INSTANCE.recipeSnapshot(player);
        boolean noProviderRecipes = snapshot.recipes().isEmpty();
        boolean sourcesLoaded = snapshot.sourceCardsLoaded();
        return switch (mode) {
            case USES -> "No uses for this item.";
            case SOURCES -> sourcesLoaded
                    ? "No source cards for this item."
                    : "No source cards for this item. Loot and worldgen resources are still loading.";
            case RECIPES -> {
                if (snapshot.recipesStillLoading()) {
                    yield "Loading recipe data from server.";
                }
                if (noProviderRecipes) {
                    yield "Provider warning: open diagnostics.";
                }
                yield "No recipes for this item.";
            }
        };
    }

    public static String slotLabel(IndexRecipeSlot slot) {
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

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static String trim(Font font, String text, int width) {
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

    public enum ViewMode {
        RECIPES("Recipes"),
        USES("Uses"),
        SOURCES("Sources");

        private final String label;

        ViewMode(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private static void requestServerViews(Item item) {
        if (item == null) {
            return;
        }
        Identifier itemId = IndexService.itemId(item);
        if (IndexRecipeQueryClientState.shouldRequest(itemId)) {
            EchoNetClientActions.trySendServerboundAction(new IndexRecipeQueryPacket(itemId, true, true, true));
        }
    }

    private static boolean clientContext(Player player) {
        return player != null && player.level() != null && player.level().getServer() == null;
    }

    private static void recordQueryState(Player player, Item item, ViewMode mode) {
        lastViewMode = mode == null ? ViewMode.RECIPES : mode;
        if (item == null) {
            lastQueryCacheState = "none";
            return;
        }
        Identifier itemId = IndexService.itemId(item);
        lastQueriedItemId = itemId;
        if (clientContext(player)) {
            lastQueryCacheState = IndexRecipeQueryClientState.result(item).isPresent() ? "hit" : "miss";
        } else {
            lastQueryCacheState = "server";
        }
    }

    public record SlotHit(int x, int y, int w, int h, ItemStack stack, IndexSlotRole role,
            String choiceKey, int choiceCount) {
        public SlotHit(int x, int y, int w, int h, ItemStack stack) {
            this(x, y, w, h, stack, IndexSlotRole.INFO, "", 0);
        }

        public boolean choiceCyclable() {
            return choiceCount > 1 && choiceKey != null && !choiceKey.isBlank();
        }
    }
}
