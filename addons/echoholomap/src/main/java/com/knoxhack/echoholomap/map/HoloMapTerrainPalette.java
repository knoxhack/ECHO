package com.knoxhack.echoholomap.map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class HoloMapTerrainPalette {
    private HoloMapTerrainPalette() {
    }

    public static int colorFor(ServerLevel level, BlockPos surfacePos) {
        if (level == null || surfacePos == null) {
            return HoloMapTerrainTile.FALLBACK_COLOR;
        }
        BlockState state = level.getBlockState(surfacePos);
        if (state.isAir() && surfacePos.getY() > level.getMinY()) {
            surfacePos = surfacePos.below();
            state = level.getBlockState(surfacePos);
        }
        Identifier biomeId = biomeId(level.getBiome(surfacePos));
        int base = baseColor(level.dimension(), biomeId, state);
        return shade(base, surfacePos.getY(), state);
    }

    public static int colorForBiome(String dimension, String biomePath, int height, boolean water) {
        Identifier dimensionId = Identifier.tryParse(dimension == null ? "" : dimension);
        Identifier biomeId = Identifier.tryParse("minecraft:" + (biomePath == null ? "plains" : biomePath));
        int base = water ? 0xFF1E5E7E : baseColor(dimensionId, biomeId, null);
        return shade(base, height, water);
    }

    private static Identifier biomeId(Holder<Biome> biome) {
        return biome == null
                ? Identifier.withDefaultNamespace("plains")
                : biome.unwrapKey().map(key -> key.identifier()).orElse(Identifier.withDefaultNamespace("plains"));
    }

    private static int baseColor(net.minecraft.resources.ResourceKey<Level> dimension, Identifier biome, BlockState state) {
        Identifier dimensionId = dimension == null ? Level.OVERWORLD.identifier() : dimension.identifier();
        return baseColor(dimensionId, biome, state);
    }

    private static int baseColor(Identifier dimension, Identifier biome, BlockState state) {
        if (state != null) {
            if (state.is(Blocks.WATER) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                return 0xFF1C6285;
            }
            if (state.is(Blocks.LAVA)) {
                return 0xFFD65F28;
            }
            if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
                return 0xFFE6F7F4;
            }
            if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
                return state.is(Blocks.RED_SAND) ? 0xFFC67948 : 0xFFC8B979;
            }
        }
        String dim = dimension == null ? "" : dimension.toString();
        String path = biome == null ? "plains" : biome.getPath();
        if (dim.contains("the_nether") || path.contains("nether") || path.contains("crimson") || path.contains("warped")) {
            return path.contains("warped") ? 0xFF287A73 : 0xFF7F2D2D;
        }
        if (dim.contains("the_end") || path.contains("end")) {
            return 0xFF6E6687;
        }
        if (path.contains("ocean") || path.contains("river")) {
            return 0xFF1F6E93;
        }
        if (path.contains("beach")) {
            return 0xFFCABF82;
        }
        if (path.contains("snow") || path.contains("ice") || path.contains("frozen")) {
            return 0xFFD7ECEB;
        }
        if (path.contains("desert") || path.contains("badlands")) {
            return path.contains("badlands") ? 0xFFA86143 : 0xFFC7B66C;
        }
        if (path.contains("swamp") || path.contains("mangrove")) {
            return 0xFF4E7049;
        }
        if (path.contains("jungle")) {
            return 0xFF2F8B3C;
        }
        if (path.contains("forest") || path.contains("grove") || path.contains("taiga")) {
            return path.contains("taiga") ? 0xFF3E7661 : 0xFF3B8B4C;
        }
        if (path.contains("savanna")) {
            return 0xFF9EA65B;
        }
        if (path.contains("meadow") || path.contains("plains")) {
            return 0xFF73A950;
        }
        if (path.contains("peak") || path.contains("slope") || path.contains("mountain")) {
            return 0xFF7E8A81;
        }
        if (path.contains("cave") || path.contains("deep")) {
            return 0xFF405159;
        }
        return 0xFF5B8D4C;
    }

    private static int shade(int color, int height, BlockState state) {
        boolean water = state != null && (state.is(Blocks.WATER) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE));
        return shade(color, height, water);
    }

    private static int shade(int color, int height, boolean water) {
        int adjustment = Math.max(-28, Math.min(34, (height - 64) / 5));
        if (water) {
            adjustment -= 8;
        }
        return adjust(color, adjustment);
    }

    private static int adjust(int color, int delta) {
        int a = (color >>> 24) & 0xFF;
        int r = clamp(((color >>> 16) & 0xFF) + delta);
        int g = clamp(((color >>> 8) & 0xFF) + delta);
        int b = clamp((color & 0xFF) + delta);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
