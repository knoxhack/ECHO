package com.knoxhack.echocore.api.index;

import net.minecraft.world.entity.player.Player;

public interface IIndexOverlayService {
    default boolean overlayEnabled(Player player) {
        return true;
    }

    default boolean excludedScreen(String screenClassName) {
        return false;
    }
}
