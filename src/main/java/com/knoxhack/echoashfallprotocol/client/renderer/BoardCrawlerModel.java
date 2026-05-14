package com.knoxhack.echoashfallprotocol.client.renderer;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echorendercore.client.NamedModelParts;
import com.knoxhack.echorendercore.client.RenderCorePartProvider;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class BoardCrawlerModel extends EntityModel<AshfallLivingRenderState> implements RenderCorePartProvider {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "board_crawler"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart frontClaws;
    private final ModelPart rearLegs;
    private final ModelPart acidSacs;
    private final ModelPart mandibles;
    private final ModelPart eyes;
    private final Map<String, ModelPart> renderCoreParts;

    public BoardCrawlerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.frontClaws = root.getChild("front_claws");
        this.rearLegs = root.getChild("rear_legs");
        this.acidSacs = body.getChild("acid_sacs");
        this.mandibles = head.getChild("mandibles");
        this.eyes = head.getChild("eyes");
        this.renderCoreParts = NamedModelParts.builder()
                .put("root", root)
                .put("body", body)
                .put("torso", body)
                .put("head", head)
                .put("front_claws", frontClaws)
                .put("rear_legs", rearLegs)
                .put("acid_sacs", acidSacs)
                .put("mandibles", mandibles)
                .put("eyes", eyes)
                .put("core", acidSacs)
                .put("scanner", eyes)
                .put("trail", rearLegs)
                .put("exhaust", acidSacs)
                .put("ground", body)
                .build()
                .asMap();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-5.0F, -2.6F, -4.5F, 10.0F, 5.2F, 9.0F)
                        .texOffs(30, 20).addBox(-3.8F, -3.2F, -1.5F, 7.6F, 1.4F, 6.0F),
                PartPose.offset(0.0F, 19.0F, 1.0F));
        body.addOrReplaceChild("acid_sacs",
                CubeListBuilder.create()
                        .texOffs(40, 0).addBox(-4.0F, -4.4F, 0.5F, 2.8F, 2.8F, 3.0F)
                        .mirror().addBox(1.2F, -4.4F, 0.5F, 2.8F, 2.8F, 3.0F),
                PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -2.5F, -5.5F, 8.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, 18.5F, -4.4F));
        head.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(0, 54).addBox(-3.0F, -1.2F, -5.85F, 6.0F, 1.1F, 0.5F),
                PartPose.ZERO);
        head.addOrReplaceChild("mandibles",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-5.0F, 0.4F, -6.4F, 3.0F, 1.2F, 4.0F)
                        .mirror().addBox(2.0F, 0.4F, -6.4F, 3.0F, 1.2F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("front_claws",
                CubeListBuilder.create()
                        .texOffs(0, 36).addBox(-8.0F, -0.8F, -2.0F, 5.0F, 1.6F, 2.0F)
                        .mirror().addBox(3.0F, -0.8F, -2.0F, 5.0F, 1.6F, 2.0F),
                PartPose.offset(0.0F, 20.0F, -4.2F));
        root.addOrReplaceChild("rear_legs",
                CubeListBuilder.create()
                        .texOffs(20, 38).addBox(-7.5F, -0.8F, 0.0F, 4.5F, 1.6F, 4.0F)
                        .mirror().addBox(3.0F, -0.8F, 0.0F, 4.5F, 1.6F, 4.0F),
                PartPose.offset(0.0F, 20.2F, 3.6F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AshfallLivingRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        body.y = Mth.sin(age * 0.2F) * 0.08F;
        head.yRot = state.yRot * ((float) Math.PI / 180F) * 0.7F;
        frontClaws.zRot = Mth.sin(age * 0.24F) * 0.12F;
        rearLegs.zRot = -frontClaws.zRot;
        acidSacs.yScale = 1.0F + Mth.sin(age * 0.28F) * 0.08F;
        mandibles.yScale = 1.0F + Mth.sin(age * 0.32F) * 0.05F;
        eyes.xScale = 1.0F + Mth.sin(age * 0.22F) * 0.08F;
    }

    @Override
    public Map<String, ModelPart> renderCoreParts() {
        return renderCoreParts;
    }
}
