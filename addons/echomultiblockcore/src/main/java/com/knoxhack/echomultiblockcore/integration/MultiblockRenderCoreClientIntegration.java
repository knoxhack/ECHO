package com.knoxhack.echomultiblockcore.integration;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.api.MultiblockState;
import com.knoxhack.echomultiblockcore.api.RobotState;
import com.knoxhack.echomultiblockcore.block.entity.MultiblockControllerBlockEntity;
import com.knoxhack.echomultiblockcore.block.entity.RoboticArmBlockEntity;
import com.knoxhack.echomultiblockcore.registry.ModBlockEntities;
import com.knoxhack.echorendercore.api.RenderCoreBlockVisualHost;
import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.client.RenderCoreBlockEntityRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class MultiblockRenderCoreClientIntegration {
   private static final Identifier CONTROLLER_PROFILE = Identifier.fromNamespaceAndPath(EchoMultiblockCore.MODID, "multiblock_controller");
   private static final Identifier ROBOTIC_ARM_PROFILE = Identifier.fromNamespaceAndPath(EchoMultiblockCore.MODID, "robotic_arm");

   private MultiblockRenderCoreClientIntegration() {
   }

   public static void registerBlockRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlockEntities.CONTROLLER.get(),
         context -> new RenderCoreBlockEntityRenderer<>(context, MultiblockRenderCoreClientIntegration::controllerHost));
      event.registerBlockEntityRenderer(ModBlockEntities.ROBOTIC_ARM.get(),
         context -> new RenderCoreBlockEntityRenderer<>(context, MultiblockRenderCoreClientIntegration::robotHost));
   }

   private static RenderCoreBlockVisualHost controllerHost(MultiblockControllerBlockEntity controller, float partialTick) {
      return new RenderCoreBlockVisualHost() {
         @Override
         public Identifier visualProfileId() {
            return CONTROLLER_PROFILE;
         }

         @Override
         public VisualState visualState() {
            MultiblockState state = controller.getState();
            return switch (state) {
               case FORMED, ACTIVE -> VisualState.ACTIVE;
               case VALIDATING -> VisualState.SCANNING;
               case DAMAGED, JAMMED, OVERLOADED -> VisualState.DAMAGED;
               case OFFLINE -> VisualState.OFFLINE;
               default -> VisualState.IDLE;
            };
         }

         @Override
         public float visualProgress() {
            return Math.max(0.0F, Math.min(1.0F, controller.getIntegrity() / 100.0F));
         }

         @Override
         public boolean visualDamaged() {
            return controller.getState() == MultiblockState.DAMAGED
               || controller.getState() == MultiblockState.JAMMED
               || controller.getState() == MultiblockState.OVERLOADED;
         }
      };
   }

   private static RenderCoreBlockVisualHost robotHost(RoboticArmBlockEntity arm, float partialTick) {
      return new RenderCoreBlockVisualHost() {
         @Override
         public Identifier visualProfileId() {
            return ROBOTIC_ARM_PROFILE;
         }

         @Override
         public VisualState visualState() {
            RobotState state = arm.getRobotState();
            return switch (state) {
               case WORKING -> VisualState.WORKING;
               case MOVING -> VisualState.ACTIVE;
               case COOLING -> VisualState.OVERHEATED;
               case JAMMED, DAMAGED -> VisualState.DAMAGED;
               case OFFLINE -> VisualState.OFFLINE;
               default -> VisualState.IDLE;
            };
         }

         @Override
         public float visualProgress() {
            return Math.max(0.0F, Math.min(1.0F, arm.getHeat() / (float)arm.getMaxHeat()));
         }

         @Override
         public boolean visualMoving() {
            return arm.getRobotState() == RobotState.MOVING || arm.getRobotState() == RobotState.WORKING;
         }

         @Override
         public boolean visualDamaged() {
            return arm.getRobotState() == RobotState.JAMMED || arm.getRobotState() == RobotState.DAMAGED;
         }
      };
   }
}
