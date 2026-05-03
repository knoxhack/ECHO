package com.knoxhack.echoashfallprotocol.block.entity;

import com.knoxhack.echoashfallprotocol.capability.EnergyStorage;
import com.knoxhack.echoashfallprotocol.capability.IEnergyStorage;
import com.knoxhack.echoashfallprotocol.energy.EnergyAccess;
import com.knoxhack.echoashfallprotocol.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LoadDistributorBlockEntity extends BlockEntity implements IEnergyStorage {
    public static final int CAPACITY = 2000;
    public static final int MAX_TRANSFER = 512;

    private final EnergyStorage energyStorage = new EnergyStorage(CAPACITY, MAX_TRANSFER, MAX_TRANSFER);
    private PriorityMode priorityMode = PriorityMode.BALANCED;

    public LoadDistributorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOAD_DISTRIBUTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LoadDistributorBlockEntity entity) {
        if (level.isClientSide()) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (!com.knoxhack.echoashfallprotocol.power.PowerNetwork.isRelay(neighbor)
                    && com.knoxhack.echoashfallprotocol.power.PowerNetwork.canSupplyNetwork(neighbor)
                    && entity.energyStorage.getEnergyStored() < CAPACITY) {
                int space = CAPACITY - entity.energyStorage.getEnergyStored();
                int pulled = EnergyAccess.extractBlockEnergy(level, neighborPos, dir.getOpposite(), Math.min(MAX_TRANSFER, space));
                entity.energyStorage.receiveEnergy(pulled, false);
                if (pulled > 0) entity.setChanged();
            }
        }

        if (entity.energyStorage.getEnergyStored() > 0) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                if (EnergyAccess.transferFromStorageToBlock(entity, level, neighborPos, dir.getOpposite(), MAX_TRANSFER) > 0) {
                    entity.setChanged();
                }
                if (entity.energyStorage.getEnergyStored() <= 0) break;
            }
        }
    }

    public PriorityMode getPriorityMode() {
        return priorityMode;
    }

    public void cyclePriorityMode() {
        priorityMode = priorityMode.next();
        setChanged();
    }

    @Override public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    @Override public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
    @Override public int receiveEnergy(int amount, boolean simulate) { return energyStorage.receiveEnergy(amount, simulate); }
    @Override public int extractEnergy(int amount, boolean simulate) { return energyStorage.extractEnergy(amount, simulate); }
    @Override public boolean canReceive() { return true; }
    @Override public boolean canExtract() { return energyStorage.getEnergyStored() > 0; }
    @Override public void setEnergyStored(int energy) { energyStorage.setEnergyStored(energy); setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energy", energyStorage.getEnergyStored());
        output.putString("priorityMode", priorityMode.name());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setEnergyStored(input.getIntOr("energy", 0));
        priorityMode = PriorityMode.fromName(input.getStringOr("priorityMode", PriorityMode.BALANCED.name()));
    }

    public enum PriorityMode {
        BALANCED("Balanced"),
        SURVIVAL("Survival First"),
        FACTORY("Factory First"),
        GRID("Grid First");

        private final String displayName;

        PriorityMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        private PriorityMode next() {
            PriorityMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        private static PriorityMode fromName(String name) {
            for (PriorityMode value : values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }
            return BALANCED;
        }
    }
}
