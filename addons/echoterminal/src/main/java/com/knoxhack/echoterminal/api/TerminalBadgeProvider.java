package com.knoxhack.echoterminal.api;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface TerminalBadgeProvider {
    String badgeText(Player player);
}
