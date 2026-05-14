package com.knoxhack.echopowergrid.capability;

import com.knoxhack.echopowergrid.api.EchoEnergyStorage;
import com.knoxhack.echopowergrid.config.PowerGridConfig;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EpEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
    private final EchoEnergyStorage storage;
    private final Runnable onChanged;

    public EpEnergyHandler(EchoEnergyStorage storage, Runnable onChanged) {
        this.storage = storage;
        this.onChanged = onChanged;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (!PowerGridConfig.ENABLE_FE_BRIDGE.get() || !storage.canReceive() || amount <= 0) return 0;
        long epAmount = (long) (amount * PowerGridConfig.FE_TO_EP_RATIO.get());
        int received = (int) Math.min(Integer.MAX_VALUE, storage.receiveEnergy(epAmount, false));
        if (received > 0 && onChanged != null) {
            onChanged.run();
        }
        return (int) (received / PowerGridConfig.FE_TO_EP_RATIO.get());
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (!PowerGridConfig.ENABLE_FE_BRIDGE.get() || !storage.canExtract() || amount <= 0) return 0;
        long epAmount = (long) (amount * PowerGridConfig.FE_TO_EP_RATIO.get());
        int extracted = (int) Math.min(Integer.MAX_VALUE, storage.extractEnergy(epAmount, false));
        if (extracted > 0 && onChanged != null) {
            onChanged.run();
        }
        return (int) (extracted / PowerGridConfig.FE_TO_EP_RATIO.get());
    }

    public int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, storage.getEnergyStored() / PowerGridConfig.EP_TO_FE_RATIO.get());
    }

    @Override
    public long getAmountAsLong() {
        return (long) (storage.getEnergyStored() / PowerGridConfig.EP_TO_FE_RATIO.get());
    }

    @Override
    public long getCapacityAsLong() {
        return (long) (storage.getMaxEnergyStored() / PowerGridConfig.EP_TO_FE_RATIO.get());
    }

    @Override
    protected Integer createSnapshot() {
        return getEnergyStored();
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        // Energy is managed by underlying storage
    }

    protected void onRootCommit(Integer snapshot) {
        if (onChanged != null) onChanged.run();
    }
}
