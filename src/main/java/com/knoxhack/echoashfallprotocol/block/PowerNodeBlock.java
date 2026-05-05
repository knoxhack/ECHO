package com.knoxhack.echoashfallprotocol.block;

import com.knoxhack.echoashfallprotocol.block.entity.PowerNodeBlockEntity;
import com.knoxhack.echoashfallprotocol.echo.EchoMessages;
import com.knoxhack.echoashfallprotocol.event.PostNexusEventHandler;
import com.knoxhack.echoashfallprotocol.faction.FactionQuest;
import com.knoxhack.echoashfallprotocol.faction.FactionQuestProgression;
import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.registry.ModBlockEntities;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PowerNodeBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final MapCodec<PowerNodeBlock> CODEC = simpleCodec(PowerNodeBlock::new);

    public PowerNodeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PowerNodeBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.POWER_NODE.get(), PowerNodeBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (state.getValue(ACTIVE)) {
                player.sendSystemMessage(Component.literal("[ECHO-7] Power Node already active. Grid contribution confirmed."));
                return InteractionResult.CONSUME;
            }

            ItemStack held = player.getMainHandItem();
            if (held.getItem() == ModItems.ENERGY_CELL.get()) {
                held.shrink(1);
                level.setBlock(pos, state.setValue(ACTIVE, true), 3);

                if (level.getBlockEntity(pos) instanceof PowerNodeBlockEntity be) {
                    be.activate();
                }

                player.sendSystemMessage(Component.literal(
                        EchoMessages.getMessage(EchoMessages.Context.POWER_NODE_ACTIVATED)));
                if (player instanceof ServerPlayer serverPlayer) {
                    FactionQuestProgression.progress(serverPlayer, FactionQuest.ObjectiveType.REPAIR, "relay", ReputationData.Faction.REMNANTS, 1);
                    com.knoxhack.echoashfallprotocol.faction.AshfallFactionContractProgression.progressRepair(serverPlayer, "power_node");
                    PostNexusEventHandler.recordPowerNodeActivated(serverPlayer, pos);
                }
            } else {
                player.sendSystemMessage(Component.literal(
                        "[ECHO-7] Power Node dormant. Insert an Energy Cell to wake the local grid anchor."));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
