package com.knoxhack.echorelictech.block;

import com.knoxhack.echorelictech.api.relic.RelicCondition;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import com.knoxhack.echorelictech.block.entity.PrototypeWorkbenchBlockEntity;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import com.knoxhack.echorelictech.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PrototypeWorkbenchBlock extends Block implements EntityBlock {
    public PrototypeWorkbenchBlock(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data != null) {
            if (data.condition() == RelicCondition.DAMAGED && player.getInventory().contains(new ItemStack(ModItems.RELIC_SHARD.get()))) {
                consumeItem(player, ModItems.RELIC_SHARD.get(), 2);
                stack.set(ModDataComponents.RELIC_DATA.get(), data.withCondition(RelicCondition.STABILIZED));
                player.sendSystemMessage(Component.literal("Prototype Workbench // Relic stabilized."));
                return InteractionResult.SUCCESS;
            }
            if (data.condition() == RelicCondition.STABILIZED && player.getInventory().contains(new ItemStack(ModItems.CONTAINMENT_GLASS.get()))) {
                consumeItem(player, ModItems.CONTAINMENT_GLASS.get(), 1);
                stack.set(ModDataComponents.RELIC_DATA.get(), data.withCondition(RelicCondition.CONTAINED));
                player.sendSystemMessage(Component.literal("Prototype Workbench // Relic contained."));
                return InteractionResult.SUCCESS;
            }
            if (data.condition() == RelicCondition.STABILIZED && player.getInventory().contains(new ItemStack(ModItems.QUANTUM_LATTICE.get()))) {
                consumeItem(player, ModItems.QUANTUM_LATTICE.get(), 1);
                stack.set(ModDataComponents.RELIC_DATA.get(), data.withCondition(RelicCondition.OVERCLOCKED));
                player.sendSystemMessage(Component.literal("Prototype Workbench // Relic overclocked."));
                return InteractionResult.SUCCESS;
            }
            if (data.condition() == RelicCondition.CORRUPTED && player.getInventory().contains(new ItemStack(ModItems.STABILIZED_RIFTSTONE.get()))) {
                consumeItem(player, ModItems.STABILIZED_RIFTSTONE.get(), 1);
                stack.set(ModDataComponents.RELIC_DATA.get(), data.withCondition(RelicCondition.DAMAGED));
                player.sendSystemMessage(Component.literal("Prototype Workbench // Corruption partially purged."));
                return InteractionResult.SUCCESS;
            }
        }
        player.sendSystemMessage(Component.literal("Prototype Workbench // Insert damaged relic with appropriate materials."));
        return InteractionResult.SUCCESS;
    }

    private void consumeItem(Player player, net.minecraft.world.item.Item item, int count) {
        for (int i = 0; i < player.getInventory().getContainerSize() && count > 0; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(item)) {
                int take = Math.min(count, s.getCount());
                s.shrink(take);
                count -= take;
            }
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrototypeWorkbenchBlockEntity(pos, state);
    }
}
