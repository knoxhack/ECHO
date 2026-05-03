package com.knoxhack.echoterminal.api;

import com.knoxhack.echoterminal.EchoTerminal;
import java.util.Locale;
import net.minecraft.resources.Identifier;

public final class TerminalVisualAssets {
    public static final Identifier TERMINAL_FRAME_BACKDROP = terminal("terminal_frame_backdrop");
    public static final Identifier OVERVIEW_PROTOCOL_DASHBOARD = terminal("overview_protocol_dashboard");
    public static final Identifier MISSIONS_VISUAL_HERO = terminal("missions_visual_hero");
    public static final Identifier MISSION_SURVIVAL = terminal("mission_survival");
    public static final Identifier MISSION_CRAFTING = terminal("mission_crafting");
    public static final Identifier MISSION_TECH = terminal("mission_tech");
    public static final Identifier MISSION_EXPLORATION = terminal("mission_exploration");
    public static final Identifier MISSION_COMBAT = terminal("mission_combat");
    public static final Identifier MISSION_STORY = terminal("mission_story");
    public static final Identifier MISSION_SIDE_OPS = terminal("mission_side_ops");
    public static final Identifier MISSION_ICON_SURVIVAL = icon("mission_survival");
    public static final Identifier MISSION_ICON_CRAFTING = icon("mission_crafting");
    public static final Identifier MISSION_ICON_TECH = icon("mission_tech");
    public static final Identifier MISSION_ICON_EXPLORATION = icon("mission_exploration");
    public static final Identifier MISSION_ICON_COMBAT = icon("mission_combat");
    public static final Identifier MISSION_ICON_STORY = icon("mission_story");
    public static final Identifier MISSION_ICON_SIDE_OPS = icon("mission_side_ops");
    public static final Identifier MISSION_ICON_HAZARD = icon("mission_hazard");
    public static final Identifier STATUS_HAZARD_SCAN = terminal("status_hazard_scan");
    public static final Identifier DRONE_COMMAND_LINK = terminal("drone_command_link");
    public static final Identifier ARCHIVES_DOSSIER_WALL = terminal("archives_dossier_wall");
    public static final Identifier CODEX_FIELD_MANUAL = terminal("codex_field_manual");
    public static final Identifier WORLD_ROUTE_MAP = terminal("world_route_map");
    public static final Identifier NEXUS_CORE_INTERFACE = terminal("nexus_core_interface");
    public static final Identifier ORBITAL_ROUTE_TELEMETRY = terminal("orbital_route_telemetry");
    public static final Identifier ADDONS_MODULE_GRID = terminal("addons_module_grid");

    private TerminalVisualAssets() {
    }

    public static Identifier terminal(String name) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "textures/gui/terminal/" + name + ".png");
    }

    public static Identifier icon(String name) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "textures/gui/icons/" + name + ".png");
    }

    public static Identifier missionIconArt(Identifier missionId, String category) {
        if (missionId != null && hasBundledMissionIcon(missionId)) {
            return Identifier.fromNamespaceAndPath(EchoTerminal.MODID,
                    "textures/gui/mission_icons/" + missionId.getNamespace() + "/" + missionId.getPath() + ".png");
        }
        return missionCategoryIcon(category);
    }

    private static boolean hasBundledMissionIcon(Identifier missionId) {
        String namespace = missionId.getNamespace();
        return "echoashfallprotocol".equals(namespace)
                || "echoorbitalremnants".equals(namespace)
                || "minecraft".equals(namespace);
    }

    public static Identifier missionCategoryArt(String category) {
        String key = category == null ? "" : category.toLowerCase(Locale.ROOT);
        if (key.contains("survival") || key.contains("water") || key.contains("radiation")) {
            return MISSION_SURVIVAL;
        }
        if (key.contains("craft") || key.contains("machine") || key.contains("recipe")) {
            return MISSION_CRAFTING;
        }
        if (key.contains("tech") || key.contains("research") || key.contains("power") || key.contains("grid")) {
            return MISSION_TECH;
        }
        if (key.contains("explor") || key.contains("world") || key.contains("route") || key.contains("poi")) {
            return MISSION_EXPLORATION;
        }
        if (key.contains("combat") || key.contains("guardian") || key.contains("warden") || key.contains("boss")) {
            return MISSION_COMBAT;
        }
        if (key.contains("story") || key.contains("nexus") || key.contains("archive")) {
            return MISSION_STORY;
        }
        return MISSION_SIDE_OPS;
    }

    public static Identifier missionCategoryIcon(String category) {
        String key = category == null ? "" : category.toLowerCase(Locale.ROOT);
        if (key.contains("hazard") || key.contains("weather") || key.contains("storm") || key.contains("biome")) {
            return MISSION_ICON_HAZARD;
        }
        if (key.contains("survival") || key.contains("water") || key.contains("radiation")) {
            return MISSION_ICON_SURVIVAL;
        }
        if (key.contains("craft") || key.contains("machine") || key.contains("recipe")) {
            return MISSION_ICON_CRAFTING;
        }
        if (key.contains("tech") || key.contains("research") || key.contains("power") || key.contains("grid")) {
            return MISSION_ICON_TECH;
        }
        if (key.contains("explor") || key.contains("world") || key.contains("route") || key.contains("poi")) {
            return MISSION_ICON_EXPLORATION;
        }
        if (key.contains("combat") || key.contains("guardian") || key.contains("warden") || key.contains("boss")) {
            return MISSION_ICON_COMBAT;
        }
        if (key.contains("story") || key.contains("nexus") || key.contains("archive")) {
            return MISSION_ICON_STORY;
        }
        return MISSION_ICON_SIDE_OPS;
    }
}
