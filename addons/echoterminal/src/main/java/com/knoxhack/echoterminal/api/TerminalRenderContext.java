package com.knoxhack.echoterminal.api;

import com.knoxhack.echoterminal.network.TerminalActionPacket;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
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
        int scrollY,
        Consumer<Identifier> tabNavigator,
        Predicate<Identifier> tabAvailability) {
    public TerminalRenderContext {
        tabNavigator = tabNavigator == null ? ignored -> { } : tabNavigator;
        tabAvailability = tabAvailability == null ? ignored -> false : tabAvailability;
    }

    public void sendAction(Identifier tabId, Identifier actionId, String payload) {
        playCommandSound();
        ClientPacketDistributor.sendToServer(new TerminalActionPacket(tabId, actionId, payload == null ? "" : payload));
    }

    public boolean canNavigateToTab(Identifier tabId) {
        return tabId != null && tabAvailability.test(tabId);
    }

    public void navigateToTab(Identifier tabId) {
        if (canNavigateToTab(tabId)) {
            playCommandSound();
            tabNavigator.accept(tabId);
        } else {
            playRejectedSound();
        }
    }

    public void playCommandSound() {
        if (minecraft == null) {
            return;
        }
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.25F, 0.45F));
    }

    public void playRejectedSound() {
        if (minecraft == null) {
            return;
        }
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.7F, 0.45F));
    }
}
