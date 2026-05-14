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
            player.sendSystemMessage(Component.literal("Phase Anchor bound to " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        }
    }

    public static boolean tryUsePhaseAnchor(ServerPlayer player, ItemStack stack) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data == null || data.boundPos().equals(BlockPos.ZERO) || data.boundDimension().isEmpty()) {
            player.sendSystemMessage(Component.literal("Phase Anchor is not bound."));
            return false;
        }
        if (!RelicTechConfig.ALLOW_PHASE_ANCHOR_CROSS_DIMENSION.get()) {
            String currentDim = player.level().dimension().identifier().toString();
            if (!currentDim.equals(data.boundDimension())) {
                player.sendSystemMessage(Component.literal("Cross-dimension recall is disabled."));
                return false;
            }
        }
        if (!consumeNullCharge(player, 1)) {
            player.sendSystemMessage(Component.literal("No Null Charge available."));
            return false;
        }
        BlockPos target = data.boundPos();
        var level = (ServerLevel) player.level();
        if (level.isLoaded(target) && !level.getBlockState(target).isSolid()) {
            player.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            addInstability(player, RelicTechConfig.PHASE_ANCHOR_INSTABILITY_COST.get(), Identifier.fromNamespaceAndPath(MODID, "phase_anchor"));
            return true;
        }
        player.sendSystemMessage(Component.literal("Recall destination is unsafe or unloaded."));
        return false;
    }

    public static final String MODID = "echorelictech";
}
