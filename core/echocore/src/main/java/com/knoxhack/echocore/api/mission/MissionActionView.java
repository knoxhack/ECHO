package com.knoxhack.echocore.api.mission;

public record MissionActionView(String id, String label, boolean enabled, String disabledReason) {
    public MissionActionView {
        id = id == null ? "" : id;
        label = label == null || label.isBlank() ? id : label;
        disabledReason = disabledReason == null ? "" : disabledReason;
    }

    public static MissionActionView enabled(String id, String label) {
        return new MissionActionView(id, label, true, "");
    }

    public static MissionActionView disabled(String id, String label, String disabledReason) {
        return new MissionActionView(id, label, false, disabledReason);
    }
}
