package com.knoxhack.echocore.client.model;

import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class EchoMobModels {
    private EchoMobModels() {
    }

    public static LayerDefinition createHumanoidLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.18F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(0, 52).addBox(-3.0F, -5.1F, -4.55F, 6.0F, 1.3F, 0.55F),
                PartPose.ZERO);
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.26F)),
                PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.5F, 0.0F, -2.4F, 9.0F, 12.0F, 4.8F)
                        .texOffs(16, 36).addBox(-5.0F, 1.3F, -2.8F, 10.0F, 4.5F, 5.6F, new CubeDeformation(0.08F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("core",
                CubeListBuilder.create().texOffs(44, 36).addBox(-2.0F, 3.4F, -3.15F, 4.0F, 4.0F, 0.8F, new CubeDeformation(0.05F)),
                PartPose.ZERO);
        PartDefinition pack = body.addOrReplaceChild("pack",
                CubeListBuilder.create()
                        .texOffs(48, 18).addBox(-3.5F, 2.0F, 2.25F, 7.0F, 8.0F, 2.4F)
                        .texOffs(48, 29).addBox(-1.1F, 0.6F, 3.1F, 2.2F, 2.0F, 1.0F),
                PartPose.ZERO);
        pack.addOrReplaceChild("scanner",
                CubeListBuilder.create().texOffs(58, 50).addBox(-0.45F, -3.0F, 3.4F, 0.9F, 3.0F, 0.9F),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.1F, -2.0F, -2.0F, 4.1F, 12.0F, 4.0F)
                        .texOffs(40, 48).addBox(-3.45F, 6.2F, -2.25F, 4.5F, 3.5F, 4.5F, new CubeDeformation(0.05F)),
                PartPose.offset(-5.4F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.1F, 12.0F, 4.0F)
                        .texOffs(40, 48).mirror().addBox(-1.05F, 6.2F, -2.25F, 4.5F, 3.5F, 4.5F, new CubeDeformation(0.05F)),
                PartPose.offset(5.4F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .texOffs(0, 36).addBox(-2.35F, 7.0F, -2.25F, 4.45F, 4.5F, 4.5F, new CubeDeformation(0.05F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).mirror().addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .texOffs(0, 36).mirror().addBox(-2.1F, 7.0F, -2.25F, 4.45F, 4.5F, 4.5F, new CubeDeformation(0.05F)),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createSurvivorNpcLayer() {
        return createHumanoidLayer();
    }

    public static LayerDefinition createStationSuitLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.2F, -8.2F, -4.2F, 8.4F, 8.4F, 8.4F)
                        .texOffs(34, 0).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.18F)),
                PartPose.ZERO);
        head.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(0, 118).addBox(-3.1F, -5.4F, -5.25F, 6.2F, 1.4F, 0.6F),
                PartPose.ZERO);
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(34, 0).addBox(-5.1F, -9.1F, -5.1F, 10.2F, 10.2F, 10.2F, new CubeDeformation(0.24F)),
                PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 22).addBox(-5.2F, 0.0F, -3.0F, 10.4F, 12.5F, 6.0F)
                        .texOffs(50, 24).addBox(-6.1F, 1.2F, -3.5F, 12.2F, 4.5F, 7.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO);
        body.addOrReplaceChild("core",
                CubeListBuilder.create().texOffs(88, 36).addBox(-2.2F, 3.3F, -3.8F, 4.4F, 4.2F, 0.9F, new CubeDeformation(0.06F)),
                PartPose.ZERO);
        PartDefinition pack = body.addOrReplaceChild("pack",
                CubeListBuilder.create()
                        .texOffs(76, 0).addBox(-4.2F, 2.0F, 2.7F, 8.4F, 9.2F, 2.8F)
                        .texOffs(102, 0).addBox(-5.6F, 2.8F, 3.0F, 1.8F, 7.6F, 2.0F)
                        .mirror().addBox(3.8F, 2.8F, 3.0F, 1.8F, 7.6F, 2.0F),
                PartPose.ZERO);
        pack.addOrReplaceChild("scanner",
                CubeListBuilder.create().texOffs(112, 18).addBox(-0.55F, -3.2F, 3.8F, 1.1F, 3.3F, 1.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-3.7F, -2.0F, -2.45F, 4.8F, 12.5F, 4.9F)
                        .texOffs(38, 50).addBox(-4.05F, 6.3F, -2.7F, 5.3F, 3.8F, 5.4F, new CubeDeformation(0.06F)),
                PartPose.offset(-6.2F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 42).mirror().addBox(-1.1F, -2.0F, -2.45F, 4.8F, 12.5F, 4.9F)
                        .texOffs(38, 50).mirror().addBox(-1.25F, 6.3F, -2.7F, 5.3F, 3.8F, 5.4F, new CubeDeformation(0.06F)),
                PartPose.offset(6.2F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(18, 42).addBox(-2.5F, 0.0F, -2.35F, 4.8F, 12.0F, 4.7F)
                        .texOffs(72, 50).addBox(-2.8F, 7.3F, -2.65F, 5.4F, 4.5F, 5.3F, new CubeDeformation(0.05F)),
                PartPose.offset(-2.35F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(18, 42).mirror().addBox(-2.3F, 0.0F, -2.35F, 4.8F, 12.0F, 4.7F)
                        .texOffs(72, 50).mirror().addBox(-2.6F, 7.3F, -2.65F, 5.4F, 4.5F, 5.3F, new CubeDeformation(0.05F)),
                PartPose.offset(2.35F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition createHeavyBossLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(40, 0).addBox(-5.8F, -10.8F, -5.8F, 11.6F, 11.6F, 11.6F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        head.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(0, 118).addBox(-3.6F, -6.3F, -5.65F, 7.2F, 1.6F, 0.75F),
                PartPose.ZERO);
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(40, 0).addBox(-5.9F, -10.9F, -5.9F, 11.8F, 11.8F, 11.8F, new CubeDeformation(0.32F)),
                PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 24).addBox(-6.5F, 0.0F, -3.6F, 13.0F, 15.0F, 7.2F)
                        .texOffs(72, 24).addBox(-8.0F, 1.0F, -4.2F, 16.0F, 6.0F, 8.4F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        body.addOrReplaceChild("reactor",
                CubeListBuilder.create()
                        .texOffs(88, 44).addBox(-3.0F, 4.0F, -4.45F, 6.0F, 6.0F, 1.2F, new CubeDeformation(0.1F))
                        .texOffs(104, 44).addBox(-1.8F, 5.2F, -4.95F, 3.6F, 3.6F, 0.8F, new CubeDeformation(0.08F)),
                PartPose.ZERO);
        body.addOrReplaceChild("shoulder_rig",
                CubeListBuilder.create()
                        .texOffs(72, 0).addBox(-10.0F, -1.0F, -4.6F, 4.0F, 5.5F, 9.2F)
                        .mirror().addBox(6.0F, -1.0F, -4.6F, 4.0F, 5.5F, 9.2F)
                        .texOffs(104, 0).addBox(-2.5F, -4.0F, 2.7F, 5.0F, 4.0F, 3.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("back_spines",
                CubeListBuilder.create()
                        .texOffs(112, 16).addBox(-0.7F, -2.5F, 3.4F, 1.4F, 5.0F, 5.0F)
                        .addBox(-4.6F, 3.0F, 3.4F, 1.4F, 5.0F, 4.0F)
                        .mirror().addBox(3.2F, 3.0F, 3.4F, 1.4F, 5.0F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(56, 48).addBox(-4.2F, -2.0F, -2.8F, 5.2F, 15.5F, 5.6F)
                        .texOffs(80, 64).addBox(-4.9F, 8.0F, -3.4F, 6.0F, 5.5F, 6.8F, new CubeDeformation(0.12F)),
                PartPose.offset(-7.2F, 0.2F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(56, 48).mirror().addBox(-1.0F, -2.0F, -2.8F, 5.2F, 15.5F, 5.6F)
                        .texOffs(80, 64).mirror().addBox(-1.1F, 8.0F, -3.4F, 6.0F, 5.5F, 6.8F, new CubeDeformation(0.12F)),
                PartPose.offset(7.2F, 0.2F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-3.0F, 0.0F, -2.8F, 5.5F, 14.0F, 5.6F)
                        .texOffs(0, 64).addBox(-3.3F, 8.2F, -3.1F, 6.2F, 5.6F, 6.2F, new CubeDeformation(0.08F)),
                PartPose.offset(-3.2F, 10.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 32).mirror().addBox(-2.5F, 0.0F, -2.8F, 5.5F, 14.0F, 5.6F)
                        .texOffs(0, 64).mirror().addBox(-2.9F, 8.2F, -3.1F, 6.2F, 5.6F, 6.2F, new CubeDeformation(0.08F)),
                PartPose.offset(3.2F, 10.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition createDroneLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("chassis",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.0F, -3.5F, 10.0F, 4.0F, 7.0F),
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
        addDroneEngine(root, "left_engine", 5.7F);
        addDroneEngine(root, "right_engine", -5.7F);
        addDroneWing(root, "left_wing", 7.25F, false);
        addDroneWing(root, "right_wing", -7.25F, true);
        addHoverCore(root, "hover_core_fl", -3.25F, -2.85F);
        addHoverCore(root, "hover_core_fr", 3.25F, -2.85F);
        addHoverCore(root, "hover_core_bl", -3.25F, 2.85F);
        addHoverCore(root, "hover_core_br", 3.25F, 2.85F);
        addAntenna(root, "antenna_left", -3.25F);
        addAntenna(root, "antenna_right", 3.25F);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createQuadrupedLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-4.8F, -4.0F, -8.0F, 9.6F, 8.0F, 16.0F)
                        .texOffs(34, 18).addBox(-5.2F, -5.0F, -3.0F, 10.4F, 2.0F, 9.0F),
                PartPose.offset(0.0F, 15.0F, 1.0F));
        body.addOrReplaceChild("spines",
                CubeListBuilder.create().texOffs(48, 0).addBox(-0.7F, -4.0F, -5.5F, 1.4F, 3.0F, 11.0F),
                PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -3.5F, -5.5F, 8.0F, 7.0F, 7.0F)
                        .texOffs(30, 0).addBox(-3.2F, -1.8F, -8.0F, 6.4F, 3.6F, 3.0F),
                PartPose.offset(0.0F, 13.8F, -7.2F));
        head.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(0, 54).addBox(-2.8F, -1.2F, -8.35F, 5.6F, 1.2F, 0.55F),
                PartPose.ZERO);
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(50, 44).addBox(-1.2F, -1.2F, 0.0F, 2.4F, 2.4F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 14.8F, 8.0F, -0.15F, 0.0F, 0.0F));
        addLeg(root, "left_front_leg", 3.2F, -4.7F);
        addLeg(root, "right_front_leg", -3.2F, -4.7F);
        addLeg(root, "left_back_leg", 3.2F, 5.2F);
        addLeg(root, "right_back_leg", -3.2F, 5.2F);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createCrawlerLayer() {
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

    public static LayerDefinition createWraithLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.6F, -7.6F, -4.6F, 9.2F, 8.0F, 9.2F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 6.5F, 0.0F));
        head.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(0, 54).addBox(-3.0F, -4.6F, -4.5F, 6.0F, 1.1F, 0.5F),
                PartPose.ZERO);
        PartDefinition veil = root.addOrReplaceChild("veil",
                CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-4.8F, -1.0F, -2.7F, 9.6F, 13.0F, 5.4F)
                        .texOffs(30, 18).addBox(-5.6F, 2.0F, -3.0F, 11.2F, 9.0F, 6.0F, new CubeDeformation(0.12F)),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        veil.addOrReplaceChild("core",
                CubeListBuilder.create().texOffs(44, 44).addBox(-2.0F, 3.0F, -3.35F, 4.0F, 4.0F, 0.8F),
                PartPose.ZERO);
        root.addOrReplaceChild("left_claw",
                CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, -1.2F, 3.0F, 10.0F, 2.4F),
                PartPose.offset(5.2F, 9.0F, -0.2F));
        root.addOrReplaceChild("right_claw",
                CubeListBuilder.create().texOffs(0, 40).mirror().addBox(-2.0F, -1.0F, -1.2F, 3.0F, 10.0F, 2.4F),
                PartPose.offset(-5.2F, 9.0F, -0.2F));
        root.addOrReplaceChild("smoke_trail",
                CubeListBuilder.create()
                        .texOffs(16, 40).addBox(-3.2F, 0.0F, -2.2F, 6.4F, 7.0F, 4.4F)
                        .texOffs(38, 50).addBox(-2.0F, 6.0F, -1.4F, 4.0F, 4.0F, 2.8F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createSlimeLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.5F, -5.5F, -5.5F, 11.0F, 11.0F, 11.0F)
                        .texOffs(0, 24).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.18F)),
                PartPose.offset(0.0F, 17.0F, 0.0F));
        body.addOrReplaceChild("inner_core",
                CubeListBuilder.create().texOffs(40, 0).addBox(-2.5F, -2.5F, -5.95F, 5.0F, 5.0F, 1.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("bubbles",
                CubeListBuilder.create()
                        .texOffs(42, 8).addBox(-4.8F, -3.5F, -4.9F, 2.0F, 2.0F, 0.8F)
                        .addBox(2.5F, 1.2F, -4.9F, 1.8F, 1.8F, 0.8F)
                        .addBox(-1.0F, 3.2F, -5.0F, 1.6F, 1.6F, 0.8F),
                PartPose.ZERO);
        body.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(0, 54).addBox(-3.3F, -1.5F, -6.25F, 6.6F, 1.2F, 0.55F),
                PartPose.ZERO);
        root.addOrReplaceChild("puddle",
                CubeListBuilder.create().texOffs(0, 50).addBox(-7.0F, -0.2F, -7.0F, 14.0F, 0.4F, 14.0F),
                PartPose.offset(0.0F, 23.2F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createIndustrialConstructLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "torso", 0, 0, -7.0F, -15.0F, -4.0F, 14.0F, 15.0F, 8.0F, 0.0F, 9.0F, 0.0F);
        part(root, "head", 42, 0, -5.0F, -8.0F, -5.0F, 10.0F, 8.0F, 10.0F, 0.0F, 2.0F, 0.0F);
        part(root, "core", 74, 0, -4.0F, -5.0F, -1.0F, 8.0F, 8.0F, 2.0F, 0.0F, 13.0F, -4.8F, 0.08F);
        part(root, "left_arm", 0, 34, 0.0F, -2.0F, -3.0F, 5.0F, 14.0F, 6.0F, 7.0F, 8.0F, 0.0F);
        part(root, "right_arm", 28, 34, -5.0F, -2.0F, -3.0F, 5.0F, 14.0F, 6.0F, -7.0F, 8.0F, 0.0F);
        part(root, "left_leg", 56, 34, -2.5F, 0.0F, -3.0F, 5.0F, 12.0F, 6.0F, 3.6F, 12.0F, 0.0F);
        part(root, "right_leg", 84, 34, -2.5F, 0.0F, -3.0F, 5.0F, 12.0F, 6.0F, -3.6F, 12.0F, 0.0F);
        part(root, "shoulder_left", 76, 66, -1.0F, -1.0F, -4.0F, 7.0F, 4.0F, 8.0F, 6.2F, 5.2F, 0.0F);
        part(root, "shoulder_right", 76, 66, -6.0F, -1.0F, -4.0F, 7.0F, 4.0F, 8.0F, -6.2F, 5.2F, 0.0F);
        part(root, "back_furnace", 58, 96, -5.0F, -11.0F, 0.0F, 10.0F, 12.0F, 4.0F, 0.0F, 10.0F, 4.4F);
        part(root, "antenna", 38, 66, -0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, 4.0F, 2.0F, 2.0F);
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition createRocketLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 24.0F, 8.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-3.0F, -32.0F, -3.0F, 6.0F, 8.0F, 6.0F)
                        .texOffs(24, 32).addBox(-2.0F, -36.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("engine",
                CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("fin_north",
                CubeListBuilder.create().texOffs(32, 10).addBox(-1.0F, -8.0F, -7.0F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("fin_south",
                CubeListBuilder.create().texOffs(42, 10).addBox(-1.0F, -8.0F, 4.0F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("fin_west",
                CubeListBuilder.create().texOffs(52, 10).addBox(-7.0F, -8.0F, -1.0F, 3.0F, 8.0F, 2.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("fin_east",
                CubeListBuilder.create().texOffs(32, 21).addBox(4.0F, -8.0F, -1.0F, 3.0F, 8.0F, 2.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("flame",
                CubeListBuilder.create().texOffs(42, 21).addBox(-2.0F, 4.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addDroneEngine(PartDefinition root, String name, float x) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(36, 0).addBox(-1.35F, -2.15F, -3.1F, 2.7F, 4.3F, 6.2F)
                        .texOffs(48, 16).addBox(-1.1F, 0.95F, -2.2F, 2.2F, 1.1F, 4.4F),
                PartPose.offset(x, 18.0F, 0.0F));
    }

    private static void addDroneWing(PartDefinition root, String name, float x, boolean mirrored) {
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

    private static void addLeg(PartDefinition root, String name, float x, float z) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(0, 42).addBox(-1.4F, 0.0F, -1.5F, 2.8F, 8.0F, 3.0F),
                PartPose.offset(x, 16.0F, z));
    }

    private static void part(PartDefinition root, String name, int u, int v, float x, float y, float z,
            float w, float h, float d, float ox, float oy, float oz) {
        part(root, name, u, v, x, y, z, w, h, d, ox, oy, oz, 0.0F);
    }

    private static void part(PartDefinition root, String name, int u, int v, float x, float y, float z,
            float w, float h, float d, float ox, float oy, float oz, float deformation) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d, new CubeDeformation(deformation)),
                PartPose.offset(ox, oy, oz));
    }

    public static class EchoHumanoidModel extends HumanoidModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final Map<String, ModelPart> namedParts;

        public EchoHumanoidModel(ModelPart root) {
            super(root);
            ModelPart core = body.getChild("core");
            ModelPart pack = body.getChild("pack");
            ModelPart scanner = pack.getChild("scanner");
            ModelPart eyes = head.getChild("eyes");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("head", head)
                    .put("hat", hat)
                    .put("body", body)
                    .put("torso", body)
                    .put("left_arm", leftArm)
                    .put("right_arm", rightArm)
                    .put("left_leg", leftLeg)
                    .put("right_leg", rightLeg)
                    .put("pack", pack)
                    .put("satchel", pack)
                    .put("radio", scanner)
                    .put("core", core)
                    .put("eyes", eyes)
                    .put("scanner", scanner)
                    .put("trail", pack)
                    .put("exhaust", pack)
                    .put("ground", body)
                    .build()
                    .asMap();
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoSurvivorNpcModel extends EchoHumanoidModel {
        public EchoSurvivorNpcModel(ModelPart root) {
            super(root);
        }
    }

    public static final class EchoStationSuitModel extends HumanoidModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final Map<String, ModelPart> namedParts;

        public EchoStationSuitModel(ModelPart root) {
            super(root);
            ModelPart core = body.getChild("core");
            ModelPart pack = body.getChild("pack");
            ModelPart scanner = pack.getChild("scanner");
            ModelPart eyes = head.getChild("eyes");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("head", head)
                    .put("helmet", hat)
                    .put("hat", hat)
                    .put("body", body)
                    .put("torso", body)
                    .put("left_arm", leftArm)
                    .put("right_arm", rightArm)
                    .put("left_leg", leftLeg)
                    .put("right_leg", rightLeg)
                    .put("life_support", pack)
                    .put("oxygen_tank", pack)
                    .put("pack", pack)
                    .put("core", core)
                    .put("eyes", eyes)
                    .put("scanner", scanner)
                    .put("trail", pack)
                    .put("exhaust", pack)
                    .put("ground", body)
                    .build()
                    .asMap();
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoHeavyBossModel extends HumanoidModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final Map<String, ModelPart> namedParts;

        public EchoHeavyBossModel(ModelPart root) {
            super(root);
            ModelPart reactor = body.getChild("reactor");
            ModelPart shoulderRig = body.getChild("shoulder_rig");
            ModelPart backSpines = body.getChild("back_spines");
            ModelPart eyes = head.getChild("eyes");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("head", head)
                    .put("hat", hat)
                    .put("body", body)
                    .put("torso", body)
                    .put("left_arm", leftArm)
                    .put("right_arm", rightArm)
                    .put("left_leg", leftLeg)
                    .put("right_leg", rightLeg)
                    .put("reactor", reactor)
                    .put("core", reactor)
                    .put("shoulder_rig", shoulderRig)
                    .put("back_spines", backSpines)
                    .put("eyes", eyes)
                    .put("scanner", eyes)
                    .put("trail", backSpines)
                    .put("exhaust", backSpines)
                    .put("ground", body)
                    .build()
                    .asMap();
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoDroneModel extends EntityModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
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
        private final Map<String, ModelPart> namedParts;

        public EchoDroneModel(ModelPart root) {
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
            this.namedParts = EchoNamedModelParts.builder()
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

        @Override
        public void setupAnim(EchoMobRenderState state) {
            super.setupAnim(state);
            float age = state.ageInTicks;
            float hover = Mth.sin(age * 0.18F) * 0.18F;
            float pulse = 1.0F + 0.08F * (0.5F + 0.5F * Mth.sin(age * 0.35F));
            root.y = hover;
            root.yRot = state.yRot * ((float) Math.PI / 180F);
            root.xRot = state.xRot * ((float) Math.PI / 180F);
            chassis.xRot = hover * 0.045F;
            chassis.zRot = Mth.sin(age * 0.12F) * 0.025F;
            frontDisplay.zRot = Mth.sin(age * 0.1F) * 0.015F;
            rearPanel.zRot = -frontDisplay.zRot;
            topPlate.yRot = Mth.sin(age * 0.08F) * 0.05F;
            bottomDisplay.yScale = pulse;
            animateEngine(leftEngine, age, 1.0F);
            animateEngine(rightEngine, age, -1.0F);
            animateWing(leftWing, age, 1.0F);
            animateWing(rightWing, age, -1.0F);
            animateHoverCore(hoverCoreFl, age, 0.0F);
            animateHoverCore(hoverCoreFr, age, 0.6F);
            animateHoverCore(hoverCoreBl, age, 1.2F);
            animateHoverCore(hoverCoreBr, age, 1.8F);
            antennaLeft.xRot = Mth.sin(age * 0.16F) * 0.05F;
            antennaRight.xRot = Mth.sin(age * 0.16F + 0.7F) * 0.05F;
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoQuadrupedModel extends EntityModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final ModelPart head;
        private final ModelPart body;
        private final ModelPart tail;
        private final ModelPart leftFrontLeg;
        private final ModelPart rightFrontLeg;
        private final ModelPart leftBackLeg;
        private final ModelPart rightBackLeg;
        private final ModelPart spines;
        private final ModelPart eyes;
        private final Map<String, ModelPart> namedParts;

        public EchoQuadrupedModel(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            this.head = root.getChild("head");
            this.tail = root.getChild("tail");
            this.leftFrontLeg = root.getChild("left_front_leg");
            this.rightFrontLeg = root.getChild("right_front_leg");
            this.leftBackLeg = root.getChild("left_back_leg");
            this.rightBackLeg = root.getChild("right_back_leg");
            this.spines = body.getChild("spines");
            this.eyes = head.getChild("eyes");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("body", body)
                    .put("torso", body)
                    .put("head", head)
                    .put("tail", tail)
                    .put("left_front_leg", leftFrontLeg)
                    .put("right_front_leg", rightFrontLeg)
                    .put("left_back_leg", leftBackLeg)
                    .put("right_back_leg", rightBackLeg)
                    .put("spines", spines)
                    .put("eyes", eyes)
                    .put("core", spines)
                    .put("scanner", eyes)
                    .put("trail", tail)
                    .put("exhaust", tail)
                    .put("ground", body)
                    .build()
                    .asMap();
        }

        @Override
        public void setupAnim(EchoMobRenderState state) {
            super.setupAnim(state);
            float walk = state.ageInTicks * 0.18F;
            head.yRot = state.yRot * ((float) Math.PI / 180F);
            head.xRot = state.xRot * ((float) Math.PI / 180F) * 0.65F;
            body.y = Mth.sin(state.ageInTicks * 0.12F) * 0.12F;
            tail.yRot = Mth.sin(state.ageInTicks * 0.16F) * 0.22F;
            leftFrontLeg.xRot = Mth.sin(walk) * 0.35F;
            rightBackLeg.xRot = leftFrontLeg.xRot;
            rightFrontLeg.xRot = Mth.sin(walk + Mth.PI) * 0.35F;
            leftBackLeg.xRot = rightFrontLeg.xRot;
            spines.yScale = 1.0F + Mth.sin(state.ageInTicks * 0.2F) * 0.04F;
            eyes.xScale = 1.0F + Mth.sin(state.ageInTicks * 0.25F) * 0.08F;
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoCrawlerModel extends EntityModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final ModelPart body;
        private final ModelPart head;
        private final ModelPart frontClaws;
        private final ModelPart rearLegs;
        private final ModelPart acidSacs;
        private final ModelPart mandibles;
        private final ModelPart eyes;
        private final Map<String, ModelPart> namedParts;

        public EchoCrawlerModel(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            this.head = root.getChild("head");
            this.frontClaws = root.getChild("front_claws");
            this.rearLegs = root.getChild("rear_legs");
            this.acidSacs = body.getChild("acid_sacs");
            this.mandibles = head.getChild("mandibles");
            this.eyes = head.getChild("eyes");
            this.namedParts = EchoNamedModelParts.builder()
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

        @Override
        public void setupAnim(EchoMobRenderState state) {
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
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoWraithModel extends EntityModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final ModelPart head;
        private final ModelPart veil;
        private final ModelPart leftClaw;
        private final ModelPart rightClaw;
        private final ModelPart smokeTrail;
        private final ModelPart core;
        private final ModelPart eyes;
        private final Map<String, ModelPart> namedParts;

        public EchoWraithModel(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.veil = root.getChild("veil");
            this.leftClaw = root.getChild("left_claw");
            this.rightClaw = root.getChild("right_claw");
            this.smokeTrail = root.getChild("smoke_trail");
            this.core = veil.getChild("core");
            this.eyes = head.getChild("eyes");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("head", head)
                    .put("veil", veil)
                    .put("body", veil)
                    .put("torso", veil)
                    .put("left_claw", leftClaw)
                    .put("right_claw", rightClaw)
                    .put("smoke_trail", smokeTrail)
                    .put("trail", smokeTrail)
                    .put("core", core)
                    .put("eyes", eyes)
                    .put("scanner", eyes)
                    .put("exhaust", smokeTrail)
                    .put("ground", smokeTrail)
                    .build()
                    .asMap();
        }

        @Override
        public void setupAnim(EchoMobRenderState state) {
            super.setupAnim(state);
            float age = state.ageInTicks;
            head.yRot = state.yRot * ((float) Math.PI / 180F) * 0.5F;
            head.xRot = state.xRot * ((float) Math.PI / 180F) * 0.4F;
            veil.y = Mth.sin(age * 0.12F) * 0.22F;
            veil.zRot = Mth.sin(age * 0.08F) * 0.025F;
            leftClaw.xRot = -0.18F + Mth.sin(age * 0.16F) * 0.12F;
            rightClaw.xRot = -0.18F - Mth.sin(age * 0.16F) * 0.12F;
            smokeTrail.yScale = 1.0F + Mth.sin(age * 0.18F) * 0.08F;
            core.xScale = 1.0F + Mth.sin(age * 0.24F) * 0.1F;
            eyes.xScale = 1.0F + Mth.sin(age * 0.28F) * 0.06F;
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoSlimeModel extends EntityModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final ModelPart body;
        private final ModelPart innerCore;
        private final ModelPart puddle;
        private final ModelPart bubbles;
        private final ModelPart eyes;
        private final Map<String, ModelPart> namedParts;

        public EchoSlimeModel(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            this.innerCore = body.getChild("inner_core");
            this.bubbles = body.getChild("bubbles");
            this.eyes = body.getChild("eyes");
            this.puddle = root.getChild("puddle");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("body", body)
                    .put("torso", body)
                    .put("inner_core", innerCore)
                    .put("core", innerCore)
                    .put("puddle", puddle)
                    .put("bubbles", bubbles)
                    .put("eyes", eyes)
                    .put("scanner", eyes)
                    .put("trail", puddle)
                    .put("ground", puddle)
                    .put("exhaust", bubbles)
                    .build()
                    .asMap();
        }

        @Override
        public void setupAnim(EchoMobRenderState state) {
            super.setupAnim(state);
            float pulse = 1.0F + Mth.sin(state.ageInTicks * 0.22F) * 0.08F;
            body.xScale = pulse;
            body.zScale = pulse;
            body.yScale = 1.0F + Mth.cos(state.ageInTicks * 0.22F) * 0.06F;
            innerCore.xScale = 1.0F + Mth.sin(state.ageInTicks * 0.32F) * 0.12F;
            bubbles.y = Mth.sin(state.ageInTicks * 0.16F) * 0.18F;
            puddle.xScale = 1.0F + Mth.sin(state.ageInTicks * 0.11F) * 0.04F;
            puddle.zScale = puddle.xScale;
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoIndustrialConstructModel extends EntityModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final ModelPart root;
        private final ModelPart torso;
        private final ModelPart head;
        private final ModelPart core;
        private final ModelPart leftArm;
        private final ModelPart rightArm;
        private final ModelPart leftLeg;
        private final ModelPart rightLeg;
        private final ModelPart backFurnace;
        private final ModelPart antenna;
        private final Map<String, ModelPart> namedParts;

        public EchoIndustrialConstructModel(ModelPart root) {
            super(root);
            this.root = root;
            this.torso = root.getChild("torso");
            this.head = root.getChild("head");
            this.core = root.getChild("core");
            this.leftArm = root.getChild("left_arm");
            this.rightArm = root.getChild("right_arm");
            this.leftLeg = root.getChild("left_leg");
            this.rightLeg = root.getChild("right_leg");
            this.backFurnace = root.getChild("back_furnace");
            this.antenna = root.getChild("antenna");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("body", torso)
                    .put("torso", torso)
                    .put("head", head)
                    .put("core", core)
                    .put("left_arm", leftArm)
                    .put("right_arm", rightArm)
                    .put("left_leg", leftLeg)
                    .put("right_leg", rightLeg)
                    .put("reactor", core)
                    .put("furnace", backFurnace)
                    .put("pack", backFurnace)
                    .put("scanner", antenna)
                    .put("eyes", head)
                    .put("exhaust", backFurnace)
                    .put("trail", backFurnace)
                    .put("ground", torso)
                    .build()
                    .asMap();
        }

        @Override
        public void setupAnim(EchoMobRenderState state) {
            super.setupAnim(state);
            float pulse = Mth.sin(state.ageInTicks * 0.18F) * 0.5F + 0.5F;
            float sway = Mth.sin(state.ageInTicks * 0.09F) * 0.08F;
            root.y = Mth.sin(state.ageInTicks * 0.07F) * 0.12F;
            head.yRot = sway * 0.45F;
            core.xScale = 1.0F + pulse * 0.1F;
            core.yScale = 1.0F + pulse * 0.08F;
            leftArm.xRot = -0.08F + sway;
            rightArm.xRot = -0.08F - sway;
            leftLeg.xRot = Mth.sin(state.ageInTicks * 0.12F) * 0.035F;
            rightLeg.xRot = -leftLeg.xRot;
            antenna.xRot = -0.12F + Mth.sin(state.ageInTicks * 0.11F) * 0.04F;
            antenna.zRot = Mth.sin(state.ageInTicks * 0.08F) * 0.035F;
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    public static final class EchoRocketModel extends EntityModel<EchoMobRenderState> implements EchoNamedModelPartProvider {
        private final ModelPart root;
        private final ModelPart body;
        private final ModelPart nose;
        private final ModelPart engine;
        private final ModelPart flame;
        private final Map<String, ModelPart> namedParts;

        public EchoRocketModel(ModelPart root) {
            super(root);
            this.root = root;
            this.body = root.getChild("body");
            this.nose = root.getChild("nose");
            this.engine = root.getChild("engine");
            this.flame = root.getChild("flame");
            this.namedParts = EchoNamedModelParts.builder()
                    .put("root", root)
                    .put("body", body)
                    .put("torso", body)
                    .put("nose", nose)
                    .put("head", nose)
                    .put("engine", engine)
                    .put("core", engine)
                    .put("flame", flame)
                    .put("exhaust", flame)
                    .put("trail", flame)
                    .put("ground", engine)
                    .build()
                    .asMap();
        }

        @Override
        public void setupAnim(EchoMobRenderState state) {
            super.setupAnim(state);
            root.y = Mth.sin(state.ageInTicks * 0.18F) * 0.08F;
            flame.yScale = 1.0F + Mth.sin(state.ageInTicks * 0.32F) * 0.18F;
        }

        @Override
        public Map<String, ModelPart> echoNamedModelParts() {
            return namedParts;
        }
    }

    private static void animateEngine(ModelPart part, float age, float direction) {
        part.yRot = direction * 0.025F * Mth.sin(age * 0.22F);
        part.zRot = direction * 0.018F * Mth.cos(age * 0.18F);
    }

    private static void animateWing(ModelPart part, float age, float direction) {
        part.zRot = direction * (0.08F + 0.025F * Mth.sin(age * 0.24F));
    }

    private static void animateHoverCore(ModelPart part, float age, float offset) {
        float pulse = 1.0F + 0.08F * Mth.sin(age * 0.42F + offset);
        part.xScale = pulse;
        part.zScale = pulse;
    }
}
