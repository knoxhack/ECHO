package com.knoxhack.echocore.api.index;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record IndexRecipeSlot(IndexSlotRole role, List<ItemStack> stacks, String label) {
    public IndexRecipeSlot {
        role = role == null ? IndexSlotRole.INFO : role;
        stacks = stacks == null ? List.of() : stacks.stream()
                .map(stack -> stack == null ? ItemStack.EMPTY : stack.copy())
                .toList();
        label = label == null ? "" : label.strip();
    }

    public static IndexRecipeSlot input(ItemStack stack) {
        return of(IndexSlotRole.INPUT, stack, "");
    }

    public static IndexRecipeSlot inputs(List<ItemStack> stacks) {
        return new IndexRecipeSlot(IndexSlotRole.INPUT, stacks, "");
    }

    public static IndexRecipeSlot output(ItemStack stack) {
        return of(IndexSlotRole.OUTPUT, stack, "");
    }

    public static IndexRecipeSlot catalyst(ItemStack stack, String label) {
        return of(IndexSlotRole.CATALYST, stack, label == null || label.isBlank() ? "Catalyst" : label);
    }

    public static IndexRecipeSlot machine(ItemStack stack) {
        return of(IndexSlotRole.MACHINE, stack, "Machine");
    }

    public static IndexRecipeSlot info(String label) {
        return new IndexRecipeSlot(IndexSlotRole.INFO, List.of(), label);
    }

    public static IndexRecipeSlot of(IndexSlotRole role, ItemStack stack, String label) {
        return new IndexRecipeSlot(role, List.of(stack == null ? ItemStack.EMPTY : stack), label);
    }
}
