package com.knoxhack.echomultiblockcore;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING = BUILDER
            .comment("Enables verbose MultiblockCore validation/runtime logging.")
            .define("debug.enableDebugLogging", false);
    public static final ModConfigSpec.BooleanValue ENABLE_PREVIEW_RENDERING = BUILDER
            .comment("Enables blueprint and controller preview overlays on clients.")
            .define("client.enablePreviewRendering", true);
    public static final ModConfigSpec.IntValue PREVIEW_MAX_RENDER_CELLS = BUILDER
            .comment("Maximum multiblock preview cells rendered per frame on clients.")
            .defineInRange("client.previewMaxRenderCells", 1024, 64, 4096);
    public static final ModConfigSpec.IntValue MAX_VALIDATION_VOLUME = BUILDER
            .comment("Maximum block volume a single multiblock validation may scan.")
            .defineInRange("validation.maxValidationVolume", 4096, 1, 65536);
    public static final ModConfigSpec.IntValue MAX_ACTIVE_MULTIBLOCKS_PER_CHUNK = BUILDER
            .comment("Maximum active formed multiblocks recorded per chunk.")
            .defineInRange("runtime.maxActiveMultiblocksPerChunk", 16, 1, 256);
    public static final ModConfigSpec.DoubleValue ROBOTIC_TASK_SPEED_MULTIPLIER = BUILDER
            .comment("Multiplier applied to robotic task speed. Higher is faster.")
            .defineInRange("robotics.roboticTaskSpeedMultiplier", 1.0D, 0.1D, 10.0D);
    public static final ModConfigSpec.BooleanValue ENABLE_ROBOTIC_ANIMATIONS = BUILDER
            .comment("Enables visible robotic task animation packets and particles.")
            .define("robotics.enableRoboticAnimations", true);
    public static final ModConfigSpec.BooleanValue ALLOW_AUTO_REPAIR = BUILDER
            .comment("Allows formed multiblocks to queue repair tasks when suitable tools exist.")
            .define("robotics.allowAutoRepair", true);
    public static final ModConfigSpec.BooleanValue ALLOW_CHUNK_LOADED_TICKING = BUILDER
            .comment("Allows formed multiblocks to tick while only chunk-loaded. Default false for safety.")
            .define("runtime.allowChunkLoadedTicking", false);
    public static final ModConfigSpec.IntValue AUTO_BUILDER_MAX_PLACEMENTS = BUILDER
            .comment("Maximum blocks an Auto Builder action may place at once.")
            .defineInRange("construction.autoBuilderMaxPlacements", 16, 1, 256);
    public static final ModConfigSpec.BooleanValue REQUIRE_AUTO_BUILDER_COMPONENT = BUILDER
            .comment("Requires a discovered Auto Builder component before construction automation can place blocks.")
            .define("construction.requireAutoBuilderComponent", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static void registerEchoConfig() {
        EchoConfigRegistry.register(EchoConfigProvider.of(EchoMultiblockCore.MODID, () -> new EchoConfigModule(
                EchoMultiblockCore.MODID,
                "MultiblockCore",
                List.of(
                        new EchoConfigCategory("client", "Client", List.of(
                                EchoConfigEntry.booleanSpec("enable_preview_rendering", "Enable Preview Rendering",
                                        "Enables blueprint and controller preview overlays on clients.",
                                        EchoConfigSide.CLIENT, ENABLE_PREVIEW_RENDERING, true, false, false),
                                EchoConfigEntry.intSpec("preview_max_render_cells", "Preview Max Render Cells",
                                        "Maximum multiblock preview cells rendered per frame.",
                                        EchoConfigSide.CLIENT, PREVIEW_MAX_RENDER_CELLS, 64, 4096, true, false, false))),
                        new EchoConfigCategory("validation", "Validation", List.of(
                                EchoConfigEntry.intSpec("max_validation_volume", "Max Validation Volume",
                                        "Maximum block volume a single multiblock validation may scan.",
                                        EchoConfigSide.COMMON, MAX_VALIDATION_VOLUME, 1, 65536, true, false, false))),
                        new EchoConfigCategory("robotics", "Robotics", List.of(
                                EchoConfigEntry.booleanSpec("enable_robotic_animations", "Enable Robotic Animations",
                                        "Enables visible robotic task animation packets and particles.",
                                        EchoConfigSide.COMMON, ENABLE_ROBOTIC_ANIMATIONS, true, false, false),
                                EchoConfigEntry.booleanSpec("allow_auto_repair", "Allow Auto Repair",
                                        "Allows formed multiblocks to queue repair tasks when suitable tools exist.",
                                        EchoConfigSide.COMMON, ALLOW_AUTO_REPAIR, true, false, false))),
                        new EchoConfigCategory("construction", "Construction", List.of(
                                EchoConfigEntry.intSpec("auto_builder_max_placements", "Auto Builder Max Placements",
                                        "Maximum blocks an Auto Builder action may place at once.",
                                        EchoConfigSide.COMMON, AUTO_BUILDER_MAX_PLACEMENTS, 1, 256, true, false, false),
                                EchoConfigEntry.booleanSpec("require_auto_builder_component", "Require Auto Builder Component",
                                        "Requires a discovered Auto Builder component before construction automation can place blocks.",
                                        EchoConfigSide.COMMON, REQUIRE_AUTO_BUILDER_COMPONENT, true, false, false)))))));
    }
}
