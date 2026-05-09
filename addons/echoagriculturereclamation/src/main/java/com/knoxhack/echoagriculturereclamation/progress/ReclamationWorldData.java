package com.knoxhack.echoagriculturereclamation.progress;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class ReclamationWorldData extends SavedData {
   public static final Codec<ReclamationWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("restorationScores", Map.of()).forGetter(data -> data.restorationScores),
      Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("greenhouseSafety", Map.of()).forGetter(data -> data.greenhouseSafety),
      Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("lastSoilStates", Map.of()).forGetter(data -> data.lastSoilStates),
      Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("worldStats", Map.of()).forGetter(data -> data.worldStats)
   ).apply(instance, (restorationScores, greenhouseSafety, lastSoilStates, worldStats) -> {
      ReclamationWorldData data = new ReclamationWorldData();
      data.restorationScores.putAll(restorationScores);
      data.greenhouseSafety.putAll(greenhouseSafety);
      data.lastSoilStates.putAll(lastSoilStates);
      data.worldStats.putAll(worldStats);
      return data;
   }));

   public static final SavedDataType<ReclamationWorldData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath(EchoAgricultureReclamation.MODID, "reclamation_world_data"), ReclamationWorldData::new, CODEC
   );

   private final Map<String, Integer> restorationScores = new LinkedHashMap<>();
   private final Map<String, Integer> greenhouseSafety = new LinkedHashMap<>();
   private final Map<String, String> lastSoilStates = new LinkedHashMap<>();
   private final Map<String, Integer> worldStats = new LinkedHashMap<>();

   public static ReclamationWorldData get(ServerLevel level) {
      return level.getDataStorage().computeIfAbsent(TYPE);
   }

   public int restorationScore(ChunkPos chunk) {
      return Math.max(0, Math.min(100, restorationScores.getOrDefault(key(chunk), 0)));
   }

   public int addRestoration(ChunkPos chunk, int amount) {
      if (amount <= 0) {
         return restorationScore(chunk);
      }
      int next = Math.min(100, restorationScore(chunk) + amount);
      restorationScores.put(key(chunk), next);
      worldStats.put("restoration_events", stat("restoration_events") + 1);
      setDirty();
      return next;
   }

   public void setRestorationScore(ChunkPos chunk, int score) {
      restorationScores.put(key(chunk), Math.max(0, Math.min(100, score)));
      setDirty();
   }

   public int greenhouseSafety(ChunkPos chunk) {
      return Math.max(0, Math.min(100, greenhouseSafety.getOrDefault(key(chunk), 0)));
   }

   public void setGreenhouseSafety(ChunkPos chunk, int safety) {
      greenhouseSafety.put(key(chunk), Math.max(0, Math.min(100, safety)));
      setDirty();
   }

   public String lastSoilState(ChunkPos chunk) {
      return lastSoilStates.getOrDefault(key(chunk), "Dead Soil");
   }

   public void setLastSoilState(ChunkPos chunk, String state) {
      lastSoilStates.put(key(chunk), state == null || state.isBlank() ? "Dead Soil" : state);
      setDirty();
   }

   public int stat(String key) {
      return Math.max(0, worldStats.getOrDefault(key, 0));
   }

   public void addStat(String key, int amount) {
      if (amount <= 0) {
         return;
      }
      worldStats.put(key, Math.min(2_000_000_000, stat(key) + amount));
      setDirty();
   }

   private static String key(ChunkPos chunk) {
      return chunk.x() + "," + chunk.z();
   }
}
