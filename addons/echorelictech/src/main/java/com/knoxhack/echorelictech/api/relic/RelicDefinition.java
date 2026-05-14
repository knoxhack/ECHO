package com.knoxhack.echorelictech.api.relic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.List;
import java.util.Optional;

public record RelicDefinition(
        Identifier id,
        RelicTier tier,
        RelicCategory category,
        String origin,
        RelicCondition defaultCondition,
        int cooldownTicks,
        int instabilityCost,
        int nullChargeCost,
        List<RelicRiskType> riskTypes,
        Identifier failureTable,
        Optional<TerminalInfo> terminal,
        Optional<LensInfo> lens,
        Optional<RepairInfo> repair,
        boolean enabled
) {
    private static final Codec<Identifier> IDENTIFIER_CODEC = Codec.STRING.xmap(Identifier::parse, Identifier::toString);

    public static final Codec<RelicDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IDENTIFIER_CODEC.fieldOf("id").forGetter(RelicDefinition::id),
            RelicTier.CODEC.fieldOf("tier").forGetter(RelicDefinition::tier),
            RelicCategory.CODEC.fieldOf("category").forGetter(RelicDefinition::category),
            Codec.STRING.fieldOf("origin").forGetter(RelicDefinition::origin),
            RelicCondition.CODEC.optionalFieldOf("defaultCondition", RelicCondition.DAMAGED).forGetter(RelicDefinition::defaultCondition),
            Codec.INT.optionalFieldOf("cooldownTicks", 0).forGetter(RelicDefinition::cooldownTicks),
            Codec.INT.optionalFieldOf("instabilityCost", 0).forGetter(RelicDefinition::instabilityCost),
            Codec.INT.optionalFieldOf("nullChargeCost", 0).forGetter(RelicDefinition::nullChargeCost),
            RelicRiskType.CODEC.listOf().optionalFieldOf("riskTypes", List.of()).forGetter(RelicDefinition::riskTypes),
            IDENTIFIER_CODEC.fieldOf("failureTable").forGetter(RelicDefinition::failureTable),
            TerminalInfo.CODEC.optionalFieldOf("terminal").forGetter(RelicDefinition::terminal),
            LensInfo.CODEC.optionalFieldOf("lens").forGetter(RelicDefinition::lens),
            RepairInfo.CODEC.optionalFieldOf("repair").forGetter(RelicDefinition::repair),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(RelicDefinition::enabled)
    ).apply(instance, RelicDefinition::new));

    public record TerminalInfo(String summary, String warning) {
        public static final Codec<TerminalInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("summary").forGetter(TerminalInfo::summary),
                Codec.STRING.optionalFieldOf("warning", "").forGetter(TerminalInfo::warning)
        ).apply(instance, TerminalInfo::new));
    }

    public record LensInfo(String compact, List<String> deepScan) {
        public static final Codec<LensInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("compact").forGetter(LensInfo::compact),
                Codec.STRING.listOf().optionalFieldOf("deepScan", List.of()).forGetter(LensInfo::deepScan)
        ).apply(instance, LensInfo::new));
    }

    public record RepairInfo(List<RepairMaterial> materials) {
        public static final Codec<RepairInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RepairMaterial.CODEC.listOf().fieldOf("materials").forGetter(RepairInfo::materials)
        ).apply(instance, RepairInfo::new));
    }

    public record RepairMaterial(String item, int count) {
        public static final Codec<RepairMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(RepairMaterial::item),
                Codec.INT.fieldOf("count").forGetter(RepairMaterial::count)
        ).apply(instance, RepairMaterial::new));
    }
}
