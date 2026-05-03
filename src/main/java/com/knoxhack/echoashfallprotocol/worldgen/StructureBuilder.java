package com.knoxhack.echoashfallprotocol.worldgen;

import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Fluent API for building Minecraft structures programmatically.
 * Uses vanilla StructureTemplate for correct NBT format.
 */
public class StructureBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<String, List<String>> ALIAS_PATHS = Map.of(
            "bio_lab", List.of("bio_lab"),
            "data_center_ruin", List.of("data_center_ruin"),
            "drop_pod", List.of("drop_pod"),
            "military_vault", List.of("military_vault"),
            "reactor_ruin", List.of("reactor_ruin")
    );

    private final int width;
    private final int height;
    private final int depth;
    private final List<StructureBlockInfo> blocks = new ArrayList<>();
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private Palette palette = Palette.globalDefault();

    public StructureBuilder(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * Biome-themed block palette.
     * Built lazily so DeferredBlock.get() is only called after registries are populated.
     */
    public static final class Palette {
        public final BlockState wall;
        public final BlockState floor;
        public final BlockState roof;
        public final BlockState accent;
        public final BlockState ground;
        public final BlockState glass;
        public final BlockState light;
        public final BlockState debris;
        public final BlockState ruin;

        public Palette(BlockState wall, BlockState floor, BlockState roof, BlockState accent,
                       BlockState ground, BlockState glass, BlockState light,
                       BlockState debris, BlockState ruin) {
            this.wall = wall;
            this.floor = floor;
            this.roof = roof;
            this.accent = accent;
            this.ground = ground;
            this.glass = glass;
            this.light = light;
            this.debris = debris;
            this.ruin = ruin;
        }

        private static BlockState s(net.neoforged.neoforge.registries.DeferredBlock<Block> b) {
            return b.get().defaultBlockState();
        }

        public static Palette crashZone() {
            return new Palette(
                    s(ModBlocks.RUSTED_METAL_SHEET),
                    s(ModBlocks.CHARRED_WOOD_LOG),
                    s(ModBlocks.RUSTED_METAL_SHEET),
                    s(ModBlocks.DROP_POD_HULL),
                    s(ModBlocks.ASHEN_WASTELAND_DIRT),
                    s(ModBlocks.DROP_POD_GLASS),
                    Blocks.GLOWSTONE.defaultBlockState(),
                    s(ModBlocks.RUSTED_METAL_DEBRIS),
                    s(ModBlocks.CONCRETE_RUBBLE)
            );
        }

        public static Palette ruinedCityscape() {
            return new Palette(
                    s(ModBlocks.OIL_STAINED_CONCRETE),
                    Blocks.STONE_BRICKS.defaultBlockState(),
                    s(ModBlocks.OIL_STAINED_CONCRETE),
                    Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    Blocks.GRAVEL.defaultBlockState(),
                    Blocks.GRAY_STAINED_GLASS.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    s(ModBlocks.CONCRETE_RUBBLE),
                    s(ModBlocks.RUBBLE)
            );
        }

        public static Palette radiation() {
            return new Palette(
                    s(ModBlocks.IRRADIATED_CRUST),
                    s(ModBlocks.RADIATION_BLOCK),
                    s(ModBlocks.IRRADIATED_CRUST),
                    s(ModBlocks.TOXIC_WASTE_BARREL),
                    s(ModBlocks.NEXUS_CRACKED_SOIL),
                    Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
                    Blocks.GLOWSTONE.defaultBlockState(),
                    s(ModBlocks.FALLOUT_DUST),
                    s(ModBlocks.SCATTERED_BONES)
            );
        }

        public static Palette toxicSwamp() {
            return new Palette(
                    s(ModBlocks.CONTAMINATED_SOIL),
                    Blocks.OAK_PLANKS.defaultBlockState(),
                    Blocks.DARK_OAK_PLANKS.defaultBlockState(),
                    s(ModBlocks.TOXIC_PUDDLE),
                    s(ModBlocks.ACIDIC_SLUDGE),
                    Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
                    Blocks.SEA_LANTERN.defaultBlockState(),
                    s(ModBlocks.TOXIC_MOSS),
                    s(ModBlocks.MUTATED_BUSH)
            );
        }

        public static Palette industrial() {
            return new Palette(
                    s(ModBlocks.RUSTED_METAL_SHEET),
                    Blocks.SMOOTH_STONE.defaultBlockState(),
                    s(ModBlocks.RUSTED_METAL_SHEET),
                    Blocks.IRON_BARS.defaultBlockState(),
                    Blocks.GRAVEL.defaultBlockState(),
                    Blocks.GLASS.defaultBlockState(),
                    Blocks.REDSTONE_LAMP.defaultBlockState(),
                    s(ModBlocks.RUSTED_METAL_DEBRIS),
                    s(ModBlocks.CONCRETE_RUBBLE)
            );
        }

        public static Palette cryogenic() {
            return new Palette(
                    Blocks.PACKED_ICE.defaultBlockState(),
                    Blocks.BLUE_ICE.defaultBlockState(),
                    Blocks.PACKED_ICE.defaultBlockState(),
                    Blocks.IRON_BLOCK.defaultBlockState(),
                    Blocks.SNOW_BLOCK.defaultBlockState(),
                    Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
                    Blocks.SEA_LANTERN.defaultBlockState(),
                    Blocks.ICE.defaultBlockState(),
                    Blocks.SNOW_BLOCK.defaultBlockState()
            );
        }

        public static Palette ruinedPlains() {
            return new Palette(
                    s(ModBlocks.OIL_STAINED_CONCRETE),
                    Blocks.OAK_PLANKS.defaultBlockState(),
                    Blocks.OAK_PLANKS.defaultBlockState(),
                    s(ModBlocks.RUBBLE),
                    s(ModBlocks.WASTELAND_DIRT),
                    Blocks.GLASS.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    s(ModBlocks.CONCRETE_RUBBLE),
                    s(ModBlocks.DRY_GRASS)
            );
        }

        public static Palette globalDefault() {
            return new Palette(
                    Blocks.COBBLESTONE.defaultBlockState(),
                    Blocks.OAK_PLANKS.defaultBlockState(),
                    Blocks.OAK_PLANKS.defaultBlockState(),
                    Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.DIRT.defaultBlockState(),
                    Blocks.GLASS.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.GRAVEL.defaultBlockState(),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState()
            );
        }
    }

    public StructureBuilder palette(Palette p) { this.palette = p; return this; }
    public BlockState wall()   { return palette.wall; }
    public BlockState floorB() { return palette.floor; }
    public BlockState roof()   { return palette.roof; }
    public BlockState accent() { return palette.accent; }
    public BlockState ground() { return palette.ground; }
    public BlockState glass()  { return palette.glass; }
    public BlockState light()  { return palette.light; }
    public BlockState debris() { return palette.debris; }
    public BlockState ruinB()  { return palette.ruin; }

    // ============= ARCHITECTURE HELPERS =============

    /** Hollow box with palette wall + floor + roof. Interior cleared to AIR. */
    public StructureBuilder room(int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        // Vertical walls
        for (int y = minY; y <= maxY; y++) {
            fill(minX, y, minZ, maxX, y, minZ, palette.wall);
            fill(minX, y, maxZ, maxX, y, maxZ, palette.wall);
            fill(minX, y, minZ, minX, y, maxZ, palette.wall);
            fill(maxX, y, minZ, maxX, y, maxZ, palette.wall);
        }
        // Floor
        fill(minX, minY, minZ, maxX, minY, maxZ, palette.floor);
        // Ceiling
        fill(minX, maxY, minZ, maxX, maxY, maxZ, palette.roof);
        // Interior air
        if (maxX - minX >= 2 && maxY - minY >= 2 && maxZ - minZ >= 2) {
            fill(minX + 1, minY + 1, minZ + 1, maxX - 1, maxY - 1, maxZ - 1, Blocks.AIR.defaultBlockState());
        }
        return this;
    }

    public StructureBuilder pillar(int x, int y1, int z, int y2) {
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        for (int y = minY; y <= maxY; y++) setBlock(x, y, z, palette.wall);
        return this;
    }

    /** 2-tall doorway opening cut through a wall. axis: 'x' or 'z'. */
    public StructureBuilder door(int x, int y, int z, char axis) {
        setBlock(x, y, z, Blocks.AIR.defaultBlockState());
        setBlock(x, y + 1, z, Blocks.AIR.defaultBlockState());
        if (axis == 'x') {
            setBlock(x + 1, y, z, Blocks.AIR.defaultBlockState());
            setBlock(x + 1, y + 1, z, Blocks.AIR.defaultBlockState());
        } else {
            setBlock(x, y, z + 1, Blocks.AIR.defaultBlockState());
            setBlock(x, y + 1, z + 1, Blocks.AIR.defaultBlockState());
        }
        return this;
    }

    /** Horizontal line of palette glass at eye height. axis: 'x' or 'z'. */
    public StructureBuilder window(int x, int y, int z, char axis, int length) {
        for (int i = 0; i < length; i++) {
            int dx = axis == 'x' ? i : 0;
            int dz = axis == 'z' ? i : 0;
            setBlock(x + dx, y, z + dz, palette.glass);
        }
        return this;
    }

    /** Tiered pyramidal roof using palette roof block. baseY is the lowest tier. */
    public StructureBuilder roofPyramid(int x1, int z1, int x2, int z2, int baseY) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        int y = baseY;
        while (minX <= maxX && minZ <= maxZ) {
            for (int x = minX; x <= maxX; x++) {
                setBlock(x, y, minZ, palette.roof);
                setBlock(x, y, maxZ, palette.roof);
            }
            for (int z = minZ; z <= maxZ; z++) {
                setBlock(minX, y, z, palette.roof);
                setBlock(maxX, y, z, palette.roof);
            }
            minX++; maxX--; minZ++; maxZ--; y++;
        }
        return this;
    }

    /** Peaked gable roof. axis: 'x' (peak runs along X) or 'z'. */
    public StructureBuilder roofGable(int x1, int z1, int x2, int z2, int baseY, char axis) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        if (axis == 'x') {
            int span = (maxZ - minZ) / 2;
            for (int i = 0; i <= span; i++) {
                int y = baseY + i;
                int za = minZ + i;
                int zb = maxZ - i;
                for (int x = minX; x <= maxX; x++) {
                    setBlock(x, y, za, palette.roof);
                    setBlock(x, y, zb, palette.roof);
                }
            }
        } else {
            int span = (maxX - minX) / 2;
            for (int i = 0; i <= span; i++) {
                int y = baseY + i;
                int xa = minX + i;
                int xb = maxX - i;
                for (int z = minZ; z <= maxZ; z++) {
                    setBlock(xa, y, z, palette.roof);
                    setBlock(xb, y, z, palette.roof);
                }
            }
        }
        return this;
    }

    /** Checker pattern using floor + accent. */
    public StructureBuilder floorPattern(int x1, int z1, int x2, int z2, int y) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                setBlock(x, y, z, ((x + z) & 1) == 0 ? palette.floor : palette.accent);
            }
        }
        return this;
    }

    /** Diagonal staircase of palette wall blocks. */
    public StructureBuilder stairsRun(int x, int y, int z, int dx, int dz, int length) {
        for (int i = 0; i < length; i++) {
            setBlock(x + dx * i, y + i, z + dz * i, palette.wall);
        }
        return this;
    }

    /** Vanilla ladder column for multi-floor access. */
    public StructureBuilder ladder(int x, int y1, int z, int y2, Direction facing) {
        BlockState ladderState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        for (int y = minY; y <= maxY; y++) setBlock(x, y, z, ladderState);
        return this;
    }

    // ============= DAMAGE / DECORATION HELPERS =============

    /** Randomly replace ~chance fraction of palette wall blocks within bounds with debris/air. */
    public StructureBuilder ruin(long seed, float chance) {
        java.util.Random r = new java.util.Random(seed);
        List<StructureBlockInfo> snapshot = new ArrayList<>(blocks);
        for (StructureBlockInfo info : snapshot) {
            if (info.state().equals(palette.wall) && r.nextFloat() < chance) {
                if (r.nextFloat() < 0.5f) {
                    setBlock(info.pos().getX(), info.pos().getY(), info.pos().getZ(), Blocks.AIR.defaultBlockState());
                } else {
                    setBlock(info.pos().getX(), info.pos().getY(), info.pos().getZ(), palette.debris);
                }
            }
        }
        return this;
    }

    /** Sprinkle palette debris on a flat area. density 0..1. */
    public StructureBuilder scatterDebris(int x1, int z1, int x2, int z2, int y, float density) {
        java.util.Random r = new java.util.Random((long) (x1 * 31 + z1 * 13 + y));
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (r.nextFloat() < density) setBlock(x, y, z, palette.debris);
            }
        }
        return this;
    }

    /** Place a single functional block (Echo Terminal, Map Table, etc.). */
    public StructureBuilder furnish(int x, int y, int z, BlockState state) {
        setBlock(x, y, z, state);
        return this;
    }

    public StructureBuilder furnish(int x, int y, int z, Block block) {
        return furnish(x, y, z, block.defaultBlockState());
    }

    /**
     * Fill a region with a block state
     */
    public StructureBuilder fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    setBlock(x, y, z, state);
                }
            }
        }
        return this;
    }

    /**
     * Fill with a block (convenience)
     */
    public StructureBuilder fill(int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        return fill(x1, y1, z1, x2, y2, z2, block.defaultBlockState());
    }

    /**
     * Create walls (hollow rectangle)
     */
    public StructureBuilder walls(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

        // Floor
        fill(minX, minY, minZ, maxX, minY, maxZ, state);
        // Ceiling
        fill(minX, maxY, minZ, maxX, maxY, maxZ, state);
        // Walls
        for (int y = minY; y <= maxY; y++) {
            // North and South walls
            fill(minX, y, minZ, maxX, y, minZ, state);
            fill(minX, y, maxZ, maxX, y, maxZ, state);
            // East and West walls
            fill(minX, y, minZ, minX, y, maxZ, state);
            fill(maxX, y, minZ, maxX, y, maxZ, state);
        }
        return this;
    }

    public StructureBuilder walls(int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        return walls(x1, y1, z1, x2, y2, z2, block.defaultBlockState());
    }

    /**
     * Create hollow box with floor, walls, ceiling
     */
    public StructureBuilder hollow(int x1, int y1, int z1, int x2, int y2, int z2, BlockState wallState) {
        walls(x1, y1, z1, x2, y2, z2, wallState);
        // Clear interior
        int minX = Math.min(x1, x2) + 1, maxX = Math.max(x1, x2) - 1;
        int minY = Math.min(y1, y2) + 1, maxY = Math.max(y1, y2) - 1;
        int minZ = Math.min(z1, z2) + 1, maxZ = Math.max(z1, z2) - 1;
        if (minX <= maxX && minY <= maxY && minZ <= maxZ) {
            fill(minX, minY, minZ, maxX, maxY, maxZ, Blocks.AIR.defaultBlockState());
        }
        return this;
    }

    /**
     * Set a single block
     */
    public StructureBuilder set(int x, int y, int z, BlockState state) {
        setBlock(x, y, z, state);
        return this;
    }

    public StructureBuilder set(int x, int y, int z, Block block) {
        return set(x, y, z, block.defaultBlockState());
    }

    /**
     * Add a chest with loot table
     */
    public StructureBuilder chest(int x, int y, int z, ResourceKey<LootTable> lootTable) {
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        CompoundTag nbt = new CompoundTag();
        nbt.putString("LootTable", lootTable.identifier().toString());
        nbt.putLong("LootTableSeed", 0L);
        setBlockWithNBT(x, y, z, chestState, nbt);
        return this;
    }

    /**
     * Add a barrel with loot table
     */
    public StructureBuilder barrel(int x, int y, int z, ResourceKey<LootTable> lootTable) {
        BlockState barrelState = Blocks.BARREL.defaultBlockState();
        CompoundTag nbt = new CompoundTag();
        nbt.putString("LootTable", lootTable.identifier().toString());
        nbt.putLong("LootTableSeed", 0L);
        setBlockWithNBT(x, y, z, barrelState, nbt);
        return this;
    }

    /**
     * Random chance to place a block (for scattered debris)
     */
    public StructureBuilder scatter(float chance, BlockState state) {
        java.util.Random random = new java.util.Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (random.nextFloat() < chance) {
                        setBlock(x, y, z, state);
                    }
                }
            }
        }
        return this;
    }

    private void setBlock(int x, int y, int z, BlockState state) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            mutablePos.set(x, y, z);
            // Remove existing block at this position
            blocks.removeIf(info -> info.pos().equals(mutablePos));
            blocks.add(new StructureBlockInfo(mutablePos.immutable(), state, null));
        }
    }

    private void setBlockWithNBT(int x, int y, int z, BlockState state, CompoundTag nbt) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            mutablePos.set(x, y, z);
            blocks.removeIf(info -> info.pos().equals(mutablePos));
            blocks.add(new StructureBlockInfo(mutablePos.immutable(), state, nbt));
        }
    }

    /**
     * Save structure directly to NBT file using vanilla format.
     * This manually constructs the NBT to match the target structure-template format.
     */
    public void save(String category, String name) {
        try {
            // Build palette and blocks list
            java.util.Map<BlockState, Integer> stateToId = new java.util.HashMap<>();
            java.util.List<BlockState> palette = new java.util.ArrayList<>();
            java.util.List<StructureBlockInfo> blockInfos = new java.util.ArrayList<>();

            // AIR is always index 0
            BlockState airState = Blocks.AIR.defaultBlockState();
            stateToId.put(airState, 0);
            palette.add(airState);

            // Assign palette IDs
            for (StructureBlockInfo info : blocks) {
                BlockState state = info.state();
                if (!stateToId.containsKey(state)) {
                    stateToId.put(state, palette.size());
                    palette.add(state);
                }
                blockInfos.add(info);
            }

            // Create NBT structure
            CompoundTag root = new CompoundTag();
            root.putInt("DataVersion", 4189); // Target structure-template data version.

            // Size - vanilla structure templates use a list of ints, not an int array.
            net.minecraft.nbt.ListTag sizeTag = new net.minecraft.nbt.ListTag();
            sizeTag.add(net.minecraft.nbt.IntTag.valueOf(width));
            sizeTag.add(net.minecraft.nbt.IntTag.valueOf(height));
            sizeTag.add(net.minecraft.nbt.IntTag.valueOf(depth));
            root.put("size", sizeTag);

            // Palette
            net.minecraft.nbt.ListTag paletteTag = new net.minecraft.nbt.ListTag();
            for (BlockState state : palette) {
                CompoundTag blockTag = new CompoundTag();
                String blockName = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                blockTag.putString("Name", blockName);

                // Add properties if present
                if (!state.getProperties().isEmpty()) {
                    CompoundTag propertiesTag = new CompoundTag();
                    for (net.minecraft.world.level.block.state.properties.Property<?> prop : state.getProperties()) {
                        propertiesTag.putString(prop.getName(), getPropertyValue(state, prop));
                    }
                    blockTag.put("Properties", propertiesTag);
                }
                paletteTag.add(blockTag);
            }
            root.put("palette", paletteTag);

            // Blocks
            net.minecraft.nbt.ListTag blocksTag = new net.minecraft.nbt.ListTag();
            for (StructureBlockInfo info : blockInfos) {
                CompoundTag blockTag = new CompoundTag();
                int stateId = stateToId.get(info.state());

                // Position
                net.minecraft.nbt.ListTag posTag = new net.minecraft.nbt.ListTag();
                posTag.add(net.minecraft.nbt.IntTag.valueOf(info.pos().getX()));
                posTag.add(net.minecraft.nbt.IntTag.valueOf(info.pos().getY()));
                posTag.add(net.minecraft.nbt.IntTag.valueOf(info.pos().getZ()));
                blockTag.put("pos", posTag);
                blockTag.putInt("state", stateId);

                // NBT data (for chests, etc.)
                if (info.nbt() != null) {
                    blockTag.put("nbt", info.nbt());
                }

                blocksTag.add(blockTag);
            }
            root.put("blocks", blocksTag);

            // Entities (empty)
            root.put("entities", new net.minecraft.nbt.ListTag());

            String relativePath = category.equals("global") ? "global" : "biomes/" + category;
            String basePath = "src/main/resources/data/echoashfallprotocol/structure";
            List<Path> outputFiles = new ArrayList<>();
            outputFiles.add(Paths.get(basePath, relativePath, name + ".nbt"));
            for (String aliasPath : ALIAS_PATHS.getOrDefault(name, List.of())) {
                outputFiles.add(Paths.get(basePath, aliasPath + ".nbt"));
            }

            for (Path outputFile : outputFiles) {
                File outputDir = outputFile.getParent().toFile();
                outputDir.mkdirs();
                NbtIo.writeCompressed(root, outputFile);
                System.out.println("Generated structure: " + outputFile + " (" + blockInfos.size() + " blocks, " + palette.size() + " states)");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save structure {}", name, e);
            throw new IllegalStateException("Failed to save structure " + name, e);
        }
    }

    private static <T extends Comparable<T>> String getPropertyValue(BlockState state, net.minecraft.world.level.block.state.properties.Property<T> prop) {
        return prop.getName(state.getValue(prop));
    }
}
