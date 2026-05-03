package com.knoxhack.echoterminal.api.mission;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class TerminalMissionActions {
    public static final Identifier MISSION_ACTION =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "mission_action");

    private TerminalMissionActions() {
    }

    public static void registerForTab(Identifier tabId) {
        TerminalActionRegistry.register(tabId, MISSION_ACTION, TerminalMissionActions::handle);
    }

    public static String payload(Identifier chapterId, Identifier missionId, String actionId) {
        return chapterId + "|" + missionId + "|" + (actionId == null ? "" : actionId);
    }

    private static void handle(ServerPlayer player, String payload) {
        String[] parts = payload == null ? new String[0] : payload.split("\\|", 3);
        if (parts.length != 3) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Invalid mission action payload."), true);
            return;
        }
        Identifier chapterId = Identifier.tryParse(parts[0]);
        Identifier missionId = Identifier.tryParse(parts[1]);
        String actionId = parts[2];
        if (chapterId == null || missionId == null || actionId == null || actionId.isBlank()) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Invalid mission action id."), true);
            return;
        }
        boolean handled = TerminalMissionRegistry.provider(chapterId)
                .map(provider -> provider.handleAction(player, missionId, actionId))
                .orElse(false);
        if (!handled) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Mission action rejected."), true);
        }
    }
}
