package com.knoxhack.signalos.service;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class SignalOsPlayerData {
    private static final String ROOT = "signalos";

    private SignalOsPlayerData() {
    }

    public static boolean isMissionClaimed(Player player, Identifier missionId) {
        return missionId != null && readSet(player, "claimed_mission").contains(missionId.toString());
    }

    public static void markMissionClaimed(Player player, Identifier missionId) {
        if (player == null || missionId == null) {
            return;
        }
        writeSet(player, "claimed_mission", with(readSet(player, "claimed_mission"), missionId.toString()));
    }

    public static boolean isArchiveRead(Player player, Identifier archiveId) {
        return archiveId != null && readSet(player, "read_archive").contains(archiveId.toString());
    }

    public static void markArchiveRead(Player player, Identifier archiveId) {
        if (player == null || archiveId == null) {
            return;
        }
        writeSet(player, "read_archive", with(readSet(player, "read_archive"), archiveId.toString()));
    }

    private static Set<String> readSet(Player player, String prefix) {
        if (player == null) {
            return Set.of();
        }
        CompoundTag root = player.getPersistentData().getCompoundOrEmpty(ROOT);
        int count = root.getIntOr(prefix + "_count", 0);
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            String value = root.getStringOr(prefix + "_" + i, "");
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private static void writeSet(Player player, String prefix, Set<String> values) {
        if (player == null) {
            return;
        }
        CompoundTag root = player.getPersistentData().getCompoundOrEmpty(ROOT);
        int previousCount = root.getIntOr(prefix + "_count", 0);
        int index = 0;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                root.putString(prefix + "_" + index++, value);
            }
        }
        for (int stale = index; stale < previousCount; stale++) {
            root.remove(prefix + "_" + stale);
        }
        root.putInt(prefix + "_count", index);
        player.getPersistentData().put(ROOT, root);
    }

    private static Set<String> with(Set<String> values, String value) {
        LinkedHashSet<String> next = new LinkedHashSet<>(values);
        if (value != null && !value.isBlank()) {
            next.add(value);
        }
        return next;
    }
}
