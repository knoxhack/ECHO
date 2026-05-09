package com.knoxhack.echologisticsnetwork.service;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echologisticsnetwork.block.LogisticsBlock;
import com.knoxhack.echologisticsnetwork.block.LogisticsBlock.LogisticsKind;
import com.knoxhack.echologisticsnetwork.block.entity.LogisticsBlockEntity;
import com.knoxhack.echologisticsnetwork.content.FactionDepotOffer;
import com.knoxhack.echologisticsnetwork.content.LoadoutPreset;
import com.knoxhack.echologisticsnetwork.content.LoadoutRequirement;
import com.knoxhack.echologisticsnetwork.content.LogisticsContent;
import com.knoxhack.echologisticsnetwork.content.SupplyCategory;
import com.knoxhack.echologisticsnetwork.entity.CourierDroneEntity;
import com.knoxhack.echologisticsnetwork.registry.ModEntities;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;

public final class LogisticsNetworkService {
   private static final int BASE_RADIUS = 24;
   private static final int INDUSTRIAL_RADIUS = 36;
   private static final int INDUSTRIAL_DUCT_LIMIT = 96;

   private LogisticsNetworkService() {
   }

   public static LogisticsSnapshot snapshot(Level level, BlockPos origin, String networkId, Player player) {
      if (level == null || origin == null) {
         return LogisticsSnapshot.empty(networkId);
      }
      List<StorageNode> nodes = storageNodes(level, origin, networkId);
      List<StockRow> stockRows = new ArrayList<>();
      List<MissingRow> missingRows = new ArrayList<>();
      for (SupplyCategory category : LogisticsContent.categories()) {
         int count = countCategory(nodes, category);
         stockRows.add(new StockRow(category.id(), category.title(), count, category.lowStockTarget(), category.accentColor()));
         if (count < category.lowStockTarget()) {
            missingRows.add(new MissingRow(category.id(), category.title(), category.lowStockTarget() - count, category.accentColor()));
         }
      }
      List<LoadoutReadiness> readiness = LogisticsContent.loadouts().stream()
         .map(loadout -> readiness(nodes, loadout))
         .toList();
      List<DeliveryJob> deliveryJobs = activeDeliveryJobs(level, origin, networkId);
      return new LogisticsSnapshot(networkId == null ? "global" : networkId, stockRows, missingRows, readiness, deliveryJobs.size(), deliveryJobs, LogisticsContent.offers());
   }

   public static boolean requestLoadout(Player player, BlockPos origin, BlockPos targetPos, String presetId) {
      if (player == null || !(player.level() instanceof ServerLevel level)) {
         return false;
      }
      LoadoutPreset preset = LogisticsContent.loadout(presetId).orElse(null);
      if (preset == null) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Unknown loadout preset: " + presetId + ". Rebind the Loadout Card or reload datapacks."));
         return false;
      }
      BlockPos requestOrigin = origin == null ? player.blockPosition() : origin;
      BlockPos deliveryTarget = targetPos == null ? requestOrigin : targetPos;
      if (!targetBlockTypeAllowed(level, deliveryTarget, preset)) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Loadout target rejected by preset rules. Allowed targets: " + targetBlockList(preset) + "."));
         return false;
      }
      String networkId = networkIdAt(level, requestOrigin);
      List<StorageNode> nodes = storageNodes(level, requestOrigin, networkId);
      ExtractionPlan plan = planLoadout(nodes, preset);
      if (!plan.ready()) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Loadout blocked. Missing " + plan.missingRequired() + " required supply item(s). Scan Logistics for low-stock rows."));
         return false;
      }
      LogisticsBlockEntity dock = nearestDock(level, requestOrigin, networkId);
      if (dock == null) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // No Drone Delivery Dock online for network " + networkId + ". Place or manifest a dock on the same network."));
         return false;
      }
      if (!canAcceptPayload(level, deliveryTarget, plan.payload())) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Target inventory cannot accept the full sealed payload. Clear space before dispatch."));
         return false;
      }
      List<ItemStack> payload = extractPlannedLoadout(plan);
      if (payload.isEmpty()) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Payload extraction failed; source storage was left unchanged."));
         return false;
      }
      CourierDroneEntity drone = ModEntities.COURIER_DRONE.get().create(level, EntitySpawnReason.EVENT);
      if (drone == null) {
         recoverPayload(level, dock.getBlockPos(), payload);
         return false;
      }
      UUID jobId = UUID.randomUUID();
      BlockPos dockPos = dock.getBlockPos();
      drone.configureDelivery(jobId, player.getUUID(), networkId, dockPos, deliveryTarget, preset.id(), payload, preset.deliveryTicks());
      drone.setPos(dockPos.getX() + 0.5D, dockPos.getY() + 1.35D, dockPos.getZ() + 0.5D);
      if (!level.addFreshEntity(drone)) {
         recoverPayload(level, dockPos, payload);
         return false;
      }
      recordDeliveryStatus(level, dockPos, deliveryTarget, "Delivery " + shortJob(jobId) + " dispatched: " + preset.title());
      player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Courier dispatched: " + preset.title()
         + " [" + shortJob(jobId) + "] | ETA " + preset.deliveryTicks() + "t."));
      if (player instanceof ServerPlayer serverPlayer) {
         EchoCoreServices.discoverVisibleRouteRecords(serverPlayer);
      }
      return true;
   }

   public static int pendingRelayRewards(Player player) {
      return player == null ? 0 : EchoCoreServices.pendingTerminalRewardCount(player);
   }

   public static boolean claimRelayRewards(ServerPlayer player, LogisticsBlockEntity relay) {
      if (player == null) {
         return false;
      }
      int pending = pendingRelayRewards(player);
      if (pending <= 0) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // No pending terminal rewards are waiting at the relay."));
         if (relay != null) {
            relay.refreshSnapshot(player);
         }
         return false;
      }
      if (relay == null || relay.kind() != LogisticsKind.REMOTE_REWARD_RELAY) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Remote Reward Relay offline. Place a relay in this logistics network before claiming " + pending + " pending reward item(s)."));
         if (relay != null) {
            relay.refreshSnapshot(player);
         }
         return false;
      }
      boolean claimed = EchoCoreServices.claimTerminalRewards(player);
      player.sendSystemMessage(net.minecraft.network.chat.Component.literal(claimed
         ? "ECHO LOGISTICS // Remote Reward Relay transferred pending rewards into your inventory."
         : "ECHO LOGISTICS // No pending terminal rewards are waiting at the relay."));
      if (relay != null) {
         relay.refreshSnapshot(player);
      }
      if (claimed) {
         EchoCoreServices.discoverVisibleRouteRecords(player);
      }
      return claimed;
   }

   public static boolean performDepotExchange(Player player, LogisticsBlockEntity depot) {
      if (player == null || depot == null) {
         return false;
      }
      if (depot.kind() != LogisticsKind.FACTION_TRADE_DEPOT) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Depot exchange requires a Faction Trade Depot."));
         return false;
      }
      if (depot.cooldownTicks() > 0) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Depot offer cooling down for " + ticks(depot.cooldownTicks()) + "."));
         return false;
      }
      List<FactionDepotOffer> offers = selectedDepotOffers(depot);
      if (offers.isEmpty()) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Selected depot offer is no longer loaded. Cycle offers or reload datapacks."));
         return false;
      }
      for (FactionDepotOffer offer : offers) {
         int reputation = EchoCoreServices.factionProfile(player, offer.factionId()).map(profile -> profile.reputation()).orElse(0);
         if (reputation < offer.minReputation()) {
            continue;
         }
         if (!removeStack(depot, offer.input())) {
            continue;
         }
         if (!insertStack(depot, offer.output().copy())) {
            player.drop(offer.output().copy(), false);
         }
         EchoCoreServices.addFactionReputation(player, offer.factionId(), offer.reputationDelta());
         depot.setDepotOfferId(offer.id().toString());
         depot.setCooldownTicks(offer.cooldownTicks());
         depot.recordManifest("Depot offer completed: " + offer.id().getPath());
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // Depot offer completed: "
            + offer.id().getPath().replace('_', ' ')
            + " | reputation " + signed(offer.reputationDelta())
            + " | cooldown " + ticks(offer.cooldownTicks()) + "."));
         depot.refreshSnapshot(player);
         return true;
      }
      player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO LOGISTICS // No available depot offer matched the stored goods and reputation."));
      return false;
   }

   public static boolean deliverPayload(Level level, BlockPos targetPos, List<ItemStack> payload) {
      if (level == null || targetPos == null || payload == null || payload.isEmpty()) {
         return false;
      }
      BlockEntity blockEntity = level.getBlockEntity(targetPos);
      if (blockEntity instanceof Container container) {
         return insertPayloadIntoContainer(container, payload);
      }
      return false;
   }

   public static boolean canAcceptPayload(Level level, BlockPos targetPos, List<ItemStack> payload) {
      if (level == null || targetPos == null || payload == null || payload.isEmpty()) {
         return false;
      }
      return level.getBlockEntity(targetPos) instanceof Container container && canInsertAll(container, payload);
   }

   public static int cancelActiveDeliveries(Player player, BlockPos origin, String networkId) {
      if (player == null || origin == null || !(player.level() instanceof ServerLevel level)) {
         return 0;
      }
      int cancelled = 0;
      for (CourierDroneEntity drone : level.getEntitiesOfClass(CourierDroneEntity.class, new AABB(origin).inflate(64.0D))) {
         DeliveryJob job = drone.deliveryJob();
         if (!networkMatches(networkId, drone.networkId())) {
            continue;
         }
         if (job.owner() != null && !job.owner().equals(player.getUUID())) {
            continue;
         }
         if (drone.cancelToDock("cancelled by terminal")) {
            cancelled++;
         }
      }
      return cancelled;
   }

   private static boolean targetBlockTypeAllowed(Level level, BlockPos targetPos, LoadoutPreset preset) {
      if (preset.targetBlockTypes().isEmpty()) {
         return true;
      }
      Identifier targetId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(targetPos).getBlock());
      return preset.targetBlockTypes().contains(targetId);
   }

   private static String targetBlockList(LoadoutPreset preset) {
      return preset.targetBlockTypes().isEmpty()
         ? "any inventory"
         : String.join(", ", preset.targetBlockTypes().stream().map(Identifier::toString).toList());
   }

   public static boolean insertPayloadIntoContainer(Container container, List<ItemStack> payload) {
      if (container == null || payload == null || payload.isEmpty()) {
         return false;
      }
      Optional<List<ItemStack>> simulated = simulateInserted(container, payload);
      if (simulated.isEmpty()) {
         return false;
      }
      List<ItemStack> slots = simulated.get();
      for (int slot = 0; slot < Math.min(container.getContainerSize(), slots.size()); slot++) {
         container.setItem(slot, slots.get(slot).copy());
      }
      container.setChanged();
      return true;
   }

   public static void recordDeliveryStatus(Level level, BlockPos sourceDock, BlockPos targetPos, String manifest) {
      if (level == null || manifest == null || manifest.isBlank()) {
         return;
      }
      if (sourceDock != null && level.getBlockEntity(sourceDock) instanceof LogisticsBlockEntity dock) {
         dock.recordManifest(manifest);
      }
      if (targetPos != null && !targetPos.equals(sourceDock) && level.getBlockEntity(targetPos) instanceof LogisticsBlockEntity target) {
         target.recordManifest(manifest);
      }
   }

   public static void returnPayload(Level level, BlockPos pos, List<ItemStack> payload) {
      if (level == null || pos == null || payload == null) {
         return;
      }
      for (ItemStack stack : payload) {
         if (!stack.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack.copy());
            level.addFreshEntity(item);
         }
      }
   }

   public static void recoverPayload(Level level, BlockPos sourceDock, List<ItemStack> payload) {
      if (!deliverPayload(level, sourceDock, payload)) {
         returnPayload(level, sourceDock, payload);
      }
   }

   private static List<DeliveryJob> activeDeliveryJobs(Level level, BlockPos origin, String networkId) {
      return level.getEntitiesOfClass(CourierDroneEntity.class, new AABB(origin).inflate(64.0D)).stream()
         .filter(drone -> networkMatches(networkId, drone.networkId()))
         .map(CourierDroneEntity::deliveryJob)
         .sorted(Comparator.comparing(DeliveryJob::createdTick).thenComparing(job -> job.id().toString()))
         .toList();
   }

   private static boolean canInsertAll(Container container, List<ItemStack> payload) {
      return simulateInserted(container, payload).isPresent();
   }

   private static Optional<List<ItemStack>> simulateInserted(Container container, List<ItemStack> payload) {
      List<ItemStack> slots = new ArrayList<>();
      for (int slot = 0; slot < container.getContainerSize(); slot++) {
         slots.add(container.getItem(slot).copy());
      }
      for (ItemStack stack : payload) {
         ItemStack remainder = stack.copy();
         for (int slot = 0; slot < slots.size() && !remainder.isEmpty(); slot++) {
            ItemStack existing = slots.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remainder) && container.canPlaceItem(slot, remainder)) {
               int limit = Math.min(existing.getMaxStackSize(), container.getMaxStackSize());
               int moved = existing.getCount() >= limit ? 0 : Math.min(remainder.getCount(), limit - existing.getCount());
               if (moved > 0) {
                  existing.grow(moved);
                  remainder.shrink(moved);
               }
            }
         }
         for (int slot = 0; slot < slots.size() && !remainder.isEmpty(); slot++) {
            if (!slots.get(slot).isEmpty() || !container.canPlaceItem(slot, remainder)) {
               continue;
            }
            int moved = Math.min(remainder.getCount(), Math.min(remainder.getMaxStackSize(), container.getMaxStackSize()));
            slots.set(slot, remainder.copyWithCount(moved));
            remainder.shrink(moved);
         }
         if (!remainder.isEmpty()) {
            return Optional.empty();
         }
      }
      return Optional.of(slots);
   }

   private static List<StorageNode> storageNodes(Level level, BlockPos origin, String networkId) {
      int radius = ModList.get().isLoaded("echoindustrialnexus") ? INDUSTRIAL_RADIUS : BASE_RADIUS;
      Map<BlockPos, StorageNode> nodes = new LinkedHashMap<>();
      List<LogisticsBlockEntity> logisticsBlocks = new ArrayList<>();
      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
         BlockEntity blockEntity = level.getBlockEntity(pos);
         if (blockEntity instanceof LogisticsBlockEntity logistics) {
            if (!networkMatches(networkId, logistics.networkId())) {
               continue;
            }
            logisticsBlocks.add(logistics);
            if (logistics.kind() == LogisticsKind.SMART_STORAGE_LABEL) {
               for (Direction direction : Direction.values()) {
                  BlockPos adjacent = pos.relative(direction);
                  if (level.getBlockEntity(adjacent) instanceof Container container && !(container instanceof LogisticsBlockEntity)) {
                     nodes.putIfAbsent(adjacent, new StorageNode(container, adjacent, logistics.categoryId()));
                  }
               }
            } else if (logistics.storageNode()) {
               nodes.putIfAbsent(pos, new StorageNode(logistics, pos, logistics.categoryId()));
            }
         }
      }
      if (ModList.get().isLoaded("echoindustrialnexus")) {
         addIndustrialDuctNodes(level, logisticsBlocks, nodes);
      }
      return nodes.values().stream().sorted(Comparator.comparing(node -> node.pos().toShortString())).toList();
   }

   private static void addIndustrialDuctNodes(Level level, List<LogisticsBlockEntity> logisticsBlocks, Map<BlockPos, StorageNode> nodes) {
      if (level == null || logisticsBlocks.isEmpty()) {
         return;
      }
      Set<BlockPos> seedDucts = new LinkedHashSet<>();
      for (LogisticsBlockEntity logistics : logisticsBlocks) {
         collectAdjacentIndustrialItemDucts(level, logistics.getBlockPos(), seedDucts);
      }
      for (BlockPos storagePos : List.copyOf(nodes.keySet())) {
         collectAdjacentIndustrialItemDucts(level, storagePos, seedDucts);
      }
      if (seedDucts.isEmpty()) {
         return;
      }
      Set<BlockPos> visited = new LinkedHashSet<>();
      Queue<BlockPos> queue = new ArrayDeque<>(seedDucts);
      while (!queue.isEmpty() && visited.size() < INDUSTRIAL_DUCT_LIMIT) {
         BlockPos ductPos = queue.remove().immutable();
         if (!visited.add(ductPos) || !isIndustrialItemDuct(level, ductPos)) {
            continue;
         }
         for (Direction direction : Direction.values()) {
            BlockPos neighbor = ductPos.relative(direction);
            if (isIndustrialItemDuct(level, neighbor)) {
               if (!visited.contains(neighbor)) {
                  queue.add(neighbor.immutable());
               }
               continue;
            }
            if (level.getBlockEntity(neighbor) instanceof Container container && !(container instanceof LogisticsBlockEntity)) {
               nodes.putIfAbsent(neighbor.immutable(), new StorageNode(container, neighbor.immutable(), ""));
            }
         }
      }
   }

   private static void collectAdjacentIndustrialItemDucts(Level level, BlockPos pos, Set<BlockPos> ductPositions) {
      if (pos == null || ductPositions == null) {
         return;
      }
      for (Direction direction : Direction.values()) {
         BlockPos adjacent = pos.relative(direction);
         if (isIndustrialItemDuct(level, adjacent)) {
            ductPositions.add(adjacent.immutable());
         }
      }
   }

   private static boolean isIndustrialItemDuct(Level level, BlockPos pos) {
      if (level == null || pos == null) {
         return false;
      }
      Identifier id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
      return "echoindustrialnexus".equals(id.getNamespace()) && id.getPath().endsWith("_duct") && !id.getPath().contains("flux");
   }

   private static LogisticsBlockEntity nearestDock(ServerLevel level, BlockPos origin, String networkId) {
      LogisticsBlockEntity best = null;
      double bestDistance = Double.MAX_VALUE;
      int radius = ModList.get().isLoaded("echoindustrialnexus") ? INDUSTRIAL_RADIUS : BASE_RADIUS;
      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
         if (level.getBlockEntity(pos) instanceof LogisticsBlockEntity logistics
            && logistics.kind() == LogisticsKind.DRONE_DELIVERY_DOCK
            && networkMatches(networkId, logistics.networkId())) {
            double distance = pos.distSqr(origin);
            if (distance < bestDistance) {
               best = logistics;
               bestDistance = distance;
            }
         }
      }
      return best;
   }

   private static List<FactionDepotOffer> selectedDepotOffers(LogisticsBlockEntity depot) {
      String selected = depot.depotOfferId();
      List<FactionDepotOffer> offers = LogisticsContent.offers();
      if (selected.isBlank()) {
         return offers;
      }
      return offers.stream().filter(offer -> offer.id().toString().equals(selected)).toList();
   }

   private static String networkIdAt(Level level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof LogisticsBlockEntity logistics ? logistics.networkId() : "global";
   }

   private static boolean networkMatches(String expected, String actual) {
      String a = expected == null || expected.isBlank() ? "global" : expected;
      String b = actual == null || actual.isBlank() ? "global" : actual;
      return a.equals(b);
   }

   private static int countCategory(List<StorageNode> nodes, SupplyCategory category) {
      int count = 0;
      for (StorageNode node : nodes) {
         for (int slot = 0; slot < node.container().getContainerSize(); slot++) {
            ItemStack stack = node.container().getItem(slot);
            if (category.matches(stack) && nodeAllows(node, category.id())) {
               count += stack.getCount();
            }
         }
      }
      return count;
   }

   private static LoadoutReadiness readiness(List<StorageNode> nodes, LoadoutPreset preset) {
      int required = 0;
      for (LoadoutRequirement requirement : preset.requirements()) {
         if (!requirement.optional()) {
            required += requirement.count();
         }
      }
      int missing = planLoadout(nodes, preset).missingRequired();
      return new LoadoutReadiness(preset.id(), preset.title(), missing <= 0, required, missing);
   }

   private static ExtractionPlan planLoadout(List<StorageNode> nodes, LoadoutPreset preset) {
      List<ItemStack> payload = new ArrayList<>();
      List<ExtractionMove> moves = new ArrayList<>();
      Map<SlotKey, ItemStack> simulated = new LinkedHashMap<>();
      int missingRequired = 0;
      for (LoadoutRequirement requirement : preset.requirements()) {
         int remaining = requirement.count();
         for (StorageNode node : nodes) {
            for (int slot = 0; slot < node.container().getContainerSize() && remaining > 0; slot++) {
               SlotKey key = new SlotKey(node, slot);
               ItemStack stack = simulated.get(key);
               if (stack == null) {
                  stack = node.container().getItem(slot).copy();
                  simulated.put(key, stack);
               }
               if (stack.isEmpty() || !requirement.matches(stack) || !nodeAllows(node, requirement)) {
                  continue;
               }
               int moved = Math.min(remaining, stack.getCount());
               ItemStack extracted = stack.copyWithCount(moved);
               mergePayload(payload, extracted.copy());
               moves.add(new ExtractionMove(node, slot, extracted.copy(), moved));
               stack.shrink(moved);
               if (stack.isEmpty()) {
                  simulated.put(key, ItemStack.EMPTY);
               }
               remaining -= moved;
            }
         }
         if (remaining > 0 && !requirement.optional()) {
            missingRequired += remaining;
         }
      }
      if (missingRequired > 0) {
         return new ExtractionPlan(List.of(), List.of(), missingRequired);
      }
      return new ExtractionPlan(payload, moves, 0);
   }

   private static List<ItemStack> extractPlannedLoadout(ExtractionPlan plan) {
      if (!plan.ready() || plan.moves().isEmpty()) {
         return List.of();
      }
      List<ExtractionMove> applied = new ArrayList<>();
      for (ExtractionMove move : plan.moves()) {
         Container container = move.node().container();
         ItemStack current = container.getItem(move.slot());
         if (!ItemStack.isSameItemSameComponents(current, move.stack()) || current.getCount() < move.count()) {
            rollback(applied);
            return List.of();
         }
         current.shrink(move.count());
         if (current.isEmpty()) {
            container.setItem(move.slot(), ItemStack.EMPTY);
         }
         container.setChanged();
         applied.add(move);
      }
      return plan.payload().stream().map(ItemStack::copy).toList();
   }

   private static void rollback(List<ExtractionMove> applied) {
      for (int i = applied.size() - 1; i >= 0; i--) {
         ExtractionMove move = applied.get(i);
         Container container = move.node().container();
         ItemStack current = container.getItem(move.slot());
         if (current.isEmpty()) {
            container.setItem(move.slot(), move.stack().copyWithCount(move.count()));
         } else if (ItemStack.isSameItemSameComponents(current, move.stack())) {
            current.grow(move.count());
         } else {
            insertPayloadIntoContainer(container, List.of(move.stack().copyWithCount(move.count())));
         }
         container.setChanged();
      }
   }

   private static boolean nodeAllows(StorageNode node, LoadoutRequirement requirement) {
      if (node.categoryId().isBlank()) {
         return true;
      }
      return requirement.kind() != LoadoutRequirement.Kind.CATEGORY || requirement.target().toString().equals(node.categoryId());
   }

   private static boolean nodeAllows(StorageNode node, Identifier categoryId) {
      return node.categoryId().isBlank() || node.categoryId().equals(categoryId.toString());
   }

   private static void mergePayload(List<ItemStack> payload, ItemStack addition) {
      for (ItemStack existing : payload) {
         if (ItemStack.isSameItemSameComponents(existing, addition) && existing.getCount() < existing.getMaxStackSize()) {
            int moved = Math.min(addition.getCount(), existing.getMaxStackSize() - existing.getCount());
            existing.grow(moved);
            addition.shrink(moved);
         }
      }
      if (!addition.isEmpty()) {
         payload.add(addition.copy());
      }
   }

   private static boolean removeStack(Container container, ItemStack wanted) {
      if (wanted.isEmpty()) {
         return true;
      }
      int available = 0;
      for (int slot = 0; slot < container.getContainerSize(); slot++) {
         ItemStack stack = container.getItem(slot);
         if (ItemStack.isSameItemSameComponents(stack, wanted)) {
            available += stack.getCount();
         }
      }
      if (available < wanted.getCount()) {
         return false;
      }
      int remaining = wanted.getCount();
      for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
         ItemStack stack = container.getItem(slot);
         if (!ItemStack.isSameItemSameComponents(stack, wanted)) {
            continue;
         }
         int removed = Math.min(remaining, stack.getCount());
         stack.shrink(removed);
         if (stack.isEmpty()) {
            container.setItem(slot, ItemStack.EMPTY);
         }
         remaining -= removed;
      }
      container.setChanged();
      return true;
   }

   private static boolean insertStack(Container container, ItemStack stack) {
      return insertPayloadIntoContainer(container, List.of(stack));
   }

   private static String shortJob(UUID jobId) {
      return jobId.toString().substring(0, 8);
   }

   private static String ticks(int ticks) {
      int safeTicks = Math.max(0, ticks);
      if (safeTicks < 20) {
         return safeTicks + "t";
      }
      int seconds = Math.round(safeTicks / 20.0F);
      return safeTicks + "t (~" + seconds + "s)";
   }

   private static String signed(int value) {
      return value > 0 ? "+" + value : String.valueOf(value);
   }

   private record StorageNode(Container container, BlockPos pos, String categoryId) {
      private StorageNode {
         categoryId = categoryId == null ? "" : categoryId;
      }
   }

   private record SlotKey(StorageNode node, int slot) {
   }

   private record ExtractionMove(StorageNode node, int slot, ItemStack stack, int count) {
      private ExtractionMove {
         stack = stack.copyWithCount(count);
      }
   }

   private record ExtractionPlan(List<ItemStack> payload, List<ExtractionMove> moves, int missingRequired) {
      private ExtractionPlan {
         payload = payload == null ? List.of() : payload.stream().map(ItemStack::copy).toList();
         moves = List.copyOf(moves == null ? List.of() : moves);
         missingRequired = Math.max(0, missingRequired);
      }

      private boolean ready() {
         return missingRequired <= 0 && !payload.isEmpty();
      }
   }

   public record StockRow(Identifier categoryId, String title, int count, int lowStockTarget, int accentColor) {
   }

   public record MissingRow(Identifier categoryId, String title, int missing, int accentColor) {
   }

   public record LoadoutReadiness(Identifier presetId, String title, boolean ready, int requiredCount, int missingCount) {
   }

   public record DeliveryJob(UUID id, UUID owner, BlockPos sourceDock, BlockPos targetPos, Identifier presetId, List<ItemStack> payload, String status, long createdTick, long etaTick) {
      public DeliveryJob {
         payload = payload == null ? List.of() : payload.stream().map(ItemStack::copy).toList();
      }
   }

   public record LogisticsSnapshot(
      String networkId,
      List<StockRow> stockRows,
      List<MissingRow> missingRows,
      List<LoadoutReadiness> loadoutReadiness,
      int activeDeliveries,
      List<DeliveryJob> deliveryJobs,
      List<FactionDepotOffer> depotOffers
   ) {
      public LogisticsSnapshot(String networkId, List<StockRow> stockRows, List<MissingRow> missingRows, List<LoadoutReadiness> loadoutReadiness, int activeDeliveries, List<FactionDepotOffer> depotOffers) {
         this(networkId, stockRows, missingRows, loadoutReadiness, activeDeliveries, List.of(), depotOffers);
      }

      public LogisticsSnapshot {
         networkId = networkId == null || networkId.isBlank() ? "global" : networkId;
         stockRows = List.copyOf(stockRows == null ? List.of() : stockRows);
         missingRows = List.copyOf(missingRows == null ? List.of() : missingRows);
         loadoutReadiness = List.copyOf(loadoutReadiness == null ? List.of() : loadoutReadiness);
         deliveryJobs = List.copyOf(deliveryJobs == null ? List.of() : deliveryJobs);
         depotOffers = List.copyOf(depotOffers == null ? List.of() : depotOffers);
      }

      public static LogisticsSnapshot empty(String networkId) {
         return new LogisticsSnapshot(networkId, List.of(), List.of(), List.of(), 0, List.of(), List.of());
      }
   }
}
