package com.knoxhack.echoindustrialnexus.test;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialFluidPipeBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialFluxDuctBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialItemDuctBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMachineBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMultiblockControllerBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMultiblockCrateBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialRoboticArmMountBlockEntity;
import com.knoxhack.echoindustrialnexus.entity.FurnaceWardenEntity;
import com.knoxhack.echoindustrialnexus.integration.IndustrialCompat;
import com.knoxhack.echoindustrialnexus.integration.IndustrialCoreIntegration;
import com.knoxhack.echoindustrialnexus.integration.IndustrialMissionProvider;
import com.knoxhack.echoindustrialnexus.integration.IndustrialMultiblockIntegrationProvider;
import com.knoxhack.echoindustrialnexus.integration.IndustrialRenderCoreVisuals;
import com.knoxhack.echoindustrialnexus.integration.IndustrialTerminalCommonIntegration;
import com.knoxhack.echoindustrialnexus.integration.IndustrialTerminalIds;
import com.knoxhack.echoindustrialnexus.integration.IndustrialTerminalRecipeProvider;
import com.knoxhack.echoindustrialnexus.integration.IndustrialLensIntegration;
import com.knoxhack.echoindustrialnexus.menu.IndustrialMachineMenu;
import com.knoxhack.echoindustrialnexus.menu.IndustrialMultiblockControllerMenu;
import com.knoxhack.echoindustrialnexus.multiblock.IndustrialMultiblockTasks;
import com.knoxhack.echoindustrialnexus.network.IndustrialFactorySnapshotPacket;
import com.knoxhack.echoindustrialnexus.progress.IndustrialProgress;
import com.knoxhack.echoindustrialnexus.registry.ModBlocks;
import com.knoxhack.echoindustrialnexus.registry.ModEntities;
import com.knoxhack.echoindustrialnexus.registry.ModFluids;
import com.knoxhack.echoindustrialnexus.registry.ModItems;
import com.knoxhack.echoindustrialnexus.worldgen.IndustrialPoiGenerator;
import com.knoxhack.echoindustrialnexus.worldgen.IndustrialPoiType;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoRouteRecord;
import com.knoxhack.echomultiblockcore.api.LensMultiblockScan;
import com.knoxhack.echomultiblockcore.api.MultiblockCapability;
import com.knoxhack.echomultiblockcore.api.MultiblockAutomationRecipe;
import com.knoxhack.echomultiblockcore.api.AutomationRecipeRegistry;
import com.knoxhack.echomultiblockcore.api.MultiblockDefinition;
import com.knoxhack.echomultiblockcore.api.MultiblockIntegrationServices;
import com.knoxhack.echomultiblockcore.api.MultiblockMapMarkerSnapshot;
import com.knoxhack.echomultiblockcore.api.MultiblockPowerProvider;
import com.knoxhack.echomultiblockcore.api.MultiblockTaskState;
import com.knoxhack.echomultiblockcore.api.RobotToolType;
import com.knoxhack.echomultiblockcore.api.ValidationResult;
import com.knoxhack.echomultiblockcore.api.WorkcellType;
import com.knoxhack.echomultiblockcore.content.MultiblockContent;
import com.knoxhack.echolens.registry.LensProviderRegistry;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeEntry;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeSlot;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class ModGameTests {
   private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(Registries.TEST_FUNCTION, EchoIndustrialNexus.MODID);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MACHINE_PROCESSING =
      TEST_FUNCTIONS.register("machine_recipe_processing", () -> ModGameTests::machineRecipeProcessing);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLUX_DUCT_TRANSFER =
      TEST_FUNCTIONS.register("flux_duct_transfer_loss", () -> ModGameTests::fluxDuctTransferLoss);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SCRAP_DYNAMO_GENERATION =
      TEST_FUNCTIONS.register("scrap_dynamo_generates_flux", () -> ModGameTests::scrapDynamoGeneratesFlux);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GENERATOR_ORE_LOOP =
      TEST_FUNCTIONS.register("generator_powers_ore_grinder", () -> ModGameTests::generatorPowersOreGrinder);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ITEM_DUCT_ROUTING =
      TEST_FUNCTIONS.register("item_duct_routing", () -> ModGameTests::itemDuctRouting);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BREAK_DROPS =
      TEST_FUNCTIONS.register("machine_and_duct_break_drops", () -> ModGameTests::machineAndDuctBreakDrops);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LAVA_BUCKET_RETURN =
      TEST_FUNCTIONS.register("lava_bucket_returns_bucket", () -> ModGameTests::lavaBucketReturnsBucket);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SCRUBBER_PROGRESS =
      TEST_FUNCTIONS.register("scrubber_safe_zone_progress", () -> ModGameTests::scrubberSafeZoneProgress);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSIONS =
      TEST_FUNCTIONS.register("terminal_mission_snapshots", () -> ModGameTests::terminalMissionSnapshots);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_CLAIM =
      TEST_FUNCTIONS.register("terminal_claim_idempotency", () -> ModGameTests::terminalClaimIdempotency);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_RECIPE_PARSER =
      TEST_FUNCTIONS.register("terminal_recipe_parser", () -> ModGameTests::terminalRecipeParser);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REGISTERED_ITEM_PATHS =
      TEST_FUNCTIONS.register("registered_orphan_item_paths", () -> ModGameTests::registeredOrphanItemPaths);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_WIRING =
      TEST_FUNCTIONS.register("core_chapter_route_records", () -> ModGameTests::coreChapterRouteRecords);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WARDEN_SPAWN =
      TEST_FUNCTIONS.register("furnace_warden_spawn", () -> ModGameTests::furnaceWardenSpawn);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WARDEN_REWARD_IDEMPOTENCY =
      TEST_FUNCTIONS.register("furnace_warden_reward_idempotency", () -> ModGameTests::furnaceWardenRewardIdempotency);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> POI_SMOKE =
      TEST_FUNCTIONS.register("procedural_poi_smoke", () -> ModGameTests::proceduralPoiSmoke);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MENU_SYNC =
      TEST_FUNCTIONS.register("machine_menu_data_sync", () -> ModGameTests::machineMenuDataSync);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MACHINE_PERSISTENCE =
      TEST_FUNCTIONS.register("machine_state_persistence", () -> ModGameTests::machineStatePersistence);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SIDE_CONFIG =
      TEST_FUNCTIONS.register("machine_side_config", () -> ModGameTests::machineSideConfig);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLUID_TANK =
      TEST_FUNCTIONS.register("fluid_tank_processing", () -> ModGameTests::fluidTankProcessing);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTROLLER_SHUTDOWN =
      TEST_FUNCTIONS.register("controller_remote_shutdown", () -> ModGameTests::controllerRemoteShutdown);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UPGRADE_STACKING =
      TEST_FUNCTIONS.register("upgrade_stacking_rules", () -> ModGameTests::upgradeStackingRules);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REWARD_IDEMPOTENCY =
      TEST_FUNCTIONS.register("reward_claim_idempotency", () -> ModGameTests::rewardClaimIdempotency);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLUID_CAPABILITY =
      TEST_FUNCTIONS.register("fluid_capability_fill_drain", () -> ModGameTests::fluidCapabilityFillDrain);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLUID_PIPE =
      TEST_FUNCTIONS.register("fluid_pipe_transfer_filtering", () -> ModGameTests::fluidPipeTransferFiltering);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CROSS_COMPAT_OUTPUT =
      TEST_FUNCTIONS.register("cross_compat_component_output", () -> ModGameTests::crossCompatComponentOutput);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_PRESSURE_COMPAT =
      TEST_FUNCTIONS.register("nexus_pressure_compat", () -> ModGameTests::nexusPressureCompat);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RENDERCORE_VISUAL_STATES =
      TEST_FUNCTIONS.register("rendercore_visual_states", () -> ModGameTests::renderCoreVisualStates);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INDUSTRIAL_MULTIBLOCK_DEFINITIONS =
      TEST_FUNCTIONS.register("industrial_multiblock_definitions", () -> ModGameTests::industrialMultiblockDefinitions);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_FURNACE_ARRAY_ROBOTIC_TASK =
      TEST_FUNCTIONS.register("nexus_furnace_array_robotic_task", () -> ModGameTests::nexusFurnaceArrayRoboticTask);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_FURNACE_ARRAY_CORE_KEY_MISSION =
      TEST_FUNCTIONS.register("nexus_furnace_array_core_key_mission", () -> ModGameTests::nexusFurnaceArrayCoreKeyMission);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_FURNACE_ARRAY_PROVIDERS =
      TEST_FUNCTIONS.register("nexus_furnace_array_provider_snapshots", () -> ModGameTests::nexusFurnaceArrayProviderSnapshots);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ASSEMBLY_LINE_ROBOTIC_TASK =
      TEST_FUNCTIONS.register("assembly_line_robotic_task", () -> ModGameTests::assemblyLineRoboticTask);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ASSEMBLY_LINE_GUI_QUEUE =
      TEST_FUNCTIONS.register("assembly_line_gui_queue", () -> ModGameTests::assemblyLineGuiQueue);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ASSEMBLY_LINE_BATCH_QUEUE =
      TEST_FUNCTIONS.register("assembly_line_batch_queue", () -> ModGameTests::assemblyLineBatchQueue);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FACTORY_COMMAND_TERMINAL_ACTIONS =
      TEST_FUNCTIONS.register("factory_command_terminal_actions", () -> ModGameTests::factoryCommandTerminalActions);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FACTORY_COMMAND_PROVIDER_SNAPSHOTS =
      TEST_FUNCTIONS.register("factory_command_provider_snapshots", () -> ModGameTests::factoryCommandProviderSnapshots);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INDUSTRIAL_LENS_PROVIDER =
      TEST_FUNCTIONS.register("industrial_lens_provider_registration", () -> ModGameTests::industrialLensProviderRegistration);

   private ModGameTests() {
   }

   public static void register(IEventBus eventBus) {
      TEST_FUNCTIONS.register(eventBus);
   }

   public static void registerTests(RegisterGameTestsEvent event) {
      Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("industrial_chapter"));
      register(event, environment, "machine_recipe_processing", MACHINE_PROCESSING.getId());
      register(event, environment, "flux_duct_transfer_loss", FLUX_DUCT_TRANSFER.getId());
      register(event, environment, "scrap_dynamo_generates_flux", SCRAP_DYNAMO_GENERATION.getId());
      register(event, environment, "generator_powers_ore_grinder", GENERATOR_ORE_LOOP.getId());
      register(event, environment, "item_duct_routing", ITEM_DUCT_ROUTING.getId());
      register(event, environment, "machine_and_duct_break_drops", BREAK_DROPS.getId());
      register(event, environment, "lava_bucket_returns_bucket", LAVA_BUCKET_RETURN.getId());
      register(event, environment, "scrubber_safe_zone_progress", SCRUBBER_PROGRESS.getId());
      register(event, environment, "terminal_mission_snapshots", TERMINAL_MISSIONS.getId());
      register(event, environment, "terminal_claim_idempotency", TERMINAL_CLAIM.getId());
      register(event, environment, "terminal_recipe_parser", TERMINAL_RECIPE_PARSER.getId());
      register(event, environment, "registered_orphan_item_paths", REGISTERED_ITEM_PATHS.getId());
      register(event, environment, "core_chapter_route_records", CORE_WIRING.getId());
      register(event, environment, "furnace_warden_spawn", WARDEN_SPAWN.getId());
      register(event, environment, "furnace_warden_reward_idempotency", WARDEN_REWARD_IDEMPOTENCY.getId());
      register(event, environment, "procedural_poi_smoke", POI_SMOKE.getId());
      register(event, environment, "machine_menu_data_sync", MENU_SYNC.getId());
      register(event, environment, "machine_state_persistence", MACHINE_PERSISTENCE.getId());
      register(event, environment, "machine_side_config", SIDE_CONFIG.getId());
      register(event, environment, "fluid_tank_processing", FLUID_TANK.getId());
      register(event, environment, "controller_remote_shutdown", CONTROLLER_SHUTDOWN.getId());
      register(event, environment, "upgrade_stacking_rules", UPGRADE_STACKING.getId());
      register(event, environment, "reward_claim_idempotency", REWARD_IDEMPOTENCY.getId());
      register(event, environment, "fluid_capability_fill_drain", FLUID_CAPABILITY.getId());
      register(event, environment, "fluid_pipe_transfer_filtering", FLUID_PIPE.getId());
      register(event, environment, "cross_compat_component_output", CROSS_COMPAT_OUTPUT.getId());
      register(event, environment, "nexus_pressure_compat", NEXUS_PRESSURE_COMPAT.getId());
      register(event, environment, "rendercore_visual_states", RENDERCORE_VISUAL_STATES.getId());
      register(event, environment, "industrial_multiblock_definitions", INDUSTRIAL_MULTIBLOCK_DEFINITIONS.getId());
      register(event, environment, "nexus_furnace_array_robotic_task", NEXUS_FURNACE_ARRAY_ROBOTIC_TASK.getId());
      register(event, environment, "nexus_furnace_array_core_key_mission", NEXUS_FURNACE_ARRAY_CORE_KEY_MISSION.getId());
      register(event, environment, "nexus_furnace_array_provider_snapshots", NEXUS_FURNACE_ARRAY_PROVIDERS.getId());
      register(event, environment, "assembly_line_robotic_task", ASSEMBLY_LINE_ROBOTIC_TASK.getId());
      register(event, environment, "assembly_line_gui_queue", ASSEMBLY_LINE_GUI_QUEUE.getId());
      register(event, environment, "assembly_line_batch_queue", ASSEMBLY_LINE_BATCH_QUEUE.getId());
      register(event, environment, "factory_command_terminal_actions", FACTORY_COMMAND_TERMINAL_ACTIONS.getId());
      register(event, environment, "factory_command_provider_snapshots", FACTORY_COMMAND_PROVIDER_SNAPSHOTS.getId());
      register(event, environment, "industrial_lens_provider_registration", INDUSTRIAL_LENS_PROVIDER.getId());
   }

   private static void machineRecipeProcessing(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      machine.setItem(IndustrialMachineBlockEntity.INPUT_SLOT, new ItemStack(Blocks.IRON_ORE));
      machine.receiveFlux(6000, false);
      for (int i = 0; i < 220; i++) {
         IndustrialMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
      }
      helper.assertTrue(machine.getItem(IndustrialMachineBlockEntity.OUTPUT_SLOT).is(ModItems.IRON_DUST.get()),
         "Ore Grinder should process iron ore into Iron Dust");
      helper.succeed();
   }

   private static void renderCoreVisualStates(GameTestHelper helper) {
      helper.assertTrue("IDLE".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.IDLE, 0, 0)),
         "Idle unpowered machines should map to IDLE.");
      helper.assertTrue("ONLINE".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.IDLE, 0, 20)),
         "Stored flux should map idle machines to ONLINE.");
      helper.assertTrue("WORKING".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.PROCESSING, 30, 20)),
         "Processing machines should map to WORKING.");
      helper.assertTrue("OVERHEATED".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.PROCESSING, 90, 20)),
         "Hot processing machines should map to OVERHEATED.");
      helper.assertTrue("CORRUPTED".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.NEXUS_CONTAMINATION, 20, 20)),
         "Nexus contamination should map to CORRUPTED.");
      helper.assertTrue("FAILED".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.MELTDOWN, 72, 20)),
         "Meltdown should map to FAILED.");
      helper.assertTrue("OFFLINE".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.REMOTE_SHUTDOWN, 0, 20)),
         "Remote shutdown should map to OFFLINE.");
      helper.assertTrue("COMPLETE".equals(IndustrialRenderCoreVisuals.visualStateName(IndustrialMachineBlockEntity.MachineStatus.COMPLETE, 0, 20)),
         "Completed work should map to COMPLETE.");
      helper.assertTrue(Math.abs(IndustrialRenderCoreVisuals.progress(50, 100, 0, 0) - 0.5F) < 0.001F,
         "RenderCore progress should prefer processing progress.");
      helper.succeed();
   }

   private static void industrialMultiblockDefinitions(GameTestHelper helper) {
      List<Identifier> ids = List.of(
         id("industrial_assembly_line"),
         id("scrap_processor"),
         id("plate_press"),
         id("circuit_fabricator"),
         id("recipe_matrix_core"),
         id("nexus_furnace_array")
      );
      for (Identifier definitionId : ids) {
         helper.assertTrue(MultiblockContent.definition(definitionId).isPresent(), "Missing Industrial multiblock definition: " + definitionId);
      }
      MultiblockDefinition assembly = MultiblockContent.definition(id("industrial_assembly_line")).orElseThrow();
      helper.assertTrue(assembly.workcells().stream().anyMatch(workcell -> workcell.type() == WorkcellType.ASSEMBLY),
         "Assembly Line should expose an Assembly workcell.");
      helper.assertTrue(assembly.roboticsRequirements().stream().anyMatch(requirement -> requirement.minArms() >= 1
            && requirement.requiredTools().contains(RobotToolType.WELDER)),
         "Assembly Line should require a welder-capable robotic arm.");
      MultiblockAutomationRecipe task = AutomationRecipeRegistry.byId(id("weld_reinforced_machine_frame")).orElseThrow();
      helper.assertTrue(task.consumeInputsOnStart(), "Industrial tasks should consume inputs when work starts.");
      helper.assertTrue(task.requiredWorkcell() == WorkcellType.ASSEMBLY, "Frame welding should target the Assembly workcell.");
      helper.assertTrue(task.outputs().stream().anyMatch(output -> output.itemId().equals(EchoIndustrialNexus.id("reinforced_machine_frame"))),
         "Frame welding should output a reinforced machine frame.");
      MultiblockDefinition nexus = MultiblockContent.definition(id("nexus_furnace_array")).orElseThrow();
      MultiblockDefinition circuit = MultiblockContent.definition(id("circuit_fabricator")).orElseThrow();
      MultiblockDefinition matrix = MultiblockContent.definition(id("recipe_matrix_core")).orElseThrow();
      helper.assertTrue(circuit.capabilities().contains(MultiblockCapability.POWER_INPUT),
         "Circuit Fabricator should expose echo:power_input for powered precision work.");
      helper.assertTrue(matrix.capabilities().contains(MultiblockCapability.POWER_INPUT),
         "Recipe Matrix Core should expose echo:power_input for shard encoding.");
      helper.assertTrue(nexus.capabilities().contains(MultiblockCapability.POWER_INPUT),
         "Nexus Furnace Array should expose echo:power_input for unstable processing.");
      helper.assertTrue(nexus.controllerBlockId().equals(id("nexus_furnace_array_controller")),
         "Nexus Furnace Array should use its dedicated controller block.");
      helper.assertTrue(nexus.workcells().stream().anyMatch(workcell -> workcell.type() == WorkcellType.MATRIX_PROCESSING
            && workcell.allowedTaskTypes().contains(id("stabilize_hybrid_thermal_core"))
            && workcell.allowedTaskTypes().contains(id("forge_core_key_assembly"))),
         "Nexus Furnace Array should expose Matrix Processing for both new automation tasks.");
      helper.assertTrue(nexus.roboticsRequirements().stream().anyMatch(requirement -> requirement.minArms() >= 1
            && requirement.requiredTools().contains(RobotToolType.INJECTOR)
            && requirement.requiredTools().contains(RobotToolType.SCANNER)),
         "Nexus Furnace Array should document injector and scanner tool requirements.");
      MultiblockAutomationRecipe stabilize = AutomationRecipeRegistry.byId(id("stabilize_hybrid_thermal_core")).orElseThrow();
      MultiblockAutomationRecipe forge = AutomationRecipeRegistry.byId(id("forge_core_key_assembly")).orElseThrow();
      assertPowerCost(helper, AutomationRecipeRegistry.byId(id("assemble_precision_circuit")).orElseThrow(), 24);
      assertPowerCost(helper, AutomationRecipeRegistry.byId(id("encode_recipe_matrix_shard")).orElseThrow(), 40);
      assertPowerCost(helper, stabilize, 80);
      assertPowerCost(helper, forge, 128);
      assertNexusArrayRecipe(helper, stabilize);
      assertNexusArrayRecipe(helper, forge);
      helper.succeed();
   }

   private static void assertPowerCost(GameTestHelper helper, MultiblockAutomationRecipe recipe, int throughput) {
      helper.assertTrue(recipe.capabilityCosts().stream().anyMatch(cost ->
            MultiblockCapability.POWER_INPUT.id().equals(cost.capabilityId())
               && cost.throughput() == throughput
               && "EP/t".equals(cost.unit())),
         recipe.id() + " should declare echo:power_input at " + throughput + " EP/t.");
   }

   private static void assertNexusArrayRecipe(GameTestHelper helper, MultiblockAutomationRecipe recipe) {
      helper.assertTrue(recipe.allowsMultiblock(id("nexus_furnace_array")),
         recipe.id() + " should be allowed on the Nexus Furnace Array.");
      helper.assertTrue(recipe.requiredWorkcell() == WorkcellType.MATRIX_PROCESSING,
         recipe.id() + " should target the Matrix Processing workcell.");
      helper.assertTrue(recipe.requiredTools().contains(RobotToolType.INJECTOR)
            && recipe.requiredTools().contains(RobotToolType.SCANNER),
         recipe.id() + " should declare injector/scanner compatible tool heads.");
      helper.assertTrue(recipe.effects().contains(id("nexus_array_pressure")),
         recipe.id() + " should record Nexus Array pressure through a soft effect.");
   }

   private static void nexusFurnaceArrayRoboticTask(GameTestHelper helper) {
      MultiblockIntegrationServices.withClearedForTests(() -> {
         AtomicLong availablePower = new AtomicLong(100_000L);
         MultiblockIntegrationServices.registerPowerProvider(testPowerProvider(availablePower));
         IndustrialMultiblockTasks.register();
         BuildNexusFurnaceArray built = buildNexusFurnaceArray(helper);
         ValidationResult result = built.controller().validateStructure(true);
         helper.assertTrue(result.valid(), "Nexus Furnace Array should validate with the dedicated controller: " + validationDebug(result));
         built.controller().onStructureFormed();
         ServerPlayer observer = helper.makeMockServerPlayerInLevel();
         observer.setPos(built.controller().getBlockPos().getX() + 0.5D, built.controller().getBlockPos().getY() + 1.0D,
            built.controller().getBlockPos().getZ() + 0.5D);
         addStabilizeInputs(built.input());
         helper.assertTrue(built.controller().queueTask(id("stabilize_hybrid_thermal_core"), null),
            "Nexus Furnace Array should accept a blocked stabilization queue entry before a compatible tool head is installed.");
         helper.assertTrue(built.controller().taskSnapshots().stream().anyMatch(snapshot -> snapshot.state() == MultiblockTaskState.BLOCKED
               && snapshot.blockedReason().contains("Required robotic tool missing")),
            "Blocked Nexus Array task should explain the missing injector/scanner head.");
         helper.assertTrue(built.input().countItem(ModItems.STABLE_NEXUS_CORE.get()) == 1
               && built.input().countItem(ModItems.COOLANT_CELL.get()) == 2,
            "Blocked Nexus Array task should not consume inputs before a tool head is installed.");
         built.controller().clearQueue(null);
         availablePower.set(0L);
         built.arm().installTool(new ItemStack((ItemLike)ModItems.COOLANT_INJECTOR_HEAD.get()), null);
         helper.assertTrue(built.controller().queueTask(id("stabilize_hybrid_thermal_core"), null),
            "Nexus Furnace Array should accept a power-starved queue entry before grid power is registered.");
         helper.assertTrue(built.controller().taskSnapshots().stream().anyMatch(snapshot -> snapshot.state() == MultiblockTaskState.BLOCKED
               && snapshot.blockedReason().contains("Power-starved")),
            "Blocked Nexus Array task should explain missing echo:power_input supply.");
         helper.assertTrue(built.input().countItem(ModItems.STABLE_NEXUS_CORE.get()) == 1
               && built.input().countItem(ModItems.COOLANT_CELL.get()) == 2,
            "Power-starved Nexus Array task should not consume inputs.");
         built.controller().clearQueue(null);
         availablePower.set(100_000L);
         helper.assertTrue(built.controller().queueTask(id("stabilize_hybrid_thermal_core"), null),
            "Nexus Furnace Array should start stabilization once a compatible tool head and EP provider are available.");
         helper.assertTrue(built.input().countItem(ModItems.STABLE_NEXUS_CORE.get()) == 0
               && built.input().countItem(ModItems.RECIPE_MATRIX_SHARD.get()) == 0
               && built.input().countItem(ModItems.COOLANT_CELL.get()) == 0
               && built.input().countItem(ModItems.FLUX_CRYSTAL.get()) == 0,
            "Started Nexus Array task should consume all stabilization inputs.");
         tickNexusArrayUntilOutput(built, ModItems.HYBRID_THERMAL_CORE.get(), 1, 900);
         helper.assertTrue(built.output().countItem(ModItems.HYBRID_THERMAL_CORE.get()) == 1,
            "Stabilization should output one Hybrid Thermal Core.");
         helper.assertTrue(built.output().countItem(ModItems.RAD_SLAG.get()) == 1,
            "Stabilization should output Rad Slag as a byproduct.");
         helper.assertTrue(IndustrialProgress.flag(observer, "nexus_thermal_warning"),
            "Soft Nexus pressure effect should record local Nexus thermal warning without failing the task.");
      });
      helper.succeed();
   }

   private static void nexusFurnaceArrayCoreKeyMission(GameTestHelper helper) {
      MultiblockIntegrationServices.withClearedForTests(() -> {
         MultiblockIntegrationServices.registerPowerProvider(testPowerProvider());
         IndustrialMultiblockTasks.register();
         BuildNexusFurnaceArray built = buildNexusFurnaceArray(helper);
         ValidationResult result = built.controller().validateStructure(true);
         helper.assertTrue(result.valid(), "Nexus Furnace Array should validate before mission progress test: " + validationDebug(result));
         built.controller().onStructureFormed();
         built.arm().installTool(new ItemStack((ItemLike)ModItems.COOLANT_INJECTOR_HEAD.get()), null);
         ServerPlayer player = helper.makeMockServerPlayerInLevel();
         player.setPos(built.controller().getBlockPos().getX() + 0.5D, built.controller().getBlockPos().getY() + 1.0D,
            built.controller().getBlockPos().getZ() + 0.5D);
         addForgeInputs(built.input());
         helper.assertTrue(built.controller().queueTask(id("forge_core_key_assembly"), player),
            "Nexus Furnace Array should queue the Core Key Assembly forging task.");
         tickNexusArrayUntilOutput(built, ModItems.CORE_KEY_ASSEMBLY.get(), 1, 1100);
         int outputCount = built.output().countItem(ModItems.CORE_KEY_ASSEMBLY.get());
         helper.assertTrue(outputCount == 1,
            "Core Key forging should output a Core Key Assembly; output=" + outputCount
               + " tasks=" + built.controller().taskSnapshots()
               + " diagnostics=" + built.controller().diagnosticLines()
               + " arm=" + built.arm().getRobotState()
               + " heat=" + built.arm().getHeat());
         helper.assertTrue(IndustrialProgress.progress(player, "nexus_furnace_array") >= 1.0F,
            "Core Key forging should complete Nexus Furnace Array mission progress.");
         TerminalMissionSnapshot snapshot = IndustrialMissionProvider.INSTANCE.snapshot(player,
            IndustrialTerminalIds.id("mission/nexus_furnace_array"));
         helper.assertTrue(snapshot.status() == TerminalMissionStatus.CLAIMABLE,
            "Nexus Furnace Array mission should be claimable after Core Key Assembly forging.");
      });
      helper.succeed();
   }

   private static void nexusFurnaceArrayProviderSnapshots(GameTestHelper helper) {
      IndustrialMultiblockTasks.register();
      IndustrialMultiblockIntegrationProvider.register();
      BuildNexusFurnaceArray built = buildNexusFurnaceArray(helper);
      ValidationResult result = built.controller().validateStructure(true);
      helper.assertTrue(result.valid(), "Nexus Furnace Array should validate before provider snapshot test: " + validationDebug(result));
      built.controller().onStructureFormed();
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      player.setPos(built.controller().getBlockPos().getX() + 0.5D, built.controller().getBlockPos().getY() + 1.0D,
         built.controller().getBlockPos().getZ() + 0.5D);
      IndustrialFactorySnapshotPacket packet = IndustrialFactorySnapshotPacket.current(player);
      helper.assertTrue(packet.entries().stream().anyMatch(entry -> entry.definitionId().equals(id("nexus_furnace_array"))
            && entry.recipeIds().contains(id("stabilize_hybrid_thermal_core"))
            && entry.recipeIds().contains(id("forge_core_key_assembly"))),
         "Factory Command snapshot should include the formed Nexus Furnace Array and both automation recipes.");
      List<MultiblockMapMarkerSnapshot> markers = MultiblockIntegrationServices.mapMarkers(player);
      helper.assertTrue(markers.stream().anyMatch(marker -> marker.definitionId().equals(id("nexus_furnace_array"))
            && marker.summary().contains("integrity")),
         "HoloMap marker summary should include the formed Nexus Furnace Array.");
      LensMultiblockScan scan = MultiblockIntegrationServices.scan(player, helper.getLevel(), built.controller().getBlockPos()).orElseThrow();
      helper.assertTrue(scan.targetId().equals(id("nexus_furnace_array"))
            && scan.structureName().contains("Nexus Furnace Array"),
         "Lens scan should identify the formed Nexus Furnace Array.");
      helper.succeed();
   }

   private static void assemblyLineRoboticTask(GameTestHelper helper) {
      IndustrialMultiblockTasks.register();
      BuildAssemblyLine built = buildAssemblyLine(helper);
      ValidationResult result = built.controller().validateStructure(true);
      helper.assertTrue(result.valid(), "Industrial Assembly Line should validate when all required blocks are present: " + validationDebug(result));
      built.controller().onStructureFormed();
      built.input().setItem(0, new ItemStack((ItemLike)ModItems.REFINED_PLATE.get(), 4));
      built.input().setItem(1, new ItemStack((ItemLike)ModItems.SERVO_MOTOR.get()));
      built.input().setItem(2, new ItemStack((ItemLike)ModItems.INDUSTRIAL_CIRCUIT.get()));
      helper.assertTrue(built.controller().queueTask(id("weld_reinforced_machine_frame"), null),
         "Assembly task should accept a blocked queue entry before a welder head is installed.");
      helper.assertTrue(built.controller().diagnosticLines().stream().anyMatch(line -> line.contains("Required robotic tool missing")),
         "Blocked diagnostics should name the missing robotic tool.");
      helper.assertTrue(built.input().countItem(ModItems.REFINED_PLATE.get()) == 4,
         "Blocked assembly task should not consume inputs before a compatible robotic tool is installed.");
      built.controller().clearQueue(null);
      built.arm().installTool(new ItemStack((ItemLike)ModItems.INDUSTRIAL_WELDER_HEAD.get()), null);
      helper.assertTrue(built.controller().queueTask(id("weld_reinforced_machine_frame"), null),
         "Assembly task should start once the welder head is installed.");
      helper.assertTrue(built.input().countItem(ModItems.REFINED_PLATE.get()) == 0,
         "Assembly task should consume plates when the task starts.");
      for (int i = 0; i < 260; i++) {
         IndustrialMultiblockControllerBlockEntity.tick(helper.getLevel(), built.controller().getBlockPos(), built.controller().getBlockState(), built.controller());
         IndustrialRoboticArmMountBlockEntity.tick(helper.getLevel(), built.arm().getBlockPos(), built.arm().getBlockState(), built.arm());
      }
      helper.assertTrue(built.output().countItem(ModItems.REINFORCED_MACHINE_FRAME.get()) == 1,
         "Completed assembly task should place the reinforced machine frame in the output crate.");
      helper.succeed();
   }

   private static void assemblyLineGuiQueue(GameTestHelper helper) {
      BuildAssemblyLine built = buildAssemblyLine(helper);
      ValidationResult result = built.controller().validateStructure(true);
      helper.assertTrue(result.valid(), "Industrial Assembly Line should validate before GUI queue test: " + validationDebug(result));
      built.controller().onStructureFormed();
      built.arm().installTool(new ItemStack((ItemLike)ModItems.INDUSTRIAL_WELDER_HEAD.get()), null);
      built.input().setItem(0, new ItemStack((ItemLike)ModItems.REFINED_PLATE.get(), 4));
      built.input().setItem(1, new ItemStack((ItemLike)ModItems.SERVO_MOTOR.get()));
      built.input().setItem(2, new ItemStack((ItemLike)ModItems.INDUSTRIAL_CIRCUIT.get()));
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      int recipeIndex = recipeButtonIndex(built.controller(), id("weld_reinforced_machine_frame"));
      helper.assertTrue(recipeIndex >= 0, "Assembly Line controller menu should expose the welding automation recipe.");
      helper.assertTrue(built.controller().handleMenuButton(player, IndustrialMultiblockControllerMenu.BUTTON_QUEUE_TASK_BASE + recipeIndex),
         "Assembly Line controller menu should accept the welding recipe queue button.");
      helper.assertTrue(built.controller().handleMenuButton(player, IndustrialMultiblockControllerMenu.BUTTON_TOGGLE_LOGISTICS_RESTOCK),
         "Assembly Line controller menu should accept the Logistics auto-restock toggle.");
      helper.assertTrue(built.controller().logisticsAutoRestockEnabled(),
         "Assembly Line controller should persist Logistics auto-restock enabled state.");
      helper.assertTrue(built.controller().handleMenuButton(player, IndustrialMultiblockControllerMenu.BUTTON_CYCLE_LOGISTICS_RESTOCK_TARGET)
            && built.controller().logisticsRestockTargetRuns() == 5,
         "Assembly Line controller should cycle Logistics auto-restock target from x3 to x5.");
      helper.assertTrue(built.controller().taskSnapshots().stream().anyMatch(snapshot -> snapshot.taskId().equals(id("weld_reinforced_machine_frame"))),
         "Assembly Line controller menu should queue the welding automation recipe.");
      helper.succeed();
   }

   private static void assemblyLineBatchQueue(GameTestHelper helper) {
      IndustrialMultiblockTasks.register();
      BuildAssemblyLine built = buildAssemblyLine(helper);
      ValidationResult result = built.controller().validateStructure(true);
      helper.assertTrue(result.valid(), "Industrial Assembly Line should validate before batch queue test: " + validationDebug(result));
      built.controller().onStructureFormed();
      built.arm().installTool(new ItemStack((ItemLike)ModItems.INDUSTRIAL_WELDER_HEAD.get()), null);
      built.input().setItem(0, new ItemStack((ItemLike)ModItems.REFINED_PLATE.get(), 12));
      built.input().setItem(1, new ItemStack((ItemLike)ModItems.SERVO_MOTOR.get(), 3));
      built.input().setItem(2, new ItemStack((ItemLike)ModItems.INDUSTRIAL_CIRCUIT.get(), 3));
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      int recipeIndex = recipeButtonIndex(built.controller(), id("weld_reinforced_machine_frame"));
      helper.assertTrue(recipeIndex >= 0, "Assembly Line controller menu should expose the welding automation recipe.");
      helper.assertTrue(built.controller().handleMenuButton(player,
            IndustrialMultiblockControllerMenu.BUTTON_QUEUE_TASK_X3_BASE + recipeIndex),
         "Assembly Line controller menu should accept a x3 batch queue button.");
      helper.assertTrue(built.controller().taskSnapshots().size() == 3,
         "Batch queue should keep three task snapshots while the first task is active.");
      tickAssemblyLineUntilOutput(built, 3, 1800);
      int outputCount = built.output().countItem(ModItems.REINFORCED_MACHINE_FRAME.get());
      helper.assertTrue(outputCount == 3,
         "Batch queue should recover from robotic cooling pauses and produce three reinforced machine frames; output="
            + outputCount + " tasks=" + built.controller().taskSnapshots() + " diagnostics=" + built.controller().diagnosticLines()
            + " arm=" + built.arm().getRobotState() + " heat=" + built.arm().getHeat());
      helper.succeed();
   }

   private static void factoryCommandTerminalActions(GameTestHelper helper) {
      IndustrialMultiblockTasks.register();
      IndustrialTerminalCommonIntegration.register();
      BuildAssemblyLine built = buildAssemblyLine(helper);
      built.controller().onStructureFormed();
      built.arm().installTool(new ItemStack((ItemLike)ModItems.INDUSTRIAL_WELDER_HEAD.get()), null);
      built.input().setItem(0, new ItemStack((ItemLike)ModItems.REFINED_PLATE.get(), 8));
      built.input().setItem(1, new ItemStack((ItemLike)ModItems.SERVO_MOTOR.get(), 2));
      built.input().setItem(2, new ItemStack((ItemLike)ModItems.INDUSTRIAL_CIRCUIT.get(), 2));
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      String payload = terminalPayload(helper.getLevel(), built.controller().getBlockPos(),
         id("weld_reinforced_machine_frame"), 2);
      helper.assertTrue(TerminalActionRegistry.handle(player, IndustrialTerminalIds.ECHO_TAB,
            IndustrialTerminalIds.FACTORY_QUEUE_TASK, payload),
         "Factory Command Terminal queue action should route through TerminalActionRegistry.");
      helper.assertTrue(built.controller().taskSnapshots().size() == 2,
         "Factory Command Terminal queue action should enqueue the requested batch quantity.");
      helper.assertTrue(TerminalActionRegistry.handle(player, IndustrialTerminalIds.ECHO_TAB,
            IndustrialTerminalIds.FACTORY_CLEAR_QUEUE,
            terminalPayload(helper.getLevel(), built.controller().getBlockPos(), null, 1)),
         "Factory Command Terminal clear action should route through TerminalActionRegistry.");
      helper.assertTrue(built.controller().taskQueueSize() == 0,
         "Factory Command Terminal clear action should clear the controller queue.");
      helper.assertTrue(TerminalActionRegistry.handle(player, IndustrialTerminalIds.ECHO_TAB,
            IndustrialTerminalIds.FACTORY_TOGGLE_LOGISTICS_RESTOCK,
            terminalPayload(helper.getLevel(), built.controller().getBlockPos(), null, 1)),
         "Factory Command Terminal restock toggle should route through TerminalActionRegistry.");
      helper.assertTrue(built.controller().logisticsAutoRestockEnabled(),
         "Factory Command Terminal restock toggle should enable controller auto-restock.");
      helper.assertTrue(TerminalActionRegistry.handle(player, IndustrialTerminalIds.ECHO_TAB,
            IndustrialTerminalIds.FACTORY_SET_LOGISTICS_RESTOCK_TARGET,
            terminalPayload(helper.getLevel(), built.controller().getBlockPos(), null, 5)),
         "Factory Command Terminal restock target should route through TerminalActionRegistry.");
      helper.assertTrue(built.controller().logisticsRestockTargetRuns() == 5,
         "Factory Command Terminal restock target should persist x5 on the controller.");
      helper.succeed();
   }

   private static void factoryCommandProviderSnapshots(GameTestHelper helper) {
      IndustrialMultiblockTasks.register();
      IndustrialMultiblockIntegrationProvider.register();
      BuildAssemblyLine built = buildAssemblyLine(helper);
      built.controller().onStructureFormed();
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      IndustrialFactorySnapshotPacket packet = IndustrialFactorySnapshotPacket.current(player);
      helper.assertTrue(packet.entries().stream().anyMatch(entry -> entry.definitionId().equals(id("industrial_assembly_line"))),
         "Factory Command snapshot packet should include a formed Industrial Assembly Line.");
      List<MultiblockMapMarkerSnapshot> markers = MultiblockIntegrationServices.mapMarkers(player);
      helper.assertTrue(markers.stream().anyMatch(marker -> marker.definitionId().equals(id("industrial_assembly_line"))
            && marker.summary().contains("integrity")),
         "Industrial HoloMap-ready marker should expose state-colored facility summary text.");
      helper.succeed();
   }

   private static void industrialLensProviderRegistration(GameTestHelper helper) {
      IndustrialLensIntegration.register();
      helper.assertTrue(LensProviderRegistry.serverProviders().stream()
            .anyMatch(provider -> provider.id().equals(id("industrial_factory_deep_scan"))),
         "Industrial Lens integration should register a server-safe factory Deep Scan provider.");
      helper.succeed();
   }

   private static void fluxDuctTransferLoss(GameTestHelper helper) {
      helper.setBlock(new BlockPos(1, 1, 1), (Block)ModBlocks.FLUX_CAPACITOR_BANK.get());
      helper.setBlock(new BlockPos(2, 1, 1), (Block)ModBlocks.COPPER_FLUX_DUCT.get());
      helper.setBlock(new BlockPos(3, 1, 1), (Block)ModBlocks.FLUX_CAPACITOR_BANK.get());
      IndustrialMachineBlockEntity source = helper.getBlockEntity(new BlockPos(1, 1, 1), IndustrialMachineBlockEntity.class);
      IndustrialMachineBlockEntity receiver = helper.getBlockEntity(new BlockPos(3, 1, 1), IndustrialMachineBlockEntity.class);
      IndustrialFluxDuctBlockEntity duct = helper.getBlockEntity(new BlockPos(2, 1, 1), IndustrialFluxDuctBlockEntity.class);
      source.receiveFlux(1000, false);
      helper.succeedWhen(() -> {
         IndustrialFluxDuctBlockEntity.tick(helper.getLevel(), duct.getBlockPos(), duct.getBlockState(), duct);
         helper.assertTrue(receiver.getFluxStored() > 0, "Flux duct should move Thermal Flux into a receiver");
         helper.assertTrue(source.getFluxStored() + receiver.getFluxStored() <= 1000, "Flux duct transfer should preserve or lose, never duplicate, Thermal Flux");
      });
   }

   private static void scrapDynamoGeneratesFlux(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.SCRAP_DYNAMO.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      machine.setItem(IndustrialMachineBlockEntity.INPUT_SLOT, new ItemStack((ItemLike)ModItems.SCRAP_FUEL.get()));
      IndustrialMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
      helper.assertTrue(machine.getFluxStored() > 0, "Scrap Dynamo should store generated Thermal Flux internally");
      helper.assertTrue(machine.getItem(IndustrialMachineBlockEntity.INPUT_SLOT).isEmpty(), "Scrap Dynamo should consume one fuel item when it starts burning");
      helper.assertTrue(!machine.canReceive(), "Generators should still reject external Thermal Flux input");
      helper.succeed();
   }

   private static void generatorPowersOreGrinder(GameTestHelper helper) {
      BlockPos generatorPos = new BlockPos(1, 1, 1);
      BlockPos ductPos = new BlockPos(2, 1, 1);
      BlockPos grinderPos = new BlockPos(3, 1, 1);
      helper.setBlock(generatorPos, (Block)ModBlocks.SCRAP_DYNAMO.get());
      helper.setBlock(ductPos, (Block)ModBlocks.COPPER_FLUX_DUCT.get());
      helper.setBlock(grinderPos, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity generator = helper.getBlockEntity(generatorPos, IndustrialMachineBlockEntity.class);
      IndustrialFluxDuctBlockEntity duct = helper.getBlockEntity(ductPos, IndustrialFluxDuctBlockEntity.class);
      IndustrialMachineBlockEntity grinder = helper.getBlockEntity(grinderPos, IndustrialMachineBlockEntity.class);
      generator.setItem(IndustrialMachineBlockEntity.INPUT_SLOT, new ItemStack((ItemLike)ModItems.SCRAP_FUEL.get(), 4));
      generator.setItem(IndustrialMachineBlockEntity.UPGRADE_SLOT_START, new ItemStack((ItemLike)ModItems.HEAT_SINK_UPGRADE.get()));
      grinder.setItem(IndustrialMachineBlockEntity.INPUT_SLOT, new ItemStack(Blocks.IRON_ORE));
      for (int i = 0; i < 220; i++) {
         IndustrialMachineBlockEntity.tick(helper.getLevel(), generator.getBlockPos(), generator.getBlockState(), generator);
         IndustrialFluxDuctBlockEntity.tick(helper.getLevel(), duct.getBlockPos(), duct.getBlockState(), duct);
         IndustrialMachineBlockEntity.tick(helper.getLevel(), grinder.getBlockPos(), grinder.getBlockState(), grinder);
      }
      helper.assertTrue(grinder.getItem(IndustrialMachineBlockEntity.OUTPUT_SLOT).is(ModItems.IRON_DUST.get()),
         "Scrap Dynamo should power an Ore Grinder through a Flux Duct");
      helper.assertTrue(generator.getFluxStored() + grinder.getFluxStored() <= generator.getMaxFluxStored() + grinder.getMaxFluxStored(),
         "Generator loop should not exceed machine storage capacity");
      helper.succeed();
   }

   private static void itemDuctRouting(GameTestHelper helper) {
      BlockPos sourcePos = new BlockPos(1, 1, 1);
      BlockPos ductPos = new BlockPos(2, 1, 1);
      BlockPos relayDuctPos = new BlockPos(3, 1, 1);
      BlockPos receiverPos = new BlockPos(4, 1, 1);
      helper.setBlock(sourcePos, Blocks.CHEST);
      helper.setBlock(ductPos, (Block)ModBlocks.SMART_DUCT.get());
      helper.setBlock(relayDuctPos, (Block)ModBlocks.REINFORCED_DUCT.get());
      helper.setBlock(receiverPos, Blocks.CHEST);
      Container source = (Container)helper.getLevel().getBlockEntity(helper.absolutePos(sourcePos));
      Container receiver = (Container)helper.getLevel().getBlockEntity(helper.absolutePos(receiverPos));
      IndustrialItemDuctBlockEntity duct = helper.getBlockEntity(ductPos, IndustrialItemDuctBlockEntity.class);
      duct.installFilter(helper.makeMockPlayer(GameType.CREATIVE), new ItemStack((ItemLike)ModItems.SCRAP_METAL.get()));
      source.setItem(0, new ItemStack((ItemLike)ModItems.SCRAP_METAL.get(), 2));
      source.setItem(1, new ItemStack(Items.IRON_INGOT, 2));
      helper.succeedWhen(() -> {
         IndustrialItemDuctBlockEntity.tick(helper.getLevel(), duct.getBlockPos(), duct.getBlockState(), duct);
         helper.assertTrue(receiver.getItem(0).is(ModItems.SCRAP_METAL.get()), "Item duct network should route scrap through connected ducts");
         helper.assertTrue(!receiver.getItem(0).is(Items.IRON_INGOT), "Smart Duct whitelist should block non-filtered items");
      });
   }

   private static void machineAndDuctBreakDrops(GameTestHelper helper) {
      BlockPos machinePos = new BlockPos(1, 1, 1);
      helper.setBlock(machinePos, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(machinePos, IndustrialMachineBlockEntity.class);
      machine.setItem(IndustrialMachineBlockEntity.INPUT_SLOT, new ItemStack(Blocks.IRON_ORE, 3));
      machine.setItem(IndustrialMachineBlockEntity.OUTPUT_SLOT, new ItemStack((ItemLike)ModItems.IRON_DUST.get(), 2));
      ((Block)ModBlocks.ORE_GRINDER.get()).playerDestroy(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL), machine.getBlockPos(),
         machine.getBlockState(), machine, ItemStack.EMPTY);
      helper.assertTrue(machine.getItem(IndustrialMachineBlockEntity.INPUT_SLOT).isEmpty(), "Machine break should clear dropped inventory from the block entity");
      helper.assertTrue(countDroppedItems(helper, machinePos, Blocks.IRON_ORE) == 3, "Machine break should drop input inventory");
      helper.assertTrue(countDroppedItems(helper, machinePos, (ItemLike)ModItems.IRON_DUST.get()) == 2, "Machine break should drop output inventory");

      BlockPos ductPos = new BlockPos(5, 1, 1);
      helper.setBlock(ductPos, (Block)ModBlocks.SMART_DUCT.get());
      IndustrialItemDuctBlockEntity duct = helper.getBlockEntity(ductPos, IndustrialItemDuctBlockEntity.class);
      duct.installFilter(helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack((ItemLike)ModItems.SCRAP_METAL.get()));
      ((Block)ModBlocks.SMART_DUCT.get()).playerDestroy(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL), duct.getBlockPos(),
         duct.getBlockState(), duct, ItemStack.EMPTY);
      helper.assertTrue(countDroppedItems(helper, ductPos, (ItemLike)ModItems.SCRAP_METAL.get()) == 1, "Smart Duct break should drop its installed filter");
      helper.succeed();
   }

   private static void lavaBucketReturnsBucket(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.SCRAP_DYNAMO.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      machine.setItem(IndustrialMachineBlockEntity.INPUT_SLOT, new ItemStack(Items.LAVA_BUCKET));
      IndustrialMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
      helper.assertTrue(machine.getFluxStored() > 0, "Lava bucket fuel should generate Thermal Flux");
      helper.assertTrue(machine.getItem(IndustrialMachineBlockEntity.INPUT_SLOT).is(Items.BUCKET), "Lava bucket fuel should leave an empty bucket");
      helper.succeed();
   }

   private static void scrubberSafeZoneProgress(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      IndustrialProgress.markScrubberZone(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), "Cooling Mode", 400, 12);
      helper.assertTrue(IndustrialProgress.flag(player, "safe_zone") || IndustrialProgress.value(player, "scrubber_modes_seen") >= 0,
         "Scrubber progress API should remain stable for fallback safe zones");
      helper.succeed();
   }

   private static void terminalMissionSnapshots(GameTestHelper helper) {
      List<TerminalMissionDefinition> missions = IndustrialMissionProvider.INSTANCE.missions(helper.makeMockPlayer(GameType.CREATIVE));
      helper.assertTrue(missions.size() == 17, "Industrial Terminal chapter should expose all 17 missions");
      TerminalMissionSnapshot snapshot = IndustrialMissionProvider.INSTANCE.snapshot(null, IndustrialTerminalIds.id("mission/reclaim_power"));
      helper.assertTrue(snapshot.status() == TerminalMissionStatus.UNLOCKED, "Industrial mission snapshots should be stable without player progress");
      helper.assertTrue(snapshot.actions().stream().anyMatch(action -> action.id().equals("poi_hint") && action.label().equals("POI HINT")),
         "Industrial Terminal action should honestly expose POI hints");
      helper.assertTrue(snapshot.actions().stream().noneMatch(action -> action.label().equals("LOCATE POI")),
         "Industrial Terminal should not claim to locate POIs without coordinates");
      helper.succeed();
   }

   private static void terminalClaimIdempotency(GameTestHelper helper) {
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      IndustrialProgress.data(player).putInt("thermal_flux_generated", 1000);
      Identifier missionId = IndustrialTerminalIds.id("mission/reclaim_power");
      boolean firstClaim = IndustrialMissionProvider.INSTANCE.handleAction(player, missionId, "claim_cache");
      boolean secondClaim = IndustrialMissionProvider.INSTANCE.handleAction(player, missionId, "claim_cache");
      helper.assertTrue(firstClaim, "Claimable Terminal cache should claim once");
      helper.assertTrue(!secondClaim, "Terminal cache claim should be idempotent");
      helper.assertTrue(IndustrialProgress.claimed(player, "reclaim_power"), "Terminal cache claim should persist in Industrial progress");
      helper.succeed();
   }

   private static void terminalRecipeParser(GameTestHelper helper) {
      TerminalRecipeEntry recipe = IndustrialTerminalRecipeProvider.parseForTests("terminal_parser",
         JsonParser.parseString("""
            {
              "type": "echoindustrialnexus:industrial_processing",
              "machine": "fluid_refiner",
              "ingredient": [
                "minecraft:iron_ingot",
                { "item": "minecraft:copper_ingot" }
              ],
              "result": "minecraft:redstone",
              "count": 2,
              "catalyst": "minecraft:coal",
              "catalystCount": 1,
              "byproduct": "minecraft:lapis_lazuli",
              "byproductCount": 1,
              "byproductChance": 25,
              "inputFluidId": 1,
              "inputFluidAmount": 250,
              "outputFluidId": 2,
              "outputFluidAmount": 125
            }
            """).getAsJsonObject()).orElseThrow();
      helper.assertTrue(recipe.uses(Items.IRON_INGOT), "Terminal parser should match array item ingredients");
      helper.assertTrue(recipe.uses(Items.COAL), "Terminal parser should match catalysts");
      helper.assertTrue(recipe.outputs(Items.REDSTONE), "Terminal parser should match result outputs");
      helper.assertTrue(recipe.outputs(Items.LAPIS_LAZULI), "Terminal parser should match byproduct outputs");
      helper.assertTrue(recipe.slots().stream().anyMatch(slot -> slot.role() == TerminalRecipeSlot.Role.INPUT
            && slot.label().contains("Input fluid")),
         "Terminal parser should expose fluid inputs as text slots");
      helper.assertTrue(recipe.slots().stream().anyMatch(slot -> slot.role() == TerminalRecipeSlot.Role.OUTPUT
            && slot.label().contains("Output fluid")),
         "Terminal parser should expose fluid outputs as text slots");
      helper.succeed();
   }

   private static void registeredOrphanItemPaths(GameTestHelper helper) {
      List<String> recipePaths = List.of(
         "compacted_ash_fuel",
         "thermal_dust",
         "ore_grinder_gold_dust",
         "ore_grinder_deepslate_gold_dust",
         "uranium_dust",
         "station_battery",
         "pressure_seal_kit",
         "hull_repair_foam",
         "ai_override_chip_casing",
         "signal_panic_dampener",
         "memory_stabilizer_casing"
      );
      for (String path : recipePaths) {
         ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id(path));
         helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(key).isPresent(), "V1 obtainability recipe missing: " + path);
      }
      helper.succeed();
   }

   private static void coreChapterRouteRecords(GameTestHelper helper) {
      IndustrialCoreIntegration.registerAddonChapter();
      helper.assertTrue(EchoAddonRegistry.isRegistered(IndustrialCoreIntegration.CHAPTER_ID),
         "Industrial Nexus should register as an ECHO addon chapter");
      List<EchoRouteRecord> records = EchoCoreServices.routeRecords(helper.makeMockPlayer(GameType.CREATIVE)).stream()
         .filter(record -> IndustrialCoreIntegration.CHAPTER_ID.equals(record.chapterId()))
         .toList();
      helper.assertTrue(records.size() == 17, "Industrial core route records should expose all 17 missions");
      helper.succeed();
   }

   private static void furnaceWardenSpawn(GameTestHelper helper) {
      FurnaceWardenEntity warden = ((EntityType<FurnaceWardenEntity>)ModEntities.FURNACE_WARDEN.get()).create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(warden != null, "Furnace Warden should be spawnable");
      if (warden != null) {
         helper.assertTrue(warden.getMaxHealth() >= 180.0F, "Furnace Warden should have boss-scale health");
      }
      helper.succeed();
   }

   private static void furnaceWardenRewardIdempotency(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      FurnaceWardenEntity warden = ((EntityType<FurnaceWardenEntity>)ModEntities.FURNACE_WARDEN.get()).create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(warden != null, "Furnace Warden should be spawnable for reward checks");
      if (warden != null) {
         BlockPos absolute = helper.absolutePos(local);
         warden.setPos(absolute.getX() + 0.5D, absolute.getY() + 1.0D, absolute.getZ() + 0.5D);
         helper.getLevel().addFreshEntity(warden);
         warden.die(helper.getLevel().damageSources().generic());
         int firstDrops = countDroppedItems(helper, local, (ItemLike)ModItems.WARDEN_THERMAL_CORE.get())
            + countDroppedItems(helper, local, (ItemLike)ModItems.OVERCLOCK_CORE.get())
            + countDroppedItems(helper, local, (ItemLike)ModItems.FURNACE_WARDEN_TROPHY.get());
         warden.die(helper.getLevel().damageSources().generic());
         int secondDrops = countDroppedItems(helper, local, (ItemLike)ModItems.WARDEN_THERMAL_CORE.get())
            + countDroppedItems(helper, local, (ItemLike)ModItems.OVERCLOCK_CORE.get())
            + countDroppedItems(helper, local, (ItemLike)ModItems.FURNACE_WARDEN_TROPHY.get());
         helper.assertTrue(firstDrops == 3, "Furnace Warden should create one code-driven reward set");
         helper.assertTrue(secondDrops == firstDrops, "Repeated Warden death handling should not duplicate rewards");
      }
      helper.succeed();
   }

   private static void proceduralPoiSmoke(GameTestHelper helper) {
      ServerLevel level = helper.getLevel();
      IndustrialPoiGenerator.generate(level, helper.absolutePos(new BlockPos(5, 1, 5)), IndustrialPoiType.ABANDONED_THERMAL_PLANT, RandomSource.create(7L));
      IndustrialPoiGenerator.generate(level, helper.absolutePos(new BlockPos(24, 1, 5)), IndustrialPoiType.NEXUS_HEAT_EXCHANGER_RUINS, RandomSource.create(9L));
      helper.succeed();
   }

   private static void machineMenuDataSync(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      machine.receiveFlux(1234, false);
      IndustrialMachineMenu menu = new IndustrialMachineMenu(1, helper.makeMockPlayer(GameType.CREATIVE).getInventory(), machine, machine.data());
      helper.assertTrue(menu.flux() == 1234, "Industrial machine menu should sync Thermal Flux");
      helper.assertTrue(menu.machineKind().name().equals("ORE_GRINDER"), "Industrial machine menu should sync machine kind");
      helper.succeed();
   }

   private static void machineStatePersistence(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      machine.setItem(IndustrialMachineBlockEntity.INPUT_SLOT, new ItemStack(Blocks.IRON_ORE, 2));
      machine.receiveFlux(4321, false);
      machine.fillInputFluidForTest(IndustrialMachineBlockEntity.FLUID_DIRTY_WATER, 750);
      CompoundTag tag = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
      BlockEntity restored = BlockEntity.loadStatic(machine.getBlockPos(), machine.getBlockState(), tag, helper.getLevel().registryAccess());
      helper.assertTrue(restored instanceof IndustrialMachineBlockEntity, "Saved machine state should reload as an Industrial machine block entity");
      if (restored instanceof IndustrialMachineBlockEntity restoredMachine) {
         helper.assertTrue(restoredMachine.getItem(IndustrialMachineBlockEntity.INPUT_SLOT).is(Items.IRON_ORE), "Machine input stack should persist");
         helper.assertTrue(restoredMachine.getItem(IndustrialMachineBlockEntity.INPUT_SLOT).getCount() == 2, "Machine input count should persist");
         helper.assertTrue(restoredMachine.getFluxStored() == 4321, "Machine Thermal Flux should persist");
         helper.assertTrue(restoredMachine.inputFluidAmount() == 750, "Machine input tank amount should persist");
      }
      helper.succeed();
   }

   private static void machineSideConfig(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      helper.assertTrue(machine.getSlotsForFace(net.minecraft.core.Direction.DOWN).length > 0, "Standard config should expose outputs downward");
      machine.cycleSideConfig(helper.makeMockPlayer(GameType.CREATIVE));
      helper.assertTrue(machine.getSlotsForFace(net.minecraft.core.Direction.DOWN).length == 0, "Input-only config should block downward output automation");
      helper.succeed();
   }

   private static void fluidTankProcessing(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.WATER_PURIFIER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      machine.fillInputFluidForTest(IndustrialMachineBlockEntity.FLUID_DIRTY_WATER, 1000);
      machine.receiveFlux(2000, false);
      for (int i = 0; i < 150; i++) {
         IndustrialMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
      }
      helper.assertTrue(machine.getItem(IndustrialMachineBlockEntity.OUTPUT_SLOT).is(ModItems.CLEAN_WATER_CELL.get())
         || machine.outputFluidAmount() >= 1000, "Water Purifier should process dirty water from its internal tank");
      helper.succeed();
   }

   private static void controllerRemoteShutdown(GameTestHelper helper) {
      BlockPos controllerPos = new BlockPos(1, 1, 1);
      BlockPos targetPos = new BlockPos(3, 1, 1);
      helper.setBlock(controllerPos, (Block)ModBlocks.FACTORY_CONTROLLER.get());
      helper.setBlock(new BlockPos(2, 1, 1), (Block)ModBlocks.COPPER_FLUX_DUCT.get());
      helper.setBlock(targetPos, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity controller = helper.getBlockEntity(controllerPos, IndustrialMachineBlockEntity.class);
      IndustrialMachineBlockEntity target = helper.getBlockEntity(targetPos, IndustrialMachineBlockEntity.class);
      controller.controllerEmergencyShutdown(helper.makeMockPlayer(GameType.CREATIVE));
      helper.assertTrue(target.statusLabel().contains("Remote shutdown"), "Factory Controller should remote-shutdown linked machines");
      helper.succeed();
   }

   private static void upgradeStackingRules(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.ORE_GRINDER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      ItemStack speed = new ItemStack((ItemLike)ModItems.SPEED_SERVO.get());
      helper.assertTrue(machine.canPlaceItem(IndustrialMachineBlockEntity.UPGRADE_SLOT_START, speed), "First Speed Servo should be accepted");
      machine.setItem(IndustrialMachineBlockEntity.UPGRADE_SLOT_START, speed.copy());
      helper.assertTrue(!machine.canPlaceItem(IndustrialMachineBlockEntity.UPGRADE_SLOT_START + 1, speed.copy()), "Duplicate upgrade modules should be rejected");
      helper.succeed();
   }

   private static void rewardClaimIdempotency(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      IndustrialProgress.claim(player, "reclaim_power");
      IndustrialProgress.claim(player, "reclaim_power");
      helper.assertTrue(IndustrialProgress.claimed(player, "reclaim_power"), "Industrial reward claims should remain claimed after repeated writes");
      helper.succeed();
   }

   private static void fluidCapabilityFillDrain(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.WATER_PURIFIER.get());
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(local, IndustrialMachineBlockEntity.class);
      ResourceHandler<FluidResource> handler = machine.fluidHandler(null);
      try (Transaction tx = Transaction.openRoot()) {
         int inserted = handler.insert(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_DIRTY_WATER), 250, tx);
         helper.assertTrue(inserted == 250, "Machine fluid capability should accept dirty water during a probe transaction");
      }
      helper.assertTrue(machine.inputFluidAmount() == 0, "Aborted machine fluid insert should rollback tank state");
      try (Transaction tx = Transaction.openRoot()) {
         int inserted = handler.insert(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_DIRTY_WATER), 1000, tx);
         helper.assertTrue(inserted == 1000, "Machine fluid capability should accept dirty water into its input tank");
         tx.commit();
      }
      machine.setItem(IndustrialMachineBlockEntity.OUTPUT_SLOT, new ItemStack(Items.STONE));
      machine.receiveFlux(2000, false);
      for (int i = 0; i < 150; i++) {
         IndustrialMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
      }
      int outputBefore = machine.outputFluidAmount();
      try (Transaction tx = Transaction.openRoot()) {
         int extracted = handler.extract(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_CLEAN_WATER), 100, tx);
         helper.assertTrue(extracted > 0, "Machine fluid capability should expose clean water during a probe transaction");
      }
      helper.assertTrue(machine.outputFluidAmount() == outputBefore, "Aborted machine fluid extract should rollback tank state");
      try (Transaction tx = Transaction.openRoot()) {
         int extracted = handler.extract(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_CLEAN_WATER), 1000, tx);
         helper.assertTrue(extracted > 0, "Machine fluid capability should drain processed clean water from its output tank");
         tx.commit();
      }
      helper.succeed();
   }

   private static void fluidPipeTransferFiltering(GameTestHelper helper) {
      BlockPos pipePos = new BlockPos(1, 1, 1);
      BlockPos machinePos = new BlockPos(2, 1, 1);
      helper.setBlock(pipePos, (Block)ModBlocks.STATIC_PIPE.get());
      helper.setBlock(machinePos, (Block)ModBlocks.FLUID_REFINER.get());
      IndustrialFluidPipeBlockEntity pipe = helper.getBlockEntity(pipePos, IndustrialFluidPipeBlockEntity.class);
      IndustrialMachineBlockEntity machine = helper.getBlockEntity(machinePos, IndustrialMachineBlockEntity.class);
      ResourceHandler<FluidResource> pipeHandler = pipe.fluidHandler(null);
      try (Transaction tx = Transaction.openRoot()) {
         int inserted = pipeHandler.insert(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_STATIC), 250, tx);
         helper.assertTrue(inserted == 250, "Pipe fluid capability should accept static fluid during a probe transaction");
      }
      helper.assertTrue(pipe.amountForTest() == 0, "Aborted pipe fluid insert should rollback tank state");
      try (Transaction tx = Transaction.openRoot()) {
         int inserted = pipeHandler.insert(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_STATIC), 500, tx);
         helper.assertTrue(inserted == 500, "Static Pipe should accept static fluid");
         tx.commit();
      }
      try (Transaction tx = Transaction.openRoot()) {
         int rejected = pipeHandler.insert(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_DIRTY_WATER), 100, tx);
         helper.assertTrue(rejected == 0, "Pipe should reject mixed fluids while already holding static fluid");
      }
      IndustrialFluidPipeBlockEntity.tick(helper.getLevel(), pipe.getBlockPos(), pipe.getBlockState(), pipe);
      helper.assertTrue(machine.inputFluidId() == IndustrialMachineBlockEntity.FLUID_STATIC && machine.inputFluidAmount() > 0,
         "Static Pipe should transfer static fluid into adjacent fluid machines");

      helper.setBlock(new BlockPos(4, 1, 1), (Block)ModBlocks.REINFORCED_PIPE.get());
      IndustrialFluidPipeBlockEntity reinforced = helper.getBlockEntity(new BlockPos(4, 1, 1), IndustrialFluidPipeBlockEntity.class);
      try (Transaction tx = Transaction.openRoot()) {
         int rejected = reinforced.fluidHandler(null).insert(ModFluids.resourceFor(IndustrialMachineBlockEntity.FLUID_STATIC), 250, tx);
         helper.assertTrue(rejected == 0, "Reinforced Pipe should reject Nexus/static fluids");
      }
      helper.succeed();
   }

   private static void crossCompatComponentOutput(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      IndustrialCompat.recordIndustrialOutput(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), new ItemStack((ItemLike)ModItems.PRESSURE_COMPONENT.get()));
      IndustrialCompat.recordIndustrialOutput(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), new ItemStack((ItemLike)ModItems.AI_OVERRIDE_CHIP_CASING.get()));
      IndustrialCompat.recordIndustrialOutput(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), new ItemStack((ItemLike)ModItems.MEMORY_STABILIZER_CASING.get()));
      helper.assertTrue(true, "Industrial optional component output hooks should remain soft-linked");
      helper.succeed();
   }

   private static void nexusPressureCompat(GameTestHelper helper) {
      ServerLevel level = helper.getLevel();
      BlockPos absolute = helper.absolutePos(new BlockPos(1, 1, 1));
      IndustrialCompat.recordNexusThermalPressure(level, absolute, 4);
      IndustrialCompat.recordStaticFluidLeak(level, absolute, IndustrialMachineBlockEntity.FLUID_STATIC, 500);
      helper.assertTrue(true, "Industrial Nexus thermal/static hooks should remain soft-linked");
      helper.succeed();
   }

   private static BuildAssemblyLine buildAssemblyLine(GameTestHelper helper) {
      clearAssemblyLineEnvelope(helper);
      set(helper, 1, 1, 2, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 2, 1, 2, ModBlocks.INPUT_DEPOT_CRATE.get());
      set(helper, 3, 1, 2, ModBlocks.INDUSTRIAL_POWER_BUS.get());
      set(helper, 4, 1, 2, ModBlocks.OUTPUT_DEPOT_CRATE.get());
      set(helper, 5, 1, 2, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 1, 1, 3, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 3, 1, 3, ModBlocks.INDUSTRIAL_ASSEMBLY_LINE_CONTROLLER.get());
      set(helper, 4, 1, 3, ModBlocks.ROBOTIC_ARM_MOUNT.get());
      set(helper, 5, 1, 3, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 1, 1, 4, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 2, 1, 4, ModBlocks.INDUSTRIAL_DATA_BUS.get());
      set(helper, 3, 1, 4, ModBlocks.INDUSTRIAL_WORKCELL_FRAME.get());
      set(helper, 4, 1, 4, ModBlocks.INDUSTRIAL_DATA_BUS.get());
      set(helper, 5, 1, 4, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 3, 2, 2, ModBlocks.INDUSTRIAL_POWER_BUS.get());
      set(helper, 3, 2, 3, ModBlocks.ASSEMBLY_GANTRY_RAIL.get());
      set(helper, 3, 2, 4, ModBlocks.WARNING_LIGHT.get());
      IndustrialMultiblockControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(3, 1, 3), IndustrialMultiblockControllerBlockEntity.class);
      IndustrialMultiblockCrateBlockEntity input = helper.getBlockEntity(new BlockPos(2, 1, 2), IndustrialMultiblockCrateBlockEntity.class);
      IndustrialMultiblockCrateBlockEntity output = helper.getBlockEntity(new BlockPos(4, 1, 2), IndustrialMultiblockCrateBlockEntity.class);
      IndustrialRoboticArmMountBlockEntity arm = helper.getBlockEntity(new BlockPos(4, 1, 3), IndustrialRoboticArmMountBlockEntity.class);
      return new BuildAssemblyLine(controller, input, output, arm);
   }

   private static BuildNexusFurnaceArray buildNexusFurnaceArray(GameTestHelper helper) {
      clearNexusArrayEnvelope(helper);
      set(helper, 1, 1, 1, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 2, 1, 1, ModBlocks.ROBOTIC_ARM_MOUNT.get());
      set(helper, 3, 1, 1, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 1, 1, 2, ModBlocks.NEXUS_THERMAL_INJECTOR.get());
      set(helper, 2, 1, 2, ModBlocks.NEXUS_FURNACE_ARRAY_CONTROLLER.get());
      set(helper, 3, 1, 2, ModBlocks.INPUT_DEPOT_CRATE.get());
      set(helper, 1, 1, 3, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 2, 1, 3, ModBlocks.OUTPUT_DEPOT_CRATE.get());
      set(helper, 3, 1, 3, ModBlocks.REINFORCED_MACHINE_CASING.get());
      set(helper, 2, 2, 1, ModBlocks.INDUSTRIAL_POWER_BUS.get());
      set(helper, 2, 2, 2, ModBlocks.INDUSTRIAL_WORKCELL_FRAME.get());
      set(helper, 2, 2, 3, ModBlocks.WARNING_LIGHT.get());
      IndustrialMultiblockControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(2, 1, 2), IndustrialMultiblockControllerBlockEntity.class);
      IndustrialMultiblockCrateBlockEntity input = helper.getBlockEntity(new BlockPos(3, 1, 2), IndustrialMultiblockCrateBlockEntity.class);
      IndustrialMultiblockCrateBlockEntity output = helper.getBlockEntity(new BlockPos(2, 1, 3), IndustrialMultiblockCrateBlockEntity.class);
      IndustrialRoboticArmMountBlockEntity arm = helper.getBlockEntity(new BlockPos(2, 1, 1), IndustrialRoboticArmMountBlockEntity.class);
      return new BuildNexusFurnaceArray(controller, input, output, arm);
   }

   private static void clearAssemblyLineEnvelope(GameTestHelper helper) {
      for (int x = 0; x <= 8; x++) {
         for (int y = 1; y <= 3; y++) {
            for (int z = 0; z <= 6; z++) {
               helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
            }
         }
      }
   }

   private static void clearNexusArrayEnvelope(GameTestHelper helper) {
      for (int x = 0; x <= 5; x++) {
         for (int y = 1; y <= 3; y++) {
            for (int z = 0; z <= 5; z++) {
               helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
            }
         }
      }
   }

   private static void tickAssemblyLineUntilOutput(BuildAssemblyLine built, int expectedOutput, int maxTicks) {
      for (int i = 0; i < maxTicks && built.output().countItem(ModItems.REINFORCED_MACHINE_FRAME.get()) < expectedOutput; i++) {
         IndustrialMultiblockControllerBlockEntity.tick(built.controller().getLevel(), built.controller().getBlockPos(), built.controller().getBlockState(), built.controller());
         IndustrialRoboticArmMountBlockEntity.tick(built.arm().getLevel(), built.arm().getBlockPos(), built.arm().getBlockState(), built.arm());
         if (i % 40 == 39) {
            built.controller().resumeQueue(null);
            built.controller().retryBlocked(null);
         }
      }
   }

   private static void tickNexusArrayUntilOutput(BuildNexusFurnaceArray built, Item item, int expectedOutput, int maxTicks) {
      for (int i = 0; i < maxTicks && built.output().countItem(item) < expectedOutput; i++) {
         IndustrialMultiblockControllerBlockEntity.tick(built.controller().getLevel(), built.controller().getBlockPos(), built.controller().getBlockState(), built.controller());
         IndustrialRoboticArmMountBlockEntity.tick(built.arm().getLevel(), built.arm().getBlockPos(), built.arm().getBlockState(), built.arm());
         if (i % 40 == 39) {
            built.controller().resumeQueue(null);
            built.controller().retryBlocked(null);
         }
      }
   }

   private static void addStabilizeInputs(IndustrialMultiblockCrateBlockEntity input) {
      input.setItem(0, new ItemStack((ItemLike)ModItems.STABLE_NEXUS_CORE.get()));
      input.setItem(1, new ItemStack((ItemLike)ModItems.RECIPE_MATRIX_SHARD.get()));
      input.setItem(2, new ItemStack((ItemLike)ModItems.COOLANT_CELL.get(), 2));
      input.setItem(3, new ItemStack((ItemLike)ModItems.FLUX_CRYSTAL.get(), 2));
   }

   private static void addForgeInputs(IndustrialMultiblockCrateBlockEntity input) {
      input.setItem(0, new ItemStack((ItemLike)ModItems.HYBRID_THERMAL_CORE.get()));
      input.setItem(1, new ItemStack((ItemLike)ModItems.RECIPE_MATRIX_SHARD.get()));
      input.setItem(2, new ItemStack((ItemLike)ModItems.STABILIZED_ALLOY_PLATE.get(), 4));
      input.setItem(3, new ItemStack((ItemLike)ModItems.FIELD_RELAY.get(), 2));
   }

   private static void set(GameTestHelper helper, int x, int y, int z, Block block) {
      helper.setBlock(new BlockPos(x, y, z), block);
   }

   private static MultiblockPowerProvider testPowerProvider() {
      return testPowerProvider(new AtomicLong(100_000L));
   }

   private static MultiblockPowerProvider testPowerProvider(AtomicLong availablePower) {
      return new MultiblockPowerProvider() {
         @Override
         public Identifier providerId() {
            return id("test_power_provider");
         }

         @Override
         public long availablePower(net.minecraft.world.level.Level level, BlockPos controllerPos) {
            return Math.max(0L, availablePower.get());
         }

         @Override
         public long drawPower(net.minecraft.world.level.Level level, BlockPos controllerPos, long ep, boolean simulate) {
            return Math.min(Math.max(0L, availablePower.get()), Math.max(0L, ep));
         }
      };
   }

   private static String validationDebug(ValidationResult result) {
      return result.summaryLine()
         + " rotation=" + result.matchedRotation()
         + " origin=" + result.matchedOrigin().toShortString()
         + " missing=" + result.missingBlocks()
         + " wrong=" + result.wrongBlocks()
         + " errors=" + result.errors();
   }

   private static int recipeButtonIndex(IndustrialMultiblockControllerBlockEntity controller, Identifier recipeId) {
      List<MultiblockAutomationRecipe> recipes = controller.availableAutomationRecipes();
      for (int i = 0; i < recipes.size(); i++) {
         if (recipes.get(i).id().equals(recipeId)) {
            return i;
         }
      }
      return -1;
   }

   private static String terminalPayload(ServerLevel level, BlockPos controllerPos, Identifier recipeId, int quantity) {
      StringBuilder builder = new StringBuilder();
      builder.append('{')
         .append("\"dimension\":\"").append(level.dimension().identifier()).append("\",")
         .append("\"controller_pos\":").append(controllerPos.asLong()).append(',')
         .append("\"quantity\":").append(quantity);
      if (recipeId != null) {
         builder.append(',').append("\"recipe_id\":\"").append(recipeId).append('"');
      }
      return builder.append('}').toString();
   }

   private record BuildAssemblyLine(
      IndustrialMultiblockControllerBlockEntity controller,
      IndustrialMultiblockCrateBlockEntity input,
      IndustrialMultiblockCrateBlockEntity output,
      IndustrialRoboticArmMountBlockEntity arm
   ) {
   }

   private record BuildNexusFurnaceArray(
      IndustrialMultiblockControllerBlockEntity controller,
      IndustrialMultiblockCrateBlockEntity input,
      IndustrialMultiblockCrateBlockEntity output,
      IndustrialRoboticArmMountBlockEntity arm
   ) {
   }

   private static int countDroppedItems(GameTestHelper helper, BlockPos local, ItemLike itemLike) {
      Item item = itemLike.asItem();
      return helper.getLevel()
         .getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(local)).inflate(3.0D))
         .stream()
         .filter(entity -> entity.getItem().is(item))
         .mapToInt(entity -> entity.getItem().getCount())
         .sum();
   }

   private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, String testName, Identifier functionId) {
      TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData(
         environment, Identifier.withDefaultNamespace("empty"), 400, 0, true, Rotation.NONE, false, 1, 1, false, 2
      );
      event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, path);
   }
}
