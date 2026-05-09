package com.knoxhack.signalos.menu;

import com.knoxhack.signalos.registry.ModBlocks;
import com.knoxhack.signalos.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class SignalOsTerminalMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final boolean remoteAccess;

    public SignalOsTerminalMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    public SignalOsTerminalMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL, true);
    }

    public SignalOsTerminalMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        this(containerId, playerInventory, access, false);
    }

    private SignalOsTerminalMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, boolean remoteAccess) {
        super(ModMenus.TERMINAL.get(), containerId);
        this.access = access == null ? ContainerLevelAccess.NULL : access;
        this.remoteAccess = remoteAccess;
    }

    @Override
    public boolean stillValid(Player player) {
        return remoteAccess || stillValid(access, player, ModBlocks.TERMINAL.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
