package com.knoxhack.echorelictech.block;

import com.knoxhack.echorelictech.api.RelicTechApi;
import com.knoxhack.echorelictech.api.event.RelicTechEvents;
import com.knoxhack.echorelictech.api.relic.RelicCondition;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import com.knoxhack.echorelictech.block.entity.PrototypeWorkbenchBlockEntity;
import com.knoxhack.echorelictech.data.RelicDefinitionLoader;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PrototypeWorkbenchBlock extends Block implements EntityBlock {
    public PrototypeWorkbenchBlock(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        var data = stack.get(ModDataComponents.RELIC_DATA.get());
        if (data == null) {
            player.sendSystemMessage(Component.translatable("block.echorelictech.prototype_workbench.insert_relic"));
            return InteractionResult.SUCCESS;
        }

        // Load repair definition from JSON
        var def = RelicDefinitionLoader.get(RelicTechApi.getRelicId(stack));
        var repairInfo = def != null ? def.repair().orElse(null) : null;

        RelicCondition current = data.condition();
        RelicCondition target = null;
        String actionKey = null;

        // Map conditions to actions
        if (current == RelicCondition.DAMAGED) {
            target = RelicCondition.STABILIZED;
            actionKey = "stabilize";
        } else if (current == RelicCondition.STABILIZED) {
            // Check what the player wants: if they hold a containment material, contain; if overclock material, overclock
            if (repairInfo != null && hasMaterials(player, repairInfo.materials())) {
                target = RelicCondition.STABILIZED; // Actually we need a better heuristic
                actionKey = "stabilize";
            } else {
                // Hardcoded fallback for common operations when no repair def exists
                if (player.getInventory().contains(new ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD))) {
                    target = RelicCondition.OVERCLOCKED;
                    actionKey = "overclock";
                } else if (player.getInventory().contains(new ItemStack(net.minecraft.world.item.Items.GLASS))) {
                    target = RelicCondition.CONTAINED;
                    actionKey = "contain";
                }
            }
        } else if (current == RelicCondition.CORRUPTED) {
            target = RelicCondition.DAMAGED;
            actionKey = "purge";
        } else {
            player.sendSystemMessage(Component.translatable("block.echorelictech.prototype_workbench.no_action"));
            return InteractionResult.SUCCESS;
        }

        // If we have repair info, use it for material validation
        if (repairInfo != null && target != null) {
            if (!hasMaterials(player, repairInfo.materials())) {
                player.sendSystemMessage(Component.translatable("block.echorelictech.prototype_workbench.missing_materials"));
                return InteractionResult.SUCCESS;
            }
            consumeMaterials(player, repairInfo.materials());
        } else {
            // Fallback hardcoded material checks for MVP beta
            boolean consumed = switch (actionKey) {
                case "stabilize" -> consumeItem(player, net.minecraft.world.item.Items.IRON_INGOT, 2);
                case "overclock" -> consumeItem(player, net.minecraft.world.item.Items.AMETHYST_SHARD, 1);
                case "contain" -> consumeItem(player, net.minecraft.world.item.Items.GLASS, 1);
                case "purge" -> consumeItem(player, net.minecraft.world.item.Items.DIAMOND, 1);
                default -> false;
            };
            if (!consumed) {
                player.sendSystemMessage(Component.translatable("block.echorelictech.prototype_workbench.missing_materials"));
                return InteractionResult.SUCCESS;
            }
        }

        if (target != null) {
            stack.set(ModDataComponents.RELIC_DATA.get(), data.withCondition(target));
            RelicTechEvents.fireWorkbench(serverPlayer, stack, current, target);
            player.sendSystemMessage(Component.translatable("block.echorelictech.prototype_workbench.success." + actionKey));
        }
        return InteractionResult.SUCCESS;
    }

    private boolean hasMaterials(Player player, java.util.List<com.knoxhack.echorelictech.api.relic.RelicDefinition.RepairMaterial> materials) {
        for (var mat : materials) {
            int needed = mat.count();
            Identifier itemId = Identifier.tryParse(mat.item());
            if (itemId == null) return false;
            var itemOpt = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemId);
            if (itemOpt.isEmpty()) return false;
            var item = itemOpt.get().value();
            int found = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(item)) found += s.getCount();
            }
            if (found < needed) return false;
        }
        return true;
    }

    private void consumeMaterials(Player player, java.util.List<com.knoxhack.echorelictech.api.relic.RelicDefinition.RepairMaterial> materials) {
        for (var mat : materials) {
            Identifier itemId = Identifier.tryParse(mat.item());
            if (itemId == null) continue;
            var itemOpt = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemId);
            if (itemOpt.isEmpty()) continue;
            var item = itemOpt.get().value();
            int count = mat.count();
            for (int i = 0; i < player.getInventory().getContainerSize() && count > 0; i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(item)) {
                    int take = Math.min(count, s.getCount());
                    s.shrink(take);
                    count -= take;
                }
            }
        }
    }

    private boolean consumeItem(Player player, net.minecraft.world.item.Item item, int count) {
        int needed = count;
        for (int i = 0; i < player.getInventory().getContainerSize() && needed > 0; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(item)) {
                int take = Math.min(needed, s.getCount());
                s.shrink(take);
                needed -= take;
            }
        }
        return needed == 0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrototypeWorkbenchBlockEntity(pos, state);
    }
}
