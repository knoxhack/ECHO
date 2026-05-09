package com.knoxhack.echoconvoyprotocol.content;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.registry.ModItems;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class ConvoyContent {
   private static final Map<Identifier, ConvoyRouteDefinition> DEFAULT_ROUTES = new LinkedHashMap<>();
   private static volatile Map<Identifier, ConvoyRouteDefinition> jsonRoutes = Map.of();

   static {
      defaults();
   }

   private ConvoyContent() {
   }

   public static void replaceJsonRoutes(Map<Identifier, ConvoyRouteDefinition> routes) {
      jsonRoutes = Map.copyOf(routes == null ? Map.of() : routes);
      EchoConvoyProtocol.LOGGER.info("ECHO Convoy loaded {} JSON convoy routes.", jsonRoutes.size());
   }

   public static List<ConvoyRouteDefinition> routes() {
      Map<Identifier, ConvoyRouteDefinition> merged = new LinkedHashMap<>(DEFAULT_ROUTES);
      merged.putAll(jsonRoutes);
      return merged.values().stream()
         .sorted(Comparator.comparingInt(ConvoyRouteDefinition::order).thenComparing(route -> route.id().toString()))
         .toList();
   }

   public static Optional<ConvoyRouteDefinition> route(Identifier id) {
      return id == null ? Optional.empty() : routes().stream().filter(route -> route.id().equals(id)).findFirst();
   }

   public static Optional<ConvoyRouteDefinition> firstRoute() {
      return routes().stream().findFirst();
   }

   public static void clearJsonForTests() {
      jsonRoutes = Map.of();
   }

   private static void defaults() {
      Identifier northern = id("northern_route");
      DEFAULT_ROUTES.put(northern, new ConvoyRouteDefinition(
         northern,
         "Open the Northern Route",
         "Scout the broken highway corridor and prove the Convoy Beacon chain is usable.",
         0,
         "scrap_bike",
         20,
         List.of(),
         List.of(new ConvoyRouteDefinition.StackSpec(Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "fuel_canister"), 1),
            new ConvoyRouteDefinition.StackSpec(Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "convoy_repair_kit"), 1)),
         1,
         Identifier.fromNamespaceAndPath("echocore", "survivors"),
         0,
         "Northern ruined highway",
         1,
         24
      ));
      Identifier salvager = id("salvager_escort");
      DEFAULT_ROUTES.put(salvager, new ConvoyRouteDefinition(
         salvager,
         "Escort a Salvager Convoy",
         "Move machine parts through a checkpoint and recover the cargo intact.",
         10,
         "wasteland_rover",
         60,
         List.of(new ConvoyRouteDefinition.StackSpec(Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "engine_core"), 1)),
         List.of(new ConvoyRouteDefinition.StackSpec(Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "armored_tire"), 2),
            new ConvoyRouteDefinition.StackSpec(Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "battery_cell"), 2)),
         3,
         Identifier.fromNamespaceAndPath("echocore", "survivors"),
         0,
         "Scavenger checkpoint road",
         2,
         48
      ));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, path);
   }
}
