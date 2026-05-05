package com.knoxhack.echocore.api;

import java.util.List;

/**
 * Read model for one NPC conversation. Addons can render this however they like.
 */
public record EchoFactionInteractionSnapshot(
        EchoFactionProfile profile,
        String roleId,
        String roleName,
        String greeting,
        String localContext,
        List<EchoFactionAction> actions,
        List<EchoFactionContract> contracts) {

    public EchoFactionInteractionSnapshot {
        if (profile == null) {
            throw new IllegalArgumentException("profile cannot be null");
        }
        roleId = roleId == null ? "" : roleId.trim();
        roleName = roleName == null || roleName.isBlank() ? "Contact" : roleName.trim();
        greeting = greeting == null ? "" : greeting.trim();
        localContext = localContext == null ? "" : localContext.trim();
        actions = actions == null ? List.of() : List.copyOf(actions);
        contracts = contracts == null ? List.of() : List.copyOf(contracts);
    }
}
