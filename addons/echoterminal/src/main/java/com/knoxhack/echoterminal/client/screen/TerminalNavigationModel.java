package com.knoxhack.echoterminal.client.screen;

import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TerminalNavigationModel {
    static final List<String> GROUP_ORDER = List.of(
            TerminalTabChrome.GROUP_PROTOCOL,
            TerminalTabChrome.GROUP_FIELD,
            TerminalTabChrome.GROUP_SYSTEMS,
            TerminalTabChrome.GROUP_NEXUS,
            TerminalTabChrome.GROUP_ORBITAL,
            TerminalTabChrome.GROUP_ADDONS,
            TerminalTabChrome.GROUP_CORE,
            TerminalTabChrome.GROUP_ENDGAME);

    private final List<TerminalTab> tabs;
    private final List<String> groups;
    private final Map<String, List<IndexedTab>> tabsByGroup;

    private TerminalNavigationModel(List<TerminalTab> tabs) {
        this.tabs = List.copyOf(tabs);
        this.groups = collectGroups(tabs);
        this.tabsByGroup = collectTabsByGroup(tabs, groups);
    }

    static TerminalNavigationModel of(List<TerminalTab> tabs) {
        return new TerminalNavigationModel(tabs == null ? List.of() : tabs);
    }

    List<TerminalTab> tabs() {
        return tabs;
    }

    List<String> groups() {
        return groups;
    }

    List<IndexedTab> tabsInGroup(String group) {
        return tabsByGroup.getOrDefault(group, List.of());
    }

    String activeGroup(int activeTab) {
        if (tabs.isEmpty() || activeTab < 0 || activeTab >= tabs.size()) {
            return TerminalTabChrome.GROUP_PROTOCOL;
        }
        return tabs.get(activeTab).chrome().group();
    }

    int firstTabInGroup(String group) {
        for (IndexedTab entry : tabsInGroup(group)) {
            return entry.index();
        }
        return tabs.isEmpty() ? 0 : Math.min(0, tabs.size() - 1);
    }

    int groupAccent(String group, int fallback) {
        for (IndexedTab entry : tabsInGroup(group)) {
            return entry.tab().descriptor().accentColor();
        }
        return fallback;
    }

    String groupLabel(String group) {
        return switch (group) {
            case TerminalTabChrome.GROUP_PROTOCOL -> "Protocol";
            case TerminalTabChrome.GROUP_CORE -> "Core";
            case TerminalTabChrome.GROUP_FIELD -> "Field";
            case TerminalTabChrome.GROUP_SYSTEMS -> "Systems";
            case TerminalTabChrome.GROUP_NEXUS -> "Nexus";
            case TerminalTabChrome.GROUP_ENDGAME -> "Endgame";
            case TerminalTabChrome.GROUP_ORBITAL -> "Orbital";
            case TerminalTabChrome.GROUP_ADDONS -> "Chapters";
            default -> group;
        };
    }

    private static List<String> collectGroups(List<TerminalTab> tabs) {
        List<String> result = new ArrayList<>();
        for (String group : GROUP_ORDER) {
            if (containsGroup(tabs, group)) {
                result.add(group);
            }
        }
        for (TerminalTab tab : tabs) {
            String group = tab.chrome().group();
            if (!result.contains(group)) {
                result.add(group);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, List<IndexedTab>> collectTabsByGroup(List<TerminalTab> tabs, List<String> groups) {
        Map<String, List<IndexedTab>> result = new LinkedHashMap<>();
        for (String group : groups) {
            result.put(group, new ArrayList<>());
        }
        for (int i = 0; i < tabs.size(); i++) {
            TerminalTab tab = tabs.get(i);
            result.computeIfAbsent(tab.chrome().group(), ignored -> new ArrayList<>())
                    .add(new IndexedTab(i, tab));
        }
        result.replaceAll((group, entries) -> List.copyOf(entries));
        return Map.copyOf(result);
    }

    private static boolean containsGroup(List<TerminalTab> tabs, String group) {
        for (TerminalTab tab : tabs) {
            if (tab.chrome().group().equals(group)) {
                return true;
            }
        }
        return false;
    }

    record IndexedTab(int index, TerminalTab tab) {
    }
}
