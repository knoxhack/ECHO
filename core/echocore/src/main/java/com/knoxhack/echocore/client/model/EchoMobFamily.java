package com.knoxhack.echocore.client.model;

import java.util.Locale;

public enum EchoMobFamily {
    HUMANOID,
    SURVIVOR_NPC,
    STATION_SUIT,
    WRAITH,
    DRONE,
    QUADRUPED,
    CRAWLER,
    SLIME,
    HEAVY_BOSS,
    INDUSTRIAL_CONSTRUCT,
    ROCKET;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
