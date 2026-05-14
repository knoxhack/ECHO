package com.knoxhack.echoruntimeguard.report;

import com.knoxhack.echoruntimeguard.api.ParticleBudgetSnapshot;
import com.knoxhack.echoruntimeguard.api.ProfilerEntry;
import com.knoxhack.echoruntimeguard.api.RuntimeGuardProfiler;
import com.knoxhack.echoruntimeguard.api.RuntimeMetricsSnapshot;
import com.knoxhack.echoruntimeguard.api.ValidationQueueSnapshot;
import com.knoxhack.echoruntimeguard.runtime.BlockEntitySleepService;
import com.knoxhack.echoruntimeguard.runtime.EntityAiGuardService;
import com.knoxhack.echoruntimeguard.runtime.IntegrationThrottleService;
import com.knoxhack.echoruntimeguard.runtime.LagSpikeReporter;
import com.knoxhack.echoruntimeguard.runtime.MultiblockValidationScheduler;
import com.knoxhack.echoruntimeguard.runtime.NetworkBudgetService;
import com.knoxhack.echoruntimeguard.runtime.ParticleBudgetService;
import com.knoxhack.echoruntimeguard.runtime.RuntimeModeService;
import com.knoxhack.echoruntimeguard.runtime.RuntimeProfilerService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public final class RuntimeGuardReportWriter {
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private RuntimeGuardReportWriter() {
    }

    public static Path write(MinecraftServer server) throws IOException {
        RuntimeMetricsSnapshot metrics = RuntimeProfilerService.INSTANCE.snapshot(server);
        ParticleBudgetSnapshot particles = ParticleBudgetService.INSTANCE.getSnapshot();
        ValidationQueueSnapshot validations = MultiblockValidationScheduler.INSTANCE.getSnapshot();
        Path root = Path.of("echo-runtimeguard", "reports");
        Files.createDirectories(root);
        Path report = root.resolve("runtimeguard-report-" + LocalDateTime.now().format(FILE_STAMP) + ".txt");
        Files.writeString(report, content(metrics, particles, validations), StandardCharsets.UTF_8);
        return report;
    }

    private static String content(RuntimeMetricsSnapshot metrics, ParticleBudgetSnapshot particles,
            ValidationQueueSnapshot validations) {
        StringBuilder builder = new StringBuilder();
        builder.append("ECHO RuntimeGuard Performance Report\n\n");
        builder.append("General:\n");
        builder.append("- Runtime mode: ").append(metrics.mode().displayName()).append('\n');
        builder.append("- Emergency mode: ").append(metrics.emergency() ? "on" : "off").append('\n');
        builder.append("- Average TPS: ").append(format(metrics.averageTps())).append('\n');
        builder.append("- Average MSPT: ").append(format(metrics.averageMspt())).append('\n');
        builder.append("- Worst MSPT: ").append(format(metrics.worstMsptLastMinute())).append('\n');
        builder.append("- Lag spikes: ").append(metrics.lagSpikesLastMinute()).append('\n');
        builder.append("- Uptime sample window: ").append(metrics.sampledTicks()).append(" tick(s)\n\n");

        builder.append("World:\n");
        builder.append("- Loaded chunks: ").append(metrics.loadedChunks()).append('\n');
        builder.append("- Entity count: ").append(metrics.entityCount()).append('\n');
        builder.append("- Block entity count: ").append(metrics.blockEntityCount()).append('\n');
        builder.append("- Players: ").append(metrics.players()).append("\n\n");

        builder.append("Budgets:\n");
        builder.append("- Particle budget/current usage: ").append(particles.budget()).append('/')
                .append(particles.used()).append(" denied ").append(particles.denied()).append('\n');
        builder.append("- Multiblock validation queue size: ").append(validations.queued())
                .append(" merged ").append(validations.mergedRequests()).append('\n');
        builder.append("- Lens scan budget: ").append(IntegrationThrottleService.INSTANCE.getDeepScanBudgetPerTick(null))
                .append(" deep units/tick\n");
        builder.append("- HoloMap refresh interval: ").append(IntegrationThrottleService.INSTANCE.getHoloMapRefreshIntervalTicks())
                .append(" ticks\n");
        builder.append("- Network packet warnings: ").append(NetworkBudgetService.INSTANCE.getSnapshot().warnings()).append("\n\n");

        builder.append("Top Issues:\n");
        List<String> warnings = LagSpikeReporter.INSTANCE.warnings();
        if (warnings.isEmpty()) {
            builder.append("- none recorded\n");
        } else {
            warnings.stream().limit(8).forEach(warning -> builder.append("- ").append(warning).append('\n'));
        }
        builder.append('\n');

        builder.append("Auto Actions Taken:\n");
        builder.append("- particle reduction: ").append(RuntimeModeService.INSTANCE.isEmergency() ? "active" : "budgeted").append('\n');
        builder.append("- emergency mode activation: ").append(RuntimeModeService.INSTANCE.automaticEmergency() ? "auto" : RuntimeModeService.INSTANCE.forcedEmergency() ? "forced" : "inactive").append('\n');
        builder.append("- multiblock queue limiting: active\n");
        builder.append("- HoloMap refresh throttle: active\n");
        builder.append("- far AI throttle: opt-in only\n\n");

        builder.append("Top Profiled Costs:\n");
        List<ProfilerEntry> entries = RuntimeGuardProfiler.getTopCosts();
        if (entries.isEmpty()) {
            builder.append("- unavailable; no RuntimeGuardProfiler-wrapped operations recorded\n");
        } else {
            entries.forEach(entry -> builder.append("- ").append(entry.id()).append(": ")
                    .append(format(entry.totalMillis())).append("ms total, ")
                    .append(format(entry.averageMillis())).append("ms avg, calls ")
                    .append(entry.calls()).append('\n'));
        }
        builder.append('\n');

        builder.append("Recommendations:\n");
        builder.append("- reduce particle mode if particle usage reaches budget\n");
        builder.append("- increase HoloMap refresh interval if marker refresh appears in top costs\n");
        builder.append("- lower multiblock validations per tick if validation queues spike MSPT\n");
        builder.append("- reduce entity spawn caps if entity count becomes available and high\n");
        builder.append("- enable Server mode for dedicated-server-heavy packs\n");
        builder.append("- inspect specific modules that call RuntimeGuardProfiler wrappers\n\n");

        builder.append("RuntimeGuard State:\n");
        builder.append("- Block entity sleep registry: ").append(BlockEntitySleepService.INSTANCE.tracked()).append('\n');
        builder.append("- Entity AI guard: ").append(EntityAiGuardService.INSTANCE.statusLine()).append('\n');
        builder.append("- Integration guard: ").append(IntegrationThrottleService.INSTANCE.statusLine()).append('\n');
        return builder.toString();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
