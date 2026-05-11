package com.knoxhack.echocore.api.mission;

@FunctionalInterface
public interface MissionContentRegistrar {
    void register(IMissionRegistry registry);
}
