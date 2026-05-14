package com.knoxhack.echotutorialcore.client;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import com.knoxhack.echotutorialcore.network.ShowTutorialHintPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class TutorialClientDisplay {
    private TutorialClientDisplay() {}

    public static void showHint(ShowTutorialHintPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.sendSystemMessage(
                Component.literal("[ECHO-7] ").append(packet.title())
                        .append(": ").append(packet.message()));
    }

    public static void showCardToast(net.minecraft.resources.Identifier cardId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.sendSystemMessage(
                Component.literal("[ECHO Terminal] Guide card unlocked: ").append(cardId.toString()));
    }

    public static void showUnlockCard(net.minecraft.resources.Identifier cardId) {
        showCardToast(cardId);
    }
}
