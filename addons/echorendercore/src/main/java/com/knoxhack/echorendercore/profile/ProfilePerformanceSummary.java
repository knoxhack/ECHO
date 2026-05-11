package com.knoxhack.echorendercore.profile;

import net.minecraft.resources.Identifier;

public record ProfilePerformanceSummary(
        Identifier profileId,
        int layerCount,
        int maskedLayerCount,
        int animationClipCount,
        int animationTrackCount,
        int emitterCount,
        int estimatedMaxEmitterBurst,
        float estimatedEmitterRate
) {
}
