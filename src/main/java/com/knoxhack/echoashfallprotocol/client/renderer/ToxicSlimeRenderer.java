package com.knoxhack.echoashfallprotocol.client.renderer;

import com.knoxhack.echoashfallprotocol.entity.ToxicSlime;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

public class ToxicSlimeRenderer extends MobRenderer<ToxicSlime, SlimeRenderState, SlimeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("echoashfallprotocol", "textures/entity/toxic_slime.png");

    public ToxicSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.35f);
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(ToxicSlime entity, SlimeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.size = 1;
        state.squish = 0.0F;
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        poseStack.scale(0.78F, 0.78F, 0.78F);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }
}
