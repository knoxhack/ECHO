package com.knoxhack.echoagriculturereclamation.test;

import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.knoxhack.echocore.api.EchoRouteRecord;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.block.ReclamationMachineBlock;
import com.knoxhack.echoagriculturereclamation.block.entity.HydroponicTrayBlockEntity;
import com.knoxhack.echoagriculturereclamation.content.CropSpec;
import com.knoxhack.echoagriculturereclamation.content.ReclamationContent;
import com.knoxhack.echoagriculturereclamation.content.ReclamationMetrics;
import com.knoxhack.echoagriculturereclamation.content.SeedProfile;
import com.knoxhack.echoagriculturereclamation.content.SoilState;
import com.knoxhack.echoagriculturereclamation.integration.ReclamationCoreIntegration;
import com.knoxhack.echoagriculturereclamation.integration.ReclamationCrossAddonIntegration;
import com.knoxhack.echoagriculturereclamation.integration.ReclamationCrossAddonIntegration.FactionPreference;
import com.knoxhack.echoagriculturereclamation.integration.ReclamationMissionProvider;
import com.knoxhack.echoagriculturereclamation.progress.ReclamationProgress;
import com.knoxhack.echoagriculturereclamation.progress.ReclamationRestoration;
import com.knoxhack.echoagriculturereclamation.progress.ReclamationWorldData;
import com.knoxhack.echoagriculturereclamation.registry.ModBlocks;
import com.knoxhack.echoagriculturereclamation.registry.ModItems;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
   private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(Registries.TEST_FUNCTION, EchoAgricultureReclamation.MODID);

   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEED_CAPSULE =
      TEST_FUNCTIONS.register("seed_capsule_analysis", () -> ModGameTests::seedCapsuleAnalysis);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOIL_CONVERSION =
      TEST_FUNCTIONS.register("soil_state_conversion", () -> ModGameTests::soilStateConversion);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HYDROPONIC_TRAY =
      TEST_FUNCTIONS.register("hydroponic_tray_persistence", () -> ModGameTests::hydroponicTrayPersistence);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GREENHOUSE =
      TEST_FUNCTIONS.register("greenhouse_safety_scoring", () -> ModGameTests::greenhouseSafetyScoring);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEED_STABILIZATION =
      TEST_FUNCTIONS.register("seed_stabilization_profile", () -> ModGameTests::seedStabilizationProfile);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL =
      TEST_FUNCTIONS.register("terminal_status_metrics", () -> ModGameTests::terminalStatusMetrics);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RESTORATION =
      TEST_FUNCTIONS.register("restoration_score_conversion", () -> ModGameTests::restorationScoreConversion);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_WIRING =
      TEST_FUNCTIONS.register("core_route_records", () -> ModGameTests::coreRouteRecords);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CROSS_ADDON_SOIL =
      TEST_FUNCTIONS.register("cross_addon_soil_compatibility", () -> ModGameTests::crossAddonSoilCompatibility);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_MILESTONES =
      TEST_FUNCTIONS.register("core_milestone_recording", () -> ModGameTests::coreMilestoneRecording);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FACTION_SEED_BIAS =
      TEST_FUNCTIONS.register("faction_biased_seed_recovery", () -> ModGameTests::factionBiasedSeedRecovery);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MAIN_LOOP =
      TEST_FUNCTIONS.register("main_progression_loop_regression", () -> ModGameTests::mainProgressionLoopRegression);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BIO_REACTOR_UTILITY =
      TEST_FUNCTIONS.register("bio_reactor_utility_outputs", () -> ModGameTests::bioReactorUtilityOutputs);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COMPOST_RECYCLER_UTILITY =
      TEST_FUNCTIONS.register("compost_recycler_utility_outputs", () -> ModGameTests::compostRecyclerUtilityOutputs);

   private ModGameTests() {
   }

   public static void register(IEventBus eventBus) {
      TEST_FUNCTIONS.register(eventBus);
   }

   public static void registerTests(RegisterGameTestsEvent event) {
      Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("agriculture_reclamation"));
      register(event, environment, "seed_capsule_analysis", SEED_CAPSULE.getId());
      register(event, environment, "soil_state_conversion", SOIL_CONVERSION.getId());
      register(event, environment, "hydroponic_tray_persistence", HYDROPONIC_TRAY.getId());
      register(event, environment, "greenhouse_safety_scoring", GREENHOUSE.getId());
      register(event, environment, "seed_stabilization_profile", SEED_STABILIZATION.getId());
      register(event, environment, "terminal_status_metrics", TERMINAL.getId());
      register(event, environment, "restoration_score_conversion", RESTORATION.getId());
      register(event, environment, "core_route_records", CORE_WIRING.getId());
      register(event, environment, "cross_addon_soil_compatibility", CROSS_ADDON_SOIL.getId());
      register(event, environment, "core_milestone_recording", CORE_MILESTONES.getId());
      register(event, environment, "faction_biased_seed_recovery", FACTION_SEED_BIAS.getId());
      register(event, environment, "main_progression_loop_regression", MAIN_LOOP.getId());
      register(event, environment, "bio_reactor_utility_outputs", BIO_REACTOR_UTILITY.getId());
      register(event, environment, "compost_recycler_utility_outputs", COMPOST_RECYCLER_UTILITY.getId());
   }

   private static void seedCapsuleAnalysis(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ItemStack capsule = new ItemStack(ModItems.RECOVERED_SEED_CAPSULE.get());
      player.setItemInHand(InteractionHand.MAIN_HAND, capsule);
      InteractionResult result = ModItems.RECOVERED_SEED_CAPSULE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
      helper.assertTrue(result == InteractionResult.SUCCESS_SERVER, "Recovered seed capsule should analyze on use");
      helper.assertTrue(!ReclamationProgress.knownSeeds(player).isEmpty(), "Capsule use should record a known seed");
      helper.assertTrue(ReclamationProgress.flag(player, "seed_recovered"), "Capsule use should mark seed recovery");
      helper.succeed();
   }

   private static void soilStateConversion(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.CONTAMINATED_SOIL.get());
      int changed = ReclamationRestoration.purifyArea(helper.getLevel(), helper.absolutePos(local), 1, 1);
      helper.assertTrue(changed == 1, "Soil purifier should convert at least one contaminated soil block");
      helper.assertBlockPresent(ModBlocks.PURIFIED_SOIL.get(), local);
      helper.succeed();
   }

   private static void hydroponicTrayPersistence(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      helper.setBlock(local, (Block)ModBlocks.HYDROPONIC_TRAY.get());
      HydroponicTrayBlockEntity tray = helper.getBlockEntity(local, HydroponicTrayBlockEntity.class);
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ItemStack seed = new ItemStack(ModItems.STABILIZED_SEED.get());
      seed.set(ModItems.seedProfileComponent(), new SeedProfile("clean_corn", 0, 100));
      helper.assertTrue(tray.insertSeed(player, seed), "Hydroponic tray should accept a profiled seed");
      for (int i = 0; i < 1800; i++) {
         HydroponicTrayBlockEntity.tick(helper.getLevel(), tray.getBlockPos(), tray.getBlockState(), tray);
      }
      helper.assertTrue(tray.profile() != null, "Hydroponic tray should persist seed profile");
      helper.assertTrue(tray.age() > 0, "Hydroponic tray should tick crop age");
      helper.succeed();
   }

   private static void greenhouseSafetyScoring(GameTestHelper helper) {
      BlockPos openCenter = new BlockPos(4, 2, 4);
      helper.setBlock(openCenter, (Block)ModBlocks.GREENHOUSE_CONTROLLER.get());
      for (int x = 1; x <= 6; x++) {
         helper.setBlock(new BlockPos(x, 3, 1), (Block)ModBlocks.GREENHOUSE_GLASS.get());
         helper.setBlock(new BlockPos(x, 3, 6), (Block)ModBlocks.GREENHOUSE_GLASS.get());
      }
      helper.setBlock(new BlockPos(2, 2, 2), (Block)ModBlocks.SPORE_FILTER.get());
      helper.setBlock(new BlockPos(3, 2, 2), (Block)ModBlocks.POLLINATOR_DRONE_DOCK.get());
      ReclamationProgress.GreenhouseScan open = ReclamationProgress.scanGreenhouse(helper.getLevel(), helper.absolutePos(openCenter));
      helper.assertTrue(open.score() >= 30, "Open greenhouse support should still provide partial safety");
      helper.assertTrue(open.score() < ReclamationContent.progression().greenhouseSafeThreshold(), "Open support should not reach safe greenhouse threshold");
      helper.assertFalse(open.enclosed(), "Open greenhouse support should report a leaking enclosure");

      BlockPos sealedController = new BlockPos(16, 2, 14);
      buildSealedGreenhouse(helper, sealedController);
      ReclamationProgress.GreenhouseScan sealed = ReclamationProgress.scanGreenhouse(helper.getLevel(), helper.absolutePos(sealedController));
      helper.assertTrue(sealed.enclosed(), "Complete Greenhouse Glass shell should report a sealed enclosure");
      helper.assertTrue(sealed.greenhouseRoof(), "Sealed greenhouse should detect Greenhouse Glass overhead");
      helper.assertTrue(sealed.activeDocks() >= 1, "Pollinator Dock should become active when crops or trays are nearby");
      helper.assertTrue(sealed.score() >= ReclamationContent.progression().greenhouseSafeThreshold(), "Sealed greenhouse should reach safe threshold");
      helper.succeed();
   }

   private static void seedStabilizationProfile(GameTestHelper helper) {
      SeedProfile contaminated = new SeedProfile("ash_wheat", 3, 22);
      SeedProfile stable = contaminated.stabilized();
      helper.assertTrue(stable.contaminationTier() == 0, "Stabilized seed should clear contamination tier");
      helper.assertTrue(stable.stability() == 100, "Stabilized seed should reach 100 stability");
      helper.succeed();
   }

   private static void terminalStatusMetrics(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      ReclamationProgress.discoverSeed(player, CropSpec.byPath("ash_wheat"));
      ReclamationProgress.max(player, "crop_stability", 100);
      ReclamationProgress.max(player, "food_security", 30);
      TerminalMissionSnapshot snapshot = ReclamationMissionProvider.INSTANCE.snapshot(player, ReclamationMissionProvider.routeMissions().get(0).id());
      helper.assertTrue(snapshot.status() == TerminalMissionStatus.CLAIMABLE || snapshot.status() == TerminalMissionStatus.UNLOCKED,
         "Terminal mission snapshot should render seed recovery state");
      ReclamationMetrics metrics = ReclamationProgress.metrics(player);
      helper.assertTrue(metrics.knownSeeds() >= 1, "Terminal metrics should include known seeds");
      helper.succeed();
   }

   private static void restorationScoreConversion(GameTestHelper helper) {
      ServerLevel level = helper.getLevel();
      BlockPos local = new BlockPos(1, 1, 1);
      BlockPos absolute = helper.absolutePos(local);
      helper.setBlock(local, (Block)ModBlocks.PURIFIED_SOIL.get());
      ChunkPos chunk = new ChunkPos(absolute.getX() >> 4, absolute.getZ() >> 4);
      ReclamationWorldData.get(level).setRestorationScore(chunk, 100);
      int changed = ReclamationRestoration.stabilizeArea(level, absolute, 1, 1);
      helper.assertTrue(changed >= 1, "Restoration score should be able to stabilize local soil blocks");
      helper.succeed();
   }

   private static void coreRouteRecords(GameTestHelper helper) {
      ReclamationCoreIntegration.registerAddonChapter();
      helper.assertTrue(EchoAddonRegistry.isRegistered(ReclamationCoreIntegration.CHAPTER_ID), "Core chapter should be registered");
      List<EchoRouteRecord> records = EchoCoreServices.routeRecords(helper.makeMockPlayer(GameType.CREATIVE)).stream()
         .filter(record -> ReclamationCoreIntegration.CHAPTER_ID.equals(record.chapterId()))
         .toList();
      helper.assertTrue(!records.isEmpty(), "Core route records should include Agriculture Reclamation");
      helper.succeed();
   }

   private static void crossAddonSoilCompatibility(GameTestHelper helper) {
      helper.assertTrue(
         ReclamationCrossAddonIntegration.externalSoilState(Identifier.fromNamespaceAndPath("echoashfallprotocol", "contaminated_soil")) == SoilState.CONTAMINATED,
         "Ashfall contaminated soil id should map to Agriculture contaminated soil state"
      );
      helper.assertTrue(
         ReclamationCrossAddonIntegration.externalSoilState(Identifier.fromNamespaceAndPath("echoashfallprotocol", "toxic_puddle")) == SoilState.TOXIC_MUD,
         "Ashfall toxic puddle id should map to Agriculture toxic mud state"
      );
      helper.assertTrue(
         ReclamationCrossAddonIntegration.externalSoilState(Identifier.fromNamespaceAndPath("echoashfallprotocol", "irradiated_crust")) == SoilState.IRRADIATED,
         "Ashfall irradiated crust id should map to Agriculture irradiated soil state"
      );
      helper.assertTrue(
         ReclamationCrossAddonIntegration.externalSoilState(Identifier.fromNamespaceAndPath("restoration_project", "restored_soil")) == SoilState.RESTORED,
         "Restoration Project restored soil id should map to Agriculture restored soil state"
      );
      helper.succeed();
   }

   private static void coreMilestoneRecording(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ReclamationProgress.mark(player, "first_growth");
      ReclamationProgress.mark(player, "greenhouse_online");
      ReclamationProgress.mark(player, "restore_chunk");
      helper.assertTrue(
         ReclamationProgress.coreMilestoneForFlag("first_growth").equals("first_growth"),
         "First growth should map to an ECHO Core milestone id"
      );
      helper.assertTrue(
         ReclamationProgress.coreMilestoneForFlag("greenhouse_online").equals("greenhouse_online"),
         "Greenhouse online should map to an ECHO Core milestone id"
      );
      helper.assertTrue(
         ReclamationProgress.coreMilestoneForFlag("restore_chunk").equals("restore_chunk"),
         "Chunk restoration should map to an ECHO Core milestone id"
      );
      helper.succeed();
   }

   private static void factionBiasedSeedRecovery(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      Identifier remnant = Identifier.fromNamespaceAndPath("echoashfallprotocol", "remnant_collective");
      ensureFaction(remnant, "Remnant Collective");
      EchoCoreServices.setFactionReputation(player, remnant, 55);

      CropSpec spec = CropSpec.byPath("clean_corn");
      SeedProfile baseline = ReclamationContent.progression().recoveredProfile(spec, RandomSource.create(311L));
      SeedProfile biased = ReclamationCrossAddonIntegration.recoveredProfile(player, spec, RandomSource.create(311L));
      helper.assertTrue(
         ReclamationCrossAddonIntegration.factionPreference(player) == FactionPreference.REMNANT,
         "Trusted Remnant standing should be detected as Agriculture seed preference"
      );
      helper.assertTrue(biased.stability() > baseline.stability(), "Remnant seed recovery should improve stability");
      helper.assertTrue(biased.contaminationTier() < baseline.contaminationTier(), "Remnant seed recovery should reduce contamination");
      helper.succeed();
   }

   private static void mainProgressionLoopRegression(GameTestHelper helper) {
      ServerLevel level = helper.getLevel();
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      BlockPos soilLocal = new BlockPos(1, 1, 1);
      BlockPos trayLocal = new BlockPos(2, 1, 1);
      helper.setBlock(soilLocal, (Block)ModBlocks.CONTAMINATED_SOIL.get());
      int purified = ReclamationRestoration.purifyArea(level, helper.absolutePos(soilLocal), 1, 1);
      helper.assertTrue(purified == 1, "Main loop should purify contaminated soil before cultivation");

      helper.setBlock(trayLocal, (Block)ModBlocks.HYDROPONIC_TRAY.get());
      HydroponicTrayBlockEntity tray = helper.getBlockEntity(trayLocal, HydroponicTrayBlockEntity.class);
      ItemStack seed = new ItemStack(ModItems.CONTAMINATED_SEED.get());
      seed.set(ModItems.seedProfileComponent(), new SeedProfile("clean_corn", 1, 45));
      helper.assertTrue(tray.insertSeed(player, seed), "Main loop should insert a profiled seed into hydroponics");
      ItemStack nutrient = new ItemStack(ModItems.SOIL_NUTRIENT_MIX.get(), 4);
      for (int i = 0; i < 4; i++) {
         tray.addNutrient(player, nutrient);
      }
      for (int ticks = 0; ticks < 12000 && tray.age() < 7; ticks++) {
         HydroponicTrayBlockEntity.tick(level, tray.getBlockPos(), tray.getBlockState(), tray);
      }
      helper.assertTrue(tray.age() >= 7, "Main loop hydroponic crop should reach harvest age");
      helper.assertTrue(tray.harvest(player), "Main loop should harvest hydroponic crop output");
      helper.assertTrue(ReclamationProgress.flag(player, "first_growth"), "Harvest should mark first growth");
      helper.assertTrue(ReclamationProgress.value(player, "crops_grown") >= 1, "Harvest should increment grown crop count");
      helper.assertTrue(ReclamationProgress.count(player, ModItems.CLEAN_CORN.get()) > 0, "Harvest should grant the correct produce");
      helper.assertTrue(ReclamationProgress.flag(player, "stabilization_seed_recovered"), "Unstable harvest should return a seed for stabilization");
      ReclamationProgress.recordStabilization(player);
      helper.assertTrue(ReclamationProgress.flag(player, "gene_stabilization"), "Stabilization should update progression");
      helper.assertTrue(ReclamationProgress.value(player, "restoration_score") > 0, "Harvest should add local restoration pressure");
      helper.succeed();
   }

   private static void bioReactorUtilityOutputs(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ReclamationMachineBlock block = (ReclamationMachineBlock)ModBlocks.BIO_REACTOR.get();

      helper.assertTrue(runMachine(block, "runBioReactor", new ItemStack(ModItems.MEDICINAL_ALOE.get()), player) == InteractionResult.SUCCESS_SERVER,
         "Bio-Reactor should process Medicinal Aloe without requiring Ashfall bridge items");
      helper.assertTrue(ReclamationProgress.count(player, ModItems.BIO_GEL.get()) >= 1, "Medicinal Aloe should produce Bio-Gel");

      int bioGelBefore = ReclamationProgress.count(player, ModItems.BIO_GEL.get());
      runMachine(block, "runBioReactor", new ItemStack(ModItems.SIGNAL_FUNGUS.get()), player);
      helper.assertTrue(ReclamationProgress.count(player, ModItems.BIO_GEL.get()) >= bioGelBefore + 2,
         "Signal Fungus should yield increased Bio-Gel");

      runMachine(block, "runBioReactor", new ItemStack(ModItems.CRYO_MOSS.get()), player);
      helper.assertTrue(ReclamationProgress.count(player, ModItems.PURIFICATION_ENZYME.get()) >= 1,
         "Cryo Moss should yield Purification Enzyme");

      runMachine(block, "runBioReactor", new ItemStack(ModItems.NEXUS_ORCHID.get()), player);
      helper.assertTrue(ReclamationProgress.count(player, ModItems.GENE_SAMPLE.get()) >= 1,
         "Nexus Orchid should yield Gene Sample");
      BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("echonexusprotocol", "nexus_gel")).ifPresent(item ->
         helper.assertTrue(ReclamationProgress.count(player, item) >= 1, "Loaded Nexus bridge should grant nexus_gel")
      );
      helper.succeed();
   }

   private static void compostRecyclerUtilityOutputs(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ReclamationMachineBlock block = (ReclamationMachineBlock)ModBlocks.COMPOST_RECYCLER.get();

      helper.assertTrue(runMachine(block, "runCompostRecycler", new ItemStack(ModItems.FILTER_REED.get()), player) == InteractionResult.SUCCESS_SERVER,
         "Compost Recycler should process Filter Reed without requiring Ashfall bridge items");
      helper.assertTrue(ReclamationProgress.count(player, ModItems.SOIL_NUTRIENT_MIX.get()) >= 3,
         "Filter Reed should yield increased Soil Nutrient Mix");

      int nutrientBefore = ReclamationProgress.count(player, ModItems.SOIL_NUTRIENT_MIX.get());
      runMachine(block, "runCompostRecycler", new ItemStack(ModItems.CRYO_MOSS.get()), player);
      helper.assertTrue(ReclamationProgress.count(player, ModItems.SOIL_NUTRIENT_MIX.get()) >= nutrientBefore + 2,
         "Cryo Moss compost should yield increased Soil Nutrient Mix");

      nutrientBefore = ReclamationProgress.count(player, ModItems.SOIL_NUTRIENT_MIX.get());
      runMachine(block, "runCompostRecycler", new ItemStack(ModItems.SIGNAL_FUNGUS.get()), player);
      helper.assertTrue(ReclamationProgress.count(player, ModItems.SOIL_NUTRIENT_MIX.get()) >= nutrientBefore + 2,
         "Signal Fungus compost should yield increased Soil Nutrient Mix");

      BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("echoashfallprotocol", "plant_fiber")).ifPresent(item ->
         helper.assertTrue(ReclamationProgress.count(player, item) >= 1, "Loaded Ashfall bridge should grant plant_fiber")
      );
      helper.succeed();
   }

   private static void buildSealedGreenhouse(GameTestHelper helper, BlockPos controller) {
      for (int x = controller.getX() - 2; x <= controller.getX() + 2; x++) {
         for (int z = controller.getZ() - 2; z <= controller.getZ() + 2; z++) {
            helper.setBlock(new BlockPos(x, controller.getY() - 1, z), (Block)ModBlocks.STABILIZED_SOIL.get());
            helper.setBlock(new BlockPos(x, controller.getY() + 2, z), (Block)ModBlocks.GREENHOUSE_GLASS.get());
         }
      }
      for (int y = controller.getY(); y <= controller.getY() + 1; y++) {
         for (int x = controller.getX() - 2; x <= controller.getX() + 2; x++) {
            helper.setBlock(new BlockPos(x, y, controller.getZ() - 2), (Block)ModBlocks.GREENHOUSE_GLASS.get());
            helper.setBlock(new BlockPos(x, y, controller.getZ() + 2), (Block)ModBlocks.GREENHOUSE_GLASS.get());
         }
         for (int z = controller.getZ() - 1; z <= controller.getZ() + 1; z++) {
            helper.setBlock(new BlockPos(controller.getX() - 2, y, z), (Block)ModBlocks.GREENHOUSE_GLASS.get());
            helper.setBlock(new BlockPos(controller.getX() + 2, y, z), (Block)ModBlocks.GREENHOUSE_GLASS.get());
         }
      }
      helper.setBlock(controller, (Block)ModBlocks.GREENHOUSE_CONTROLLER.get());
      helper.setBlock(controller.west(), (Block)ModBlocks.POLLINATOR_DRONE_DOCK.get());
      helper.setBlock(controller.east(), (Block)ModBlocks.SPORE_FILTER.get());
      helper.setBlock(controller.south(), (Block)ModBlocks.HYDROPONIC_TRAY.get());
   }

   private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, String testName, Identifier functionId) {
      TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData(
         environment, Identifier.withDefaultNamespace("empty"), 400, 0, true, Rotation.NONE, false, 1, 1, false, 2
      );
      event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoAgricultureReclamation.MODID, path);
   }

   private static InteractionResult runMachine(ReclamationMachineBlock block, String methodName, ItemStack stack, Player player) {
      try {
         Method method = ReclamationMachineBlock.class.getDeclaredMethod(methodName, ItemStack.class, Player.class);
         method.setAccessible(true);
         return (InteractionResult)method.invoke(block, stack, player);
      } catch (ReflectiveOperationException exception) {
         throw new AssertionError("Unable to invoke Agriculture machine test hook " + methodName, exception);
      }
   }

   private static void ensureFaction(Identifier factionId, String displayName) {
      if (EchoCoreServices.factionDefinition(factionId).isPresent()) {
         return;
      }
      EchoCoreServices.registerFaction(new EchoFactionDefinition(
         factionId,
         displayName,
         displayName,
         "Test Route",
         "Agriculture Reclamation test faction.",
         "",
         "",
         "",
         0x66E8FF,
         false,
         List.of(),
         List.of(),
         List.of(),
         List.of(),
         null
      ));
   }
}
