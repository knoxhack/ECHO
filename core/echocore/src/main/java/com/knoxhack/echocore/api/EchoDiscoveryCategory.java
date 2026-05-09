package com.knoxhack.echocore.api;

public enum EchoDiscoveryCategory {
    STRUCTURE("Structures"),
    BIOME("Biomes"),
    GUARDIAN("Guardians"),
    EVENT("Events"),
    FACTION("Factions");

    private final String displayName;

    EchoDiscoveryCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
