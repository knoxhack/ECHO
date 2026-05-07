package com.knoxhack.echoblackboxprotocol.integration;

import com.knoxhack.echoblackboxprotocol.progression.BlackboxDungeon;
import com.knoxhack.echoblackboxprotocol.progression.BlackboxEnding;
import com.knoxhack.echoblackboxprotocol.progression.BlackboxProgress;
import com.knoxhack.echoblackboxprotocol.progression.MemoryType;
import com.knoxhack.echoblackboxprotocol.registry.ModBlocks;
import com.knoxhack.echoblackboxprotocol.registry.ModItems;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRequirement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionReward;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class BlackboxMissionProvider implements TerminalMissionProvider {
   public static final BlackboxMissionProvider INSTANCE = new BlackboxMissionProvider();
   private static final String ACTION_CLAIM_CACHE = "claim_cache";
   private static final int ACCENT = -8726529;

   private BlackboxMissionProvider() {
   }

   public TerminalMissionChapter chapter() {
      return new TerminalMissionChapter(
         BlackboxTerminalIds.CHAPTER_ID,
         "BLACKBOX PROTOCOL",
         "Final chapter records for hostile memory reconstruction, monolith dungeons, Core keys, and endings.",
         420,
         -8726529,
         true
      );
   }

   public List<TerminalMissionDefinition> missions(Player player) {
      BlackboxProgress progress = BlackboxProgress.get(player);
      return List.of(BlackboxMissionProvider.Mission.values()).stream().map(mission -> definition(progress, mission)).toList();
   }

   public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
      BlackboxMissionProvider.Mission mission = mission(missionId);
      if (mission == null) {
         return new TerminalMissionSnapshot(
            missionId, TerminalMissionStatus.LOCKED, 0.0F, "UNKNOWN", "Blackbox mission signal missing.", "No Blackbox route record is available.", List.of()
         );
      } else {
         BlackboxProgress progress = BlackboxProgress.get(player);
         boolean available = mission.available(progress);
         boolean complete = mission.complete(progress);
         boolean claimed = progress.hasTerminalCacheClaimed(mission.path);
         TerminalMissionStatus status = !available
            ? TerminalMissionStatus.LOCKED
            : (complete ? (claimed ? TerminalMissionStatus.CLAIMED : TerminalMissionStatus.CLAIMABLE) : TerminalMissionStatus.UNLOCKED);
         List<TerminalMissionAction> actions = !complete
            ? List.of()
            : List.of(
               claimed
                  ? TerminalMissionAction.disabled("claim_cache", "CLAIM CACHE", "Blackbox cache already claimed.")
                  : TerminalMissionAction.enabled("claim_cache", "CLAIM CACHE")
            );
         return new TerminalMissionSnapshot(
            mission.id(),
            status,
            mission.progress(progress),
            statusLabel(status),
            available ? "" : mission.lockReason(progress),
            mission.actionHint(progress),
            actions
         );
      }
   }

   public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
      BlackboxMissionProvider.Mission mission = mission(missionId);
      if (mission != null && "claim_cache".equals(actionId)) {
         BlackboxProgress progress = BlackboxProgress.get(player);
         if (!mission.complete(progress)) {
            player.sendSystemMessage(Component.literal("ECHO-7 // Blackbox cache locked. Complete the route record first."), true);
            return true;
         } else if (!progress.markTerminalCacheClaimed(mission.path)) {
            player.sendSystemMessage(Component.literal("ECHO-7 // Blackbox cache already claimed."), true);
            return true;
         } else {
            List<ItemStack> rewards = mission.rewards();
            if (!EchoCoreServices.storeTerminalRewards(player, mission.id().toString(), rewards)) {
               for (ItemStack reward : rewards) {
                  if (!player.getInventory().add(reward.copy())) {
                     player.drop(reward.copy(), false);
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private static TerminalMissionDefinition definition(BlackboxProgress progress, BlackboxMissionProvider.Mission mission) {
      return new TerminalMissionDefinition(
         mission.id(),
         BlackboxTerminalIds.CHAPTER_ID,
         mission.phaseId(),
         mission.phaseTitle,
         mission.phaseOrder,
         mission.order,
         mission.title,
         mission.briefing,
         mission.guide,
         mission.category,
         mission.difficulty,
         mission.icon(),
         mission.previous() == null ? List.of() : List.of(mission.previous().title),
         mission.requirements(progress),
         mission.rewards().stream().map(TerminalMissionReward::of).toList()
      );
   }

   private static BlackboxMissionProvider.Mission mission(Identifier id) {
      if (id == null) {
         return null;
      } else {
         for (BlackboxMissionProvider.Mission mission : BlackboxMissionProvider.Mission.values()) {
            if (mission.id().equals(id)) {
               return mission;
            }
         }

         return null;
      }
   }

   private static String statusLabel(TerminalMissionStatus status) {
      return switch (status) {
         case LOCKED -> "LOCKED";
         case UNLOCKED -> "ACTIVE";
         case COMPLETED -> "COMPLETE";
         case CLAIMABLE -> "CACHE READY";
         case CLAIMED -> "CLAIMED";
         case VIEW_ONLY -> "VIEW";
         default -> throw new MatchException(null, null);
      };
   }

   private static enum Mission {
      DECODE(
         "decode_memories",
         "ACCESS",
         0,
         0,
         "Decode the Blackbox",
         "Recover typed Blackbox Fragments and reconstruct the first archive layer.",
         "Use the Blackbox Decoder on Personal, Security, ECHO, Command, Core, and Deleted fragments.",
         "Memory",
         "Late Game"
      ),
      VAULT(
         "blackbox_vault",
         "DUNGEONS",
         1,
         0,
         "Blackbox Vault",
         "Open the underground archive after Personal and Security evidence stabilizes.",
         "Use the Vault Monolith once Personal and Security memories are decoded.",
         "Dungeon",
         "Dangerous"
      ),
      BOSSES(
         "memory_bosses",
         "DUNGEONS",
         1,
         1,
         "False ECHO and Command Remnant",
         "Defeat the memory copy and the command AI guarding final key proof.",
         "Clear Labyrinth and Temple pressure until both boss flags are recorded.",
         "Boss",
         "Endgame"
      ),
      KEY(
         "core_key",
         "CORE",
         2,
         0,
         "Assemble Core Access",
         "Use boss proof, Core Logs, and the Core Key Assembler to produce the Nexus Core Access Key.",
         "Bring ECHO Identity Fragment, Command Key, Protocol Extractor Schematic, and Core Logs.",
         "Crafting",
         "Endgame"
      ),
      GUARDIAN(
         "nexus_guardian",
         "CORE",
         2,
         1,
         "Nexus Guardian",
         "Enter the Nexus Core Chamber and defeat the final protector of the Core.",
         "Use the Core Chamber Monolith with a Nexus Core Access Key, then survive the Guardian phases.",
         "Boss",
         "Final"
      ),
      ENDING(
         "truth_engine",
         "TRUTH",
         3,
         0,
         "Truth Engine Choice",
         "Commit Restore, Control, Destroy, or secret Merge to change the world state.",
         "Use the Truth Engine with an ending directive. Merge requires Deleted Logs and all boss proof.",
         "Ending",
         "Final"
      );

      private final String path;
      private final String phaseTitle;
      private final int phaseOrder;
      private final int order;
      private final String title;
      private final String briefing;
      private final String guide;
      private final String category;
      private final String difficulty;

      private Mission(
         String path, String phaseTitle, int phaseOrder, int order, String title, String briefing, String guide, String category, String difficulty
      ) {
         this.path = path;
         this.phaseTitle = phaseTitle;
         this.phaseOrder = phaseOrder;
         this.order = order;
         this.title = title;
         this.briefing = briefing;
         this.guide = guide;
         this.category = category;
         this.difficulty = difficulty;
      }

      Identifier id() {
         return BlackboxTerminalIds.id(this.path);
      }

      String phaseId() {
         return this.phaseTitle.toLowerCase(Locale.ROOT);
      }

      BlackboxMissionProvider.Mission previous() {
         int i = this.ordinal() - 1;
         return i < 0 ? null : values()[i];
      }

      boolean available(BlackboxProgress progress) {
         BlackboxMissionProvider.Mission previous = this.previous();
         return previous == null || previous.complete(progress);
      }

      boolean complete(BlackboxProgress progress) {
         return switch (this) {
            case DECODE -> progress.decodedMemoryTotal() >= 6;
            case VAULT -> progress.completed(BlackboxDungeon.VAULT);
            case BOSSES -> progress.bossDefeated("false_echo") && progress.bossDefeated("command_remnant");
            case KEY -> progress.hasNexusCoreAccessKey();
            case GUARDIAN -> progress.bossDefeated("nexus_guardian");
            case ENDING -> progress.ending() != BlackboxEnding.NONE;
         };
      }

      float progress(BlackboxProgress progress) {
         if (this.complete(progress)) {
            return 1.0F;
         } else {
            return switch (this) {
               case DECODE -> Math.min(1.0F, progress.decodedMemoryTotal() / 6.0F);
               case VAULT -> progress.canEnter(BlackboxDungeon.VAULT) ? 0.5F : 0.0F;
               case BOSSES -> ((progress.bossDefeated("false_echo") ? 1.0F : 0.0F) + (progress.bossDefeated("command_remnant") ? 1.0F : 0.0F)) / 2.0F;
               case KEY -> progress.hasMemory(MemoryType.CORE, 2) ? 0.5F : 0.0F;
               case GUARDIAN -> progress.canEnter(BlackboxDungeon.CORE_CHAMBER) ? 0.5F : 0.0F;
               case ENDING -> 0.0F;
            };
         }
      }

      String lockReason(BlackboxProgress progress) {
         BlackboxMissionProvider.Mission previous = this.previous();
         return previous == null ? "Blackbox signal unavailable." : "Complete " + previous.title + " first.";
      }

      String actionHint(BlackboxProgress progress) {
         return this.complete(progress) ? "Route record complete. Claim optional support cache." : this.guide;
      }

      List<TerminalMissionRequirement> requirements(BlackboxProgress progress) {
         return switch (this) {
            case DECODE -> List.of(
               requirement(
                  "Decoded memories",
                  "Any typed Blackbox memory records.",
                  new ItemStack((ItemLike)ModItems.PERSONAL_BLACKBOX_FRAGMENT.get()),
                  progress.decodedMemoryTotal(),
                  6
               )
            );
            case VAULT -> List.of(
               requirement(
                  "Personal Logs",
                  "Personal memory route proof.",
                  new ItemStack((ItemLike)ModItems.PERSONAL_MEMORY_RECORD.get()),
                  progress.memoryCount(MemoryType.PERSONAL),
                  2
               ),
               requirement(
                  "Security Logs",
                  "Security memory route proof.",
                  new ItemStack((ItemLike)ModItems.SECURITY_MEMORY_RECORD.get()),
                  progress.memoryCount(MemoryType.SECURITY),
                  2
               )
            );
            case BOSSES -> List.of(
               requirement(
                  "False ECHO",
                  "ECHO Identity Fragment proof.",
                  new ItemStack((ItemLike)ModItems.ECHO_IDENTITY_FRAGMENT.get()),
                  progress.bossDefeated("false_echo") ? 1 : 0,
                  1
               ),
               requirement(
                  "Command Remnant",
                  "Command Key proof.",
                  new ItemStack((ItemLike)ModItems.COMMAND_KEY.get()),
                  progress.bossDefeated("command_remnant") ? 1 : 0,
                  1
               )
            );
            case KEY -> List.of(
               requirement(
                  "Nexus Core Access Key",
                  "Assembled at the Core Key Assembler.",
                  new ItemStack((ItemLike)ModItems.NEXUS_CORE_ACCESS_KEY.get()),
                  progress.hasNexusCoreAccessKey() ? 1 : 0,
                  1
               )
            );
            case GUARDIAN -> List.of(
               requirement(
                  "Guardian Core",
                  "Nexus Guardian defeated.",
                  new ItemStack((ItemLike)ModItems.GUARDIAN_CORE.get()),
                  progress.bossDefeated("nexus_guardian") ? 1 : 0,
                  1
               )
            );
            case ENDING -> List.of(
               requirement(
                  "Truth Engine",
                  "Final choice committed.",
                  new ItemStack((ItemLike)ModBlocks.TRUTH_ENGINE.get()),
                  progress.ending() == BlackboxEnding.NONE ? 0 : 1,
                  1
               )
            );
         };
      }

      List<ItemStack> rewards() {
         return switch (this) {
            case DECODE -> List.of(new ItemStack((ItemLike)ModItems.STATIC_FLUID.get(), 2));
            case VAULT -> List.of(new ItemStack((ItemLike)ModItems.CORRUPTED_FERRITE.get(), 4));
            case BOSSES -> List.of(new ItemStack((ItemLike)ModItems.CORE_ACCESS_KEY_MATRIX.get()));
            case KEY -> List.of(new ItemStack((ItemLike)ModItems.STATIC_FLUID.get(), 4));
            case GUARDIAN -> List.of(new ItemStack((ItemLike)ModItems.RESTORE_DIRECTIVE.get()), new ItemStack((ItemLike)ModItems.DESTROY_DIRECTIVE.get()));
            case ENDING -> List.of(new ItemStack((ItemLike)ModItems.DELETED_BLACKBOX_FRAGMENT.get()));
         };
      }

      ItemStack icon() {
         return switch (this) {
            case DECODE -> new ItemStack((ItemLike)ModBlocks.BLACKBOX_DECODER.get());
            case VAULT -> new ItemStack((ItemLike)ModBlocks.VAULT_MONOLITH.get());
            case BOSSES -> new ItemStack((ItemLike)ModItems.ECHO_IDENTITY_FRAGMENT.get());
            case KEY -> new ItemStack((ItemLike)ModItems.NEXUS_CORE_ACCESS_KEY.get());
            case GUARDIAN -> new ItemStack((ItemLike)ModItems.GUARDIAN_CORE.get());
            case ENDING -> new ItemStack((ItemLike)ModBlocks.TRUTH_ENGINE.get());
         };
      }

      private static TerminalMissionRequirement requirement(String label, String detail, ItemStack icon, int have, int need) {
         int safeNeed = Math.max(1, need);
         int safeHave = Math.max(0, Math.min(safeNeed, have));
         return TerminalMissionRequirement.custom(label, detail, icon, safeHave, safeNeed, safeHave >= safeNeed);
      }
   }
}
