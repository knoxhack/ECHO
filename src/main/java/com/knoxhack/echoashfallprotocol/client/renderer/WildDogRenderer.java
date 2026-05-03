package com.knoxhack.echoashfallprotocol.client.renderer;

import com.knoxhack.echoashfallprotocol.entity.WildDog;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;

public class WildDogRenderer extends MobRenderer<WildDog, WolfRenderState, AdultWolfModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("echoashfallprotocol", "textures/entity/wild_dog.png");

    public WildDogRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultWolfModel(context.bakeLayer(ModelLayers.WOLF)), 0.5f);
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    @Override
    public void extractRenderState(WildDog entity, WolfRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState state) {
        return TEXTURE;
    }
}
