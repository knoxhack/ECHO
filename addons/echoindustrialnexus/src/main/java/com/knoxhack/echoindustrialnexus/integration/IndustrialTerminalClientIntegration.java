package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echoindustrialnexus.progress.IndustrialProgress;
import com.knoxhack.echoterminal.api.TerminalAddonGuide;
import com.knoxhack.echoterminal.api.TerminalAddonInfo;
import com.knoxhack.echoterminal.api.TerminalAddonInfoProvider;
import com.knoxhack.echoterminal.api.TerminalAddonInfoRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonLink;
import com.knoxhack.echoterminal.api.TerminalAddonMetric;
import com.knoxhack.echoterminal.api.TerminalAddonSection;
import com.knoxhack.echoterminal.api.TerminalNavigationProfile;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.client.mission.TerminalMissionBrowser;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.entity.player.Player;

public final class IndustrialTerminalClientIntegration {
   private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
   private static final int ACCENT = 0xFFFF9F3D;

   private IndustrialTerminalClientIntegration() {
   }

   public static void register() {
      if (!REGISTERED.compareAndSet(false, true)) {
         return;
      }
      TerminalTab tab = new IndustrialNexusTab();
      TerminalTabRegistry.register(tab);
      TerminalNavigationProfiles.register(
         tab.descriptor().id(),
         TerminalNavigationProfile.chapter("industrial", "Optional: Industrial Nexus", "OP", 70)
      );
      TerminalAddonInfoRegistry.register(new IndustrialAddonInfoProvider());
   }

   private static final class IndustrialAddonInfoProvider implements TerminalAddonInfoProvider {
      @Override
      public String chapterId() {
         return IndustrialCoreIntegration.CHAPTER_ID;
      }

      @Override
      public TerminalAddonInfo info(Player player) {
         if (player == null) {
            return new TerminalAddonInfo(
               "Factory recovery, scrubber support, Thermal Flux, and Furnace Warden production survival.",
               List.of(new TerminalAddonMetric("Signal", "OFFLINE", "waiting for player telemetry", ACCENT)),
               List.of(new TerminalAddonSection("Factory Feed",
                  List.of("Open Industrial Nexus after player telemetry is available."))),
               links(),
               guide());
         }
         int flux = IndustrialProgress.value(player, "thermal_flux_generated");
         int machines = IndustrialProgress.value(player, "machines");
         int ducts = IndustrialProgress.value(player, "item_ducts") + IndustrialProgress.value(player, "flux_ducts");
         boolean safeZone = IndustrialProgress.flag(player, "safe_zone");
         boolean wardenDefeated = IndustrialProgress.flag(player, "furnace_warden_defeated");
         return new TerminalAddonInfo(
            "Factory recovery, scrubber support, Thermal Flux, and Furnace Warden production survival.",
            List.of(
               new TerminalAddonMetric("Thermal Flux", String.valueOf(flux), "generated TF", ACCENT),
               new TerminalAddonMetric("Machines", String.valueOf(machines), "scanned machines", TerminalUi.CYAN),
               new TerminalAddonMetric("Ducts", String.valueOf(ducts), "item and flux network", TerminalUi.GREEN),
               new TerminalAddonMetric("Safe Zone", safeZone ? "ONLINE" : "PENDING",
                  "industrial scrubber support", safeZone ? TerminalUi.GREEN : TerminalUi.AMBER)),
            List.of(new TerminalAddonSection("Factory Feed", List.of(
               "Factory scan: " + (IndustrialProgress.flag(player, "factory_scanned") ? "recorded" : "pending"),
               "Controllers: " + IndustrialProgress.value(player, "controllers"),
               "Hot machines: " + IndustrialProgress.value(player, "hot_machines"),
               "Furnace Warden: " + (wardenDefeated ? "defeated" : "active")))),
            links(),
            guide());
      }

      private static TerminalAddonGuide guide() {
         return TerminalAddonGuide.optional(600, "Side route",
            "Industrial Nexus is optional factory progression; start it when your base can support heat, power, and machine recovery.",
            List.of(
               "Open Industrial Nexus and scan factory route telemetry.",
               "Build toward scrubbers and safe-zone support.",
               "Treat Furnace Warden progress as production survival, not a main saga gate."));
      }

      private static List<TerminalAddonLink> links() {
         return List.of(new TerminalAddonLink(IndustrialTerminalIds.ECHO_TAB,
            "Industrial Nexus", "Factory route telemetry", ACCENT));
      }
   }

   private static final class IndustrialNexusTab implements TerminalTab {
      private final TerminalTabDescriptor descriptor =
         new TerminalTabDescriptor(IndustrialTerminalIds.ECHO_TAB, "INDUSTRIAL NEXUS", 70, ACCENT);
      private final TerminalTabChrome chrome =
         TerminalTabChrome.of("Industrial Nexus", TerminalTabChrome.GROUP_ADDONS, "IN", "Factory route telemetry", 70);
      private final TerminalMissionBrowser browser =
         new TerminalMissionBrowser(IndustrialMissionProvider.INSTANCE, descriptor.id(), true);

      public TerminalTabDescriptor descriptor() {
         return descriptor;
      }

      public TerminalTabChrome chrome() {
         return chrome;
      }

      public void onSelected(TerminalRenderContext context) {
         browser.onSelected(context);
      }

      public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
         browser.render(context, graphics, mouseX, mouseY, partialTick);
      }

      public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
         return browser.mouseClicked(context, mouseX, mouseY, button);
      }

      public boolean mouseScrolled(TerminalRenderContext context, double mouseX, double mouseY, double delta) {
         return browser.mouseScrolled(context, mouseX, mouseY, delta);
      }

      public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
         return browser.keyPressed(context, event);
      }

      public boolean charTyped(TerminalRenderContext context, CharacterEvent event) {
         return browser.charTyped(context, event);
      }

      public int contentHeight(TerminalRenderContext context) {
         return browser.contentHeight(context);
      }
   }
}
