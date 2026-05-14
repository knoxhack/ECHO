package com.knoxhack.echoholomap.client;

import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echoholomap.network.HoloMapSnapshotPacket;
import com.knoxhack.echoholomap.waypoint.HoloMapWaypoint;
import com.knoxhack.echoholomap.waypoint.HoloMapWaypoint.Scope;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HoloMapGlyphRenderer {
    private HoloMapGlyphRenderer() {
    }

    public static void drawMarker(GuiGraphicsExtractor graphics, HoloMapSnapshotPacket.MarkerData marker,
            int x, int y, int color, int size, boolean selected) {
        if (marker.radius() > 0.0F && marker.kind() != IMapMarker.MarkerKind.ROUTE) {
            int radius = Math.max(size + 3, Math.min(44, (int) Math.round(marker.radius() / 8.0D)));
            graphics.outline(x - radius, y - radius, radius * 2, radius * 2,
                    HoloMapVisualStyle.withAlpha(color, 0x55));
        }
        if (marker.state() == IMapMarker.MarkerState.LOCKED) {
            drawLocked(graphics, x, y, color, size);
        } else if (marker.state() == IMapMarker.MarkerState.CHECKED) {
            drawChecked(graphics, x, y, color, size);
        } else {
            switch (marker.kind()) {
                case CRASH_SITE -> drawCrashSite(graphics, x, y, color, size);
                case ROUTE -> drawRouteNode(graphics, x, y, color, size);
                case HAZARD -> drawHazard(graphics, x, y, color, size);
                case MISSION -> drawMission(graphics, x, y, color, size);
                case BASE_OUTPOST -> drawBase(graphics, x, y, color, size);
                case ORBITAL_SCAN -> drawOrbital(graphics, x, y, color, size);
                case NEXUS_ANOMALY -> drawAnomaly(graphics, x, y, color, size);
                case DRONE_SCAN -> drawDrone(graphics, x, y, color, size);
                case REGION, GENERIC -> drawGeneric(graphics, x, y, color, size);
            }
        }
        if (selected) {
            drawSelection(graphics, x, y, Math.max(8, size + 5), HoloMapVisualStyle.TEXT);
        }
    }

    public static void drawMarkerKind(GuiGraphicsExtractor graphics, IMapMarker.MarkerKind kind,
            IMapMarker.MarkerState state, int x, int y, int color, int size) {
        if (state == IMapMarker.MarkerState.LOCKED) {
            drawLocked(graphics, x, y, color, size);
        } else if (state == IMapMarker.MarkerState.CHECKED) {
            drawChecked(graphics, x, y, color, size);
        } else {
            switch (kind == null ? IMapMarker.MarkerKind.GENERIC : kind) {
                case CRASH_SITE -> drawCrashSite(graphics, x, y, color, size);
                case ROUTE -> drawRouteNode(graphics, x, y, color, size);
                case HAZARD -> drawHazard(graphics, x, y, color, size);
                case MISSION -> drawMission(graphics, x, y, color, size);
                case BASE_OUTPOST -> drawBase(graphics, x, y, color, size);
                case ORBITAL_SCAN -> drawOrbital(graphics, x, y, color, size);
                case NEXUS_ANOMALY -> drawAnomaly(graphics, x, y, color, size);
                case DRONE_SCAN -> drawDrone(graphics, x, y, color, size);
                case REGION, GENERIC -> drawGeneric(graphics, x, y, color, size);
            }
        }
    }

    public static void drawWaypoint(GuiGraphicsExtractor graphics, HoloMapWaypoint waypoint,
            int x, int y, int color, int size, boolean selected) {
        if (!waypoint.visible()) {
            color = HoloMapVisualStyle.MUTED;
        }
        if (waypoint.scope() == Scope.SHARED) {
            graphics.outline(x - size, y - size, size * 2, size * 2, color);
            graphics.fill(x - 2, y - 2, x + 3, y + 3, color);
            graphics.fill(x - size, y, x + size + 1, y + 1, color);
        } else if (waypoint.scope() == Scope.PERSONAL) {
            graphics.fill(x - 1, y - size - 2, x + 2, y + size + 3, color);
            graphics.fill(x - size - 2, y - 1, x + size + 3, y + 2, color);
            graphics.outline(x - size, y - size, size * 2, size * 2, color);
        } else {
            graphics.fill(x, y - size - 2, x + 1, y - 2, color);
            graphics.fill(x, y + 3, x + 1, y + size + 3, color);
            graphics.fill(x - size - 2, y, x - 2, y + 1, color);
            graphics.fill(x + 3, y, x + size + 3, y + 1, color);
            graphics.outline(x - size, y - size, size * 2, size * 2, color);
        }
        if (selected) {
            drawSelection(graphics, x, y, Math.max(8, size + 5), HoloMapVisualStyle.TEXT);
        }
    }

    public static void drawWaypointScope(GuiGraphicsExtractor graphics, Scope scope,
            int x, int y, int color, int size) {
        if (scope == Scope.SHARED) {
            graphics.outline(x - size, y - size, size * 2, size * 2, color);
            graphics.fill(x - 2, y - 2, x + 3, y + 3, color);
            graphics.fill(x - size, y, x + size + 1, y + 1, color);
        } else if (scope == Scope.PERSONAL) {
            graphics.fill(x - 1, y - size - 2, x + 2, y + size + 3, color);
            graphics.fill(x - size - 2, y - 1, x + size + 3, y + 2, color);
            graphics.outline(x - size, y - size, size * 2, size * 2, color);
        } else {
            graphics.fill(x, y - size - 2, x + 1, y - 2, color);
            graphics.fill(x, y + 3, x + 1, y + size + 3, color);
            graphics.fill(x - size - 2, y, x - 2, y + 1, color);
            graphics.fill(x + 3, y, x + size + 3, y + 1, color);
            graphics.outline(x - size, y - size, size * 2, size * 2, color);
        }
    }

    public static void drawEdgeIndicator(GuiGraphicsExtractor graphics, int x, int y, int color) {
        graphics.outline(x - 3, y - 3, 6, 6, HoloMapVisualStyle.withAlpha(color, 0xCC));
        graphics.fill(x - 1, y - 1, x + 2, y + 2, color);
    }

    public static void drawSelection(GuiGraphicsExtractor graphics, int x, int y, int radius, int color) {
        graphics.outline(x - radius, y - radius, radius * 2, radius * 2, HoloMapVisualStyle.withAlpha(color, 0xDD));
        graphics.fill(x - radius - 2, y, x - radius + 3, y + 1, color);
        graphics.fill(x + radius - 2, y, x + radius + 3, y + 1, color);
        graphics.fill(x, y - radius - 2, x + 1, y - radius + 3, color);
        graphics.fill(x, y + radius - 2, x + 1, y + radius + 3, color);
    }

    public static void drawLine(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
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

    private static void drawCrashSite(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.fill(x - 1, y - size - 1, x + 2, y + size + 2, color);
        graphics.fill(x - size - 1, y - 1, x + size + 2, y + 2, color);
        graphics.outline(x - size, y - size, size * 2, size * 2, color);
    }

    private static void drawRouteNode(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.fill(x - size - 1, y - 2, x + size + 2, y + 3, color);
        graphics.fill(x - 2, y - size - 1, x + 3, y + size + 2, color);
    }

    private static void drawHazard(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.outline(x - size, y - size, size * 2, size * 2, color);
        graphics.fill(x - 1, y - size - 2, x + 2, y + size + 3, color);
        graphics.fill(x - size - 2, y - 1, x + size + 3, y + 2, color);
    }

    private static void drawMission(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.fill(x - size, y - size, x + size + 1, y - size + 2, color);
        graphics.fill(x - size, y + size - 1, x + size + 1, y + size + 1, color);
        graphics.fill(x - size, y - size, x - size + 2, y + size + 1, color);
        graphics.fill(x + size - 1, y - size, x + size + 1, y + size + 1, color);
        graphics.fill(x - 1, y - 1, x + 2, y + 2, color);
    }

    private static void drawBase(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.outline(x - size - 1, y - size - 1, (size + 1) * 2, (size + 1) * 2, color);
        graphics.fill(x - size + 1, y - size + 1, x + size, y + size, color);
    }

    private static void drawOrbital(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.outline(x - size - 2, y - size - 2, (size + 2) * 2, (size + 2) * 2,
                HoloMapVisualStyle.withAlpha(color, 0xBB));
        graphics.fill(x - 1, y - 1, x + 2, y + 2, color);
        graphics.fill(x - size - 4, y, x - size, y + 1, color);
        graphics.fill(x + size, y, x + size + 5, y + 1, color);
    }

    private static void drawAnomaly(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.fill(x, y - size - 3, x + 1, y - 2, color);
        graphics.fill(x, y + 3, x + 1, y + size + 4, color);
        graphics.fill(x - size - 3, y, x - 2, y + 1, color);
        graphics.fill(x + 3, y, x + size + 4, y + 1, color);
        graphics.outline(x - size, y - size, size * 2, size * 2, color);
    }

    private static void drawDrone(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.outline(x - size, y - size, size * 2, size * 2, color);
        graphics.fill(x - size, y - size, x - size + 3, y - size + 3, color);
        graphics.fill(x + size - 2, y + size - 2, x + size + 1, y + size + 1, color);
    }

    private static void drawGeneric(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.fill(x - size / 2, y - size / 2, x + size / 2 + 1, y + size / 2 + 1, color);
        graphics.outline(x - size, y - size, size * 2, size * 2, color);
    }

    private static void drawLocked(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        graphics.outline(x - size, y - size, size * 2, size * 2, HoloMapVisualStyle.withAlpha(color, 0xAA));
        graphics.fill(x - 1, y - 1, x + 2, y + 2, HoloMapVisualStyle.withAlpha(color, 0xCC));
        graphics.fill(x - size + 2, y - size - 2, x + size - 1, y - size, color);
    }

    private static void drawChecked(GuiGraphicsExtractor graphics, int x, int y, int color, int size) {
        drawGeneric(graphics, x, y, color, size);
        graphics.fill(x - size, y, x - 1, y + 2, HoloMapVisualStyle.TEXT);
        graphics.fill(x - 1, y + 1, x + size + 2, y + 3, HoloMapVisualStyle.TEXT);
    }
}
