package com.knoxhack.echoashfallprotocol.energy;

import com.knoxhack.echoashfallprotocol.capability.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * NeoForge FE adapter around the mod's existing internal energy API.
 */
public class LegacyEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
    private final IEnergyStorage storage;
    private final Runnable onChanged;

    public LegacyEnergyHandler(IEnergyStorage storage, Runnable onChanged) {
        this.storage = storage;
        this.onChanged = onChanged;
    }

    @Override
    public long getAmountAsLong() {
        return storage.getEnergyStored();
    }

    @Override
    public long getCapacityAsLong() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (amount <= 0 || !storage.canReceive()) {
            return 0;
        }
        int accepted = storage.receiveEnergy(amount, true);
        if (accepted > 0) {
            updateSnapshots(transaction);
            storage.receiveEnergy(accepted, false);
        }
        return accepted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (amount <= 0 || !storage.canExtract()) {
            return 0;
        }
        int extracted = storage.extractEnergy(amount, true);
        if (extracted > 0) {
            updateSnapshots(transaction);
            storage.extractEnergy(extracted, false);
        }
        return extracted;
    }

    @Override
    protected Integer createSnapshot() {
        return storage.getEnergyStored();
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        storage.setEnergyStored(snapshot);
    }

    @Override
    protected void onRootCommit(Integer snapshot) {
        onChanged.run();
    }
}
