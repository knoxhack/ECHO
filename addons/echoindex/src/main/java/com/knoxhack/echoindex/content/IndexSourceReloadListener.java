package com.knoxhack.echoindex.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.knoxhack.echoindex.EchoIndex;
import com.knoxhack.echoindex.service.IndexSourceRecipeProvider;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;

public final class IndexSourceReloadListener
        extends SimplePreparableReloadListener<List<IndexSourceRecipeProvider.SourceFact>> {
    private static final String LOOT_TABLE_DIR = "loot_table";
    private static final String WORLDGEN_DIR = "worldgen";

    @Override
    protected List<IndexSourceRecipeProvider.SourceFact> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, IndexSourceRecipeProvider.SourceFact> facts = new LinkedHashMap<>();
        scanLootTables(manager, facts);
        scanWorldgen(manager, facts);
        return List.copyOf(facts.values());
    }

    @Override
    protected void apply(List<IndexSourceRecipeProvider.SourceFact> payload, ResourceManager manager, ProfilerFiller profiler) {
        IndexSourceRecipeProvider.INSTANCE.replaceSources(payload);
        EchoIndex.LOGGER.debug("ECHO: Index loaded {} source card fact(s).", payload.size());
    }

    private static void scanLootTables(ResourceManager manager, Map<String, IndexSourceRecipeProvider.SourceFact> facts) {
        scan(manager, LOOT_TABLE_DIR, (resourceId, json) -> {
            Set<Identifier> itemIds = collectItemIds(json);
            Identifier blockItemId = blockLootItemId(resourceId);
            if (blockItemId != null) {
                itemIds.add(blockItemId);
            }
            boolean blockLoot = isBlockLoot(resourceId);
            for (Identifier itemId : itemIds) {
                add(facts, IndexSourceRecipeProvider.SourceFact.of(
                        itemId,
                        resourceId,
                        blockLoot ? "Block Drop" : "Loot Source",
                        lootNotes(resourceId, blockLoot),
                        blockLoot ? Items.GRASS_BLOCK : Items.CHEST,
                        resourceId.getNamespace()));
            }
        });
    }

    private static void scanWorldgen(ResourceManager manager, Map<String, IndexSourceRecipeProvider.SourceFact> facts) {
        scan(manager, WORLDGEN_DIR, (resourceId, json) -> {
            for (Identifier itemId : collectItemIds(json)) {
                add(facts, IndexSourceRecipeProvider.SourceFact.of(
                        itemId,
                        resourceId,
                        "World Generation",
                        List.of("Referenced by world generation.", "Definition: " + displayId(resourceId)),
                        Items.GRASS_BLOCK,
                        resourceId.getNamespace()));
            }
        });
    }

    private static void scan(ResourceManager manager, String directory, JsonConsumer consumer) {
        for (Map.Entry<Identifier, Resource> entry : manager.listResources(directory, id -> id.getPath().endsWith(".json")).entrySet()) {
            Identifier resourceId = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                consumer.accept(resourceId, JsonParser.parseReader(reader));
            } catch (IOException | RuntimeException exception) {
                EchoIndex.LOGGER.debug("Could not inspect Index source data {}.", resourceId, exception);
            }
        }
    }

    private static Set<Identifier> collectItemIds(JsonElement element) {
        Set<Identifier> ids = new LinkedHashSet<>();
        collectItemIds(element, ids);
        return ids;
    }

    private static void collectItemIds(JsonElement element, Set<Identifier> ids) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            Identifier id = Identifier.tryParse(element.getAsString());
            if (id != null && BuiltInRegistries.ITEM.getOptional(id).isPresent()) {
                ids.add(id);
            }
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectItemIds(child, ids);
            }
            return;
        }
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> child : element.getAsJsonObject().entrySet()) {
                collectItemIds(child.getValue(), ids);
            }
        }
    }

    private static Identifier blockLootItemId(Identifier resourceId) {
        String prefix = LOOT_TABLE_DIR + "/blocks/";
        String path = resourceId.getPath();
        if (!path.startsWith(prefix) || !path.endsWith(".json")) {
            return null;
        }
        Identifier itemId = Identifier.fromNamespaceAndPath(
                resourceId.getNamespace(),
                path.substring(prefix.length(), path.length() - ".json".length()));
        return BuiltInRegistries.ITEM.getOptional(itemId).isPresent() ? itemId : null;
    }

    private static boolean isBlockLoot(Identifier resourceId) {
        return resourceId.getPath().startsWith(LOOT_TABLE_DIR + "/blocks/");
    }

    private static List<String> lootNotes(Identifier resourceId, boolean blockLoot) {
        List<String> notes = new ArrayList<>();
        notes.add(blockLoot ? "Drops from a block loot table." : "Referenced by a loot table.");
        notes.add("Loot table: " + displayId(resourceId));
        return notes;
    }

    private static void add(Map<String, IndexSourceRecipeProvider.SourceFact> facts,
            IndexSourceRecipeProvider.SourceFact fact) {
        facts.putIfAbsent(fact.itemId() + "|" + fact.title() + "|" + fact.sourceId(), fact);
    }

    private static String displayId(Identifier resourceId) {
        String path = resourceId.getPath();
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        return resourceId.getNamespace() + ":" + path;
    }

    @FunctionalInterface
    private interface JsonConsumer {
        void accept(Identifier resourceId, JsonElement json);
    }
}
