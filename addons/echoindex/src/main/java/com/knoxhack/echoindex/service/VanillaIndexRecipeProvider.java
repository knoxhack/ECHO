package com.knoxhack.echoindex.service;

import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echoindex.EchoIndex;
import com.knoxhack.echoindex.IndexIds;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;

public enum VanillaIndexRecipeProvider implements IIndexRecipeProvider {
    INSTANCE;

    private static final Identifier CRAFTING = EchoIndex.id("recipe/crafting");
    private static final Identifier SMELTING = EchoIndex.id("recipe/smelting");
    private static final Identifier BLASTING = EchoIndex.id("recipe/blasting");
    private static final Identifier SMOKING = EchoIndex.id("recipe/smoking");
    private static final Identifier CAMPFIRE = EchoIndex.id("recipe/campfire_cooking");
    private static final Identifier STONECUTTING = EchoIndex.id("recipe/stonecutting");
    private static final Identifier SMITHING = EchoIndex.id("recipe/smithing");

    @Override
    public Identifier id() {
        return IndexIds.PROVIDER_VANILLA_RECIPES;
    }

    @Override
    public List<IndexRecipeCategory> recipeCategories(Player player) {
        return List.of(
                category(CRAFTING, "Crafting", Items.CRAFTING_TABLE, 0xFF66E8FF, 10),
                category(SMELTING, "Smelting", Items.FURNACE, 0xFFFFD166, 20),
                category(BLASTING, "Blasting", Items.BLAST_FURNACE, 0xFFFF8FA3, 30),
                category(SMOKING, "Smoking", Items.SMOKER, 0xFFFFA05B, 40),
                category(CAMPFIRE, "Campfire Cooking", Items.CAMPFIRE, 0xFFFFD166, 50),
                category(STONECUTTING, "Stonecutting", Items.STONECUTTER, 0xFFE9FBFF, 60),
                category(SMITHING, "Smithing", Items.SMITHING_TABLE, 0xFF92F7A6, 70));
    }

    @Override
    public List<IndexRecipeView> recipes(Player player) {
        if (player == null || player.level() == null) {
            return List.of();
        }
        List<IndexRecipeView> views = new ArrayList<>();
        if (player.level().getServer() != null) {
            for (RecipeHolder<?> holder : recipeHolders(player)) {
                IndexRecipeView view = adapt(holder);
                if (view != null) {
                    views.add(view);
                }
            }
        } else {
            for (RecipeDisplayEntry entry : clientDisplayEntries(player)) {
                IndexRecipeView view = adaptDisplayEntry(entry);
                if (view != null) {
                    views.add(view);
                }
            }
        }
        return views;
    }

    private static List<RecipeHolder<?>> recipeHolders(Player player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return List.of();
        }
        try {
            return List.copyOf(server.getRecipeManager().getRecipes());
        } catch (RuntimeException exception) {
            EchoIndex.LOGGER.debug("Index could not enumerate runtime recipes for {}.", player.getName().getString(), exception);
        }
        return List.of();
    }

    public static int rawRecipeCount(Player player) {
        if (player == null || player.level() == null) {
            return 0;
        }
        return recipeHolders(player).size();
    }

    public static int adaptedRecipeCount(Player player) {
        return INSTANCE.recipes(player).size();
    }

    private static List<RecipeDisplayEntry> clientDisplayEntries(Player player) {
        try {
            Object recipeBook = player.getClass().getMethod("getRecipeBook").invoke(player);
            Object collections = recipeBook.getClass().getMethod("getCollections").invoke(recipeBook);
            if (!(collections instanceof Iterable<?> iterable)) {
                return List.of();
            }
            List<RecipeDisplayEntry> entries = new ArrayList<>();
            Set<Integer> seen = new LinkedHashSet<>();
            for (Object collection : iterable) {
                Object recipes = collection.getClass().getMethod("getRecipes").invoke(collection);
                if (!(recipes instanceof Iterable<?> recipeIterable)) {
                    continue;
                }
                for (Object candidate : recipeIterable) {
                    if (candidate instanceof RecipeDisplayEntry entry && seen.add(entry.id().index())) {
                        entries.add(entry);
                    }
                }
            }
            return entries;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            EchoIndex.LOGGER.debug("Index could not enumerate client recipe displays for {}.",
                    player.getName().getString(), exception);
            return List.of();
        }
    }

    private static IndexRecipeView adapt(RecipeHolder<?> holder) {
        Recipe<?> recipe = holder.value();
        Identifier categoryId = categoryId(recipe);
        if (categoryId == null) {
            return null;
        }
        ItemStack output = output(recipe);
        if (output.isEmpty()) {
            return null;
        }
        ItemStack machine = machine(recipe, categoryId);
        List<IndexRecipeSlot> slots = new ArrayList<>();
        for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
            List<ItemStack> stacks = ingredient.items()
                    .map(Holder::value)
                    .map(ItemStack::new)
                    .filter(stack -> !stack.isEmpty())
                    .limit(24)
                    .toList();
            if (!stacks.isEmpty()) {
                slots.add(IndexRecipeSlot.inputs(stacks));
            }
        }
        if (!machine.isEmpty()) {
            slots.add(IndexRecipeSlot.machine(machine));
        }
        slots.add(IndexRecipeSlot.output(output));
        String title = output.getHoverName().getString();
        int ticks = processTicks(recipe);
        return new IndexRecipeView(
                holder.id().identifier(),
                categoryId,
                title,
                machine,
                slots,
                List.of("Source: " + holder.id().identifier().getNamespace()),
                ticks,
                false,
                holder.id().identifier().getNamespace());
    }

    private static IndexRecipeView adaptDisplayEntry(RecipeDisplayEntry entry) {
        RecipeDisplay display = entry.display();
        ItemStack output = display.result().resolveForFirstStack(ContextMap.EMPTY);
        if (output.isEmpty()) {
            return null;
        }
        ItemStack machine = display.craftingStation().resolveForFirstStack(ContextMap.EMPTY);
        Identifier categoryId = categoryId(display, machine);
        List<IndexRecipeSlot> slots = new ArrayList<>();
        Optional<List<Ingredient>> requirements = entry.craftingRequirements();
        if (requirements.isPresent()) {
            for (Ingredient ingredient : requirements.get()) {
                addIngredientSlot(slots, ingredient);
            }
        }
        if (slots.isEmpty()) {
            for (SlotDisplay input : inputDisplays(display)) {
                List<ItemStack> stacks = input.resolveForStacks(ContextMap.EMPTY).stream()
                        .filter(stack -> stack != null && !stack.isEmpty())
                        .map(ItemStack::copy)
                        .limit(24)
                        .toList();
                if (!stacks.isEmpty()) {
                    slots.add(IndexRecipeSlot.inputs(stacks));
                }
            }
        }
        if (!machine.isEmpty()) {
            slots.add(IndexRecipeSlot.machine(machine));
        }
        slots.add(IndexRecipeSlot.output(output.copy()));
        int ticks = display instanceof FurnaceRecipeDisplay furnace ? furnace.duration() : 0;
        Identifier outputId = IndexService.itemId(output.getItem());
        return new IndexRecipeView(
                EchoIndex.id("client_recipe/" + entry.id().index()),
                categoryId,
                output.getHoverName().getString(),
                machine,
                slots,
                List.of("Source: " + outputId.getNamespace()),
                ticks,
                false,
                outputId.getNamespace());
    }

    private static IndexRecipeCategory category(Identifier id, String title, Item icon, int color, int order) {
        return new IndexRecipeCategory(id, title, new ItemStack(icon), color, order);
    }

    private static Identifier categoryId(Recipe<?> recipe) {
        RecipeType<?> type = recipe.getType();
        if (type == RecipeType.CRAFTING) {
            return CRAFTING;
        }
        if (type == RecipeType.SMELTING) {
            return SMELTING;
        }
        if (type == RecipeType.BLASTING) {
            return BLASTING;
        }
        if (type == RecipeType.SMOKING) {
            return SMOKING;
        }
        if (type == RecipeType.CAMPFIRE_COOKING) {
            return CAMPFIRE;
        }
        if (type == RecipeType.STONECUTTING) {
            return STONECUTTING;
        }
        if (type == RecipeType.SMITHING) {
            return SMITHING;
        }
        return null;
    }

    private static Identifier categoryId(RecipeDisplay display, ItemStack machine) {
        if (display instanceof StonecutterRecipeDisplay || machine.is(Items.STONECUTTER)) {
            return STONECUTTING;
        }
        if (display instanceof SmithingRecipeDisplay || machine.is(Items.SMITHING_TABLE)) {
            return SMITHING;
        }
        if (display instanceof FurnaceRecipeDisplay) {
            if (machine.is(Items.BLAST_FURNACE)) {
                return BLASTING;
            }
            if (machine.is(Items.SMOKER)) {
                return SMOKING;
            }
            if (machine.is(Items.CAMPFIRE) || machine.is(Items.SOUL_CAMPFIRE)) {
                return CAMPFIRE;
            }
            return SMELTING;
        }
        return CRAFTING;
    }

    private static ItemStack output(Recipe<?> recipe) {
        for (RecipeDisplay display : recipe.display()) {
            ItemStack stack = display.result().resolveForFirstStack(ContextMap.EMPTY);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack machine(Recipe<?> recipe, Identifier categoryId) {
        for (RecipeDisplay display : recipe.display()) {
            ItemStack stack = display.craftingStation().resolveForFirstStack(ContextMap.EMPTY);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        String path = categoryId.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("smelting")) {
            return new ItemStack(Items.FURNACE);
        }
        if (path.contains("blasting")) {
            return new ItemStack(Items.BLAST_FURNACE);
        }
        if (path.contains("smoking")) {
            return new ItemStack(Items.SMOKER);
        }
        if (path.contains("campfire")) {
            return new ItemStack(Items.CAMPFIRE);
        }
        if (path.contains("stonecutting")) {
            return new ItemStack(Items.STONECUTTER);
        }
        if (path.contains("smithing")) {
            return new ItemStack(Items.SMITHING_TABLE);
        }
        return new ItemStack(Items.CRAFTING_TABLE);
    }

    private static int processTicks(Recipe<?> recipe) {
        try {
            Object value = recipe.getClass().getMethod("cookingTime").invoke(recipe);
            return value instanceof Integer ticks ? ticks : 0;
        } catch (ReflectiveOperationException exception) {
            return 0;
        }
    }

    private static void addIngredientSlot(List<IndexRecipeSlot> slots, Ingredient ingredient) {
        List<ItemStack> stacks = ingredient.items()
                .map(Holder::value)
                .map(ItemStack::new)
                .filter(stack -> !stack.isEmpty())
                .limit(24)
                .toList();
        if (!stacks.isEmpty()) {
            slots.add(IndexRecipeSlot.inputs(stacks));
        }
    }

    private static List<SlotDisplay> inputDisplays(RecipeDisplay display) {
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            return shaped.ingredients();
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return shapeless.ingredients();
        }
        if (display instanceof FurnaceRecipeDisplay furnace) {
            return List.of(furnace.ingredient());
        }
        if (display instanceof StonecutterRecipeDisplay stonecutter) {
            return List.of(stonecutter.input());
        }
        if (display instanceof SmithingRecipeDisplay smithing) {
            return List.of(smithing.template(), smithing.base(), smithing.addition());
        }
        return List.of();
    }
}
