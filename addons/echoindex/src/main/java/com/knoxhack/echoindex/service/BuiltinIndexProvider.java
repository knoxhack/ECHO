package com.knoxhack.echoindex.service;

import com.knoxhack.echocore.api.index.IIndexEntryProvider;
import com.knoxhack.echocore.api.index.IIndexRegistry;
import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echocore.api.index.IndexEntryState;
import com.knoxhack.echoindex.IndexIds;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum BuiltinIndexProvider implements IIndexEntryProvider {
    INSTANCE;

    @Override
    public Identifier id() {
        return IndexIds.PROVIDER_BUILTIN;
    }

    @Override
    public void register(IIndexRegistry registry) {
        registry.registerCategory(category(IndexIds.CATEGORY_ITEMS, "items", Items.CHEST, 10));
        registry.registerCategory(category(IndexIds.CATEGORY_BLOCKS, "blocks", Items.STONE, 20));
        registry.registerCategory(category(IndexIds.CATEGORY_MACHINES, "machines", Items.CRAFTER, 30));
        registry.registerCategory(category(IndexIds.CATEGORY_TOOLS, "tools", Items.IRON_PICKAXE, 40));
        registry.registerCategory(category(IndexIds.CATEGORY_COMBAT, "combat", Items.IRON_SWORD, 50));
        registry.registerCategory(category(IndexIds.CATEGORY_RECIPES, "recipes", Items.CRAFTING_TABLE, 60));
        registry.registerCategory(category(IndexIds.CATEGORY_TUTORIALS, "tutorials", Items.BOOK, 70));
        registry.registerCategory(category(IndexIds.CATEGORY_LORE, "lore", Items.WRITABLE_BOOK, 80));
        registry.registerCategory(category(IndexIds.CATEGORY_RESEARCH, "research", Items.AMETHYST_SHARD, 90));
        registry.registerEntry(new IndexEntry(
                IndexIds.ENTRY_OVERVIEW,
                IndexIds.CATEGORY_TUTORIALS,
                "echoindex.entry.index_overview",
                "echoindex.entry.index_overview.subtitle",
                "echoindex.entry.index_overview.summary",
                "echoindex.entry.index_overview.body",
                new ItemStack(Items.BOOK),
                "echoindex",
                List.of("index", "codex", "archive", "recipe"),
                IndexEntryState.DISCOVERED,
                List.of(),
                List.of(),
                List.of(),
                0));
    }

    private static IndexCategory category(Identifier id, String key, net.minecraft.world.item.Item icon, int order) {
        return new IndexCategory(
                id,
                "echoindex.category." + key,
                "echoindex.category." + key + ".desc",
                new ItemStack(icon),
                order,
                "echoindex");
    }
}
