package com.knoxhack.echorelictech.block;

import com.knoxhack.echorelictech.block.entity.NullBatteryDockBlockEntity;
import com.knoxhack.echorelictech.config.RelicTechConfig;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import com.knoxhack.echorelictech.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class NullBatteryDockBlock extends Block implements EntityBlock {
    public NullBatteryDockBlock(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (stack.is(ModItems.NULL_BATTERY.get())) {
            int charge = stack.getOrDefault(ModDataComponents.NULL_CHARGE.get(), 0);
            int max = RelicTechConfig.NULL_BATTERY_MAX_CHARGE.get();
            if (charge < max && player.getInventory().contains(new ItemStack(ModItems.NULL_CELL.get()))) {
                consumeCell(player);
                stack.set(ModDataComponents.NULL_CHARGE.get(), Math.min(max, charge + 2));
                player.sendSystemMessage(Component.literal("Null Battery Dock // Charge increased to " + stack.getOrDefault(ModDataComponents.NULL_CHARGE.get(), 0) + "/" + max));
            } else if (charge >= max) {
                player.sendSystemMessage(Component.literal("Null Battery Dock // Battery already full."));
            } else {
                player.sendSystemMessage(Component.literal("Null Battery Dock // Requires Null Cell to charge."));
            }
            return InteractionResult.SUCCESS;
        }
        player.sendSystemMessage(Component.literal("Null Battery Dock // Insert Null Battery to charge."));
        return InteractionResult.SUCCESS;
    }

    private void consumeCell(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ModItems.NULL_CELL.get())) {
                s.shrink(1);
                return;
            }
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NullBatteryDockBlockEntity(pos, state);
    }
}
