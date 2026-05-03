package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echoorbitalremnants.network.EchoTerminalSnapshot;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
import com.knoxhack.echoterminal.client.mission.TerminalMissionBrowser;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/**
 * Optional client-side ECHO Terminal bridge. Server actions and mission
 * providers are registered by {@link OrbitalTerminalCommonIntegration}.
 */
public final class OrbitalTerminalIntegration {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final int ACCENT = 0xFF82E9FF;

    private OrbitalTerminalIntegration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalTabRegistry.register(new OrbitalCommandTab());
        TerminalTabRegistry.register(new OrbitalSurveyTab());
        TerminalTabRegistry.register(new OrbitalEchoTab());
    }

    private static final class OrbitalCommandTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(OrbitalTerminalIds.COMMAND_TAB, "ORBITAL COMMAND", 300, ACCENT);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Orbital Command", TerminalTabChrome.GROUP_ORBITAL, "OC",
                        "ECHO-0 route telemetry", 300);
        private int lastScanX;
        private int lastScanY;
        private int lastScanW;

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            EchoTerminalSnapshot snapshot = EchoTerminalSnapshot.from(context.player());
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int h = context.contentHeight();
            boolean wide = w >= 760;
            int gap = 14;
            int telemetryH = w >= 680 ? 68 : 116;
            int availableMainH = Math.max(160, h - telemetryH - gap);
            int mainH = wide ? availableMainH : Math.max(300, availableMainH);
            int leftW = wide ? Math.max(430, Math.min(620, w * 60 / 100)) : w;
            int rightX = wide ? x + leftW + gap : x;
            int rightY = wide ? y : y + mainH + gap;
            int rightW = wide ? Math.max(260, w - leftW - gap) : w;

            TerminalUi.imagePanel(context, graphics, TerminalVisualAssets.ORBITAL_ROUTE_TELEMETRY,
                    x, y, leftW, mainH, descriptor.accentColor(), 0.72F, false);
            TerminalUi.cinematicPanel(graphics, x, y, leftW, mainH, descriptor.accentColor());
            TerminalUi.iconBadge(graphics, TerminalIcon.ORBITAL, x + 18, y + 22, 44, descriptor.accentColor(), true);
            TerminalUi.line(context, graphics, "ORBITAL COMMAND", x + 74, y + 24, leftW - 100, descriptor.accentColor());
            TerminalUi.line(context, graphics, snapshot.missionStep(), x + 74, y + 41, leftW - 100, TerminalUi.MUTED);
            int cy = TerminalUi.wrap(context, graphics, snapshot.nextObjective(), x + 18, y + 72, leftW - 36, TerminalUi.TEXT) + 6;
            cy = TerminalUi.wrap(context, graphics, snapshot.scanRequirement(), x + 18, cy, leftW - 36, TerminalUi.AMBER) + 12;

            lastScanX = x + 18;
            lastScanY = cy;
            lastScanW = Math.min(210, Math.max(150, leftW / 3));
            boolean scanHover = TerminalUi.inside(mouseX, mouseY, lastScanX, lastScanY, lastScanW, 24);
            TerminalUi.primaryCommandButton(context, graphics, lastScanX, lastScanY, lastScanW, 24,
                    "SCAN ORBITAL ROUTE", descriptor.accentColor(), scanHover);
            TerminalUi.line(context, graphics, scanReport(snapshot),
                    lastScanX + lastScanW + 14, lastScanY + 8, Math.max(80, leftW - lastScanW - 44), TerminalUi.MUTED);

            int routeBlockH = Math.min(148, Math.max(116, mainH - 168));
            int routesY = Math.min(Math.max(lastScanY + 36, y + 156), y + mainH - routeBlockH - 14);
            TerminalUi.line(context, graphics, "ROUTE WORLDS", x + 18, routesY, leftW - 36, descriptor.accentColor());
            TerminalUi.divider(graphics, x + 18, routesY + 15, leftW - 36, descriptor.accentColor());
            cy = routesY + 24;
            cy = routeWorldRow(context, graphics, x + 18, cy, leftW - 36, "LOW ORBIT", snapshot.lowOrbitReached());
            cy = routeWorldRow(context, graphics, x + 18, cy, leftW - 36, "MOON", snapshot.lunarOpen());
            cy = routeWorldRow(context, graphics, x + 18, cy, leftW - 36, "MARS", snapshot.marsOpen());
            cy = routeWorldRow(context, graphics, x + 18, cy, leftW - 36, "EUROPA", snapshot.europaOpen());
            routeWorldRow(context, graphics, x + 18, cy, leftW - 36, "NEXUS ANOMALY", snapshot.nexusOpen());

            int readinessH = Math.min(108, Math.max(82, (mainH - gap * 2) / 3));
            int sitesH = Math.min(108, Math.max(82, (mainH - gap * 2) / 3));
            int factionH = Math.max(92, mainH - readinessH - sitesH - gap * 2);

            TerminalUi.cinematicPanel(graphics, rightX, rightY, rightW, readinessH, descriptor.accentColor());
            TerminalUi.line(context, graphics, "READINESS", rightX + 16, rightY + 14, rightW - 32, descriptor.accentColor());
            int ry = rightY + 42;
            ry = readiness(context, graphics, rightX + 20, ry, rightW - 40,
                    "LAUNCH SYSTEMS", snapshot.launchReady(), snapshot.launchMissing());
            readiness(context, graphics, rightX + 20, ry, rightW - 40,
                    "ASSEMBLY SYSTEMS", snapshot.assemblyReady(), snapshot.assemblyMissing());

            int sitesY = rightY + readinessH + gap;
            TerminalUi.cinematicPanel(graphics, rightX, sitesY, rightW, sitesH, descriptor.accentColor());
            TerminalUi.line(context, graphics, "GROUND SITES", rightX + 16, sitesY + 14, rightW - 32, descriptor.accentColor());
            ry = sitesY + 42;
            if (snapshot.groundSiteLines().isEmpty()) {
                TerminalUi.line(context, graphics, "No ground site records found.", rightX + 20, ry, rightW - 40, TerminalUi.MUTED);
                TerminalUi.line(context, graphics, "Recovery sites seed from calibration scan.", rightX + 20, ry + 16, rightW - 40, TerminalUi.MUTED);
            } else {
                for (String line : snapshot.groundSiteLines()) {
                    ry = TerminalUi.wrap(context, graphics, "- " + line, rightX + 20, ry, rightW - 40, TerminalUi.TEXT) + 3;
                }
            }

            int factionY = sitesY + sitesH + gap;
            TerminalUi.cinematicPanel(graphics, rightX, factionY, rightW, factionH, descriptor.accentColor());
            TerminalUi.line(context, graphics, "FACTION CONTRACT", rightX + 16, factionY + 14, rightW - 32, descriptor.accentColor());
            ry = TerminalUi.wrap(context, graphics, snapshot.factionContract(), rightX + 20, factionY + 42, rightW - 40, TerminalUi.TEXT) + 8;
            TerminalUi.wrap(context, graphics, snapshot.missionHelp(), rightX + 20, ry, rightW - 40, TerminalUi.MUTED);

            int telemetryY = wide ? y + mainH + gap : factionY + factionH + gap;
            drawSuitTelemetry(context, graphics, snapshot, x, telemetryY, w, telemetryH, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            if (TerminalUi.inside(mouseX, mouseY, lastScanX, lastScanY, lastScanW, 24)) {
                context.sendAction(OrbitalTerminalIds.COMMAND_TAB, OrbitalTerminalIds.SCAN_ACTION, "");
                return true;
            }
            return false;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            int w = context.contentWidth();
            boolean wide = w >= 760;
            return Math.max(context.contentHeight(), wide ? 420 : 760);
        }
    }

    private static final class OrbitalSurveyTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(OrbitalTerminalIds.SURVEY_TAB, "ROUTE SURVEY", 310, 0xFF92F7A6);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Route Survey", TerminalTabChrome.GROUP_ORBITAL, "RS",
                        "Route survey network", 310);
        private int lastScanX;
        private int lastScanY;
        private int lastScanW;

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            EchoTerminalSnapshot snapshot = EchoTerminalSnapshot.from(context.player());
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int h = context.contentHeight();
            boolean wide = w >= 720;
            int gap = 14;
            int leftW = wide ? Math.max(350, Math.min(520, w * 52 / 100)) : w;
            int rightX = wide ? x + leftW + gap : x;
            int rightY = wide ? y : y + 360 + gap;
            int rightW = wide ? Math.max(260, w - leftW - gap) : w;
            int panelH = wide ? h : 360;

            TerminalUi.imagePanel(context, graphics, TerminalVisualAssets.ORBITAL_ROUTE_TELEMETRY,
                    x, y, leftW, panelH, descriptor.accentColor(), 0.68F, false);
            TerminalUi.cinematicPanel(graphics, x, y, leftW, panelH, descriptor.accentColor());
            TerminalUi.iconBadge(graphics, TerminalIcon.STATUS, x + 18, y + 22, 42, descriptor.accentColor(), true);
            TerminalUi.line(context, graphics, "ROUTE SURVEY NETWORK", x + 72, y + 23, leftW - 92, descriptor.accentColor());
            int cy = TerminalUi.wrap(context, graphics, snapshot.surveyStatus(), x + 18, y + 70, leftW - 36, TerminalUi.TEXT) + 5;
            cy = TerminalUi.wrap(context, graphics, snapshot.localHazard(), x + 18, cy, leftW - 36, TerminalUi.AMBER) + 12;

            lastScanX = x + 18;
            lastScanY = cy;
            lastScanW = Math.min(188, Math.max(138, leftW / 3));
            boolean scanHover = TerminalUi.inside(mouseX, mouseY, lastScanX, lastScanY, lastScanW, 24);
            TerminalUi.primaryCommandButton(context, graphics, lastScanX, lastScanY, lastScanW, 24,
                    "SCAN SURVEY", descriptor.accentColor(), scanHover);
            TerminalUi.line(context, graphics, scanReport(snapshot),
                    lastScanX + lastScanW + 12, lastScanY + 8, Math.max(90, leftW - lastScanW - 42), TerminalUi.MUTED);

            cy += 44;
            TerminalUi.line(context, graphics, "ROUTE REPAIR CHAINS", x + 18, cy, leftW - 36, descriptor.accentColor());
            TerminalUi.divider(graphics, x + 18, cy + 15, leftW - 36, descriptor.accentColor());
            cy += 25;
            for (String line : repairLines(snapshot.surveyLines())) {
                cy = surveyRow(context, graphics, x + 18, cy, leftW - 36, line);
            }
            cy += 8;
            TerminalUi.line(context, graphics, "CURRENT ROUTE SURVEYS", x + 18, cy, leftW - 36, descriptor.accentColor());
            TerminalUi.divider(graphics, x + 18, cy + 15, leftW - 36, descriptor.accentColor());
            cy += 25;
            for (String line : routeSurveyLines(snapshot.surveyLines())) {
                cy = surveyRow(context, graphics, x + 18, cy, leftW - 36, line);
            }

            int rightH = wide ? h : 268;
            TerminalUi.cinematicPanel(graphics, rightX, rightY, rightW, rightH, descriptor.accentColor());
            TerminalUi.line(context, graphics, "ECHO HELP", rightX + 16, rightY + 14, rightW - 32, descriptor.accentColor());
            int ry = TerminalUi.wrap(context, graphics, snapshot.missionHelp(), rightX + 18, rightY + 40,
                    rightW - 36, TerminalUi.TEXT) + 12;
            TerminalUi.line(context, graphics, "GROUND RECOVERY", rightX + 16, ry, rightW - 32, descriptor.accentColor());
            ry += 22;
            if (snapshot.groundSiteLines().isEmpty()) {
                TerminalUi.wrap(context, graphics, "No Earth recovery sites are currently tracked.",
                        rightX + 18, ry, rightW - 36, TerminalUi.MUTED);
            } else {
                for (String line : snapshot.groundSiteLines()) {
                    ry = TerminalUi.wrap(context, graphics, "- " + line, rightX + 18, ry, rightW - 36, TerminalUi.TEXT) + 3;
                }
            }
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            if (TerminalUi.inside(mouseX, mouseY, lastScanX, lastScanY, lastScanW, 24)) {
                context.sendAction(OrbitalTerminalIds.SURVEY_TAB, OrbitalTerminalIds.SCAN_ACTION, "");
                return true;
            }
            return false;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return Math.max(context.contentHeight(), context.contentWidth() >= 720 ? 560 : 660);
        }
    }

    private static final class OrbitalEchoTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(OrbitalTerminalIds.ECHO_TAB, "ECHO-0 RECORDS", 320, 0xFFFFD166);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("ECHO-0 Records", TerminalTabChrome.GROUP_ORBITAL, "E0",
                        "Orbital mission records", 320);
        private final TerminalMissionBrowser browser =
                new TerminalMissionBrowser(OrbitalMissionProvider.INSTANCE, descriptor.id(), true);

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void onSelected(TerminalRenderContext context) {
            browser.onSelected(context);
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            browser.render(context, graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            return browser.mouseClicked(context, mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(TerminalRenderContext context, double mouseX, double mouseY, double delta) {
            return browser.mouseScrolled(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
            return browser.keyPressed(context, event);
        }

        @Override
        public boolean charTyped(TerminalRenderContext context, CharacterEvent event) {
            return browser.charTyped(context, event);
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return browser.contentHeight(context);
        }
    }

    private static int routeWorldRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, boolean open) {
        graphics.fill(x, y, x + width, y + 20, 0x88071117);
        graphics.outline(x, y, width, 20, 0x33244352);
        TerminalIcon.ORBITAL.draw(graphics, x + 8, y + 4, 12, open ? TerminalUi.GREEN : TerminalUi.MUTED, open);
        TerminalUi.line(context, graphics, label, x + 28, y + 6, width - 108, open ? TerminalUi.TEXT : TerminalUi.MUTED);
        TerminalUi.line(context, graphics, open ? "OPEN" : "LOCKED", x + width - 84, y + 6, 74,
                open ? TerminalUi.GREEN : TerminalUi.RED);
        return y + 22;
    }

    private static int readiness(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, boolean ready, List<String> missing) {
        TerminalUi.line(context, graphics, label + ": " + (ready ? "READY" : "BLOCKED"), x, y, width,
                ready ? TerminalUi.GREEN : TerminalUi.AMBER);
        int cy = y + 12;
        for (String line : missing) {
            cy = TerminalUi.wrap(context, graphics, "- " + line, x + 8, cy, width - 8, TerminalUi.MUTED) + 3;
        }
        return cy + 4;
    }

    private static int surveyRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String line) {
        boolean complete = line.contains("3/3") || line.contains("BYPASS");
        int color = complete ? TerminalUi.GREEN : TerminalUi.TEXT;
        graphics.fill(x, y, x + width, y + 23, 0x88071117);
        graphics.outline(x, y, width, 23, 0x33244352);
        TerminalIcon.STATUS.draw(graphics, x + 8, y + 5, 12, color, complete);
        TerminalUi.line(context, graphics, line, x + 28, y + 7, width - 36, color);
        return y + 26;
    }

    private static List<String> repairLines(List<String> lines) {
        return lines.stream().filter(line -> line.startsWith("Route ")).limit(4).toList();
    }

    private static List<String> routeSurveyLines(List<String> lines) {
        return lines.stream().filter(line -> !line.startsWith("Route ")).limit(5).toList();
    }

    private static void drawSuitTelemetry(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            EchoTerminalSnapshot snapshot, int x, int y, int w, int h, int mouseX, int mouseY) {
        TerminalUi.cinematicPanel(graphics, x, y, w, h, ACCENT);
            TerminalUi.iconBadge(graphics, TerminalIcon.STATUS, x + 18, y + 15, 34, ACCENT, true);
            TerminalUi.line(context, graphics, "SUIT TELEMETRY", x + 62, y + 15, 120, ACCENT);
            if (w >= 680) {
                int meterX = x + 198;
                int meterW = Math.max(78, (w - 326) / 4);
                TerminalUi.compactMeter(context, graphics, meterX, y + 15, meterW, "OXYGEN", snapshot.oxygen(), TerminalUi.CYAN);
                TerminalUi.compactMeter(context, graphics, meterX + meterW + 16, y + 15, meterW, "PRESSURE", snapshot.pressure(), TerminalUi.GREEN);
                TerminalUi.compactMeter(context, graphics, meterX + (meterW + 16) * 2, y + 15, meterW, "RADIATION", snapshot.radiation(), TerminalUi.RED);
                TerminalUi.compactMeter(context, graphics, meterX + (meterW + 16) * 3, y + 15, meterW, "POWER", snapshot.stationPower(), TerminalUi.CYAN);
                TerminalUi.line(context, graphics, "Seal " + (snapshot.sealSecure() ? "secure" : "open")
                        + " / Leak " + (snapshot.suitLeak() ? "detected" : "none")
                        + " / Gravity " + snapshot.gravity(), x + 62, y + 44, w - 90, TerminalUi.MUTED);
        } else {
            int meterW = Math.max(110, (w - 52) / 2);
            TerminalUi.compactMeter(context, graphics, x + 18, y + 58, meterW, "OXYGEN", snapshot.oxygen(), TerminalUi.CYAN);
            TerminalUi.compactMeter(context, graphics, x + 34 + meterW, y + 58, meterW, "PRESSURE", snapshot.pressure(), TerminalUi.GREEN);
            TerminalUi.compactMeter(context, graphics, x + 18, y + 88, meterW, "RADIATION", snapshot.radiation(), TerminalUi.RED);
            TerminalUi.compactMeter(context, graphics, x + 34 + meterW, y + 88, meterW, "POWER", snapshot.stationPower(), TerminalUi.CYAN);
        }
    }

    private static String scanReport(EchoTerminalSnapshot snapshot) {
        return snapshot.scanReport().isBlank() ? "Terminal link ready." : snapshot.scanReport();
    }
}
