package com.knoxhack.echoashfallprotocol.client.renderer;

import com.knoxhack.echoashfallprotocol.entity.EchoDrone;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class EchoDroneRenderer extends MobRenderer<EchoDrone, DroneRenderState, DroneModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("echoashfallprotocol", "textures/entity/echo_drone.png");

    public EchoDroneRenderer(EntityRendererProvider.Context context) {
        super(context, new DroneModel(context.bakeLayer(DroneModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    public DroneRenderState createRenderState() {
        return new DroneRenderState();
    }

    @Override
    public void extractRenderState(EchoDrone entity, DroneRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public Identifier getTextureLocation(DroneRenderState state) {
        return TEXTURE;
    }
}
