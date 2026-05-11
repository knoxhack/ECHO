package com.knoxhack.echolens.provider;

import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.EntityLensProvider;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;

public enum EntityStatsProvider implements EntityLensProvider {
    INSTANCE;

    @Override
    public Identifier id() {
        return EchoLens.id("entity_stats");
    }

    @Override
    public int priority() {
        return 120;
    }

    @Override
    public LensDataCategory category() {
        return LensDataCategory.ENTITY;
    }

    @Override
    public List<LensInfoSection> inspectEntity(LensContext context, Entity entity) {
        List<LensInfoRow> rows = new ArrayList<>();
        if (entity instanceof LivingEntity living) {
            rows.add(LensInfoRow.of("Health", health(living), "♥", healthTone(living), LensVisibility.COMPACT));
            rows.add(LensInfoRow.of("Armor", Integer.toString(living.getArmorValue()), "◆", LensTone.INFO,
                    LensVisibility.EXPANDED));
            rows.add(LensInfoRow.of("Effects", effects(living), "✚", LensTone.NEUTRAL, LensVisibility.EXPANDED));
            rows.add(LensInfoRow.of("Danger", danger(living), "▲", dangerTone(living), LensVisibility.COMPACT));
        } else {
            rows.add(LensInfoRow.of("Danger", "None detected", "▲", LensTone.GOOD, LensVisibility.COMPACT));
        }
        if (entity instanceof TamableAnimal tameable) {
            rows.add(LensInfoRow.of("Tame", tameable.isTame() ? "Tamed" : "Untamed", "◇",
                    tameable.isTame() ? LensTone.GOOD : LensTone.MUTED, LensVisibility.EXPANDED));
            UUID owner = ownerUuid(tameable);
            rows.add(LensInfoRow.of("Owner", owner == null ? "None" : owner.toString(), "◎",
                    LensTone.MUTED, LensVisibility.DEEP));
        }
        return List.of(LensInfoSection.of(EchoLens.id("section/entity"), LensDataCategory.ENTITY, "Entity",
                "⬡", LensTone.NEUTRAL, LensVisibility.COMPACT, rows));
    }

    private static String health(LivingEntity living) {
        return String.format(Locale.ROOT, "%.1f / %.1f", living.getHealth(), living.getMaxHealth());
    }

    private static LensTone healthTone(LivingEntity living) {
        float ratio = living.getMaxHealth() <= 0.0F ? 0.0F : living.getHealth() / living.getMaxHealth();
        if (ratio <= 0.25F) {
            return LensTone.DANGER;
        }
        if (ratio <= 0.5F) {
            return LensTone.WARNING;
        }
        return LensTone.GOOD;
    }

    private static String effects(LivingEntity living) {
        List<String> effects = new ArrayList<>();
        for (MobEffectInstance effect : living.getActiveEffects()) {
            try {
                effects.add(effect.getEffect().value().getDisplayName().getString());
            } catch (RuntimeException exception) {
                effects.add(effect.getDescriptionId());
            }
            if (effects.size() >= 4) {
                break;
            }
        }
        return effects.isEmpty() ? "None" : String.join(", ", effects);
    }

    private static String danger(LivingEntity living) {
        if (living instanceof Enemy) {
            return living.getArmorValue() > 8 || living.getMaxHealth() > 35.0F ? "High" : "Hostile";
        }
        return living.getMaxHealth() > 80.0F ? "Massive" : "Low";
    }

    private static LensTone dangerTone(LivingEntity living) {
        if (living instanceof Enemy) {
            return living.getArmorValue() > 8 || living.getMaxHealth() > 35.0F ? LensTone.DANGER : LensTone.WARNING;
        }
        return living.getMaxHealth() > 80.0F ? LensTone.WARNING : LensTone.GOOD;
    }

    private static UUID ownerUuid(TamableAnimal tameable) {
        for (String methodName : List.of("getOwnerUUID", "getOwnerUuid")) {
            try {
                Object value = tameable.getClass().getMethod(methodName).invoke(tameable);
                if (value instanceof UUID uuid) {
                    return uuid;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }
}
