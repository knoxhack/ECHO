package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echoterminal.api.TerminalArchiveEntry;
import com.knoxhack.echoterminal.api.TerminalArchiveRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import java.util.List;

public final class IndustrialTerminalCommonIntegration {
   private static boolean registered;

   private IndustrialTerminalCommonIntegration() {
   }

   public static void register() {
      if (registered) {
         return;
      }
      registered = true;
      TerminalMissionRegistry.register(IndustrialMissionProvider.INSTANCE);
      TerminalMissionActions.registerForTab(IndustrialTerminalIds.ECHO_TAB);
      registerArchive();
      EchoIndustrialNexus.LOGGER.info("ECHO Industrial Nexus terminal chapter registered.");
   }

   private static void registerArchive() {
      TerminalArchiveRegistry.register(new TerminalArchiveEntry(IndustrialTerminalIds.id("archive/thermal_flux"), "Industrial Nexus", "Thermal Flux", "RECOVERED", List.of("Recovered industrial heat-energy can be routed through Flux Ducts.", "Conversion into other ECHO energy forms remains intentionally inefficient."), false));
      TerminalArchiveRegistry.register(new TerminalArchiveEntry(IndustrialTerminalIds.id("archive/overheating"), "Industrial Nexus", "Overheating", "ACTIVE", List.of("Cool, Warm, Hot, Critical, and Meltdown states define machine risk.", "Heat sinks, coolant cells, scrubbers, and shutdown modules reduce failure chains."), false));
      TerminalArchiveRegistry.register(new TerminalArchiveEntry(IndustrialTerminalIds.id("archive/factory_control"), "Industrial Nexus", "Factory Control", "PARTIAL", List.of("Factory Controllers scan nearby machines, ducts, and Thermal Flux storage.", "Linked machines surface alerts through terminal missions and exo-suit telemetry."), false));
      TerminalArchiveRegistry.register(new TerminalArchiveEntry(IndustrialTerminalIds.id("archive/nexus_thermal_risk"), "Industrial Nexus", "Nexus Thermal Risk", "WARNING", List.of("Nexus materials in non-stabilized machines can trigger field drift.", "Use Nexus-safe ducts, stabilizer upgrades, and corruption-safe recyclers."), false));
      TerminalArchiveRegistry.register(new TerminalArchiveEntry(IndustrialTerminalIds.id("archive/furnace_warden"), "Industrial Nexus", "Furnace Warden", "HOSTILE", List.of("Industrial guardian active.", "It was not built to protect people. It was built to protect production."), false));
   }
}
