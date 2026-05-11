package com.knoxhack.echoindustrialnexus.client;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class IndustrialFurnaceModel extends EntityModel<ZombieRenderState> {
   public static final ModelLayerLocation WARDEN_LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, "furnace_warden"), "main");
   public static final ModelLayerLocation DRONE_LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, "furnace_drone"), "main");

   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart core;
   private final ModelPart leftArm;
   private final ModelPart rightArm;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;
   private final ModelPart antenna;

   public IndustrialFurnaceModel(ModelPart root) {
      super(root);
      this.root = root;
      this.head = root.getChild("head");
      this.core = root.getChild("core");
      this.leftArm = root.getChild("left_arm");
      this.rightArm = root.getChild("right_arm");
      this.leftLeg = root.getChild("left_leg");
      this.rightLeg = root.getChild("right_leg");
      this.antenna = root.getChild("antenna");
   }

   public static LayerDefinition createWardenLayer() {
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

   public static LayerDefinition createDroneLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      part(root, "torso", 0, 0, -5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, 0.0F, 15.0F, 0.0F);
      part(root, "head", 42, 0, -4.0F, -4.0F, -2.0F, 8.0F, 5.0F, 3.0F, 0.0F, 10.0F, -4.8F);
      part(root, "core", 74, 0, -3.0F, -3.0F, -1.0F, 6.0F, 5.0F, 2.0F, 0.0F, 15.0F, -4.5F, 0.08F);
      part(root, "left_arm", 0, 34, 0.0F, -1.0F, -2.5F, 4.0F, 8.0F, 5.0F, 5.0F, 13.0F, 0.0F);
      part(root, "right_arm", 28, 34, -4.0F, -1.0F, -2.5F, 4.0F, 8.0F, 5.0F, -5.0F, 13.0F, 0.0F);
      part(root, "left_leg", 56, 34, -1.5F, 0.0F, -2.0F, 3.0F, 7.0F, 4.0F, 2.8F, 17.0F, 0.0F);
      part(root, "right_leg", 84, 34, -1.5F, 0.0F, -2.0F, 3.0F, 7.0F, 4.0F, -2.8F, 17.0F, 0.0F);
      part(root, "back_furnace", 58, 96, -4.0F, -6.0F, 0.0F, 8.0F, 7.0F, 3.0F, 0.0F, 15.0F, 4.0F);
      part(root, "antenna", 38, 66, -0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, 3.5F, 9.0F, 1.8F);
      return LayerDefinition.create(mesh, 128, 128);
   }

   private static void part(PartDefinition root, String name, int u, int v, float x, float y, float z, float w, float h, float d, float ox, float oy, float oz) {
      part(root, name, u, v, x, y, z, w, h, d, ox, oy, oz, 0.0F);
   }

   private static void part(PartDefinition root, String name, int u, int v, float x, float y, float z, float w, float h, float d, float ox, float oy, float oz, float deformation) {
      root.addOrReplaceChild(name,
         CubeListBuilder.create()
            .texOffs(u, v)
            .addBox(x, y, z, w, h, d, new CubeDeformation(deformation)),
         PartPose.offset(ox, oy, oz));
   }

   @Override
   public void setupAnim(ZombieRenderState state) {
      super.setupAnim(state);
      float pulse = Mth.sin(state.ageInTicks * 0.18F) * 0.5F + 0.5F;
      float sway = Mth.sin(state.ageInTicks * 0.09F) * 0.08F;
      this.root.y = Mth.sin(state.ageInTicks * 0.07F) * 0.12F;
      this.head.yRot = sway * 0.45F;
      this.core.xScale = 1.0F + pulse * 0.1F;
      this.core.yScale = 1.0F + pulse * 0.08F;
      this.leftArm.xRot = -0.08F + sway;
      this.rightArm.xRot = -0.08F - sway;
      this.leftLeg.xRot = Mth.sin(state.ageInTicks * 0.12F) * 0.035F;
      this.rightLeg.xRot = -this.leftLeg.xRot;
      this.antenna.xRot = -0.12F + Mth.sin(state.ageInTicks * 0.11F) * 0.04F;
      this.antenna.zRot = Mth.sin(state.ageInTicks * 0.08F) * 0.035F;
   }
}
