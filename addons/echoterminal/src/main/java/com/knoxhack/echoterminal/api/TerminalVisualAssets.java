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
    public static final Identifier ICON_GROUP_PROTOCOL = terminalIcon("group_protocol");
    public static final Identifier ICON_GROUP_FIELD = terminalIcon("group_field");
    public static final Identifier ICON_GROUP_SYSTEMS = terminalIcon("group_systems");
    public static final Identifier ICON_GROUP_NEXUS = terminalIcon("group_nexus");
    public static final Identifier ICON_GROUP_ORBITAL = terminalIcon("group_orbital");
    public static final Identifier ICON_GROUP_CHAPTERS = terminalIcon("group_chapters");
    public static final Identifier ICON_PAGE_COMMAND_DECK = terminalIcon("page_command_deck");
    public static final Identifier ICON_PAGE_PROTOCOL_ROADMAP = terminalIcon("page_protocol_roadmap");
    public static final Identifier ICON_PAGE_SIGNAL_LEADS = terminalIcon("page_signal_leads");
    public static final Identifier ICON_PAGE_VITALS_SCAN = terminalIcon("page_vitals_scan");
    public static final Identifier ICON_PAGE_COMPANION_LINK = terminalIcon("page_companion_link");
    public static final Identifier ICON_PAGE_ROUTE_MAP = terminalIcon("page_route_map");
    public static final Identifier ICON_PAGE_FIELD_ARCHIVE = terminalIcon("page_field_archive");
    public static final Identifier ICON_PAGE_SURVIVAL_INDEX = terminalIcon("page_survival_index");
    public static final Identifier ICON_PAGE_BASELINE = terminalIcon("page_baseline");
    public static final Identifier ICON_PAGE_ORBITAL_COMMAND = terminalIcon("page_orbital_command");
    public static final Identifier ICON_PAGE_ROUTE_SURVEY = terminalIcon("page_route_survey");
    public static final Identifier ICON_PAGE_ECHO0_RECORDS = terminalIcon("page_echo0_records");
    public static final Identifier ICON_PAGE_NEXUS_CORE = terminalIcon("page_nexus_core");
    public static final Identifier ICON_PAGE_CHAPTERS = terminalIcon("page_chapters");
    public static final Identifier ICON_ACTION_VIEW = terminalIcon("action_view");
    public static final Identifier ICON_ACTION_TURN_IN = terminalIcon("action_turn_in");
    public static final Identifier ICON_ACTION_CLAIM = terminalIcon("action_claim");
    public static final Identifier ICON_ACTION_SCAN = terminalIcon("action_scan");
    public static final Identifier ICON_ACTION_OPEN_ROADMAP = terminalIcon("action_open_roadmap");
    public static final Identifier ICON_STATE_LOCKED = terminalIcon("state_locked");
    public static final Identifier ICON_STATE_ACTIVE = terminalIcon("state_active");
    public static final Identifier ICON_STATE_NEEDED = terminalIcon("state_needed");
    public static final Identifier ICON_STATE_OPEN = terminalIcon("state_open");
    public static final Identifier ICON_STATE_AVAILABLE = terminalIcon("state_available");
    public static final Identifier ICON_STATE_ONLINE = terminalIcon("state_online");
    public static final Identifier ICON_BRAND_ECHO = terminalIcon("brand_echo");
    public static final Identifier CARD_ACTIVE_PROTOCOL_HERO = card("active_protocol_hero");
    public static final Identifier CARD_MISSION_DETAIL_HEADER = card("mission_detail_header");
    public static final Identifier CARD_SIGNAL_DETAIL_HEADER = card("signal_detail_header");
    public static final Identifier CARD_ROUTE_STATUS_PANEL = card("route_status_panel");
    public static final Identifier CARD_NEXT_ACTION_PANEL = card("next_action_panel");
    public static final Identifier CARD_METRIC_TILE_PLATE = card("metric_tile_plate");
    public static final Identifier CARD_PANEL_LIST_COMPACT = card("panel_list_compact");
    public static final Identifier CARD_PANEL_DETAIL_STANDARD = card("panel_detail_standard");
    public static final Identifier CARD_PANEL_STATUS_HEALTH = card("panel_status_health");
    public static final Identifier CARD_PANEL_STATUS_SYNC = card("panel_status_sync");
    public static final Identifier CARD_PANEL_DRONE_COMMAND = card("panel_drone_command");
    public static final Identifier CARD_PANEL_ROUTE_MAP = card("panel_route_map");
    public static final Identifier CARD_PANEL_ARCHIVE_CODEX = card("panel_archive_codex");
    public static final Identifier CARD_PANEL_NEXUS_PATH = card("panel_nexus_path");
    public static final Identifier CARD_PANEL_CHAPTER_STATUS = card("panel_chapter_status");
    public static final Identifier CARD_PANEL_ORBITAL_COMMAND = card("panel_orbital_command");

    private TerminalVisualAssets() {
    }

    public static Identifier terminal(String name) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "textures/gui/terminal/" + name + ".png");
    }

    public static Identifier icon(String name) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "textures/gui/icons/" + name + ".png");
    }

    public static Identifier terminalIcon(String name) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "textures/gui/icons/terminal/" + name + ".png");
    }

    public static Identifier card(String name) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "textures/gui/terminal/cards/" + name + ".png");
    }

    public static Identifier terminalGroupIcon(String group) {
        if (TerminalTabChrome.GROUP_PROTOCOL.equals(group) || TerminalTabChrome.GROUP_CORE.equals(group)) {
            return ICON_GROUP_PROTOCOL;
        }
        if (TerminalTabChrome.GROUP_FIELD.equals(group)) {
            return ICON_GROUP_FIELD;
        }
        if (TerminalTabChrome.GROUP_SYSTEMS.equals(group)) {
            return ICON_GROUP_SYSTEMS;
        }
        if (TerminalTabChrome.GROUP_NEXUS.equals(group) || TerminalTabChrome.GROUP_ENDGAME.equals(group)) {
            return ICON_GROUP_NEXUS;
        }
        if (TerminalTabChrome.GROUP_ORBITAL.equals(group)) {
            return ICON_GROUP_ORBITAL;
        }
        if (TerminalTabChrome.GROUP_ADDONS.equals(group)) {
            return ICON_GROUP_CHAPTERS;
        }
        return ICON_BRAND_ECHO;
    }

    public static Identifier terminalPageIcon(String title) {
        String value = title == null ? "" : title.toLowerCase(Locale.ROOT);
        if (value.contains("command deck") || value.contains("overview")) {
            return ICON_PAGE_COMMAND_DECK;
        }
        if (value.contains("protocol roadmap") || value.contains("mission")) {
            return ICON_PAGE_PROTOCOL_ROADMAP;
        }
        if (value.contains("signal lead")) {
            return ICON_PAGE_SIGNAL_LEADS;
        }
        if (value.contains("vitals") || value.contains("hazard")) {
            return ICON_PAGE_VITALS_SCAN;
        }
        if (value.contains("companion") || value.contains("drone")) {
            return ICON_PAGE_COMPANION_LINK;
        }
        if (value.contains("route map") || value.contains("poi")) {
            return ICON_PAGE_ROUTE_MAP;
        }
        if (value.contains("field archive") || value.contains("archive")) {
            return ICON_PAGE_FIELD_ARCHIVE;
        }
        if (value.contains("survival index") || value.contains("codex") || value.contains("recipe")) {
            return ICON_PAGE_SURVIVAL_INDEX;
        }
        if (value.contains("baseline") || value.contains("minecraft")) {
            return ICON_PAGE_BASELINE;
        }
        if (value.contains("orbital command")) {
            return ICON_PAGE_ORBITAL_COMMAND;
        }
        if (value.contains("route survey")) {
            return ICON_PAGE_ROUTE_SURVEY;
        }
        if (value.contains("echo-0") || value.contains("echo 0")) {
            return ICON_PAGE_ECHO0_RECORDS;
        }
        if (value.contains("nexus core") || value.contains("final path")) {
            return ICON_PAGE_NEXUS_CORE;
        }
        if (value.contains("chapter") || value.contains("addon")) {
            return ICON_PAGE_CHAPTERS;
        }
        return null;
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
