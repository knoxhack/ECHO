package com.knoxhack.echolens.client;

import com.knoxhack.echolens.EchoLensClient;
import com.knoxhack.echolens.api.LensAccessPolicy;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensReport;
import com.knoxhack.echolens.api.LensScanMode;
import com.knoxhack.echolens.config.LensConfig;
import com.knoxhack.echolens.registry.LensInspectionService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public final class LensHudOverlay {
    private static ItemStack currentTargetStack = ItemStack.EMPTY;
    private static float visibleProgress;

    private LensHudOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!LensConfig.bool(LensConfig.HUD_ENABLED, true) || minecraft.player == null || minecraft.level == null
                || minecraft.options.hideGui || minecraft.screen != null) {
            currentTargetStack = ItemStack.EMPTY;
            visibleProgress = approach(visibleProgress, 0.0F, partialTick);
            return;
        }
        LensContext context = contextFromHit(minecraft);
        if (context == null) {
            currentTargetStack = ItemStack.EMPTY;
            visibleProgress = approach(visibleProgress, 0.0F, partialTick);
            return;
        }
        LensReport report = LensInspectionService.INSTANCE.inspect(context);
        if (report.isEmpty()) {
            currentTargetStack = ItemStack.EMPTY;
            visibleProgress = approach(visibleProgress, 0.0F, partialTick);
            return;
        }
        currentTargetStack = report.icon();
        visibleProgress = approach(visibleProgress, 1.0F, partialTick);
        drawReport(graphics, minecraft.font, report, context.scanMode(), visibleProgress);
    }

    public static ItemStack currentTargetStack() {
        return currentTargetStack.copy();
    }

    private static LensContext contextFromHit(Minecraft minecraft) {
        HitResult hit = minecraft.hitResult;
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return null;
        }
        double maxDistance = LensConfig.decimal(LensConfig.MAX_SCAN_DISTANCE, 18.0D);
        if (hit.getLocation().distanceToSqr(minecraft.player.getEyePosition()) > maxDistance * maxDistance) {
            return null;
        }
        LensScanMode mode = EchoLensClient.DEEP_SCAN_KEY.isDown()
                ? LensScanMode.DEEP
                : (shiftDown(minecraft) ? LensScanMode.EXPANDED : LensScanMode.COMPACT);
        LensAccessPolicy policy = LensConfig.value(LensConfig.INVENTORY_ACCESS_POLICY, LensAccessPolicy.PUBLIC_ONLY);
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = minecraft.level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            if (fluid.isEmpty()) {
                fluid = minecraft.level.getFluidState(pos);
            }
            return LensContext.block(minecraft.player, minecraft.level, pos, state, fluid, mode, policy);
        }
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            return LensContext.entity(minecraft.player, minecraft.level, entity, mode, policy);
        }
        return null;
    }

    private static void drawReport(GuiGraphicsExtractor graphics, Font font, LensReport report,
            LensScanMode mode, float progress) {
        LensTheme theme = LensTheme.current();
        float opacity = (float) LensConfig.decimal(LensConfig.OPACITY, 0.86D)
                * (LensConfig.bool(LensConfig.REDUCED_MOTION, false)
                || !LensConfig.bool(LensConfig.ANIMATION, true) ? 1.0F : progress);
        int width = panelWidth(mode);
        List<RenderedRow> rows = flatten(report, mode);
        int actionHeight = report.actions().isEmpty() ? 0 : 18;
        int height = Math.min(panelHeight(mode), 34 + rows.size() * 12 + actionHeight + 12);
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = panelX(screenW, width);
        int y = panelY(screenH, height);
        if (LensConfig.bool(LensConfig.ANIMATION, true) && !LensConfig.bool(LensConfig.REDUCED_MOTION, false)) {
            y -= Math.round((1.0F - progress) * 8.0F);
        }
        graphics.fill(x, y, x + width, y + height, theme.alpha(theme.panel(), opacity));
        graphics.fill(x, y, x + width, y + 24, theme.alpha(theme.header(), opacity));
        graphics.outline(x, y, width, height, theme.alpha(theme.border(), opacity));
        graphics.fill(x, y, x + Math.max(32, width / 5), y + 2, theme.alpha(theme.echo(), opacity));
        graphics.fill(x, y + height - 2, x + Math.max(28, width / 7), y + height, theme.alpha(theme.glow(), opacity));
        if (!report.icon().isEmpty()) {
            graphics.item(report.icon(), x + 7, y + 5);
        }
        graphics.text(font, fit(font, report.title().getString(), width - 42), x + 28, y + 5,
                theme.alpha(theme.text(), opacity), false);
        graphics.text(font, fit(font, report.subtitle().getString(), width - 42), x + 28, y + 15,
                theme.alpha(theme.muted(), opacity), false);
        int rowY = y + 30;
        String lastSection = "";
        for (RenderedRow row : rows) {
            if (mode == LensScanMode.DEEP && !row.sectionTitle().equals(lastSection)) {
                graphics.text(font, row.sectionIcon() + " " + row.sectionTitle(), x + 9, rowY,
                        theme.alpha(theme.echo(), opacity), false);
                rowY += 11;
                lastSection = row.sectionTitle();
            }
            graphics.text(font, row.row().icon(), x + 10, rowY, theme.alpha(theme.tone(row.row().tone()), opacity), false);
            graphics.text(font, fit(font, row.row().label().getString(), 76), x + 24, rowY,
                    theme.alpha(theme.muted(), opacity), false);
            graphics.text(font, fit(font, row.row().value().getString(), width - 108), x + 100, rowY,
                    theme.alpha(theme.tone(row.row().tone()), opacity), false);
            rowY += 12;
        }
        if (!report.actions().isEmpty() && LensConfig.bool(LensConfig.SHOW_ACTIONS, true)) {
            int actionY = y + height - 17;
            int actionX = x + 8;
            for (var action : report.actions()) {
                int chipW = Math.max(46, font.width(action.icon() + " " + action.label().getString()) + 12);
                graphics.fill(actionX, actionY, actionX + chipW, actionY + 12,
                        theme.alpha(action.available() ? 0x3316E0FF : 0x22111111, opacity));
                graphics.outline(actionX, actionY, chipW, 12,
                        theme.alpha(action.available() ? theme.border() : theme.muted(), opacity));
                graphics.text(font, action.icon() + " " + action.label().getString(), actionX + 5, actionY + 2,
                        theme.alpha(theme.tone(action.tone()), opacity), false);
                actionX += chipW + 5;
            }
        }
    }

    private static List<RenderedRow> flatten(LensReport report, LensScanMode mode) {
        int maxRows = switch (mode) {
            case COMPACT -> LensConfig.integer(LensConfig.COMPACT_ROW_LIMIT, 4);
            case EXPANDED -> LensConfig.integer(LensConfig.EXPANDED_ROW_LIMIT, 12);
            case DEEP -> LensConfig.integer(LensConfig.DEEP_ROW_LIMIT, 40);
        };
        List<RenderedRow> rows = new ArrayList<>();
        int sectionLimit = mode == LensScanMode.COMPACT ? 3 : Integer.MAX_VALUE;
        int sections = 0;
        for (LensInfoSection section : report.sections()) {
            if (sections >= sectionLimit || rows.size() >= maxRows) {
                break;
            }
            int before = rows.size();
            for (LensInfoRow row : LensInspectionService.visibleRows(section, mode)) {
                if (rows.size() >= maxRows) {
                    break;
                }
                rows.add(new RenderedRow(section.title().getString(), section.icon(), row));
            }
            if (rows.size() > before) {
                sections++;
            }
        }
        return rows;
    }

    private static int panelWidth(LensScanMode mode) {
        int base = switch (mode) {
            case COMPACT -> 230;
            case EXPANDED -> 276;
            case DEEP -> 332;
        };
        return Math.round(base * (float) LensConfig.decimal(LensConfig.SCALE, 1.0D));
    }

    private static int panelHeight(LensScanMode mode) {
        return switch (mode) {
            case COMPACT -> 108;
            case EXPANDED -> 210;
            case DEEP -> 320;
        };
    }

    private static int panelX(int screenW, int width) {
        int offset = LensConfig.integer(LensConfig.OFFSET_X, 0);
        return switch (LensConfig.value(LensConfig.OVERLAY_POSITION, LensConfig.OverlayPosition.TOP_CENTER)) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 12 + offset;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> screenW - width - 12 + offset;
            case TOP_CENTER, BOTTOM_CENTER -> (screenW - width) / 2 + offset;
        };
    }

    private static int panelY(int screenH, int height) {
        int offset = LensConfig.integer(LensConfig.OFFSET_Y, 12);
        return switch (LensConfig.value(LensConfig.OVERLAY_POSITION, LensConfig.OverlayPosition.TOP_CENTER)) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 10 + offset;
            case CENTER_LEFT, CENTER_RIGHT -> (screenH - height) / 2 + offset;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenH - height - 30 + offset;
        };
    }

    private static float approach(float value, float target, float partialTick) {
        if (LensConfig.bool(LensConfig.REDUCED_MOTION, false) || !LensConfig.bool(LensConfig.ANIMATION, true)) {
            return target;
        }
        float speed = Math.max(0.15F, partialTick * 0.35F);
        if (value < target) {
            return Math.min(target, value + speed);
        }
        return Math.max(target, value - speed);
    }

    private static boolean shiftDown(Minecraft minecraft) {
        return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static String fit(Font font, String text, int maxWidth) {
        if (text == null || text.isBlank() || font.width(text) <= maxWidth) {
            return text == null ? "" : text;
        }
        String ellipsis = "...";
        String trimmed = text;
        while (!trimmed.isEmpty() && font.width(trimmed + ellipsis) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ellipsis;
    }

    private record RenderedRow(String sectionTitle, String sectionIcon, LensInfoRow row) {
    }
}
