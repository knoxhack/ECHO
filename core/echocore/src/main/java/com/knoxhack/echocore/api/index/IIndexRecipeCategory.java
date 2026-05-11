package com.knoxhack.echocore.api.index;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface IIndexRecipeCategory {
    Identifier id();

    String title();

    ItemStack icon();

    int accentColor();

    int order();
}
