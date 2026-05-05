package com.knoxhack.echocore.api;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface EchoFactionStandingService {
    String standingLine(Player player, String factionId);
}
