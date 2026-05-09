package com.knoxhack.signalos.service;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.api.TerminalActionRegistry;
import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalMission;
import com.knoxhack.signalos.content.SignalOsContentRegistry;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SignalOsBuiltinActions {
    public static final Identifier PAGE_REWARDS = id("rewards");
    public static final Identifier PAGE_MISSIONS = id("missions");
    public static final Identifier PAGE_ARCHIVES = id("archives");
    public static final Identifier CLAIM_REWARDS = id("claim_rewards");
    public static final Identifier CLAIM_MISSION = id("claim_mission");
    public static final Identifier MARK_ARCHIVE_READ = id("mark_archive_read");
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private SignalOsBuiltinActions() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalActionRegistry.register(PAGE_REWARDS, CLAIM_REWARDS,
                (player, payload) -> SignalOsTerminalServices.claimRewards(player));
        TerminalActionRegistry.register(PAGE_MISSIONS, CLAIM_MISSION, SignalOsBuiltinActions::claimMission);
        TerminalActionRegistry.register(PAGE_ARCHIVES, MARK_ARCHIVE_READ, SignalOsBuiltinActions::markArchiveRead);
    }

    private static void claimMission(ServerPlayer player, String payload) {
        Identifier missionId = Identifier.tryParse(payload == null ? "" : payload);
        TerminalMission mission = SignalOsContentRegistry.mission(missionId);
        if (mission == null) {
            if (missionId != null) {
                status(player, "[SignalOS] Mission cache unavailable.");
            }
            return;
        }
        if (!mission.rewardClaim() || mission.rewards().isEmpty()) {
            status(player, "[SignalOS] Mission has no claimable cache.");
            return;
        }
        if (SignalOsPlayerData.isMissionClaimed(player, mission.id())) {
            status(player, "[SignalOS] Mission cache already claimed.");
            return;
        }
        List<ItemStack> rewards = mission.rewardStacks();
        if (rewards.isEmpty()) {
            status(player, "[SignalOS] Mission cache has no valid rewards.");
            return;
        }
        if (!completed(player, mission)) {
            status(player, "[SignalOS] Mission completion signal is not ready.");
            return;
        }
        if (!SignalOsTerminalServices.storeRewards(player, mission.id().toString(), rewards)) {
            return;
        }
        SignalOsPlayerData.markMissionClaimed(player, mission.id());
    }

    private static void markArchiveRead(ServerPlayer player, String payload) {
        Identifier archiveId = Identifier.tryParse(payload == null ? "" : payload);
        markArchiveRead(player, archiveId);
    }

    public static boolean markArchiveRead(Player player, Identifier archiveId) {
        TerminalArchiveRecord archive = SignalOsContentRegistry.archive(archiveId);
        if (archive == null) {
            if (archiveId != null) {
                status(player, "[SignalOS] Archive record unavailable.");
            }
            return false;
        }
        if (archive.locked()) {
            status(player, "[SignalOS] Archive record locked.");
            return false;
        }
        SignalOsPlayerData.markArchiveRead(player, archive.id());
        return true;
    }

    public static boolean completed(ServerPlayer player, TerminalMission mission) {
        if (mission == null) {
            return false;
        }
        if (mission.completionAdvancement() == null) {
            return true;
        }
        if (player == null || player.level().getServer() == null) {
            return false;
        }
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(mission.completionAdvancement());
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SignalOS.MODID, path);
    }

    private static void status(Player player, String message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(message), true);
        } else if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }
}
