package com.knoxhack.echoorbitalremnants.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EchoDefenseDroneEntity extends Vex {
    public EchoDefenseDroneEntity(EntityType<? extends Vex> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 100 == 0 && getTarget() instanceof Player player && distanceToSqr(player) < 64.0D) {
            player.sendSystemMessage(Component.literal("ECHO-7 // Defense drone scan lock detected."));
        }
    }
}
