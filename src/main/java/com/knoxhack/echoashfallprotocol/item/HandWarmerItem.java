package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.survival.ColdData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Consumable emergency heat source for cryogenic exploration.
 */
public class HandWarmerItem extends Item {

    public HandWarmerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        ColdData coldData = ColdData.get(player);
        coldData.addTemperature(25);
        player.setData(com.knoxhack.echoashfallprotocol.registry.ModAttachments.COLD_DATA.get(), coldData);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            QuestData quest = QuestData.get(serverPlayer);
            quest.visitLocation("special", "cold:warmed_up");
            QuestData.saveAndSync(serverPlayer, quest);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.sendSystemMessage(Component.literal("Hand warmer activated. Body temperature rising.")
            .withStyle(ChatFormatting.AQUA));
        return InteractionResult.CONSUME;
    }
}
