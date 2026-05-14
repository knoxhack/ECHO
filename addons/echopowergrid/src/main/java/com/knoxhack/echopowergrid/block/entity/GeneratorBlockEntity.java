package com.knoxhack.echopowergrid.block.entity;

import com.knoxhack.echopowergrid.api.EchoEnergyStorage;
import com.knoxhack.echopowergrid.api.EchoPowerNode;
import com.knoxhack.echopowergrid.api.EchoPowerNodeType;
import com.knoxhack.echopowergrid.api.EchoPowerQuality;
import com.knoxhack.echopowergrid.api.EchoPowerTier;
import com.knoxhack.echopowergrid.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntity extends BlockEntity implements EchoEnergyStorage, EchoPowerNode {
    private long generationRate;
    private long bufferSize;
    private boolean usesFuel;
    private long energy;
    private int burnTime;
    private int totalBurnTime;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR.get(), pos, state);
        if (state.getBlock() instanceof com.knoxhack.echopowergrid.block.GeneratorBlock gen) {
            this.generationRate = gen.getGenerationRate();
            this.bufferSize = gen.getBufferSize();
            this.usesFuel = gen.usesFuel();
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity gen) {
        if (level.isClientSide()) return;

        boolean changed = false;
        boolean creativeSource = !gen.usesFuel && gen.generationRate >= Long.MAX_VALUE / 8;
        if (gen.burnTime > 0) {
            gen.burnTime--;
            changed = true;
        }

        long generatedThisTick = gen.getGenerationPerTick();
        if (generatedThisTick > 0 && gen.bufferSize > 0 && !creativeSource) {
            long toGen = Math.min(generatedThisTick, gen.bufferSize - gen.energy);
            if (toGen > 0) {
                gen.energy += toGen;
                changed = true;
            }
        }

        if (gen.usesFuel && gen.burnTime <= 0 && gen.energy < gen.bufferSize) {
            // Fuel would be consumed here in a full implementation
            // For MVP, we simulate burn with a simple timer if fueled by interaction
            if (gen.burnTime == 0 && gen.totalBurnTime > 0) {
                gen.totalBurnTime = 0;
                changed = true;
            }
        }

        // Creative source bypass
        if (creativeSource) {
            gen.energy = gen.bufferSize;
            changed = true;
        }

        if (changed) {
            gen.setChanged();
        }
    }

    public void onUse(Player player, ItemStack stack) {
        if (usesFuel) {
            boolean isFuel = net.minecraft.world.item.Items.COAL.equals(stack.getItem())
                    || net.minecraft.world.item.Items.CHARCOAL.equals(stack.getItem())
                    || net.minecraft.world.item.Items.OAK_PLANKS.equals(stack.getItem());
            if (isFuel && burnTime <= 0) {
                burnTime = 200; // 200 ticks burn time simplified
                totalBurnTime = burnTime;
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.sendSystemMessage(Component.literal("ECHO GRID // Fuel loaded. Burn time: " + burnTime + " ticks."));
                setChanged();
            } else if (burnTime > 0) {
                player.sendSystemMessage(Component.literal("ECHO GRID // Buffer: " + energy + "/" + bufferSize + " EP. Burning for " + burnTime + " more ticks."));
            } else {
                player.sendSystemMessage(Component.literal("ECHO GRID // Buffer: " + energy + "/" + bufferSize + " EP. Insert burnable fuel."));
            }
        } else {
            player.sendSystemMessage(Component.literal("ECHO GRID // Generator: " + generationRate + " EP/t. Buffer: " + energy + "/" + bufferSize + " EP."));
        }
    }

    public long getGenerationRate() { return generationRate; }

    public long getGenerationPerTick() {
        if (usesFuel) return burnTime > 0 ? generationRate : 0;
        if (generationRate >= Long.MAX_VALUE / 8) return generationRate; // Creative
        return level != null && (level.getGameTime() % 24000L) < 13000L ? generationRate : 0;
    }

    public long getAvailableEnergyForNetwork(int ticks) {
        if (generationRate >= Long.MAX_VALUE / 8) return Long.MAX_VALUE / 4;
        if (bufferSize <= 0) return saturatedMultiply(getGenerationPerTick(), Math.max(1, ticks));
        return Math.min(energy, saturatedMultiply(getMaxOutput(), Math.max(1, ticks)));
    }

    public long extractEnergyForNetwork(long amount) {
        if (amount <= 0) return 0;
        if (generationRate >= Long.MAX_VALUE / 8 || bufferSize <= 0) {
            return Math.min(amount, Long.MAX_VALUE / 4);
        }
        return extractEnergy(amount, false);
    }

    private static long saturatedMultiply(long value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Long.MAX_VALUE / multiplier) return Long.MAX_VALUE;
        return value * multiplier;
    }

    public EchoPowerQuality getPowerQuality() {
        if (usesFuel) return EchoPowerQuality.DIRTY;
        return EchoPowerQuality.STABLE;
    }

    @Override
    public long getEnergyStored() { return energy; }

    @Override
    public long getMaxEnergyStored() { return bufferSize; }

    @Override
    public long receiveEnergy(long amount, boolean simulate) {
        long space = bufferSize - energy;
        long received = Math.min(amount, space);
        if (!simulate) {
            energy += received;
            setChanged();
        }
        return received;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long extracted = Math.min(amount, energy);
        if (!simulate) {
            energy -= extracted;
            setChanged();
        }
        return extracted;
    }

    @Override
    public long getMaxInput() { return generationRate; }

    @Override
    public long getMaxOutput() { return bufferSize > 0 ? generationRate : 0; }

    @Override
    public boolean canReceive() { return false; }

    @Override
    public boolean canExtract() { return bufferSize > 0; }

    @Override
    public BlockPos getNodePos() { return worldPosition; }

    @Override
    public ResourceKey<Level> getDimension() { return level != null ? level.dimension() : null; }

    @Override
    public EchoPowerNodeType getNodeType() {
        if (generationRate >= Long.MAX_VALUE / 8) return EchoPowerNodeType.CREATIVE_SOURCE;
        return EchoPowerNodeType.GENERATOR;
    }

    @Override
    public long getDemandPerTick() { return 0; }

    @Override
    public long getStoredEnergy() { return energy; }

    @Override
    public long getCapacity() { return bufferSize; }

    @Override
    public long getTransferLimit() { return generationRate; }

    @Override
    public boolean isOnline() { return !usesFuel || burnTime > 0; }

    @Override
    public boolean isOverloaded() { return false; }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("Energy", energy);
        output.putInt("BurnTime", burnTime);
        output.putInt("TotalBurnTime", totalBurnTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy = input.getLongOr("Energy", 0);
        burnTime = input.getIntOr("BurnTime", 0);
        totalBurnTime = input.getIntOr("TotalBurnTime", 0);
    }
}
