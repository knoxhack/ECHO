package com.knoxhack.echoashfallprotocol.worldgen;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Defines different types of procedural structures with their characteristics.
 */
public enum StructureType {
    
    BIO_LAB("bio_lab", 3, 8, 16, 32, 
            new BlockPalette(Blocks.SMOOTH_STONE, Blocks.STONE_BRICKS, Blocks.GLASS, 
                    Blocks.MOSSY_STONE_BRICKS, Blocks.IRON_BARS)),
    
    DATA_CENTER("data_center", 4, 10, 20, 40,
            new BlockPalette(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.IRON_BARS,
                    Blocks.BOOKSHELF, Blocks.REDSTONE_BLOCK)),
    
    MILITARY_VAULT("military_vault", 5, 12, 24, 48,
            new BlockPalette(Blocks.SMOOTH_STONE, Blocks.IRON_BLOCK, Blocks.OBSIDIAN,
                    Blocks.BARREL, Blocks.IRON_DOOR)),
    
    REACTOR_RUIN("reactor_ruin", 4, 10, 20, 36,
            new BlockPalette(Blocks.STONE_BRICKS, Blocks.GLOWSTONE, Blocks.IRON_BARS,
                    Blocks.CRACKED_STONE_BRICKS, Blocks.LAVA)),
    
    DROP_POD("drop_pod", 1, 3, 7, 14,
            new BlockPalette(Blocks.IRON_BLOCK, Blocks.GLASS, Blocks.IRON_TRAPDOOR,
                    Blocks.CRAFTING_TABLE, Blocks.CHEST)),
    
    SUBWAY_STATION("subway_station", 3, 6, 18, 30,
            new BlockPalette(Blocks.STONE_BRICKS, Blocks.SMOOTH_STONE, Blocks.IRON_BARS,
                    Blocks.CRACKED_STONE_BRICKS, Blocks.RAIL)),
    
    SATELLITE_ARRAY("satellite_array", 2, 4, 12, 24,
            new BlockPalette(Blocks.IRON_BLOCK, Blocks.LIGHT_GRAY_CONCRETE, Blocks.GLASS,
                    Blocks.OXIDIZED_COPPER, Blocks.REDSTONE_BLOCK)),
    
    RADIO_TOWER("radio_tower", 2, 3, 10, 20,
            new BlockPalette(Blocks.IRON_BARS, Blocks.IRON_BLOCK, Blocks.LIGHTNING_ROD,
                    Blocks.OXIDIZED_COPPER, Blocks.REDSTONE_LAMP)),
    
    SEWER_JUNCTION("sewer_junction", 3, 5, 15, 25,
            new BlockPalette(Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.WATER,
                    Blocks.SLIME_BLOCK, Blocks.IRON_BARS)),
    
    TRAIN_YARD("train_yard", 4, 8, 20, 40,
            new BlockPalette(Blocks.SMOOTH_STONE, Blocks.IRON_BLOCK, Blocks.RAIL,
                    Blocks.OAK_PLANKS, Blocks.CHEST)),
    
    // === EXPLORATION 1.1: FACTION HUBS ===
    REMNANT_OUTPOST("remnant_outpost", 4, 8, 20, 36,
            new BlockPalette(Blocks.STONE_BRICKS, Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK,
                    Blocks.CRACKED_STONE_BRICKS, Blocks.DISPENSER)),
    
    SALVAGER_TRADING_POST("salvager_trading_post", 3, 6, 15, 28,
            new BlockPalette(Blocks.OAK_PLANKS, Blocks.COBBLESTONE, Blocks.CHEST,
                    Blocks.MOSSY_COBBLESTONE, Blocks.EMERALD_BLOCK)),
    
    MUTANT_SANCTUARY("mutant_sanctuary", 4, 10, 18, 32,
            new BlockPalette(Blocks.MOSSY_STONE_BRICKS, Blocks.GLOWSTONE, Blocks.SLIME_BLOCK,
                    Blocks.DIRT, Blocks.BEACON)),
    
    // === EXPLORATION 1.1: WORLD POIs ===
    CRYOGENIC_RUINS("cryogenic_ruins", 3, 7, 15, 30,
            new BlockPalette(Blocks.PACKED_ICE, Blocks.IRON_BLOCK, Blocks.GLASS,
                    Blocks.BLUE_ICE, Blocks.CHEST)),
    
    RELAY_STATION("relay_station", 2, 4, 10, 20,
            new BlockPalette(Blocks.IRON_BLOCK, Blocks.LIGHT_GRAY_CONCRETE, Blocks.REDSTONE_LAMP,
                    Blocks.OXIDIZED_COPPER, Blocks.BEACON)),
    
    DERELICT_WORKSHOP("derelict_workshop", 3, 5, 14, 24,
            new BlockPalette(Blocks.SMOOTH_STONE, Blocks.IRON_BARS, Blocks.CRAFTING_TABLE,
                    Blocks.COBBLESTONE, Blocks.ANVIL)),
    
    ABANDONED_MINE("abandoned_mine", 5, 12, 20, 40,
            new BlockPalette(Blocks.STONE, Blocks.COAL_ORE, Blocks.RAIL,
                    Blocks.GRAVEL, Blocks.CHEST)),
    
    OBSERVATION_POST("observation_post", 2, 3, 8, 16,
            new BlockPalette(Blocks.IRON_BLOCK, Blocks.GLASS, Blocks.DAYLIGHT_DETECTOR,
                    Blocks.OXIDIZED_COPPER, Blocks.COMPARATOR));
    
    private final String name;
    private final int minRooms;
    private final int maxRooms;
    private final int minSize;
    private final int maxSize;
    private final BlockPalette palette;
    
    StructureType(String name, int minRooms, int maxRooms, int minSize, int maxSize, BlockPalette palette) {
        this.name = name;
        this.minRooms = minRooms;
        this.maxRooms = maxRooms;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.palette = palette;
    }
    
    public String getName() { return name; }
    public int getMinRooms() { return minRooms; }
    public int getMaxRooms() { return maxRooms; }
    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }
    public BlockPalette getPalette() { return palette; }
    
    public int getRandomRoomCount(RandomSource random) {
        return minRooms + random.nextInt(maxRooms - minRooms + 1);
    }
    
    public int getRandomSize(RandomSource random) {
        return minSize + random.nextInt(maxSize - minSize + 1);
    }
    
    public static StructureType getRandomType(RandomSource random) {
        return values()[random.nextInt(values().length)];
    }
    
    public static StructureType byName(String name) {
        for (StructureType type : values()) {
            if (type.name.equals(name)) return type;
        }
        return null;
    }
    
    /**
     * Block palette for a structure type
     */
    public record BlockPalette(Block primary, Block secondary, Block accent,
                               Block decayed, Block special) {

        public Block getRandomBlock(RandomSource random, float decayChance) {
            float roll = random.nextFloat();
            if (roll < decayChance * 0.3f) return decayed;
            if (roll < 0.5f) return primary;
            if (roll < 0.8f) return secondary;
            if (roll < 0.95f) return accent;
            return special;
        }
    }

    /**
     * Architectural styles (Pass 5).
     *
     * Each style carries an alternate palette so two structures of the same
     * StructureType placed side-by-side look visibly different. The style is
     * picked per-structure (seeded by origin position) and its palette is used
     * for walls/floors/ceilings in place of {@link #getPalette()}.
     *
     * Each StructureType opts in to a subset via {@link #getValidStyles()}.
     */
    public enum ArchStyle {
        BRUTALIST(new BlockPalette(
                Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.IRON_BARS,
                Blocks.CRACKED_STONE_BRICKS, Blocks.SMOOTH_STONE)),
        MODULAR(new BlockPalette(
                Blocks.WHITE_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.GLASS_PANE,
                Blocks.WHITE_CONCRETE_POWDER, Blocks.IRON_BLOCK)),
        INDUSTRIAL_DECAY(new BlockPalette(
                Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.OXIDIZED_COPPER,
                Blocks.CRACKED_STONE_BRICKS, Blocks.IRON_BLOCK)),
        RETROFIT_TIMBER(new BlockPalette(
                Blocks.STRIPPED_OAK_LOG, Blocks.OAK_PLANKS, Blocks.MUD_BRICKS,
                Blocks.MOSSY_COBBLESTONE, Blocks.OAK_FENCE)),
        BUNKER(new BlockPalette(
                Blocks.SMOOTH_STONE, Blocks.IRON_BLOCK, Blocks.OBSIDIAN,
                Blocks.CRACKED_STONE_BRICKS, Blocks.LIGHT_GRAY_CONCRETE));

        private final BlockPalette palette;

        ArchStyle(BlockPalette palette) {
            this.palette = palette;
        }

        public BlockPalette getPalette() { return palette; }
    }

    /**
     * Returns the list of architectural styles this StructureType is allowed
     * to use. The first call returns null only if the type has no overrides
     * and should fall back to {@link #getPalette()}.
     */
    public List<ArchStyle> getValidStyles() {
        return switch (this) {
            case BIO_LAB -> List.of(ArchStyle.BRUTALIST, ArchStyle.MODULAR);
            case DATA_CENTER -> List.of(ArchStyle.BRUTALIST, ArchStyle.MODULAR, ArchStyle.INDUSTRIAL_DECAY);
            case MILITARY_VAULT -> List.of(ArchStyle.BUNKER);
            case REACTOR_RUIN -> List.of(ArchStyle.INDUSTRIAL_DECAY, ArchStyle.BRUTALIST);
            case SUBWAY_STATION -> List.of(ArchStyle.INDUSTRIAL_DECAY, ArchStyle.BRUTALIST);
            case SATELLITE_ARRAY, RELAY_STATION, OBSERVATION_POST -> List.of(ArchStyle.MODULAR, ArchStyle.BUNKER);
            case RADIO_TOWER -> List.of(ArchStyle.INDUSTRIAL_DECAY);
            case SEWER_JUNCTION -> List.of(ArchStyle.INDUSTRIAL_DECAY);
            case TRAIN_YARD -> List.of(ArchStyle.INDUSTRIAL_DECAY, ArchStyle.RETROFIT_TIMBER);
            case REMNANT_OUTPOST -> List.of(ArchStyle.BUNKER, ArchStyle.BRUTALIST);
            case SALVAGER_TRADING_POST -> List.of(ArchStyle.RETROFIT_TIMBER, ArchStyle.INDUSTRIAL_DECAY);
            case MUTANT_SANCTUARY -> List.of(ArchStyle.RETROFIT_TIMBER, ArchStyle.INDUSTRIAL_DECAY);
            case CRYOGENIC_RUINS -> List.of(ArchStyle.MODULAR, ArchStyle.BUNKER);
            case DERELICT_WORKSHOP -> List.of(ArchStyle.RETROFIT_TIMBER, ArchStyle.INDUSTRIAL_DECAY);
            case ABANDONED_MINE -> List.of(ArchStyle.RETROFIT_TIMBER, ArchStyle.INDUSTRIAL_DECAY);
            default -> List.of();
        };
    }

    /**
     * Pick one ArchStyle for a structure instance, seeded by the origin so the
     * same chunk regenerates identically. Returns null if the type has no
     * style overrides; callers fall back to {@link #getPalette()}.
     */
    /**
     * Maximum floors a structure of this type may generate (Pass 1).
     * Default is 1 (single-story); types with multi-floor support opt in here.
     * Gated rollout; start with DATA_CENTER and MILITARY_VAULT only.
     */
    public int maxFloors() {
        return switch (this) {
            case DATA_CENTER, MILITARY_VAULT -> 2;
            default -> 1;
        };
    }

    public ArchStyle pickStyle(long seed) {
        List<ArchStyle> styles = getValidStyles();
        if (styles.isEmpty()) return null;
        // Deterministic mixing of the seed so neighbouring origins don't
        // collapse onto the same style.
        long mixed = (seed ^ (seed >>> 32)) * 2862933555777941757L + 3037000493L;
        int idx = (int) Math.floorMod(mixed, styles.size());
        return styles.get(idx);
    }
}
