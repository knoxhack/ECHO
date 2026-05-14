package com.knoxhack.echoindustrialnexus.block;

import com.knoxhack.echomultiblockcore.block.MultiblockControllerBlock;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMultiblockControllerBlockEntity;
import com.knoxhack.echoindustrialnexus.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class IndustrialMultiblockControllerBlock extends MultiblockControllerBlock {
   private final Identifier defaultTaskId;

   public IndustrialMultiblockControllerBlock(Identifier defaultDefinitionId, Identifier defaultTaskId, Properties properties) {
      super(defaultDefinitionId, properties);
      this.defaultTaskId = defaultTaskId;
   }

   public Identifier defaultTaskId() {
      return defaultTaskId;
   }

   @Override
   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new IndustrialMultiblockControllerBlockEntity(pos, state);
   }

   @Override
   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == ModBlockEntities.INDUSTRIAL_MULTIBLOCK_CONTROLLER.get()
         ? (tickLevel, pos, blockState, blockEntity) -> IndustrialMultiblockControllerBlockEntity.tick(
            tickLevel, pos, blockState, (IndustrialMultiblockControllerBlockEntity)blockEntity)
         : null;
   }
}
