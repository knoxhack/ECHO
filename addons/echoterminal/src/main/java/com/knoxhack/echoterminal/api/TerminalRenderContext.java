package com.knoxhack.echoterminal.api;

import com.knoxhack.echoterminal.network.TerminalActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public record TerminalRenderContext(
        Minecraft minecraft,
        Player player,
        int screenWidth,
        int screenHeight,
        int contentX,
        int contentY,
        int contentWidth,
        int contentHeight,
        int scrollY) {
    public void sendAction(Identifier tabId, Identifier actionId, String payload) {
        ClientPacketDistributor.sendToServer(new TerminalActionPacket(tabId, actionId, payload == null ? "" : payload));
    }
}
