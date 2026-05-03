package com.knoxhack.echoorbitalremnants.progression;

import net.minecraft.network.chat.Component;

public enum OrbitalEventType {
    DEBRIS_STORM("Debris storm", "Shelter behind station plating. Solar power interruption likely."),
    SOLAR_FLARE("Solar flare", "Radiation rising. Electronics may flicker."),
    STATION_BLACKOUT("Station blackout", "Life support unstable. Vacuum signatures detected."),
    NEXUS_PULSE("Nexus pulse", "Gravity shear detected. ECHO memory interference rising.");

    private final String title;
    private final String diagnostic;

    OrbitalEventType(String title, String diagnostic) {
        this.title = title;
        this.diagnostic = diagnostic;
    }

    public Component diagnosticMessage() {
        return Component.literal("ECHO-7 // " + title + ": " + diagnostic);
    }
}
