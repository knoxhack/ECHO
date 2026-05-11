package com.knoxhack.echorendercore.profile;

import java.util.Map;
import net.minecraft.resources.Identifier;

public final class RenderCoreProfiles {
   private static volatile LoadedContent loaded = LoadedContent.EMPTY;

   private RenderCoreProfiles() {
   }

   public static LoadedContent loaded() {
      return loaded;
   }

   public static void replace(LoadedContent content) {
      loaded = content == null ? LoadedContent.EMPTY : content;
   }

   public static VisualProfile visual(Identifier id) {
      return id == null ? null : loaded.visualProfiles().get(id);
   }

   public static AnimationProfile animation(Identifier id) {
      return id == null ? null : loaded.animationProfiles().get(id);
   }

   public static ParticleProfile particle(Identifier id) {
      return id == null ? null : loaded.particleProfiles().get(id);
   }

   public record LoadedContent(
      Map<Identifier, VisualProfile> visualProfiles,
      Map<Identifier, AnimationProfile> animationProfiles,
      Map<Identifier, ParticleProfile> particleProfiles,
      ProfileValidationReport validationReport,
      ProfilePerformanceReport performanceReport,
      ProfileCacheMetrics cacheMetrics,
      ProfileDiagnosticsReport diagnosticsReport,
      int discovered,
      int loaded,
      int failed
   ) {
      public static final LoadedContent EMPTY = new LoadedContent(
         Map.of(),
         Map.of(),
         Map.of(),
         ProfileValidationReport.EMPTY,
         ProfilePerformanceReport.EMPTY,
         ProfileCacheMetrics.EMPTY,
         ProfileDiagnosticsReport.EMPTY,
         0,
         0,
         0
      );

      public LoadedContent {
         visualProfiles = visualProfiles == null ? Map.of() : Map.copyOf(visualProfiles);
         animationProfiles = animationProfiles == null ? Map.of() : Map.copyOf(animationProfiles);
         particleProfiles = particleProfiles == null ? Map.of() : Map.copyOf(particleProfiles);
         validationReport = validationReport == null ? ProfileValidationReport.EMPTY : validationReport;
         performanceReport = performanceReport == null ? ProfilePerformanceReport.EMPTY : performanceReport;
         cacheMetrics = cacheMetrics == null ? ProfileCacheMetrics.EMPTY : cacheMetrics;
         diagnosticsReport = diagnosticsReport == null ? new ProfileDiagnosticsReport(validationReport, performanceReport, cacheMetrics) : diagnosticsReport;
         discovered = Math.max(0, discovered);
         loaded = Math.max(0, loaded);
         failed = Math.max(0, failed);
      }
   }
}
