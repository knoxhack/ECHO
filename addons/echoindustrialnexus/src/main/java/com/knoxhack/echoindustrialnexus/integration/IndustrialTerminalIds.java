package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import net.minecraft.resources.Identifier;

public final class IndustrialTerminalIds {
   public static final Identifier ECHO_TAB = Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, "industrial_nexus");
   public static final Identifier CHAPTER = Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, "industrial_nexus");

   private IndustrialTerminalIds() {
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, path);
   }
}
