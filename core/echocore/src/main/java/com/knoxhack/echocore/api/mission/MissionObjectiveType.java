package com.knoxhack.echocore.api.mission;

import java.util.Locale;

public enum MissionObjectiveType {
    CUSTOM("custom"),
    DISCOVER_STRUCTURE("discover_structure"),
    ENTER_REGION("enter_region"),
    SCAN_BLOCK("scan_block"),
    SCAN_ENTITY("scan_entity"),
    OBTAIN_ITEM("obtain_item"),
    CRAFT_ITEM("craft_item"),
    DELIVER_ITEM("deliver_item"),
    KILL_ENTITY("kill_entity"),
    PLACE_BLOCK("place_block"),
    REPAIR_MACHINE("repair_machine"),
    BUILD_MULTIBLOCK("build_multiblock"),
    DRIVE_VEHICLE("drive_vehicle"),
    ESTABLISH_ROUTE("establish_route"),
    COMPLETE_ORBITAL_SCAN("complete_orbital_scan"),
    UNLOCK_RESEARCH("unlock_research"),
    SURVIVE_TIME("survive_time"),
    SURVIVE_DAYS("survive_days");

    private final String id;

    MissionObjectiveType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static MissionObjectiveType byId(String id) {
        if (id == null || id.isBlank()) {
            return CUSTOM;
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (MissionObjectiveType type : values()) {
            if (type.id.equals(normalized) || type.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
