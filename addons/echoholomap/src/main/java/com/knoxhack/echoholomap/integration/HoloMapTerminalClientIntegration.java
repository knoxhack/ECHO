package com.knoxhack.echoholomap.integration;

import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.HoloMapIds;
import com.knoxhack.echoholomap.map.HoloMapTerrainTile;
import com.knoxhack.echoholomap.network.HoloMapClientState;
import com.knoxhack.echoholomap.network.HoloMapSnapshotPacket;
import com.knoxhack.echoholomap.network.HoloMapTerrainClientState;
import com.knoxhack.echoholomap.network.HoloMapTileRequestPacket;
import com.knoxhack.echoterminal.api.TerminalNavigationProfile;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HoloMapTerminalClientIntegration {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final int ACCENT = 0xFF38DFF4;

    private HoloMapTerminalClientIntegration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalTab tab = new HoloMapTab();
        TerminalTabRegistry.register(tab);
        TerminalNavigationProfiles.register(tab.descriptor().id(), TerminalNavigationProfile.intel(185));
    }

    private enum StateFilter {
        ALL("ALL"),
        OPEN("OPEN"),
        LOCKED("LOCKED"),
        CHECKED("DONE");

        private final String label;

        StateFilter(String label) {
            this.label = label;
        }

        private StateFilter next() {
            StateFilter[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private static final class HoloMapTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor = new TerminalTabDescriptor(HoloMapIds.TAB, "HOLOMAP", 185, ACCENT);
        private final TerminalTabChrome chrome = TerminalTabChrome.of("HoloMap", TerminalTabChrome.GROUP_FIELD,
                "HM", "World telemetry map", 185);
        private final Map<Identifier, Boolean> layerEnabled = new LinkedHashMap<>();
        private final List<Button> buttons = new ArrayList<>();
        private final List<MarkerHit> markerHits = new ArrayList<>();
        private StateFilter stateFilter = StateFilter.ALL;
        private String selectedMarkerId = "";
        private int detailMode = 1;
        private long lastRefreshTick = -200L;
        private double centerX;
        private double centerZ;
        private double zoom = 1.35D;
        private boolean cameraReady;
        private long lastTerrainRequestTick = -200L;
        private int lastRequestChunkX = Integer.MIN_VALUE;
        private int lastRequestChunkZ = Integer.MIN_VALUE;
        private int lastRequestRadius = -1;
        private int lastMapX;
        private int lastMapY;
        private int lastMapW;
        private int lastMapH;

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
            requestRefresh(context);
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                float partialTick) {
            maybeRefresh(context);
            buttons.clear();
            markerHits.clear();

            HoloMapSnapshotPacket snapshot = HoloMapClientState.snapshot();
            syncLayers(snapshot.layers());
            int x = context.contentX() + 12;
            int y = context.contentY() + 10 - context.scrollY();
            int w = context.contentWidth() - 24;
            y = TerminalUi.sectionHeader(context, graphics, "HOLOMAP", "COMMAND MAP", x, y, w, ACCENT);

            int controlH = 70;
            TerminalUi.flatHudPanel(context, graphics, x, y, w, controlH, ACCENT);
            TerminalUi.line(context, graphics, snapshot.statusLine(), x + 12, y + 10, w - 192,
                    TerminalUi.accent(context));
            button(graphics, context, "CENTER", x + w - 230, y + 8, 58, true, mouseX, mouseY, ButtonKind.CENTER, "");
            button(graphics, context, "SYNC", x + w - 166, y + 8, 70, true, mouseX, mouseY, ButtonKind.REFRESH, "");
            button(graphics, context, "TEST", x + w - 88, y + 8, 68, true, mouseX, mouseY, ButtonKind.TEST, "");
            button(graphics, context, "STATE " + stateFilter.label, x + w - 166, y + 34, 104, true,
                    mouseX, mouseY, ButtonKind.STATE, "");
            button(graphics, context, detailMode == 0 ? "LOW" : detailMode == 1 ? "MED" : "HIGH",
                    x + w - 54, y + 34, 34, true, mouseX, mouseY, ButtonKind.DETAIL, "");
            drawLayerButtons(context, graphics, snapshot.layers(), x + 12, y + 34, Math.max(120, w - 194),
                    mouseX, mouseY);

            int bodyY = y + controlH + 12;
            int bodyH = Math.max(330, context.contentHeight() - 116);
            int detailW = Math.min(220, Math.max(174, w / 3));
            int mapW = Math.max(260, w - detailW - 12);
            List<HoloMapSnapshotPacket.MarkerData> visible = visibleMarkers(snapshot.markers());
            drawMap(context, graphics, visible, x, bodyY, mapW, bodyH, mouseX, mouseY, snapshot.gameTime());
            drawDetailPanel(context, graphics, snapshot, visible, x + mapW + 12, bodyY, detailW, bodyH);
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            for (Button hit : buttons) {
                if (!TerminalUi.inside(mouseX, mouseY, hit.x(), hit.y(), hit.w(), hit.h())) {
                    continue;
                }
                switch (hit.kind()) {
                    case CENTER -> centerOnPlayer(context);
                    case REFRESH -> requestRefresh(context);
                    case TEST -> context.sendAction(HoloMapIds.TAB, HoloMapIds.TEST_MARKER_ACTION, "drones_scans");
                    case STATE -> stateFilter = stateFilter.next();
                    case DETAIL -> detailMode = (detailMode + 1) % 3;
                    case LAYER -> layerEnabled.put(hit.layerId(), !layerEnabled.getOrDefault(hit.layerId(), true));
                }
                return true;
            }
            for (MarkerHit hit : markerHits) {
                if (TerminalUi.inside(mouseX, mouseY, hit.x() - 5, hit.y() - 5, 10, 10)) {
                    selectedMarkerId = hit.marker().id().toString();
                    context.playCommandSound();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(TerminalRenderContext context, double mouseX, double mouseY, double delta) {
            if (!TerminalUi.inside(mouseX, mouseY, lastMapX, lastMapY, lastMapW, lastMapH)) {
                return false;
            }
            double worldX = screenToWorldX(mouseX);
            double worldZ = screenToWorldZ(mouseY);
            double before = zoom;
            zoom = clamp(zoom * (delta > 0.0D ? 1.2D : 0.82D), 0.35D, 6.0D);
            if (before != zoom && lastMapW > 0 && lastMapH > 0) {
                double afterX = screenToWorldX(mouseX);
                double afterZ = screenToWorldZ(mouseY);
                centerX += worldX - afterX;
                centerZ += worldZ - afterZ;
            }
            requestTerrain(context, true);
            return true;
        }

        @Override
        public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
            int key = event.key();
            double pan = Math.max(16.0D, 72.0D / Math.max(0.35D, zoom));
            if (key == GLFW.GLFW_KEY_LEFT) {
                centerX -= pan;
            } else if (key == GLFW.GLFW_KEY_RIGHT) {
                centerX += pan;
            } else if (key == GLFW.GLFW_KEY_UP) {
                centerZ -= pan;
            } else if (key == GLFW.GLFW_KEY_DOWN) {
                centerZ += pan;
            } else if (key == GLFW.GLFW_KEY_C || key == GLFW.GLFW_KEY_HOME) {
                centerOnPlayer(context);
            } else if (key == GLFW.GLFW_KEY_EQUAL || key == GLFW.GLFW_KEY_KP_ADD) {
                zoom = clamp(zoom * 1.2D, 0.35D, 6.0D);
            } else if (key == GLFW.GLFW_KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
                zoom = clamp(zoom * 0.82D, 0.35D, 6.0D);
            } else {
                return false;
            }
            cameraReady = true;
            requestTerrain(context, true);
            return true;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return 620;
        }

        private void maybeRefresh(TerminalRenderContext context) {
            if (context.player() == null) {
                return;
            }
            long now = context.player().level().getGameTime();
            if (HoloMapClientState.snapshot().gameTime() == 0L || now - lastRefreshTick > 120L) {
                requestRefresh(context);
            }
        }

        private void requestRefresh(TerminalRenderContext context) {
            if (context.player() == null) {
                return;
            }
            lastRefreshTick = context.player().level().getGameTime();
            context.sendAction(HoloMapIds.TAB, HoloMapIds.REFRESH_ACTION, "");
        }

        private void ensureCamera(TerminalRenderContext context) {
            if (cameraReady || context.player() == null) {
                return;
            }
            centerOnPlayer(context);
        }

        private void centerOnPlayer(TerminalRenderContext context) {
            if (context.player() == null) {
                return;
            }
            centerX = context.player().getX();
            centerZ = context.player().getZ();
            cameraReady = true;
            requestTerrain(context, true);
        }

        private void requestTerrain(TerminalRenderContext context, boolean force) {
            if (context.player() == null || lastMapW <= 0 || lastMapH <= 0) {
                return;
            }
            long now = context.player().level().getGameTime();
            int centerChunkX = Math.floorDiv((int) Math.floor(centerX), 16);
            int centerChunkZ = Math.floorDiv((int) Math.floor(centerZ), 16);
            int radius = visibleChunkRadius();
            if (!force
                    && now - lastTerrainRequestTick < 20L
                    && centerChunkX == lastRequestChunkX
                    && centerChunkZ == lastRequestChunkZ
                    && radius == lastRequestRadius) {
                return;
            }
            lastTerrainRequestTick = now;
            lastRequestChunkX = centerChunkX;
            lastRequestChunkZ = centerChunkZ;
            lastRequestRadius = radius;
            EchoNetClientActions.sendServerboundAction(new HoloMapTileRequestPacket(
                    context.player().level().dimension().identifier().toString(),
                    centerChunkX,
                    centerChunkZ,
                    radius));
        }

        private int visibleChunkRadius() {
            double blocksAcross = Math.max(lastMapW, lastMapH) / Math.max(0.35D, zoom);
            return Math.max(1, Math.min(32, (int) Math.ceil(blocksAcross / 32.0D) + 1));
        }

        private int worldToScreenX(double worldX) {
            return lastMapX + lastMapW / 2 + (int) Math.round((worldX - centerX) * zoom);
        }

        private int worldToScreenZ(double worldZ) {
            return lastMapY + lastMapH / 2 + (int) Math.round((worldZ - centerZ) * zoom);
        }

        private double screenToWorldX(double screenX) {
            return centerX + (screenX - (lastMapX + lastMapW / 2.0D)) / Math.max(0.35D, zoom);
        }

        private double screenToWorldZ(double screenY) {
            return centerZ + (screenY - (lastMapY + lastMapH / 2.0D)) / Math.max(0.35D, zoom);
        }

        private void syncLayers(List<HoloMapSnapshotPacket.LayerData> layers) {
            for (HoloMapSnapshotPacket.LayerData layer : layers) {
                layerEnabled.putIfAbsent(layer.id(), layer.visibleByDefault());
            }
        }

        private void drawLayerButtons(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                List<HoloMapSnapshotPacket.LayerData> layers, int x, int y, int width, int mouseX, int mouseY) {
            int cx = x;
            int cy = y;
            for (HoloMapSnapshotPacket.LayerData layer : layers) {
                String label = layerLabel(layer.title());
                int bw = Math.max(48, Math.min(84, label.length() * 6 + 16));
                if (cx + bw > x + width) {
                    cx = x;
                    cy += 20;
                }
                boolean enabled = layerEnabled.getOrDefault(layer.id(), layer.visibleByDefault());
                int color = enabled ? layer.color() : TerminalUi.accentDim(context);
                TerminalUi.compactButton(context, graphics, cx, cy, bw, label, color, true,
                        TerminalUi.inside(mouseX, mouseY, cx, cy, bw, 16));
                buttons.add(new Button(cx, cy, bw, 16, ButtonKind.LAYER, layer.id()));
                cx += bw + 6;
                if (cy > y + 20) {
                    break;
                }
            }
        }

        private void drawMap(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                List<HoloMapSnapshotPacket.MarkerData> markers, int x, int y, int w, int h,
                int mouseX, int mouseY, long gameTime) {
            ensureCamera(context);
            lastMapX = x;
            lastMapY = y;
            lastMapW = w;
            lastMapH = h;
            requestTerrain(context, false);
            TerminalUi.flatHudPanel(context, graphics, x, y, w, h, ACCENT);
            graphics.enableScissor(x + 4, y + 4, x + w - 4, y + h - 4);
            int terrainTiles = drawTerrain(context, graphics, x, y, w, h, gameTime);
            drawWorldGrid(context, graphics, x, y, w, h);
            if (markers.isEmpty()) {
                TerminalUi.line(context, graphics, terrainTiles == 0
                                ? "Terrain scan pending. Move through loaded chunks or run debug scan_terrain."
                                : "No synced markers for the current filters.",
                        x + 16, y + 22, w - 32, TerminalUi.muted(context));
                graphics.disableScissor();
                return;
            }
            Map<String, List<MarkerPoint>> routePoints = new LinkedHashMap<>();
            List<MarkerPoint> points = new ArrayList<>();
            for (HoloMapSnapshotPacket.MarkerData marker : markers) {
                int px = worldToScreenX(marker.x());
                int py = worldToScreenZ(marker.z());
                if (px < x - 48 || px > x + w + 48 || py < y - 48 || py > y + h + 48) {
                    continue;
                }
                MarkerPoint point = new MarkerPoint(marker, px, py);
                points.add(point);
                if (!marker.routeId().isBlank()) {
                    routePoints.computeIfAbsent(marker.routeId(), ignored -> new ArrayList<>()).add(point);
                }
            }
            for (List<MarkerPoint> route : routePoints.values()) {
                route.sort(Comparator.comparingInt(point -> point.marker().routeOrder()));
                for (int i = 1; i < route.size(); i++) {
                    drawLine(graphics, route.get(i - 1).x(), route.get(i - 1).y(),
                            route.get(i).x(), route.get(i).y(), 0xAA92F7A6);
                }
            }
            int labels = detailLabelLimit();
            int drawnLabels = 0;
            for (MarkerPoint point : points) {
                boolean hovered = TerminalUi.inside(mouseX, mouseY, point.x() - 5, point.y() - 5, 10, 10);
                boolean selected = point.marker().id().toString().equals(selectedMarkerId);
                drawMarker(context, graphics, point, hovered || selected);
                markerHits.add(new MarkerHit(point.marker(), point.x(), point.y()));
                if (hovered || selected || drawnLabels++ < labels && point.marker().precise()) {
                    TerminalUi.line(context, graphics, point.marker().title(), point.x() + 8, point.y() - 4,
                            124, selected ? TerminalUi.text(context) : colorForMarker(context, point.marker()));
                }
            }
            TerminalUi.line(context, graphics, "ECHO TERRAIN ATLAS | " + terrainTiles + " cached / "
                            + HoloMapTerrainClientState.discoveredCount() + " discovered | "
                            + String.format(Locale.ROOT, "%.2fx", zoom),
                    x + 12, y + 10, w - 24, TerminalUi.accent(context));
            graphics.disableScissor();
        }

        private int drawTerrain(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int w, int h, long gameTime) {
            String dimension = context.player() == null
                    ? "minecraft:overworld"
                    : context.player().level().dimension().identifier().toString();
            int minChunkX = Math.floorDiv((int) Math.floor(screenToWorldX(x + 4)), 16) - 1;
            int maxChunkX = Math.floorDiv((int) Math.floor(screenToWorldX(x + w - 4)), 16) + 1;
            int minChunkZ = Math.floorDiv((int) Math.floor(screenToWorldZ(y + 4)), 16) - 1;
            int maxChunkZ = Math.floorDiv((int) Math.floor(screenToWorldZ(y + h - 4)), 16) + 1;
            List<HoloMapTerrainTile> tiles = HoloMapTerrainClientState.tiles(dimension,
                    minChunkX, maxChunkX, minChunkZ, maxChunkZ);
            if (tiles.isEmpty()) {
                drawGrid(context, graphics, x, y, w, h, gameTime);
                return 0;
            }
            graphics.fill(x + 4, y + 4, x + w - 4, y + h - 4, 0xD8061014);
            for (HoloMapTerrainTile tile : tiles) {
                drawTerrainTile(graphics, tile);
            }
            return tiles.size();
        }

        private void drawTerrainTile(GuiGraphicsExtractor graphics, HoloMapTerrainTile tile) {
            double baseX = tile.chunkX() * 16.0D;
            double baseZ = tile.chunkZ() * 16.0D;
            int screenX = worldToScreenX(baseX);
            int screenY = worldToScreenZ(baseZ);
            int chunkSize = Math.max(1, (int) Math.ceil(16.0D * zoom));
            if (chunkSize <= 18) {
                graphics.fill(screenX, screenY, screenX + chunkSize, screenY + chunkSize, tile.averageColor());
                return;
            }
            int pixelSize = Math.max(1, (int) Math.ceil(zoom));
            for (int localZ = 0; localZ < HoloMapTerrainTile.SIZE; localZ++) {
                for (int localX = 0; localX < HoloMapTerrainTile.SIZE; localX++) {
                    int px = worldToScreenX(baseX + localX);
                    int py = worldToScreenZ(baseZ + localZ);
                    graphics.fill(px, py, px + pixelSize, py + pixelSize, tile.pixel(localX, localZ));
                }
            }
        }

        private void drawWorldGrid(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int w, int h) {
            int worldStep = 16;
            while (worldStep * zoom < 12.0D && worldStep < 256) {
                worldStep *= 2;
            }
            if (worldStep * zoom < 10.0D) {
                return;
            }
            double left = screenToWorldX(x + 4);
            double right = screenToWorldX(x + w - 4);
            double top = screenToWorldZ(y + 4);
            double bottom = screenToWorldZ(y + h - 4);
            int startX = (int) Math.floor(left / worldStep) * worldStep;
            for (int worldX = startX; worldX <= right; worldX += worldStep) {
                int sx = worldToScreenX(worldX);
                int color = worldX == 0 ? 0x6638DFF4 : 0x2438DFF4;
                graphics.fill(sx, y + 8, sx + 1, y + h - 8, color);
            }
            int startZ = (int) Math.floor(top / worldStep) * worldStep;
            for (int worldZ = startZ; worldZ <= bottom; worldZ += worldStep) {
                int sy = worldToScreenZ(worldZ);
                int color = worldZ == 0 ? 0x6638DFF4 : 0x2438DFF4;
                graphics.fill(x + 8, sy, x + w - 8, sy + 1, color);
            }
            if (context.player() != null) {
                int px = worldToScreenX(context.player().getX());
                int pz = worldToScreenZ(context.player().getZ());
                graphics.fill(px - 3, pz - 3, px + 4, pz + 4, 0xFFFFFFFF);
                graphics.outline(px - 6, pz - 6, 12, 12, TerminalUi.accent(context));
            }
        }

        private void drawGrid(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int w, int h, long gameTime) {
            graphics.fill(x + 4, y + 4, x + w - 4, y + h - 4, 0xD8050D14);
            for (int gx = x + 18; gx < x + w - 10; gx += 24) {
                graphics.fill(gx, y + 8, gx + 1, y + h - 8, 0x2438DFF4);
            }
            for (int gy = y + 18; gy < y + h - 10; gy += 24) {
                graphics.fill(x + 8, gy, x + w - 8, gy + 1, 0x2438DFF4);
            }
            int centerX = x + w / 2;
            int centerY = y + h / 2;
            graphics.fill(centerX - 22, centerY, centerX + 23, centerY + 1, 0x7738DFF4);
            graphics.fill(centerX, centerY - 22, centerX + 1, centerY + 23, 0x7738DFF4);
            int sweep = x + 12 + (int) Math.floorMod(gameTime, Math.max(1, w - 24));
            graphics.fill(sweep, y + 8, sweep + 2, y + h - 8, 0x5538DFF4);
            TerminalUi.line(context, graphics, "ECHO HOLOGRAPHIC ATLAS", x + 12, y + 10, w - 24,
                    TerminalUi.accent(context));
        }

        private void drawMarker(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                MarkerPoint point, boolean highlighted) {
            HoloMapSnapshotPacket.MarkerData marker = point.marker();
            int color = colorForMarker(context, marker);
            int x = point.x();
            int y = point.y();
            if (marker.kind() == IMapMarker.MarkerKind.HAZARD && marker.radius() > 0) {
                int r = Math.max(8, Math.min(42, (int) (marker.radius() / 8.0F)));
                graphics.outline(x - r, y - r, r * 2, r * 2, withAlpha(color, 0x66));
                graphics.fill(x - r, y, x + r + 1, y + 1, withAlpha(color, 0x44));
                graphics.fill(x, y - r, x + 1, y + r + 1, withAlpha(color, 0x44));
            }
            if (marker.state() == IMapMarker.MarkerState.LOCKED) {
                graphics.outline(x - 4, y - 4, 8, 8, 0x889FB4BE);
                graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xAA9FB4BE);
            } else if (marker.kind() == IMapMarker.MarkerKind.ROUTE) {
                graphics.fill(x - 5, y - 2, x + 6, y + 3, TerminalUi.opaque(color));
                graphics.fill(x - 2, y - 5, x + 3, y + 6, TerminalUi.opaque(color));
            } else if (marker.kind() == IMapMarker.MarkerKind.CRASH_SITE) {
                graphics.fill(x - 1, y - 6, x + 2, y + 7, TerminalUi.opaque(color));
                graphics.fill(x - 6, y - 1, x + 7, y + 2, TerminalUi.opaque(color));
                graphics.outline(x - 4, y - 4, 8, 8, TerminalUi.opaque(color));
            } else if (marker.kind() == IMapMarker.MarkerKind.BASE_OUTPOST) {
                graphics.outline(x - 6, y - 6, 12, 12, TerminalUi.opaque(color));
                graphics.fill(x - 3, y - 3, x + 4, y + 4, TerminalUi.opaque(color));
            } else if (marker.kind() == IMapMarker.MarkerKind.ORBITAL_SCAN) {
                graphics.outline(x - 7, y - 7, 14, 14, withAlpha(color, 0xAA));
                graphics.fill(x - 1, y - 1, x + 2, y + 2, TerminalUi.opaque(color));
                graphics.fill(x - 9, y, x - 5, y + 1, TerminalUi.opaque(color));
                graphics.fill(x + 5, y, x + 10, y + 1, TerminalUi.opaque(color));
            } else if (marker.kind() == IMapMarker.MarkerKind.NEXUS_ANOMALY) {
                graphics.fill(x, y - 7, x + 1, y - 3, TerminalUi.opaque(color));
                graphics.fill(x, y + 4, x + 1, y + 8, TerminalUi.opaque(color));
                graphics.fill(x - 7, y, x - 3, y + 1, TerminalUi.opaque(color));
                graphics.fill(x + 4, y, x + 8, y + 1, TerminalUi.opaque(color));
                graphics.outline(x - 4, y - 4, 8, 8, TerminalUi.opaque(color));
            } else if (marker.kind() == IMapMarker.MarkerKind.DRONE_SCAN) {
                graphics.outline(x - 5, y - 5, 10, 10, TerminalUi.opaque(color));
                graphics.fill(x - 5, y - 5, x - 2, y - 2, TerminalUi.opaque(color));
                graphics.fill(x + 3, y + 3, x + 6, y + 6, TerminalUi.opaque(color));
            } else if (marker.kind() == IMapMarker.MarkerKind.REGION) {
                graphics.outline(x - 7, y - 7, 14, 14, withAlpha(color, 0xBB));
                graphics.outline(x - 3, y - 3, 6, 6, TerminalUi.opaque(color));
            } else {
                graphics.fill(x - 3, y - 3, x + 4, y + 4, TerminalUi.opaque(color));
                graphics.outline(x - 5, y - 5, 10, 10, TerminalUi.opaque(color));
            }
            if (highlighted) {
                graphics.outline(x - 8, y - 8, 16, 16, TerminalUi.text(context));
            }
        }

        private void drawDetailPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                HoloMapSnapshotPacket snapshot, List<HoloMapSnapshotPacket.MarkerData> markers,
                int x, int y, int w, int h) {
            TerminalUi.flatHudPanel(context, graphics, x, y, w, h, ACCENT);
            TerminalUi.line(context, graphics, "FILTERED MARKERS", x + 12, y + 10, w - 24, TerminalUi.accent(context));
            int cy = y + 30;
            cy = metric(context, graphics, x + 12, cy, w - 24, "Visible", String.valueOf(markers.size()), ACCENT);
            cy = metric(context, graphics, x + 12, cy, w - 24, "Synced", String.valueOf(snapshot.markers().size()),
                    TerminalUi.muted(context));
            cy = metric(context, graphics, x + 12, cy, w - 24, "Layers", String.valueOf(snapshot.layers().size()),
                    TerminalUi.muted(context));
            String dimension = context.player() == null ? "minecraft:overworld"
                    : context.player().level().dimension().identifier().toString();
            cy = metric(context, graphics, x + 12, cy, w - 24, "Tiles",
                    HoloMapTerrainClientState.tileCount(dimension) + " / " + HoloMapTerrainClientState.discoveredCount(),
                    TerminalUi.accent(context));
            HoloMapSnapshotPacket.MarkerData selected = selectedMarker(markers);
            TerminalUi.divider(graphics, x + 12, cy + 2, w - 24, ACCENT);
            cy += 12;
            if (selected == null) {
                TerminalUi.wrap(context, graphics, "Select a marker on the atlas to inspect source, state, and coordinates.",
                        x + 12, cy, w - 24, TerminalUi.muted(context));
                return;
            }
            TerminalUi.line(context, graphics, selected.title(), x + 12, cy, w - 24, colorForMarker(context, selected));
            cy += 16;
            cy = metric(context, graphics, x + 12, cy, w - 24, "State", selected.state().name(), colorForMarker(context, selected));
            cy = metric(context, graphics, x + 12, cy, w - 24, "Source", selected.sourceId().toString(), TerminalUi.muted(context));
            cy = metric(context, graphics, x + 12, cy, w - 24, "Layer", selected.layerId().getPath().replace("layer/", ""),
                    TerminalUi.muted(context));
            cy = metric(context, graphics, x + 12, cy, w - 24, "Dim", selected.dimension(), TerminalUi.muted(context));
            cy = metric(context, graphics, x + 12, cy, w - 24, "XYZ",
                    (int) selected.x() + " / " + (int) selected.y() + " / " + (int) selected.z(),
                    selected.precise() ? TerminalUi.text(context) : TerminalUi.warning(context));
            if (!selected.routeId().isBlank()) {
                cy = metric(context, graphics, x + 12, cy, w - 24, "Route", selected.routeId(), TerminalUi.success(context));
            }
            TerminalUi.wrap(context, graphics, selected.summary(), x + 12, cy + 8, w - 24, TerminalUi.text(context));
        }

        private int metric(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int w, String label, String value, int color) {
            TerminalUi.line(context, graphics, label, x, y, Math.max(42, w / 3), TerminalUi.muted(context));
            TerminalUi.line(context, graphics, value, x + Math.max(46, w / 3), y,
                    Math.max(42, w - Math.max(46, w / 3)), color);
            return y + 13;
        }

        private HoloMapSnapshotPacket.MarkerData selectedMarker(List<HoloMapSnapshotPacket.MarkerData> markers) {
            if (!selectedMarkerId.isBlank()) {
                for (HoloMapSnapshotPacket.MarkerData marker : markers) {
                    if (selectedMarkerId.equals(marker.id().toString())) {
                        return marker;
                    }
                }
            }
            return markers.isEmpty() ? null : markers.getFirst();
        }

        private int detailLabelLimit() {
            if (detailMode == 0) {
                return 0;
            }
            int configured = configuredDetailLabelLimit();
            return detailMode == 1 ? Math.min(14, configured) : configured;
        }

        private static int configuredDetailLabelLimit() {
            try {
                return Math.max(0, Config.DETAIL_LABEL_LIMIT.get());
            } catch (RuntimeException exception) {
                return 42;
            }
        }

        private List<HoloMapSnapshotPacket.MarkerData> visibleMarkers(List<HoloMapSnapshotPacket.MarkerData> markers) {
            return markers.stream()
                    .filter(marker -> layerEnabled.getOrDefault(marker.layerId(), true))
                    .filter(this::matchesStateFilter)
                    .filter(marker -> marker.state() != IMapMarker.MarkerState.HIDDEN)
                    .sorted(Comparator.comparing((HoloMapSnapshotPacket.MarkerData marker) -> marker.layerId().toString())
                            .thenComparing(marker -> marker.state().ordinal())
                            .thenComparing(HoloMapSnapshotPacket.MarkerData::title))
                    .toList();
        }

        private boolean matchesStateFilter(HoloMapSnapshotPacket.MarkerData marker) {
            return switch (stateFilter) {
                case ALL -> true;
                case OPEN -> marker.state() == IMapMarker.MarkerState.DISCOVERED;
                case LOCKED -> marker.state() == IMapMarker.MarkerState.LOCKED;
                case CHECKED -> marker.state() == IMapMarker.MarkerState.CHECKED;
            };
        }

        private void button(GuiGraphicsExtractor graphics, TerminalRenderContext context, String label,
                int x, int y, int w, boolean enabled, int mouseX, int mouseY, ButtonKind kind, String payload) {
            TerminalUi.compactButton(context, graphics, x, y, w, label, ACCENT, enabled,
                    TerminalUi.inside(mouseX, mouseY, x, y, w, 16));
            buttons.add(new Button(x, y, w, 16, kind, payload == null || payload.isBlank()
                    ? HoloMapIds.id("button/" + kind.name().toLowerCase(Locale.ROOT))
                    : Identifier.tryParse(payload)));
        }

        private static int colorForMarker(TerminalRenderContext context, HoloMapSnapshotPacket.MarkerData marker) {
            if (marker.state() == IMapMarker.MarkerState.LOCKED) {
                return TerminalUi.muted(context);
            }
            if (marker.state() == IMapMarker.MarkerState.CHECKED) {
                return TerminalUi.success(context);
            }
            return switch (marker.kind()) {
                case CRASH_SITE -> 0xFFFFA05B;
                case ROUTE -> TerminalUi.success(context);
                case HAZARD -> TerminalUi.danger(context);
                case MISSION -> TerminalUi.accent(context);
                case BASE_OUTPOST -> TerminalUi.warning(context);
                case ORBITAL_SCAN -> 0xFFA58BFF;
                case NEXUS_ANOMALY -> 0xFFFF8FEA;
                case DRONE_SCAN -> 0xFF7CF7D4;
                case REGION, GENERIC -> TerminalUi.text(context);
            };
        }

        private static int withAlpha(int color, int alpha) {
            return ((alpha & 0xFF) << 24) | (TerminalUi.opaque(color) & 0x00FFFFFF);
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        private static String layerLabel(String title) {
            String clean = title == null ? "LAYER" : title.toUpperCase(Locale.ROOT);
            clean = clean.replace("BASES/OUTPOSTS", "BASES").replace("ORBITAL SCANS", "ORBITAL")
                    .replace("NEXUS/ANOMALY", "NEXUS").replace("DRONES/SCANS", "DRONES");
            return clean.length() > 10 ? clean.substring(0, 10) : clean;
        }

        private static void drawLine(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
            int dx = x1 - x0;
            int dy = y1 - y0;
            int steps = Math.max(Math.abs(dx), Math.abs(dy));
            if (steps <= 0) {
                graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
                return;
            }
            for (int i = 0; i <= steps; i++) {
                int x = x0 + dx * i / steps;
                int y = y0 + dy * i / steps;
                graphics.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    private enum ButtonKind {
        CENTER,
        REFRESH,
        TEST,
        STATE,
        DETAIL,
        LAYER
    }

    private record Button(int x, int y, int w, int h, ButtonKind kind, Identifier layerId) {
    }

    private record MarkerHit(HoloMapSnapshotPacket.MarkerData marker, int x, int y) {
    }

    private record MarkerPoint(HoloMapSnapshotPacket.MarkerData marker, int x, int y) {
    }

    private record Bounds(double minX, double maxX, double minZ, double maxZ) {
        private static Bounds from(List<HoloMapSnapshotPacket.MarkerData> markers) {
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            for (HoloMapSnapshotPacket.MarkerData marker : markers) {
                minX = Math.min(minX, marker.x());
                maxX = Math.max(maxX, marker.x());
                minZ = Math.min(minZ, marker.z());
                maxZ = Math.max(maxZ, marker.z());
            }
            if (maxX - minX < 32.0D) {
                minX -= 16.0D;
                maxX += 16.0D;
            }
            if (maxZ - minZ < 32.0D) {
                minZ -= 16.0D;
                maxZ += 16.0D;
            }
            return new Bounds(minX, maxX, minZ, maxZ);
        }

        private int projectX(HoloMapSnapshotPacket.MarkerData marker, int x, int w) {
            double t = (marker.x() - minX) / Math.max(1.0D, maxX - minX);
            return x + (int) Math.round(t * w);
        }

        private int projectZ(HoloMapSnapshotPacket.MarkerData marker, int y, int h) {
            double t = (marker.z() - minZ) / Math.max(1.0D, maxZ - minZ);
            return y + (int) Math.round(t * h);
        }
    }
}
