package com.knoxhack.echoashfallprotocol.block.entity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MachineInventory extends SimpleContainer {
    private final Runnable onChanged;

    public MachineInventory(int size, Runnable onChanged) {
        super(size);
        this.onChanged = onChanged;
    }

    public ItemStack getStackInSlot(int slot) {
        return getItem(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    public void serialize(ValueOutput output) {
        storeAsItemList(output.list("items", ItemStack.CODEC));
    }

    public void deserialize(ValueInput input) {
        fromItemList(input.listOrEmpty("items", ItemStack.CODEC));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChanged.run();
    }
}
