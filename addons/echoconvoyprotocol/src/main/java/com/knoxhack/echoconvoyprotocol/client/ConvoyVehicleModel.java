package com.knoxhack.echoconvoyprotocol.client;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleKind;
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

public class ConvoyVehicleModel extends EntityModel<ConvoyVehicleRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, "convoy_vehicle"), "main");

   private final ModelPart root;
   private final ModelPart cab;
   private final ModelPart cargo;
   private final ModelPart scanner;
   private final ModelPart armor;

   public ConvoyVehicleModel(ModelPart root) {
      super(root);
      this.root = root;
      this.cab = root.getChild("cab");
      this.cargo = root.getChild("cargo");
      this.scanner = root.getChild("scanner");
      this.armor = root.getChild("armor");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("chassis",
         CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -5.0F, -18.0F, 20.0F, 5.0F, 36.0F),
         PartPose.offset(0.0F, 22.0F, 0.0F));
      root.addOrReplaceChild("cab",
         CubeListBuilder.create().texOffs(0, 42).addBox(-7.0F, -13.0F, -11.0F, 14.0F, 8.0F, 13.0F),
         PartPose.offset(0.0F, 22.0F, 0.0F));
      root.addOrReplaceChild("cargo",
         CubeListBuilder.create().texOffs(54, 42).addBox(-8.0F, -11.0F, 2.0F, 16.0F, 8.0F, 14.0F),
         PartPose.offset(0.0F, 22.0F, 0.0F));
      root.addOrReplaceChild("scanner",
         CubeListBuilder.create().texOffs(76, 0).addBox(-2.0F, -18.0F, -4.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.1F)),
         PartPose.offset(0.0F, 22.0F, 0.0F));
      root.addOrReplaceChild("armor",
         CubeListBuilder.create().texOffs(96, 0).addBox(-11.0F, -8.0F, -19.0F, 22.0F, 4.0F, 38.0F, new CubeDeformation(0.2F)),
         PartPose.offset(0.0F, 22.0F, 0.0F));
      root.addOrReplaceChild("front_left_wheel",
         CubeListBuilder.create().texOffs(0, 20).addBox(-13.0F, -8.0F, -15.0F, 4.0F, 8.0F, 8.0F),
         PartPose.offset(0.0F, 24.0F, 0.0F));
      root.addOrReplaceChild("front_right_wheel",
         CubeListBuilder.create().texOffs(0, 20).addBox(9.0F, -8.0F, -15.0F, 4.0F, 8.0F, 8.0F),
         PartPose.offset(0.0F, 24.0F, 0.0F));
      root.addOrReplaceChild("rear_left_wheel",
         CubeListBuilder.create().texOffs(0, 20).addBox(-13.0F, -8.0F, 9.0F, 4.0F, 8.0F, 8.0F),
         PartPose.offset(0.0F, 24.0F, 0.0F));
      root.addOrReplaceChild("rear_right_wheel",
         CubeListBuilder.create().texOffs(0, 20).addBox(9.0F, -8.0F, 9.0F, 4.0F, 8.0F, 8.0F),
         PartPose.offset(0.0F, 24.0F, 0.0F));
      return LayerDefinition.create(mesh, 128, 64);
   }

   @Override
   public void setupAnim(ConvoyVehicleRenderState state) {
      super.setupAnim(state);
      ConvoyVehicleKind kind = ConvoyVehicleKind.byId(state.kind);
      float scale = switch (kind) {
         case SCRAP_BIKE -> 0.48F;
         case WASTELAND_ROVER -> 0.78F;
         case CARGO_CRAWLER -> 1.08F;
         case ARMORED_RELAY_TRUCK -> 1.0F;
      };
      root.xScale = scale;
      root.yScale = scale;
      root.zScale = scale;
      root.y = Mth.sin(state.ageInTicks * 0.25F) * state.damageRatio * 0.3F;
      cab.visible = kind != ConvoyVehicleKind.SCRAP_BIKE;
      cargo.visible = kind == ConvoyVehicleKind.CARGO_CRAWLER || kind == ConvoyVehicleKind.ARMORED_RELAY_TRUCK;
      scanner.visible = kind == ConvoyVehicleKind.ARMORED_RELAY_TRUCK || kind == ConvoyVehicleKind.WASTELAND_ROVER;
      armor.visible = kind == ConvoyVehicleKind.ARMORED_RELAY_TRUCK;
   }
}
