package com.knoxhack.echoblackboxprotocol.world;

import com.knoxhack.echoblackboxprotocol.progression.BlackboxDungeon;
import com.knoxhack.echoblackboxprotocol.registry.ModBlocks;
import com.knoxhack.echoblackboxprotocol.registry.ModWorldgen;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.Blender;

public class BlackboxDungeonGenerator extends ChunkGenerator {
   public static final MapCodec<BlackboxDungeonGenerator> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            BlackboxDungeon.CODEC.fieldOf("dungeon").forGetter(BlackboxDungeonGenerator::dungeon),
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource)
         )
         .apply(instance, BlackboxDungeonGenerator::new)
   );
   private static final int BASE_Y = 70;
   private final BlackboxDungeon dungeon;

   public BlackboxDungeonGenerator(BlackboxDungeon dungeon, BiomeSource biomeSource) {
      super(biomeSource);
      this.dungeon = dungeon;
   }

   public BlackboxDungeon dungeon() {
      return this.dungeon;
   }

   protected MapCodec<? extends ChunkGenerator> codec() {
      return (MapCodec<? extends ChunkGenerator>)ModWorldgen.BLACKBOX_DUNGEON.get();
   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
      ChunkPos chunkPos = chunk.getPos();
      Heightmap ocean = chunk.getOrCreateHeightmapUnprimed(Types.OCEAN_FLOOR_WG);
      Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Types.WORLD_SURFACE_WG);
      MutableBlockPos pos = new MutableBlockPos();

      for (int localX = 0; localX < 16; localX++) {
         for (int localZ = 0; localZ < 16; localZ++) {
            int worldX = chunkPos.getBlockX(localX);
            int worldZ = chunkPos.getBlockZ(localZ);
            int top = topHeight(this.dungeon, worldX, worldZ);

            for (int y = 70; y <= top; y++) {
               BlockState state = stateFor(this.dungeon, worldX, y, worldZ, top);
               pos.set(localX, y, localZ);
               chunk.setBlockState(pos, state);
               ocean.update(localX, y, localZ, state);
               surface.update(localX, y, localZ, state);
            }

            if (isMemoryPillar(this.dungeon, worldX, worldZ)) {
               pos.set(localX, top + 1, localZ);
               chunk.setBlockState(pos, ((Block)ModBlocks.SIGNAL_GLASS.get()).defaultBlockState());
            }
         }
      }

      return CompletableFuture.completedFuture(chunk);
   }

   public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
   }

   public void applyCarvers(
      WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk
   ) {
   }

   public void spawnOriginalMobs(WorldGenRegion region) {
   }

   public int getGenDepth() {
      return 256;
   }

   public int getMinY() {
      return 0;
   }

   public int getSeaLevel() {
      return -63;
   }

   public int getSpawnHeight(LevelHeightAccessor level) {
      return 96;
   }

   public int getBaseHeight(int x, int z, Types type, LevelHeightAccessor level, RandomState random) {
      return Math.max(level.getMinY(), topHeight(this.dungeon, x, z) + 1);
   }

   public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
      BlockState[] states = new BlockState[level.getHeight()];
      int top = topHeight(this.dungeon, x, z);

      for (int y = 70; y <= top && y - level.getMinY() < states.length; y++) {
         int index = y - level.getMinY();
         if (index >= 0) {
            states[index] = stateFor(this.dungeon, x, y, z, top);
         }
      }

      for (int i = 0; i < states.length; i++) {
         if (states[i] == null) {
            states[i] = Blocks.AIR.defaultBlockState();
         }
      }

      return new NoiseColumn(level.getMinY(), states);
   }

   public void addDebugScreenInfo(List<String> lines, RandomState random, BlockPos pos) {
      lines.add("ECHO-7 blackbox dungeon: " + this.dungeon.getSerializedName());
   }

   public static int topHeight(BlackboxDungeon dungeon, int x, int z) {
      int wave = Math.floorMod(x * 31 + z * 17 + x * z, 9) - 4;
      int radius = Math.abs(Math.floorMod(x, 64) - 32) + Math.abs(Math.floorMod(z, 64) - 32);

      return switch (dungeon) {
         case VAULT -> 76 + wave + (radius % 9 == 0 ? 2 : 0);
         case BUNKER -> 74 + (Math.floorMod(x, 11) != 0 && Math.floorMod(z, 11) != 0 ? 0 : 3);
         case LABYRINTH -> 72 + wave + (Math.floorMod(x + z, 13) < 3 ? 5 : 0);
         case TEMPLE -> 78 + (Math.abs(Math.floorMod(x - z, 23) - 11) < 2 ? 4 : 0);
         case CORE_CHAMBER -> 82 + wave + (radius < 22 ? 5 : 0);
      };
   }

   private static BlockState stateFor(BlackboxDungeon dungeon, int x, int y, int z, int top) {
      boolean surface = y == top;

      return switch (dungeon) {
         case VAULT -> surface ? ((Block)ModBlocks.BLACK_METAL_BLOCK.get()).defaultBlockState() : ((Block)ModBlocks.CORE_BRICK.get()).defaultBlockState();
         case BUNKER -> Math.floorMod(x + z + y, 7) == 0 ? Blocks.RED_CONCRETE.defaultBlockState() : ((Block)ModBlocks.CORE_BRICK.get()).defaultBlockState();
         case LABYRINTH -> Math.floorMod(x * x + z * z + y, 11) < 3
            ? ((Block)ModBlocks.SIGNAL_GLASS.get()).defaultBlockState()
            : ((Block)ModBlocks.CORRUPTED_FERRITE_BLOCK.get()).defaultBlockState();
         case TEMPLE -> surface ? ((Block)ModBlocks.SIGNAL_GLASS.get()).defaultBlockState() : ((Block)ModBlocks.BLACK_METAL_BLOCK.get()).defaultBlockState();
         case CORE_CHAMBER -> Math.floorMod(x + y + z, 5) == 0
            ? ((Block)ModBlocks.SIGNAL_GLASS.get()).defaultBlockState()
            : ((Block)ModBlocks.BLACK_METAL_BLOCK.get()).defaultBlockState();
      };
   }

   private static boolean isMemoryPillar(BlackboxDungeon dungeon, int x, int z) {
      return Math.floorMod(x * 19 + z * 23 + dungeon.ordinal() * 7, 97) == 0;
   }
}
