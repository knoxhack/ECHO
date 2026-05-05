package com.knoxhack.echoashfallprotocol.client.renderer;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.NexusPressureMobEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class NexusPressureMobRenderer extends HumanoidMobRenderer<NexusPressureMobEntity, NexusPressureMobRenderer.State, HumanoidModel<NexusPressureMobRenderer.State>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "textures/entity/glowing_ghoul.png");

    public NexusPressureMobRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.55F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NexusPressureMobEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.tint = entity.profile().accentColor();
    }

    @Override
    protected int getModelTint(State state) {
        return state.tint;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    public static class State extends HumanoidRenderState {
        private int tint = 0xFFFFFFFF;
    }
}
