package com.knoxhack.echoholomap.client;

import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echonetcore.client.EchoNetClientActions;
import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.map.HoloMapTerrainTile;
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
    private static final int ACCENT = 0xFF38DFF4;
    private static boolean toggledVisible = true;
    private static long lastRequestTick = -200L;

    private HoloMapMiniMapOverlay() {
    }

    public static void toggle() {
        toggledVisible = !toggledVisible;
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
        graphics.fill(x - 4, y - 4, x + size + 4, y + size + 18, 0xB8061014);
        graphics.outline(x - 4, y - 4, size + 8, size + 22, 0xAA38DFF4);
        graphics.fill(x - 3, y - 3, x + size + 3, y - 1, ACCENT);
        graphics.enableScissor(x, y, x + size, y + size);
        drawTerrain(graphics, player, x, y, size, size, minimapZoom());
        drawMarkers(graphics, player, x, y, size, size, minimapZoom());
        drawPlayer(graphics, player, x, y, size);
        graphics.disableScissor();
        Font font = minecraft.font;
        graphics.text(font, Component.literal("HOLOMAP " + HoloMapTerrainClientState.discoveredCount()),
                x, y + size + 5, 0xD9F7FF, true);
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
        if (chunkSize <= 18) {
            graphics.fill(screenX, screenY, screenX + chunkSize, screenY + chunkSize, tile.averageColor());
            return;
        }
        int pixelSize = Math.max(1, (int) Math.ceil(zoom));
        for (int localZ = 0; localZ < HoloMapTerrainTile.SIZE; localZ++) {
            for (int localX = 0; localX < HoloMapTerrainTile.SIZE; localX++) {
                int px = x + w / 2 + (int) Math.floor((baseX + localX - centerX) * zoom);
                int py = y + h / 2 + (int) Math.floor((baseZ + localZ - centerZ) * zoom);
                graphics.fill(px, py, px + pixelSize, py + pixelSize, tile.pixel(localX, localZ));
            }
        }
    }

    private static void drawMarkers(GuiGraphicsExtractor graphics, Player player,
            int x, int y, int w, int h, double zoom) {
        String dimension = player.level().dimension().identifier().toString();
        double centerX = player.getX();
        double centerZ = player.getZ();
        HoloMapSnapshotPacket snapshot = HoloMapClientState.snapshot();
        snapshot.markers().stream()
                .filter(marker -> dimension.equals(marker.dimension()))
                .filter(marker -> marker.state() != IMapMarker.MarkerState.HIDDEN)
                .sorted(Comparator.comparing(marker -> marker.state().ordinal()))
                .limit(48)
                .forEach(marker -> {
                    int mx = x + w / 2 + (int) Math.round((marker.x() - centerX) * zoom);
                    int my = y + h / 2 + (int) Math.round((marker.z() - centerZ) * zoom);
                    if (mx < x - 6 || mx > x + w + 6 || my < y - 6 || my > y + h + 6) {
                        return;
                    }
                    int color = markerColor(marker);
                    if (marker.radius() > 0.0F) {
                        int radius = Math.max(3, Math.min(32, (int) Math.round(marker.radius() * zoom)));
                        graphics.outline(mx - radius, my - radius, radius * 2, radius * 2, withAlpha(color, 0x55));
                    }
                    graphics.fill(mx - 2, my - 2, mx + 3, my + 3, color);
                    graphics.outline(mx - 4, my - 4, 8, 8, withAlpha(color, 0xAA));
                });
    }

    private static void drawPlayer(GuiGraphicsExtractor graphics, Player player, int x, int y, int size) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFFFFFFF);
        double yaw = Math.toRadians(player.getYRot());
        int tipX = cx - (int) Math.round(Math.sin(yaw) * 11.0D);
        int tipY = cy + (int) Math.round(Math.cos(yaw) * 11.0D);
        drawLine(graphics, cx, cy, tipX, tipY, 0xFFFFFFFF);
    }

    private static void drawGrid(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        for (int gx = x; gx <= x + w; gx += 16) {
            graphics.fill(gx, y, gx + 1, y + h, 0x2438DFF4);
        }
        for (int gy = y; gy <= y + h; gy += 16) {
            graphics.fill(x, gy, x + w, gy + 1, 0x2438DFF4);
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
            return Math.max(0.5D, Math.min(4.0D, Config.MINIMAP_ZOOM.get()));
        } catch (RuntimeException exception) {
            return 1.35D;
        }
    }

    private static int markerColor(HoloMapSnapshotPacket.MarkerData marker) {
        if (marker.state() == IMapMarker.MarkerState.LOCKED) {
            return 0xFF9FB4BE;
        }
        if (marker.state() == IMapMarker.MarkerState.CHECKED) {
            return 0xFF92F7A6;
        }
        return switch (marker.kind()) {
            case CRASH_SITE -> 0xFFFFA05B;
            case ROUTE -> 0xFF92F7A6;
            case HAZARD -> 0xFFFF6688;
            case MISSION -> ACCENT;
            case BASE_OUTPOST -> 0xFFFFDA73;
            case ORBITAL_SCAN -> 0xFFA58BFF;
            case NEXUS_ANOMALY -> 0xFFFF8FEA;
            case DRONE_SCAN -> 0xFF7CF7D4;
            case REGION, GENERIC -> 0xFFD9F7FF;
        };
    }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
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
            int px = x0 + dx * i / steps;
            int py = y0 + dy * i / steps;
            graphics.fill(px, py, px + 1, py + 1, color);
        }
    }
}
