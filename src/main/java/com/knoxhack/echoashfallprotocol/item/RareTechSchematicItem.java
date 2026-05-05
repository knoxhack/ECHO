package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Crashbreak elite research reward that decodes into a missing schematic branch.
 */
public class RareTechSchematicItem extends Item {
    public static final int MISSING_CATEGORY_RP = 75;
    public static final int DUPLICATE_ARCHIVE_RP = 125;

    public RareTechSchematicItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (!level.getBlockState(context.getClickedPos()).is(ModBlocks.RESEARCH_LAB.get())) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        decodeAtResearchLab(serverPlayer, context.getItemInHand());
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static DecodeResult decodeAtResearchLab(Player player, ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModItems.RARE_TECH_SCHEMATIC.get())) {
            return new DecodeResult(false, null, 0);
        }

        ResearchData research = ResearchData.get(player);
        SchematicFragmentItem.SchematicType unlockedType = null;
        for (SchematicFragmentItem.SchematicType type : SchematicFragmentItem.SchematicType.values()) {
            String category = categoryKey(type);
            if (!research.hasSchematic(category)) {
                research.unlockSchematic(category);
                unlockedType = type;
                break;
            }
        }

        int rp = unlockedType == null ? DUPLICATE_ARCHIVE_RP : MISSING_CATEGORY_RP;
        int added = research.addPoints(rp);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ResearchData.saveAndSync(serverPlayer, research);
        }

        if (unlockedType == null) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Rare schematic archived. +" + added + " RP")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            player.sendSystemMessage(Component.literal("[ECHO-7] Rare schematic decoded: "
                    + unlockedType.getDisplayName() + ". +" + added + " RP")
                    .withStyle(ChatFormatting.GREEN));
        }

        return new DecodeResult(true, unlockedType, added);
    }

    private static String categoryKey(SchematicFragmentItem.SchematicType type) {
        return type.getDisplayName().toLowerCase(Locale.ROOT);
    }

    public record DecodeResult(boolean consumed, SchematicFragmentItem.SchematicType unlockedType, int researchPoints) {
    }
}
