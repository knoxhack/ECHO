package com.knoxhack.echorelictech.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.knoxhack.echorelictech.EchoRelicTech;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.*;

public class RelicVaultLoader extends SimplePreparableReloadListener<Map<Identifier, RelicVaultInfo>> {
    private static final Map<Identifier, RelicVaultInfo> VAULTS = new HashMap<>();
    private static final String DIR = "relic_vaults";

    @Override
    protected Map<Identifier, RelicVaultInfo> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, RelicVaultInfo> result = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : manager.listResources(DIR, id -> id.getPath().endsWith(".json")).entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                if (root.isJsonObject()) {
                    var obj = root.getAsJsonObject();
                    String idStr = obj.has("id") ? obj.get("id").getAsString() : entry.getKey().toString();
                    String displayName = obj.has("displayName") ? obj.get("displayName").getAsString() : idStr;
                    String tier = obj.has("tier") ? obj.get("tier").getAsString() : "FIELD";
                    String lootTable = obj.has("lootTable") ? obj.get("lootTable").getAsString() : "";
                    String materialLootTable = obj.has("materialLootTable") ? obj.get("materialLootTable").getAsString() : "";
                    String securityLevel = obj.has("securityLevel") ? obj.get("securityLevel").getAsString() : "LOW";
                    result.put(Identifier.parse(idStr), new RelicVaultInfo(idStr, displayName, tier, lootTable, materialLootTable, securityLevel));
                }
            } catch (Exception e) {
                EchoRelicTech.LOGGER.error("Failed to parse relic vault {}", entry.getKey(), e);
            }
        }
        return result;
    }

    @Override
    protected void apply(Map<Identifier, RelicVaultInfo> map, ResourceManager manager, ProfilerFiller profiler) {
        VAULTS.clear();
        VAULTS.putAll(map);
        EchoRelicTech.LOGGER.info("Loaded {} relic vault definitions.", VAULTS.size());
    }

    public static RelicVaultInfo get(Identifier id) {
        return VAULTS.get(id);
    }

    public static List<RelicVaultInfo> all() {
        return List.copyOf(VAULTS.values());
    }

}
