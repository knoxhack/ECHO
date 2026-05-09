package com.knoxhack.echoconvoyprotocol.test;

import com.google.gson.JsonParser;
import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyStationBlockEntity;
import com.knoxhack.echoconvoyprotocol.content.ConvoyContent;
import com.knoxhack.echoconvoyprotocol.content.ConvoyJsonReloadListener;
import com.knoxhack.echoconvoyprotocol.content.ConvoyRouteDefinition;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleEntity;
import com.knoxhack.echoconvoyprotocol.integration.ConvoyCoreIntegration;
import com.knoxhack.echoconvoyprotocol.integration.ConvoyTerminalCommonIntegration;
import com.knoxhack.echoconvoyprotocol.integration.ConvoyTerminalIds;
import com.knoxhack.echoconvoyprotocol.network.ConvoyTerminalClientState;
import com.knoxhack.echoconvoyprotocol.network.ConvoyTerminalStatePacket;
import com.knoxhack.echoconvoyprotocol.progress.ConvoyProgress;
import com.knoxhack.echoconvoyprotocol.registry.ModBlocks;
import com.knoxhack.echoconvoyprotocol.registry.ModEntities;
import com.knoxhack.echoconvoyprotocol.registry.ModItems;
import com.knoxhack.echoconvoyprotocol.service.ConvoyRouteService;
import com.knoxhack.echocore.api.EchoDiagnosticBlocker;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDiscoveryCategory;
import com.knoxhack.echocore.api.EchoDiscoveryEntry;
import com.knoxhack.echocore.api.EchoDiscoveryProvider;
import com.knoxhack.echocore.api.EchoDiscoveryState;
import com.knoxhack.echocore.api.EchoRouteRecord;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
   private static final String PROGRESS_ROOT = "echoconvoyprotocol";

   private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(Registries.TEST_FUNCTION, EchoConvoyProtocol.MODID);

   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MODULE_REGISTRATION =
      TEST_FUNCTIONS.register("module_registration", () -> ModGameTests::moduleRegistration);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTE_PARSER =
      TEST_FUNCTIONS.register("route_parser", () -> ModGameTests::routeParser);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VEHICLE_STATE =
      TEST_FUNCTIONS.register("vehicle_state", () -> ModGameTests::vehicleState);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FUEL_CONSUMPTION =
      TEST_FUNCTIONS.register("fuel_consumption", () -> ModGameTests::fuelConsumption);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STATION_REPAIR =
      TEST_FUNCTIONS.register("station_repair", () -> ModGameTests::stationRepair);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTE_COMPLETION =
      TEST_FUNCTIONS.register("route_completion", () -> ModGameTests::routeCompletion);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTE_DISCOVERY =
      TEST_FUNCTIONS.register("route_discovery", () -> ModGameTests::routeDiscovery);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHECKPOINT_GATING =
      TEST_FUNCTIONS.register("checkpoint_gating", () -> ModGameTests::checkpointGating);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHECKPOINT_PROGRESS =
      TEST_FUNCTIONS.register("checkpoint_progress", () -> ModGameTests::checkpointProgress);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MARKER_MISMATCH =
      TEST_FUNCTIONS.register("marker_mismatch", () -> ModGameTests::markerMismatch);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STATION_OWNERSHIP =
      TEST_FUNCTIONS.register("station_ownership", () -> ModGameTests::stationOwnership);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_WIRING =
      TEST_FUNCTIONS.register("core_wiring", () -> ModGameTests::coreWiring);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_ACTIONS =
      TEST_FUNCTIONS.register("terminal_actions", () -> ModGameTests::terminalActions);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_SNAPSHOT =
      TEST_FUNCTIONS.register("terminal_snapshot", () -> ModGameTests::terminalSnapshot);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_ROUTE_SELECTION =
      TEST_FUNCTIONS.register("terminal_route_selection", () -> ModGameTests::terminalRouteSelection);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CALLSIGN_SHIELDING_PERSISTENCE =
      TEST_FUNCTIONS.register("callsign_shielding_persistence", () -> ModGameTests::callsignShieldingPersistence);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SCANNER_ROUTE_UTILITY =
      TEST_FUNCTIONS.register("scanner_route_utility", () -> ModGameTests::scannerRouteUtility);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RELAY_DEPLOYMENT =
      TEST_FUNCTIONS.register("relay_deployment", () -> ModGameTests::relayDeployment);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_ROUTE_TELEMETRY =
      TEST_FUNCTIONS.register("core_route_telemetry", () -> ModGameTests::coreRouteTelemetry);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDGEN_RESOURCE_COVERAGE =
      TEST_FUNCTIONS.register("worldgen_resource_coverage", () -> ModGameTests::worldgenResourceCoverage);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PRODUCTION_RELOAD_SOAK =
      TEST_FUNCTIONS.register("production_reload_soak", () -> ModGameTests::productionReloadSoak);

   private ModGameTests() {
   }

   public static void register(IEventBus eventBus) {
      TEST_FUNCTIONS.register(eventBus);
   }

   public static void registerTests(RegisterGameTestsEvent event) {
      Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("convoy_protocol"));
      register(event, environment, "module_registration", MODULE_REGISTRATION.getId());
      register(event, environment, "route_parser", ROUTE_PARSER.getId());
      register(event, environment, "vehicle_state", VEHICLE_STATE.getId());
      register(event, environment, "fuel_consumption", FUEL_CONSUMPTION.getId());
      register(event, environment, "station_repair", STATION_REPAIR.getId());
      register(event, environment, "route_completion", ROUTE_COMPLETION.getId());
      register(event, environment, "route_discovery", ROUTE_DISCOVERY.getId());
      register(event, environment, "checkpoint_gating", CHECKPOINT_GATING.getId());
      register(event, environment, "checkpoint_progress", CHECKPOINT_PROGRESS.getId());
      register(event, environment, "marker_mismatch", MARKER_MISMATCH.getId());
      register(event, environment, "station_ownership", STATION_OWNERSHIP.getId());
      register(event, environment, "core_wiring", CORE_WIRING.getId());
      register(event, environment, "terminal_actions", TERMINAL_ACTIONS.getId());
      register(event, environment, "terminal_snapshot", TERMINAL_SNAPSHOT.getId());
      register(event, environment, "terminal_route_selection", TERMINAL_ROUTE_SELECTION.getId());
      register(event, environment, "callsign_shielding_persistence", CALLSIGN_SHIELDING_PERSISTENCE.getId());
      register(event, environment, "scanner_route_utility", SCANNER_ROUTE_UTILITY.getId());
      register(event, environment, "relay_deployment", RELAY_DEPLOYMENT.getId());
      register(event, environment, "core_route_telemetry", CORE_ROUTE_TELEMETRY.getId());
      register(event, environment, "worldgen_resource_coverage", WORLDGEN_RESOURCE_COVERAGE.getId());
      register(event, environment, "production_reload_soak", PRODUCTION_RELOAD_SOAK.getId());
   }

   private static void moduleRegistration(GameTestHelper helper) {
      helper.assertTrue(ModBlocks.VEHICLE_WORKBENCH.get() != Blocks.AIR, "Vehicle Workbench should be registered");
      helper.assertTrue(ModItems.SCRAP_BIKE_KIT.get() != Items.AIR, "Scrap Bike Kit should be registered");
      helper.assertTrue(ModEntities.SCRAP_BIKE.get() != null, "Scrap Bike entity should be registered");
      helper.assertTrue(ModEntities.ARMORED_RELAY_TRUCK.get() != null, "Armored Relay Truck entity should be registered");
      helper.succeed();
   }

   private static void routeParser(GameTestHelper helper) {
      ConvoyRouteDefinition route = ConvoyJsonReloadListener.parseRouteForTests(id("parser_route"),
         JsonParser.parseString("{\"title\":\"Parser Route\",\"requiredVehicle\":\"scrap_bike\",\"minFuel\":7,\"requiredCargo\":[{\"item\":\"minecraft:apple\",\"count\":2}],\"rewards\":[{\"item\":\"minecraft:bread\",\"count\":1}],\"threatLevel\":2,\"legs\":[{\"id\":\"checkpoint\",\"title\":\"Parser Checkpoint\",\"minDistanceFromStart\":16,\"requiresCheckpoint\":true,\"roadsideStructure\":\"echoconvoyprotocol:roadside/cargo_checkpoint\"}]}").getAsJsonObject());
      helper.assertTrue(route.minFuel() == 7, "Route parser should load minFuel");
      helper.assertTrue(route.requiredCargo().size() == 1 && route.requiredCargo().get(0).count() == 2, "Route parser should load required cargo");
      helper.assertTrue(route.rewards().size() == 1 && route.threatLevel() == 2, "Route parser should load rewards and threats");
      helper.assertTrue(route.legs().size() == 1 && route.requiredSignalMarkers() == 1, "Route parser should derive signal markers from legs");
      helper.assertTrue(route.leg(0).requiresCheckpoint() && route.leg(0).minDistanceFromStart() == 16, "Route parser should load checkpoint leg constraints");
      helper.assertTrue(route.leg(0).roadsideStructure().equals(id("roadside/cargo_checkpoint")), "Route parser should load roadside structure ids");
      helper.succeed();
   }

   private static void vehicleState(GameTestHelper helper) {
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(1, 2, 1));
      vehicle.insertCargo(new ItemStack(Items.APPLE, 3));
      vehicle.applyVehicleDamage(12);
      helper.assertTrue(vehicle.cargoItemCount(Items.APPLE) == 3, "Cargo should be retained in vehicle inventory");
      helper.assertTrue(vehicle.damage() == 12, "Vehicle damage should increase");
      vehicle.repair(5);
      helper.assertTrue(vehicle.damage() == 7, "Vehicle repair should reduce damage");
      helper.succeed();
   }

   private static void fuelConsumption(GameTestHelper helper) {
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(1, 2, 1));
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      player.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
      helper.assertTrue(player.startRiding(vehicle, true, true), "Mock player should mount the vehicle");
      int fuel = vehicle.fuel();
      for (int i = 0; i < 25; i++) {
         player.zza = 1.0F;
         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setLastClientInput(new Input(true, false, false, false, false, false, false));
         }
         vehicle.tick();
      }
      helper.assertTrue(vehicle.fuel() < fuel, "Driving should consume fuel");
      helper.succeed();
   }

   private static void stationRepair(GameTestHelper helper) {
      BlockPos stationPos = new BlockPos(1, 1, 1);
      helper.setBlock(stationPos, (Block)ModBlocks.FIELD_REPAIR_STATION.get());
      ConvoyStationBlockEntity station = helper.getBlockEntity(stationPos, ConvoyStationBlockEntity.class);
      station.setItem(ConvoyStationBlockEntity.INPUT_SLOT, new ItemStack(ModItems.CONVOY_REPAIR_KIT.get()));
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(2, 2, 1));
      vehicle.applyVehicleDamage(30);
      helper.runAfterDelay(25L, () -> {
         helper.assertTrue(vehicle.damage() < 30, "Repair station should repair nearby vehicle");
         helper.succeed();
      });
   }

   private static void routeCompletion(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(1, 2, 1));
      boolean activated = ConvoyRouteService.activateRoute(player, vehicle, id("northern_route"));
      boolean completed = ConvoyRouteService.advanceRouteAtSignal(player, vehicle, helper.absolutePos(new BlockPos(40, 2, 1)));
      boolean claimed = ConvoyRouteService.claimRouteRewards(player, id("northern_route"));
      boolean duplicateClaim = ConvoyRouteService.claimRouteRewards(player, id("northern_route"));
      helper.assertTrue(activated && completed && claimed, "Northern route should activate, complete, and claim rewards");
      helper.assertTrue(!duplicateClaim, "Route rewards should not duplicate");
      helper.succeed();
   }

   private static void routeDiscovery(GameTestHelper helper) {
      EchoCoreServices.clearPlatformServicesForTests();
      ConvoyCoreIntegration.registerAddonChapter();
      registerRouteDiscoveryProviderForTests();
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(1, 2, 1));
      Identifier routeId = id("northern_route");
      Identifier discoveryId = EchoCoreServices.routeDiscoveryId(ConvoyTerminalIds.id("route/" + routeId.getPath()));

      helper.assertFalse(ConvoyRouteService.activateRoute(player, null, routeId),
         "Blocked route starts should fail before discovery is recorded");
      helper.assertFalse(EchoCoreServices.hasDiscoveredFeature(player, discoveryId),
         "Blocked route starts should not persist route discovery");

      helper.assertTrue(ConvoyRouteService.activateRoute(player, vehicle, routeId),
         "Valid route starts should succeed");
      helper.assertTrue(EchoCoreServices.hasDiscoveredFeature(player, discoveryId),
         "Valid route starts should persist the matching route discovery id");
      helper.assertFalse(EchoCoreServices.discoverFeature(player, discoveryId),
         "Duplicate route discovery should not fire a second time");

      helper.assertTrue(ConvoyRouteService.advanceRouteAtSignal(player, vehicle, helper.absolutePos(new BlockPos(40, 2, 1))),
         "Completing a discovered route at a distant route signal should succeed");
      helper.assertTrue(ConvoyRouteService.claimRouteRewards(player, routeId),
         "Claiming completed route rewards should succeed once");
      helper.assertFalse(ConvoyRouteService.claimRouteRewards(player, routeId),
         "Duplicate route reward claims should stay blocked");
      helper.assertFalse(EchoCoreServices.discoverFeature(player, discoveryId),
         "Completion and claim hooks should not duplicate discovery state");

      helper.getLevel().getServer().getPlayerList().remove(player);
      EchoCoreServices.clearPlatformServicesForTests();
      helper.succeed();
   }

   private static void checkpointGating(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(1, 2, 1));
      ConvoyRouteDefinition gated = new ConvoyRouteDefinition(id("gated"), "Gated", "", 0, "scrap_bike", 0, List.of(), List.of(), 0,
         Identifier.fromNamespaceAndPath("echocore", "survivors"), 50, "");
      helper.assertTrue(!ConvoyRouteService.readiness(player, vehicle, gated).ready(), "Faction checkpoint should block low reputation");
      helper.succeed();
   }

   private static void checkpointProgress(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity vehicle = spawnVehicle(helper, ModEntities.WASTELAND_ROVER.get(), new BlockPos(1, 2, 1));
      vehicle.insertCargo(new ItemStack(ModItems.ENGINE_CORE.get()));
      Identifier routeId = id("salvager_escort");
      helper.assertTrue(ConvoyRouteService.activateRoute(player, vehicle, routeId, helper.absolutePos(new BlockPos(-32, 1, 1))),
         "Checkpoint route should activate");
      helper.assertTrue(ConvoyRouteService.advanceRouteAtSignal(player, vehicle, helper.absolutePos(new BlockPos(3, 2, 1))),
         "Checkpoint leg should complete");
      ConvoyProgress progress = ConvoyProgress.get(player);
      helper.assertTrue(progress.checkpointCleared(routeId), "Checkpoint clear state should persist");
      helper.assertTrue(progress.activeRouteLeg() == 1, "Checkpoint route should advance to the destination leg");
      helper.assertFalse(progress.completed(routeId), "Checkpoint leg alone should not complete the full route");
      helper.succeed();
   }

   private static void markerMismatch(GameTestHelper helper) {
      BlockPos markerPos = new BlockPos(2, 1, 2);
      helper.setBlock(markerPos, (Block)ModBlocks.ROADSIDE_SIGNAL_MARKER.get());
      ConvoyStationBlockEntity marker = helper.getBlockEntity(markerPos, ConvoyStationBlockEntity.class);
      marker.setMarkerMetadata(null, null, id("roadside/cargo_checkpoint"));

      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(1, 2, 1));
      Identifier routeId = id("northern_route");
      helper.assertTrue(ConvoyRouteService.activateRoute(player, vehicle, routeId), "Northern route should activate");
      helper.assertFalse(ConvoyRouteService.advanceRouteAtSignal(player, vehicle, helper.absolutePos(new BlockPos(40, 2, 1)), marker),
         "Cargo checkpoint marker should not satisfy a signal marker leg");
      ConvoyProgress progress = ConvoyProgress.get(player);
      helper.assertFalse(progress.completed(routeId), "Mismatched marker should not complete the route");
      helper.assertTrue(progress.activeRouteLeg() == 0, "Mismatched marker should not advance the route leg");
      helper.succeed();
   }

   private static void stationOwnership(GameTestHelper helper) {
      BlockPos stationPos = new BlockPos(1, 1, 1);
      helper.setBlock(stationPos, (Block)ModBlocks.FIELD_REPAIR_STATION.get());
      ConvoyStationBlockEntity station = helper.getBlockEntity(stationPos, ConvoyStationBlockEntity.class);
      station.setItem(ConvoyStationBlockEntity.INPUT_SLOT, new ItemStack(ModItems.CONVOY_REPAIR_KIT.get()));

      Player owner = helper.makeMockPlayer(GameType.CREATIVE);
      Player other = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(2, 2, 1));
      owner.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
      other.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
      vehicle.interact(owner, InteractionHand.MAIN_HAND, vehicle.position());
      helper.assertFalse(vehicle.isOwner(other), "Claimed vehicle should reject a different operator");

      vehicle.applyVehicleDamage(30);
      station.linkOwner(other);
      helper.runAfterDelay(45L, () -> {
         helper.assertTrue(vehicle.damage() == 30, "Station linked to another operator should not repair the vehicle");
         station.linkOwner(owner);
         helper.runAfterDelay(45L, () -> {
            helper.assertTrue(vehicle.damage() < 30, "Station linked to owner should repair the vehicle");
            helper.succeed();
         });
      });
   }

   private static void coreWiring(GameTestHelper helper) {
      ConvoyCoreIntegration.registerAddonChapter();
      helper.assertTrue(com.knoxhack.echocore.api.EchoAddonRegistry.isRegistered(ConvoyCoreIntegration.CHAPTER_ID),
         "Convoy should register as an ECHO addon chapter");
      helper.succeed();
   }

   private static void terminalActions(GameTestHelper helper) {
      TerminalActionRegistry.withClearedForTests(() -> {
         ConvoyTerminalCommonIntegration.register();
         boolean handled = TerminalActionRegistry.handle(null, ConvoyTerminalIds.CONVOY_TAB, ConvoyTerminalIds.SCAN_ACTION, "");
         helper.assertTrue(handled, "Convoy terminal scan action should be registered");
         ServerPlayer player = helper.makeMockServerPlayerInLevel();
         ConvoyVehicleEntity vehicle = spawnBike(helper, new BlockPos(3, 2, 3));
         BlockPos markerPos = new BlockPos(4, 1, 3);
         helper.setBlock(markerPos, (Block)ModBlocks.ROADSIDE_SIGNAL_MARKER.get());
         player.setPos(helper.absolutePos(markerPos.above()).getCenter());
         vehicle.setPos(helper.absolutePos(markerPos.above().east()).getCenter());
         Identifier routeId = id("northern_route");
         helper.assertTrue(ConvoyRouteService.activateRoute(player, vehicle, routeId, helper.absolutePos(new BlockPos(-32, 1, 3))),
            "Terminal action test route should activate");
         helper.assertTrue(TerminalActionRegistry.handle(player, ConvoyTerminalIds.CONVOY_TAB, ConvoyTerminalIds.COMPLETE_ACTION, ""),
            "Convoy terminal signal action should be registered");
         helper.assertTrue(ConvoyProgress.get(player).completed(routeId), "Terminal signal action should complete at nearby marker");
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
      helper.succeed();
   }

   private static void terminalSnapshot(GameTestHelper helper) {
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      ConvoyVehicleEntity vehicle = spawnVehicle(helper, ModEntities.WASTELAND_ROVER.get(), new BlockPos(2, 2, 2));
      vehicle.setCustomName(Component.literal("Route Fox"));
      vehicle.insertCargo(new ItemStack(ModItems.ENGINE_CORE.get()));
      helper.setBlock(new BlockPos(3, 1, 2), (Block)ModBlocks.ROADSIDE_SIGNAL_MARKER.get());
      player.setPos(helper.absolutePos(new BlockPos(2, 2, 3)).getCenter());
      helper.assertTrue(ConvoyRouteService.activateRoute(player, vehicle, id("salvager_escort"), helper.absolutePos(new BlockPos(-32, 1, 2))),
         "Snapshot route should activate");

      ConvoyTerminalStatePacket snapshot = ConvoyTerminalStatePacket.from(player);
      ConvoyTerminalClientState.apply(snapshot);
      helper.assertTrue(ConvoyTerminalClientState.snapshot().vehicleTitle().contains("Route Fox"), "Terminal snapshot should include vehicle callsign");
      helper.assertTrue(snapshot.vehicleStatus().contains("Damage 0/80"), "Terminal snapshot should include vehicle damage");
      helper.assertTrue(snapshot.vehicleCargo().contains("Cargo 1/18"), "Terminal snapshot should include cargo slot use");
      helper.assertTrue(snapshot.cargoLines().stream().anyMatch(line -> line.contains("Engine Core")), "Terminal snapshot should include cargo item lines");
      helper.assertTrue(snapshot.activeLegStatus().contains("Salvager Checkpoint"), "Terminal snapshot should include active route leg");
      helper.assertTrue(snapshot.nearbyPoiLines().stream().anyMatch(line -> line.contains("Roadside Signal Marker")), "Terminal snapshot should include nearby POIs");
      helper.getLevel().getServer().getPlayerList().remove(player);
      helper.succeed();
   }

   private static void terminalRouteSelection(GameTestHelper helper) {
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      ConvoyVehicleEntity vehicle = spawnVehicle(helper, ModEntities.WASTELAND_ROVER.get(), new BlockPos(2, 2, 2));
      player.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
      Identifier routeId = id("salvager_escort");

      TerminalActionRegistry.withClearedForTests(() -> {
         ConvoyTerminalCommonIntegration.register();
         helper.assertTrue(TerminalActionRegistry.handle(player, ConvoyTerminalIds.CONVOY_TAB, ConvoyTerminalIds.START_ACTION, id("missing_route").toString()),
            "Terminal should handle invalid selected-route payloads");
         helper.assertTrue(ConvoyProgress.get(player).activeRouteId().isBlank(), "Invalid route selection should not start the recommended route");
      });

      ConvoyTerminalStatePacket blockedSnapshot = ConvoyTerminalStatePacket.from(player);
      int blockedIndex = blockedSnapshot.routeBoardRouteIds().indexOf(routeId.toString());
      helper.assertTrue(blockedIndex >= 0, "Terminal snapshot should expose route ids for selectable rows");
      helper.assertTrue("blocked".equals(blockedSnapshot.routeBoardActions().get(blockedIndex)),
         "Terminal snapshot should mark missing-cargo routes as blocked");

      TerminalActionRegistry.withClearedForTests(() -> {
         ConvoyTerminalCommonIntegration.register();
         helper.assertTrue(TerminalActionRegistry.handle(player, ConvoyTerminalIds.CONVOY_TAB, ConvoyTerminalIds.START_ACTION, routeId.toString()),
            "Terminal should handle selected blocked route payloads");
         helper.assertTrue(ConvoyProgress.get(player).activeRouteId().isBlank(), "Blocked selected routes should not activate");
      });

      vehicle.insertCargo(new ItemStack(ModItems.ENGINE_CORE.get()));
      ConvoyTerminalStatePacket readySnapshot = ConvoyTerminalStatePacket.from(player);
      int readyIndex = readySnapshot.routeBoardRouteIds().indexOf(routeId.toString());
      helper.assertTrue(readyIndex >= 0 && "start".equals(readySnapshot.routeBoardActions().get(readyIndex)),
         "Terminal snapshot should mark prepared selected routes as startable");
      helper.assertTrue(routeId.toString().equals(readySnapshot.recommendedStartRouteId()),
         "Prepared selected route should become the terminal start payload when it is first startable");

      TerminalActionRegistry.withClearedForTests(() -> {
         ConvoyTerminalCommonIntegration.register();
         helper.assertTrue(TerminalActionRegistry.handle(player, ConvoyTerminalIds.CONVOY_TAB, ConvoyTerminalIds.START_ACTION, routeId.toString()),
            "Terminal should start the selected prepared route");
         helper.assertTrue(routeId.toString().equals(ConvoyProgress.get(player).activeRouteId()),
            "Terminal route selection should activate the selected route, not the default route");
      });
      helper.getLevel().getServer().getPlayerList().remove(player);
      helper.succeed();
   }

   private static void callsignShieldingPersistence(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity vehicle = spawnVehicle(helper, ModEntities.WASTELAND_ROVER.get(), new BlockPos(1, 2, 1));
      player.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());

      ItemStack nameTag = new ItemStack(Items.NAME_TAG);
      nameTag.set(DataComponents.CUSTOM_NAME, Component.literal("Dustline"));
      player.setItemInHand(InteractionHand.MAIN_HAND, nameTag);
      vehicle.interact(player, InteractionHand.MAIN_HAND, vehicle.position());
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.RADIATION_SHIELDING_PLATE.get()));
      vehicle.interact(player, InteractionHand.MAIN_HAND, vehicle.position());
      vehicle.insertCargo(new ItemStack(Items.APPLE, 4));

      TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
      vehicle.saveWithoutId(output);
      CompoundTag saved = output.buildResult();
      ConvoyVehicleEntity loaded = ModEntities.WASTELAND_ROVER.get().create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(loaded != null, "Reloaded vehicle should instantiate");
      loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));

      helper.assertTrue("Dustline".equals(loaded.callsign()), "Vehicle callsign should survive save/load");
      helper.assertTrue(loaded.shieldingPlates() == 1, "Vehicle shielding should survive save/load");
      helper.assertTrue(loaded.cargoItemCount(Items.APPLE) == 4, "Vehicle cargo should survive save/load");
      helper.succeed();
   }

   private static void scannerRouteUtility(GameTestHelper helper) {
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      ConvoyVehicleEntity vehicle = spawnVehicle(helper, ModEntities.WASTELAND_ROVER.get(), new BlockPos(1, 2, 1));
      player.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
      ItemStack beacon = new ItemStack(ModItems.ROUTE_BEACON.get());
      player.setItemInHand(InteractionHand.MAIN_HAND, beacon);
      vehicle.interact(player, InteractionHand.MAIN_HAND, vehicle.position());
      helper.assertTrue(beacon.getCount() == 1, "Route Beacon scanner sweep should not consume the beacon without an active route");
      helper.assertTrue(vehicle.activeRouteId().isBlank(), "Scanner sweep without active route should not pair a route");

      vehicle.insertCargo(new ItemStack(ModItems.ENGINE_CORE.get()));
      helper.assertTrue(ConvoyRouteService.activateRoute(player, vehicle, id("salvager_escort"), helper.absolutePos(new BlockPos(-64, 1, 1))),
         "Scanner route should activate");
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ROUTE_BEACON.get()));
      vehicle.interact(player, InteractionHand.MAIN_HAND, vehicle.position());
      ConvoyProgress progress = ConvoyProgress.get(player);
      helper.assertTrue(progress.activeRouteVehicle().isPresent() && progress.activeRouteVehicle().get().equals(vehicle.getUUID()),
         "Route Beacon should pair the active route vehicle");
      helper.assertTrue(ConvoyTerminalStatePacket.from(player).activeLegStatus().contains("within scanner range"),
         "Terminal snapshot should expose scanner range status");
      helper.getLevel().getServer().getPlayerList().remove(player);
      helper.succeed();
   }

   private static void relayDeployment(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity relay = spawnVehicle(helper, ModEntities.ARMORED_RELAY_TRUCK.get(), new BlockPos(2, 2, 2));
      player.setPos(relay.getX(), relay.getY(), relay.getZ());
      helper.setBlock(new BlockPos(3, 1, 2), Blocks.STONE);
      int battery = relay.battery();
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.FIELD_REPAIR_STATION.get().asItem()));
      relay.interact(player, InteractionHand.MAIN_HAND, relay.position());
      helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(3, 2, 2))).is(ModBlocks.FIELD_REPAIR_STATION.get()),
         "Armored Relay Truck should deploy a real Field Repair Station");
      helper.assertTrue(relay.battery() == battery - 20, "Field station deployment should consume relay battery");
      helper.succeed();
   }

   private static void coreRouteTelemetry(GameTestHelper helper) {
      EchoCoreServices.clearPlatformServicesForTests();
      ConvoyCoreIntegration.registerAddonChapter();
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ConvoyVehicleEntity vehicle = spawnVehicle(helper, ModEntities.WASTELAND_ROVER.get(), new BlockPos(1, 2, 1));
      vehicle.setCustomName(Component.literal("Core Rover"));
      vehicle.insertCargo(new ItemStack(ModItems.ENGINE_CORE.get()));
      helper.assertTrue(ConvoyRouteService.activateRoute(player, vehicle, id("salvager_escort"), helper.absolutePos(new BlockPos(-32, 1, 1))),
         "Core telemetry route should activate");

      EchoRouteRecord record = EchoCoreServices.routeRecords(player).stream()
         .filter(route -> route.id().equals(ConvoyTerminalIds.id("route/salvager_escort")))
         .findFirst()
         .orElseThrow();
      helper.assertTrue(record.status().contains("ACTIVE LEG"), "Core route record should expose active leg status");
      helper.assertTrue(record.summary().contains("Core Rover"), "Core route record should expose vehicle callsign");
      helper.assertTrue(record.summary().contains("Next leg"), "Core route record should expose next leg telemetry");
      helper.assertTrue(EchoCoreServices.diagnostics(player).stream().anyMatch(diagnostic -> diagnostic.id().equals(ConvoyTerminalIds.id("diagnostic/active_route"))),
         "Core diagnostics should expose active convoy route");
      EchoCoreServices.clearPlatformServicesForTests();
      helper.succeed();
   }

   private static void worldgenResourceCoverage(GameTestHelper helper) {
      assertResource(helper, "worldgen/structure/roadside_corridors.json");
      assertResource(helper, "worldgen/template_pool/roadside_corridors.json");
      assertResource(helper, "worldgen/structure_set/roadside_corridors.json");
      for (String route : List.of("northern_route", "salvager_escort", "northern_freight")) {
         assertResource(helper, "worldgen/structure/route/" + route + "_corridor.json");
         assertResource(helper, "worldgen/template_pool/route/" + route + "_corridor.json");
         assertResource(helper, "worldgen/structure_set/route/" + route + "_corridor.json");
      }
      for (String structure : List.of(
         "roadside/signal_marker.nbt",
         "roadside/cargo_checkpoint.nbt",
         "roadside/fuel_cache.nbt",
         "roadside/wreck_ambush.nbt",
         "roadside/repair_pullout.nbt",
         "roadside/routes/northern_route_destination.nbt",
         "roadside/routes/salvager_escort_checkpoint.nbt",
         "roadside/routes/salvager_escort_destination.nbt",
         "roadside/routes/northern_freight_checkpoint.nbt",
         "roadside/routes/northern_freight_destination.nbt"
      )) {
         assertResource(helper, "structures/" + structure);
      }
      helper.succeed();
   }

   private static void productionReloadSoak(GameTestHelper helper) {
      Identifier completedRoute = id("northern_route");
      Identifier activeRoute = id("salvager_escort");
      ServerPlayer player = helper.makeMockServerPlayerInLevel();

      ConvoyVehicleEntity bike = spawnBike(helper, new BlockPos(1, 2, 1));
      helper.assertTrue(ConvoyRouteService.activateRoute(player, bike, completedRoute, helper.absolutePos(new BlockPos(-40, 1, 1))),
         "Soak setup should activate a completion route");
      helper.assertTrue(ConvoyRouteService.advanceRouteAtSignal(player, bike, helper.absolutePos(new BlockPos(1, 2, 1))),
         "Soak setup should complete the first route");
      helper.assertTrue(ConvoyRouteService.claimRouteRewards(player, completedRoute),
         "Initial reward claim should succeed before reload");
      helper.assertFalse(ConvoyRouteService.claimRouteRewards(player, completedRoute),
         "Duplicate reward claim should be rejected before reload");

      ConvoyVehicleEntity rover = spawnVehicle(helper, ModEntities.WASTELAND_ROVER.get(), new BlockPos(2, 2, 2));
      ItemStack nameTag = new ItemStack(Items.NAME_TAG);
      nameTag.set(DataComponents.CUSTOM_NAME, Component.literal("Soak Rover"));
      player.setItemInHand(InteractionHand.MAIN_HAND, nameTag);
      rover.interact(player, InteractionHand.MAIN_HAND, rover.position());
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.RADIATION_SHIELDING_PLATE.get()));
      rover.interact(player, InteractionHand.MAIN_HAND, rover.position());
      rover.insertCargo(new ItemStack(ModItems.ENGINE_CORE.get()));
      rover.insertCargo(new ItemStack(Items.APPLE, 3));
      rover.applyVehicleDamage(19);

      helper.assertTrue(ConvoyRouteService.activateRoute(player, rover, activeRoute, helper.absolutePos(new BlockPos(-40, 1, 2))),
         "Soak setup should activate an in-progress route");
      helper.assertTrue(ConvoyRouteService.advanceRouteAtSignal(player, rover, helper.absolutePos(new BlockPos(2, 2, 2))),
         "First salvager route leg should advance before reload");

      TagValueOutput vehicleOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
      rover.saveWithoutId(vehicleOutput);
      CompoundTag savedVehicle = vehicleOutput.buildResult();
      CompoundTag savedPlayerData = player.getPersistentData().copy();

      bike.discard();
      rover.discard();
      player.getPersistentData().put(PROGRESS_ROOT, new CompoundTag());
      player.getPersistentData().put(PROGRESS_ROOT, savedPlayerData.getCompoundOrEmpty(PROGRESS_ROOT).copy());

      ConvoyVehicleEntity loadedRover = ModEntities.WASTELAND_ROVER.get().create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(loadedRover != null, "Reloaded rover should instantiate");
      loadedRover.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), savedVehicle));
      loadedRover.setPos(helper.absolutePos(new BlockPos(2, 2, 2)).getCenter());
      helper.getLevel().addFreshEntity(loadedRover);
      player.setPos(loadedRover.getX(), loadedRover.getY(), loadedRover.getZ());

      ConvoyProgress reloadedProgress = ConvoyProgress.get(player);
      helper.assertTrue(loadedRover.isOwner(player), "Vehicle owner UUID should survive reload");
      helper.assertTrue("Soak Rover".equals(loadedRover.callsign()), "Vehicle callsign should survive reload");
      helper.assertTrue(loadedRover.shieldingPlates() == 1, "Shielding plates should survive reload");
      helper.assertTrue(loadedRover.damage() == 19, "Vehicle damage should survive reload");
      helper.assertTrue(loadedRover.cargoItemCount(ModItems.ENGINE_CORE.get()) == 1, "Route cargo should survive reload");
      helper.assertTrue(loadedRover.cargoItemCount(Items.APPLE) == 3, "General cargo should survive reload");
      helper.assertTrue(activeRoute.toString().equals(loadedRover.activeRouteId()), "Vehicle active route should survive reload");
      helper.assertTrue(activeRoute.toString().equals(reloadedProgress.activeRouteId()),
         "Player active route should recover after reload");
      helper.assertTrue(reloadedProgress.activeRouteLeg() == 1, "Active route leg should recover after reload");
      helper.assertTrue(reloadedProgress.activeRouteVehicle().filter(loadedRover.getUUID()::equals).isPresent(),
         "Active route vehicle binding should recover after reload");
      helper.assertTrue(reloadedProgress.completed(completedRoute), "Completed route should survive reload");
      helper.assertTrue(reloadedProgress.claimed(completedRoute), "Claimed reward state should survive reload");
      helper.assertFalse(ConvoyRouteService.claimRouteRewards(player, completedRoute),
         "Duplicate reward claim should remain rejected after reload");

      ConvoyTerminalStatePacket snapshot = ConvoyTerminalStatePacket.from(player);
      ConvoyTerminalClientState.apply(snapshot);
      helper.assertTrue(snapshot.vehicleTitle().contains("Soak Rover"), "Terminal snapshot should recover loaded vehicle callsign");
      helper.assertTrue(snapshot.activeRouteTitle().contains("Escort"), "Terminal snapshot should recover active route title");
      helper.assertTrue(snapshot.activeRouteStatus().contains("Leg 2/2"), "Terminal snapshot should recover active leg progress");
      int completedIndex = snapshot.routeBoardRouteIds().indexOf(completedRoute.toString());
      int activeIndex = snapshot.routeBoardRouteIds().indexOf(activeRoute.toString());
      helper.assertTrue(completedIndex >= 0 && "claimed".equals(snapshot.routeBoardActions().get(completedIndex)),
         "Terminal route board should recover claimed route state");
      helper.assertTrue(activeIndex >= 0 && "active".equals(snapshot.routeBoardActions().get(activeIndex)),
         "Terminal route board should recover active route state");

      loadedRover.discard();
      helper.getLevel().getServer().getPlayerList().remove(player);
      helper.succeed();
   }

   private static void registerRouteDiscoveryProviderForTests() {
      EchoCoreServices.registerDiscoveryProvider(new EchoDiscoveryProvider() {
         @Override
         public List<EchoDiscoveryEntry> entries(Player player) {
            return EchoCoreServices.routeRecords(player).stream()
               .map(record -> new EchoDiscoveryEntry(
                  EchoCoreServices.routeDiscoveryId(record.id()),
                  id("convoy_protocol"),
                  EchoDiscoveryCategory.STRUCTURE,
                  record.title(),
                  "Unmapped Convoy Signal",
                  "Find and activate this convoy route to reveal its record.",
                  record.summary(),
                  null,
                  null,
                  0xFF66E8FF,
                  record.id(),
                  10))
               .toList();
         }

         @Override
         public EchoDiscoveryState state(Player player, EchoDiscoveryEntry entry) {
            return EchoDiscoveryState.LOCKED;
         }
      });
   }

   private static void assertResource(GameTestHelper helper, String path) {
      Identifier resourceId = id(path);
      helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(resourceId).isPresent(),
         "Expected resource " + resourceId);
   }

   private static ConvoyVehicleEntity spawnVehicle(GameTestHelper helper, EntityType<? extends ConvoyVehicleEntity> type, BlockPos relativePos) {
      ConvoyVehicleEntity vehicle = type.create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(vehicle != null, "Convoy vehicle should spawn");
      vehicle.setPos(helper.absolutePos(relativePos).getCenter());
      helper.getLevel().addFreshEntity(vehicle);
      return vehicle;
   }

   private static ConvoyVehicleEntity spawnBike(GameTestHelper helper, BlockPos relativePos) {
      return spawnVehicle(helper, ModEntities.SCRAP_BIKE.get(), relativePos);
   }

   private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, String testName, Identifier functionId) {
      TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData(
         environment, Identifier.withDefaultNamespace("empty"), 400, 0, true, Rotation.NONE, false, 1, 1, false, 2
      );
      event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, path);
   }
}
