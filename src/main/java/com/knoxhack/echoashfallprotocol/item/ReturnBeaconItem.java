package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import com.knoxhack.echoashfallprotocol.world.NexusCampaignData;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Reusable late-game return utility to the saved Nexus Core anchor.
 */
public class ReturnBeaconItem extends Item {
    private static final String COOLDOWN_KEY = "echoashfallprotocol_return_beacon_ready_tick";
    private static final long COOLDOWN_TICKS = 20L * 60L * 5L;

    public ReturnBeaconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        ServerLevel overworld = serverLevel.getServer().overworld();
        NexusCampaignData campaign = NexusCampaignData.get(overworld);
        PostNexusData post = PostNexusData.get(serverPlayer);
        if (!campaign.isWarfrontComplete() && !post.hasMadeChoice()) {
            serverPlayer.sendSystemMessage(Component.literal("[ECHO-7] Return Beacon locked until the Warfront is stable or a Nexus path is committed.")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }
        BlockPos core = campaign.getNexusPos();
        if (core == null || core.equals(BlockPos.ZERO)) {
            serverPlayer.sendSystemMessage(Component.literal("[ECHO-7] No saved Nexus Core anchor is available.")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }

        CompoundTag data = serverPlayer.getPersistentData();
        long now = serverLevel.getServer().overworld().getGameTime();
        long readyAt = data.getLong(COOLDOWN_KEY).orElse(0L);
        if (readyAt > now && !serverPlayer.getAbilities().instabuild) {
            long seconds = Math.max(1L, (readyAt - now + 19L) / 20L);
            serverPlayer.sendSystemMessage(Component.literal("[ECHO-7] Return Beacon recharging: " + seconds + "s.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }

        data.putLong(COOLDOWN_KEY, now + COOLDOWN_TICKS);
        serverPlayer.teleportTo(overworld, core.getX() + 0.5D, core.getY() + 1.0D, core.getZ() + 0.5D,
                Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);
        overworld.playSound(null, core, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, 0.95F);
        serverPlayer.sendSystemMessage(Component.literal("[NEXUS] Return Beacon locked onto Core anchor.")
                .withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS;
    }
}
