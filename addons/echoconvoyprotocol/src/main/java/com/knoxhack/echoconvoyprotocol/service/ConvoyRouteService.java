package com.knoxhack.echoconvoyprotocol.service;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.content.ConvoyContent;
import com.knoxhack.echoconvoyprotocol.content.ConvoyRouteDefinition;
import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyStationBlockEntity;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleEntity;
import com.knoxhack.echoconvoyprotocol.progress.ConvoyProgress;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.WorldMarker;
import com.knoxhack.echocore.api.WorldMarkerType;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ConvoyRouteService {
   private ConvoyRouteService() {
   }

   public static boolean activateFirstRoute(Player player, ConvoyVehicleEntity vehicle) {
      return activateFirstRoute(player, vehicle, vehicle == null ? player.blockPosition() : vehicle.blockPosition());
   }

   public static boolean activateFirstRoute(Player player, ConvoyVehicleEntity vehicle, BlockPos startPos) {
      Optional<ConvoyRouteDefinition> route = ConvoyContent.firstRoute();
      return route.filter(definition -> activateRoute(player, vehicle, definition.id(), startPos)).isPresent();
   }

   public static boolean activateRoute(Player player, ConvoyVehicleEntity vehicle, Identifier routeId) {
      return activateRoute(player, vehicle, routeId, vehicle == null ? player.blockPosition() : vehicle.blockPosition());
   }

   public static boolean activateRoute(Player player, ConvoyVehicleEntity vehicle, Identifier routeId, BlockPos startPos) {
      Optional<ConvoyRouteDefinition> route = ConvoyContent.route(routeId);
      if (route.isEmpty()) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Route definition unavailable."));
         return false;
      }
      ConvoyRouteDefinition definition = route.get();
      RouteCheck check = readiness(player, vehicle, definition, false);
      if (!check.ready()) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Route blocked: " + check.message() + ". " + readinessHint(check, definition)));
         return false;
      }
      ConvoyProgress progress = ConvoyProgress.get(player);
      progress.activate(definition.id(), startPos, vehicle.getUUID());
      progress.activateBeacon(Identifier.fromNamespaceAndPath(definition.id().getNamespace(), "beacon/" + definition.id().getPath()));
      recordWorldMarker(player, definition, startPos, WorldMarkerType.ROUTE_START,
         "Convoy Start: " + definition.title(), "Convoy route start beacon.");
      vehicle.setActiveRouteId(definition.id().toString());
      player.sendSystemMessage(Component.literal(
         "ECHO CONVOY // Route active: " + definition.title()
            + ". Log " + definition.requiredSignalMarkers() + " roadside marker"
            + (definition.requiredSignalMarkers() == 1 ? "" : "s") + "."
      ));
      if (player instanceof ServerPlayer serverPlayer) {
         EchoCoreServices.discoverVisibleRouteRecords(serverPlayer);
      }
      return true;
   }

   public static boolean completeActiveRoute(Player player, ConvoyVehicleEntity vehicle) {
      player.sendSystemMessage(Component.literal("ECHO CONVOY // Active routes must be closed at Roadside Signal Markers."));
      return false;
   }

   public static boolean advanceRouteAtSignal(Player player, ConvoyVehicleEntity vehicle, BlockPos markerPos) {
      return advanceRouteAtSignal(player, vehicle, markerPos, null);
   }

   public static boolean advanceRouteAtSignal(
      Player player,
      ConvoyVehicleEntity vehicle,
      BlockPos markerPos,
      @Nullable ConvoyStationBlockEntity marker
   ) {
      ConvoyProgress progress = ConvoyProgress.get(player);
      Identifier routeId = Identifier.tryParse(progress.activeRouteId());
      if (routeId == null) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // No active route to advance."));
         return false;
      }
      Optional<ConvoyRouteDefinition> route = ConvoyContent.route(routeId);
      if (route.isEmpty()) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Active route definition missing."));
         return false;
      }
      ConvoyRouteDefinition definition = route.get();
      RouteCheck check = readiness(player, vehicle, definition, false);
      if (!check.ready()) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Route cannot close: " + check.message() + ". " + readinessHint(check, definition)));
         return false;
      }
      if (!routeVehicleMatches(progress, vehicle, definition)) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Route marker rejected: paired convoy vehicle required."));
         return false;
      }
      if (progress.markerVisited(definition.id(), markerPos)) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Roadside signal already logged for this route."));
         return false;
      }
      int nextLeg = progress.activeRouteLeg() + 1;
      ConvoyRouteDefinition.RouteLeg leg = definition.leg(progress.activeRouteLeg());
      if (marker != null && !marker.acceptsRouteLeg(definition.id(), leg)) {
         player.sendSystemMessage(Component.literal(
            "ECHO CONVOY // Roadside signal mismatch. Marker expects " + marker.markerRequirementLabel() + "."
         ));
         return false;
      }
      if (!legReached(progress, markerPos, leg)) {
         player.sendSystemMessage(Component.literal(
            "ECHO CONVOY // " + leg.title() + " too close. Push at least "
               + leg.minDistanceFromStart() + " blocks from the start beacon."
         ));
         return false;
      }
      if (leg.requiresCheckpoint() && !checkpointReady(player, definition, progress)) {
         return false;
      }
      if (nextLeg < definition.requiredSignalMarkers()) {
         progress.markSignal(definition.id(), markerPos);
         recordWorldMarker(player, definition, markerPos, WorldMarkerType.ROUTE_CHECKPOINT,
            "Convoy Checkpoint: " + leg.title(), "Roadside signal logged for " + definition.title() + ".");
         player.sendSystemMessage(Component.literal(
            "ECHO CONVOY // Route leg " + nextLeg + "/" + definition.requiredSignalMarkers()
               + " logged: " + definition.title() + "."
         ));
         if (player instanceof ServerPlayer serverPlayer) {
            EchoCoreServices.discoverVisibleRouteRecords(serverPlayer);
         }
         return true;
      }
      return finishRoute(player, vehicle, definition, progress, markerPos);
   }

   private static boolean finishRoute(
      Player player,
      ConvoyVehicleEntity vehicle,
      ConvoyRouteDefinition definition,
      ConvoyProgress progress,
      BlockPos markerPos
   ) {
      for (ConvoyRouteDefinition.StackSpec cargo : definition.requiredCargo()) {
         if (!vehicle.consumeCargo(cargo.item(), cargo.count())) {
            player.sendSystemMessage(Component.literal("ECHO CONVOY // Cargo manifest changed before turn-in."));
            return false;
         }
      }
      progress.markSignal(definition.id(), markerPos);
      progress.complete(definition.id());
      recordWorldMarker(player, definition, markerPos, WorldMarkerType.ROUTE_DESTINATION,
         "Convoy Destination: " + definition.title(), "Completed convoy route destination.");
      vehicle.setActiveRouteId("");
      player.sendSystemMessage(Component.literal("ECHO CONVOY // Contract complete: " + definition.title() + ". Rewards pending in Convoy Routes."));
      if (player instanceof ServerPlayer serverPlayer) {
         EchoCoreServices.discoverVisibleRouteRecords(serverPlayer);
      }
      return true;
   }

   public static boolean claimRouteRewards(Player player, Identifier routeId) {
      Optional<ConvoyRouteDefinition> route = ConvoyContent.route(routeId);
      if (route.isEmpty()) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Reward route not found."));
         return false;
      }
      ConvoyProgress progress = ConvoyProgress.get(player);
      ConvoyRouteDefinition definition = route.get();
      if (!progress.completed(definition.id())) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Route not complete."));
         return false;
      }
      if (progress.claimed(definition.id())) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Rewards already claimed for " + definition.title() + "."));
         return false;
      }
      for (ConvoyRouteDefinition.StackSpec reward : definition.rewards()) {
         give(player, reward.stack());
      }
      progress.markClaimed(definition.id());
      player.sendSystemMessage(Component.literal("ECHO CONVOY // Rewards claimed: " + definition.title() + "."));
      if (player instanceof ServerPlayer serverPlayer) {
         EchoCoreServices.discoverVisibleRouteRecords(serverPlayer);
      }
      return true;
   }

   public static RouteCheck readiness(Player player, ConvoyVehicleEntity vehicle, ConvoyRouteDefinition route) {
      return readiness(player, vehicle, route, true);
   }

   private static RouteCheck readiness(Player player, ConvoyVehicleEntity vehicle, ConvoyRouteDefinition route, boolean includeCheckpoint) {
      if (vehicle == null) {
         return new RouteCheck(false, "no convoy vehicle nearby");
      }
      if (!vehicle.isOwner(player)) {
         return new RouteCheck(false, "vehicle locked to another operator");
      }
      if (!route.acceptsVehicle(vehicle.kind())) {
         return new RouteCheck(false, "requires " + route.requiredVehicle().replace('_', ' '));
      }
      if (vehicle.fuel() < route.minFuel()) {
         return new RouteCheck(false, "fuel " + vehicle.fuel() + "/" + route.minFuel());
      }
      for (ConvoyRouteDefinition.StackSpec cargo : route.requiredCargo()) {
         if (vehicle.cargoItemCount(cargo.item()) < cargo.count()) {
            return new RouteCheck(false, "missing cargo " + cargo.itemId() + " x" + cargo.count());
         }
      }
      if (includeCheckpoint && route.minReputation() > 0 && !ConvoyProgress.get(player).checkpointCleared(route.id())) {
         int reputation = EchoCoreServices.factionProfile(player, route.checkpointFactionId())
            .map(profile -> profile.reputation())
            .orElse(Integer.MIN_VALUE);
         if (reputation < route.minReputation()) {
            return new RouteCheck(false, route.checkpoint().label() + " requires reputation " + route.minReputation());
         }
      }
      return new RouteCheck(true, "ready");
   }

   public static String readinessHint(Player player, ConvoyVehicleEntity vehicle, ConvoyRouteDefinition route) {
      return readinessHint(readiness(player, vehicle, route), route);
   }

   public static String readinessHint(RouteCheck check, ConvoyRouteDefinition route) {
      if (check == null || route == null) {
         return "Scan convoy routes again to refresh requirements.";
      }
      if (check.ready()) {
         return "Ready: start this route at a Convoy Beacon or from the Convoy tab.";
      }
      String message = check.message() == null ? "" : check.message();
      if (message.contains("no convoy vehicle")) {
         return "Craft a Vehicle Workbench, process a Scrap Bike Kit, deploy it, then stand near the vehicle.";
      }
      if (message.contains("locked")) {
         return "Use a vehicle you deployed or reclaim your own vehicle before starting.";
      }
      if (message.startsWith("requires ")) {
         return "Bring a " + route.requiredVehicle().replace('_', ' ') + " kit or choose a route for the current vehicle.";
      }
      if (message.startsWith("fuel ")) {
         return "Add Fuel Canisters directly or through a Vehicle Dock until fuel reaches " + route.minFuel() + ".";
      }
      if (message.startsWith("missing cargo ")) {
         return "Load " + cargoManifest(route) + " by sneak-using cargo on the vehicle or with a Cargo Anchor.";
      }
      if (message.contains("requires reputation")) {
         return "Build " + route.checkpoint().label() + " reputation or select an unguarded route first.";
      }
      return "Review route requirements in the Convoy tab, then scan again.";
   }

   public static String cargoManifest(ConvoyRouteDefinition route) {
      if (route == null || route.requiredCargo().isEmpty()) {
         return "the required cargo";
      }
      return route.requiredCargo().stream()
         .map(cargo -> cargo.count() + "x " + cargo.stack().getHoverName().getString())
         .reduce((left, right) -> left + ", " + right)
         .orElse("the required cargo");
   }

   public static long claimableRewards(Player player) {
      ConvoyProgress progress = ConvoyProgress.get(player);
      return ConvoyContent.routes().stream()
         .filter(route -> progress.completed(route.id()) && !progress.claimed(route.id()))
      .count();
   }

   private static void give(Player player, ItemStack stack) {
      if (!player.getInventory().add(stack.copy())) {
         player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()));
      }
   }

   private static void recordWorldMarker(
      Player player,
      ConvoyRouteDefinition route,
      BlockPos pos,
      WorldMarkerType type,
      String title,
      String summary
   ) {
      if (!(player instanceof ServerPlayer serverPlayer) || route == null || pos == null) {
         return;
      }
      Identifier markerId = Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID,
         "world_marker/" + route.id().getPath() + "/" + type.name().toLowerCase(java.util.Locale.ROOT)
            + "/" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
      WorldMarker marker = new WorldMarker(
         markerId,
         Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "convoy_route"),
         type,
         title,
         summary + " " + route.summary(),
         serverPlayer.level().dimension(),
         pos,
         96,
         true,
         serverPlayer.level().getGameTime()
      );
      EchoCoreServices.worldMarkerService().revealMarker(serverPlayer, marker);
      EchoCoreServices.structureDiscoveryService().recordStructureScan(
         serverPlayer,
         Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "convoy_route"),
         pos,
         title,
         route.summary()
      );
   }

   private static boolean routeVehicleMatches(ConvoyProgress progress, ConvoyVehicleEntity vehicle, ConvoyRouteDefinition definition) {
      Optional<UUID> activeVehicle = progress.activeRouteVehicle();
      if (activeVehicle.isPresent() && !activeVehicle.get().equals(vehicle.getUUID())) {
         return false;
      }
      return definition.id().toString().equals(vehicle.activeRouteId());
   }

   private static boolean checkpointReady(Player player, ConvoyRouteDefinition definition, ConvoyProgress progress) {
      if (definition.minReputation() <= 0 || progress.checkpointCleared(definition.id())) {
         progress.clearCheckpoint(definition.id());
         return true;
      }
      int reputation = EchoCoreServices.factionProfile(player, definition.checkpointFactionId())
         .map(profile -> profile.reputation())
         .orElse(Integer.MIN_VALUE);
      if (reputation < definition.minReputation()) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // " + definition.checkpoint().blockedMessage(definition, reputation)));
         return false;
      }
      progress.clearCheckpoint(definition.id());
      player.sendSystemMessage(Component.literal("ECHO CONVOY // " + definition.checkpoint().clearedMessage(definition)));
      return true;
   }

   private static boolean legReached(ConvoyProgress progress, BlockPos markerPos, ConvoyRouteDefinition.RouteLeg leg) {
      if (leg.minDistanceFromStart() <= 0) {
         return true;
      }
      Optional<BlockPos> start = progress.activeRouteStart();
      if (start.isEmpty()) {
         return false;
      }
      long dx = (long)markerPos.getX() - start.get().getX();
      long dz = (long)markerPos.getZ() - start.get().getZ();
      long required = (long)leg.minDistanceFromStart() * leg.minDistanceFromStart();
      return dx * dx + dz * dz >= required;
   }

   public record RouteCheck(boolean ready, String message) {
   }
}
