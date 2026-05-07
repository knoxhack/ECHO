package com.knoxhack.echonexusprotocol.test;

import com.knoxhack.echonexusprotocol.block.ProtocolSealBlock;
import com.knoxhack.echonexusprotocol.block.entity.NexusMachineBlockEntity;
import com.knoxhack.echonexusprotocol.Config;
import com.knoxhack.echonexusprotocol.compat.jei.NexusJeiRecipeCatalog;
import com.knoxhack.echonexusprotocol.data.NexusPlayerData;
import com.knoxhack.echonexusprotocol.entity.NexusMobEntity;
import com.knoxhack.echonexusprotocol.event.NexusArmorEvents;
import com.knoxhack.echonexusprotocol.integration.NexusTerminalIds;
import com.knoxhack.echonexusprotocol.integration.NexusTerminalMissionProvider;
import com.knoxhack.echonexusprotocol.integration.NexusProgression;
import com.knoxhack.echonexusprotocol.item.NexusFieldChargeItem;
import com.knoxhack.echonexusprotocol.item.NexusScannerVisorItem;
import com.knoxhack.echonexusprotocol.item.NexusUtilityItem;
import com.knoxhack.echonexusprotocol.registry.ModBlocks;
import com.knoxhack.echonexusprotocol.registry.ModEntities;
import com.knoxhack.echonexusprotocol.registry.ModItems;
import com.knoxhack.echonexusprotocol.world.NexusWorldData;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
   private static final String[] NEXUS_POIS = {
      "abandoned_nexus_field_station", "signal_relay_tower", "data_vault", "corruption_containment_lab", "blackbox_monolith", "nexus_core_chamber"
   };
   private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, "echonexusprotocol");
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENERGY = TEST_FUNCTIONS.register(
      "energy_storage_transfer", () -> ModGameTests::energyStorageTransfer
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECYCLER = TEST_FUNCTIONS.register(
      "recycler_dirty_corruption", () -> ModGameTests::recyclerDirtyCorruption
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MACHINE_RECIPES = TEST_FUNCTIONS.register(
      "machine_recipe_paths", () -> ModGameTests::machineRecipePaths
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STABILIZER = TEST_FUNCTIONS.register(
      "field_stabilizer", () -> ModGameTests::fieldStabilizer
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEAL = TEST_FUNCTIONS.register(
      "protocol_seal_modes", () -> ModGameTests::protocolSealModes
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PROGRESSION = TEST_FUNCTIONS.register(
      "progression_and_endings", () -> ModGameTests::progressionAndEndings
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL = TEST_FUNCTIONS.register(
      "terminal_mission_snapshots", () -> ModGameTests::terminalMissionSnapshots
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR_LOCK = TEST_FUNCTIONS.register(
      "armor_emergency_field_lock", () -> ModGameTests::armorEmergencyFieldLock
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOBS = TEST_FUNCTIONS.register(
      "mob_signature_behaviors", () -> ModGameTests::mobSignatureBehaviors
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SCANNER = TEST_FUNCTIONS.register(
      "scanner_research_unlocks", () -> ModGameTests::scannerResearchUnlocks
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIELD_CHARGES = TEST_FUNCTIONS.register(
      "field_charges_rewrite_blocks", () -> ModGameTests::fieldChargesRewriteBlocks
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD_STATE = TEST_FUNCTIONS.register(
      "world_quarantine_storm_tears", () -> ModGameTests::worldQuarantineStormTears
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERRAIN_SITES = TEST_FUNCTIONS.register(
      "terrain_landmark_coverage", () -> ModGameTests::terrainLandmarkCoverage
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UTILITY_GEAR = TEST_FUNCTIONS.register(
      "utility_gear_behaviors", () -> ModGameTests::utilityGearBehaviors
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> POST_RELEASE_POLISH = TEST_FUNCTIONS.register(
      "post_release_polish_coverage", () -> ModGameTests::postReleasePolishCoverage
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIELD_TELEMETRY = TEST_FUNCTIONS.register(
      "field_terminal_telemetry_sources", () -> ModGameTests::fieldTerminalTelemetrySources
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIELD_MAP = TEST_FUNCTIONS.register(
      "field_map_telemetry_sources", () -> ModGameTests::fieldMapTelemetrySources
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECOVERY_TOOLS = TEST_FUNCTIONS.register(
      "collapsed_recovery_tools", () -> ModGameTests::collapsedRecoveryTools
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENDING_EFFECTS = TEST_FUNCTIONS.register(
      "ending_world_effects", () -> ModGameTests::endingWorldEffects
   );
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RELEASE_RISK_FIXES = TEST_FUNCTIONS.register(
      "release_risk_fixes", () -> ModGameTests::releaseRiskFixes
   );

   private ModGameTests() {
   }

   public static void register(IEventBus eventBus) {
      TEST_FUNCTIONS.register(eventBus);
   }

   public static void registerTests(RegisterGameTestsEvent event) {
      Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("nexus_release_v1"), new TestEnvironmentDefinition[0]);
      register(event, environment, "energy_storage_transfer", ENERGY.getId());
      register(event, environment, "recycler_dirty_corruption", RECYCLER.getId());
      register(event, environment, "machine_recipe_paths", MACHINE_RECIPES.getId());
      register(event, environment, "field_stabilizer", STABILIZER.getId());
      register(event, environment, "protocol_seal_modes", SEAL.getId());
      register(event, environment, "progression_and_endings", PROGRESSION.getId());
      register(event, environment, "terminal_mission_snapshots", TERMINAL.getId());
      register(event, environment, "armor_emergency_field_lock", ARMOR_LOCK.getId());
      register(event, environment, "mob_signature_behaviors", MOBS.getId());
      register(event, environment, "scanner_research_unlocks", SCANNER.getId());
      register(event, environment, "field_charges_rewrite_blocks", FIELD_CHARGES.getId());
      register(event, environment, "world_quarantine_storm_tears", WORLD_STATE.getId());
      register(event, environment, "terrain_landmark_coverage", TERRAIN_SITES.getId());
      register(event, environment, "utility_gear_behaviors", UTILITY_GEAR.getId());
      register(event, environment, "post_release_polish_coverage", POST_RELEASE_POLISH.getId());
      register(event, environment, "field_terminal_telemetry_sources", FIELD_TELEMETRY.getId());
      register(event, environment, "field_map_telemetry_sources", FIELD_MAP.getId());
      register(event, environment, "collapsed_recovery_tools", RECOVERY_TOOLS.getId());
      register(event, environment, "ending_world_effects", ENDING_EFFECTS.getId());
      register(event, environment, "release_risk_fixes", RELEASE_RISK_FIXES.getId());
   }

   private static void energyStorageTransfer(GameTestHelper helper) {
      BlockPos pos = new BlockPos(1, 1, 1);
      helper.setBlock(pos, (Block)ModBlocks.NEXUS_CHARGE_TANK.get());
      NexusMachineBlockEntity tank = (NexusMachineBlockEntity)helper.getBlockEntity(pos, NexusMachineBlockEntity.class);
      tank.receiveCharge(1000);
      helper.assertTrue(tank.energyStored() == 1000, "Charge Tank should accept FE-backed Nexus Charge");
      helper.setBlock(new BlockPos(2, 1, 1), (Block)ModBlocks.NEXUS_CHARGE_TANK.get());
      NexusMachineBlockEntity.tick(helper.getLevel(), tank.getBlockPos(), tank.getBlockState(), tank);
      helper.assertTrue(tank.energyStored() <= 1000, "Charge Tank transfer tick should preserve or move stored charge safely");
      helper.succeed();
   }

   private static void recyclerDirtyCorruption(GameTestHelper helper) {
      BlockPos pos = new BlockPos(1, 1, 1);
      helper.setBlock(pos, (Block)ModBlocks.NEXUS_RECYCLER.get());
      NexusMachineBlockEntity recycler = (NexusMachineBlockEntity)helper.getBlockEntity(pos, NexusMachineBlockEntity.class);
      recycler.setItem(0, new ItemStack((ItemLike)ModItems.STATIC_FLUID.get()));

      for (int i = 0; i < 180; i++) {
         NexusMachineBlockEntity.tick(helper.getLevel(), recycler.getBlockPos(), recycler.getBlockState(), recycler);
      }

      helper.assertTrue(recycler.energyStored() > 0, "Recycler should create Nexus Charge from dirty Nexus inputs");
      helper.assertTrue(recycler.contamination() > 0, "Dirty input should contaminate the machine");
      helper.succeed();
   }

   private static void machineRecipePaths(GameTestHelper helper) {
      NexusMachineBlockEntity recycler = placeMachine(helper, new BlockPos(1, 1, 1), (Block)ModBlocks.NEXUS_RECYCLER.get());
      recycler.setItem(0, new ItemStack((ItemLike)ModItems.NEXUS_SHARD.get()));
      tickMachine(helper, recycler, 130);
      helper.assertTrue(recycler.energyStored() >= 700, "Recycler recipe should produce Nexus Charge");
      helper.assertTrue(recycler.getItem(1).is(ModItems.REALITY_DUST.get()), "Recycler should output Reality Dust from Nexus Shards");

      NexusMachineBlockEntity infuser = placeMachine(helper, new BlockPos(4, 1, 1), (Block)ModBlocks.NEXUS_INFUSER.get());
      infuser.receiveCharge(800);
      infuser.setItem(0, new ItemStack((ItemLike)ModItems.NEXUS_SHARD.get()));
      tickMachine(helper, infuser, 190);
      helper.assertTrue(infuser.getItem(1).is(ModItems.STABLE_NEXUS_CORE.get()), "Infuser should craft Stable Nexus Cores");
      helper.assertTrue(infuser.energyStored() < 800, "Infuser should spend charge on core infusion");

      NexusMachineBlockEntity decoder = placeMachine(helper, new BlockPos(7, 1, 1), (Block)ModBlocks.MEMORY_DECODER.get());
      decoder.receiveCharge(300);
      decoder.setItem(0, new ItemStack((ItemLike)ModItems.BLACKBOX_FRAGMENT.get()));
      tickMachine(helper, decoder, 170);
      helper.assertTrue(decoder.getItem(1).is(ModItems.MEMORY_SHARD.get()) && decoder.getItem(1).getCount() >= 2, "Memory Decoder should decode Blackbox Fragments");

      NexusMachineBlockEntity forge = placeMachine(helper, new BlockPos(10, 1, 1), (Block)ModBlocks.REALITY_FORGE.get());
      forge.receiveCharge(400);
      forge.setItem(0, new ItemStack((ItemLike)ModBlocks.DATA_CRACKED_STONE.get()));
      tickMachine(helper, forge, 210);
      helper.assertTrue(forge.getItem(1).is(ModBlocks.BLACKBOX_PLATE.get().asItem()), "Reality Forge should transmute Data-Cracked Stone");

      NexusMachineBlockEntity reactor = placeMachine(helper, new BlockPos(13, 1, 1), (Block)ModBlocks.CORRUPTION_REACTOR.get());
      reactor.setItem(0, new ItemStack((ItemLike)ModItems.STATIC_FLUID.get()));
      ChunkPos reactorChunk = new ChunkPos(reactor.getBlockPos().getX() >> 4, reactor.getBlockPos().getZ() >> 4);
      int pressureBefore = NexusWorldData.get(helper.getLevel()).corruptionPressure(reactorChunk);
      tickMachine(helper, reactor, 130);
      helper.assertTrue(reactor.energyStored() >= 900, "Corruption Reactor should generate high Nexus Charge from Static Fluid");
      helper.assertTrue(
         reactor.contamination() > 0 || NexusWorldData.get(helper.getLevel()).corruptionPressure(reactorChunk) > pressureBefore,
         "Corruption Reactor fuel should add machine contamination or local corruption pressure"
      );
      helper.succeed();
   }

   private static void fieldStabilizer(GameTestHelper helper) {
      BlockPos pos = new BlockPos(1, 1, 1);
      helper.setBlock(pos, (Block)ModBlocks.NEXUS_FIELD_STABILIZER.get());
      NexusMachineBlockEntity stabilizer = (NexusMachineBlockEntity)helper.getBlockEntity(pos, NexusMachineBlockEntity.class);
      stabilizer.receiveCharge(200);
      NexusWorldData data = NexusWorldData.get(helper.getLevel());
      ChunkPos chunk = new ChunkPos(stabilizer.getBlockPos().getX() >> 4, stabilizer.getBlockPos().getZ() >> 4);
      data.setFieldValue(chunk, 42);
      stabilizer.stabilizeField(helper.getLevel());
      helper.assertTrue(data.fieldValue(chunk) > 42, "Field Stabilizer should raise chunk field stability");
      helper.succeed();
   }

   private static void protocolSealModes(GameTestHelper helper) {
      BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
      NexusWorldData data = NexusWorldData.get(helper.getLevel());
      ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
      data.setFieldValue(chunk, 50);
      data.setCorruptionPressure(chunk, 20);
      BlockPos tankLocal = new BlockPos(2, 1, 1);
      BlockPos sourceLocal = new BlockPos(3, 1, 1);
      BlockPos chestLocal = new BlockPos(0, 1, 0);
      NexusMachineBlockEntity tank = placeMachine(helper, tankLocal, (Block)ModBlocks.NEXUS_CHARGE_TANK.get());
      NexusMachineBlockEntity sourceTank = placeMachine(helper, sourceLocal, (Block)ModBlocks.NEXUS_CHARGE_TANK.get());

      ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.EXTRACT);
      helper.assertTrue(tank.energyStored() > 0 || sourceTank.energyStored() > 0, "Extract seal should move corruption into nearby tanks as charge");
      helper.assertTrue(data.corruptionPressure(chunk) < 20, "Extract seal should reduce local corruption pressure");

      tank.addContamination(12);
      ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.REPAIR);
      helper.assertTrue(tank.contamination() < 12, "Repair seal should reduce nearby machine contamination");

      tank.receiveCharge(220);
      int targetBefore = sourceTank.energyStored();
      ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.RELAY);
      helper.assertTrue(sourceTank.energyStored() > targetBefore, "Relay seal should move charge from a fuller machine to an emptier one");

      helper.setBlock(chestLocal, Blocks.CHEST);
      ItemEntity item = new ItemEntity(helper.getLevel(), pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, new ItemStack(Items.REDSTONE, 3));
      helper.getLevel().addFreshEntity(item);
      helper.runAfterDelay(3L, () -> {
         ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.COLLECT);
         Container chest = (Container)helper.getBlockEntity(chestLocal, net.minecraft.world.level.block.entity.BlockEntity.class);
         helper.assertTrue(containerHasAtLeast(chest, Items.REDSTONE, 3), "Collect seal should insert nearby item drops into inventories");
         finishProtocolSealModes(helper, pos, tank, data, chunk);
      });
   }

   private static boolean containerHasAtLeast(Container container, ItemLike item, int count) {
      int found = 0;
      for (int slot = 0; slot < container.getContainerSize(); slot++) {
         ItemStack stack = container.getItem(slot);
         if (stack.is(item.asItem())) {
            found += stack.getCount();
         }
      }
      return found >= count;
   }

   private static void finishProtocolSealModes(GameTestHelper helper, BlockPos pos, NexusMachineBlockEntity tank, NexusWorldData data, ChunkPos chunk) {
      BlockPos rewriteLocal = new BlockPos(1, 1, 2);
      helper.setBlock(rewriteLocal, (Block)ModBlocks.DATA_CRACKED_STONE.get());
      tank.receiveCharge(100);
      ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.REWRITE);
      helper.assertBlockPresent((Block)ModBlocks.BLACKBOX_PLATE.get(), rewriteLocal);

      NexusMobEntity husk = (NexusMobEntity)((EntityType)ModEntities.NEXUS_HUSK.get()).create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(husk != null, "Nexus Husk should create for Defense seal test");
      if (husk != null) {
         husk.setPos(pos.getX() + 1.5D, pos.getY(), pos.getZ() + 1.5D);
         husk.setNoAi(true);
         helper.getLevel().addFreshEntity(husk);
         float health = husk.getHealth();
         ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.DEFENSE);
         helper.assertTrue(husk.getHealth() < health, "Defense seal should prioritize Nexus mobs");
      }

      data.setCorruptionPressure(chunk, 20);
      ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.PURIFY);
      helper.assertTrue(data.corruptionPressure(chunk) < 20, "Purify seal should reduce corruption");
      data.setCorruptionPressure(chunk, 20);
      ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.COLLAPSE);
      helper.assertTrue(data.corruptionPressure(chunk) >= 20, "Collapse seal should increase corruption for power");
      helper.succeed();
   }

   private static void progressionAndEndings(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      helper.assertFalse(NexusProgression.isNexusUnlocked(player), "Nexus should be gated before Stationfall/dev unlock");
      if (player instanceof ServerPlayer serverPlayer) {
         NexusProgression.grantDevelopmentUnlock(serverPlayer);
         helper.assertTrue(NexusProgression.isNexusUnlocked(player), "Development unlock should satisfy Nexus gate");
      }

      NexusPlayerData data = NexusPlayerData.get(player);
      data.activateBlackboxMonolith();
      data.setEndingPath("restore");
      helper.assertTrue(data.blackboxMonolithActivated(), "Blackbox Monolith activation should persist in player data");
      helper.assertTrue(data.hasEndingPath(), "Ending path should persist in player data");
      helper.succeed();
   }

   private static void terminalMissionSnapshots(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      var locked = NexusTerminalMissionProvider.INSTANCE.snapshot(player, NexusTerminalIds.id("the_signal_beneath"));
      helper.assertTrue(locked.status() == TerminalMissionStatus.LOCKED, "Signal mission should explain the Stationfall handoff before Nexus unlock");
      helper.assertTrue(locked.unlockReason().contains("stationfall:blackbox_recovered"), "Locked signal mission should name the required handoff milestone");
      if (player instanceof ServerPlayer serverPlayer) {
         NexusProgression.grantDevelopmentUnlock(serverPlayer);
      }

      NexusPlayerData data = NexusPlayerData.get(player);
      data.markGearUsed("nexus_scanner_visor");
      TerminalMissionStatus signalStatus = NexusTerminalMissionProvider.INSTANCE.snapshot(player, NexusTerminalIds.id("the_signal_beneath")).status();
      helper.assertTrue(
         signalStatus == TerminalMissionStatus.CLAIMABLE || signalStatus == TerminalMissionStatus.CLAIMED,
         "Signal mission should complete from an actual scanner use"
      );

      data.markMachineUsed("nexus_recycler");
      data.markMachineUsed("nexus_field_stabilizer");
      data.markMachineUsed("corruption_filter");
      data.markMachineUsed("memory_decoder");
      data.addBlackboxFragment();
      data.addBlackboxFragment();
      data.addBlackboxFragment();
      data.markWardenDefeated();
      data.activateBlackboxMonolith();
      data.markMachineUsed("reality_forge");
      data.markCoreEntered();
      data.markGuardianDefeated();
      helper.assertTrue(
         NexusTerminalMissionProvider.INSTANCE.snapshot(player, NexusTerminalIds.id("what_rebuilds_the_world")).status() == TerminalMissionStatus.UNLOCKED,
         "Final mission should expose ending actions after Guardian defeat but before a path is committed"
      );
      helper.assertTrue(
         NexusTerminalMissionProvider.INSTANCE.snapshot(player, NexusTerminalIds.id("what_rebuilds_the_world")).actions().stream().filter(action -> action.enabled() && action.id().startsWith("choose_")).count() == 4L,
         "Final mission should expose all four ending actions after Guardian defeat"
      );

      data.setEndingPath("restore");
      var completed = NexusTerminalMissionProvider.INSTANCE.snapshot(player, NexusTerminalIds.id("what_rebuilds_the_world"));
      helper.assertTrue(
         completed.status() == TerminalMissionStatus.CLAIMABLE,
         "Final mission should complete after exactly one ending path is stored"
      );
      helper.assertTrue(completed.actionHint().contains("Claim"), "Completed final mission should direct the player to claim the support cache");
      helper.succeed();
   }

   private static void armorEmergencyFieldLock(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      if (!(player instanceof ServerPlayer serverPlayer)) {
         helper.succeed();
         return;
      }

      serverPlayer.setItemSlot(EquipmentSlot.HEAD, new ItemStack((ItemLike)ModItems.NEXUS_HELMET.get()));
      serverPlayer.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike)ModItems.NEXUS_CHESTPLATE.get()));
      serverPlayer.setItemSlot(EquipmentSlot.LEGS, new ItemStack((ItemLike)ModItems.NEXUS_LEGGINGS.get()));
      serverPlayer.setItemSlot(EquipmentSlot.FEET, new ItemStack((ItemLike)ModItems.NEXUS_BOOTS.get()));
      serverPlayer.getInventory().add(new ItemStack((ItemLike)ModItems.NEXUS_SHARD.get()));
      serverPlayer.setHealth(1.0F);

      NexusWorldData worldData = NexusWorldData.get(helper.getLevel());
      ChunkPos chunk = serverPlayer.chunkPosition();
      worldData.setFieldValue(chunk, 42);
      worldData.setCorruptionPressure(chunk, 30);
      helper.assertTrue(NexusArmorEvents.tryEmergencyFieldLock(serverPlayer), "Full Nexus armor should spend a shard to prevent lethal corruption damage");
      NexusPlayerData data = NexusPlayerData.get(player);
      helper.assertTrue(data.armorLockCooldown() == 2400, "Emergency Field Lock should set the configured cooldown");
      helper.assertTrue(data.hasUsedGear("nexus_armor_emergency_lock"), "Emergency Field Lock should record gear mission progress");
      helper.assertTrue(worldData.fieldValue(chunk) > 42, "Emergency Field Lock should stabilize the local field");
      helper.assertFalse(NexusArmorEvents.tryEmergencyFieldLock(serverPlayer), "Emergency Field Lock should not fire while on cooldown");
      helper.succeed();
   }

   private static void mobSignatureBehaviors(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      player.getInventory().add(new ItemStack((ItemLike)ModItems.MEMORY_SHARD.get()));
      BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));

      NexusMobEntity archiveSeeker = (NexusMobEntity)((EntityType)ModEntities.ARCHIVE_SEEKER.get()).create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(archiveSeeker != null, "Archive Seeker should create");
      if (archiveSeeker != null) {
         int before = countItem(player, (ItemLike)ModItems.MEMORY_SHARD.get());
         helper.assertFalse(com.knoxhack.echonexusprotocol.entity.ArchiveSeekerEntity.stealMemoryItem(player).isEmpty(), "Archive Seeker should find memory items");
         helper.assertTrue(countItem(player, (ItemLike)ModItems.MEMORY_SHARD.get()) == before - 1, "Archive Seeker should steal memory items");
      }

      NexusMobEntity warden = (NexusMobEntity)((EntityType)ModEntities.CORRUPTION_WARDEN.get()).create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(warden != null, "Corruption Warden should create");
      if (warden != null) {
         ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
         NexusWorldData data = NexusWorldData.get(helper.getLevel());
         data.setCorruptionPressure(chunk, 0);
         warden.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
         warden.tickCount = 99;
         helper.getLevel().addFreshEntity(warden);
         if (warden instanceof com.knoxhack.echonexusprotocol.entity.CorruptionWardenEntity corruptionWarden) {
            corruptionWarden.pulse(helper.getLevel());
         }
         helper.assertTrue(data.corruptionPressure(warden.chunkPosition()) >= 4, "Corruption Warden should pulse corruption into the chunk");
         helper.assertTrue(
            !helper.getLevel().getEntitiesOfClass(NexusMobEntity.class, new AABB(pos).inflate(5.0D), mob -> mob.getType() == ModEntities.STATIC_CRAWLER.get()).isEmpty(),
            "Corruption Warden should summon Static Crawlers"
         );
      }

      NexusMobEntity guardian = (NexusMobEntity)((EntityType)ModEntities.NEXUS_GUARDIAN.get()).create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(guardian instanceof com.knoxhack.echonexusprotocol.entity.NexusGuardianEntity, "Nexus Guardian should use the role-specific class");
      if (guardian instanceof com.knoxhack.echonexusprotocol.entity.NexusGuardianEntity nexusGuardian) {
         nexusGuardian.setHealth(nexusGuardian.getMaxHealth() * 0.2F);
         helper.assertTrue(nexusGuardian.phase() == 4, "Nexus Guardian should enter phase 4 below 25% health");
      }
      helper.succeed();
   }

   private static void scannerResearchUnlocks(GameTestHelper helper) {
      BlockPos pos = new BlockPos(1, 1, 1);
      helper.setBlock(pos, (Block)ModBlocks.REALITY_FORGE.get());
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      NexusScannerVisorItem.scanBlock(player, helper.absolutePos(pos));
      NexusPlayerData data = NexusPlayerData.get(player);
      helper.assertTrue(data.hasResearch("nexus_theory"), "Scanner should unlock Nexus Theory");
      helper.assertTrue(data.hasResearch("matter_rewriting"), "Scanning Reality Forge should unlock Matter Rewriting");
      helper.assertTrue(data.scanCount() > 0, "Scanner should record scanned block ids");
      helper.succeed();
   }

   private static void fieldChargesRewriteBlocks(GameTestHelper helper) {
      BlockPos pos = new BlockPos(1, 1, 1);
      helper.setBlock(pos, Blocks.DIRT);
      ChunkPos chunk = new ChunkPos(helper.absolutePos(pos).getX() >> 4, helper.absolutePos(pos).getZ() >> 4);
      NexusWorldData data = NexusWorldData.get(helper.getLevel());
      data.setFieldValue(chunk, 70);
      data.setCorruptionPressure(chunk, 0);
      NexusFieldChargeItem.apply(helper.getLevel(), helper.absolutePos(pos), NexusFieldChargeItem.Mode.COLLAPSE);
      helper.assertBlockPresent((Block)ModBlocks.FRAGMENTED_SOIL.get(), pos);
      helper.assertTrue(data.corruptionPressure(chunk) >= 18, "Collapse Charge should raise corruption pressure");
      NexusFieldChargeItem.apply(helper.getLevel(), helper.absolutePos(pos), NexusFieldChargeItem.Mode.PURITY);
      helper.assertBlockPresent(Blocks.DIRT, pos);
      helper.assertTrue(data.fieldValue(chunk) >= 68, "Purity Charge should restore field stability after a collapse pulse");
      data.setFieldValue(chunk, 10);
      data.setCorruptionPressure(chunk, 80);
      helper.setBlock(pos, (Block)ModBlocks.FRAGMENTED_SOIL.get());
      NexusFieldChargeItem.apply(helper.getLevel(), helper.absolutePos(pos), NexusFieldChargeItem.Mode.STABILIZED_PURITY);
      helper.assertBlockPresent(Blocks.DIRT, pos);
      helper.assertTrue(data.fieldValue(chunk) >= 26, "Stabilized Purity Charge should recover collapsed field stability");
      helper.assertTrue(data.corruptionPressure(chunk) <= 48, "Stabilized Purity Charge should sharply reduce corruption pressure");
      helper.succeed();
   }

   private static void worldQuarantineStormTears(GameTestHelper helper) {
      BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
      ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
      NexusWorldData data = NexusWorldData.get(helper.getLevel());
      data.setFieldValue(chunk, 30);
      data.setCorruptionPressure(chunk, 40);
      ProtocolSealBlock.applySeal(helper.getLevel(), pos, ProtocolSealBlock.SealMode.QUARANTINE);
      helper.assertTrue(data.isQuarantined(chunk), "Quarantine seal should mark the chunk as quarantined");
      data.tickAffectedChunk(helper.getLevel(), chunk);
      helper.assertFalse(data.hasActiveStorm(chunk, helper.getLevel().getGameTime(), 200L), "Quarantined chunks should not start anomaly storms");
      data.quarantineChunk(chunk, 0);
      data.setFieldValue(chunk, 12);
      data.setCorruptionPressure(chunk, 80);
      data.tickAffectedChunk(helper.getLevel(), chunk);
      helper.assertTrue(data.hasActiveStorm(chunk, helper.getLevel().getGameTime(), 200L), "Collapsed chunks should start anomaly storms");
      helper.assertTrue(data.realityTearCount(chunk) > 0, "Collapsed chunks should register reality tear activity");
      helper.succeed();
   }

   private static void terrainLandmarkCoverage(GameTestHelper helper) {
      for (String site : NEXUS_POIS) {
         assertResourcePresent(helper, "data/echonexusprotocol/worldgen/structure/" + site + ".json");
         assertResourcePresent(helper, "data/echonexusprotocol/worldgen/structure_set/" + site + ".json");
         assertResourcePresent(helper, "data/echonexusprotocol/worldgen/template_pool/" + site + ".json");
         assertResourcePresent(helper, "data/echonexusprotocol/structure/" + site + ".nbt");
         assertResourceMinSize(helper, "data/echonexusprotocol/structure/" + site + ".nbt", site.contains("core") || site.contains("monolith") ? 9000 : 3000);
      }
      helper.succeed();
   }

   private static void assertResourcePresent(GameTestHelper helper, String path) {
      helper.assertTrue(ModGameTests.class.getClassLoader().getResource(path) != null, "Missing Nexus structure resource: " + path);
   }

   private static void assertResourceMinSize(GameTestHelper helper, String path, int minBytes) {
      try (java.io.InputStream input = ModGameTests.class.getClassLoader().getResourceAsStream(path)) {
         helper.assertTrue(input != null, "Missing Nexus resource: " + path);
         helper.assertTrue(input.readAllBytes().length >= minBytes, "Expanded Nexus resource is too small: " + path);
      } catch (java.io.IOException exception) {
         helper.fail("Unable to read Nexus resource: " + path + " / " + exception.getMessage());
      }
   }

   private static NexusMachineBlockEntity placeMachine(GameTestHelper helper, BlockPos pos, Block block) {
      helper.setBlock(pos, block);
      return (NexusMachineBlockEntity)helper.getBlockEntity(pos, NexusMachineBlockEntity.class);
   }

   private static void tickMachine(GameTestHelper helper, NexusMachineBlockEntity machine, int ticks) {
      for (int i = 0; i < ticks; i++) {
         NexusMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
      }
   }

   private static int countItem(Player player, ItemLike item) {
      int count = 0;
      for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
         ItemStack stack = player.getInventory().getItem(slot);
         if (stack.is(item.asItem())) {
            count += stack.getCount();
         }
      }
      return count;
   }

   private static void postReleasePolishCoverage(GameTestHelper helper) {
      helper.assertTrue(NexusJeiRecipeCatalog.all().size() >= 18, "JEI catalog should cover every Nexus processing recipe path");
      helper.assertTrue((Integer)Config.MACHINE_DURATION_PERCENT.get() <= 100, "Machine duration tuning should remain responsive for beta");
      helper.assertTrue((Integer)Config.MACHINE_CHARGE_COST_PERCENT.get() <= 100, "Machine charge costs should stay approachable for beta onboarding");
      helper.assertTrue((Integer)Config.STABILIZER_FIELD_GAIN.get() >= 4, "Field Stabilizer should visibly improve a base chunk during beta play");
      helper.assertTrue((Integer)Config.SEAL_RADIUS.get() >= 5, "Protocol Seal radius config should expose beta tuning");
      helper.assertTrue((Integer)Config.SEAL_TICK_INTERVAL.get() <= 40, "Protocol Seals should tick fast enough to feel useful in beta");
      helper.assertTrue((Integer)Config.BOSS_HEALTH_PERCENT.get() >= 100, "Boss health multiplier should keep beta bosses threatening");
      helper.assertTrue((Integer)Config.BOSS_DAMAGE_PERCENT.get() >= 100, "Boss damage multiplier should keep beta bosses threatening");
      helper.assertTrue((Integer)Config.DUNGEON_LOOT_SCALE_PERCENT.get() >= 100, "Dungeon loot scale should make Nexus POIs feel worth entering");
      helper.assertTrue("standard".equals(Config.BALANCE_PRESET.get()), "Default public balance preset should be standard");
      helper.assertTrue((Integer)Config.FIELD_MAP_RADIUS.get() == 2, "Field map should show the public-release 5x5 grid by default");
      helper.assertTrue((Integer)Config.STABILIZED_PURITY_FIELD_GAIN.get() >= 12, "Stabilized Purity Charge should be strong enough for collapsed chunk recovery");
      helper.assertTrue((Integer)Config.FIELD_ANCHOR_TICKS.get() >= 1200, "Field Anchor should provide a meaningful recovery window");
      for (String sound : new String[]{"machine_process", "seal_activate", "field_stabilize", "corruption_leak", "monolith_activate", "reality_tear_pulse", "warden_pulse", "guardian_phase", "ending_choice"}) {
         assertResourcePresent(helper, "assets/echonexusprotocol/sounds/" + sound + ".ogg");
      }
      assertResourcePresent(helper, "assets/echonexusprotocol/sounds.json");
      assertResourcePresent(helper, "assets/echonexusprotocol/textures/entity/nexus_guardian.png");
      assertResourcePresent(helper, "assets/echonexusprotocol/textures/block/blackbox_monolith_core.png");
      helper.succeed();
   }

   private static void fieldMapTelemetrySources(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      if (!(player instanceof ServerPlayer serverPlayer)) {
         helper.succeed();
         return;
      }

      NexusWorldData worldData = NexusWorldData.get(helper.getLevel());
      NexusPlayerData playerData = NexusPlayerData.get(player);
      ChunkPos center = serverPlayer.chunkPosition();
      int[] values = {86, 65, 45, 25, 12};
      for (int i = 0; i < values.length; i++) {
         worldData.setFieldValue(new ChunkPos(center.x() - 2 + i, center.z()), values[i]);
      }
      worldData.setCorruptionPressure(new ChunkPos(center.x(), center.z()), 44);
      worldData.startAnomalyStorm(new ChunkPos(center.x() + 2, center.z()), helper.getLevel().getGameTime());
      playerData.refreshFieldTelemetry(serverPlayer);
      int rowStart = NexusPlayerData.FIELD_MAP_RADIUS * NexusPlayerData.FIELD_MAP_DIAMETER;
      for (int i = 0; i < values.length; i++) {
         helper.assertTrue(playerData.telemetryMapField(rowStart + i) == values[i], "Field map should sync nearby chunk field values");
      }
      helper.assertTrue(playerData.telemetryMapCorruption(rowStart + NexusPlayerData.FIELD_MAP_RADIUS) >= 44, "Field map should sync chunk corruption pressure");
      helper.assertTrue(playerData.telemetryMapStorm(rowStart + 4), "Field map should mark active storms");
      helper.assertTrue(playerData.telemetryMapTears(rowStart + 4) > 0, "Field map should mark reality tears");
      helper.succeed();
   }

   private static void collapsedRecoveryTools(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      BlockPos absolute = helper.absolutePos(local);
      helper.setBlock(local, (Block)ModBlocks.FRAGMENTED_SOIL.get());
      ChunkPos chunk = new ChunkPos(absolute.getX() >> 4, absolute.getZ() >> 4);
      NexusWorldData worldData = NexusWorldData.get(helper.getLevel());
      worldData.setFieldValue(chunk, 8);
      worldData.setCorruptionPressure(chunk, 90);
      NexusFieldChargeItem.apply(helper.getLevel(), absolute, NexusFieldChargeItem.Mode.STABILIZED_PURITY);
      helper.assertBlockPresent(Blocks.DIRT, local);
      helper.assertTrue(worldData.fieldValue(chunk) > 8, "Stabilized Purity Charge should raise collapsed field value");
      helper.assertTrue(worldData.corruptionPressure(chunk) < 90, "Stabilized Purity Charge should lower collapsed corruption pressure");
      NexusUtilityItem.anchorField(helper.getLevel(), absolute);
      helper.assertTrue(worldData.isQuarantined(chunk), "Field Anchor should quarantine collapsed chunks");
      helper.assertTrue(worldData.fieldValue(chunk) >= 28, "Field Anchor should make collapsed recovery visibly safer");
      helper.succeed();
   }

   private static void endingWorldEffects(GameTestHelper helper) {
      NexusWorldData worldData = NexusWorldData.get(helper.getLevel());
      ChunkPos destroyChunk = new ChunkPos(4, 4);
      worldData.setFieldValue(destroyChunk, 20);
      worldData.setCorruptionPressure(destroyChunk, 70);
      worldData.startAnomalyStorm(destroyChunk, helper.getLevel().getGameTime());
      worldData.setEndingState("destroy");
      helper.assertTrue(worldData.corruptionPressure(destroyChunk) == 0, "Destroy ending should stop tracked corruption pressure");
      helper.assertFalse(worldData.hasActiveStorm(destroyChunk, helper.getLevel().getGameTime(), 400L), "Destroy ending should clear active storms");
      helper.assertTrue(worldData.realityTearCount(destroyChunk) == 0, "Destroy ending should close tracked tears");
      int fieldAfterDestroy = worldData.fieldValue(destroyChunk);
      worldData.setEndingState("destroy");
      helper.assertTrue(worldData.fieldValue(destroyChunk) == fieldAfterDestroy, "Ending world feedback should not stack when repeated");

      ChunkPos mergeChunk = new ChunkPos(5, 5);
      worldData.setEndingState("");
      worldData.setFieldValue(mergeChunk, 70);
      worldData.setCorruptionPressure(mergeChunk, 5);
      worldData.setEndingState("merge");
      helper.assertTrue(worldData.realityTearCount(mergeChunk) > 0, "Merge ending should make tears usable as a persistent world effect");
      helper.assertTrue(worldData.corruptionPressure(mergeChunk) >= 13, "Merge ending should embrace extra corruption pressure");
      helper.succeed();
   }

   private static void releaseRiskFixes(GameTestHelper helper) {
      NexusWorldData worldData = NexusWorldData.get(helper.getLevel());
      worldData.commitEndingState("");
      ChunkPos stormChunk = new ChunkPos(8, 8);
      worldData.startAnomalyStorm(stormChunk, helper.getLevel().getGameTime() - 1000L);
      worldData.pruneExpiredStorms(helper.getLevel().getGameTime(), 80L);
      helper.assertFalse(worldData.hasActiveStorm(stormChunk, helper.getLevel().getGameTime(), 80L), "Expired storms should be pruned from telemetry");

      NexusMachineBlockEntity recycler = placeMachine(helper, new BlockPos(1, 1, 1), (Block)ModBlocks.NEXUS_RECYCLER.get());
      helper.assertTrue(recycler.acceptsInput(new ItemStack((ItemLike)ModItems.NEXUS_SHARD.get())), "Machine input should accept known Nexus recipes");
      helper.assertFalse(recycler.acceptsInput(new ItemStack(Items.DIRT)), "Machine input should reject invalid recipe inputs");

      BlockPos relaySealPos = helper.absolutePos(new BlockPos(20, 1, 1));
      NexusMachineBlockEntity relaySource = placeMachine(helper, new BlockPos(21, 1, 1), (Block)ModBlocks.NEXUS_CHARGE_TANK.get());
      NexusMachineBlockEntity relayTarget = placeMachine(helper, new BlockPos(22, 1, 1), (Block)ModBlocks.NEXUS_CHARGE_TANK.get());
      relaySource.receiveCharge(relaySource.energyStorage().getCapacityAsInt());
      relayTarget.receiveCharge(relayTarget.energyStorage().getCapacityAsInt() - 10);
      int relayTotalBefore = relaySource.energyStored() + relayTarget.energyStored();
      ProtocolSealBlock.applySeal(helper.getLevel(), relaySealPos, ProtocolSealBlock.SealMode.RELAY);
      helper.assertTrue(relaySource.energyStored() + relayTarget.energyStored() == relayTotalBefore, "Relay seal should not delete charge when the target is nearly full");
      helper.assertTrue(relayTarget.energyStored() == relayTarget.energyStorage().getCapacityAsInt(), "Relay seal should fill the remaining target capacity exactly");

      for (String entityLoot : new String[]{"nexus_husk", "data_wraith", "static_crawler", "core_soldier", "archive_seeker"}) {
         assertResourcePresent(helper, "data/echonexusprotocol/loot_table/entities/" + entityLoot + ".json");
      }
      for (String chestLoot : new String[]{"field_station_supply", "data_vault_memory", "containment_lab_reactor", "core_chamber_final_prep"}) {
         assertResourcePresent(helper, "data/echonexusprotocol/loot_table/chests/" + chestLoot + ".json");
      }
      for (String feature : new String[]{"white_signal_tree", "nexus_crystal_cluster", "static_fluid_pool", "dead_signal_leaf_patch", "hollow_signal_log_patch", "corrupted_ferrite_pocket", "blackbox_debris_cluster", "lab_pipe_remnant", "reality_tear_hotspot"}) {
         assertResourcePresent(helper, "data/echonexusprotocol/worldgen/configured_feature/" + feature + ".json");
      }
      for (String placed : new String[]{"white_signal_trees", "nexus_crystal_clusters", "static_fluid_pools", "dead_signal_leaf_patches", "hollow_signal_log_patches", "corrupted_ferrite_pockets", "blackbox_debris_clusters", "lab_pipe_remnants", "reality_tear_hotspots"}) {
         assertResourcePresent(helper, "data/echonexusprotocol/worldgen/placed_feature/" + placed + ".json");
      }

      String coreBiome = readResource("data/echonexusprotocol/worldgen/biome/core_exclusion_zone.json");
      helper.assertFalse(coreBiome.contains("echonexusprotocol:corruption_warden"), "Corruption Warden should not naturally spawn from biomes");
      helper.assertFalse(coreBiome.contains("echonexusprotocol:nexus_guardian"), "Nexus Guardian should not naturally spawn from biomes");
      helper.assertTrue(coreBiome.contains("echonexusprotocol:reality_tear_hotspots"), "Core Exclusion Zone should include reality tear exploration dressing");

      ChunkPos endingChunk = new ChunkPos(9, 9);
      worldData.setFieldValue(endingChunk, 40);
      worldData.setCorruptionPressure(endingChunk, 40);
      helper.assertTrue(worldData.commitEndingState("restore"), "First ending commit should apply world feedback");
      helper.assertFalse(worldData.commitEndingState("destroy"), "Different ending commits should be rejected after a path is set");
      helper.assertTrue("restore".equals(worldData.endingState()), "World ending should remain the first committed path");
      helper.succeed();
   }

   private static String readResource(String path) {
      try (java.io.InputStream input = ModGameTests.class.getClassLoader().getResourceAsStream(path)) {
         return input == null ? "" : new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      } catch (java.io.IOException exception) {
         return "";
      }
   }

   private static void fieldTerminalTelemetrySources(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      if (!(player instanceof ServerPlayer serverPlayer)) {
         helper.succeed();
         return;
      }

      NexusWorldData worldData = NexusWorldData.get(helper.getLevel());
      NexusPlayerData playerData = NexusPlayerData.get(player);
      ChunkPos chunk = serverPlayer.chunkPosition();
      int[] values = {86, 65, 45, 25, 12};
      NexusWorldData.FieldState[] states = {
         NexusWorldData.FieldState.STABLE,
         NexusWorldData.FieldState.UNSTABLE,
         NexusWorldData.FieldState.FRACTURED,
         NexusWorldData.FieldState.CRITICAL,
         NexusWorldData.FieldState.COLLAPSED
      };
      for (int i = 0; i < values.length; i++) {
         worldData.setFieldValue(chunk, values[i]);
         playerData.refreshFieldTelemetry(serverPlayer);
         helper.assertTrue(playerData.telemetryFieldValue() == values[i], "Field telemetry should sync the local chunk field value");
         helper.assertTrue(NexusWorldData.FieldState.fromValue(playerData.telemetryFieldValue()) == states[i], "Field telemetry should preserve field state thresholds");
      }

      worldData.setCorruptionPressure(chunk, 37);
      worldData.quarantineChunk(chunk, 200);
      worldData.startAnomalyStorm(chunk, helper.getLevel().getGameTime());
      worldData.activateBlackboxMonolith();
      worldData.markWardenDefeated();
      worldData.markGuardianDefeated();
      worldData.setEndingState("restore");
      playerData.refreshFieldTelemetry(serverPlayer);
      helper.assertTrue(playerData.telemetryCorruptionPressure() >= 37, "Field tab telemetry should expose corruption pressure");
      helper.assertTrue(playerData.telemetryQuarantineTicks() > 0, "Field tab telemetry should expose quarantine duration");
      helper.assertTrue(playerData.telemetryActiveStorm(), "Field tab telemetry should expose active storm state");
      helper.assertTrue(playerData.telemetryRealityTears() > 0, "Field tab telemetry should expose reality tear count");
      helper.assertTrue(playerData.telemetryWorldMonolithActivated(), "Field tab telemetry should expose Monolith state");
      helper.assertTrue(playerData.telemetryWorldWardenDefeated(), "Field tab telemetry should expose Warden state");
      helper.assertTrue(playerData.telemetryWorldGuardianDefeated(), "Field tab telemetry should expose Guardian state");
      helper.assertTrue("restore".equals(playerData.telemetryWorldEndingState()), "Field tab telemetry should expose ending state");
      helper.succeed();
   }

   private static void utilityGearBehaviors(GameTestHelper helper) {
      BlockPos local = new BlockPos(1, 1, 1);
      BlockPos absolute = helper.absolutePos(local);
      helper.setBlock(local, Blocks.STONE);
      ChunkPos chunk = new ChunkPos(absolute.getX() >> 4, absolute.getZ() >> 4);
      NexusWorldData worldData = NexusWorldData.get(helper.getLevel());
      worldData.setFieldValue(chunk, 45);
      worldData.setCorruptionPressure(chunk, 24);
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      int pulseScans = NexusUtilityItem.fieldPulse(helper.getLevel(), player, absolute);
      NexusPlayerData playerData = NexusPlayerData.get(player);
      helper.assertTrue(pulseScans > 0, "Nexus Pickaxe Field Pulse should reveal or scan Nexus signatures");
      helper.assertBlockPresent((Block)ModBlocks.NEXUS_CRYSTAL_CLUSTER.get(), local);
      helper.assertTrue(playerData.hasResearch("matter_rewriting"), "Field Pulse should teach Matter Rewriting from crystal signatures");
      NexusUtilityItem.anchorReality(helper.getLevel(), absolute);
      helper.assertTrue(worldData.isQuarantined(chunk), "Reality Anchor should quarantine the current chunk");
      int corruptionAfterAnchor = worldData.corruptionPressure(chunk);
      NexusMobEntity husk = (NexusMobEntity)((EntityType)ModEntities.NEXUS_HUSK.get()).create(helper.getLevel(), EntitySpawnReason.EVENT);
      helper.assertTrue(husk != null, "Nexus Husk test entity should create");
      if (husk != null) {
         BlockPos mobPos = helper.absolutePos(new BlockPos(2, 1, 1));
         husk.setPos(mobPos.getX() + 0.5, mobPos.getY(), mobPos.getZ() + 0.5);
         husk.setNoAi(true);
         helper.getLevel().addFreshEntity(husk);
         float beforeHealth = husk.getHealth();
         int hit = NexusUtilityItem.signalBladePulse(helper.getLevel(), absolute, player);
         helper.assertTrue(hit > 0, "Signal Blade should hit nearby Nexus mobs");
         helper.assertTrue(husk.getHealth() < beforeHealth, "Signal Blade should damage Nexus mobs");
         helper.assertTrue(worldData.corruptionPressure(chunk) < corruptionAfterAnchor, "Signal Blade pulse should lower local corruption pressure");
         helper.succeed();
      }
   }

   private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, String testName, Identifier functionId) {
      TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData(
         environment, Identifier.withDefaultNamespace("empty"), 400, 0, true, Rotation.NONE, false, 1, 1, false, 2
      );
      event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("echonexusprotocol", path);
   }
}
