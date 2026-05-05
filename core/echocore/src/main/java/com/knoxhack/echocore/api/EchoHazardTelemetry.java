package com.knoxhack.echocore.api;

public record EchoHazardTelemetry(
        int hydration,
        int radiation,
        int toxicAir,
        int oxygen,
        int pressure,
        int cold,
        int heat,
        int exposure,
        String statusLine) {
    public EchoHazardTelemetry {
        hydration = clamp(hydration);
        radiation = clamp(radiation);
        toxicAir = clamp(toxicAir);
        oxygen = clamp(oxygen);
        pressure = clamp(pressure);
        cold = clamp(cold);
        heat = clamp(heat);
        exposure = clamp(exposure);
        statusLine = statusLine == null || statusLine.isBlank() ? "Vitals nominal." : statusLine;
    }

    public static EchoHazardTelemetry nominal() {
        return new EchoHazardTelemetry(100, 0, 0, 100, 100, 0, 0, 0, "Vitals nominal.");
    }

    public EchoHazardTelemetry merge(EchoHazardTelemetry other) {
        if (other == null) {
            return this;
        }
        String status = "Vitals nominal.".equals(statusLine) ? other.statusLine : statusLine;
        return new EchoHazardTelemetry(
                Math.min(hydration, other.hydration),
                Math.max(radiation, other.radiation),
                Math.max(toxicAir, other.toxicAir),
                Math.min(oxygen, other.oxygen),
                Math.min(pressure, other.pressure),
                Math.max(cold, other.cold),
                Math.max(heat, other.heat),
                Math.max(exposure, other.exposure),
                status);
    }

    public boolean warning() {
        return hydration <= 35 || radiation >= 50 || toxicAir >= 50 || oxygen <= 35 || pressure <= 35
                || cold >= 50 || heat >= 50 || exposure >= 50;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
