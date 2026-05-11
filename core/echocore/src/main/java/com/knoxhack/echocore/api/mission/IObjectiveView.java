package com.knoxhack.echocore.api.mission;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface IObjectiveView {
    Identifier id();

    MissionObjectiveType type();

    String label();

    String detail();

    ItemStack icon();

    int progress();

    int required();

    boolean complete();

    boolean hidden();

    Map<String, String> criteria();
}
