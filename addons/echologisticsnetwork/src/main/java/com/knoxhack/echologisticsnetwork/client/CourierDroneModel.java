package com.knoxhack.echologisticsnetwork.client;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class CourierDroneModel extends EntityModel<CourierDroneRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(EchoLogisticsNetwork.MODID, "courier_drone"), "main");

   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart topPlate;
   private final ModelPart sensorArray;
   private final ModelPart core;
   private final ModelPart thrusterFl;
   private final ModelPart thrusterFr;
   private final ModelPart thrusterBl;
   private final ModelPart thrusterBr;
   private final ModelPart leftFin;
   private final ModelPart rightFin;
   private final ModelPart antennaLeft;
   private final ModelPart antennaRight;

   public CourierDroneModel(ModelPart root) {
      super(root);
      this.root = root;
      this.body = root.getChild("body");
      this.topPlate = root.getChild("top_plate");
      this.sensorArray = root.getChild("sensor_array");
      this.core = root.getChild("core");
      this.thrusterFl = root.getChild("thruster_fl");
      this.thrusterFr = root.getChild("thruster_fr");
      this.thrusterBl = root.getChild("thruster_bl");
      this.thrusterBr = root.getChild("thruster_br");
      this.leftFin = root.getChild("left_fin");
      this.rightFin = root.getChild("right_fin");
      this.antennaLeft = root.getChild("antenna_left");
      this.antennaRight = root.getChild("antenna_right");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();

      root.addOrReplaceChild("body",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.5F, -2.0F, -3.5F, 7.0F, 4.0F, 7.0F)
            .texOffs(0, 16)
            .addBox(-2.5F, -2.75F, -2.25F, 5.0F, 1.0F, 5.0F),
         PartPose.offset(0.0F, 18.0F, 0.0F));

      root.addOrReplaceChild("top_plate",
         CubeListBuilder.create()
            .texOffs(28, 0)
            .addBox(-2.5F, -0.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.04F))
            .texOffs(48, 0)
            .addBox(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.08F)),
         PartPose.offset(0.0F, 15.15F, 0.0F));

      root.addOrReplaceChild("sensor_array",
         CubeListBuilder.create()
            .texOffs(0, 24)
            .addBox(-3.25F, -1.75F, -0.75F, 6.5F, 3.5F, 1.5F)
            .texOffs(20, 24)
            .addBox(-2.75F, -1.25F, -1.05F, 5.5F, 2.5F, 1.0F),
         PartPose.offset(0.0F, 17.65F, -4.0F));

      root.addOrReplaceChild("core",
         CubeListBuilder.create()
            .texOffs(36, 24)
            .addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.06F)),
         PartPose.offset(0.0F, 20.45F, -0.25F));

      addThruster(root, "thruster_fl", 3.75F, 19.85F, -3.15F);
      addThruster(root, "thruster_fr", -3.75F, 19.85F, -3.15F);
      addThruster(root, "thruster_bl", 3.75F, 19.85F, 3.15F);
      addThruster(root, "thruster_br", -3.75F, 19.85F, 3.15F);

      root.addOrReplaceChild("left_fin",
         CubeListBuilder.create()
            .texOffs(0, 40)
            .addBox(-0.5F, -0.75F, -2.5F, 1.0F, 1.5F, 5.0F),
         PartPose.offset(4.35F, 17.4F, 0.0F));

      root.addOrReplaceChild("right_fin",
         CubeListBuilder.create()
            .texOffs(0, 40)
            .mirror()
            .addBox(-0.5F, -0.75F, -2.5F, 1.0F, 1.5F, 5.0F)
            .mirror(false),
         PartPose.offset(-4.35F, 17.4F, 0.0F));

      root.addOrReplaceChild("antenna_left",
         CubeListBuilder.create()
            .texOffs(16, 40)
            .addBox(-0.5F, -2.25F, -0.5F, 1.0F, 3.0F, 1.0F),
         PartPose.offset(2.5F, 15.3F, -2.0F));

      root.addOrReplaceChild("antenna_right",
         CubeListBuilder.create()
            .texOffs(16, 40)
            .addBox(-0.5F, -2.25F, -0.5F, 1.0F, 3.0F, 1.0F),
         PartPose.offset(-2.5F, 15.3F, -2.0F));

      return LayerDefinition.create(mesh, 64, 64);
   }

   private static void addThruster(PartDefinition root, String name, float x, float y, float z) {
      root.addOrReplaceChild(name,
         CubeListBuilder.create()
            .texOffs(40, 32)
            .addBox(-1.1F, -1.0F, -1.1F, 2.2F, 3.0F, 2.2F)
            .texOffs(52, 32)
            .addBox(-0.9F, 1.85F, -0.9F, 1.8F, 0.7F, 1.8F),
         PartPose.offset(x, y, z));
   }

   @Override
   public void setupAnim(CourierDroneRenderState state) {
      super.setupAnim(state);
      float hoverBob = Mth.sin(state.ageInTicks * 0.15F) * 0.3F;
      this.root.y = hoverBob + state.hoverOffset;

      float pulse = Mth.sin(state.ageInTicks * 0.2F) * 0.5F + 0.5F;
      this.body.yRot = Mth.sin(state.ageInTicks * 0.035F) * 0.025F;
      this.sensorArray.yRot = Mth.sin(state.ageInTicks * 0.05F) * 0.12F;
      this.topPlate.y = 15.15F - pulse * 0.1F;
      this.core.xScale = 1.0F + pulse * 0.12F;
      this.core.zScale = 1.0F + pulse * 0.12F;

      float thrusterSway = Mth.sin(state.ageInTicks * 0.22F) * 0.05F;
      this.thrusterFl.xRot = thrusterSway;
      this.thrusterBr.xRot = thrusterSway;
      this.thrusterFr.xRot = -thrusterSway;
      this.thrusterBl.xRot = -thrusterSway;

      float finSweep = Mth.sin(state.ageInTicks * 0.08F) * 0.07F;
      this.leftFin.zRot = finSweep;
      this.rightFin.zRot = -finSweep;
      this.antennaLeft.xRot = -0.16F + finSweep;
      this.antennaRight.xRot = -0.16F - finSweep;
   }
}
