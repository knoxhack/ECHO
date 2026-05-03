package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Early-game contaminated water collection.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class WaterCollectionHandler {
    private WaterCollectionHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack held = event.getItemStack();
        if (!held.is(Items.GLASS_BOTTLE)) {
            return;
        }

        Level level = player.level();
        BlockState state = level.getBlockState(event.getPos());
        if (!isDirtyWaterSource(level, state, event)) {
            return;
        }

        fillDirtyWater(level, player, event.getHand(), event.getPos());
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack held = event.getItemStack();
        if (!held.is(Items.GLASS_BOTTLE)) {
            return;
        }

        Level level = player.level();
        BlockHitResult hit = getWaterSourceHit(level, player);
        if (hit == null || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return;
        }

        fillDirtyWater(level, player, event.getHand(), hit.getBlockPos());
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }

    private static void fillDirtyWater(Level level, ServerPlayer player, InteractionHand hand, BlockPos soundPos) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack dirtyWater = new ItemStack(ModItems.DIRTY_WATER_BOTTLE.get());
        ItemStack result = ItemUtils.createFilledResult(held, player, dirtyWater);
        player.setItemInHand(hand, result);

        level.playSound(null, soundPos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.7F, 0.8F);
        player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.water.collect_dirty"));
    }

    private static BlockHitResult getWaterSourceHit(Level level, ServerPlayer player) {
        Vec3 from = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double reach = player.blockInteractionRange();
        Vec3 to = from.add(look.x * reach, look.y * reach, look.z * reach);
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    private static boolean isDirtyWaterSource(Level level, BlockState state, PlayerInteractEvent.RightClickBlock event) {
        if (state.is(Blocks.WATER_CAULDRON)) {
            return false;
        }

        if (state.is(ModBlocks.TOXIC_PUDDLE.get()) || state.is(ModBlocks.ACIDIC_SLUDGE.get())) {
            return true;
        }

        if (level.getFluidState(event.getPos()).is(FluidTags.WATER)) {
            return true;
        }

        return level.getFluidState(event.getPos().relative(event.getFace())).is(FluidTags.WATER);
    }
}
