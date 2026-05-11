package com.knoxhack.echocore.api.mission;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface IRewardView {
    Identifier id();

    MissionRewardClaimMode claimMode();

    ItemStack stack();

    String label();

    String detail();

    boolean claimable();

    boolean claimed();

    Map<String, String> metadata();
}
