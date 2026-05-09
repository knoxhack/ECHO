package com.knoxhack.echoconvoyprotocol.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class ConvoyJsonReloadListener extends SimplePreparableReloadListener<Map<Identifier, ConvoyRouteDefinition>> {
   private static final String ROUTE_DIR = "echoconvoyprotocol/convoy_routes";

   @Override
   protected Map<Identifier, ConvoyRouteDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
      Map<Identifier, ConvoyRouteDefinition> routes = new LinkedHashMap<>();
      for (Map.Entry<Identifier, Resource> entry : manager.listResources(ROUTE_DIR, id -> id.getPath().endsWith(".json")).entrySet()) {
         Identifier resourceId = entry.getKey();
         Identifier id = contentId(resourceId);
         try (Reader reader = entry.getValue().openAsReader()) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
               throw new JsonParseException("Root must be a JSON object.");
            }
            ConvoyRouteDefinition route = parseRoute(id, root.getAsJsonObject());
            if (routes.putIfAbsent(id, route) != null) {
               EchoConvoyProtocol.LOGGER.warn("Duplicate Convoy route id {} from {} ignored.", id, resourceId);
            }
         } catch (IOException | RuntimeException exception) {
            EchoConvoyProtocol.LOGGER.warn("Could not parse Convoy route file {}.", resourceId, exception);
         }
      }
      return routes;
   }

   @Override
   protected void apply(Map<Identifier, ConvoyRouteDefinition> routes, ResourceManager manager, ProfilerFiller profiler) {
      ConvoyContent.replaceJsonRoutes(routes);
   }

   public static ConvoyRouteDefinition parseRouteForTests(Identifier id, JsonObject json) {
      return parseRoute(id, json);
   }

   private static ConvoyRouteDefinition parseRoute(Identifier id, JsonObject json) {
      List<ConvoyRouteDefinition.RouteLeg> legs = legArray(json);
      int threatLevel = integer(json, "threatLevel", 0);
      JsonObject checkpoint = object(json, "checkpoint");
      Identifier checkpointFaction = identifier(
         checkpoint,
         "factionId",
         identifier(json, "checkpointFactionId", Identifier.fromNamespaceAndPath("echocore", "survivors"))
      );
      int minReputation = integer(checkpoint, "minReputation", integer(json, "minReputation", 0));
      return new ConvoyRouteDefinition(
         id,
         string(json, "title", id.getPath()),
         string(json, "summary", ""),
         integer(json, "order", 0),
         string(json, "requiredVehicle", "any"),
         integer(json, "minFuel", 0),
         stackArray(json, "requiredCargo"),
         stackArray(json, "rewards"),
         threatLevel,
         threatSpec(json, threatLevel),
         checkpointFaction,
         minReputation,
         checkpointSpec(json, checkpoint),
         string(json, "destinationHint", "Overworld roadside corridor"),
         integer(json, "requiredSignalMarkers", legs.isEmpty() ? 1 : legs.size()),
         integer(json, "minDistanceFromStart", maxLegDistance(legs, 24)),
         legs
      );
   }

   private static List<ConvoyRouteDefinition.RouteLeg> legArray(JsonObject json) {
      JsonArray array = array(json, "legs");
      if (array == null) {
         return List.of();
      }
      List<ConvoyRouteDefinition.RouteLeg> legs = new ArrayList<>();
      int index = 1;
      for (JsonElement element : array) {
         if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            legs.add(new ConvoyRouteDefinition.RouteLeg(
               string(object, "id", "leg_" + index),
               string(object, "title", "Roadside Signal " + index),
               integer(object, "minDistanceFromStart", 0),
               bool(object, "requiresCheckpoint", false),
               identifier(object, "roadsideStructure", ConvoyRouteDefinition.RouteLeg.DEFAULT_ROADSIDE_STRUCTURE)
            ));
         }
         index++;
      }
      return legs;
   }

   private static ConvoyRouteDefinition.ThreatSpec threatSpec(JsonObject json, int threatLevel) {
      ConvoyRouteDefinition.ThreatSpec base = ConvoyRouteDefinition.ThreatSpec.fromThreatLevel(threatLevel);
      JsonObject threat = object(json, "threat");
      if (threat == null) {
         threat = object(json, "roadThreat");
      }
      if (threat == null) {
         return base;
      }
      int count = integer(threat, "count", -1);
      return new ConvoyRouteDefinition.ThreatSpec(
         string(threat, "label", base.label()),
         identifier(threat, "entity", base.entityType()),
         integer(threat, "minCount", count >= 0 ? count : base.minCount()),
         integer(threat, "maxCount", count >= 0 ? count : base.maxCount()),
         integer(threat, "chanceOneIn", integer(threat, "chance", base.chanceOneIn())),
         integer(threat, "cooldownTicks", base.cooldownTicks()),
         integer(threat, "vehicleDamage", base.vehicleDamage()),
         decimal(threat, "spawnRadius", base.spawnRadius()),
         string(threat, "warning", base.warning())
      );
   }

   private static ConvoyRouteDefinition.CheckpointSpec checkpointSpec(JsonObject json, JsonObject checkpoint) {
      JsonObject source = checkpoint == null ? json : checkpoint;
      return new ConvoyRouteDefinition.CheckpointSpec(
         string(source, "checkpointLabel", string(source, "label", "Faction Checkpoint")),
         string(source, "checkpointWarning", string(source, "warning", "")),
         string(source, "checkpointClearedMessage", string(source, "clearedMessage", ""))
      );
   }

   private static List<ConvoyRouteDefinition.StackSpec> stackArray(JsonObject json, String key) {
      JsonArray array = array(json, key);
      if (array == null) {
         return List.of();
      }
      List<ConvoyRouteDefinition.StackSpec> stacks = new ArrayList<>();
      for (JsonElement element : array) {
         if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            stacks.add(new ConvoyRouteDefinition.StackSpec(
               identifier(object, "item", Identifier.withDefaultNamespace("air")),
               integer(object, "count", 1)
            ));
         }
      }
      return stacks;
   }

   private static JsonObject object(JsonObject json, String key) {
      if (json == null) {
         return null;
      }
      JsonElement element = json.get(key);
      return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
   }

   private static Identifier contentId(Identifier resourceId) {
      String path = resourceId.getPath();
      String prefix = ROUTE_DIR + "/";
      if (path.startsWith(prefix)) {
         path = path.substring(prefix.length());
      }
      if (path.endsWith(".json")) {
         path = path.substring(0, path.length() - ".json".length());
      }
      return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), path);
   }

   private static JsonArray array(JsonObject json, String key) {
      JsonElement element = json.get(key);
      return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
   }

   private static Identifier identifier(JsonObject json, String key, Identifier fallback) {
      String value = string(json, key, "");
      return value.isBlank() ? fallback : Identifier.parse(value);
   }

   private static String string(JsonObject json, String key, String fallback) {
      if (json == null) {
         return fallback;
      }
      JsonElement element = json.get(key);
      return element == null || element.isJsonNull() ? fallback : element.getAsString();
   }

   private static int integer(JsonObject json, String key, int fallback) {
      if (json == null) {
         return fallback;
      }
      JsonElement element = json.get(key);
      return element == null || element.isJsonNull() ? fallback : element.getAsInt();
   }

   private static double decimal(JsonObject json, String key, double fallback) {
      if (json == null) {
         return fallback;
      }
      JsonElement element = json.get(key);
      return element == null || element.isJsonNull() ? fallback : element.getAsDouble();
   }

   private static boolean bool(JsonObject json, String key, boolean fallback) {
      JsonElement element = json.get(key);
      return element == null || element.isJsonNull() ? fallback : element.getAsBoolean();
   }

   private static int maxLegDistance(List<ConvoyRouteDefinition.RouteLeg> legs, int fallback) {
      return legs.stream()
         .mapToInt(ConvoyRouteDefinition.RouteLeg::minDistanceFromStart)
         .max()
         .orElse(fallback);
   }
}
