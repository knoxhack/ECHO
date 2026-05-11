package com.knoxhack.echorendercore.client;

import com.knoxhack.echorendercore.EchoRenderCore;
import com.knoxhack.echorendercore.api.IAdvancedVisualBlockEntity;
import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.profile.BlockPartSelectorProfile;
import com.knoxhack.echorendercore.profile.ProfileValidationIssue;
import com.knoxhack.echorendercore.profile.RenderCoreProfiles;
import com.knoxhack.echorendercore.profile.VisualProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public final class RenderCoreClientCommands {
   private RenderCoreClientCommands() {
   }

   public static void register(RegisterClientCommandsEvent event) {
      event.getDispatcher().register(
         Commands.literal("rendercore")
            .then(Commands.literal("reload")
               .executes(context -> reload()))
            .then(Commands.literal("validate")
               .executes(context -> validate("all"))
               .then(Commands.argument("namespace", StringArgumentType.word())
                  .executes(context -> validate(StringArgumentType.getString(context, "namespace")))))
            .then(Commands.literal("debug")
               .then(Commands.literal("state")
                  .then(Commands.argument("state", StringArgumentType.word())
                     .executes(context -> forceState(StringArgumentType.getString(context, "state"), 30))
                     .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 3600))
                        .executes(context -> forceState(
                           StringArgumentType.getString(context, "state"),
                           IntegerArgumentType.getInteger(context, "seconds")
                        )))))
               .then(Commands.literal("missingparts")
                  .then(Commands.argument("enabled", BoolArgumentType.bool())
                     .executes(context -> missingParts(BoolArgumentType.getBool(context, "enabled")))))
               .then(Commands.literal("hud")
                  .then(Commands.argument("enabled", BoolArgumentType.bool())
                     .executes(context -> hud(BoolArgumentType.getBool(context, "enabled")))))
               .then(Commands.literal("anchors")
                  .then(Commands.argument("enabled", BoolArgumentType.bool())
                     .executes(context -> anchors(BoolArgumentType.getBool(context, "enabled")))))
               .then(Commands.literal("blockparts")
                  .executes(context -> blockParts())))
      );
   }

   private static int reload() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.reloadResourcePacks();
      message("RenderCore profiles queued for reload.");
      return 1;
   }

   private static int forceState(String stateName, int seconds) {
      VisualState state = VisualState.byName(stateName, VisualState.IDLE);
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level == null || minecraft.hitResult == null) {
         message("No client target is available.");
         return 0;
      }
      HitResult hit = minecraft.hitResult;
      if (hit instanceof EntityHitResult entityHit) {
         Entity entity = entityHit.getEntity();
         DebugVisualOverrides.setEntity(entity.getUUID(), state, seconds);
         message("RenderCore state override set to " + state.name() + " for " + entity.getName().getString() + ".");
         return 1;
      }
      if (hit instanceof BlockHitResult blockHit) {
         BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHit.getBlockPos());
         if (blockEntity instanceof IAdvancedVisualBlockEntity || blockEntity != null) {
            DebugVisualOverrides.setBlock(minecraft.level, blockHit.getBlockPos(), state, seconds);
            message("RenderCore block state override set to " + state.name() + ".");
            return 1;
         }
      }
      message("Target does not expose a RenderCore visual profile.");
      return 0;
   }

   private static int missingParts(boolean enabled) {
      DebugVisualOverrides.setMissingPartWarnings(enabled);
      message("RenderCore missing part warnings " + (enabled ? "enabled." : "disabled."));
      return 1;
   }

   private static int validate(String namespace) {
      var report = RenderCoreProfiles.loaded().validationReport().forNamespace(namespace);
      String normalized = namespace == null || namespace.isBlank() ? "all" : namespace;
      long shown = report.issues().stream()
         .limit(6)
         .peek(RenderCoreClientCommands::messageIssue)
         .count();
      message("RenderCore validation: " + report.summaryLine() + ". Showing " + shown + " issue(s) for " + normalized + ".");
      return report.hasErrors() ? 0 : 1;
   }

   private static int hud(boolean enabled) {
      DebugVisualOverrides.setHudEnabled(enabled);
      message("RenderCore debug HUD " + (enabled ? "enabled." : "disabled."));
      return 1;
   }

   private static int anchors(boolean enabled) {
      DebugVisualOverrides.setAnchorsEnabled(enabled);
      message("RenderCore anchor debug " + (enabled ? "enabled." : "disabled."));
      return 1;
   }

   private static int blockParts() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult blockHit)) {
         message("Look at a RenderCore-supported block entity first.");
         return 0;
      }
      BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHit.getBlockPos());
      if (blockEntity == null) {
         message("The looked-at block has no block entity.");
         return 0;
      }
      Identifier profileId = blockProfileId(minecraft, blockEntity);
      if (profileId == null) {
         message("The looked-at block entity has not exposed a RenderCore profile yet.");
         return 0;
      }
      VisualProfile profile = RenderCoreProfiles.visual(profileId);
      if (profile == null) {
         message("RenderCore profile " + profileId + " is not loaded.");
         return 0;
      }
      BlockState blockState = minecraft.level.getBlockState(blockHit.getBlockPos());
      List<BlockStateModelPart> collected = BakedBlockPartResolver.collect(blockState);
      Map<String, List<BlockStateModelPart>> aliases = BakedBlockPartResolver.resolve(collected, blockState, profile);
      var tintIndices = BakedBlockPartResolver.availableTintIndices(collected);
      var report = com.knoxhack.echorendercore.profile.RenderCoreProfileValidator.validateBlockPartSelectors(
         profile,
         collected.size(),
         blockState,
         tintIndices
      );
      message("RenderCore block parts: " + profileId + " collected " + collected.size() + ", aliases " + aliases.size()
         + ", warnings " + report.warnings() + ". See log for details.");
      EchoRenderCore.LOGGER.info("RenderCore block part export profile={} pos={} blockState={} collected={} aliases={} tintIndices={} warnings={}",
         profileId, blockHit.getBlockPos().toShortString(), blockState, collected.size(), aliases.keySet(), tintIndices, report.warnings());
      for (Map.Entry<String, BlockPartSelectorProfile> entry : profile.blockParts().entrySet()) {
         List<BlockStateModelPart> selected = aliases.getOrDefault(entry.getKey(), List.of());
         EchoRenderCore.LOGGER.info("RenderCore block alias {} matched indices {} selector {}",
            entry.getKey(), BakedBlockPartResolver.matchedIndices(collected, selected), selectorSummary(entry.getValue()));
      }
      for (ProfileValidationIssue issue : report.issues()) {
         EchoRenderCore.LOGGER.warn("RenderCore block part export {} {} [{}]: {}{}",
            issue.code(), issue.profileId(), issue.path(), issue.message(),
            issue.suggestion().isBlank() ? "" : " Suggestion: " + issue.suggestion());
      }
      return 1;
   }

   private static Identifier blockProfileId(Minecraft minecraft, BlockEntity blockEntity) {
      if (blockEntity instanceof IAdvancedVisualBlockEntity visual) {
         return visual.visualProfileId();
      }
      return RenderCoreDebugTargets.lookedAt(minecraft).map(RenderCoreDebugTargets.DebugTarget::profileId).orElse(null);
   }

   private static String selectorSummary(BlockPartSelectorProfile selector) {
      return "{indices=" + selector.indices()
         + ", directions=" + selector.directions()
         + ", material_flags=" + selector.materialFlags()
         + ", ambient_occlusion=" + selector.ambientOcclusion()
         + ", tint_indices=" + selector.tintIndices()
         + ", block_state=" + selector.blockState()
         + "}";
   }

   private static void messageIssue(ProfileValidationIssue issue) {
      message(issue.severity() + " " + issue.code() + " " + issue.profileId() + " [" + issue.path() + "]: " + issue.message());
   }

   private static void message(String text) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null) {
         minecraft.player.sendSystemMessage(Component.literal(text));
      }
   }
}
