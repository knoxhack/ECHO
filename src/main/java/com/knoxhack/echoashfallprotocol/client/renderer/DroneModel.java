package com.knoxhack.echoashfallprotocol.client.renderer;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Military/space/industrial drone model with pulsing core and hover animation.
 * Shared across EchoDrone, ScoutDrone, and CombatDrone variants.
 */
public class DroneModel extends EntityModel<DroneRenderState> {
    
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "drone"), "main");
    
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart thruster_fl;
    private final ModelPart thruster_fr;
    private final ModelPart thruster_bl;
    private final ModelPart thruster_br;
    private final ModelPart sensor_array;
    private final ModelPart core;
    
    public DroneModel(ModelPart root) {
        super(root);
        this.root = root;
        this.body = root.getChild("body");
        this.thruster_fl = root.getChild("thruster_fl");
        this.thruster_fr = root.getChild("thruster_fr");
        this.thruster_bl = root.getChild("thruster_bl");
        this.thruster_br = root.getChild("thruster_br");
        this.sensor_array = root.getChild("sensor_array");
        this.core = root.getChild("core");
    }
    
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        
        // Main chassis - angular industrial body
        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -3.0F, -5.0F, 8.0F, 6.0F, 10.0F), // Main body
                PartPose.offset(0.0F, 18.0F, 0.0F));
        
        // Front-left thruster
        partdefinition.addOrReplaceChild("thruster_fl",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                        .texOffs(12, 16) // Thruster glow
                        .addBox(-1.0F, 3.5F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(3.0F, 21.0F, -3.0F));
        
        // Front-right thruster
        partdefinition.addOrReplaceChild("thruster_fr",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                        .texOffs(12, 16) // Thruster glow
                        .addBox(-1.0F, 3.5F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(-3.0F, 21.0F, -3.0F));
        
        // Back-left thruster
        partdefinition.addOrReplaceChild("thruster_bl",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                        .texOffs(12, 16) // Thruster glow
                        .addBox(-1.0F, 3.5F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(3.0F, 21.0F, 3.0F));
        
        // Back-right thruster
        partdefinition.addOrReplaceChild("thruster_br",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                        .texOffs(12, 16) // Thruster glow
                        .addBox(-1.0F, 3.5F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(-3.0F, 21.0F, 3.0F));
        
        // Sensor array - forward facing optical sensors
        partdefinition.addOrReplaceChild("sensor_array",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-3.0F, -1.5F, -7.0F, 6.0F, 3.0F, 2.0F)
                        .texOffs(36, 5) // Sensor lens
                        .addBox(-2.0F, -1.0F, -7.5F, 4.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        
        // Pulsing core - emissive element
        partdefinition.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(52, 0)
                        .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    
    @Override
    public void setupAnim(DroneRenderState state) {
        super.setupAnim(state);
        
        // Hover bobbing animation
        float hoverBob = Mth.sin(state.ageInTicks * 0.15F) * 0.3F;
        this.root.y = hoverBob + state.hoverOffset;
        
        // Pulse animation for core
        float pulse = Mth.sin(state.ageInTicks * 0.2F) * 0.5F + 0.5F;
        state.pulsePhase = pulse;
        
        // Slight rotation of sensor array for "scanning" effect
        float scanAngle = Mth.sin(state.ageInTicks * 0.05F) * 0.15F;
        this.sensor_array.yRot = scanAngle;
    }
}
