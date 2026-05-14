package com.knoxhack.signalos.client.api;

import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.knoxhack.signalos.api.SignalOsApp;
import com.knoxhack.signalos.network.SignalOsActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public record SignalOsAppRenderContext(
        SignalOsApp app,
        int x,
        int y,
        int width,
        int height) {
    public Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public void sendAction(Identifier pageId, Identifier actionId, String payload) {
        EchoNetClientActions.sendServerboundAction(new SignalOsActionPacket(pageId, actionId, payload == null ? "" : payload));
    }
}
