package com.knoxhack.echorendercore.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knoxhack.echorendercore.EchoRenderCore;
import com.knoxhack.echorendercore.profile.AnimationProfile;
import com.knoxhack.echorendercore.profile.ParticleProfile;
import com.knoxhack.echorendercore.profile.ProfileDiagnosticsReport;
import com.knoxhack.echorendercore.profile.RenderCoreJsonParsers;
import com.knoxhack.echorendercore.profile.RenderCoreProfileValidator;
import com.knoxhack.echorendercore.profile.RenderCoreProfiles;
import com.knoxhack.echorendercore.profile.VisualProfile;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class RenderCoreClientReloadListener extends SimplePreparableReloadListener<RenderCoreProfiles.LoadedContent> {
   private static final String VISUAL_DIR = "rendercore/visual_profiles";
   private static final String ANIMATION_DIR = "rendercore/animations";
   private static final String PARTICLE_DIR = "rendercore/particles";

   @Override
   protected RenderCoreProfiles.LoadedContent prepare(ResourceManager manager, ProfilerFiller profiler) {
      LoadCounter counter = new LoadCounter();
      Map<Identifier, VisualProfile> visuals = loadVisualProfiles(manager, counter);
      Map<Identifier, AnimationProfile> animations = loadAnimationProfiles(manager, counter);
      Map<Identifier, ParticleProfile> particles = loadParticleProfiles(manager, counter);
      ProfileDiagnosticsReport diagnostics = RenderCoreProfileValidator.diagnostics(visuals, animations, particles,
         counter.discovered, counter.loaded, counter.failed);
      return new RenderCoreProfiles.LoadedContent(
         visuals,
         animations,
         particles,
         diagnostics.validationReport(),
         diagnostics.performanceReport(),
         diagnostics.cacheMetrics(),
         diagnostics,
         counter.discovered,
         counter.loaded,
         counter.failed
      );
   }

   @Override
   protected void apply(RenderCoreProfiles.LoadedContent loaded, ResourceManager manager, ProfilerFiller profiler) {
      RenderCoreProfiles.replace(loaded);
      RenderCoreWarnings.clear();
      EchoRenderCore.LOGGER.info(
         "Loaded {} RenderCore profiles across {} discovered JSON files ({} failed, {} validation warnings, {} validation errors, {} performance warnings).",
         loaded.loaded(),
         loaded.discovered(),
         loaded.failed(),
         loaded.validationReport().warnings(),
         loaded.validationReport().errors(),
         loaded.performanceReport().warningCount()
      );
      for (var issue : loaded.validationReport().issues()) {
         EchoRenderCore.LOGGER.warn("RenderCore validation {} {} {} [{}]: {}{}",
            issue.severity(), issue.code(), issue.profileId(), issue.path(), issue.message(),
            issue.suggestion().isBlank() ? "" : " Suggestion: " + issue.suggestion());
      }
   }

   private static Map<Identifier, VisualProfile> loadVisualProfiles(ResourceManager manager, LoadCounter counter) {
      Map<Identifier, VisualProfile> profiles = new LinkedHashMap<>();
      for (Map.Entry<Identifier, Resource> entry : jsonResources(manager, VISUAL_DIR).entrySet()) {
         Identifier resourceId = entry.getKey();
         Identifier id = contentId(resourceId, VISUAL_DIR);
         counter.discovered++;
         try {
            VisualProfile profile = RenderCoreJsonParsers.parseVisualProfile(id, readObject(entry.getValue()));
            if (profiles.putIfAbsent(id, profile) == null) {
               counter.loaded++;
            } else {
               EchoRenderCore.LOGGER.warn("Duplicate RenderCore visual profile id {} from {} ignored.", id, resourceId);
            }
         } catch (IOException | RuntimeException exception) {
            counter.failed++;
            EchoRenderCore.LOGGER.warn("Could not parse RenderCore visual profile {}: {}", resourceId, exception.getMessage());
         }
      }
      return profiles;
   }

   private static Map<Identifier, AnimationProfile> loadAnimationProfiles(ResourceManager manager, LoadCounter counter) {
      Map<Identifier, AnimationProfile> profiles = new LinkedHashMap<>();
      for (Map.Entry<Identifier, Resource> entry : jsonResources(manager, ANIMATION_DIR).entrySet()) {
         Identifier resourceId = entry.getKey();
         Identifier id = contentId(resourceId, ANIMATION_DIR);
         counter.discovered++;
         try {
            AnimationProfile profile = RenderCoreJsonParsers.parseAnimationProfile(id, readObject(entry.getValue()));
            if (profiles.putIfAbsent(id, profile) == null) {
               counter.loaded++;
            } else {
               EchoRenderCore.LOGGER.warn("Duplicate RenderCore animation profile id {} from {} ignored.", id, resourceId);
            }
         } catch (IOException | RuntimeException exception) {
            counter.failed++;
            EchoRenderCore.LOGGER.warn("Could not parse RenderCore animation profile {}: {}", resourceId, exception.getMessage());
         }
      }
      return profiles;
   }

   private static Map<Identifier, ParticleProfile> loadParticleProfiles(ResourceManager manager, LoadCounter counter) {
      Map<Identifier, ParticleProfile> profiles = new LinkedHashMap<>();
      for (Map.Entry<Identifier, Resource> entry : jsonResources(manager, PARTICLE_DIR).entrySet()) {
         Identifier resourceId = entry.getKey();
         Identifier id = contentId(resourceId, PARTICLE_DIR);
         counter.discovered++;
         try {
            ParticleProfile profile = RenderCoreJsonParsers.parseParticleProfile(id, readObject(entry.getValue()));
            if (profiles.putIfAbsent(id, profile) == null) {
               counter.loaded++;
            } else {
               EchoRenderCore.LOGGER.warn("Duplicate RenderCore particle profile id {} from {} ignored.", id, resourceId);
            }
         } catch (IOException | RuntimeException exception) {
            counter.failed++;
            EchoRenderCore.LOGGER.warn("Could not parse RenderCore particle profile {}: {}", resourceId, exception.getMessage());
         }
      }
      return profiles;
   }

   private static Map<Identifier, Resource> jsonResources(ResourceManager manager, String directory) {
      return manager.listResources(directory, id -> id.getPath().endsWith(".json"));
   }

   private static JsonObject readObject(Resource resource) throws IOException {
      try (Reader reader = resource.openAsReader()) {
         JsonElement root = JsonParser.parseReader(reader);
         if (!root.isJsonObject()) {
            throw new JsonParseException("Root must be a JSON object.");
         }
         return root.getAsJsonObject();
      }
   }

   private static Identifier contentId(Identifier resourceId, String directory) {
      String path = resourceId.getPath();
      String prefix = directory + "/";
      if (path.startsWith(prefix)) {
         path = path.substring(prefix.length());
      }
      if (path.endsWith(".json")) {
         path = path.substring(0, path.length() - ".json".length());
      }
      return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), path);
   }

   private static final class LoadCounter {
      private int discovered;
      private int loaded;
      private int failed;
   }
}
