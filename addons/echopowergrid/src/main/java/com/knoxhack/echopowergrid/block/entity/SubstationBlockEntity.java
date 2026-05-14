package com.knoxhack.echopowergrid.block.entity;

import com.knoxhack.echopowergrid.api.EchoGridState;
import com.knoxhack.echopowergrid.api.EchoPowerGridApi;
import com.knoxhack.echopowergrid.api.EchoPowerNetwork;
import com.knoxhack.echopowergrid.api.PowerGridSnapshot;
import com.knoxhack.echopowergrid.menu.SubstationMenu;
import com.knoxhack.echopowergrid.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SubstationBlockEntity extends BlockEntity implements Container {
    private final SubstationContainerData data = new SubstationContainerData();

    public SubstationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUBSTATION.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SubstationBlockEntity be) {
        if (level.isClientSide()) return;
        PowerGridSnapshot snap = EchoPowerGridApi.getSnapshot(level, pos);
        be.data.generation = (int) Math.min(snap.totalGeneration(), Integer.MAX_VALUE);
        be.data.demand = (int) Math.min(snap.totalDemand(), Integer.MAX_VALUE);
        be.data.stored = (int) Math.min(snap.totalStored(), Integer.MAX_VALUE);
        be.data.capacity = (int) Math.min(snap.totalCapacity(), Integer.MAX_VALUE);
        be.data.state = snap.state().ordinal();
        be.data.nodeCount = snap.nodeCount();
    }

    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new SubstationMenu(containerId, playerInventory, data);
    }

    @Override
    public int getContainerSize() { return 0; }
    @Override
    public boolean isEmpty() { return true; }
    @Override
    public net.minecraft.world.item.ItemStack getItem(int slot) { return net.minecraft.world.item.ItemStack.EMPTY; }
    @Override
    public net.minecraft.world.item.ItemStack removeItem(int slot, int amount) { return net.minecraft.world.item.ItemStack.EMPTY; }
    @Override
    public net.minecraft.world.item.ItemStack removeItemNoUpdate(int slot) { return net.minecraft.world.item.ItemStack.EMPTY; }
    @Override
    public void setItem(int slot, net.minecraft.world.item.ItemStack stack) {}
    @Override
    public boolean stillValid(Player player) { return true; }
    @Override
    public void clearContent() {}

    private static class SubstationContainerData implements ContainerData {
        int generation;
        int demand;
        int stored;
        int capacity;
        int state;
        int nodeCount;

        @Override
        public int get(int index) {
            return switch (index) {
                case SubstationMenu.DATA_GENERATION -> generation;
                case SubstationMenu.DATA_DEMAND -> demand;
                case SubstationMenu.DATA_STORED -> stored;
                case SubstationMenu.DATA_CAPACITY -> capacity;
                case SubstationMenu.DATA_STATE -> state;
                case SubstationMenu.DATA_NODE_COUNT -> nodeCount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return SubstationMenu.DATA_COUNT;
        }
    }
}
