package com.knoxhack.echorelictech.api;

import com.knoxhack.echorelictech.api.relic.*;
import com.knoxhack.echorelictech.config.RelicTechConfig;
import com.knoxhack.echorelictech.data.RelicDefinitionLoader;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import com.knoxhack.echorelictech.registry.ModItems;
import com.knoxhack.echorelictech.server.RelicFailureManager;
import com.knoxhack.echorelictech.server.RelicInstabilityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

public final class RelicTechApi {
    private RelicTechApi() {}

    public static boolean isRelic(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.has(ModDataComponents.RELIC_DATA.get());
    }

    public static Identifier getRelicId(ItemStack stack) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        return data != null ? data.relicId() : Identifier.fromNamespaceAndPath("echorelictech", "unknown");
    }

    public static RelicCondition getRelicCondition(ItemStack stack) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        return data != null ? data.condition() : RelicCondition.UNKNOWN;
    }

    public static void setRelicCondition(ItemStack stack, RelicCondition condition) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data != null) {
            stack.set(ModDataComponents.RELIC_DATA.get(), data.withCondition(condition));
        }
    }

    public static RelicTier getRelicTier(ItemStack stack) {
        var def = RelicDefinitionLoader.get(getRelicId(stack));
        return def != null ? def.tier() : RelicTier.FIELD;
    }

    public static RelicCategory getRelicCategory(ItemStack stack) {
        var def = RelicDefinitionLoader.get(getRelicId(stack));
        return def != null ? def.category() : RelicCategory.UTILITY;
    }

    public static void identifyRelic(ServerPlayer player, ItemStack stack) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data != null) {
            stack.set(ModDataComponents.RELIC_DATA.get(), data.makeIdentified());
        }
    }

    public static void addInstability(ServerPlayer player, int amount, Identifier source) {
        RelicInstabilityManager.addInstability(player, amount);
    }

    public static int getInstability(ServerPlayer player) {
        return RelicInstabilityManager.getInstability(player);
    }

    public static int getInstabilityLevel(ServerPlayer player) {
        return RelicInstabilityManager.getInstabilityLevel(player);
    }

    public static boolean tryTriggerFailure(ServerPlayer player, ItemStack relic, RelicUseContext context) {
        return RelicFailureManager.tryTrigger(player, relic, context);
    }

    public static boolean isContained(ItemStack stack) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        return data != null && data.containmentFlag();
    }

    public static void markContained(ItemStack stack, boolean contained) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data != null) {
            stack.set(ModDataComponents.RELIC_DATA.get(), new RelicInstanceData(
                data.relicId(), data.condition(), data.instabilityModifier(), data.boundPos(), data.boundDimension(),
                data.charge(), data.corruptionFlag(), data.overclockFlag(), contained, data.identified(), data.cooldownRemaining()
            ));
        }
    }

    public static List<String> getLensScanRows(ItemStack stack) {
        var def = RelicDefinitionLoader.get(getRelicId(stack));
        if (def == null || def.lens().isEmpty()) return List.of();
        var lens = def.lens().get();
        List<String> rows = new java.util.ArrayList<>();
        rows.add(lens.compact());
        rows.addAll(lens.deepScan());
        return rows;
    }

    public static String getTerminalRelicSummary(ServerPlayer player) {
        return "RelicTech terminal integration scaffold.";
    }

    public static void reportRelicUse(ServerPlayer player, Identifier relicId, Collection<RelicRiskType> risks) {
        // No-op scaffold for faction integration
    }

    public static boolean consumeNullCharge(ServerPlayer player, int amount) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.NULL_BATTERY.get())) {
                int charge = stack.getOrDefault(ModDataComponents.NULL_CHARGE.get(), 0);
                if (charge >= amount) {
                    stack.set(ModDataComponents.NULL_CHARGE.get(), charge - amount);
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack findNullBattery(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.NULL_BATTERY.get())) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasUsableNullCharge(ServerPlayer player, int amount) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.NULL_BATTERY.get())) {
                int charge = stack.getOrDefault(ModDataComponents.NULL_CHARGE.get(), 0);
                if (charge >= amount) return true;
            }
        }
        return false;
    }

    public static void bindPhaseAnchor(ServerPlayer player, ItemStack stack, BlockPos pos) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data != null) {
            String dim = player.level().dimension().identifier().toString();
            stack.set(ModDataComponents.RELIC_DATA.get(), data.withBound(pos, dim));
            player.sendSystemMessage(Component.translatable("item.echorelictech.phase_anchor.bound", pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        }
    }

    public static boolean tryUsePhaseAnchor(ServerPlayer player, ItemStack stack) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data == null || data.boundPos().equals(BlockPos.ZERO) || data.boundDimension().isEmpty()) {
            player.sendSystemMessage(Component.translatable("item.echorelictech.phase_anchor.unbound"));
            return false;
        }
        if (!RelicTechConfig.ALLOW_PHASE_ANCHOR_CROSS_DIMENSION.get()) {
            String currentDim = player.level().dimension().identifier().toString();
            if (!currentDim.equals(data.boundDimension())) {
                player.sendSystemMessage(Component.translatable("item.echorelictech.phase_anchor.cross_dim_disabled"));
                return false;
            }
        }
        if (!consumeNullCharge(player, 1)) {
            player.sendSystemMessage(Component.translatable("item.echorelictech.relic.no_null_charge"));
            return false;
        }
        BlockPos target = data.boundPos();
        var level = (ServerLevel) player.level();
        if (!level.isLoaded(target)) {
            player.sendSystemMessage(Component.translatable("item.echorelictech.phase_anchor.unloaded"));
            return false;
        }
        BlockPos safe = findSafeTeleportPos(level, target);
        if (safe != null) {
            player.teleportTo(safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
            addInstability(player, RelicTechConfig.PHASE_ANCHOR_INSTABILITY_COST.get(), Identifier.fromNamespaceAndPath(MODID, "phase_anchor"));
            if (!safe.equals(target)) {
                player.sendSystemMessage(Component.translatable("item.echorelictech.phase_anchor.drift"));
            }
            return true;
        }
        player.sendSystemMessage(Component.translatable("item.echorelictech.phase_anchor.unsafe"));
        return false;
    }

    // Beta integration API methods
    public static java.util.Map<Identifier, Integer> getPlayerRelicSummary(ServerPlayer player) {
        java.util.Map<Identifier, Integer> summary = new java.util.HashMap<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isRelic(stack)) {
                Identifier id = getRelicId(stack);
                summary.merge(id, 1, Integer::sum);
            }
        }
        return summary;
    }

    public static int getAnalyzedRelicCount(ServerPlayer player) {
        var data = getPlayerEchoData(player);
        return data != null ? data.analyzedCount : 0;
    }

    public static int getContainedRelicCount(ServerPlayer player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isContained(stack)) count++;
        }
        return count;
    }

    public static java.util.List<BlockPos> getDiscoveredVaultMarkers(ServerPlayer player) {
        var data = getPlayerEchoData(player);
        return data != null ? java.util.List.copyOf(data.discoveredVaults) : java.util.List.of();
    }

    public static java.util.Map<String, Object> getMachineStatusSnapshot(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        java.util.Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        if (be instanceof com.knoxhack.echorelictech.block.entity.RelicAnalyzerBlockEntity analyzer) {
            snapshot.put("type", "relic_analyzer");
            snapshot.put("has_input", !analyzer.getInput().isEmpty());
            snapshot.put("has_output", analyzer.hasOutput());
            snapshot.put("progress", analyzer.progress());
        } else if (be instanceof com.knoxhack.echorelictech.block.entity.PrototypeWorkbenchBlockEntity wb) {
            snapshot.put("type", "prototype_workbench");
            snapshot.put("has_relic", !wb.getRelicSlot().isEmpty());
        } else if (be instanceof com.knoxhack.echorelictech.block.entity.ContainmentLockerBlockEntity locker) {
            snapshot.put("type", "containment_locker");
            snapshot.put("occupied_slots", locker.getContainerSize() - (locker.isEmpty() ? locker.getContainerSize() : 0));
        } else if (be instanceof com.knoxhack.echorelictech.block.entity.NullBatteryDockBlockEntity dock) {
            snapshot.put("type", "null_battery_dock");
            snapshot.put("has_battery", !dock.getBattery().isEmpty());
            snapshot.put("charge", dock.getBattery().getOrDefault(ModDataComponents.NULL_CHARGE.get(), 0));
        }
        return snapshot;
    }

    private static com.knoxhack.echorelictech.server.RelicPlayerSavedData.PlayerEchoData getPlayerEchoData(ServerPlayer player) {
        var saved = com.knoxhack.echorelictech.server.RelicPlayerSavedData.get((ServerLevel) player.level());
        return saved.get(player.getUUID());
    }

    public static void recordVaultDiscovery(ServerPlayer player, Identifier vaultId, BlockPos pos) {
        var saved = com.knoxhack.echorelictech.server.RelicPlayerSavedData.get((ServerLevel) player.level());
        var data = saved.get(player.getUUID());
        if (!data.discoveredVaults.contains(pos)) {
            data.discoveredVaults.add(pos);
            data.firstVaultDiscovered = true;
            saved.set(player.getUUID(), data);
        }
    }

    public static void recordAnalyzedRelic(ServerPlayer player) {
        var saved = com.knoxhack.echorelictech.server.RelicPlayerSavedData.get((ServerLevel) player.level());
        var data = saved.get(player.getUUID());
        data.analyzedCount++;
        saved.set(player.getUUID(), data);
    }

    public static void recordRelicUse(ServerPlayer player, Identifier relicId) {
        var saved = com.knoxhack.echorelictech.server.RelicPlayerSavedData.get((ServerLevel) player.level());
        var data = saved.get(player.getUUID());
        data.totalUses++;
        data.discoveredRelics.add(relicId.toString());
        saved.set(player.getUUID(), data);
    }

    private static BlockPos findSafeTeleportPos(ServerLevel level, BlockPos origin) {
        if (isSafeTeleportPos(level, origin)) return origin;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (isSafeTeleportPos(level, candidate)) return candidate;
                }
            }
        }
        return null;
    }

    private static boolean isSafeTeleportPos(ServerLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinY() || pos.getY() >= level.getMinY() + level.getHeight()) return false;
        var state = level.getBlockState(pos);
        var below = level.getBlockState(pos.below());
        if (state.isSolid() || state.is(net.minecraft.world.level.block.Blocks.LAVA) || state.is(net.minecraft.world.level.block.Blocks.FIRE)
            || state.is(net.minecraft.world.level.block.Blocks.CACTUS) || state.is(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH)) return false;
        if (below.isAir() || below.is(net.minecraft.world.level.block.Blocks.LAVA) || below.is(net.minecraft.world.level.block.Blocks.FIRE)) return false;
        return true;
    }

    public static final String MODID = "echorelictech";
}
