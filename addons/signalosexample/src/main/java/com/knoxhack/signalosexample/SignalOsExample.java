package com.knoxhack.signalosexample;

import com.knoxhack.signalos.api.SignalOsApi;
import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalChapter;
import com.knoxhack.signalos.api.TerminalDiagnosticProvider;
import com.knoxhack.signalos.api.TerminalMission;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(SignalOsExample.MODID)
public class SignalOsExample {
    public static final String MODID = "signalosexample";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SignalOsExample(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(SignalOsExample::registerSignalOsContent);
    }

    private static void registerSignalOsContent() {
        SignalOsApi.registerChapter(TerminalChapter.builder(id("java_ops"))
                .title("Java Ops")
                .section("system")
                .order(80)
                .accentColor(0x7AF7C1)
                .page("missions")
                .page("archives")
                .page("diagnostics")
                .build());

        SignalOsApi.registerMission(TerminalMission.builder(id("java_boot"))
                .chapter(id("java_ops").toString())
                .title("Boot the Java Link")
                .description("A mission registered entirely through the public SignalOS Java API.")
                .objective("Open a SignalOS terminal")
                .objective("Verify the diagnostics pane")
                .reward("minecraft:torch", 8)
                .build());

        SignalOsApi.registerArchive(TerminalArchiveRecord.builder(id("java_notes"))
                .chapter(id("java_ops").toString())
                .title("Java API Notes")
                .group("Developer")
                .status("OPEN")
                .line("SignalOS content can come from Java, datapack JSON, or the soft script bridge.")
                .line("The registry merge is reload-safe for JSON and script content.")
                .build());

        SignalOsApi.registerDiagnostics(new TerminalDiagnosticProvider() {
            @Override
            public Identifier id() {
                return SignalOsExample.id("example_diagnostics");
            }

            @Override
            public List<Diagnostic> diagnostics(Player player) {
                return List.of(
                        new Diagnostic(SignalOsExample.id("example_diagnostics/java_api"), "Java API",
                                "Example provider online.", Severity.INFO),
                        new Diagnostic(SignalOsExample.id("example_diagnostics/player_link"), "Player Link",
                                player == null ? "No local player context." : "Operator context available.",
                                player == null ? Severity.WARNING : Severity.INFO));
            }

            @Override
            public int order() {
                return 50;
            }
        });

        LOGGER.info("SignalOS example content registered.");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
