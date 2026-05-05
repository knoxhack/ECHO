package com.knoxhack.echocore.api;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface EchoPackModeService {
    EchoPackMode packMode(Player player);
}
