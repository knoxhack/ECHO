package com.knoxhack.echoterminal.api;

import java.util.List;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface TerminalNotificationProvider {
    List<String> notifications(Player player);
}
