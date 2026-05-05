package com.knoxhack.echocore.api;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.Identifier;

/**
 * Shared faction definition registered by an addon through Echo Core.
 */
public record EchoFactionDefinition(
        Identifier id,
        String displayName,
        String shortName,
        String route,
        String summary,
        String hazard,
        String prepHint,
        String serviceSummary,
        int accentColor,
        boolean landmarkFaction,
        List<EchoNpcRole> roles,
        List<EchoFactionAction> actions,
        List<EchoFactionContract> contracts,
        List<EchoFactionPoiAffinity> poiAffinities,
        EchoDialogueTree dialogue) {

    public EchoFactionDefinition {
        Objects.requireNonNull(id, "id");
        displayName = cleanRequired(displayName, "displayName");
        shortName = clean(shortName);
        if (shortName.isBlank()) {
            shortName = displayName;
        }
        route = clean(route);
        summary = clean(summary);
        hazard = clean(hazard);
        prepHint = clean(prepHint);
        serviceSummary = clean(serviceSummary);
        roles = List.copyOf(Objects.requireNonNullElse(roles, List.of()));
        actions = List.copyOf(Objects.requireNonNullElse(actions, List.of()));
        contracts = List.copyOf(Objects.requireNonNullElse(contracts, List.of()));
        poiAffinities = List.copyOf(Objects.requireNonNullElse(poiAffinities, List.of()));
        requireUniqueRoleIds(roles);
        requireUniqueActionIds(actions);
        requireUniqueContractIds(contracts);
        dialogue = dialogue == null ? EchoDialogueTree.EMPTY : dialogue;
    }

    public String modId() {
        return id.getNamespace();
    }

    private static String cleanRequired(String value, String field) {
        String cleaned = clean(value);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Faction " + field + " cannot be blank");
        }
        return cleaned;
    }

    private static String clean(String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }

    private static void requireUniqueRoleIds(List<EchoNpcRole> roles) {
        Set<String> ids = new LinkedHashSet<>();
        for (EchoNpcRole role : roles) {
            if (role == null || !ids.add(role.id())) {
                throw new IllegalArgumentException("Faction role ids must be unique and non-null");
            }
        }
    }

    private static void requireUniqueActionIds(List<EchoFactionAction> actions) {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (EchoFactionAction action : actions) {
            if (action == null || !ids.add(action.id())) {
                throw new IllegalArgumentException("Faction action ids must be unique and non-null");
            }
        }
    }

    private static void requireUniqueContractIds(List<EchoFactionContract> contracts) {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (EchoFactionContract contract : contracts) {
            if (contract == null || !ids.add(contract.id())) {
                throw new IllegalArgumentException("Faction contract ids must be unique and non-null");
            }
        }
    }
}
