package com.knoxhack.echocore.api;

public enum EchoDiscoveryState {
    LOCKED("Locked"),
    DISCOVERED("Discovered"),
    CHECKED("Checked");

    private final String displayName;

    EchoDiscoveryState(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
