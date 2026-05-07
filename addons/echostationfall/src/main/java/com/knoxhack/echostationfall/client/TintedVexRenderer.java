package com.knoxhack.echostationfall.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Vex;

public class TintedVexRenderer extends VexRenderer {
    private final Identifier texture;
    private final int tint;
    private final float scale;
    private final float shadow;

    public TintedVexRenderer(EntityRendererProvider.Context context, Identifier texture, int tint, float scale, float shadow) {
        super(context);
        this.texture = texture;
        this.tint = tint;
        this.scale = scale;
        this.shadow = shadow;
    }

    @Override
    public Identifier getTextureLocation(VexRenderState state) {
        return texture;
    }

    @Override
    protected int getModelTint(VexRenderState state) {
        return tint;
    }

    @Override
    protected void scale(VexRenderState state, PoseStack poseStack) {
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public void extractRenderState(Vex entity, VexRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        this.shadowRadius = shadow;
    }
}
