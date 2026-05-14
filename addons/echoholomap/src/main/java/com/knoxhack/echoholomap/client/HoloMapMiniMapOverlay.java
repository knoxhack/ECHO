package com.knoxhack.echoholomap.client;

import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.map.HoloMapTerrainTile;
import com.knoxhack.echoholomap.map.HoloMapVisualPriority;
import com.knoxhack.echoholomap.network.HoloMapClientState;
import com.knoxhack.echoholomap.network.HoloMapSnapshotPacket;
import com.knoxhack.echoholomap.network.HoloMapTerrainClientState;
import com.knoxhack.echoholomap.network.HoloMapTileRequestPacket;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class HoloMapMiniMapOverlay {
    private static boolean toggledVisible = true;
    private static long lastRequestTick = -200L;
    private static Config.MiniMapCorner cornerOverride;
    private static double zoomOffset;

    private HoloMapMiniMapOverlay() {
    }

    public static void toggle() {
        toggledVisible = !toggledVisible;
    }

    public static void zoomIn() {
        zoomOffset = Math.min(1.5D, zoomOffset + 0.25D);
    }

    public static void zoomOut() {
        zoomOffset = Math.max(-0.75D, zoomOffset - 0.25D);
    }

    public static void cycleCorner() {
        Config.MiniMapCorner current = corner();
        Config.MiniMapCorner[] values = Config.MiniMapCorner.values();
        cornerOverride = values[(current.ordinal() + 1) % values.length];
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || !enabled()) {
            return;
        }
        int size = minimapSize();
        int margin = 12;
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int x = switch (corner()) {
            case TOP_LEFT, BOTTOM_LEFT -> margin;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenW - size - margin;
        };
        int y = switch (corner()) {
            case TOP_LEFT, TOP_RIGHT -> margin;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenH - size - margin;
        };
        requestNearbyTiles(player);
        int accent = HoloMapVisualStyle.accent(player);
        int panel = HoloMapVisualStyle.withAlpha(HoloMapVisualStyle.panel(player),
                Math.round(HoloMapVisualStyle.hologramOpacity(player) * 255.0F));
        graphics.fill(x - 4, y - 4, x + size + 4, y + size + 18, panel);
        graphics.outline(x - 4, y - 4, size + 8, size + 22, HoloMapVisualStyle.withAlpha(accent, 0xAA));
        graphics.fill(x - 3, y - 3, x + size + 3, y - 1, accent);
        graphics.enableScissor(x, y, x + size, y + size);
        drawTerrain(graphics, player, x, y, size, size, minimapZoom());
        drawMarkers(graphics, player, x, y, size, size, minimapZoom());
        drawPlayer(graphics, player, x, y, size);
        graphics.disableScissor();
        drawReadout(graphics, minecraft.font, player, x, y, size);
        renderCoreFrame(graphics, x - 4, y - 4, size + 8, size + 22);
    }

    private static void requestNearbyTiles(Player player) {
        long now = player.level().getGameTime();
        if (now - lastRequestTick < 40L) {
            return;
        }
        lastRequestTick = now;
        EchoNetClientActions.sendServerboundAction(HoloMapTileRequestPacket.forPlayer(
                player, player.getX(), player.getZ(), Math.max(2, (int) Math.ceil(minimapSize() / 32.0D))));
    }

    private static void drawTerrain(GuiGraphicsExtractor graphics, Player player,
            int x, int y, int w, int h, double zoom) {
        String dimension = player.level().dimension().identifier().toString();
        double centerX = player.getX();
        double centerZ = player.getZ();
        int minChunkX = Math.floorDiv((int) Math.floor(centerX - w / (2.0D * zoom)), 16) - 1;
        int maxChunkX = Math.floorDiv((int) Math.floor(centerX + w / (2.0D * zoom)), 16) + 1;
        int minChunkZ = Math.floorDiv((int) Math.floor(centerZ - h / (2.0D * zoom)), 16) - 1;
        int maxChunkZ = Math.floorDiv((int) Math.floor(centerZ + h / (2.0D * zoom)), 16) + 1;
        List<HoloMapTerrainTile> tiles = HoloMapTerrainClientState.tiles(dimension,
                minChunkX, maxChunkX, minChunkZ, maxChunkZ);
        graphics.fill(x, y, x + w, y + h, 0xCC061014);
        if (tiles.isEmpty()) {
            drawGrid(graphics, x, y, w, h);
            return;
        }
        for (HoloMapTerrainTile tile : tiles) {
            drawTile(graphics, tile, centerX, centerZ, zoom, x, y, w, h);
        }
        drawGrid(graphics, x, y, w, h);
    }

    private static void drawTile(GuiGraphicsExtractor graphics, HoloMapTerrainTile tile,
            double centerX, double centerZ, double zoom, int x, int y, int w, int h) {
        double baseX = tile.chunkX() * 16.0D;
        double baseZ = tile.chunkZ() * 16.0D;
        int screenX = x + w / 2 + (int) Math.floor((baseX - centerX) * zoom);
        int screenY = y + h / 2 + (int) Math.floor((baseZ - centerZ) * zoom);
        int chunkSize = Math.max(1, (int) Math.ceil(16.0D * zoom));
        if (chunkSize <= 18 || !highDetailTerrain()) {
            graphics.fill(screenX, screenY, screenX + chunkSize, screenY + chunkSize,
                    HoloMapVisualStyle.terrainColor(tile.averageColor()));
            return;
        }
        int pixelSize = Math.max(1, (int) Math.ceil(zoom));
        for (int localZ = 0; localZ < HoloMapTerrainTile.SIZE; localZ++) {
            for (int localX = 0; localX < HoloMapTerrainTile.SIZE; localX++) {
                int px = x + w / 2 + (int) Math.floor((baseX + localX - centerX) * zoom);
                int py = y + h / 2 + (int) Math.floor((baseZ + localZ - centerZ) * zoom);
                graphics.fill(px, py, px + pixelSize, py + pixelSize,
                        HoloMapVisualStyle.terrainColor(tile.pixel(localX, localZ)));
            }
        }
    }

    private static void drawMarkers(GuiGraphicsExtractor graphics, Player player,
            int x, int y, int w, int h, double zoom) {
        String dimension = player.level().dimension().identifier().toString();
        double centerX = player.getX();
        double centerZ = player.getZ();
        HoloMapSnapshotPacket snapshot = HoloMapClientState.snapshot();
        int limit = markerLimit();
        snapshot.markers().stream()
                .filter(marker -> dimension.equals(marker.dimension()))
                .filter(marker -> marker.state() != IMapMarker.MarkerState.HIDDEN)
                .sorted(Comparator.comparingDouble(marker -> HoloMapVisualPriority.drawPriority(
                        distance(centerX, centerZ, marker.x(), marker.z()), marker.state(), marker.kind(), false)))
                .limit(limit)
                .forEach(marker -> {
                    int mx = x + w / 2 + (int) Math.round((marker.x() - centerX) * zoom);
                    int my = y + h / 2 + (int) Math.round((marker.z() - centerZ) * zoom);
                    int color = HoloMapVisualStyle.markerColor(player, marker);
                    int size = HoloMapVisualStyle.markerScalePixels(4);
                    if (mx < x - 8 || mx > x + w + 8 || my < y - 8 || my > y + h + 8) {
                        HoloMapGlyphRenderer.drawEdgeIndicator(graphics,
                                Math.max(x + 4, Math.min(x + w - 4, mx)),
                                Math.max(y + 4, Math.min(y + h - 4, my)), color);
                        return;
                    }
                    HoloMapGlyphRenderer.drawMarker(graphics, marker, mx, my, color, size, false);
                });
    }

    private static void drawPlayer(GuiGraphicsExtractor graphics, Player player, int x, int y, int size) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFFFFFFF);
        double yaw = Math.toRadians(player.getYRot());
        int tipX = cx - (int) Math.round(Math.sin(yaw) * 11.0D);
        int tipY = cy + (int) Math.round(Math.cos(yaw) * 11.0D);
        HoloMapGlyphRenderer.drawLine(graphics, cx, cy, tipX, tipY, 0xFFFFFFFF);
    }

    private static void drawReadout(GuiGraphicsExtractor graphics, Font font, Player player, int x, int y, int size) {
        String text = "HOLOMAP " + HoloMapTerrainClientState.discoveredCount();
        if (booleanConfig(Config.MINIMAP_SHOW_COORDINATES, true)) {
            text += " // " + player.blockPosition().getX() + "," + player.blockPosition().getZ();
        }
        graphics.text(font, Component.literal(text), x, y + size + 5, HoloMapVisualStyle.text(player), true);
    }

    private static void drawGrid(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        for (int gx = x; gx <= x + w; gx += 16) {
            graphics.fill(gx, y, gx + 1, y + h, HoloMapVisualStyle.withAlpha(HoloMapVisualStyle.accent(Minecraft.getInstance().player), 0x24));
        }
        for (int gy = y; gy <= y + h; gy += 16) {
            graphics.fill(x, gy, x + w, gy + 1, HoloMapVisualStyle.withAlpha(HoloMapVisualStyle.accent(Minecraft.getInstance().player), 0x24));
        }
    }

    private static boolean enabled() {
        try {
            return toggledVisible && Config.MINIMAP_ENABLED.get();
        } catch (RuntimeException exception) {
            return toggledVisible;
        }
    }

    private static Config.MiniMapCorner corner() {
        if (cornerOverride != null) {
            return cornerOverride;
        }
        try {
            return Config.MINIMAP_CORNER.get();
        } catch (RuntimeException exception) {
            return Config.MiniMapCorner.TOP_RIGHT;
        }
    }

    private static int minimapSize() {
        try {
            return Math.max(64, Math.min(196, Config.MINIMAP_SIZE.get()));
        } catch (RuntimeException exception) {
            return 104;
        }
    }

    private static double minimapZoom() {
        try {
            return Math.max(0.5D, Math.min(4.0D, Config.MINIMAP_ZOOM.get() + zoomOffset));
        } catch (RuntimeException exception) {
            return 1.35D;
        }
    }

    private static int markerLimit() {
        try {
            return Math.max(0, Math.min(192, Config.MINIMAP_MARKER_DENSITY.get()));
        } catch (RuntimeException exception) {
            return 24;
        }
    }

    private static boolean highDetailTerrain() {
        return booleanConfig(Config.MINIMAP_HIGH_DETAIL_TERRAIN, false);
    }

    private static void renderCoreFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        try {
            Class.forName("com.knoxhack.echoholomap.integration.HoloMapRenderCoreClientIntegration")
                    .getMethod("drawMinimapFrame", GuiGraphicsExtractor.class, int.class, int.class, int.class, int.class)
                    .invoke(null, graphics, x, y, width, height);
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }

    private static boolean booleanConfig(net.neoforged.neoforge.common.ModConfigSpec.BooleanValue value,
            boolean fallback) {
        try {
            return value.get();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static double distance(double x0, double z0, double x1, double z1) {
        double dx = x1 - x0;
        double dz = z1 - z0;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
