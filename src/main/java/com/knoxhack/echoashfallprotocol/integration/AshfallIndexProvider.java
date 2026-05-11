package com.knoxhack.echoashfallprotocol.integration;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.index.IIndexEntryProvider;
import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IIndexRegistry;
import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echocore.api.index.IndexEntryState;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.entity.OreGrinderBlockEntity;
import com.knoxhack.echoashfallprotocol.recipe.ScrapPressRecipe;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum AshfallIndexProvider implements IIndexEntryProvider, IIndexRecipeProvider {
    INSTANCE;

    private static final Identifier CATEGORY_MACHINES = id("machines");
    private static final Identifier SCRAP_PRESS = id("recipe/scrap_press");
    private static final Identifier SUBSTRATE_GRINDER = id("recipe/substrate_grinder");

    public static void register() {
        EchoCoreServices.registerIndexProvider(INSTANCE);
        EchoCoreServices.registerIndexRecipeProvider(INSTANCE);
    }

    @Override
    public Identifier id() {
        return id("provider/index");
    }

    @Override
    public void register(IIndexRegistry registry) {
        registry.registerCategory(new IndexCategory(
                CATEGORY_MACHINES,
                "Ashfall Machines",
                "Core survival processing machines from Ashfall Protocol.",
                new ItemStack(ModBlocks.SCRAP_PRESS_ITEM.get()),
                120,
                EchoAshfallProtocol.MODID));
        registry.registerEntry(entry(
                "scrap_press",
                "Scrap Press",
                "Compresses raw scrap into stable crafting components.",
                "The Scrap Press turns salvage piles into usable machine parts without depending on vanilla crafting grids.",
                new ItemStack(ModBlocks.SCRAP_PRESS_ITEM.get()),
                List.of(itemId(ModBlocks.SCRAP_PRESS_ITEM.get().asItem())),
                10));
        registry.registerEntry(entry(
                "substrate_grinder",
                "Substrate Grinder",
                "Refines stone, rubble, and trace fragments into Ashfall resource streams.",
                "The Substrate Grinder exposes biome substrate routes, byproducts, power costs, and handling hints to ECHO: Index.",
                new ItemStack(ModBlocks.ORE_GRINDER_ITEM.get()),
                List.of(itemId(ModBlocks.ORE_GRINDER_ITEM.get().asItem())),
                20));
    }

    @Override
    public List<IndexRecipeCategory> recipeCategories(Player player) {
        return List.of(
                new IndexRecipeCategory(SCRAP_PRESS, "Ashfall Scrap Press",
                        new ItemStack(ModBlocks.SCRAP_PRESS_ITEM.get()), 0xFF66E8FF, 220),
                new IndexRecipeCategory(SUBSTRATE_GRINDER, "Ashfall Substrate Grinder",
                        new ItemStack(ModBlocks.ORE_GRINDER_ITEM.get()), 0xFFFFD166, 230));
    }

    @Override
    public List<IndexRecipeView> recipes(Player player) {
        List<IndexRecipeView> views = new ArrayList<>();
        for (ScrapPressRecipe recipe : ScrapPressRecipe.getAllRecipes()) {
            ItemStack input = recipe.createInputStack();
            ItemStack output = recipe.createOutputStack();
            views.add(new IndexRecipeView(
                    id("scrap_press/" + path(input.getItem()) + "_to_" + path(output.getItem())),
                    SCRAP_PRESS,
                    output.getHoverName().getString(),
                    new ItemStack(ModBlocks.SCRAP_PRESS_ITEM.get()),
                    List.of(
                            IndexRecipeSlot.input(input),
                            IndexRecipeSlot.machine(new ItemStack(ModBlocks.SCRAP_PRESS_ITEM.get())),
                            IndexRecipeSlot.output(output)),
                    List.of("Pressure-forming salvage route.", "Time: " + recipe.processingTime() + " ticks"),
                    recipe.processingTime(),
                    false,
                    EchoAshfallProtocol.MODID));
        }
        for (OreGrinderBlockEntity.GrinderRecipe recipe : OreGrinderBlockEntity.getSubstrateRecipes().values()) {
            List<IndexRecipeSlot> slots = new ArrayList<>();
            slots.add(IndexRecipeSlot.input(new ItemStack(recipe.input(), recipe.inputCount())));
            slots.add(IndexRecipeSlot.machine(new ItemStack(ModBlocks.ORE_GRINDER_ITEM.get())));
            slots.add(IndexRecipeSlot.output(new ItemStack(recipe.output(), recipe.outputCount())));
            if (recipe.byproduct() != null) {
                slots.add(new IndexRecipeSlot(IndexSlotRole.OUTPUT,
                        List.of(new ItemStack(recipe.byproduct(), recipe.byproductCount())),
                        "Byproduct " + Math.round(recipe.byproductChance() * 100.0F) + "%"));
            }
            views.add(new IndexRecipeView(
                    id("substrate_grinder/" + path(recipe.input()) + "_to_" + path(recipe.output())),
                    SUBSTRATE_GRINDER,
                    new ItemStack(recipe.output()).getHoverName().getString(),
                    new ItemStack(ModBlocks.ORE_GRINDER_ITEM.get()),
                    slots,
                    List.of(recipe.categoryLabel(), recipe.handlingHint(),
                            "Power: " + recipe.powerPerOperation() + " FE/op"),
                    recipe.processTime(),
                    false,
                    EchoAshfallProtocol.MODID));
        }
        return views;
    }

    private static IndexEntry entry(String path, String title, String summary, String body, ItemStack icon,
            List<Identifier> linkedItems, int sortOrder) {
        return new IndexEntry(
                id("machine/" + path),
                CATEGORY_MACHINES,
                title,
                "",
                summary,
                body,
                icon,
                EchoAshfallProtocol.MODID,
                List.of("machine", "ashfall", "recipe", path),
                IndexEntryState.VISIBLE,
                List.of(),
                linkedItems,
                List.of(),
                sortOrder);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, sanitize(path));
    }

    private static Identifier itemId(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? Identifier.withDefaultNamespace("air") : id;
    }

    private static String path(Item item) {
        return sanitize(itemId(item).getPath());
    }

    private static String sanitize(String path) {
        String clean = path == null ? "unknown" : path.trim().toLowerCase(Locale.ROOT);
        clean = clean.replace('\\', '/').replace(':', '/').replaceAll("[^a-z0-9_./-]", "_");
        while (clean.contains("//")) {
            clean = clean.replace("//", "/");
        }
        return clean.isBlank() ? "unknown" : clean;
    }
}
