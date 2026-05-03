package com.knoxhack.echoashfallprotocol.block;

import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WildBerryBushBlock extends Block {
    public WildBerryBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            ItemStack berry = new ItemStack(ModItems.WILD_BERRY.get());
            if (!player.addItem(berry)) {
                player.drop(berry, false);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
