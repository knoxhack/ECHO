package com.knoxhack.echocore.api;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface EchoHazardTelemetryService {
    EchoHazardTelemetry telemetry(Player player);
}
