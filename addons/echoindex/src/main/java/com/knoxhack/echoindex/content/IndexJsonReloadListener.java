package com.knoxhack.echoindex.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echocore.api.index.IndexEntryState;
import com.knoxhack.echoindex.EchoIndex;
import com.knoxhack.echoindex.service.IndexService;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IndexJsonReloadListener extends SimplePreparableReloadListener<IndexJsonReloadListener.Payload> {
    private static final String CATEGORY_DIR = "echo_index/categories";
    private static final String ENTRY_DIR = "echo_index/entries";

    @Override
    protected Payload prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, IndexCategory> categories = new LinkedHashMap<>();
        Map<Identifier, IndexEntry> entries = new LinkedHashMap<>();
        read(manager, CATEGORY_DIR, (resourceId, json) -> {
            IndexCategory category = parseCategory(resourceId, json);
            categories.put(category.id(), category);
        });
        read(manager, ENTRY_DIR, (resourceId, json) -> {
            IndexEntry entry = parseEntry(resourceId, json);
            entries.put(entry.id(), entry);
        });
        return new Payload(categories, entries);
    }

    @Override
    protected void apply(Payload payload, ResourceManager manager, ProfilerFiller profiler) {
        IndexService.INSTANCE.replaceDataDriven(payload.categories(), payload.entries());
    }

    public static IndexCategory parseCategoryForTests(Identifier resourceId, JsonObject json) {
        return parseCategory(resourceId, json);
    }

    public static IndexEntry parseEntryForTests(Identifier resourceId, JsonObject json) {
        return parseEntry(resourceId, json);
    }

    private static void read(ResourceManager manager, String directory, JsonConsumer consumer) {
        for (Map.Entry<Identifier, Resource> entry : manager.listResources(directory, id -> id.getPath().endsWith(".json")).entrySet()) {
            Identifier resourceId = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonObject()) {
                    throw new JsonParseException("Root must be a JSON object.");
                }
                consumer.accept(resourceId, root.getAsJsonObject());
            } catch (IOException | RuntimeException exception) {
                EchoIndex.LOGGER.warn("Could not parse Index definition {}.", resourceId, exception);
            }
        }
    }

    private static IndexCategory parseCategory(Identifier resourceId, JsonObject json) {
        Identifier id = identifier(json, "id", contentId(resourceId, CATEGORY_DIR));
        return new IndexCategory(
                id,
                string(json, "title", "echoindex.category." + id.getPath().replace('/', '.')),
                string(json, "description", ""),
                stack(json, "icon"),
                integer(json, "sort_order", integer(json, "sortOrder", 100)),
                string(json, "source_mod", string(json, "sourceMod", id.getNamespace())));
    }

    private static IndexEntry parseEntry(Identifier resourceId, JsonObject json) {
        Identifier id = identifier(json, "id", contentId(resourceId, ENTRY_DIR));
        Identifier category = identifier(json, "category", Identifier.fromNamespaceAndPath(EchoIndex.MODID, "tutorials"));
        return new IndexEntry(
                id,
                category,
                string(json, "title", "echoindex.entry." + id.getPath().replace('/', '.')),
                string(json, "subtitle", ""),
                string(json, "summary", ""),
                string(json, "body", ""),
                stack(json, "icon"),
                string(json, "source_mod", string(json, "sourceMod", id.getNamespace())),
                stringList(json, "tags"),
                state(json, "default_state", state(json, "defaultState", IndexEntryState.VISIBLE)),
                identifiers(json, "related"),
                identifiers(json, "linked_items"),
                identifiers(json, "linked_recipes"),
                integer(json, "sort_order", integer(json, "sortOrder", 100)));
    }

    private static Identifier contentId(Identifier resourceId, String directory) {
        String path = resourceId.getPath();
        if (path.startsWith(directory + "/")) {
            path = path.substring(directory.length() + 1);
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), path);
    }

    private static ItemStack stack(JsonObject json, String key) {
        Identifier id = identifier(json, key, null);
        Item item = id == null ? Items.BOOK : BuiltInRegistries.ITEM.getOptional(id).orElse(Items.BOOK);
        return new ItemStack(item);
    }

    private static List<String> stringList(JsonObject json, String key) {
        JsonArray array = array(json, key);
        if (array == null) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                String value = element.getAsString();
                if (!value.isBlank()) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private static List<Identifier> identifiers(JsonObject json, String key) {
        JsonArray array = array(json, key);
        if (array == null) {
            return List.of();
        }
        List<Identifier> values = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                Identifier id = Identifier.tryParse(element.getAsString());
                if (id != null) {
                    values.add(id);
                }
            }
        }
        return values;
    }

    private static IndexEntryState state(JsonObject json, String key, IndexEntryState fallback) {
        String value = string(json, key, "");
        if (value.isBlank()) {
            return fallback;
        }
        try {
            return IndexEntryState.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private static Identifier identifier(JsonObject json, String key, Identifier fallback) {
        String value = string(json, key, "");
        Identifier id = value.isBlank() ? null : Identifier.tryParse(value);
        return id == null ? fallback : id;
    }

    private static String string(JsonObject json, String key, String fallback) {
        JsonElement element = json == null ? null : json.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : fallback;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        JsonElement element = json == null ? null : json.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsInt() : fallback;
    }

    private static JsonArray array(JsonObject json, String key) {
        JsonElement element = json == null ? null : json.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    @FunctionalInterface
    private interface JsonConsumer {
        void accept(Identifier resourceId, JsonObject json);
    }

    public record Payload(Map<Identifier, IndexCategory> categories, Map<Identifier, IndexEntry> entries) {
    }
}
