package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Gas Mask — equippable in the head slot.
 * Provides atmospheric protection inside toxic hazard zones.
 */
public class GasMaskItem extends Item {

    public GasMaskItem(Properties properties) {
        super(properties.durability(500).stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Equip to head slot on right-click
        ItemStack stack = player.getItemInHand(hand);
        ItemStack currentHead = player.getItemBySlot(EquipmentSlot.HEAD);

        if (currentHead.isEmpty()) {
            player.setItemSlot(EquipmentSlot.HEAD, stack.copy());
            stack.shrink(1);
            level.playSound(null, player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        } else {
            // Swap with current head item
            player.setItemSlot(EquipmentSlot.HEAD, stack.copy());
            player.setItemInHand(hand, currentHead);
            level.playSound(null, player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
    }
}
