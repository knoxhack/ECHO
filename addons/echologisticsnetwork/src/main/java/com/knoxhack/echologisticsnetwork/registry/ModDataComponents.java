package com.knoxhack.echologisticsnetwork.registry;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.content.LoadoutCardSelection;
import com.knoxhack.echologisticsnetwork.content.RemoteRequestSelection;
import com.knoxhack.echologisticsnetwork.content.RouteManifestSelection;
import com.knoxhack.echologisticsnetwork.content.SupplyTagSelection;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
   public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
      DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EchoLogisticsNetwork.MODID);

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SupplyTagSelection>> SUPPLY_TAG_SELECTION =
      DATA_COMPONENT_TYPES.register("supply_tag_selection", () -> DataComponentType.<SupplyTagSelection>builder()
         .persistent(SupplyTagSelection.CODEC)
         .networkSynchronized(SupplyTagSelection.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<LoadoutCardSelection>> LOADOUT_CARD_SELECTION =
      DATA_COMPONENT_TYPES.register("loadout_card_selection", () -> DataComponentType.<LoadoutCardSelection>builder()
         .persistent(LoadoutCardSelection.CODEC)
         .networkSynchronized(LoadoutCardSelection.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<RouteManifestSelection>> ROUTE_MANIFEST_SELECTION =
      DATA_COMPONENT_TYPES.register("route_manifest_selection", () -> DataComponentType.<RouteManifestSelection>builder()
         .persistent(RouteManifestSelection.CODEC)
         .networkSynchronized(RouteManifestSelection.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<RemoteRequestSelection>> REMOTE_REQUEST_SELECTION =
      DATA_COMPONENT_TYPES.register("remote_request_selection", () -> DataComponentType.<RemoteRequestSelection>builder()
         .persistent(RemoteRequestSelection.CODEC)
         .networkSynchronized(RemoteRequestSelection.STREAM_CODEC)
         .build());

   private ModDataComponents() {
   }

   public static void register(IEventBus eventBus) {
      DATA_COMPONENT_TYPES.register(eventBus);
   }
}
