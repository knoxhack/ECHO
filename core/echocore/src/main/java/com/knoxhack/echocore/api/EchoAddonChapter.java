package com.knoxhack.echocore.api;

import net.minecraft.world.entity.player.Player;

/**
 * Lightweight descriptor used by ECHO-branded addon chapters.
 */
public interface EchoAddonChapter {
    String id();

    String modId();

    String displayName();

    String summary();

    default boolean isAvailable(Player player) {
        return true;
    }

    default String statusLine(Player player) {
        return isAvailable(player) ? "Chapter available." : "Chapter locked.";
    }
}
