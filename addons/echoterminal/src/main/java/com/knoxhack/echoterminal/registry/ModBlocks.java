package com.knoxhack.echoterminal.registry;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.block.EchoTerminalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoTerminal.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(EchoTerminal.MODID);

    public static final DeferredBlock<Block> ECHO_TERMINAL_BLOCK = BLOCKS.registerBlock("echo_terminal",
            EchoTerminalBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 8));

    public static final DeferredItem<BlockItem> ECHO_TERMINAL_BLOCK_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("echo_terminal", ECHO_TERMINAL_BLOCK);

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
