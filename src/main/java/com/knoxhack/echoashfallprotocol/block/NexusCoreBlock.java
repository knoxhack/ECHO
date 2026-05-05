package com.knoxhack.echoashfallprotocol.block;

import com.knoxhack.echoashfallprotocol.block.entity.NexusCoreBlockEntity;
import com.knoxhack.echoashfallprotocol.echo.EchoMessages;
import com.knoxhack.echoashfallprotocol.endgame.NexusAccessRules;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * The Nexus Core - the endgame block that caused the Gridfall.
 */
public class NexusCoreBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final MapCodec<NexusCoreBlock> CODEC = simpleCodec(NexusCoreBlock::new);
    public static final int REQUIRED_NODES = 5;
    public NexusCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, true));
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
        return new NexusCoreBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()
                && level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof NexusCoreBlockEntity be) {
            if (!be.isDiscovered()) {
                be.setDiscovered();
                player.sendSystemMessage(Component.literal(
                        EchoMessages.getMessage(EchoMessages.Context.NEXUS_CORE_FOUND)));
            }

            NexusAccessRules.Status access = NexusAccessRules.evaluate(serverPlayer, serverLevel, be);
            if (!access.allowed()) {
                player.sendSystemMessage(access.denialMessage());
                return InteractionResult.CONSUME;
            }

            player.sendSystemMessage(Component.literal("[NEXUS CORE] " + access.statusText()));
            if (ModList.get().isLoaded("echoterminal")) {
                player.sendSystemMessage(Component.literal(
                        "[ECHO-7] Nexus interface armed in the ECHO Terminal NEXUS tab. Fallback commands remain: /nexus restore|destroy|control."));
            } else {
                player.sendSystemMessage(Component.literal(
                        "[ECHO-7] Nexus interface armed. Use /nexus restore, /nexus destroy, or /nexus control when you are ready to make history permanent."));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
