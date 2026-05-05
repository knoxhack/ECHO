package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.endgame.NexusCampaignActions;
import com.knoxhack.echoashfallprotocol.world.NexusCampaignData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Consumable field stabilizer for the overworld Nexus instability meter.
 */
public class InstabilityDampenerItem extends Item {
    private static final int INSTABILITY_REDUCTION = 20;

    public InstabilityDampenerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        ServerLevel overworld = serverLevel.getServer().overworld();
        NexusCampaignData campaign = NexusCampaignData.get(overworld);
        if (!campaign.isAwakened()) {
            serverPlayer.sendSystemMessage(Component.literal("[ECHO-7] No awakened Nexus instability signal to dampen.")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }

        boolean changed = campaign.reduceInstability(INSTABILITY_REDUCTION);
        if (!changed) {
            serverPlayer.sendSystemMessage(Component.literal("[ECHO-7] Nexus instability already reads zero.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        NexusCampaignActions.syncCampaignState(overworld);
        serverPlayer.sendSystemMessage(Component.literal("[NEXUS] Instability dampened by "
                + INSTABILITY_REDUCTION + "%. Current instability: " + campaign.getInstability() + "%.")
                .withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS;
    }
}
