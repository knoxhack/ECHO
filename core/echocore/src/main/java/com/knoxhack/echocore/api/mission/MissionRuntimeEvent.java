package com.knoxhack.echocore.api.mission;

import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record MissionRuntimeEvent(
        Identifier eventType,
        UUID playerId,
        Identifier missionId,
        Identifier objectiveId,
        Identifier rewardId,
        int amount,
        Map<String, String> context) {
    public static final Identifier MISSION_STARTED = Identifier.fromNamespaceAndPath("echocore", "mission_started");
    public static final Identifier OBJECTIVE_PROGRESSED = Identifier.fromNamespaceAndPath("echocore", "objective_progressed");
    public static final Identifier MISSION_COMPLETED = Identifier.fromNamespaceAndPath("echocore", "mission_completed");
    public static final Identifier REWARD_CLAIMED = Identifier.fromNamespaceAndPath("echocore", "reward_claimed");
    public static final Identifier CHAPTER_UNLOCKED = Identifier.fromNamespaceAndPath("echocore", "chapter_unlocked");

    public MissionRuntimeEvent {
        context = Map.copyOf(context == null ? Map.of() : context);
    }

    public static MissionRuntimeEvent of(
            Identifier eventType,
            ServerPlayer player,
            Identifier missionId,
            Identifier objectiveId,
            Identifier rewardId,
            int amount,
            Map<String, String> context) {
        return new MissionRuntimeEvent(
                eventType,
                player == null ? null : player.getUUID(),
                missionId,
                objectiveId,
                rewardId,
                amount,
                context);
    }
}
