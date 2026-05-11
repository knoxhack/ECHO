package com.knoxhack.echocore.api.mission;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MissionStatusRule {
    MissionStatusRule NONE = (player, mission) -> Optional.empty();

    Optional<MissionStatus> status(Player player, MissionDefinition mission);
}
