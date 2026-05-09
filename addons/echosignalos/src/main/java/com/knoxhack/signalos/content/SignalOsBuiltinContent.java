package com.knoxhack.signalos.content;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.api.SignalOsApi;
import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalDiagnosticProvider;
import com.knoxhack.signalos.api.TerminalPage;
import com.knoxhack.signalos.service.SignalOsTerminalServices;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class SignalOsBuiltinContent {
    private static final Identifier CORE_CHAPTER = id("signalos");

    private SignalOsBuiltinContent() {
    }

    public static void register() {
        registerCorePages();
        registerCoreArchive();
        SignalOsApi.registerDiagnostics(new CoreDiagnosticsProvider());
    }

    private static void registerCorePages() {
        SignalOsApi.registerPage(page("signalos_missions", "Missions", "missions", 0));
        SignalOsApi.registerPage(page("signalos_archives", "Archives", "archives", 10));
        SignalOsApi.registerPage(page("signalos_rewards", "Rewards", "rewards", 20));
        SignalOsApi.registerPage(page("signalos_diagnostics", "Diagnostics", "diagnostics", 30));
    }

    private static void registerCoreArchive() {
        SignalOsApi.registerArchive(TerminalArchiveRecord.builder(id("runtime_interface"))
                .chapter(CORE_CHAPTER.toString())
                .title("Runtime Interface")
                .group("SignalOS")
                .status("OPEN")
                .order(10)
                .line("SignalOS core content is registered through the Java API and merged with JSON content at reload time.")
                .line("The MVP screen currently renders missions, archives, rewards, and diagnostics page types.")
                .build());
    }

    private static TerminalPage page(String path, String title, String type, int order) {
        return TerminalPage.builder(id(path).toString())
                .chapter(CORE_CHAPTER.toString())
                .title(title)
                .type(type)
                .order(order)
                .build();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SignalOS.MODID, path);
    }

    private static final class CoreDiagnosticsProvider implements TerminalDiagnosticProvider {
        @Override
        public Identifier id() {
            return SignalOsBuiltinContent.id("core_diagnostics");
        }

        @Override
        public List<Diagnostic> diagnostics(Player player) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            int chapterCount = SignalOsContentRegistry.chapters().size();
            int missionCount = SignalOsContentRegistry.missions().size();
            int archiveCount = SignalOsContentRegistry.archives().size();
            diagnostics.add(new Diagnostic(
                    SignalOsBuiltinContent.id("core_diagnostics/content_index"),
                    "Content Index",
                    chapterCount + " chapter(s), " + missionCount + " mission(s), " + archiveCount + " archive record(s).",
                    chapterCount == 0 ? Severity.WARNING : Severity.INFO));

            diagnostics.add(new Diagnostic(
                    SignalOsBuiltinContent.id("core_diagnostics/operator_link"),
                    "Operator Link",
                    player == null ? "No local player context." : "Operator context available.",
                    player == null ? Severity.WARNING : Severity.INFO));

            int pendingRewards = player == null ? 0 : SignalOsTerminalServices.pendingRewardCount(player);
            diagnostics.add(new Diagnostic(
                    SignalOsBuiltinContent.id("core_diagnostics/reward_relay"),
                    "Reward Relay",
                    pendingRewards + " cached reward item(s) visible from the linked terminal.",
                    Severity.INFO));
            return diagnostics;
        }

        @Override
        public int order() {
            return 0;
        }
    }
}
