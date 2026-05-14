package com.knoxhack.echoconvoyprotocol.task;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyMultiblockControllerBlockEntity;
import com.knoxhack.echoconvoyprotocol.content.ConvoyContent;
import com.knoxhack.echoconvoyprotocol.content.ConvoyRouteDefinition;
import com.knoxhack.echoconvoyprotocol.integration.ConvoyLogisticsIntegration;
import com.knoxhack.echoconvoyprotocol.integration.ConvoyMissionHooks;
import com.knoxhack.echomultiblockcore.api.AutomationEffectHandler;
import com.knoxhack.echomultiblockcore.api.AutomationEffectHandlers;
import com.knoxhack.echomultiblockcore.api.AutomationEffectInvocation;
import com.knoxhack.echomultiblockcore.api.AutomationEffectResult;
import java.util.Locale;
import java.util.Set;
import net.minecraft.resources.Identifier;

public final class ConvoyMultiblockTasks {
   public static final Identifier REPAIR_CONVOY_VEHICLE = id("repair_convoy_vehicle");
   public static final Identifier INSTALL_ARMOR_PLATING = id("install_armor_plating");
   public static final Identifier LOAD_FIELD_SUPPLY_CRATE = id("load_field_supply_crate");
   public static final Identifier REFUEL_CONVOY = id("refuel_convoy");
   public static final Identifier PREPARE_ROUTE_DISPATCH = id("prepare_route_dispatch");
   public static final Identifier DISPATCH_CONVOY_TO_ROUTE = id("dispatch_convoy_to_route");
   public static final Identifier RECOVER_DAMAGED_CONVOY = id("recover_damaged_convoy");
   public static final Identifier UNLOAD_SALVAGE_RETURN = id("unload_salvage_return");
   public static final Identifier REQUEST_ROUTE_SUPPLIES = id("request_route_supplies");
   public static final Identifier SYNC_LOGISTICS_INVENTORY = id("sync_logistics_inventory");
   public static final Identifier CANCEL_ROUTE_SUPPLY_REQUEST = id("cancel_route_supply_request");
   public static final Identifier EXPORT_SALVAGE_MANIFEST = id("export_salvage_manifest");
   public static final Identifier STAGE_FIELD_OPERATION = id("stage_field_operation");
   public static final Identifier LAUNCH_FIELD_OPERATION = id("launch_field_operation");
   public static final Identifier RESOLVE_FIELD_INCIDENT = id("resolve_field_incident");
   public static final Identifier RECALL_CONVOY_OPERATION = id("recall_convoy_operation");
   public static final Identifier RECOVER_FAILED_OPERATION = id("recover_failed_operation");

   private static final Identifier EFFECT_PROVIDER = id("automation_effects");
   private static final Set<Identifier> EFFECT_IDS = Set.of(
      REPAIR_CONVOY_VEHICLE,
      INSTALL_ARMOR_PLATING,
      LOAD_FIELD_SUPPLY_CRATE,
      REFUEL_CONVOY,
      PREPARE_ROUTE_DISPATCH,
      DISPATCH_CONVOY_TO_ROUTE,
      RECOVER_DAMAGED_CONVOY,
      UNLOAD_SALVAGE_RETURN,
      REQUEST_ROUTE_SUPPLIES,
      SYNC_LOGISTICS_INVENTORY,
      CANCEL_ROUTE_SUPPLY_REQUEST,
      EXPORT_SALVAGE_MANIFEST,
      STAGE_FIELD_OPERATION,
      LAUNCH_FIELD_OPERATION,
      RESOLVE_FIELD_INCIDENT,
      RECALL_CONVOY_OPERATION,
      RECOVER_FAILED_OPERATION
   );
   private static boolean registered;

   private ConvoyMultiblockTasks() {
   }

   public static void register() {
      if (registered) {
         return;
      }
      registered = true;
      AutomationEffectHandlers.register(new ConvoyAutomationEffectHandler());
   }

   private static final class ConvoyAutomationEffectHandler implements AutomationEffectHandler {
      @Override
      public Identifier providerId() {
         return EFFECT_PROVIDER;
      }

      @Override
      public boolean handles(Identifier effectId) {
         return EFFECT_IDS.contains(effectId);
      }

      @Override
      public AutomationEffectResult onComplete(AutomationEffectInvocation invocation) {
         if (invocation == null || invocation.effectId() == null || invocation.level() == null) {
            return AutomationEffectResult.fail("Convoy automation effect could not resolve server context.");
         }
         if (!(invocation.level().getBlockEntity(invocation.controllerPos()) instanceof ConvoyMultiblockControllerBlockEntity controller)) {
            return AutomationEffectResult.allow("Convoy automation effect skipped because the controller is not loaded.");
         }
         Identifier effectId = invocation.effectId();
         ConvoyRouteDefinition route = selectedRoute(controller);
         if (effectId.equals(REPAIR_CONVOY_VEHICLE)) {
            controller.convoyState().repairVehicle();
            ConvoyMissionHooks.recordRefuelRepairNear(invocation.level(), invocation.controllerPos(), "depot_repair");
         } else if (effectId.equals(INSTALL_ARMOR_PLATING)) {
            controller.convoyState().installArmor();
         } else if (effectId.equals(LOAD_FIELD_SUPPLY_CRATE)) {
            controller.convoyState().loadCargo();
         } else if (effectId.equals(REFUEL_CONVOY)) {
            controller.convoyState().refuel();
            ConvoyMissionHooks.recordRefuelRepairNear(invocation.level(), invocation.controllerPos(), "depot_refuel");
         } else if (effectId.equals(PREPARE_ROUTE_DISPATCH)) {
            controller.convoyState().prepareRoute(route);
         } else if (effectId.equals(DISPATCH_CONVOY_TO_ROUTE)) {
            controller.convoyState().dispatch();
         } else if (effectId.equals(RECOVER_DAMAGED_CONVOY)) {
            controller.convoyState().recoverConvoy();
            ConvoyMissionHooks.recordRecoveryNear(invocation.level(), invocation.controllerPos(), "depot_recovery");
         } else if (effectId.equals(UNLOAD_SALVAGE_RETURN)) {
            controller.convoyState().completeActiveRoute();
         } else if (effectId.equals(REQUEST_ROUTE_SUPPLIES)) {
            ConvoyLogisticsIntegration.requestRouteSupplies(controller, route);
         } else if (effectId.equals(SYNC_LOGISTICS_INVENTORY)) {
            ConvoyLogisticsIntegration.syncInventory(controller, route);
         } else if (effectId.equals(CANCEL_ROUTE_SUPPLY_REQUEST)) {
            ConvoyLogisticsIntegration.cancelRouteSupplyRequest(controller);
         } else if (effectId.equals(EXPORT_SALVAGE_MANIFEST)) {
            boolean exported = ConvoyLogisticsIntegration.exportSalvageManifest(controller, route);
            controller.fieldOperation().markSalvageExported(exported);
         } else if (effectId.equals(STAGE_FIELD_OPERATION)) {
            if (controller.fieldOperation().stage(route, invocation.level().getGameTime())) {
               ConvoyMissionHooks.recordFieldOperationStagedNear(invocation.level(), invocation.controllerPos(), route == null ? null : route.id());
            }
         } else if (effectId.equals(LAUNCH_FIELD_OPERATION)) {
            if (controller.fieldOperation().launch(route, controller.convoyState(), invocation.level().getGameTime())) {
               ConvoyMissionHooks.recordFieldOperationStagedNear(invocation.level(), invocation.controllerPos(), route == null ? null : route.id());
            }
         } else if (effectId.equals(RESOLVE_FIELD_INCIDENT)) {
            Identifier incident = Identifier.tryParse(controller.fieldOperation().incidentId());
            if (controller.fieldOperation().resolveIncident(controller.convoyState())) {
               ConvoyMissionHooks.recordIncidentResolvedNear(invocation.level(), invocation.controllerPos(), incident);
            }
         } else if (effectId.equals(RECALL_CONVOY_OPERATION)) {
            controller.fieldOperation().recall(controller.convoyState());
         } else if (effectId.equals(RECOVER_FAILED_OPERATION)) {
            if (controller.fieldOperation().recover(controller.convoyState())) {
               ConvoyMissionHooks.recordRecoveryNear(invocation.level(), invocation.controllerPos(), "field_operation");
            }
         }
         return AutomationEffectResult.allow();
      }
   }

   private static ConvoyRouteDefinition selectedRoute(ConvoyMultiblockControllerBlockEntity controller) {
      String operation = controller == null ? "" : controller.fieldOperation().routeId();
      String active = controller == null ? "" : controller.convoyState().activeRouteId();
      String prepared = controller == null ? "" : controller.convoyState().routeId();
      String raw = !operation.isBlank() ? operation : (!active.isBlank() ? active : prepared);
      Identifier routeId = Identifier.tryParse(raw);
      return routeId == null
         ? ConvoyContent.firstRoute().orElse(null)
         : ConvoyContent.route(routeId).orElseGet(() -> ConvoyContent.firstRoute().orElse(null));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, path);
   }

   public static Identifier taskId(String raw) {
      String clean = raw == null ? "" : raw.strip();
      return clean.contains(":") ? Identifier.tryParse(clean) : id(clean.toLowerCase(Locale.ROOT));
   }
}
