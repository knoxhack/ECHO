package com.knoxhack.echorendercore.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knoxhack.echorendercore.EchoRenderCore;
import com.knoxhack.echorendercore.animation.AnimationChannel;
import com.knoxhack.echorendercore.animation.AnimationClip;
import com.knoxhack.echorendercore.animation.AnimationController;
import com.knoxhack.echorendercore.animation.AnimationKeyframe;
import com.knoxhack.echorendercore.animation.AnimationPlayer;
import com.knoxhack.echorendercore.animation.AnimationTrack;
import com.knoxhack.echorendercore.animation.AnimationTimeline;
import com.knoxhack.echorendercore.animation.Easing;
import com.knoxhack.echorendercore.animation.ModelPose;
import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.api.VisualVariant;
import com.knoxhack.echorendercore.profile.AnimationProfile;
import com.knoxhack.echorendercore.profile.AnimationProfileBuilder;
import com.knoxhack.echorendercore.profile.BlockPartSelectorProfile;
import com.knoxhack.echorendercore.profile.ParticleProfileBuilder;
import com.knoxhack.echorendercore.profile.ParticleProfile;
import com.knoxhack.echorendercore.profile.RenderCoreJsonParsers;
import com.knoxhack.echorendercore.profile.RenderCoreProfileValidator;
import com.knoxhack.echorendercore.profile.RenderCoreVector;
import com.knoxhack.echorendercore.profile.VisualLayerKind;
import com.knoxhack.echorendercore.profile.VisualLayerProfile;
import com.knoxhack.echorendercore.profile.VisualProfileBuilder;
import com.knoxhack.echorendercore.profile.VisualProfile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
   private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(Registries.TEST_FUNCTION, EchoRenderCore.MODID);

   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PROFILE_PARSERS =
      TEST_FUNCTIONS.register("profile_parsers", () -> ModGameTests::profileParsers);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INVALID_JSON_SAFETY =
      TEST_FUNCTIONS.register("invalid_json_safety", () -> ModGameTests::invalidJsonSafety);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ANIMATION_PLAYBACK =
      TEST_FUNCTIONS.register("animation_playback", () -> ModGameTests::animationPlayback);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> V2_PROFILE_VALIDATION =
      TEST_FUNCTIONS.register("v2_profile_validation", () -> ModGameTests::v2ProfileValidation);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> V3_LINT_REPORTS =
      TEST_FUNCTIONS.register("v3_lint_reports", () -> ModGameTests::v3LintReports);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> V4_PROFILE_TOOLS =
      TEST_FUNCTIONS.register("v4_profile_tools", () -> ModGameTests::v4ProfileTools);
   private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> V5_DIAGNOSTICS_AND_SCHEMAS =
      TEST_FUNCTIONS.register("v5_diagnostics_and_schemas", () -> ModGameTests::v5DiagnosticsAndSchemas);

   private ModGameTests() {
   }

   public static void register(IEventBus eventBus) {
      TEST_FUNCTIONS.register(eventBus);
   }

   public static void registerTests(RegisterGameTestsEvent event) {
      if (!shouldRegisterTests()) {
         return;
      }
      Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("rendercore"));
      register(event, environment, "profile_parsers", PROFILE_PARSERS.getId());
      register(event, environment, "invalid_json_safety", INVALID_JSON_SAFETY.getId());
      register(event, environment, "animation_playback", ANIMATION_PLAYBACK.getId());
      register(event, environment, "v2_profile_validation", V2_PROFILE_VALIDATION.getId());
      register(event, environment, "v3_lint_reports", V3_LINT_REPORTS.getId());
      register(event, environment, "v4_profile_tools", V4_PROFILE_TOOLS.getId());
      register(event, environment, "v5_diagnostics_and_schemas", V5_DIAGNOSTICS_AND_SCHEMAS.getId());
   }

   private static void profileParsers(GameTestHelper helper) {
      helper.assertTrue(VisualState.byName("active", VisualState.IDLE) == VisualState.ACTIVE,
         "Visual states should parse serialized names.");
      helper.assertTrue(VisualVariant.of("  DUSTY_REPAIR ").id().equals("dusty_repair"),
         "Visual variants should normalize ids.");

      VisualProfile visual = RenderCoreJsonParsers.parseVisualProfile(id("rover"), object("""
         {
           "base_texture": "echoconvoyprotocol:textures/entity/wasteland_rover.png",
           "glow_texture": "echoconvoyprotocol:textures/entity/wasteland_rover.png",
           "animation_profile": "echoconvoyprotocol:wasteland_rover",
           "particle_profile": "echoconvoyprotocol:wasteland_rover",
           "default_state": "ONLINE",
           "state_animations": { "ACTIVE": "drive" },
           "state_overlays": { "DAMAGED": ["echoconvoyprotocol:textures/entity/wasteland_rover.png"] },
           "anchors": { "exhaust_left": { "offset": [-0.7, 0.4, 1.1] } }
         }
         """));
      helper.assertTrue(visual.defaultState() == VisualState.ONLINE, "Visual profile default state should parse.");
      helper.assertTrue("drive".equals(visual.animationFor(VisualState.ACTIVE)), "State animation should parse.");
      helper.assertTrue(visual.anchor("exhaust_left") != null, "Profile anchors should parse.");

      AnimationProfile animation = RenderCoreJsonParsers.parseAnimationProfile(id("rover"), object("""
         {
           "animations": {
             "scanner_rotate": {
               "loop": true,
               "length": 4.0,
               "tracks": [
                 {
                   "part": "scanner",
                   "channel": "rotation_y",
                   "keyframes": [
                     { "time": 0.0, "value": 0.0 },
                     { "time": 4.0, "value": 360.0, "easing": "linear" }
                   ]
                 }
               ]
             }
           }
         }
         """));
      helper.assertTrue(animation.clip("scanner_rotate") != null, "Animation clips should parse.");

      ParticleProfile particles = RenderCoreJsonParsers.parseParticleProfile(id("rover"), object("""
         {
           "emitters": {
             "damaged_smoke": {
               "anchor": "exhaust_left",
               "particle": "minecraft:smoke",
               "state": "DAMAGED",
               "rate": 0.05,
               "offset": [0.0, 0.1, 0.0],
               "velocity": [0.0, 0.02, 0.0]
             },
             "dust": {
               "anchor": "wheel_back_left",
               "particle": "minecraft:dust",
               "states": ["ACTIVE", "WORKING"],
               "requires_moving": true,
               "rate": 0.1,
               "options": { "type": "minecraft:dust", "color": [0.4, 0.3, 0.2], "scale": 0.8, "lifetime": 20 }
             }
           }
         }
         """));
      helper.assertTrue(particles.emitters().containsKey("damaged_smoke"), "Particle emitters should parse.");
      helper.assertTrue(particles.emitters().get("dust").states().contains(VisualState.ACTIVE), "V2 emitter states should parse.");
      helper.assertTrue("minecraft:dust".equals(particles.emitters().get("dust").options().type()), "V3 particle option type should parse.");
      helper.assertTrue(close(particles.emitters().get("dust").options().scale(), 0.8F), "Dust particle options should parse.");
      helper.succeed();
   }

   private static void v5DiagnosticsAndSchemas(GameTestHelper helper) {
      VisualProfile visual = RenderCoreJsonParsers.parseVisualProfile(id("v5_machine"), object("""
         {
           "schema_version": 5,
           "base_texture": "echorendercore:textures/block/machine.png",
           "layers": [
             {
               "id": "active_core",
               "kind": "glow",
               "texture": "echorendercore:textures/block/machine.png",
               "states": ["ACTIVE"],
               "parts": ["powered_core", "list_core", "tinted_core"]
             }
           ],
           "block_parts": {
             "powered_core": { "directions": ["north"], "block_state": { "powered": "true" } },
             "list_core": { "directions": ["south"], "blockState": { "powered": ["true", "false"] } },
             "bad_property": { "block_state": { "missing": "true" } },
             "bad_value": { "block_state": { "powered": ["maybe", "false"] } },
             "tinted_core": { "tint_indices": [0, 4] }
           }
         }
         """));
      helper.assertTrue(visual.schemaVersion() == 5, "V5 schema version should parse.");
      helper.assertTrue(visual.blockParts().get("powered_core").blockState().get("powered").contains("true"),
         "V5 block_state string selector should parse.");
      helper.assertTrue(visual.blockParts().get("list_core").blockState().get("powered").contains("false"),
         "V5 block_state list selector should parse.");

      BlockState poweredLever = Blocks.LEVER.defaultBlockState().setValue(BlockStateProperties.POWERED, true);
      var blockReport = RenderCoreProfileValidator.validateBlockPartSelectors(visual, 1, poweredLever, Set.of(0));
      helper.assertTrue(blockReport.issues().stream().anyMatch(issue -> "block_state_property_missing".equals(issue.code())),
         "V5 block state validation should report missing properties.");
      helper.assertTrue(blockReport.issues().stream().anyMatch(issue -> "block_state_property_value_missing".equals(issue.code())),
         "V5 block state validation should report unmatched serialized values.");
      helper.assertTrue(blockReport.issues().stream().anyMatch(issue -> "block_part_tint_index_missing".equals(issue.code())),
         "V5 tint validation should report missing tint indices.");

      var diagnostics = RenderCoreProfileValidator.diagnostics(Map.of(visual.id(), visual), Map.of(), Map.of(), 3, 1, 2);
      helper.assertTrue(diagnostics.cacheMetrics().visualProfileCount() == 1, "V5 diagnostics should include visual profile count.");
      helper.assertTrue(diagnostics.cacheMetrics().discoveredJsonCount() == 3, "V5 diagnostics should preserve discovered JSON count.");
      helper.assertTrue(diagnostics.cacheMetrics().failedJsonCount() == 2, "V5 diagnostics should preserve failed JSON count.");
      helper.assertTrue(diagnostics.cacheMetrics().namespaces().contains(EchoRenderCore.MODID), "V5 metrics should collect namespaces.");
      helper.assertTrue(diagnostics.cacheMetrics().minSchemaVersion() == 5 && diagnostics.cacheMetrics().maxSchemaVersion() == 5,
         "V5 metrics should expose schema version range.");
      helper.assertTrue(diagnostics.cacheMetrics().validationWarningCount() == diagnostics.validationReport().warnings(),
         "V5 metrics should mirror merged validation warnings.");

      assertSchemaResource(helper, "visual_profile.schema.json");
      assertSchemaResource(helper, "animation_profile.schema.json");
      assertSchemaResource(helper, "particle_profile.schema.json");
      assertCommonPackagesClientSafe(helper);
      helper.succeed();
   }

   private static void invalidJsonSafety(GameTestHelper helper) {
      expectJsonFailure(helper, () -> RenderCoreJsonParsers.parseParticleProfile(id("bad_particles"), object("""
         { "emitters": { "broken": { "anchor": "core" } } }
         """)), "Missing particle should fail cleanly.");
      expectJsonFailure(helper, () -> RenderCoreJsonParsers.parseVisualProfile(id("bad_visual"), object("""
         { "anchors": { "core": [0, 1] } }
         """)), "Bad anchor vectors should fail cleanly.");
      helper.succeed();
   }

   private static void animationPlayback(GameTestHelper helper) {
      AnimationClip loop = new AnimationClip(
         "loop",
         true,
         1.0F,
         List.of(new AnimationTrack("wheel", AnimationChannel.ROTATION_X, 0.0F, 360.0F, 0.0F, 1.0F, Easing.LINEAR))
      );
      ModelPose pose = new ModelPose();
      AnimationPlayer.sample(loop, 10.0F, pose);
      helper.assertTrue(close(pose.transform("wheel").xRot(), 180.0F), "Looping clip should interpolate.");

      AnimationClip oneShot = new AnimationClip(
         "shot",
         false,
         0.5F,
         List.of(new AnimationTrack("arm", AnimationChannel.POSITION_Y, 0.0F, 1.0F, 0.0F, 0.5F, Easing.EASE_OUT))
      );
      AnimationPlayer player = new AnimationPlayer();
      player.play(oneShot, 0.0F);
      player.sample(20.0F, pose);
      helper.assertFalse(player.playing(), "One-shot player should stop after clip length.");

      AnimationController controller = new AnimationController();
      controller.bind(VisualState.ACTIVE, loop);
      controller.update(VisualState.ACTIVE, 0.0F);
      controller.sample(10.0F, pose);
      helper.assertTrue(pose.parts().contains("wheel"), "State controller should sample bound clips.");

      AnimationClip keyframed = new AnimationClip(
         "keyframed",
         true,
         2.0F,
         List.of(new AnimationTrack(
            "scanner",
            AnimationChannel.ROTATION_Y,
            0.0F,
            0.0F,
            0.0F,
            2.0F,
            Easing.LINEAR,
            new AnimationTimeline(List.of(
               new AnimationKeyframe(0.0F, 0.0F, Easing.LINEAR),
               new AnimationKeyframe(1.0F, 90.0F, Easing.LINEAR),
               new AnimationKeyframe(2.0F, 180.0F, Easing.LINEAR)
            )),
            com.knoxhack.echorendercore.animation.AnimationBlendMode.REPLACE
         ))
      );
      AnimationPlayer.sample(keyframed, 20.0F, pose);
      helper.assertTrue(close(pose.transform("scanner").yRot(), 90.0F), "Keyframed clips should interpolate.");
      helper.succeed();
   }

   private static void v2ProfileValidation(GameTestHelper helper) {
      VisualProfile visual = RenderCoreJsonParsers.parseVisualProfile(id("machine"), object("""
         {
           "schema_version": 2,
           "base_texture": "echorendercore:textures/entity/machine.png",
           "animation_profile": "echorendercore:missing_animation",
           "particle_profile": "echorendercore:machine_particles",
           "transition_seconds": 0.25,
           "layers": [
             {
               "id": "glow",
               "kind": "glow",
               "texture": "echorendercore:textures/entity/machine.png",
               "states": ["ONLINE", "WORKING"],
               "color": "#FF66E8FF",
               "alpha": 0.75,
               "emissive": true
             }
           ],
           "anchors": { "core": [0.0, 0.5, 0.0] },
           "state_animations": { "WORKING": "work_loop" }
         }
         """));
      helper.assertTrue(visual.schemaVersion() == 2, "V2 schema version should parse.");
      helper.assertTrue(close(visual.transitionSeconds(), 0.25F), "Transition seconds should parse.");
      helper.assertTrue(!visual.layersFor(VisualState.WORKING, VisualVariant.DEFAULT).isEmpty(), "V2 layers should match states.");

      ParticleProfile particles = RenderCoreJsonParsers.parseParticleProfile(id("machine_particles"), object("""
         {
           "emitters": {
             "missing_anchor": {
               "anchor": "vent",
               "particle": "minecraft:smoke",
               "state": "DAMAGED",
               "rate": 0.1
             }
           }
         }
         """));
      var report = RenderCoreProfileValidator.validate(Map.of(visual.id(), visual), Map.of(), Map.of(particles.id(), particles));
      helper.assertTrue(report.warnings() >= 2, "Validation should report missing animation profile and particle anchor warnings.");
      helper.succeed();
   }

   private static void v3LintReports(GameTestHelper helper) {
      VisualProfile visual = RenderCoreJsonParsers.parseVisualProfile(id("v3_machine"), object("""
         {
           "schema_version": 3,
           "base_texture": "echorendercore:textures/entity/machine.png",
           "particle_profile": "echorendercore:v3_particles",
           "layers": [
             {
               "id": "masked_glow",
               "kind": "glow",
               "texture": "echorendercore:textures/entity/machine.png",
               "states": ["ONLINE"],
               "parts": ["core", "missing_core"]
             }
           ],
           "anchors": { "core": [0.0, 0.5, 0.0] }
         }
         """));
      helper.assertTrue(visual.schemaVersion() == 3, "V3 schema version should parse.");
      helper.assertTrue(visual.layers().getFirst().partFilter().contains("core"), "V3 layer parts should parse.");

      ParticleProfile particles = RenderCoreJsonParsers.parseParticleProfile(id("v3_particles"), object("""
         {
           "emitters": {
             "custom": {
               "anchor": "core",
               "particle": "minecraft:dust",
               "state": "ONLINE",
               "rate": 0.1,
               "options": {
                 "type": "minecraft:dust",
                 "color": "#FF66E8FF",
                 "scale": 0.6,
                 "lifetime": 12,
                 "drag": 0.2
               }
             }
           }
         }
         """));

      var report = RenderCoreProfileValidator.validate(Map.of(visual.id(), visual), Map.of(), Map.of(particles.id(), particles));
      helper.assertTrue(report.issues().stream().anyMatch(issue -> "unsupported_particle_option".equals(issue.code())),
         "V3 validation should expose stable unsupported particle option codes.");
      helper.assertTrue(report.forNamespace(EchoRenderCore.MODID).warnings() == report.warnings(),
         "Validation reports should filter by namespace.");

      var partReport = RenderCoreProfileValidator.validateLayerParts(visual, Set.of("core"));
      helper.assertTrue(partReport.issues().stream().anyMatch(issue -> "masked_part_missing".equals(issue.code())),
         "V3 layer part lint should report missing named part masks.");
      helper.succeed();
   }

   private static void v4ProfileTools(GameTestHelper helper) {
      VisualProfile visual = RenderCoreJsonParsers.parseVisualProfile(id("v4_machine"), object("""
         {
           "schema_version": 4,
           "base_texture": "echorendercore:textures/block/machine.png",
           "layers": [
             {
               "id": "core_glow",
               "kind": "glow",
               "texture": "echorendercore:textures/block/machine.png",
               "states": ["ONLINE"],
               "parts": ["core"]
             }
           ],
           "block_parts": {
             "core": { "indices": [0, 3], "directions": ["north"], "ambient_occlusion": true },
             "empty": {}
           }
         }
         """));
      helper.assertTrue(visual.schemaVersion() == 4, "V4 schema version should parse.");
      helper.assertTrue(visual.blockParts().containsKey("core"), "V4 block part aliases should parse.");
      var blockReport = RenderCoreProfileValidator.validateBlockPartSelectors(visual, 2);
      helper.assertTrue(blockReport.issues().stream().anyMatch(issue -> "block_part_index_out_of_range".equals(issue.code())),
         "V4 block part validation should report out-of-range collected indices.");
      helper.assertTrue(blockReport.issues().stream().anyMatch(issue -> "block_part_selector_empty".equals(issue.code())),
         "V4 block part validation should report empty selectors.");

      VisualProfileBuilder visualBuilder = VisualProfileBuilder.create(id("built_visual"))
         .schemaVersion(4)
         .baseTexture(id("textures/block/built_machine.png"))
         .animationProfile(id("built_animation"))
         .particleProfile(id("built_particles"))
         .defaultState(VisualState.ONLINE)
         .anchor("core", new RenderCoreVector(0.0F, 0.5F, 0.0F))
         .blockPart("core", new BlockPartSelectorProfile("core", List.of(), Set.of(Direction.NORTH), 0, true, List.of()));
      for (int i = 0; i < 13; i++) {
         visualBuilder.layer(new VisualLayerProfile(
            "layer_" + i,
            VisualLayerKind.GLOW,
            id("textures/block/built_machine.png"),
            "default",
            Set.of(VisualState.ONLINE),
            Set.of(),
            List.of("core"),
            0xFFFFFFFF,
            1.0F,
            true
         ));
      }
      VisualProfile builtVisual = RenderCoreJsonParsers.parseVisualProfile(visualBuilder.id(), visualBuilder.toJson());
      helper.assertTrue(builtVisual.layers().size() == 13, "V4 visual builder JSON should round-trip through the parser.");

      AnimationProfileBuilder animationBuilder = AnimationProfileBuilder.create(id("built_animation"));
      animationBuilder.clip("spin", true, 1.0F)
         .keyframeTrack("core", AnimationChannel.ROTATION_Y, Easing.LINEAR,
            new AnimationProfileBuilder.Keyframe(0.0F, 0.0F),
            new AnimationProfileBuilder.Keyframe(1.0F, 360.0F))
         .endClip();
      AnimationProfile builtAnimation = RenderCoreJsonParsers.parseAnimationProfile(animationBuilder.id(), animationBuilder.toJson());
      helper.assertFalse(builtAnimation.clip("spin").tracks().getFirst().timeline().empty(),
         "V4 animation builder keyframes should round-trip.");

      ParticleProfileBuilder particleBuilder = ParticleProfileBuilder.create(id("built_particles"));
      particleBuilder.emitter("dust_ramp", "core", Identifier.withDefaultNamespace("dust"))
         .state(VisualState.ONLINE)
         .rate(9.0F)
         .burstCount(2)
         .optionType("dust_transition")
         .option("from_color", "#FF66E8FF")
         .option("to_color", "#FFFF7A28")
         .endEmitter();
      ParticleProfile builtParticles = RenderCoreJsonParsers.parseParticleProfile(particleBuilder.id(), particleBuilder.toJson());
      helper.assertTrue(builtParticles.emitters().get("dust_ramp").options().custom().containsKey("from_color"),
         "V4 particle option fields should parse.");

      var report = RenderCoreProfileValidator.validate(
         Map.of(builtVisual.id(), builtVisual),
         Map.of(builtAnimation.id(), builtAnimation),
         Map.of(builtParticles.id(), builtParticles)
      );
      helper.assertFalse(report.issues().stream().anyMatch(issue -> "unsupported_particle_option".equals(issue.code())),
         "V4 built-in particle option fields should not be reported as unsupported.");

      var performance = RenderCoreProfileValidator.analyzePerformance(
         Map.of(builtVisual.id(), builtVisual),
         Map.of(builtAnimation.id(), builtAnimation),
         Map.of(builtParticles.id(), builtParticles)
      );
      helper.assertTrue(performance.issues().stream().anyMatch(issue -> "profile_perf_high_layer_count".equals(issue.code())),
         "V4 performance diagnostics should flag high layer counts.");
      helper.assertTrue(performance.issues().stream().anyMatch(issue -> "profile_perf_high_emitter_rate".equals(issue.code())),
         "V4 performance diagnostics should flag high emitter rates.");
      helper.succeed();
   }

   private static void expectJsonFailure(GameTestHelper helper, Runnable runnable, String message) {
      try {
         runnable.run();
      } catch (JsonParseException | IllegalArgumentException exception) {
         return;
      }
      helper.fail(message);
   }

   private static boolean close(float left, float right) {
      return Math.abs(left - right) < 0.001F;
   }

   private static JsonObject object(String json) {
      return JsonParser.parseString(json).getAsJsonObject();
   }

   private static void assertSchemaResource(GameTestHelper helper, String fileName) {
      String resource = "assets/" + EchoRenderCore.MODID + "/rendercore/schemas/" + fileName;
      try (InputStream stream = ModGameTests.class.getClassLoader().getResourceAsStream(resource)) {
         helper.assertTrue(stream != null, "Schema resource should exist: " + resource);
         JsonObject schema = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
         helper.assertTrue(schema.has("$schema") && schema.has("type"), "Schema resource should parse as a JSON object: " + resource);
      } catch (IOException | IllegalStateException exception) {
         helper.fail("Schema resource should parse cleanly: " + resource + " " + exception.getMessage());
      }
   }

   private static void assertCommonPackagesClientSafe(GameTestHelper helper) {
      Path root = Path.of("src/main/java/com/knoxhack/echorendercore");
      if (!Files.exists(root)) {
         root = Path.of("addons/echorendercore/src/main/java/com/knoxhack/echorendercore");
      }
      if (!Files.exists(root)) {
         return;
      }
      Path scanRoot = root;
      try (var paths = Files.walk(root)) {
         List<Path> offenders = paths
            .filter(path -> path.toString().endsWith(".java"))
            .filter(path -> {
               String relative = scanRoot.relativize(path).toString().replace('\\', '/');
               return !relative.startsWith("client/") && !relative.startsWith("test/");
            })
            .filter(path -> {
               try {
                  return Files.readString(path).contains("net.minecraft.client");
               } catch (IOException exception) {
                  return true;
               }
            })
            .toList();
         helper.assertTrue(offenders.isEmpty(), "Common RenderCore packages must not import net.minecraft.client: " + offenders);
      } catch (IOException exception) {
         helper.fail("Could not scan RenderCore source safety: " + exception.getMessage());
      }
   }

   private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
         String testName, Identifier functionId) {
      TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
         environment,
         Identifier.withDefaultNamespace("empty"),
         100,
         0,
         true,
         net.minecraft.world.level.block.Rotation.NONE,
         false,
         1,
         1,
         false,
         2
      );
      event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
   }

   private static boolean shouldRegisterTests() {
      String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
      if (namespaces == null || namespaces.isBlank()) {
         return true;
      }
      for (String namespace : namespaces.split(",")) {
         String normalized = namespace.trim();
         if (normalized.equals(EchoRenderCore.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
            return true;
         }
      }
      return false;
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoRenderCore.MODID, path);
   }
}
