package com.knoxhack.echoagriculturereclamation.progress;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoagriculturereclamation.content.CropSpec;
import com.knoxhack.echoagriculturereclamation.content.ReclamationContent;
import com.knoxhack.echoagriculturereclamation.content.ReclamationMetrics;
import com.knoxhack.echoagriculturereclamation.content.SeedProfile;
import com.knoxhack.echoagriculturereclamation.content.SoilState;
import com.knoxhack.echoagriculturereclamation.block.ReclamationCropBlock;
import com.knoxhack.echoagriculturereclamation.integration.ReclamationCrossAddonIntegration;
import com.knoxhack.echoagriculturereclamation.registry.ModBlocks;
import com.knoxhack.echoagriculturereclamation.registry.ModDataComponents;
import com.knoxhack.echoagriculturereclamation.registry.ModItems;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ReclamationProgress {
   public static final String ROOT = "echoagriculturereclamation_progress";

   private ReclamationProgress() {
   }

   public static CompoundTag data(Player player) {
      CompoundTag root = player.getPersistentData().getCompoundOrEmpty(ROOT);
      player.getPersistentData().put(ROOT, root);
      return root;
   }

   public static Set<String> knownSeeds(Player player) {
      CompoundTag data = data(player);
      int count = data.getIntOr("known_seed_count", 0);
      LinkedHashSet<String> seeds = new LinkedHashSet<>();
      for (int index = 0; index < count; index++) {
         String value = data.getStringOr("known_seed_" + index, "");
         if (!value.isBlank()) {
            seeds.add(value);
         }
      }
      return seeds;
   }

   public static boolean discoverSeed(Player player, CropSpec spec) {
      LinkedHashSet<String> seeds = new LinkedHashSet<>(knownSeeds(player));
      boolean changed = seeds.add(spec.path());
      writeKnownSeeds(player, seeds);
      mark(player, "seed_recovered");
      mark(player, "seed_analyzed");
      max(player, "known_seed_total", seeds.size());
      discoverRoutes(player);
      return changed;
   }

   public static void writeKnownSeeds(Player player, Set<String> seeds) {
      CompoundTag data = data(player);
      int index = 0;
      for (String seed : seeds) {
         data.putString("known_seed_" + index++, seed);
      }
      data.putInt("known_seed_count", index);
   }

   public static void mark(Player player, String flag) {
      boolean first = !flag(player, flag);
      data(player).putBoolean(flag, true);
      if (first) {
         recordCoreMilestone(player, flag);
      }
   }

   public static boolean flag(Player player, String flag) {
      return data(player).getBoolean(flag).orElse(false);
   }

   public static int value(Player player, String key) {
      return data(player).getIntOr(key, 0);
   }

   public static void add(Player player, String key, int amount) {
      if (amount <= 0) {
         return;
      }
      CompoundTag data = data(player);
      data.putInt(key, Math.min(2_000_000_000, data.getIntOr(key, 0) + amount));
   }

   public static void max(Player player, String key, int value) {
      CompoundTag data = data(player);
      data.putInt(key, Math.max(data.getIntOr(key, 0), value));
   }

   public static boolean claimed(Player player, String id) {
      return flag(player, "claimed_" + id);
   }

   public static void claim(Player player, String id) {
      mark(player, "claimed_" + id);
      discoverRoutes(player);
   }

   public static void recordGrowth(Player player, CropSpec spec, boolean stabilized) {
      mark(player, "first_growth");
      add(player, "crops_grown", 1);
      if (stabilized) {
         max(player, "crop_stability", 100);
      } else {
         max(player, "crop_stability", cropStability(player));
      }
      max(player, "food_security", foodSecurity(player));
      int restorationWeight = ReclamationContent.crop(spec).restorationWeight();
      if (restorationWeight > 1) {
         add(player, "restoration_crop_growth", restorationWeight);
      }
      discoverRoutes(player);
   }

   public static boolean needsStabilizationSeed(Player player) {
      return !flag(player, "gene_stabilization") && !flag(player, "stabilization_seed_recovered");
   }

   public static void recordStabilizationSeed(Player player) {
      mark(player, "stabilization_seed_recovered");
      discoverRoutes(player);
   }

   public static void recordStabilization(Player player) {
      mark(player, "gene_stabilization");
      max(player, "crop_stability", 100);
      add(player, "stabilized_seeds", 1);
      discoverRoutes(player);
   }

   public static ReclamationMetrics metrics(Player player) {
      if (player == null) {
         return new ReclamationMetrics(0, SoilState.DEAD, 0, 0, 0, 0);
      }
      Level level = player.level();
      BlockPos pos = player.blockPosition();
      SoilState soil = detectSoil(level, pos);
      int greenhouse = scanGreenhouseSafety(level, pos);
      int stability = cropStability(player);
      int food = foodSecurity(player);
      int restoration = 0;
      if (level instanceof ServerLevel serverLevel) {
         ReclamationWorldData world = ReclamationWorldData.get(serverLevel);
         ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
         restoration = world.restorationScore(chunk);
         world.setGreenhouseSafety(chunk, greenhouse);
         world.setLastSoilState(chunk, soil.displayName());
      }
      max(player, "greenhouse_safety", greenhouse);
      max(player, "crop_stability", stability);
      max(player, "food_security", food);
      max(player, "restoration_score", restoration);
      return new ReclamationMetrics(knownSeeds(player).size(), soil, greenhouse, stability, food, restoration);
   }

   public static SoilState detectSoil(Level level, BlockPos center) {
      SoilState best = SoilState.DEAD;
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 1, 2))) {
         SoilState state = SoilState.fromBlock(level.getBlockState(pos));
         if (state.ordinal() > best.ordinal()) {
            best = state;
         }
      }
      return best;
   }

   public static int scanGreenhouseSafety(Level level, BlockPos center) {
      return scanGreenhouse(level, center).score();
   }

   public static GreenhouseScan scanGreenhouse(Level level, BlockPos center) {
      int glass = 0;
      int filters = 0;
      int activeDocks = 0;
      int idleDocks = 0;
      int controllers = 0;
      int trays = 0;
      int cropTargets = 0;
      var rules = ReclamationContent.machines();
      BlockPos min = center.offset(-rules.greenhouseHorizontalRange(), -rules.greenhouseDownRange(), -rules.greenhouseHorizontalRange());
      BlockPos max = center.offset(rules.greenhouseHorizontalRange(), rules.greenhouseUpRange(), rules.greenhouseHorizontalRange());
      for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
         var block = level.getBlockState(pos).getBlock();
         if (block == ModBlocks.GREENHOUSE_GLASS.get()) {
            glass++;
         } else if (block == ModBlocks.SPORE_FILTER.get()) {
            filters++;
         } else if (block == ModBlocks.POLLINATOR_DRONE_DOCK.get()) {
            if (pollinationTargets(level, pos) > 0) {
               activeDocks++;
            } else {
               idleDocks++;
            }
         } else if (block == ModBlocks.GREENHOUSE_CONTROLLER.get()) {
            controllers++;
         } else if (block == ModBlocks.HYDROPONIC_TRAY.get()) {
            trays++;
            cropTargets++;
         } else if (block instanceof ReclamationCropBlock) {
            cropTargets++;
         }
      }

      EnclosureScan enclosure = scanEnclosure(level, center, min, max);
      int supportScore = glass * rules.greenhouseGlassWeight()
         + filters * rules.greenhouseFilterWeight()
         + activeDocks * rules.greenhouseDockWeight()
         + idleDocks * Math.max(1, rules.greenhouseDockWeight() / 3)
         + controllers * rules.greenhouseControllerWeight()
         + trays * rules.greenhouseTrayWeight();
      int enclosureScore = enclosure.enclosed() ? 18 : 0;
      if (enclosure.enclosed() && enclosure.hasGreenhouseRoof()) {
         enclosureScore += 10;
      }
      if (enclosure.enclosed() && enclosure.hasFloor()) {
         enclosureScore += 4;
      }
      if (enclosure.enclosed() && cropTargets > 0) {
         enclosureScore += 3;
      }
      if (enclosure.enclosed() && filters > 0) {
         enclosureScore += 3;
      }
      enclosureScore = Math.min(35, enclosureScore);

      int score = Math.min(100, supportScore + enclosureScore);
      int safeThreshold = ReclamationContent.progression().greenhouseSafeThreshold();
      if (!enclosure.enclosed()) {
         score = Math.min(score, Math.max(0, safeThreshold - 10));
      } else if (!enclosure.hasGreenhouseRoof()) {
         score = Math.min(score, Math.max(0, safeThreshold - 5));
      }
      return new GreenhouseScan(
         score,
         Math.min(100, supportScore),
         enclosureScore,
         glass,
         filters,
         activeDocks,
         idleDocks,
         controllers,
         trays,
         cropTargets,
         enclosure.enclosed(),
         enclosure.hasGreenhouseRoof(),
         enclosure.hasFloor(),
         enclosure.interiorVolume()
      );
   }

   public static int pollinationTargets(Level level, BlockPos dockPos) {
      int targets = 0;
      for (BlockPos pos : BlockPos.betweenClosed(dockPos.offset(-4, -2, -4), dockPos.offset(4, 2, 4))) {
         if (isPollinationTarget(level.getBlockState(pos).getBlock())) {
            targets++;
         }
      }
      return targets;
   }

   private static EnclosureScan scanEnclosure(Level level, BlockPos center, BlockPos min, BlockPos max) {
      List<BlockPos> candidates = new ArrayList<>();
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 3, 3))) {
         BlockPos candidate = pos.immutable();
         if (inside(candidate, min, max) && isInteriorPassable(level, candidate)) {
            candidates.add(candidate);
         }
      }
      candidates.sort(Comparator.comparingLong(pos -> distanceSquared(pos, center)));
      EnclosureScan best = EnclosureScan.open();
      for (BlockPos candidate : candidates) {
         EnclosureScan scan = floodInterior(level, candidate, min, max);
         if (scan.enclosed()) {
            if (!best.enclosed() || scan.interiorVolume() > best.interiorVolume()) {
               best = scan;
            }
         } else if (!best.enclosed() && scan.interiorVolume() > best.interiorVolume()) {
            best = scan;
         }
      }
      return best;
   }

   private static EnclosureScan floodInterior(Level level, BlockPos start, BlockPos min, BlockPos max) {
      Queue<BlockPos> queue = new ArrayDeque<>();
      Set<BlockPos> visited = new HashSet<>();
      queue.add(start);
      visited.add(start);
      boolean escaped = false;
      boolean hasRoof = false;
      boolean hasFloor = false;
      while (!queue.isEmpty()) {
         BlockPos pos = queue.remove();
         if (touchesBoundary(pos, min, max)) {
            escaped = true;
         }
         hasRoof = hasRoof || hasGreenhouseRoofAbove(level, pos, max.getY());
         BlockPos below = pos.below();
         hasFloor = hasFloor || !inside(below, min, max) || !isInteriorPassable(level, below);
         for (Direction direction : Direction.values()) {
            BlockPos next = pos.relative(direction).immutable();
            if (!inside(next, min, max)) {
               escaped = true;
            } else if (!visited.contains(next) && isInteriorPassable(level, next)) {
               visited.add(next);
               queue.add(next);
            }
         }
      }
      return new EnclosureScan(!escaped && visited.size() >= 4, hasRoof, hasFloor, visited.size());
   }

   private static boolean hasGreenhouseRoofAbove(Level level, BlockPos pos, int maxY) {
      for (int y = pos.getY() + 1; y <= maxY; y++) {
         BlockState state = level.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
         if (state.getBlock() == ModBlocks.GREENHOUSE_GLASS.get()) {
            return true;
         }
         if (!isInteriorPassable(state)) {
            return false;
         }
      }
      return false;
   }

   private static boolean isInteriorPassable(Level level, BlockPos pos) {
      return isInteriorPassable(level.getBlockState(pos));
   }

   private static boolean isInteriorPassable(BlockState state) {
      Block block = state.getBlock();
      return state.isAir() || block instanceof ReclamationCropBlock || block == ModBlocks.HYDROPONIC_TRAY.get();
   }

   private static boolean isPollinationTarget(Block block) {
      return block instanceof ReclamationCropBlock || block == ModBlocks.HYDROPONIC_TRAY.get();
   }

   private static boolean inside(BlockPos pos, BlockPos min, BlockPos max) {
      return pos.getX() >= min.getX() && pos.getX() <= max.getX()
         && pos.getY() >= min.getY() && pos.getY() <= max.getY()
         && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
   }

   private static boolean touchesBoundary(BlockPos pos, BlockPos min, BlockPos max) {
      return pos.getX() == min.getX() || pos.getX() == max.getX()
         || pos.getY() == min.getY() || pos.getY() == max.getY()
         || pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
   }

   private static long distanceSquared(BlockPos pos, BlockPos center) {
      long x = pos.getX() - center.getX();
      long y = pos.getY() - center.getY();
      long z = pos.getZ() - center.getZ();
      return x * x + y * y + z * z;
   }

   public record GreenhouseScan(
      int score,
      int supportScore,
      int enclosureScore,
      int glass,
      int filters,
      int activeDocks,
      int idleDocks,
      int controllers,
      int trays,
      int cropTargets,
      boolean enclosed,
      boolean greenhouseRoof,
      boolean floor,
      int interiorVolume
   ) {
      public String enclosureLabel() {
         if (enclosed && greenhouseRoof && floor) {
            return "sealed";
         }
         if (enclosed) {
            return "bounded";
         }
         return "leaking";
      }
   }

   private record EnclosureScan(boolean enclosed, boolean hasGreenhouseRoof, boolean hasFloor, int interiorVolume) {
      private static EnclosureScan open() {
         return new EnclosureScan(false, false, false, 0);
      }
   }

   public static int cropStability(Player player) {
      int total = 0;
      int count = 0;
      for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
         ItemStack stack = player.getInventory().getItem(slot);
         SeedProfile profile = stack.get(ModDataComponents.SEED_PROFILE.get());
         if (profile != null) {
            total += profile.stability();
            count++;
         }
      }
      if (count == 0) {
         return value(player, "crop_stability");
      }
      return Math.max(value(player, "crop_stability"), total / count);
   }

   public static int foodSecurity(Player player) {
      int food = 0;
      for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
         ItemStack stack = player.getInventory().getItem(slot);
         if (stack.is(ModItems.ASH_WHEAT.get()) || stack.is(ModItems.HARDROOT.get()) || stack.is(ModItems.GLOW_BEANS.get())
            || stack.is(ModItems.MUTANT_BERRIES.get()) || stack.is(ModItems.CLEAN_CORN.get())) {
            food += stack.getCount();
         }
      }
      return Math.min(100, knownSeeds(player).size() * ReclamationContent.progression().foodKnownSeedBonus()
         + food * ReclamationContent.progression().foodItemValue());
   }

   public static int count(Player player, Item item) {
      int total = 0;
      for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
         ItemStack stack = player.getInventory().getItem(slot);
         if (stack.is(item)) {
            total += stack.getCount();
         }
      }
      return total;
   }

   public static void syncServerMetric(ServerPlayer player) {
      metrics(player);
      discoverRoutes(player);
   }

   private static void discoverRoutes(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         EchoCoreServices.discoverVisibleRouteRecords(serverPlayer);
      }
   }

   public static String coreMilestoneForFlag(String flag) {
      return switch (flag) {
         case "seed_recovered", "seed_analyzed" -> "recover_seed";
         case "soil_analyzed" -> "analyze_soil";
         case "first_growth" -> "first_growth";
         case "gene_stabilization" -> "gene_stabilization";
         case "greenhouse_online" -> "greenhouse_online";
         case "restore_chunk" -> "restore_chunk";
         case "hydroponics_online" -> "hydroponics_online";
         case "bio_reactor_online" -> "bio_reactor_online";
         case "compost_recycler_online" -> "compost_recycler_online";
         default -> "";
      };
   }

   private static void recordCoreMilestone(Player player, String flag) {
      String milestone = coreMilestoneForFlag(flag);
      if (!milestone.isBlank()) {
         ReclamationCrossAddonIntegration.recordMilestone(player, milestone);
      }
   }
}
