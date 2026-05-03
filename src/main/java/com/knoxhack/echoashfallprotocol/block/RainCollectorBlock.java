package com.knoxhack.echoashfallprotocol.block;

import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.event.EnvironmentalEventHandler;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RainCollectorBlock extends Block {
    public RainCollectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.isRainingAt(pos.above()) || EnvironmentalEventHandler.isStormRainAt(level, pos.above())) {
                ItemStack dirtyWater = new ItemStack(ModItems.DIRTY_WATER_BOTTLE.get());
                if (!player.addItem(dirtyWater)) {
                    player.drop(dirtyWater, false);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    QuestData quest = QuestData.get(serverPlayer);
                    quest.visitLocation("special", "rain:collected");
                    QuestData.saveAndSync(serverPlayer, quest);
                }
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7No rain to collect right now."));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
