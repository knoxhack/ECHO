package com.knoxhack.echoconvoyprotocol.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleKind;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
      List<ConvoyRouteDefinition.RouteLeg> legs = legArray(id, json);
      int threatLevel = boundedInteger(id, json, "threatLevel", 0, 0, 5);
      JsonObject checkpoint = object(json, "checkpoint");
      Identifier checkpointFaction = identifier(
         checkpoint,
         "factionId",
         identifier(json, "checkpointFactionId", Identifier.fromNamespaceAndPath("echoashfallprotocol", "crashbreak_salvage"))
      );
      int minReputation = nonNegativeInteger(id, checkpoint, "minReputation", nonNegativeInteger(id, json, "minReputation", 0));
      String requiredVehicle = string(json, "requiredVehicle", "any");
      int minFuel = nonNegativeInteger(id, json, "minFuel", 0);
      validateVehicleRequirement(id, requiredVehicle, minFuel);
      return new ConvoyRouteDefinition(
         id,
         string(json, "title", id.getPath()),
         string(json, "summary", ""),
         integer(json, "order", 0),
         requiredVehicle,
         minFuel,
         stackArray(id, json, "requiredCargo"),
         stackArray(id, json, "rewards"),
         threatLevel,
         threatSpec(id, json, threatLevel),
         checkpointFaction,
         minReputation,
         checkpointSpec(json, checkpoint),
         string(json, "destinationHint", "Overworld roadside corridor"),
         positiveInteger(id, json, "requiredSignalMarkers", legs.isEmpty() ? 1 : legs.size()),
         nonNegativeInteger(id, json, "minDistanceFromStart", maxLegDistance(legs, 24)),
         legs
      );
   }

   private static List<ConvoyRouteDefinition.RouteLeg> legArray(Identifier routeId, JsonObject json) {
      JsonArray array = array(json, "legs");
      if (array == null) {
         return List.of();
      }
      List<ConvoyRouteDefinition.RouteLeg> legs = new ArrayList<>();
      Set<String> seen = new HashSet<>();
      int index = 1;
      for (JsonElement element : array) {
         if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String legId = string(object, "id", "leg_" + index);
            if (!seen.add(legId)) {
               throw new JsonParseException("Convoy route " + routeId + " has duplicate leg id '" + legId + "'.");
            }
            legs.add(new ConvoyRouteDefinition.RouteLeg(
               legId,
               string(object, "title", "Roadside Signal " + index),
               nonNegativeInteger(routeId, object, "minDistanceFromStart", 0),
               bool(object, "requiresCheckpoint", false),
               identifier(object, "roadsideStructure", ConvoyRouteDefinition.RouteLeg.DEFAULT_ROADSIDE_STRUCTURE)
            ));
         }
         index++;
      }
      return legs;
   }

   private static ConvoyRouteDefinition.ThreatSpec threatSpec(Identifier routeId, JsonObject json, int threatLevel) {
      ConvoyRouteDefinition.ThreatSpec base = ConvoyRouteDefinition.ThreatSpec.fromThreatLevel(threatLevel);
      JsonObject threat = object(json, "threat");
      if (threat == null) {
         threat = object(json, "roadThreat");
      }
      if (threat == null) {
         return base;
      }
      int count = integer(threat, "count", -1);
      if (has(threat, "count") && count < 0) {
         throw new JsonParseException("Convoy route " + routeId + " threat.count must be non-negative.");
      }
      int minCount = nonNegativeInteger(routeId, threat, "minCount", count >= 0 ? count : base.minCount());
      int maxCount = nonNegativeInteger(routeId, threat, "maxCount", count >= 0 ? count : base.maxCount());
      if (maxCount < minCount) {
         throw new JsonParseException("Convoy route " + routeId + " threat.maxCount must be >= minCount.");
      }
      return new ConvoyRouteDefinition.ThreatSpec(
         string(threat, "label", base.label()),
         identifier(threat, "entity", base.entityType()),
         minCount,
         maxCount,
         nonNegativeInteger(routeId, threat, "chanceOneIn", nonNegativeInteger(routeId, threat, "chance", base.chanceOneIn())),
         positiveInteger(routeId, threat, "cooldownTicks", base.cooldownTicks()),
         nonNegativeInteger(routeId, threat, "vehicleDamage", base.vehicleDamage()),
         positiveDecimal(routeId, threat, "spawnRadius", base.spawnRadius()),
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

   private static List<ConvoyRouteDefinition.StackSpec> stackArray(Identifier routeId, JsonObject json, String key) {
      JsonArray array = array(json, key);
      if (array == null) {
         return List.of();
      }
      List<ConvoyRouteDefinition.StackSpec> stacks = new ArrayList<>();
      for (JsonElement element : array) {
         if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            Identifier itemId = requiredIdentifier(routeId, object, "item", key);
            validateItemReference(routeId, key, itemId);
            int count = positiveInteger(routeId, object, "count", 1);
            stacks.add(new ConvoyRouteDefinition.StackSpec(
               itemId,
               count
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

   private static boolean has(JsonObject json, String key) {
      return json != null && json.has(key) && !json.get(key).isJsonNull();
   }

   private static Identifier identifier(JsonObject json, String key, Identifier fallback) {
      String value = string(json, key, "");
      return value.isBlank() ? fallback : Identifier.parse(value);
   }

   private static Identifier requiredIdentifier(Identifier routeId, JsonObject json, String key, String context) {
      String value = string(json, key, "");
      if (value.isBlank()) {
         throw new JsonParseException("Convoy route " + routeId + " " + context + " entry is missing required '" + key + "'.");
      }
      try {
         return Identifier.parse(value);
      } catch (RuntimeException exception) {
         throw new JsonParseException("Convoy route " + routeId + " " + context + " has invalid identifier '" + value + "'.", exception);
      }
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

   private static int nonNegativeInteger(Identifier routeId, JsonObject json, String key, int fallback) {
      int value = integer(json, key, fallback);
      if (value < 0) {
         throw new JsonParseException("Convoy route " + routeId + " field '" + key + "' must be non-negative.");
      }
      return value;
   }

   private static int positiveInteger(Identifier routeId, JsonObject json, String key, int fallback) {
      int value = integer(json, key, fallback);
      if (value <= 0) {
         throw new JsonParseException("Convoy route " + routeId + " field '" + key + "' must be positive.");
      }
      return value;
   }

   private static int boundedInteger(Identifier routeId, JsonObject json, String key, int fallback, int min, int max) {
      int value = integer(json, key, fallback);
      if (value < min || value > max) {
         throw new JsonParseException("Convoy route " + routeId + " field '" + key + "' must be between " + min + " and " + max + ".");
      }
      return value;
   }

   private static double decimal(JsonObject json, String key, double fallback) {
      if (json == null) {
         return fallback;
      }
      JsonElement element = json.get(key);
      return element == null || element.isJsonNull() ? fallback : element.getAsDouble();
   }

   private static double positiveDecimal(Identifier routeId, JsonObject json, String key, double fallback) {
      double value = decimal(json, key, fallback);
      if (value <= 0.0D) {
         throw new JsonParseException("Convoy route " + routeId + " field '" + key + "' must be positive.");
      }
      return value;
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

   private static void validateItemReference(Identifier routeId, String key, Identifier itemId) {
      if (BuiltInRegistries.ITEM.getOptional(itemId).isEmpty() || BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.AIR) == Items.AIR) {
         throw new JsonParseException("Convoy route " + routeId + " " + key + " references missing or invalid item " + itemId + ".");
      }
   }

   private static void validateVehicleRequirement(Identifier routeId, String requiredVehicle, int minFuel) {
      String vehicle = requiredVehicle == null || requiredVehicle.isBlank() ? "any" : requiredVehicle.strip();
      int maxFuel = 0;
      boolean matched = "any".equals(vehicle);
      for (ConvoyVehicleKind kind : ConvoyVehicleKind.values()) {
         maxFuel = Math.max(maxFuel, kind.maxFuel());
         if (kind.getSerializedName().equals(vehicle)) {
            matched = true;
            if (minFuel > kind.maxFuel()) {
               throw new JsonParseException("Convoy route " + routeId + " requires fuel " + minFuel + " but " + vehicle + " only stores " + kind.maxFuel() + ".");
            }
         }
      }
      if (!matched) {
         throw new JsonParseException("Convoy route " + routeId + " has unknown requiredVehicle '" + vehicle + "'.");
      }
      if ("any".equals(vehicle) && minFuel > maxFuel) {
         throw new JsonParseException("Convoy route " + routeId + " requires fuel " + minFuel + " but no convoy vehicle can store that much.");
      }
   }
}
