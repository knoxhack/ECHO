package com.knoxhack.echoterminal.api.mission;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface TerminalMissionProvider {
    TerminalMissionChapter chapter();

    List<TerminalMissionDefinition> missions(Player player);

    TerminalMissionSnapshot snapshot(Player player, Identifier missionId);

    default TerminalMissionPresentation presentation(
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        return TerminalMissionPresentation.fallback(definition, snapshot);
    }

    default TerminalMissionVisuals visuals(
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        return TerminalMissionVisuals.fallback(definition, snapshot);
    }

    default TerminalMissionRole role(
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        return TerminalMissionRole.fallback(definition, snapshot);
    }

    default boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
        return false;
    }
}
