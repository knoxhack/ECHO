package com.knoxhack.echocore.api.mission;

import java.util.List;
import net.minecraft.resources.Identifier;

public interface IMissionProgressView {
    MissionDefinition definition();

    Identifier id();

    Identifier chapterId();

    MissionStatus status();

    float progress();

    String statusLabel();

    String unlockReason();

    String actionHint();

    List<IObjectiveView> objectives();

    List<IRewardView> rewards();

    List<MissionActionView> actions();
}
