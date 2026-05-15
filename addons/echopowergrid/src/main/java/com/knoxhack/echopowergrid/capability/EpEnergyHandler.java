package com.knoxhack.echopowergrid.capability;

import com.knoxhack.echopowergrid.api.EchoEnergyStorage;
import com.knoxhack.echopowergrid.config.PowerGridConfig;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EpEnergyHandler extends SnapshotJournal<Long> implements EnergyHandler {
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
        long receivedEp = storage.receiveEnergy(epAmount, true);
        int received = (int) Math.min(Integer.MAX_VALUE, receivedEp / PowerGridConfig.FE_TO_EP_RATIO.get());
        if (received > 0) {
            if (transaction != null) {
                updateSnapshots(transaction);
            }
            storage.receiveEnergy((long) (received * PowerGridConfig.FE_TO_EP_RATIO.get()), false);
        }
        return received;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (!PowerGridConfig.ENABLE_FE_BRIDGE.get() || !storage.canExtract() || amount <= 0) return 0;
        long epAmount = (long) (amount * PowerGridConfig.FE_TO_EP_RATIO.get());
        long extractedEp = storage.extractEnergy(epAmount, true);
        int extracted = (int) Math.min(Integer.MAX_VALUE, extractedEp / PowerGridConfig.FE_TO_EP_RATIO.get());
        if (extracted > 0) {
            if (transaction != null) {
                updateSnapshots(transaction);
            }
            storage.extractEnergy((long) (extracted * PowerGridConfig.FE_TO_EP_RATIO.get()), false);
        }
        return extracted;
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
    protected Long createSnapshot() {
        return storage.getEnergyStored();
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        long target = Math.max(0L, Math.min(storage.getMaxEnergyStored(), snapshot == null ? 0L : snapshot));
        long current = storage.getEnergyStored();
        if (current > target) {
            drainDirect(current - target);
        } else if (current < target) {
            fillDirect(target - current);
        }
    }

    protected void onRootCommit(Long snapshot) {
        if (onChanged != null) onChanged.run();
    }

    private void drainDirect(long amount) {
        long remaining = amount;
        int guard = 0;
        while (remaining > 0L && guard++ < 1024) {
            long extracted = storage.extractEnergy(remaining, false);
            if (extracted <= 0L) {
                break;
            }
            remaining -= extracted;
        }
    }

    private void fillDirect(long amount) {
        long remaining = amount;
        int guard = 0;
        while (remaining > 0L && guard++ < 1024) {
            long received = storage.receiveEnergy(remaining, false);
            if (received <= 0L) {
                break;
            }
            remaining -= received;
        }
    }
}
