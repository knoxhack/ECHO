package com.knoxhack.echoorbitalremnants.client;

import com.knoxhack.echorendercore.client.RenderCoreEntityVisuals;
import com.knoxhack.echorendercore.client.RenderCoreVisualLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class OrbitalRenderCoreZombieRenderer extends TintedZombieRenderer {
    private final Identifier profileId;
    private final Identifier fallbackTexture;

    public OrbitalRenderCoreZombieRenderer(EntityRendererProvider.Context context, Identifier fallbackTexture,
          Identifier profileId, int tint, float scale, float shadow) {
        super(context, fallbackTexture, tint, scale, shadow);
        this.profileId = profileId;
        this.fallbackTexture = fallbackTexture;
        addLayer(new RenderCoreVisualLayer<>(this));
    }

    @Override
    public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        RenderCoreEntityVisuals.attach(entity, state, profileId, fallbackTexture, partialTicks);
    }
}
