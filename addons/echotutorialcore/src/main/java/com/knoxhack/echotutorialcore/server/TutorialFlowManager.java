package com.knoxhack.echotutorialcore.server;

import com.knoxhack.echotutorialcore.api.trigger.TutorialFlow;
import com.knoxhack.echotutorialcore.data.TutorialCoreRegistries;
import com.knoxhack.echotutorialcore.data.TutorialPlayerData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class TutorialFlowManager {
    private TutorialFlowManager() {}

    public static void completeFlow(Player player, Identifier flowId) {
        if (player == null || flowId == null) return;
        TutorialPlayerData data = TutorialPlayerData.get(player);
        if (data.isFlowCompleted(flowId)) return;
        data.completeFlow(flowId);
        TutorialPlayerData.save(player, data);

        TutorialFlow flow = TutorialCoreRegistries.getFlow(flowId).orElse(null);
        if (flow != null && flow.unlockCards() != null) {
            for (Identifier cardId : flow.unlockCards()) {
                TutorialCardManager.unlockCard(player, cardId);
            }
        }
    }

    public static boolean isFlowCompleted(Player player, Identifier flowId) {
        if (player == null || flowId == null) return false;
        TutorialPlayerData data = TutorialPlayerData.get(player);
        return data.isFlowCompleted(flowId);
    }
}
