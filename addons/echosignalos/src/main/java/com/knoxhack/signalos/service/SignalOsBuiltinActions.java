package com.knoxhack.signalos.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    public static final Identifier PAGE_NOTES = id("notes");
    public static final Identifier PAGE_SETTINGS = id("settings");
    public static final Identifier CLAIM_REWARDS = id("claim_rewards");
    public static final Identifier CLAIM_MISSION = id("claim_mission");
    public static final Identifier MARK_ARCHIVE_READ = id("mark_archive_read");
    public static final Identifier SAVE_NOTE = id("save_note");
    public static final Identifier DELETE_NOTE = id("delete_note");
    public static final Identifier CLEAR_NOTES = id("clear_notes");
    public static final Identifier SET_PREFERENCE = id("set_preference");
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
        TerminalActionRegistry.register(PAGE_NOTES, SAVE_NOTE, SignalOsBuiltinActions::saveNote);
        TerminalActionRegistry.register(PAGE_NOTES, DELETE_NOTE, SignalOsBuiltinActions::deleteNote);
        TerminalActionRegistry.register(PAGE_NOTES, CLEAR_NOTES,
                (player, payload) -> SignalOsPlayerData.clearNotes(player));
        TerminalActionRegistry.register(PAGE_SETTINGS, SET_PREFERENCE, SignalOsBuiltinActions::setPreference);
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

    private static void saveNote(ServerPlayer player, String payload) {
        String safe = payload == null ? "" : payload;
        String title = "Operator Note";
        String body = "Created from the SignalOS Notes app.";
        Identifier noteId = null;
        if (safe.stripLeading().startsWith("{")) {
            try {
                JsonObject json = JsonParser.parseString(safe).getAsJsonObject();
                title = string(json, "title", title);
                body = string(json, "body", body);
                String idValue = string(json, "id", "");
                noteId = idValue.isBlank() ? null : Identifier.tryParse(idValue);
            } catch (RuntimeException exception) {
                status(player, "[SignalOS] Invalid note payload.");
                return;
            }
        } else {
            int split = safe.indexOf('\n');
            if (split >= 0) {
                title = safe.substring(0, split).strip();
                body = safe.substring(split + 1).strip();
            } else if (!safe.isBlank()) {
                body = safe.strip();
            }
        }
        SignalOsPlayerData.saveNote(player, noteId, title, body);
        status(player, "[SignalOS] Note saved.");
    }

    private static void deleteNote(ServerPlayer player, String payload) {
        Identifier noteId = Identifier.tryParse(payload == null ? "" : payload.strip());
        if (!SignalOsPlayerData.deleteNote(player, noteId)) {
            status(player, "[SignalOS] Note unavailable.");
            return;
        }
        status(player, "[SignalOS] Note deleted.");
    }

    private static void setPreference(ServerPlayer player, String payload) {
        String safe = payload == null ? "" : payload;
        int split = safe.indexOf('=');
        if (split <= 0) {
            status(player, "[SignalOS] Invalid setting payload.");
            return;
        }
        String key = safe.substring(0, split).strip();
        String value = safe.substring(split + 1).strip();
        SignalOsPlayerData.setPreference(player, key, value);
        status(player, "[SignalOS] Setting updated.");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SignalOS.MODID, path);
    }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : fallback;
    }

    private static void status(Player player, String message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(message), true);
        } else if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }
}
