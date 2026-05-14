package com.knoxhack.echoorbitalremnants.client;

import com.knoxhack.echorendercore.client.RenderCoreEntityVisuals;
import com.knoxhack.echorendercore.client.RenderCoreVisualLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Vex;

public class OrbitalRenderCoreVexRenderer extends TintedVexRenderer {
    private final Identifier profileId;
    private final Identifier fallbackTexture;

    public OrbitalRenderCoreVexRenderer(EntityRendererProvider.Context context, Identifier fallbackTexture,
          Identifier profileId, int tint, float scale, float shadow) {
        super(context, fallbackTexture, tint, scale, shadow);
        this.profileId = profileId;
        this.fallbackTexture = fallbackTexture;
        addLayer(new RenderCoreVisualLayer<>(this));
    }

    @Override
    public void extractRenderState(Vex entity, VexRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        RenderCoreEntityVisuals.attach(entity, state, profileId, fallbackTexture, partialTicks);
    }
}
