package com.knoxhack.echocore.api;

public enum EchoPackMode {
    COMPLETE_SAGA("Complete ECHO Saga", "Earth, orbital, Stationfall, Nexus, and Blackbox chapters active."),
    FULL_SAGA_WITH_EXTENSIONS("Full ECHO Saga + Extensions", "Main saga routes, industry, logistics, convoy, and armory chapters active."),
    ASHFALL_STANDALONE("Ashfall Protocol", "Earth survival route active."),
    ORBITAL_STANDALONE("Orbital Remnants", "Orbital recovery route active."),
    FULL_SAGA("Full ECHO Saga", "Earth and orbital recovery routes active."),
    INDUSTRIAL_EXTENSION("Industrial Nexus", "Factory survival extension active."),
    UNKNOWN("ECHO Stack", "No active field chapter detected.");

    private final String displayName;
    private final String statusLine;

    EchoPackMode(String displayName, String statusLine) {
        this.displayName = displayName;
        this.statusLine = statusLine;
    }

    public String displayName() {
        return displayName;
    }

    public String statusLine() {
        return statusLine;
    }
}
