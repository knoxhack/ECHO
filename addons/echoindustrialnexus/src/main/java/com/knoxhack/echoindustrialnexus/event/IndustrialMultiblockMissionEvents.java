package com.knoxhack.echoindustrialnexus.event;

import com.knoxhack.echomultiblockcore.event.RoboticTaskCompletedEvent;
import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echoindustrialnexus.progress.IndustrialProgress;
import net.neoforged.bus.api.SubscribeEvent;

public final class IndustrialMultiblockMissionEvents {
   @SubscribeEvent
   public void onRoboticTaskCompleted(RoboticTaskCompletedEvent event) {
      if (event == null || event.taskId == null || !EchoIndustrialNexus.MODID.equals(event.taskId.getNamespace())) {
         return;
      }
      IndustrialProgress.recordAutomationTaskCompleted(event.level, event.controllerPos, event.taskId);
   }
}
