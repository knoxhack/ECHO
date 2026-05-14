package com.knoxhack.echorelictech.item;

import com.knoxhack.echorelictech.api.RelicTechApi;
import com.knoxhack.echorelictech.api.relic.RelicCondition;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import com.knoxhack.echorelictech.config.RelicTechConfig;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import com.knoxhack.echorelictech.server.RelicInstabilityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class MatterStitcherItem extends Item {
    public MatterStitcherItem(Properties props) {
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
                Identifier.fromNamespaceAndPath("echorelictech", "matter_stitcher"),
                RelicCondition.DAMAGED, 0, BlockPos.ZERO, "", 0, false, false, false, false, 0));
            return InteractionResult.SUCCESS;
        }

        if (!data.identified()) {
            serverPlayer.sendSystemMessage(Component.literal("This relic is unidentified. Analyze it first."));
            return InteractionResult.FAIL;
        }

        if (!RelicTechApi.consumeNullCharge(serverPlayer, 1)) {
            serverPlayer.sendSystemMessage(Component.literal("No Null Charge available."));
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!armor.isEmpty() && armor.isDamaged()) {
                    armor.setDamageValue(Math.max(0, armor.getDamageValue() - 20));
                }
            }
            serverPlayer.sendSystemMessage(Component.literal("Matter Stitcher // Armor integrity partially restored."));
        } else {
            player.heal(4.0f);
            serverPlayer.sendSystemMessage(Component.literal("Matter Stitcher // Biological matter stabilized."));
        }

        RelicInstabilityManager.addInstability(serverPlayer, 8);
        if (RelicInstabilityManager.getInstabilityLevel(serverPlayer) >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0));
        }
        RelicTechApi.tryTriggerFailure(serverPlayer, stack, new com.knoxhack.echorelictech.api.relic.RelicUseContext(level, player, stack, player.blockPosition(), player.isShiftKeyDown()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Sneak-use: repair armor. Normal use: heal."));
        tooltip.accept(Component.literal("Consumes Null Charge."));
        tooltip.accept(Component.literal("Risk: Side effects at high instability."));
    }
}
