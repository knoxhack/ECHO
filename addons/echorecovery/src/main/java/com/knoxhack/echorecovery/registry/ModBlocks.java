package com.knoxhack.echorecovery.registry;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.block.GraveBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoRecovery.MODID);
    private static final List<DeferredBlock<Block>> BLOCK_ITEMS = new ArrayList<>();

    public static final DeferredBlock<Block> GRAVE = grave("grave", GraveBlock.GraveVariant.GRAVE);
    public static final DeferredBlock<Block> DEATH_CACHE = grave("death_cache", GraveBlock.GraveVariant.DEATH_CACHE);
    public static final DeferredBlock<Block> RECOVERY_CACHE = grave("recovery_cache", GraveBlock.GraveVariant.RECOVERY_CACHE);
    public static final DeferredBlock<Block> SOUL_URN = grave("soul_urn", GraveBlock.GraveVariant.SOUL_URN);
    public static final DeferredBlock<Block> VOID_CACHE = grave("void_cache", GraveBlock.GraveVariant.VOID_CACHE);

    private ModBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static List<DeferredBlock<Block>> blockItems() {
        return List.copyOf(BLOCK_ITEMS);
    }

    private static DeferredBlock<Block> grave(String name, GraveBlock.GraveVariant variant) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(name, p -> new GraveBlock(variant, p),
            p -> p.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE).noOcclusion());
        BLOCK_ITEMS.add(block);
        return block;
    }
}
