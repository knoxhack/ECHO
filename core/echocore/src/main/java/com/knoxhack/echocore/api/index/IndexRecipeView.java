package com.knoxhack.echocore.api.index;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record IndexRecipeView(
        Identifier id,
        Identifier categoryId,
        String title,
        ItemStack machine,
        List<IndexRecipeSlot> slots,
        List<String> notes,
        int processTicks,
        boolean locked,
        String sourceModId) {
    public IndexRecipeView {
        Objects.requireNonNull(id, "Index recipe id is required.");
        Objects.requireNonNull(categoryId, "Index recipe category id is required.");
        title = title == null || title.isBlank() ? id.getPath() : title.strip();
        machine = machine == null ? ItemStack.EMPTY : machine.copy();
        slots = slots == null ? List.of() : List.copyOf(slots);
        notes = notes == null ? List.of() : notes.stream()
                .filter(note -> note != null && !note.isBlank())
                .map(String::strip)
                .toList();
        processTicks = Math.max(0, processTicks);
        sourceModId = sourceModId == null || sourceModId.isBlank() ? id.getNamespace() : sourceModId.strip();
    }

    public boolean outputs(Item item) {
        return contains(item, IndexSlotRole.OUTPUT);
    }

    public boolean uses(Item item) {
        return contains(item, IndexSlotRole.INPUT)
                || contains(item, IndexSlotRole.CATALYST)
                || contains(item, IndexSlotRole.MACHINE);
    }

    public Set<Item> itemsForRole(IndexSlotRole role) {
        Set<Item> items = new LinkedHashSet<>();
        if (role == IndexSlotRole.MACHINE && !machine.isEmpty()) {
            items.add(machine.getItem());
        }
        for (IndexRecipeSlot slot : slots) {
            if (slot.role() != role) {
                continue;
            }
            for (ItemStack stack : slot.stacks()) {
                if (!stack.isEmpty()) {
                    items.add(stack.getItem());
                }
            }
        }
        return Set.copyOf(items);
    }

    private boolean contains(Item item, IndexSlotRole role) {
        if (item == null) {
            return false;
        }
        if (role == IndexSlotRole.MACHINE && !machine.isEmpty() && machine.is(item)) {
            return true;
        }
        for (IndexRecipeSlot slot : slots) {
            if (slot.role() != role) {
                continue;
            }
            for (ItemStack stack : slot.stacks()) {
                if (!stack.isEmpty() && stack.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
