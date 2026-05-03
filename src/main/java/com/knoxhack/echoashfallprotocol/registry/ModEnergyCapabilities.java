package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.energy.EnergyAccess;
import com.knoxhack.echoashfallprotocol.item.BatteryItem;
import com.knoxhack.echoashfallprotocol.capability.IEnergyStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;

public final class ModEnergyCapabilities {
    private ModEnergyCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Energy.ITEM, ModEnergyCapabilities::batteryHandler,
                ModItems.BASIC_BATTERY.get(), ModItems.ADVANCED_BATTERY.get(), ModItems.ELITE_BATTERY.get());

        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.BATTERY_BANK.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.SCRAP_DYNAMO.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.NEXUS_CAPACITOR.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.LOAD_DISTRIBUTOR.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.MICRO_GENERATOR.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.THERMAL_ARRAY.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.THERMAL_BURNER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.POWER_CABLE.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.POWER_NODE.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.HAND_RECYCLER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.WATER_PURIFIER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.FILTER_WORKBENCH.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.SCRAP_PRESS.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ORE_GRINDER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ISOTOPE_REFINER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.CRYSTALLINE_SYNTHESIZER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.DEEP_CORE_MINER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.RADIATION_CLEANSER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ATMOSPHERIC_SCRUBBER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.FIELD_MED_BAY.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.CONTAMINANT_CONDENSER.get(),
                (be, side) -> blockEnergyHandler(be));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.AUTOFEED_HOPPER.get(),
                (be, side) -> blockEnergyHandler(be));
    }

    private static EnergyHandler blockEnergyHandler(BlockEntity be) {
        if (be instanceof IEnergyStorage storage) {
            return EnergyAccess.wrap(storage, be::setChanged);
        }
        return null;
    }

    private static EnergyHandler batteryHandler(ItemStack stack, ItemAccess access) {
        if (stack.getItem() instanceof BatteryItem battery) {
            return new ItemAccessEnergyHandler(access.oneByOne(), ModDataComponents.STORED_ENERGY.get(),
                    battery.getCapacity(), battery.getMaxReceive(), battery.getMaxExtract());
        }
        return null;
    }
}
