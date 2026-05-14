package com.knoxhack.echorelictech.block;

import com.knoxhack.echorelictech.api.relic.RelicCondition;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import com.knoxhack.echorelictech.block.entity.RelicAnalyzerBlockEntity;
import com.knoxhack.echorelictech.registry.ModBlockEntities;
import com.knoxhack.echorelictech.registry.ModItems;
import com.knoxhack.echorelictech.registry.ModDataComponents;
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

public class RelicAnalyzerBlock extends Block implements EntityBlock {
    public RelicAnalyzerBlock(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        player.sendSystemMessage(Component.literal("Relic Analyzer // Insert Unidentified Relic to analyze."));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (stack.is(ModItems.UNIDENTIFIED_RELIC.get())) {
            stack.shrink(1);
            ItemStack output = new ItemStack(ModItems.PHASE_ANCHOR.get());
            output.set(ModDataComponents.RELIC_DATA.get(), new RelicInstanceData(
                Identifier.fromNamespaceAndPath("echorelictech", "phase_anchor"),
                RelicCondition.DAMAGED, 0, BlockPos.ZERO, "", 0, false, false, false, false, 0));
            if (!player.getInventory().add(output)) {
                player.drop(output, false);
            }
            player.sendSystemMessage(Component.literal("Relic Analyzer // Identification complete. Phase Anchor recovered."));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelicAnalyzerBlockEntity(pos, state);
    }
}
