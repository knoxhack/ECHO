package com.knoxhack.echoterminal.menu;

import com.knoxhack.echoterminal.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class EchoTerminalMenu extends AbstractContainerMenu {
    public EchoTerminalMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    public EchoTerminalMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.ECHO_TERMINAL.get(), containerId);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
