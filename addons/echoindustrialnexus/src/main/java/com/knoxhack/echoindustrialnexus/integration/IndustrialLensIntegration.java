package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMultiblockControllerBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMultiblockCrateBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialRoboticArmMountBlockEntity;
import com.knoxhack.echoindustrialnexus.factory.IndustrialFactoryAlertLevel;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import com.knoxhack.echolens.api.ServerLensProvider;
import com.knoxhack.echolens.registry.LensProviderRegistry;
import com.knoxhack.echomultiblockcore.api.MultiblockRuntimeSnapshot;
import com.knoxhack.echomultiblockcore.api.MultiblockStatusSnapshot;
import com.knoxhack.echomultiblockcore.api.MultiblockTaskState;
import com.knoxhack.echomultiblockcore.api.TaskExecutionSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class IndustrialLensIntegration {
   private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

   private IndustrialLensIntegration() {
   }

   public static void register() {
      if (REGISTERED.compareAndSet(false, true)) {
         LensProviderRegistry.register(Provider.INSTANCE);
      }
   }

   private enum Provider implements ServerLensProvider {
      INSTANCE;

      @Override
      public Identifier id() {
         return EchoIndustrialNexus.id("industrial_factory_deep_scan");
      }

      @Override
      public int priority() {
         return 120;
      }

      @Override
      public LensDataCategory category() {
         return LensDataCategory.MACHINE;
      }

      @Override
      public boolean supports(LensContext context) {
         if (context == null || !context.hasBlock() || context.level() == null) {
            return false;
         }
         BlockEntity blockEntity = context.level().getBlockEntity(context.blockPos());
         return blockEntity instanceof IndustrialMultiblockControllerBlockEntity
            || blockEntity instanceof IndustrialRoboticArmMountBlockEntity
            || blockEntity instanceof IndustrialMultiblockCrateBlockEntity;
      }

      @Override
      public List<LensInfoSection> inspect(LensContext context) {
         BlockEntity blockEntity = context.level().getBlockEntity(context.blockPos());
         if (blockEntity instanceof IndustrialMultiblockControllerBlockEntity controller) {
            return controllerSections(controller);
         }
         if (blockEntity instanceof IndustrialRoboticArmMountBlockEntity arm) {
            return robotSections(arm);
         }
         if (blockEntity instanceof IndustrialMultiblockCrateBlockEntity crate) {
            return crateSections(crate);
         }
         return List.of();
      }

      private static List<LensInfoSection> controllerSections(IndustrialMultiblockControllerBlockEntity controller) {
         MultiblockStatusSnapshot status = controller.statusSnapshot();
         MultiblockRuntimeSnapshot runtime = controller.runtimeSnapshot();
         IndustrialFactoryAlertLevel alert = IndustrialFactoryAlertLevel.from(runtime.state(), runtime.tasks(),
            runtime.warningCount(), runtime.completion());
         TaskExecutionSnapshot active = runtime.tasks().stream()
            .filter(task -> task.state() == MultiblockTaskState.ACTIVE)
            .findFirst()
            .orElse(null);
         TaskExecutionSnapshot blocked = runtime.tasks().stream()
            .filter(task -> task.state() == MultiblockTaskState.BLOCKED)
            .findFirst()
            .orElse(null);
         List<LensInfoRow> rows = new ArrayList<>();
         rows.add(row("Facility", status.name(), "F", tone(alert), LensVisibility.COMPACT));
         rows.add(row("Alert", alert.name(), "A", tone(alert), LensVisibility.COMPACT));
         rows.add(row("Integrity", Math.round(status.integrity()) + "%", "I", LensTone.GOOD, LensVisibility.COMPACT));
         rows.add(row("Completion", Math.round(status.completion() * 100.0D) + "%", "C", LensTone.INFO, LensVisibility.EXPANDED));
         rows.add(row("Robotics", runtime.roboticComponentCount() + " arm(s)", "R",
            runtime.roboticComponentCount() > 0 ? LensTone.GOOD : LensTone.WARNING, LensVisibility.COMPACT));
         rows.add(row("Queue", runtime.taskCount() + "/" + controller.taskQueueCapacity(), "Q",
            runtime.taskCount() >= controller.taskQueueCapacity() ? LensTone.WARNING : LensTone.INFO, LensVisibility.COMPACT));
         rows.add(row("Restock", (controller.logisticsAutoRestockEnabled() ? "ON" : "OFF")
            + " x" + controller.logisticsRestockTargetRuns(), "L",
            controller.logisticsAutoRestockEnabled() ? LensTone.GOOD : LensTone.MUTED, LensVisibility.EXPANDED));
         rows.add(row("Task", active == null ? "Idle" : active.displayName() + " "
            + active.progressTicks() + "/" + active.durationTicks(), "T",
            active == null ? LensTone.MUTED : LensTone.GOOD, LensVisibility.EXPANDED));
         rows.add(row("Blocked", blocked == null ? "none" : blocked.blockedReason(), "B",
            blocked == null ? LensTone.MUTED : LensTone.WARNING, LensVisibility.EXPANDED));
         rows.add(row("Workcells", runtime.tasks().stream()
            .map(task -> task.workcellId() == null ? task.recipeCategory() : task.workcellId().getPath())
            .distinct()
            .reduce((left, right) -> left + ", " + right)
            .orElse("awaiting task"), "W", LensTone.INFO, LensVisibility.DEEP));
         rows.add(row("Warnings", runtime.warnings().isEmpty() ? "none" : runtime.warnings().get(0), "!",
            runtime.warnings().isEmpty() ? LensTone.MUTED : LensTone.WARNING, LensVisibility.DEEP));
         return List.of(LensInfoSection.of(EchoIndustrialNexus.id("lens/factory_controller"),
            LensDataCategory.MACHINE, "Industrial Factory Command", "F", tone(alert), LensVisibility.COMPACT, rows));
      }

      private static List<LensInfoSection> robotSections(IndustrialRoboticArmMountBlockEntity arm) {
         List<LensInfoRow> rows = List.of(
            row("State", arm.getRobotState().name(), "S", LensTone.INFO, LensVisibility.COMPACT),
            row("Tool", arm.getInstalledTools().isEmpty() ? "none" : arm.getInstalledTools().get(0).name(), "T",
               arm.getInstalledTools().isEmpty() ? LensTone.WARNING : LensTone.GOOD, LensVisibility.COMPACT),
            row("Heat", arm.getHeat() + "/" + arm.getMaxHeat(), "H",
               arm.getHeat() > 75 ? LensTone.WARNING : LensTone.INFO, LensVisibility.COMPACT),
            row("Reach", Integer.toString(arm.getReach()), "R", LensTone.INFO, LensVisibility.EXPANDED),
            row("Target", arm.targetPos().equals(net.minecraft.core.BlockPos.ZERO)
               ? "none" : arm.targetPos().toShortString(), "P", LensTone.MUTED, LensVisibility.DEEP),
            row("Runtime", arm.getRobotId().toString(), "ID", LensTone.MUTED, LensVisibility.DEEP)
         );
         return List.of(LensInfoSection.of(EchoIndustrialNexus.id("lens/robotic_arm_mount"),
            LensDataCategory.MACHINE, "Industrial Robotic Arm", "R", LensTone.ECHO, LensVisibility.COMPACT, rows));
      }

      private static List<LensInfoSection> crateSections(IndustrialMultiblockCrateBlockEntity crate) {
         int occupied = 0;
         int total = 0;
         if (crate instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
               ItemStack stack = container.getItem(i);
               if (!stack.isEmpty()) {
                  occupied++;
                  total += stack.getCount();
               }
            }
         }
         List<LensInfoRow> rows = List.of(
            row("Kind", crate.kind().label(), "K", LensTone.INFO, LensVisibility.COMPACT),
            row("Slots", occupied + "/" + crate.getContainerSize(), "S", LensTone.INFO, LensVisibility.COMPACT),
            row("Items", Integer.toString(total), "I", total > 0 ? LensTone.GOOD : LensTone.MUTED, LensVisibility.COMPACT),
            row("Endpoint", crate.kind().name().toLowerCase(java.util.Locale.ROOT), "E", LensTone.ECHO, LensVisibility.EXPANDED),
            row("Status", crate.statusLine(), "D", LensTone.MUTED, LensVisibility.DEEP)
         );
         return List.of(LensInfoSection.of(EchoIndustrialNexus.id("lens/factory_crate"),
            LensDataCategory.INVENTORY, "Industrial Depot Crate", "C", LensTone.INFO, LensVisibility.COMPACT, rows));
      }

      private static LensInfoRow row(String label, String value, String icon, LensTone tone, LensVisibility visibility) {
         return LensInfoRow.of(label, value, icon, tone, visibility);
      }

      private static LensTone tone(IndustrialFactoryAlertLevel alert) {
         return switch (alert == null ? IndustrialFactoryAlertLevel.IDLE : alert) {
            case ONLINE, IDLE -> LensTone.GOOD;
            case ACTIVE -> LensTone.ECHO;
            case BLOCKED, DAMAGED -> LensTone.WARNING;
            case INCOMPLETE -> LensTone.MUTED;
         };
      }
   }
}
