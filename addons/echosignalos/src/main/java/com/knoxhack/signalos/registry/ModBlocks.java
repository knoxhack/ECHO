package com.knoxhack.signalos.registry;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.block.SignalOsServerRackBlock;
import com.knoxhack.signalos.block.SignalOsTerminalBlock;
import com.knoxhack.signalos.item.SignalOsDataDriveItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    public static final DeferredBlock<Block> WORKSTATION = BLOCKS.registerBlock("workstation",
            SignalOsTerminalBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 9));

    public static final DeferredBlock<Block> SERVER_RACK = BLOCKS.registerBlock("server_rack",
            SignalOsServerRackBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(4.0F, 8.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 4));

    public static final DeferredBlock<Block> NETWORK_RELAY = BLOCKS.registerSimpleBlock("network_relay",
            properties -> properties
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(2.5F, 5.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 10));

    public static final DeferredItem<BlockItem> TERMINAL_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("terminal", TERMINAL);
    public static final DeferredItem<BlockItem> WORKSTATION_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("workstation", WORKSTATION);
    public static final DeferredItem<BlockItem> SERVER_RACK_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("server_rack", SERVER_RACK);
    public static final DeferredItem<BlockItem> NETWORK_RELAY_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("network_relay", NETWORK_RELAY);
    public static final DeferredItem<SignalOsDataDriveItem> DATA_DRIVE =
            BLOCK_ITEMS.registerItem("data_drive",
                    SignalOsDataDriveItem::new,
                    properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));

    private ModBlocks() {
    }

    public static boolean isComputerAccessBlock(Block block) {
        return block == TERMINAL.get() || block == WORKSTATION.get();
    }

    public static boolean isComputerNetworkBlock(Block block) {
        return isComputerAccessBlock(block) || block == SERVER_RACK.get() || block == NETWORK_RELAY.get();
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
