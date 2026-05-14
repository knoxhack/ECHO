package com.knoxhack.echorelictech.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.api.relic.RelicDefinition;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RelicDefinitionLoader extends SimplePreparableReloadListener<Map<Identifier, RelicDefinition>> {
    private static final Map<Identifier, RelicDefinition> DEFINITIONS = new HashMap<>();
    private static final String DIR = "relics";

    @Override
    protected Map<Identifier, RelicDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, RelicDefinition> result = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : manager.listResources(DIR, id -> id.getPath().endsWith(".json")).entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                if (root.isJsonObject()) {
                    var parseResult = RelicDefinition.CODEC.parse(JsonOps.INSTANCE, root);
                    if (parseResult.isSuccess()) {
                        RelicDefinition def = parseResult.getOrThrow();
                        result.put(def.id(), def);
                    } else {
                        EchoRelicTech.LOGGER.error("Failed to parse relic definition {}: {}", entry.getKey(), parseResult.error().map(e -> e.message()).orElse("unknown"));
                    }
                }
            } catch (Exception e) {
                EchoRelicTech.LOGGER.error("Exception parsing relic definition {}", entry.getKey(), e);
            }
        }
        return result;
    }

    @Override
    protected void apply(Map<Identifier, RelicDefinition> map, ResourceManager manager, ProfilerFiller profiler) {
        DEFINITIONS.clear();
        DEFINITIONS.putAll(map);
        EchoRelicTech.LOGGER.info("Loaded {} relic definitions.", DEFINITIONS.size());
    }

    public static RelicDefinition get(Identifier id) {
        return DEFINITIONS.get(id);
    }

    public static Map<Identifier, RelicDefinition> all() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
