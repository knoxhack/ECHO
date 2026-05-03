package com.knoxhack.echoashfallprotocol.energy;

import com.knoxhack.echoashfallprotocol.capability.IEnergyStorage;
import com.knoxhack.echoashfallprotocol.item.BatteryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.knoxhack.echoashfallprotocol.power.PowerNetwork;

public final class EnergyAccess {
    private EnergyAccess() {
    }

    public static EnergyHandler wrap(IEnergyStorage storage, Runnable onChanged) {
        return new LegacyEnergyHandler(storage, onChanged);
    }

    public static boolean isEnergyItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof BatteryItem) {
            return true;
        }
        return Capabilities.Energy.ITEM.getCapability(stack, ItemAccess.forStack(stack)) != null;
    }

    public static int insert(EnergyHandler handler, int amount) {
        if (handler == null || amount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    public static int extract(EnergyHandler handler, int amount) {
        if (handler == null || amount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    public static int simulateInsert(EnergyHandler handler, int amount) {
        if (handler == null || amount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.insert(amount, transaction);
        }
    }

    public static int simulateExtract(EnergyHandler handler, int amount) {
        if (handler == null || amount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.extract(amount, transaction);
        }
    }

    public static EnergyHandler getItemEnergy(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return Capabilities.Energy.ITEM.getCapability(stack, ItemAccess.forStack(stack));
    }

    public static EnergyHandler getBlockEnergy(Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        EnergyHandler handler = Capabilities.Energy.BLOCK.getCapability(level, pos, state, be, side);
        if (handler != null) {
            return handler;
        }
        if (be instanceof IEnergyStorage storage) {
            return wrap(storage, be::setChanged);
        }
        return null;
    }

    public static int insertBlockEnergy(Level level, BlockPos pos, Direction side, int amount) {
        return insert(getBlockEnergy(level, pos, side), amount);
    }

    public static int extractBlockEnergy(Level level, BlockPos pos, Direction side, int amount) {
        return extract(getBlockEnergy(level, pos, side), amount);
    }

    public static int simulateInsertBlockEnergy(Level level, BlockPos pos, Direction side, int amount) {
        return simulateInsert(getBlockEnergy(level, pos, side), amount);
    }

    public static int simulateExtractBlockEnergy(Level level, BlockPos pos, Direction side, int amount) {
        return simulateExtract(getBlockEnergy(level, pos, side), amount);
    }

    public static int getBlockEnergyStored(Level level, BlockPos pos, Direction side) {
        EnergyHandler handler = getBlockEnergy(level, pos, side);
        return handler == null ? 0 : handler.getAmountAsInt();
    }

    public static int getBlockEnergyCapacity(Level level, BlockPos pos, Direction side) {
        EnergyHandler handler = getBlockEnergy(level, pos, side);
        return handler == null ? 0 : handler.getCapacityAsInt();
    }

    public static int transferFromStorageToBlock(IEnergyStorage source, Level level, BlockPos targetPos, Direction side, int maxAmount) {
        if (source == null || maxAmount <= 0 || !source.canExtract()) {
            return 0;
        }
        EnergyHandler target = getBlockEnergy(level, targetPos, side);
        if (target == null) {
            return 0;
        }
        int movable = Math.min(maxAmount, source.extractEnergy(maxAmount, true));
        int accepted = simulateInsert(target, movable);
        int extracted = source.extractEnergy(accepted, false);
        int inserted = insert(target, extracted);
        if (inserted < extracted) {
            source.receiveEnergy(extracted - inserted, false);
        }
        return inserted;
    }

    public static boolean hasLocalOrNetworkPower(IEnergyStorage storage, Level level, BlockPos pos, int amount) {
        if (storage != null && storage.extractEnergy(amount, true) >= amount) {
            return true;
        }
        return PowerNetwork.hasPowerAccess(level, pos);
    }

    public static boolean tryConsumeLocalOrNetworkPower(IEnergyStorage storage, Level level, BlockPos pos, int amount) {
        if (storage != null && storage.extractEnergy(amount, true) >= amount) {
            storage.extractEnergy(amount, false);
            return true;
        }
        return PowerNetwork.tryConsumePower(level, pos, amount);
    }

    public static int dischargeBatteryToStorage(ItemStack stack, IEnergyStorage storage) {
        if (stack.isEmpty() || storage == null || !storage.canReceive()) {
            return 0;
        }
        EnergyHandler battery = getItemEnergy(stack);
        if (battery == null) {
            return 0;
        }
        int space = Math.max(0, storage.getMaxEnergyStored() - storage.getEnergyStored());
        int extractable = simulateExtract(battery, space);
        int accepted = storage.receiveEnergy(extractable, true);
        int extracted = extract(battery, accepted);
        int inserted = storage.receiveEnergy(extracted, false);
        if (inserted < extracted) {
            insert(battery, extracted - inserted);
        }
        return inserted;
    }

    public static int chargeBatteryFromStorage(ItemStack stack, IEnergyStorage storage) {
        if (stack.isEmpty() || storage == null || !storage.canExtract()) {
            return 0;
        }
        EnergyHandler battery = getItemEnergy(stack);
        if (battery == null) {
            return 0;
        }
        int accepted = simulateInsert(battery, storage.extractEnergy(Integer.MAX_VALUE, true));
        int extracted = storage.extractEnergy(accepted, false);
        int inserted = insert(battery, extracted);
        if (inserted < extracted) {
            storage.receiveEnergy(extracted - inserted, false);
        }
        return inserted;
    }
}
