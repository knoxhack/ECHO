package com.knoxhack.echonexusprotocol.data;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class NexusEnergyStorage extends SnapshotJournal<Integer> implements EnergyHandler {
   private final int capacity;
   private final int maxReceive;
   private final int maxExtract;
   private final Runnable onChanged;
   private int energy;

   public NexusEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
      this.capacity = Math.max(0, capacity);
      this.maxReceive = Math.max(0, maxReceive);
      this.maxExtract = Math.max(0, maxExtract);
      this.onChanged = onChanged == null ? () -> {} : onChanged;
   }

   public long getAmountAsLong() {
      return this.energy;
   }

   public long getCapacityAsLong() {
      return this.capacity;
   }

   public int insert(int amount, TransactionContext transaction) {
      if (amount > 0 && this.maxReceive > 0) {
         int accepted = Math.min(Math.min(amount, this.maxReceive), Math.max(0, this.capacity - this.energy));
         if (accepted > 0) {
            this.updateSnapshots(transaction);
            this.energy += accepted;
         }

         return accepted;
      } else {
         return 0;
      }
   }

   public int extract(int amount, TransactionContext transaction) {
      if (amount > 0 && this.maxExtract > 0) {
         int extracted = Math.min(Math.min(amount, this.maxExtract), this.energy);
         if (extracted > 0) {
            this.updateSnapshots(transaction);
            this.energy -= extracted;
         }

         return extracted;
      } else {
         return 0;
      }
   }

   public int getEnergyStored() {
      return this.energy;
   }

   public int getCapacityAsInt() {
      return this.capacity;
   }

   public int getSpace() {
      return Math.max(0, this.capacity - this.energy);
   }

   public void setEnergyStored(int energy) {
      int next = Math.max(0, Math.min(this.capacity, energy));
      if (this.energy != next) {
         this.energy = next;
         this.onChanged.run();
      }
   }

   public boolean consume(int amount) {
      if (this.energy < amount) {
         return false;
      } else {
         this.setEnergyStored(this.energy - amount);
         return true;
      }
   }

   public void receiveDirect(int amount) {
      this.setEnergyStored(this.energy + Math.max(0, amount));
   }

   protected Integer createSnapshot() {
      return this.energy;
   }

   protected void revertToSnapshot(Integer snapshot) {
      this.energy = Math.max(0, Math.min(this.capacity, snapshot));
   }

   protected void onRootCommit(Integer snapshot) {
      this.onChanged.run();
   }
}
