package com.knoxhack.echoarmory.content;

import java.util.List;
import net.minecraft.resources.Identifier;

public record ArmoryLoadoutDefinition(
   Identifier id,
   String title,
   int order,
   Identifier icon,
   String weapon,
   List<String> armor,
   List<String> modules,
   int minTier,
   int minProtection,
   String logisticsPreset
) {
   public ArmoryLoadoutDefinition {
      if (id == null) {
         throw new IllegalArgumentException("Loadout id is required.");
      }
      title = title == null || title.isBlank() ? id.getPath().replace('_', ' ') : title.strip();
      icon = icon == null ? Identifier.withDefaultNamespace("chest") : icon;
      weapon = weapon == null ? "" : weapon.strip();
      armor = List.copyOf(armor == null ? List.of() : armor);
      modules = List.copyOf(modules == null ? List.of() : modules);
      minTier = Math.max(1, Math.min(4, minTier));
      minProtection = Math.max(0, minProtection);
      logisticsPreset = logisticsPreset == null ? "" : logisticsPreset.strip();
   }
}
