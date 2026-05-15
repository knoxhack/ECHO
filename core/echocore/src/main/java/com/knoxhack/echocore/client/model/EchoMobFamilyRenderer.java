package com.knoxhack.echocore.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

public class EchoMobFamilyRenderer<T extends Mob>
        extends MobRenderer<T, EchoMobRenderState, net.minecraft.client.model.EntityModel<EchoMobRenderState>> {
    private final Identifier texture;
    private final float scale;
    private final float shadow;

    public EchoMobFamilyRenderer(EntityRendererProvider.Context context, String modid, String entityName,
            EchoMobFamily family, float scale, float shadow) {
        super(context, EchoMobModelFactory.create(context, family), shadow);
        this.texture = texture(modid, entityName);
        this.scale = scale;
        this.shadow = shadow;
    }

    @Override
    public EchoMobRenderState createRenderState() {
        return new EchoMobRenderState();
    }

    @Override
    public void extractRenderState(T entity, EchoMobRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        this.shadowRadius = shadow;
        state.tint = tint(entity, state, partialTick);
    }

    @Override
    public Identifier getTextureLocation(EchoMobRenderState state) {
        return texture;
    }

    @Override
    protected int getModelTint(EchoMobRenderState state) {
        return state.tint;
    }

    @Override
    protected void scale(EchoMobRenderState state, PoseStack poseStack) {
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
    }

    protected int tint(T entity, EchoMobRenderState state, float partialTick) {
        return 0xFFFFFFFF;
    }

    protected static Identifier texture(String modid, String entityName) {
        return Identifier.fromNamespaceAndPath(modid, "textures/entity/rendercore_echo_mobs/" + entityName + ".png");
    }
}
