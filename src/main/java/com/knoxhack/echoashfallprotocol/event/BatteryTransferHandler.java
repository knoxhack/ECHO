package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.energy.EnergyAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class BatteryTransferHandler {
    private static final int DIRECT_TRANSFER = 512;

    private BatteryTransferHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isShiftKeyDown()) {
            return;
        }

        ItemStack held = event.getItemStack();
        if (!EnergyAccess.isEnergyItem(held)) {
            return;
        }

        Level level = player.level();
        BlockPos pos = event.getPos();
        EnergyHandler blockEnergy = EnergyAccess.getBlockEnergy(level, pos, event.getFace());
        EnergyHandler itemEnergy = EnergyAccess.getItemEnergy(held);
        if (blockEnergy == null || itemEnergy == null) {
            return;
        }

        int moved = 0;
        if (itemEnergy.getAmountAsLong() < itemEnergy.getCapacityAsLong()) {
            int receivable = EnergyAccess.simulateInsert(itemEnergy, DIRECT_TRANSFER);
            int extracted = EnergyAccess.extract(blockEnergy, receivable);
            moved = EnergyAccess.insert(itemEnergy, extracted);
            if (moved < extracted) {
                EnergyAccess.insert(blockEnergy, extracted - moved);
            }
            if (moved > 0) {
                player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.battery.pull", moved));
            }
        }

        if (moved == 0 && itemEnergy.getAmountAsLong() > 0) {
            int receivable = EnergyAccess.simulateInsert(blockEnergy, DIRECT_TRANSFER);
            int extracted = EnergyAccess.extract(itemEnergy, receivable);
            moved = EnergyAccess.insert(blockEnergy, extracted);
            if (moved < extracted) {
                EnergyAccess.insert(itemEnergy, extracted - moved);
            }
            if (moved > 0) {
                player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.battery.push", moved));
            }
        }

        if (moved > 0) {
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.45F, 1.25F);
            event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
            event.setCanceled(true);
        }
    }
}
