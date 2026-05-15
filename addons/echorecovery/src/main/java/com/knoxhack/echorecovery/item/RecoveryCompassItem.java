package com.knoxhack.echorecovery.item;

import com.knoxhack.echorecovery.config.RecoveryConfig;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class RecoveryCompassItem extends Item {
    public RecoveryCompassItem(Properties properties) {
        super(properties);
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!RecoveryConfig.RECOVERY_COMPASS_ENABLED.get()) {
            return;
        }
        // Compass updates handled client-side via tooltip/sync if needed
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Component.literal("Points to your nearest grave."));
        if (RecoveryConfig.RECOVERY_COMPASS_WORKS_CROSS_DIMENSION.get()) {
            tooltip.accept(Component.literal("Cross-dimensional tracking enabled."));
        }
    }
}
