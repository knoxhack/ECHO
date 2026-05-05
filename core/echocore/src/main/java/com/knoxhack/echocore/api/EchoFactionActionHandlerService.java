package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Addon-owned faction action bridge. Echo Core owns persistence and routing; addons own effects.
 */
public interface EchoFactionActionHandlerService {
    boolean supports(Identifier factionId);

    default List<EchoFactionAction> actions(Player player, EchoFactionProfile profile, String roleId) {
        return List.of();
    }

    default String localContext(Player player, EchoFactionProfile profile, String roleId) {
        return "";
    }

    default EchoFactionContractState contractState(Player player, EchoFactionProfile profile,
            EchoFactionContract contract, String roleId) {
        return EchoFactionContractState.fromProfile(profile, contract);
    }

    default EchoFactionActionResult acceptContract(ServerPlayer player, EchoFactionProfile profile,
            EchoFactionContract contract, String roleId) {
        return null;
    }

    default EchoFactionActionResult completeContract(ServerPlayer player, EchoFactionProfile profile,
            EchoFactionContract contract, String roleId) {
        return null;
    }

    default EchoFactionActionResult handle(ServerPlayer player, Identifier factionId, Identifier actionId,
            String roleId, Identifier targetId) {
        return EchoFactionActionResult.failure("Signal Unclaimed", "No chapter contact accepted this faction signal.");
    }
}
