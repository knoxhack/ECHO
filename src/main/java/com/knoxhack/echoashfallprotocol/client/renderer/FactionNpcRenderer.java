package com.knoxhack.echoashfallprotocol.client.renderer;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.faction.FactionNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class FactionNpcRenderer extends HumanoidMobRenderer<FactionNpcEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "textures/entity/crash_survivor.png");

    public FactionNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }
}
