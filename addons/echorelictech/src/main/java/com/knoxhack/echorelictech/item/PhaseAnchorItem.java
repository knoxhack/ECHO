package com.knoxhack.echorelictech.item;

import com.knoxhack.echorelictech.api.RelicTechApi;
import com.knoxhack.echorelictech.api.relic.RelicCondition;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import com.knoxhack.echorelictech.config.RelicTechConfig;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class PhaseAnchorItem extends Item {
    public PhaseAnchorItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data == null) {
            stack.set(ModDataComponents.RELIC_DATA.get(), new RelicInstanceData(
                Identifier.fromNamespaceAndPath("echorelictech", "phase_anchor"),
                RelicCondition.DAMAGED, 0, BlockPos.ZERO, "", 0, false, false, false, false, 0));
            return InteractionResult.SUCCESS;
        }

        if (!data.identified()) {
            serverPlayer.sendSystemMessage(Component.literal("This relic is unidentified. Analyze it first."));
            return InteractionResult.FAIL;
        }

        if (data.cooldownRemaining() > 0) {
            serverPlayer.sendSystemMessage(Component.literal("Phase Anchor is on cooldown."));
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            RelicTechApi.bindPhaseAnchor(serverPlayer, stack, serverPlayer.blockPosition());
            stack.set(ModDataComponents.RELIC_DATA.get(), data.withCooldown(RelicTechConfig.PHASE_ANCHOR_COOLDOWN_TICKS.get()));
            return InteractionResult.SUCCESS;
        }

        if (RelicTechApi.tryUsePhaseAnchor(serverPlayer, stack)) {
            stack.set(ModDataComponents.RELIC_DATA.get(), data.withCooldown(RelicTechConfig.PHASE_ANCHOR_COOLDOWN_TICKS.get()));
            RelicTechApi.tryTriggerFailure(serverPlayer, stack, new com.knoxhack.echorelictech.api.relic.RelicUseContext(level, player, stack, player.blockPosition(), false));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data == null) {
            tooltip.accept(Component.literal("Unidentified pre-Gridfall device"));
            return;
        }
        tooltip.accept(Component.literal("Condition: " + data.condition()));
        tooltip.accept(Component.literal(data.boundPos().equals(BlockPos.ZERO) ? "Status: Unbound" : "Bound to " + data.boundPos().toShortString()));
        tooltip.accept(Component.literal("Instability Cost: +" + RelicTechConfig.PHASE_ANCHOR_INSTABILITY_COST.get()));
        if (data.cooldownRemaining() > 0) tooltip.accept(Component.literal("Cooldown: " + data.cooldownRemaining() + " ticks"));
        tooltip.accept(Component.literal("Warning: Destination drift possible."));
    }
}
