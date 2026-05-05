package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import com.knoxhack.echoashfallprotocol.survival.MutationData;
import com.knoxhack.echoashfallprotocol.survival.SurvivalData;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.event.SmartEventData;
import com.knoxhack.echoashfallprotocol.faction.AshfallFactionContractData;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import com.knoxhack.echoashfallprotocol.survival.ColdData;
import com.knoxhack.echoashfallprotocol.survival.CombatData;
import com.knoxhack.echoashfallprotocol.survival.PlayerTechTracker;
import com.knoxhack.echoashfallprotocol.world.FieldOpsData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EchoAshfallProtocol.MODID);

    public static final Supplier<AttachmentType<SurvivalData>> SURVIVAL_DATA = ATTACHMENT_TYPES.register(
            "survival_data",
            () -> AttachmentType.<SurvivalData>serializable(SurvivalData::new)
                    .sync((holder, player) -> holder == player, SurvivalData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<CombatData>> COMBAT_DATA = ATTACHMENT_TYPES.register(
            "combat_data",
            () -> AttachmentType.<CombatData>serializable(CombatData::new)
                    .build()
    );

    public static final Supplier<AttachmentType<MutationData>> MUTATION_DATA = ATTACHMENT_TYPES.register(
            "mutation_data",
            () -> AttachmentType.<MutationData>serializable(MutationData::new)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<QuestData>> QUEST_DATA = ATTACHMENT_TYPES.register(
            "quest_data",
            () -> AttachmentType.<QuestData>serializable(QuestData::new)
                    .sync((holder, player) -> holder == player, QuestData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<SmartEventData>> SMART_EVENT_DATA = ATTACHMENT_TYPES.register(
            "smart_event_data",
            () -> AttachmentType.<SmartEventData>serializable(SmartEventData::new)
                    .build()
    );

    public static final Supplier<AttachmentType<ResearchData>> RESEARCH_DATA = ATTACHMENT_TYPES.register(
            "research_data",
            () -> AttachmentType.<ResearchData>serializable(ResearchData::new)
                    .sync((holder, player) -> holder == player, ResearchData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<ColdData>> COLD_DATA = ATTACHMENT_TYPES.register(
            "cold_data",
            () -> AttachmentType.<ColdData>serializable(ColdData::new)
                    .sync((holder, player) -> holder == player, ColdData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<PlayerTechTracker.PlayerTechData>> PLAYER_TECH_DATA = ATTACHMENT_TYPES.register(
            "player_tech_data",
            () -> AttachmentType.<PlayerTechTracker.PlayerTechData>serializable(PlayerTechTracker.PlayerTechData::new)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<com.knoxhack.echoashfallprotocol.fasttravel.RadioNetwork>> RADIO_NETWORK = ATTACHMENT_TYPES.register(
            "radio_network",
            () -> AttachmentType.<com.knoxhack.echoashfallprotocol.fasttravel.RadioNetwork>serializable(com.knoxhack.echoashfallprotocol.fasttravel.RadioNetwork::new)
                    .copyOnDeath()
                    .build()
    );

    public static final java.util.function.Supplier<AttachmentType<com.knoxhack.echoashfallprotocol.data.MigrationData>> MIGRATION_DATA = ATTACHMENT_TYPES.register(
            "migration_data",
            () -> AttachmentType.<com.knoxhack.echoashfallprotocol.data.MigrationData>builder(() -> new com.knoxhack.echoashfallprotocol.data.MigrationData())
                    .serialize(com.knoxhack.echoashfallprotocol.data.MigrationData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<PostNexusData>> POST_NEXUS_DATA = ATTACHMENT_TYPES.register(
            "post_nexus_data",
            () -> AttachmentType.<PostNexusData>serializable(PostNexusData::new)
                    .sync((holder, player) -> holder == player, PostNexusData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<FieldOpsData>> FIELD_OPS_DATA = ATTACHMENT_TYPES.register(
            "field_ops_data",
            () -> AttachmentType.<FieldOpsData>serializable(FieldOpsData::new)
                    .copyOnDeath()
                    .build()
    );
    
    // --- Deeper Factions System ---
    
    public static final Supplier<AttachmentType<com.knoxhack.echoashfallprotocol.faction.FactionDiplomacy>> FACTION_DIPLOMACY = ATTACHMENT_TYPES.register(
            "faction_diplomacy",
            () -> AttachmentType.<com.knoxhack.echoashfallprotocol.faction.FactionDiplomacy>serializable(
                    com.knoxhack.echoashfallprotocol.faction.FactionDiplomacy::new)
                    .sync((holder, player) -> holder == player, com.knoxhack.echoashfallprotocol.faction.FactionDiplomacy.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );
    
    public static final Supplier<AttachmentType<com.knoxhack.echoashfallprotocol.echo.EchoIntel>> ECHO_INTEL = ATTACHMENT_TYPES.register(
            "echo_intel",
            () -> AttachmentType.<com.knoxhack.echoashfallprotocol.echo.EchoIntel>serializable(
                    com.knoxhack.echoashfallprotocol.echo.EchoIntel::new)
                    .sync((holder, player) -> holder == player, com.knoxhack.echoashfallprotocol.echo.EchoIntel.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );
    
    public static final Supplier<AttachmentType<com.knoxhack.echoashfallprotocol.faction.FactionTerritory>> FACTION_TERRITORY = ATTACHMENT_TYPES.register(
            "faction_territory",
            () -> AttachmentType.<com.knoxhack.echoashfallprotocol.faction.FactionTerritory>serializable(
                    com.knoxhack.echoashfallprotocol.faction.FactionTerritory::new)
                    .sync((holder, player) -> holder == player, com.knoxhack.echoashfallprotocol.faction.FactionTerritory.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<AshfallFactionContractData>> ASHFALL_FACTION_CONTRACT_DATA = ATTACHMENT_TYPES.register(
            "ashfall_faction_contract_data",
            () -> AttachmentType.<AshfallFactionContractData>serializable(AshfallFactionContractData::new)
                    .sync((holder, player) -> holder == player, AshfallFactionContractData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );
}
