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

public class DroneModel extends EntityModel<DroneRenderState> implements RenderCorePartProvider {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "drone"), "main");

    private final ModelPart root;
    private final ModelPart chassis;
    private final ModelPart frontDisplay;
    private final ModelPart rearPanel;
    private final ModelPart topPlate;
    private final ModelPart bottomDisplay;
    private final ModelPart leftEngine;
    private final ModelPart rightEngine;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart hoverCoreFl;
    private final ModelPart hoverCoreFr;
    private final ModelPart hoverCoreBl;
    private final ModelPart hoverCoreBr;
    private final ModelPart antennaLeft;
    private final ModelPart antennaRight;
    private final Map<String, ModelPart> renderCoreParts;

    public DroneModel(ModelPart root) {
        super(root);
        this.root = root;
        this.chassis = root.getChild("chassis");
        this.frontDisplay = root.getChild("front_display");
        this.rearPanel = root.getChild("rear_panel");
        this.topPlate = root.getChild("top_plate");
        this.bottomDisplay = root.getChild("bottom_display");
        this.leftEngine = root.getChild("left_engine");
        this.rightEngine = root.getChild("right_engine");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.hoverCoreFl = root.getChild("hover_core_fl");
        this.hoverCoreFr = root.getChild("hover_core_fr");
        this.hoverCoreBl = root.getChild("hover_core_bl");
        this.hoverCoreBr = root.getChild("hover_core_br");
        this.antennaLeft = root.getChild("antenna_left");
        this.antennaRight = root.getChild("antenna_right");
        this.renderCoreParts = NamedModelParts.builder()
                .put("root", root)
                .put("chassis", chassis)
                .put("body", chassis)
                .put("torso", chassis)
                .put("lens", frontDisplay)
                .put("eyes", frontDisplay)
                .put("scanner", frontDisplay)
                .put("left_rotor", leftEngine)
                .put("right_rotor", rightEngine)
                .put("rear_rotor", rearPanel)
                .put("tool_arm", bottomDisplay)
                .put("core", bottomDisplay)
                .put("trail", rearPanel)
                .put("exhaust", rearPanel)
                .put("ground", bottomDisplay)
                .build()
                .asMap();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("chassis",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -2.0F, -3.5F, 10.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        root.addOrReplaceChild("top_plate",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-4.4F, -0.55F, -3.0F, 8.8F, 1.1F, 6.0F)
                        .texOffs(30, 12).addBox(-2.2F, -0.95F, -1.8F, 4.4F, 0.75F, 3.6F),
                PartPose.offset(0.0F, 15.55F, 0.0F));

        root.addOrReplaceChild("front_display",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-4.75F, -2.0F, -0.55F, 9.5F, 4.0F, 1.1F)
                        .texOffs(22, 24).addBox(-3.05F, -1.35F, -0.95F, 6.1F, 2.7F, 0.55F),
                PartPose.offset(0.0F, 18.0F, -4.0F));

        root.addOrReplaceChild("rear_panel",
                CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-4.4F, -1.75F, -0.5F, 8.8F, 3.5F, 1.0F)
                        .texOffs(22, 34).addBox(-2.8F, -0.9F, 0.2F, 5.6F, 1.8F, 0.65F),
                PartPose.offset(0.0F, 18.05F, 4.0F));

        root.addOrReplaceChild("bottom_display",
                CubeListBuilder.create()
                        .texOffs(20, 44).addBox(-3.35F, -0.55F, -2.5F, 6.7F, 1.1F, 5.0F)
                        .texOffs(44, 44).addBox(-2.35F, -0.9F, -1.45F, 4.7F, 0.55F, 2.9F),
                PartPose.offset(0.0F, 20.35F, 0.0F));

        addEngine(root, "left_engine", 5.7F);
        addEngine(root, "right_engine", -5.7F);
        addWing(root, "left_wing", 7.25F, false);
        addWing(root, "right_wing", -7.25F, true);
        addHoverCore(root, "hover_core_fl", -3.25F, -2.85F);
        addHoverCore(root, "hover_core_fr", 3.25F, -2.85F);
        addHoverCore(root, "hover_core_bl", -3.25F, 2.85F);
        addHoverCore(root, "hover_core_br", 3.25F, 2.85F);
        addAntenna(root, "antenna_left", -3.25F);
        addAntenna(root, "antenna_right", 3.25F);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addEngine(PartDefinition root, String name, float x) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(36, 0).addBox(-1.35F, -2.15F, -3.1F, 2.7F, 4.3F, 6.2F)
                        .texOffs(48, 16).addBox(-1.1F, 0.95F, -2.2F, 2.2F, 1.1F, 4.4F),
                PartPose.offset(x, 18.0F, 0.0F));
    }

    private static void addWing(PartDefinition root, String name, float x, boolean mirrored) {
        float zRot = mirrored ? -0.08F : 0.08F;
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-0.45F, -0.8F, -3.85F, 0.9F, 1.6F, 7.7F)
                        .texOffs(16, 48).addBox(-0.55F, -1.45F, 2.05F, 1.1F, 1.1F, 1.8F),
                PartPose.offsetAndRotation(x, 18.1F, 0.0F, 0.0F, 0.0F, zRot));
    }

    private static void addHoverCore(PartDefinition root, String name, float x, float z) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(40, 32).addBox(-1.45F, -0.55F, -1.45F, 2.9F, 1.4F, 2.9F)
                        .texOffs(52, 32).addBox(-1.0F, -0.9F, -1.0F, 2.0F, 0.55F, 2.0F),
                PartPose.offset(x, 20.35F, z));
    }

    private static void addAntenna(PartDefinition root, String name, float x) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(56, 0).addBox(-0.35F, -2.7F, -0.35F, 0.7F, 2.7F, 0.7F)
                        .texOffs(56, 8).addBox(-0.45F, -3.25F, -0.45F, 0.9F, 0.65F, 0.9F),
                PartPose.offset(x, 15.2F, -2.45F));
    }

    @Override
    public void setupAnim(DroneRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float hover = (float) Math.sin(age * 0.18F) * 0.18F;
        float pulse = 1.0F + 0.08F * (0.5F + 0.5F * (float) Math.sin(age * 0.35F));

        root.y = hover;
        root.yRot = state.yRot * ((float) Math.PI / 180F);
        root.xRot = state.xRot * ((float) Math.PI / 180F);

        chassis.xRot = hover * 0.045F;
        chassis.zRot = (float) Math.sin(age * 0.12F) * 0.025F;
        frontDisplay.zRot = (float) Math.sin(age * 0.1F) * 0.015F;
        rearPanel.zRot = -frontDisplay.zRot;
        topPlate.yRot = (float) Math.sin(age * 0.08F) * 0.05F;
        bottomDisplay.yScale = pulse;

        animateEngine(leftEngine, age, 1.0F);
        animateEngine(rightEngine, age, -1.0F);
        animateWing(leftWing, age, 1.0F);
        animateWing(rightWing, age, -1.0F);
        animateHoverCore(hoverCoreFl, age, 0.0F);
        animateHoverCore(hoverCoreFr, age, 0.6F);
        animateHoverCore(hoverCoreBl, age, 1.2F);
        animateHoverCore(hoverCoreBr, age, 1.8F);

        antennaLeft.xRot = (float) Math.sin(age * 0.16F) * 0.05F;
        antennaRight.xRot = (float) Math.sin(age * 0.16F + 0.7F) * 0.05F;
    }

    private static void animateEngine(ModelPart part, float age, float direction) {
        part.yRot = direction * 0.025F * (float) Math.sin(age * 0.22F);
        part.zRot = direction * 0.018F * (float) Math.cos(age * 0.18F);
    }

    private static void animateWing(ModelPart part, float age, float direction) {
        part.zRot = direction * (0.08F + 0.025F * (float) Math.sin(age * 0.24F));
    }

    private static void animateHoverCore(ModelPart part, float age, float offset) {
        float pulse = 1.0F + 0.08F * (float) Math.sin(age * 0.42F + offset);
        part.xScale = pulse;
        part.zScale = pulse;
    }

    @Override
    public Map<String, ModelPart> renderCoreParts() {
        return renderCoreParts;
    }
}
