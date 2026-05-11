package com.knoxhack.echorendercore.profile;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class RenderCoreProfileDataProvider implements DataProvider {
   private final PackOutput.PathProvider visualProfiles;
   private final PackOutput.PathProvider animationProfiles;
   private final PackOutput.PathProvider particleProfiles;
   private final Map<Identifier, JsonObject> visuals = new LinkedHashMap<>();
   private final Map<Identifier, JsonObject> animations = new LinkedHashMap<>();
   private final Map<Identifier, JsonObject> particles = new LinkedHashMap<>();
   private final String name;

   protected RenderCoreProfileDataProvider(PackOutput output, String name) {
      this.name = name == null || name.isBlank() ? "RenderCore Profiles" : name;
      this.visualProfiles = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "rendercore/visual_profiles");
      this.animationProfiles = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "rendercore/animations");
      this.particleProfiles = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "rendercore/particles");
   }

   @Override
   public final CompletableFuture<?> run(CachedOutput output) {
      visuals.clear();
      animations.clear();
      particles.clear();
      registerProfiles();
      java.util.ArrayList<CompletableFuture<?>> futures = new java.util.ArrayList<>();
      visuals.forEach((id, json) -> futures.add(DataProvider.saveStable(output, json, visualProfiles.json(id))));
      animations.forEach((id, json) -> futures.add(DataProvider.saveStable(output, json, animationProfiles.json(id))));
      particles.forEach((id, json) -> futures.add(DataProvider.saveStable(output, json, particleProfiles.json(id))));
      return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
   }

   @Override
   public String getName() {
      return name;
   }

   protected abstract void registerProfiles();

   protected void visual(VisualProfileBuilder builder) {
      visuals.put(builder.id(), builder.toJson());
   }

   protected void animation(AnimationProfileBuilder builder) {
      animations.put(builder.id(), builder.toJson());
   }

   protected void particle(ParticleProfileBuilder builder) {
      particles.put(builder.id(), builder.toJson());
   }
}
