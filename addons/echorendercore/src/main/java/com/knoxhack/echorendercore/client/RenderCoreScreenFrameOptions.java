package com.knoxhack.echorendercore.client;

public record RenderCoreScreenFrameOptions(
      String label,
      boolean drawLabel,
      boolean drawScanlines,
      boolean scanlinesBehindContent,
      boolean accentBars,
      boolean quietFallback) {
   public RenderCoreScreenFrameOptions {
      label = label == null ? "" : label.strip();
   }

   public static RenderCoreScreenFrameOptions legacy(String label) {
      return new RenderCoreScreenFrameOptions(label, label != null && !label.isBlank(), true, false, true, false);
   }

   public static RenderCoreScreenFrameOptions quiet() {
      return new RenderCoreScreenFrameOptions("", false, false, true, false, true);
   }
}
