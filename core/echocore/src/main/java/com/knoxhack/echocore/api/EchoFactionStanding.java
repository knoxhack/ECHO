package com.knoxhack.echocore.api;

/**
 * Shared standing tiers used by Echo Core faction integrations.
 */
public enum EchoFactionStanding {
    HOSTILE("Hostile", -100, 0xE25959),
    UNKNOWN("Unknown", 0, 0x8E9399),
    CONTACTED("Contacted", 1, 0xD6C88B),
    TRUSTED("Trusted", 35, 0x78C8A4),
    ALIGNED("Aligned", 75, 0x72A7FF);

    private final String displayName;
    private final int reputationFloor;
    private final int accentColor;

    EchoFactionStanding(String displayName, int reputationFloor, int accentColor) {
        this.displayName = displayName;
        this.reputationFloor = reputationFloor;
        this.accentColor = accentColor;
    }

    public String displayName() {
        return displayName;
    }

    public int reputationFloor() {
        return reputationFloor;
    }

    public int accentColor() {
        return accentColor;
    }

    public static EchoFactionStanding fromReputation(int reputation, boolean contacted) {
        if (reputation <= -50) {
            return HOSTILE;
        }
        if (!contacted && reputation <= 0) {
            return UNKNOWN;
        }
        if (reputation >= ALIGNED.reputationFloor) {
            return ALIGNED;
        }
        if (reputation >= TRUSTED.reputationFloor) {
            return TRUSTED;
        }
        return CONTACTED;
    }
}
