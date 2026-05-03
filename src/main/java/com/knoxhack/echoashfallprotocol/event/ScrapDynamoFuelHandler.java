package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.entity.ScrapDynamoBlockEntity;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class ScrapDynamoFuelHandler {
    private ScrapDynamoFuelHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Level level = player.level();
        if (!level.getBlockState(event.getPos()).is(ModBlocks.SCRAP_DYNAMO.get())
                || !(level.getBlockEntity(event.getPos()) instanceof ScrapDynamoBlockEntity dynamo)) {
            return;
        }

        ItemStack held = event.getItemStack();
        if (held.isEmpty() || !dynamo.isFuel(held)) {
            return;
        }

        dynamo.addFuel(held);
        player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.scrap_dynamo.fueled"));
        level.playSound(null, event.getPos(), SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 1.15F);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }
}
