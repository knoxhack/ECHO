package com.knoxhack.echoashfallprotocol.client;

import com.knoxhack.echoashfallprotocol.block.WorkshopBlock;
import com.knoxhack.echoashfallprotocol.machine.MachineState;
import com.knoxhack.echoashfallprotocol.machine.MachineStateProvider;
import com.knoxhack.echoashfallprotocol.research.PerkEffectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Locale;

/**
 * Client-side machine tooltip feedback for workshop and operator bonuses.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class MachineStateTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof MachineStateProvider provider) || !provider.showStateInTooltip()) {
            return;
        }

        MachineState machineState = provider.getMachineState(mc.level, pos, state);
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§8[Machine Status]"));
        tooltip.add(machineState.getTooltip());
        tooltip.add(Component.literal("§7Fix: §f" + getRecoveryHint(machineState)));

        if (WorkshopBlock.isInWorkshop(mc.level, pos)) {
            tooltip.add(Component.literal("§6Workshop Boosted§r +20% speed, -10% power"));
        }

        float perkSpeed = PerkEffectHandler.getMachineSpeedMultiplier(mc.player);
        if (perkSpeed > 1.0F) {
            tooltip.add(Component.literal(
                "§bOperator Perk Boosted§r x" + String.format(Locale.ROOT, "%.2f", perkSpeed)
            ));
        }

        if (machineState == MachineState.PROCESSING) {
            tooltip.add(Component.literal("§7Click to view progress").withStyle(net.minecraft.ChatFormatting.ITALIC));
        }
    }

    private static String getRecoveryHint(MachineState state) {
        return switch (state) {
            case IDLE -> "add valid input or check this machine's recipe";
            case PROCESSING -> "wait, or add speed/power support";
            case UNPOWERED -> "place generator, battery, or cable nearby";
            case JAMMED -> "repair wear and clear blocked inventories";
            case OFFLINE -> "confirm placement, activation, and ownership";
            case BLOCKED -> "empty output or connect item routing";
            case BROWNOUT -> "add fuel, charge storage, or reduce machine demand";
            case BOTTLENECK -> "upgrade the cable path or reduce FE/t demand";
            case PRIORITY_PAUSED -> "change Load Distributor mode or charge reserves";
            case UNSTABLE -> "repair before running rare materials";
            case GENERATING -> "connect storage or nearby machines";
        };
    }
}
