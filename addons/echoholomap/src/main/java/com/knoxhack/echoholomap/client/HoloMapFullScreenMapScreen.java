package com.knoxhack.echoholomap.client;

import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.knoxhack.echoholomap.EchoHoloMapClient;
import com.knoxhack.echoholomap.map.HoloMapTerrainTile;
import com.knoxhack.echoholomap.network.HoloMapClientState;
import com.knoxhack.echoholomap.network.HoloMapSnapshotPacket;
import com.knoxhack.echoholomap.network.HoloMapSyncRequestPacket;
import com.knoxhack.echoholomap.network.HoloMapTerrainClientState;
import com.knoxhack.echoholomap.network.HoloMapTileRequestPacket;
import com.knoxhack.echoholomap.network.HoloMapWaypointActionPacket;
import com.knoxhack.echoholomap.network.HoloMapWaypointClientState;
import com.knoxhack.echoholomap.waypoint.HoloMapWaypoint;
import com.knoxhack.echoholomap.waypoint.HoloMapWaypoint.Scope;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HoloMapFullScreenMapScreen extends Screen {
    private static final int BG = 0xF002070A;
    private static final int PANEL = 0xDD061014;
    private static final int PANEL_ALT = 0xBB0A1720;
    private static final int ROW = 0x77112430;
    private static final int ROW_HOVER = 0xAA183642;
    private static final int ACCENT = HoloMapVisualStyle.ACCENT;

    private final List<Hitbox> hitboxes = new ArrayList<>();
    private final List<MarkerHit> markerHits = new ArrayList<>();
    private final List<WaypointHit> waypointHits = new ArrayList<>();
    private double centerX;
    private double centerZ;
    private double zoom = 1.35D;
    private boolean cameraReady;
    private boolean draggingMap;
    private boolean showWaypoints = true;
    private boolean showMarkers = true;
    private boolean actionMenuOpen;
    private int actionMenuX;
    private int actionMenuY;
    private double actionWorldX;
    private double actionWorldZ;
    private String selectedMarkerId = "";
    private String selectedWaypointId = "";
    private long lastSyncTick = -200L;
    private long lastTerrainRequestTick = -200L;
    private int lastRequestChunkX = Integer.MIN_VALUE;
    private int lastRequestChunkZ = Integer.MIN_VALUE;
    private int lastRequestRadius = -1;
    private int mapX;
    private int mapY;
    private int mapW;
    private int mapH;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public HoloMapFullScreenMapScreen() {
        super(Component.translatable("screen.echoholomap.fullscreen"));
    }

    @Override
    protected void init() {
        HoloMapLocalWaypointStore.ensureLoaded();
        requestSync(true);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        hitboxes.clear();
        markerHits.clear();
        waypointHits.clear();
        HoloMapLocalWaypointStore.ensureLoaded();
        layout();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            drawOffline(graphics);
            return;
        }

        maybeRequestSync();
        ensureCamera();
        requestTerrain(false);

        HoloMapSnapshotPacket snapshot = HoloMapClientState.snapshot();
        List<HoloMapSnapshotPacket.MarkerData> markers = visibleMarkers(snapshot);
        List<HoloMapWaypoint> waypoints = visibleWaypoints();

        graphics.fill(0, 0, width, height, BG);
        drawHeader(graphics, minecraft.font, snapshot, mouseX, mouseY);
        drawMap(graphics, minecraft.font, markers, waypoints, mouseX, mouseY);
        drawDetailPanel(graphics, minecraft.font, snapshot, markers, waypoints, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && inside(mouseX, mouseY, mapX, mapY, mapW, mapH)) {
            actionMenuOpen = true;
            actionMenuX = (int) mouseX;
            actionMenuY = (int) mouseY;
            actionWorldX = screenToWorldX(mouseX);
            actionWorldZ = screenToWorldZ(mouseY);
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            actionMenuOpen = false;
            return super.mouseClicked(event, doubleClick);
        }
        for (Hitbox hitbox : List.copyOf(hitboxes)) {
            if (!hitbox.inside(mouseX, mouseY)) {
                continue;
            }
            handleHitbox(hitbox);
            actionMenuOpen = false;
            return true;
        }
        for (WaypointHit hit : waypointHits) {
            if (inside(mouseX, mouseY, hit.x() - 7, hit.y() - 7, 14, 14)) {
                selectedWaypointId = hit.waypoint().id().toString();
                selectedMarkerId = "";
                actionMenuOpen = false;
                return true;
            }
        }
        for (MarkerHit hit : markerHits) {
            if (inside(mouseX, mouseY, hit.x() - 6, hit.y() - 6, 12, 12)) {
                selectedMarkerId = hit.marker().id().toString();
                selectedWaypointId = "";
                actionMenuOpen = false;
                return true;
            }
        }
        if (inside(mouseX, mouseY, mapX, mapY, mapW, mapH)) {
            if (doubleClick) {
                centerOnPlayer();
            } else {
                draggingMap = true;
                selectedMarkerId = "";
                selectedWaypointId = "";
            }
            actionMenuOpen = false;
            return true;
        }
        actionMenuOpen = false;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingMap) {
            centerX -= dragX / Math.max(0.25D, zoom);
            centerZ -= dragY / Math.max(0.25D, zoom);
            cameraReady = true;
            requestTerrain(false);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingMap) {
            draggingMap = false;
            requestTerrain(true);
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!inside(mouseX, mouseY, mapX, mapY, mapW, mapH)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        double worldX = screenToWorldX(mouseX);
        double worldZ = screenToWorldZ(mouseY);
        double before = zoom;
        zoom = clamp(zoom * (scrollY > 0.0D ? 1.2D : 0.82D), 0.25D, 8.0D);
        if (before != zoom) {
            centerX += worldX - screenToWorldX(mouseX);
            centerZ += worldZ - screenToWorldZ(mouseY);
            requestTerrain(true);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || EchoHoloMapClient.OPEN_MAP_KEY.matches(event)) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (key == GLFW.GLFW_KEY_R) {
            requestSync(true);
            return true;
        }
        if (key == GLFW.GLFW_KEY_W) {
            showWaypoints = !showWaypoints;
            return true;
        }
        if (key == GLFW.GLFW_KEY_V) {
            showMarkers = !showMarkers;
            return true;
        }
        if (key == GLFW.GLFW_KEY_C || key == GLFW.GLFW_KEY_HOME) {
            centerOnPlayer();
            return true;
        }
        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
            deleteSelectedWaypoint();
            return true;
        }
        double pan = Math.max(16.0D, 96.0D / Math.max(0.25D, zoom));
        if (key == GLFW.GLFW_KEY_LEFT) {
            centerX -= pan;
        } else if (key == GLFW.GLFW_KEY_RIGHT) {
            centerX += pan;
        } else if (key == GLFW.GLFW_KEY_UP) {
            centerZ -= pan;
        } else if (key == GLFW.GLFW_KEY_DOWN) {
            centerZ += pan;
        } else if (key == GLFW.GLFW_KEY_EQUAL || key == GLFW.GLFW_KEY_KP_ADD) {
            zoom = clamp(zoom * 1.2D, 0.25D, 8.0D);
        } else if (key == GLFW.GLFW_KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
            zoom = clamp(zoom * 0.82D, 0.25D, 8.0D);
        } else {
            return super.keyPressed(event);
        }
        cameraReady = true;
        requestTerrain(true);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void layout() {
        int margin = width < 360 ? 4 : 8;
        int top = 28;
        panelW = width >= 460 ? Math.min(220, Math.max(176, width / 4)) : 0;
        panelX = panelW == 0 ? width : width - panelW - margin;
        panelY = top;
        panelH = Math.max(1, height - top - margin);
        mapX = margin;
        mapY = top;
        mapW = Math.max(1, width - margin * 2 - (panelW == 0 ? 0 : panelW + margin));
        mapH = Math.max(1, height - top - margin);
    }

    private void drawOffline(GuiGraphicsExtractor graphics) {
        graphics.fill(0, 0, width, height, BG);
        graphics.text(Minecraft.getInstance().font, "ECHO HOLOMAP OFFLINE", 12, 12,
                HoloMapVisualStyle.warning(null), false);
    }

    private void drawHeader(GuiGraphicsExtractor graphics, Font font, HoloMapSnapshotPacket snapshot,
            int mouseX, int mouseY) {
        graphics.fill(0, 0, width, 24, 0xF0061014);
        graphics.fill(0, 0, Math.max(56, width / 5), 2, ACCENT);
        graphics.text(font, "ECHO HOLOMAP", 10, 8, HoloMapVisualStyle.TEXT, false);
        String status = snapshot.statusLine() + " | " + String.format(Locale.ROOT, "%.2fx", zoom);
        graphics.text(font, trim(font, status, Math.max(20, width - 360)), 100, 8,
                HoloMapVisualStyle.MUTED, false);
        button(graphics, font, width - 246, 4, 48, "CENTER", HitKind.CENTER, null, mouseX, mouseY);
        button(graphics, font, width - 194, 4, 42, "SYNC", HitKind.SYNC, null, mouseX, mouseY);
        button(graphics, font, width - 148, 4, 62, showWaypoints ? "WP ON" : "WP OFF",
                HitKind.TOGGLE_WAYPOINTS, null, mouseX, mouseY);
        button(graphics, font, width - 82, 4, 50, showMarkers ? "M ON" : "M OFF",
                HitKind.TOGGLE_MARKERS, null, mouseX, mouseY);
        button(graphics, font, width - 28, 4, 20, "X", HitKind.CLOSE, null, mouseX, mouseY);
    }

    private void drawMap(GuiGraphicsExtractor graphics, Font font,
            List<HoloMapSnapshotPacket.MarkerData> markers, List<HoloMapWaypoint> waypoints,
            int mouseX, int mouseY) {
        graphics.fill(mapX, mapY, mapX + mapW, mapY + mapH, PANEL);
        graphics.outline(mapX, mapY, mapW, mapH, 0x8846DFF4);
        graphics.fill(mapX, mapY, mapX + Math.max(60, mapW / 5), mapY + 3, ACCENT);
        graphics.enableScissor(mapX + 3, mapY + 3, mapX + mapW - 3, mapY + mapH - 3);
        int terrainTiles = drawTerrain(graphics);
        drawWorldGrid(graphics);
        if (showMarkers) {
            drawRoutes(graphics, markers);
            drawMarkers(graphics, font, markers, mouseX, mouseY);
        }
        if (showWaypoints) {
            drawWaypoints(graphics, font, waypoints, mouseX, mouseY);
        }
        drawPlayer(graphics);
        String dim = Minecraft.getInstance().player.level().dimension().identifier().toString();
        HoloMapTerrainClientState.DetailStats stats = HoloMapTerrainClientState.detailStats(dim);
        graphics.fill(mapX + 10, mapY + 8, mapX + mapW - 10, mapY + 22, 0x99061014);
        graphics.text(font, "ATLAS " + terrainTiles + " cached / " + HoloMapTerrainClientState.discoveredCount()
                        + " discovered | " + stats.label() + " | XYZ "
                        + (int) centerX + " / " + (int) centerZ,
                mapX + 14, mapY + 12, HoloMapVisualStyle.ACCENT, false);
        drawLegend(graphics, font, markers.size(), waypoints.size());
        if (actionMenuOpen) {
            drawActionMenu(graphics, font, mouseX, mouseY);
        }
        graphics.disableScissor();
    }

    private int drawTerrain(GuiGraphicsExtractor graphics) {
        String dimension = Minecraft.getInstance().player.level().dimension().identifier().toString();
        int minChunkX = Math.floorDiv((int) Math.floor(screenToWorldX(mapX + 4)), 16) - 1;
        int maxChunkX = Math.floorDiv((int) Math.floor(screenToWorldX(mapX + mapW - 4)), 16) + 1;
        int minChunkZ = Math.floorDiv((int) Math.floor(screenToWorldZ(mapY + 4)), 16) - 1;
        int maxChunkZ = Math.floorDiv((int) Math.floor(screenToWorldZ(mapY + mapH - 4)), 16) + 1;
        List<HoloMapTerrainTile> tiles = HoloMapTerrainClientState.tiles(dimension,
                minChunkX, maxChunkX, minChunkZ, maxChunkZ);
        graphics.fill(mapX + 3, mapY + 3, mapX + mapW - 3, mapY + mapH - 3, 0xCC061014);
        for (HoloMapTerrainTile tile : tiles) {
            drawTile(graphics, tile);
        }
        return tiles.size();
    }

    private void drawTile(GuiGraphicsExtractor graphics, HoloMapTerrainTile tile) {
        double baseX = tile.chunkX() * 16.0D;
        double baseZ = tile.chunkZ() * 16.0D;
        int screenX = worldToScreenX(baseX);
        int screenY = worldToScreenZ(baseZ);
        int chunkSize = Math.max(1, (int) Math.ceil(16.0D * zoom));
        if (chunkSize <= 18) {
            graphics.fill(screenX, screenY, screenX + chunkSize, screenY + chunkSize,
                    HoloMapVisualStyle.terrainColor(tile.averageColor()));
            return;
        }
        int pixelSize = Math.max(1, (int) Math.ceil(zoom));
        for (int localZ = 0; localZ < HoloMapTerrainTile.SIZE; localZ++) {
            for (int localX = 0; localX < HoloMapTerrainTile.SIZE; localX++) {
                int px = worldToScreenX(baseX + localX);
                int py = worldToScreenZ(baseZ + localZ);
                graphics.fill(px, py, px + pixelSize, py + pixelSize,
                        HoloMapVisualStyle.terrainColor(tile.pixel(localX, localZ)));
            }
        }
    }

    private void drawWorldGrid(GuiGraphicsExtractor graphics) {
        double left = screenToWorldX(mapX + 4);
        double right = screenToWorldX(mapX + mapW - 4);
        double top = screenToWorldZ(mapY + 4);
        double bottom = screenToWorldZ(mapY + mapH - 4);
        int step = zoom < 0.7D ? 128 : zoom < 1.6D ? 64 : 16;
        int minX = (int) Math.floor(left / step) * step;
        int maxX = (int) Math.ceil(right / step) * step;
        int minZ = (int) Math.floor(top / step) * step;
        int maxZ = (int) Math.ceil(bottom / step) * step;
        for (int worldX = minX; worldX <= maxX; worldX += step) {
            int sx = worldToScreenX(worldX);
            graphics.fill(sx, mapY + 4, sx + 1, mapY + mapH - 4, 0x2438DFF4);
        }
        for (int worldZ = minZ; worldZ <= maxZ; worldZ += step) {
            int sy = worldToScreenZ(worldZ);
            graphics.fill(mapX + 4, sy, mapX + mapW - 4, sy + 1, 0x2438DFF4);
        }
    }

    private void drawRoutes(GuiGraphicsExtractor graphics, List<HoloMapSnapshotPacket.MarkerData> markers) {
        Map<String, List<MarkerPoint>> routes = new LinkedHashMap<>();
        for (HoloMapSnapshotPacket.MarkerData marker : markers) {
            if (marker.routeId().isBlank()) {
                continue;
            }
            routes.computeIfAbsent(marker.routeId(), ignored -> new ArrayList<>())
                    .add(new MarkerPoint(marker, worldToScreenX(marker.x()), worldToScreenZ(marker.z())));
        }
        for (List<MarkerPoint> route : routes.values()) {
            route.sort(Comparator.comparingInt(point -> point.marker().routeOrder()));
            for (int i = 1; i < route.size(); i++) {
                HoloMapGlyphRenderer.drawLine(graphics, route.get(i - 1).x(), route.get(i - 1).y(),
                        route.get(i).x(), route.get(i).y(), 0xAA92F7A6);
            }
        }
    }

    private void drawMarkers(GuiGraphicsExtractor graphics, Font font,
            List<HoloMapSnapshotPacket.MarkerData> markers, int mouseX, int mouseY) {
        int labels = labelLimit();
        int drawn = 0;
        for (HoloMapSnapshotPacket.MarkerData marker : markers) {
            int px = worldToScreenX(marker.x());
            int py = worldToScreenZ(marker.z());
            if (px < mapX - 48 || px > mapX + mapW + 48 || py < mapY - 48 || py > mapY + mapH + 48) {
                continue;
            }
            boolean selected = marker.id().toString().equals(selectedMarkerId);
            boolean hovered = inside(mouseX, mouseY, px - 6, py - 6, 12, 12);
            int color = HoloMapVisualStyle.markerColor(Minecraft.getInstance().player, marker);
            HoloMapGlyphRenderer.drawMarker(graphics, marker, px, py, color, 5, selected || hovered);
            markerHits.add(new MarkerHit(marker, px, py));
            if (selected || hovered || marker.precise() && drawn++ < labels) {
                graphics.text(font, trim(font, marker.title(), 132), px + 9, py - 4,
                        selected ? HoloMapVisualStyle.TEXT : color, false);
            }
        }
    }

    private void drawWaypoints(GuiGraphicsExtractor graphics, Font font,
            List<HoloMapWaypoint> waypoints, int mouseX, int mouseY) {
        int labels = Math.max(2, labelLimit() / 2);
        int drawn = 0;
        for (HoloMapWaypoint waypoint : waypoints) {
            int px = worldToScreenX(waypoint.x());
            int py = worldToScreenZ(waypoint.z());
            if (px < mapX - 32 || px > mapX + mapW + 32 || py < mapY - 32 || py > mapY + mapH + 32) {
                continue;
            }
            boolean selected = waypoint.id().toString().equals(selectedWaypointId);
            boolean hovered = inside(mouseX, mouseY, px - 7, py - 7, 14, 14);
            HoloMapGlyphRenderer.drawWaypoint(graphics, waypoint, px, py, waypoint.color(), 5, selected || hovered);
            waypointHits.add(new WaypointHit(waypoint, px, py));
            if (selected || hovered || drawn++ < labels) {
                graphics.text(font, trim(font, waypoint.title(), 132), px + 9, py - 4,
                        selected ? HoloMapVisualStyle.TEXT : waypoint.color(), false);
            }
        }
    }

    private void drawPlayer(GuiGraphicsExtractor graphics) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        var player = Minecraft.getInstance().player;
        int px = worldToScreenX(player.getX());
        int py = worldToScreenZ(player.getZ());
        graphics.fill(px - 2, py - 2, px + 3, py + 3, 0xFFFFFFFF);
        double yaw = Math.toRadians(player.getYRot());
        int tipX = px - (int) Math.round(Math.sin(yaw) * 14.0D);
        int tipY = py + (int) Math.round(Math.cos(yaw) * 14.0D);
        HoloMapGlyphRenderer.drawLine(graphics, px, py, tipX, tipY, 0xFFFFFFFF);
    }

    private void drawLegend(GuiGraphicsExtractor graphics, Font font, int markerCount, int waypointCount) {
        int y = mapY + mapH - 20;
        graphics.fill(mapX + 10, y, mapX + mapW - 10, y + 14, 0x99061014);
        String text = "Markers " + markerCount + " | Waypoints " + waypointCount
                + " | V markers | RMB waypoint | +/- zoom | arrows pan | R sync";
        graphics.text(font, trim(font, text, mapW - 28), mapX + 14, y + 4,
                HoloMapVisualStyle.MUTED, false);
    }

    private void drawActionMenu(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        int w = selectedWaypointId.isBlank() ? 120 : 136;
        int h = selectedWaypointId.isBlank() ? 74 : 94;
        int x = Math.max(mapX + 8, Math.min(actionMenuX, mapX + mapW - w - 8));
        int y = Math.max(mapY + 8, Math.min(actionMenuY, mapY + mapH - h - 8));
        graphics.fill(x, y, x + w, y + h, 0xEE061014);
        graphics.outline(x, y, w, h, ACCENT);
        graphics.text(font, "MAP ACTION", x + 8, y + 7, HoloMapVisualStyle.ACCENT, false);
        button(graphics, font, x + 8, y + 22, 48, "LOCAL", HitKind.MENU_LOCAL, null, mouseX, mouseY);
        button(graphics, font, x + 62, y + 22, 50, "PERS", HitKind.MENU_PERSONAL, null, mouseX, mouseY);
        button(graphics, font, x + 8, y + 42, 48, "SHARE", HitKind.MENU_SHARED, null, mouseX, mouseY);
        button(graphics, font, x + 62, y + 42, 50, "COPY", HitKind.MENU_COPY, null, mouseX, mouseY);
        if (!selectedWaypointId.isBlank()) {
            button(graphics, font, x + 8, y + 62, 58, "MOVE", HitKind.MENU_MOVE, null, mouseX, mouseY);
            button(graphics, font, x + 72, y + 62, 56, "DELETE", HitKind.DELETE_WAYPOINT, null, mouseX, mouseY);
        }
    }

    private void drawDetailPanel(GuiGraphicsExtractor graphics, Font font, HoloMapSnapshotPacket snapshot,
            List<HoloMapSnapshotPacket.MarkerData> markers, List<HoloMapWaypoint> waypoints,
            int mouseX, int mouseY) {
        if (panelW <= 0) {
            return;
        }
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_ALT);
        graphics.outline(panelX, panelY, panelW, panelH, 0x5538DFF4);
        graphics.text(font, "MAP INDEX", panelX + 10, panelY + 10, ACCENT, false);
        int y = panelY + 30;
        y = metric(graphics, font, y, "Visible", String.valueOf(markers.size()), ACCENT);
        y = metric(graphics, font, y, "Synced", String.valueOf(snapshot.markers().size()), HoloMapVisualStyle.MUTED);
        y = metric(graphics, font, y, "Waypoints", String.valueOf(waypoints.size()), HoloMapVisualStyle.WARNING);
        y = metric(graphics, font, y, "Tiles", String.valueOf(HoloMapTerrainClientState.discoveredCount()),
                HoloMapVisualStyle.ACCENT);
        y += 8;
        graphics.fill(panelX + 10, y, panelX + panelW - 10, y + 1, 0x6638DFF4);
        y += 10;

        List<MapEntry> entries = listEntries(markers, waypoints);
        int rows = Math.max(3, Math.min(10, (panelH - (y - panelY) - 120) / 17));
        for (int i = 0; i < Math.min(rows, entries.size()); i++) {
            MapEntry entry = entries.get(i);
            boolean selected = entry.id().equals(selectedMarkerId) || entry.id().equals(selectedWaypointId);
            boolean hovered = inside(mouseX, mouseY, panelX + 10, y - 2, panelW - 20, 16);
            graphics.fill(panelX + 10, y - 2, panelX + panelW - 10, y + 14,
                    selected ? 0x5538DFF4 : hovered ? ROW_HOVER : ROW);
            graphics.text(font, trim(font, entry.prefix() + " " + entry.title(), panelW - 28),
                    panelX + 14, y + 2, selected ? HoloMapVisualStyle.TEXT : entry.color(), false);
            hitboxes.add(new Hitbox(HitKind.SELECT_ENTRY, entry.id(), panelX + 10, y - 2, panelW - 20, 16));
            y += 17;
        }
        y += 8;
        graphics.fill(panelX + 10, y, panelX + panelW - 10, y + 1, 0x6638DFF4);
        y += 10;
        drawSelectionDetails(graphics, font, markers, waypoints, y, mouseX, mouseY);
    }

    private void drawSelectionDetails(GuiGraphicsExtractor graphics, Font font,
            List<HoloMapSnapshotPacket.MarkerData> markers, List<HoloMapWaypoint> waypoints,
            int y, int mouseX, int mouseY) {
        HoloMapWaypoint waypoint = selectedWaypoint(waypoints);
        if (waypoint != null) {
            graphics.text(font, trim(font, waypoint.title(), panelW - 22), panelX + 10, y,
                    waypoint.color(), false);
            y += 16;
            y = metric(graphics, font, y, "Scope", waypoint.scope().name(), waypoint.color());
            y = metric(graphics, font, y, "Dim", waypoint.dimension(), HoloMapVisualStyle.MUTED);
            y = metric(graphics, font, y, "XYZ",
                    (int) waypoint.x() + " / " + (int) waypoint.y() + " / " + (int) waypoint.z(),
                    HoloMapVisualStyle.TEXT);
            button(graphics, font, panelX + 10, y + 8, 58, "DELETE", HitKind.DELETE_WAYPOINT,
                    null, mouseX, mouseY);
            return;
        }
        HoloMapSnapshotPacket.MarkerData marker = selectedMarker(markers);
        if (marker == null) {
            graphics.text(font, "Select a marker or waypoint.", panelX + 10, y,
                    HoloMapVisualStyle.MUTED, false);
            return;
        }
        int color = HoloMapVisualStyle.markerColor(Minecraft.getInstance().player, marker);
        graphics.text(font, trim(font, marker.title(), panelW - 22), panelX + 10, y, color, false);
        y += 16;
        y = metric(graphics, font, y, "State", marker.state().name(), color);
        y = metric(graphics, font, y, "Layer", marker.layerId().getPath().replace("layer/", ""),
                HoloMapVisualStyle.MUTED);
        y = metric(graphics, font, y, "XYZ",
                (int) marker.x() + " / " + (int) marker.y() + " / " + (int) marker.z(),
                marker.precise() ? HoloMapVisualStyle.TEXT : HoloMapVisualStyle.WARNING);
        graphics.text(font, trim(font, marker.summary(), panelW - 22), panelX + 10, y + 8,
                HoloMapVisualStyle.MUTED, false);
    }

    private int metric(GuiGraphicsExtractor graphics, Font font, int y, String label, String value, int color) {
        graphics.text(font, label, panelX + 10, y, HoloMapVisualStyle.MUTED, false);
        graphics.text(font, trim(font, value, panelW - 76), panelX + 70, y, color, false);
        return y + 13;
    }

    private void handleHitbox(Hitbox hitbox) {
        switch (hitbox.kind()) {
            case CENTER -> centerOnPlayer();
            case SYNC -> requestSync(true);
            case TOGGLE_WAYPOINTS -> showWaypoints = !showWaypoints;
            case TOGGLE_MARKERS -> showMarkers = !showMarkers;
            case CLOSE -> Minecraft.getInstance().setScreen(null);
            case MENU_LOCAL -> createWaypoint(Scope.LOCAL);
            case MENU_PERSONAL -> createWaypoint(Scope.PERSONAL);
            case MENU_SHARED -> createWaypoint(Scope.SHARED);
            case MENU_COPY -> copyActionCoordinates();
            case MENU_MOVE -> moveSelectedWaypointToAction();
            case DELETE_WAYPOINT -> deleteSelectedWaypoint();
            case SELECT_ENTRY -> focusEntry(hitbox.id());
        }
    }

    private void createWaypoint(Scope scope) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        var player = Minecraft.getInstance().player;
        long time = player.level().getGameTime();
        String dimension = player.level().dimension().identifier().toString();
        String title = switch (scope) {
            case LOCAL -> "Local " + (int) actionWorldX + ", " + (int) actionWorldZ;
            case PERSONAL -> "Personal " + (int) actionWorldX + ", " + (int) actionWorldZ;
            case SHARED -> "Shared " + (int) actionWorldX + ", " + (int) actionWorldZ;
        };
        int color = scope == Scope.SHARED ? HoloMapVisualStyle.WARNING
                : scope == Scope.PERSONAL ? HoloMapVisualStyle.SUCCESS : HoloMapVisualStyle.ACCENT;
        HoloMapWaypoint waypoint = HoloMapWaypoint.create(scope, player.getUUID(), dimension,
                actionWorldX, Math.floor(player.getY()), actionWorldZ, title, color, time);
        if (scope == Scope.LOCAL) {
            HoloMapLocalWaypointStore.upsert(waypoint);
            selectedWaypointId = waypoint.id().toString();
            selectedMarkerId = "";
        } else {
            EchoNetClientActions.sendServerboundAction(HoloMapWaypointActionPacket.upsert(waypoint));
            requestSync(true);
        }
    }

    private void moveSelectedWaypointToAction() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        Identifier id = Identifier.tryParse(selectedWaypointId);
        if (id == null) {
            return;
        }
        HoloMapWaypoint selected = HoloMapWaypointClientState.waypoints().stream()
                .filter(waypoint -> waypoint.id().equals(id))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            return;
        }
        var player = Minecraft.getInstance().player;
        long time = player.level().getGameTime();
        HoloMapWaypoint moved = new HoloMapWaypoint(
                selected.id(), selected.owner(), selected.scope(),
                player.level().dimension().identifier().toString(),
                actionWorldX, Math.floor(player.getY()), actionWorldZ,
                selected.title(), selected.color(), selected.icon(), selected.visible(),
                selected.createdTime(), time);
        if (moved.scope() == Scope.LOCAL) {
            HoloMapLocalWaypointStore.upsert(moved);
        } else {
            EchoNetClientActions.sendServerboundAction(HoloMapWaypointActionPacket.upsert(moved));
            requestSync(true);
        }
        selectedWaypointId = moved.id().toString();
        selectedMarkerId = "";
    }

    private void deleteSelectedWaypoint() {
        Identifier id = Identifier.tryParse(selectedWaypointId);
        if (id == null) {
            return;
        }
        HoloMapWaypoint selected = HoloMapWaypointClientState.waypoints().stream()
                .filter(waypoint -> waypoint.id().equals(id))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            return;
        }
        if (selected.scope() == Scope.LOCAL) {
            HoloMapLocalWaypointStore.remove(id);
        } else {
            EchoNetClientActions.sendServerboundAction(HoloMapWaypointActionPacket.delete(id));
            requestSync(true);
        }
        selectedWaypointId = "";
    }

    private void copyActionCoordinates() {
        int y = Minecraft.getInstance().player == null ? 64 : (int) Minecraft.getInstance().player.getY();
        Minecraft.getInstance().keyboardHandler.setClipboard((int) actionWorldX + " " + y + " " + (int) actionWorldZ);
    }

    private void focusEntry(Identifier id) {
        if (id == null) {
            return;
        }
        for (HoloMapWaypoint waypoint : HoloMapWaypointClientState.waypoints()) {
            if (waypoint.id().equals(id)) {
                selectedWaypointId = id.toString();
                selectedMarkerId = "";
                centerX = waypoint.x();
                centerZ = waypoint.z();
                requestTerrain(true);
                return;
            }
        }
        for (HoloMapSnapshotPacket.MarkerData marker : HoloMapClientState.snapshot().markers()) {
            if (marker.id().equals(id)) {
                selectedMarkerId = id.toString();
                selectedWaypointId = "";
                centerX = marker.x();
                centerZ = marker.z();
                requestTerrain(true);
                return;
            }
        }
    }

    private void maybeRequestSync() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        long now = Minecraft.getInstance().player.level().getGameTime();
        if (HoloMapClientState.snapshot().gameTime() == 0L || now - lastSyncTick > 120L) {
            requestSync(false);
        }
    }

    private void requestSync(boolean force) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        long now = Minecraft.getInstance().player.level().getGameTime();
        if (!force && now - lastSyncTick < 40L) {
            return;
        }
        lastSyncTick = now;
        EchoNetClientActions.sendServerboundAction(new HoloMapSyncRequestPacket());
    }

    private void requestTerrain(boolean force) {
        if (Minecraft.getInstance().player == null || mapW <= 0 || mapH <= 0) {
            return;
        }
        long now = Minecraft.getInstance().player.level().getGameTime();
        int centerChunkX = Math.floorDiv((int) Math.floor(centerX), 16);
        int centerChunkZ = Math.floorDiv((int) Math.floor(centerZ), 16);
        int radius = visibleChunkRadius();
        if (!force && now - lastTerrainRequestTick < 20L
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
                Minecraft.getInstance().player.level().dimension().identifier().toString(),
                centerChunkX, centerChunkZ, radius));
    }

    private void ensureCamera() {
        if (cameraReady || Minecraft.getInstance().player == null) {
            return;
        }
        centerOnPlayer();
    }

    private void centerOnPlayer() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        centerX = Minecraft.getInstance().player.getX();
        centerZ = Minecraft.getInstance().player.getZ();
        cameraReady = true;
        requestTerrain(true);
    }

    private int visibleChunkRadius() {
        double blocksAcross = Math.max(mapW, mapH) / Math.max(0.25D, zoom);
        return Math.max(1, Math.min(32, (int) Math.ceil(blocksAcross / 32.0D) + 1));
    }

    private int worldToScreenX(double worldX) {
        return mapX + mapW / 2 + (int) Math.round((worldX - centerX) * zoom);
    }

    private int worldToScreenZ(double worldZ) {
        return mapY + mapH / 2 + (int) Math.round((worldZ - centerZ) * zoom);
    }

    private double screenToWorldX(double screenX) {
        return centerX + (screenX - (mapX + mapW / 2.0D)) / Math.max(0.25D, zoom);
    }

    private double screenToWorldZ(double screenY) {
        return centerZ + (screenY - (mapY + mapH / 2.0D)) / Math.max(0.25D, zoom);
    }

    private List<HoloMapSnapshotPacket.MarkerData> visibleMarkers(HoloMapSnapshotPacket snapshot) {
        if (!showMarkers || Minecraft.getInstance().player == null) {
            return List.of();
        }
        String dimension = Minecraft.getInstance().player.level().dimension().identifier().toString();
        return snapshot.markers().stream()
                .filter(marker -> dimension.equals(marker.dimension()))
                .filter(marker -> marker.state() != IMapMarker.MarkerState.HIDDEN)
                .sorted(Comparator.comparing((HoloMapSnapshotPacket.MarkerData marker) -> marker.layerId().toString())
                        .thenComparing(marker -> marker.state().ordinal())
                        .thenComparing(HoloMapSnapshotPacket.MarkerData::title))
                .toList();
    }

    private List<HoloMapWaypoint> visibleWaypoints() {
        if (!showWaypoints || Minecraft.getInstance().player == null) {
            return List.of();
        }
        String dimension = Minecraft.getInstance().player.level().dimension().identifier().toString();
        return HoloMapWaypointClientState.waypoints().stream()
                .filter(HoloMapWaypoint::visible)
                .filter(waypoint -> waypoint.inDimension(dimension))
                .sorted(Comparator.comparing(HoloMapWaypoint::scope)
                        .thenComparing(waypoint -> !waypoint.isDeathpoint())
                        .thenComparing(HoloMapWaypoint::title, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<MapEntry> listEntries(List<HoloMapSnapshotPacket.MarkerData> markers, List<HoloMapWaypoint> waypoints) {
        List<MapEntry> entries = new ArrayList<>();
        for (HoloMapWaypoint waypoint : waypoints) {
            entries.add(MapEntry.waypoint(waypoint, distanceToCenter(waypoint.x(), waypoint.z())));
        }
        for (HoloMapSnapshotPacket.MarkerData marker : markers) {
            entries.add(MapEntry.marker(marker, distanceToCenter(marker.x(), marker.z())));
        }
        entries.sort(Comparator.comparingDouble(MapEntry::distance)
                .thenComparing(MapEntry::title, String.CASE_INSENSITIVE_ORDER));
        return entries;
    }

    private double distanceToCenter(double x, double z) {
        if (Minecraft.getInstance().player != null) {
            double dx = x - Minecraft.getInstance().player.getX();
            double dz = z - Minecraft.getInstance().player.getZ();
            return Math.sqrt(dx * dx + dz * dz);
        }
        double dx = x - centerX;
        double dz = z - centerZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private HoloMapWaypoint selectedWaypoint(List<HoloMapWaypoint> waypoints) {
        if (selectedWaypointId.isBlank()) {
            return null;
        }
        for (HoloMapWaypoint waypoint : waypoints) {
            if (selectedWaypointId.equals(waypoint.id().toString())) {
                return waypoint;
            }
        }
        return null;
    }

    private HoloMapSnapshotPacket.MarkerData selectedMarker(List<HoloMapSnapshotPacket.MarkerData> markers) {
        if (selectedMarkerId.isBlank()) {
            return null;
        }
        for (HoloMapSnapshotPacket.MarkerData marker : markers) {
            if (selectedMarkerId.equals(marker.id().toString())) {
                return marker;
            }
        }
        return null;
    }

    private int labelLimit() {
        return switch (HoloMapVisualStyle.visualDensity()) {
            case LOW -> 2;
            case MEDIUM -> 8;
            case HIGH -> 18;
        };
    }

    private void button(GuiGraphicsExtractor graphics, Font font, int x, int y, int w, String label,
            HitKind kind, Identifier id, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, 16);
        graphics.fill(x, y, x + w, y + 16, hovered ? 0xFF183743 : 0xDD112430);
        graphics.outline(x, y, w, 16, hovered ? HoloMapVisualStyle.TEXT : ACCENT);
        graphics.centeredText(font, trim(font, label, w - 6), x + w / 2, y + 5,
                hovered ? HoloMapVisualStyle.TEXT : HoloMapVisualStyle.MUTED);
        hitboxes.add(new Hitbox(kind, id, x, y, w, 16));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String trim(Font font, String text, int maxWidth) {
        if (text == null || maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String value = text;
        String ellipsis = "...";
        int allowed = Math.max(1, maxWidth - font.width(ellipsis));
        while (!value.isEmpty() && font.width(value) > allowed) {
            value = value.substring(0, value.length() - 1);
        }
        return value + ellipsis;
    }

    private enum HitKind {
        CENTER,
        SYNC,
        TOGGLE_WAYPOINTS,
        TOGGLE_MARKERS,
        CLOSE,
        MENU_LOCAL,
        MENU_PERSONAL,
        MENU_SHARED,
        MENU_COPY,
        MENU_MOVE,
        DELETE_WAYPOINT,
        SELECT_ENTRY
    }

    private record Hitbox(HitKind kind, Identifier id, int x, int y, int w, int h) {
        boolean inside(double mouseX, double mouseY) {
            return HoloMapFullScreenMapScreen.inside(mouseX, mouseY, x, y, w, h);
        }
    }

    private record MarkerHit(HoloMapSnapshotPacket.MarkerData marker, int x, int y) {
    }

    private record WaypointHit(HoloMapWaypoint waypoint, int x, int y) {
    }

    private record MarkerPoint(HoloMapSnapshotPacket.MarkerData marker, int x, int y) {
    }

    private record MapEntry(
            Identifier id,
            String title,
            double distance,
            int color,
            boolean waypoint) {
        static MapEntry marker(HoloMapSnapshotPacket.MarkerData marker, double distance) {
            return new MapEntry(marker.id(), marker.title(), distance,
                    HoloMapVisualStyle.markerColor(Minecraft.getInstance().player, marker), false);
        }

        static MapEntry waypoint(HoloMapWaypoint waypoint, double distance) {
            return new MapEntry(waypoint.id(), waypoint.title(), distance, waypoint.color(), true);
        }

        String prefix() {
            return waypoint ? "W" : "M";
        }
    }
}
