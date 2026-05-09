package com.knoxhack.signalos.registry;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.block.SignalOsTerminalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SignalOS.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(SignalOS.MODID);

    public static final DeferredBlock<Block> TERMINAL = BLOCKS.registerBlock("terminal",
            SignalOsTerminalBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7));

    public static final DeferredItem<BlockItem> TERMINAL_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("terminal", TERMINAL);

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
