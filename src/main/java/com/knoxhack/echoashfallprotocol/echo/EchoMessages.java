package com.knoxhack.echoashfallprotocol.echo;

import java.util.*;

/**
 * Context-aware message bank for ECHO-7.
 * Messages are selected based on player state and environment.
 */
public class EchoMessages {

    public enum Context {
        LOW_HEALTH, LOW_FILTER, NO_FILTER, HIGH_RADIATION,
        LOW_HYDRATION, STORM, NIGHT, MUTATION_GAINED,
        GENERATOR_FAILED, FIRST_JOIN, IDLE, MUTAGEN_USED, RADAWAY_USED,
        POWER_NODE_ACTIVATED, NEXUS_CORE_FOUND, SCOUT_DRONE_DEPLOYED,
        BIOME_WASTELAND, BIOME_TOXIC_SWAMP, BIOME_RUINED_CITY,
        ENTITY_DRONE, ENTITY_SCAVENGER, ENTITY_MUTANT,
        ENTITY_GLOWING_GHOUL, ENTITY_CITY_STALKER, ENTITY_TOXIC_SLIME,
        ENTITY_RUST_WALKER, ENTITY_ASH_WRAITH, ENTITY_STEAM_WRAITH, ENTITY_MUTATED_CRAWLER,
        DISCOVERY_RARE_TECH, STABILITY_GLITCH,
        // New contexts for v1.3+ content
        BIOME_CRYOGENIC, BIOME_CRASH_ZONE, BIOME_INDUSTRIAL_RUINS,
        BIOME_RADIATION_ZONE, BIOME_NEXUS_SCAR,
        RADIO_STATION_ACTIVATED, RESEARCH_UNLOCKED,
        FACTION_REPUTATION_POSITIVE, FACTION_REPUTATION_NEGATIVE,
        BANDAGE_USED, STIMPAK_USED,
        // Phase transition hints
        PHASE_TRANSITION_BIO_TO_GEO, DENSE_ALLOY_NEEDED
    }

    private static final Map<Context, List<String>> MESSAGES = new EnumMap<>(Context.class);

    static {
        MESSAGES.put(Context.FIRST_JOIN, List.of(
                "§b[ECHO-7]§r Systems rebooting... Crash impact damaged primary systems. Running diagnostic...",
                "§b[ECHO-7]§r Survivor detected. Vital signs: critical but stable. I am ECHO-7, your emergency AI assistant.",
                "§b[ECHO-7]§r Local air is stable near the crash site. Secure water and tools before scouting hazard zones."
        ));

        MESSAGES.put(Context.LOW_HEALTH, List.of(
                "§b[ECHO-7]§r Warning: Vital signs deteriorating. Seek shelter and medical supplies.",
                "§b[ECHO-7]§r Structural damage to host detected. Recommend immediate triage.",
                "§b[ECHO-7]§r Critical status. Reducing non-essential system monitoring to conserve power."
        ));

        MESSAGES.put(Context.LOW_FILTER, List.of(
                "§b[ECHO-7]§r Filter reserve is low. Replace it before the next toxic route.",
                "§b[ECHO-7]§r Gas mask cartridge is nearly spent. Retreat or reload before deeper exposure.",
                "§b[ECHO-7]§r Filter charge below 15%. That is enough for a short exit, not another expedition.",
                "§b[ECHO-7]§r Filtration margin is thin. Install a cartridge before entering another hazard pocket."
        ));

        MESSAGES.put(Context.NO_FILTER, List.of(
                "§b[ECHO-7]§r Toxic pocket detected. Equip a mask with filter charge or leave the zone.",
                "§b[ECHO-7]§r Unfiltered exposure rising. You have seconds to mask up or retreat.",
                "§b[ECHO-7]§r Hazard air confirmed. Scrubber field, mask filter, or distance will stabilize this."
        ));

        MESSAGES.put(Context.HIGH_RADIATION, List.of(
                "§b[ECHO-7]§r Radiation levels elevated. Prolonged exposure may cause... unexpected changes.",
                "§b[ECHO-7]§r Geiger readings spiking. Recommend evacuation from contaminated zone.",
                "§b[ECHO-7]§r WARNING: Radiation threshold approaching mutation trigger. Proceed with caution.",
                "§b[ECHO-7]§r Isotope saturation at dangerous levels. Use Rad-Away or reach a clean zone immediately.",
                "§b[ECHO-7]§r Cellular mutation window active. Your DNA is being rewritten. I hope you're comfortable with that."
        ));

        MESSAGES.put(Context.LOW_HYDRATION, List.of(
                "§b[ECHO-7]§r Water reserve is low. Drink before the next expedition leg.",
                "§b[ECHO-7]§r Fluid levels critical. Return to stored water or use dirty water as an emergency fallback."
        ));

        MESSAGES.put(Context.STORM, List.of(
                "§b[ECHO-7]§r Atmospheric disturbance detected. Visibility and route safety will degrade.",
                "§b[ECHO-7]§r Seek shelter if supplies are thin. Storm movement makes expeditions harder.",
                "§b[ECHO-7]§r Acid precipitation detected. Exposed surfaces will corrode. Find cover.",
                "§b[ECHO-7]§r Storm front confirmed. Roof cover is the cleanest answer; filters are for toxic zones."
        ));

        MESSAGES.put(Context.NIGHT, List.of(
                "§b[ECHO-7]§r Solar input dropping. Nocturnal threat levels increasing. Secure perimeter.",
                "§b[ECHO-7]§r Night cycle detected. Hostile entity activity projected to increase 300%.",
                "§b[ECHO-7]§r Darkness is not your friend here. Thermal signatures are multiplying on all channels.",
                "§b[ECHO-7]§r Night protocol active. Recommend torchlight and elevated ground. They prefer the shadows."
        ));

        MESSAGES.put(Context.MUTAGEN_USED, List.of(
                "§b[ECHO-7]§r Warning: Unstable mutagenic compound administered. Massive cellular reconfiguration imminent.",
                "§b[ECHO-7]§r Chemical shock detected. Attempting to isolate altered DNA strands. Stand by.",
                "§b[ECHO-7]§r Why would you ingest that? Genetic breakdown accelerating..."
        ));

        MESSAGES.put(Context.RADAWAY_USED, List.of(
                "§b[ECHO-7]§r Iodine-based isotope flush initiated. Radiation levels decreasing.",
                "§b[ECHO-7]§r Cellular cleansing complete. Commencing minor tissue regeneration."
        ));

        MESSAGES.put(Context.BANDAGE_USED, List.of(
                "§b[ECHO-7]§r Wound dressed. Coagulant active. Monitor for infection.",
                "§b[ECHO-7]§r Trauma site stabilized. Recommend rest if possible.",
                "§b[ECHO-7]§r Field dressing applied. Poison inhibitor active."
        ));

        MESSAGES.put(Context.STIMPAK_USED, List.of(
                "§b[ECHO-7]§r Stimulant administered. Adrenaline spike detected. Duration: limited.",
                "§b[ECHO-7]§r Combat stim active. Performance enhancement in effect. Crash imminent.",
                "§b[ECHO-7]§r Chemical boost registered. I advise against habitual use."
        ));

        MESSAGES.put(Context.MUTATION_GAINED, List.of(
                "§b[ECHO-7]§r BIOLOGICAL ALERT: Genetic anomaly detected in host DNA. Mutation event logged.",
                "§b[ECHO-7]§r Interesting... your cellular structure is adapting to the radiation. Side effects probable.",
                "§b[ECHO-7]§r Mutation registered. Your biology is changing. I recommend monitoring for side effects."
        ));

        MESSAGES.put(Context.GENERATOR_FAILED, List.of(
                "§b[ECHO-7]§r Power fluctuation detected. Generator failure. Manual restart required.",
                "§b[ECHO-7]§r Unstable power grid event. Generator offline. Right-click unit to restart.",
                "§b[ECHO-7]§r Energy supply interrupted. Machines are going dark. Add fuel and restart the generator."
        ));

        MESSAGES.put(Context.POWER_NODE_ACTIVATED, List.of(
                "§b[ECHO-7]§r Power node reactivated. Grid integrity improving. Locate more nodes to restore full coverage.",
                "§b[ECHO-7]§r Node online. Energy signature added to the grid. The Nexus Core may be responding.",
                "§b[ECHO-7]§r Grid node active. I'm detecting a faint pulse from deeper in the ruins. Keep restoring nodes."
        ));

        MESSAGES.put(Context.NEXUS_CORE_FOUND, List.of(
                "§b[ECHO-7]§r ...I... I'm detecting the Nexus Core. It's still active. This changes everything.",
                "§b[ECHO-7]§r WARNING: Full Nexus Core signal acquired. It knows you're here. Prepare for elevated threat response.",
                "§b[ECHO-7]§r Nexus Core located. The system that caused the Gridfall is still running. What you do next will determine this world's fate."
        ));

        MESSAGES.put(Context.SCOUT_DRONE_DEPLOYED, List.of(
                "§b[ECHO-7]§r Scout drone deployed. Unit will follow your last known position and flag resources.",
                "§b[ECHO-7]§r Drone operational. Basic salvage protocol engaged. Keep it away from high-threat zones.",
                "§b[ECHO-7]§r Drone uplink established. Automation module active. Don't lose it — repairs are expensive."
        ));

        MESSAGES.put(Context.IDLE, List.of(
                "§b[ECHO-7]§r Scanning area... No immediate threats detected. Continue operations.",
                "§b[ECHO-7]§r Status: Nominal. Recommend continuing salvage operations.",
                "§b[ECHO-7]§r Systems stable. Don't get too comfortable — this place has a way of changing quickly.",
                "§b[ECHO-7]§r I'm detecting faint signals from the northeast. Could be worth investigating.",
                "§b[ECHO-7]§r Running efficiency analysis... Your survival odds have improved 12% since landing.",
                "§b[ECHO-7]§r My companion drone uplink is still degraded. Repair it when you find the components.",
                "§b[ECHO-7]§r The Nexus was designed to optimize, not destroy. Something went very wrong in its final cycle.",
                "§b[ECHO-7]§r Keep water stocked and filters ready for toxic routes. Base air is currently stable."
        ));

        MESSAGES.put(Context.BIOME_WASTELAND, List.of(
                "§b[ECHO-7]§r Entering the Great Wasteland. Geiger readings are... unsettling. Watch your step.",
                "§b[ECHO-7]§r Barren terrain confirmed. Long-range scans show nothing but dust and history.",
                "§b[ECHO-7]§r This used to be farmland. The soil hasn't recovered since the Gridfall.",
                "§b[ECHO-7]§r Open terrain — you're visible from a long way out. Threat vectors are increasing."
        ));

        MESSAGES.put(Context.BIOME_TOXIC_SWAMP, List.of(
                "§b[ECHO-7]§r Atmospheric density increasing. These swamps are a breeding ground for mutagenic spores. Tighten your mask seal.",
                "§b[ECHO-7]§r Warning: Bio-hazard levels elevated. The local flora is... significantly altered.",
                "§b[ECHO-7]§r Liquid toxicity off the charts. Do not consume standing water in this zone under any circumstances.",
                "§b[ECHO-7]§r Spore density critical. Carry a charged mask before entering marked hazard pockets."
        ));

        MESSAGES.put(Context.BIOME_RUINED_CITY, List.of(
                "§b[ECHO-7]§r Navigating urban ruins. I'm detecting multiple scavenger frequencies. Keep your weapon ready.",
                "§b[ECHO-7]§r These structures used to be skyscrapers. Now they're just skeletons of the Old World.",
                "§b[ECHO-7]§r High ambush probability in this environment. City Stalkers use the vertical space. Watch above.",
                "§b[ECHO-7]§r Pre-Gridfall infrastructure intact in sections. Valuable tech may be buried in the lower levels."
        ));

        MESSAGES.put(Context.ENTITY_DRONE, List.of(
                "§b[ECHO-7]§r Enemy drone detected. Identification: Rogue Scout Unit. It's calling for backup!",
                "§b[ECHO-7]§r Hostile machine signature locked. Neutralize it before it uploads our coordinates.",
                "§b[ECHO-7]§r Rogue unit detected. It shares my chassis design — but not my purpose. Destroy it."
        ));

        MESSAGES.put(Context.ENTITY_SCAVENGER, List.of(
                "§b[ECHO-7]§r Scavengers spotted. They aren't interested in talking, only in your tech. Engagement recommended.",
                "§b[ECHO-7]§r Human signatures detected. Threat assessment: Dangerous. They've lived out here too long.",
                "§b[ECHO-7]§r Scavenger Bandit in range. They're armed and desperate. Don't give them the initiative."
        ));

        MESSAGES.put(Context.ENTITY_MUTANT, List.of(
                "§b[ECHO-7]§r Biological anomaly closing in. It doesn't follow known metabolic laws. Stay at range!",
                "§b[ECHO-7]§r That... thing... used to be biological. Now it's mostly radiation and rage.",
                "§b[ECHO-7]§r Irradiated lifeform detected. Do not let it close the distance — the contamination it carries is contagious.",
                "§b[ECHO-7]§r Unknown mutant variant. Recommend caution. Their bite carries residual isotope contamination."
        ));

        MESSAGES.put(Context.DISCOVERY_RARE_TECH, List.of(
                "§b[ECHO-7]§r High-value component acquired! I can use this to optimize our power grid significantly.",
                "§b[ECHO-7]§r Interesting find. This tech predates the Gridfall. I'll begin a deep scan immediately."
        ));

        MESSAGES.put(Context.STABILITY_GLITCH, List.of(
                "§b[E§kC§bHO-7]§r Sy§kste§rm erro§kr... r-r-radiation interfe§kren§rce detected.",
                "§b[ECHO-7]§r I-I see... §kthe sky is bleeding§r... Warning: Sensor buffer overflow.",
                "§b[ECHO-7]§r §kERROR 0x7F4§r... Log data corrupted. I... I think I'm losing the signal..."
        ));

        // New entity encounters
        MESSAGES.put(Context.ENTITY_GLOWING_GHOUL, List.of(
                "§b[ECHO-7]§r WARNING: Glowing Ghoul detected. It radiates an aura of ionized particles. Close proximity will accelerate your contamination.",
                "§b[ECHO-7]§r That creature is a walking reactor. Keep distance and attack from range — its death pulse is dangerous."
        ));

        MESSAGES.put(Context.ENTITY_CITY_STALKER, List.of(
                "§b[ECHO-7]§r City Stalker identified. Stealth-adapted predator. It may already have your position. Watch your flanks.",
                "§b[ECHO-7]§r Threat detected: City Stalker. Fast, silent, uses blindness attacks. Don't let it get behind you."
        ));

        MESSAGES.put(Context.ENTITY_TOXIC_SLIME, List.of(
                "§b[ECHO-7]§r Toxic Slime specimen identified. It leaves corrosive residue on terrain. Avoid standing in its trail.",
                "§b[ECHO-7]§r Biological hazard: Toxic Slime. It will apply poison on contact. I'd recommend a ranged approach."
        ));

        MESSAGES.put(Context.ENTITY_RUST_WALKER, List.of(
                "§b[ECHO-7]§r Rust Walker detected. Animated scrap construct — slow but heavily armored. High-damage hits. Keep moving.",
                "§b[ECHO-7]§r WARNING: Rust Walker closing. Its armor is dense alloy composite. Don't fight it in a narrow space."
        ));

        MESSAGES.put(Context.ENTITY_ASH_WRAITH, List.of(
                "§b[ECHO-7]§r Ash Wraith incoming. Non-corporeal entity. Contact disrupts vision and muscle response.",
                "§b[ECHO-7]§r Threat detected: Ash Wraith. Keep distance; it punishes close-range panic."
        ));

        MESSAGES.put(Context.ENTITY_STEAM_WRAITH, List.of(
                "§b[ECHO-7]§r Steam Wraith detected near thermal vents. Contact causes burn damage and disorientation. Maintain distance.",
                "§b[ECHO-7]§r WARNING: Steam Wraith. Superheated entity — its touch applies burn and nausea. Find cooler ground."
        ));

        MESSAGES.put(Context.ENTITY_MUTATED_CRAWLER, List.of(
                "§b[ECHO-7]§r Mutated Crawler spotted. Extremely fast and agile. It leaps — expect unpredictable movement patterns.",
                "§b[ECHO-7]§r Threat detected: Mutated Crawler. Low health but rapid. It will outpace you. Stand your ground."
        ));

        // New contexts for v1.3+ content
        MESSAGES.put(Context.BIOME_CRYOGENIC, List.of(
                "§b[ECHO-7]§r Entering cryogenic zone. Temperature critical. Thermal insulation mandatory.",
                "§b[ECHO-7]§r Warning: Climate weapon test site. Extreme cold hazard. Seek heat sources.",
                "§b[ECHO-7]§r Frozen wasteland detected. Cryo-containers may hold preserved technology.",
                "§b[ECHO-7]§r Sub-zero readings confirmed. Exposed circuitry and biological tissue both at risk.",
                "§b[ECHO-7]§r Cryogenic field active. Pre-Gridfall climate experiment gone unchecked for years."
        ));

        MESSAGES.put(Context.BIOME_CRASH_ZONE, List.of(
                "§b[ECHO-7]§r Crash zone confirmed. Impact glass, ash, and reactor debris all register above safe exposure limits.",
                "§b[ECHO-7]§r Scorched terrain ahead. The Gridfall did not end here; it left pieces still burning.",
                "§b[ECHO-7]§r Crash debris signatures detected. Salvage value high, radiation variance unstable."
        ));

        MESSAGES.put(Context.BIOME_INDUSTRIAL_RUINS, List.of(
                "§b[ECHO-7]§r Industrial ruins detected. Expect dense alloy salvage, machine hazards, and old automation still trying to work.",
                "§b[ECHO-7]§r Factory district entered. The machines outlived their operators. Treat every powered system as hostile until proven otherwise.",
                "§b[ECHO-7]§r Pre-Fall fabrication zone confirmed. This is where the old world learned to build faster than it could think."
        ));

        MESSAGES.put(Context.BIOME_RADIATION_ZONE, List.of(
                "§b[ECHO-7]§r Radiation zone entered. RadAway, scrubber routes, and exit timing are now mission-critical.",
                "§b[ECHO-7]§r Fallout crust underfoot. Power Node components and dense alloys are likely, but so are high-dose lifeforms.",
                "§b[ECHO-7]§r Geiger readings have exceeded advisory limits. The Remnants used places like this as proving grounds."
        ));

        MESSAGES.put(Context.BIOME_NEXUS_SCAR, List.of(
                "§5[ECHO-7]§r Nexus Scar detected. The Core's influence is not historical here. It is active.",
                "§5[ECHO-7]§r Anomalous soil composition confirmed. The Gridfall left a wound, and the wound is still transmitting.",
                "§5[ECHO-7]§r Reality stability degraded. If you hear another voice in the signal, do not answer it."
        ));

        MESSAGES.put(Context.RADIO_STATION_ACTIVATED, List.of(
                "§b[ECHO-7]§r Radio station online! Fast travel network expanded. Scout Drone deployment ready.",
                "§b[ECHO-7]§r Relay station repaired. Signal strength optimal. You can now fast travel to this location.",
                "§b[ECHO-7]§r Communications array active. Network coverage improved. ECHO-7 can guide you further."
        ));

        MESSAGES.put(Context.RESEARCH_UNLOCKED, List.of(
                "§b[ECHO-7]§r Schematic decoded! New technology available. Check Research tab for details.",
                "§b[ECHO-7]§r Knowledge fragment integrated. Research tier advanced. Perks now accessible.",
                "§b[ECHO-7]§r Old World technology understood. Blueprints added to your archive."
        ));

        MESSAGES.put(Context.FACTION_REPUTATION_POSITIVE, List.of(
                "§b[ECHO-7]§r Faction reputation improved. New trade opportunities available. Quests unlocked.",
                "§b[ECHO-7]§r They are beginning to trust you. Exclusive rewards now accessible.",
                "§b[ECHO-7]§r Your actions have been noted. This faction considers you an ally."
        ));

        MESSAGES.put(Context.FACTION_REPUTATION_NEGATIVE, List.of(
                "§b[ECHO-7]§r Warning: Faction reputation declining. Hostile encounters increasing.",
                "§b[ECHO-7]§r Caution: You are making enemies. Their territory is now dangerous for you.",
                "§b[ECHO-7]§r Alert: Faction considers you a threat. Expect aggression in their zones."
        ));

        // Phase transition and progression hints
        MESSAGES.put(Context.PHASE_TRANSITION_BIO_TO_GEO, List.of(
                "§b[ECHO-7]§r Surface resources depleted. Advanced machinery requires Dense Alloy - explore Military Vaults for acquisition.",
                "§b[ECHO-7]§r Biological adaptation complete. Geological extraction awaits. Military Vaults contain the Dense Alloy you need.",
                "§b[ECHO-7]§r Time to look deeper. The Substrate Grinder requires Dense Alloy components found in Military Vaults."
        ));

        MESSAGES.put(Context.DENSE_ALLOY_NEEDED, List.of(
                "§b[ECHO-7]§r Dense Alloy required for Tier 4 machinery. Military Vaults are your best source.",
                "§b[ECHO-7]§r High-grade alloys detected in Military Vault POIs. Dense Alloy is essential for progression.",
                "§b[ECHO-7]§r Without Dense Alloy, you cannot build the Substrate Grinder. Signal Scanner can locate Military Vaults."
        ));
    }

    private static final Random RANDOM = new Random();

    /**
     * Message priority levels for cooldown management.
     * CRITICAL: Always sent immediately (no cooldown)
     * HIGH: 5 second cooldown
     * NORMAL: 30 second cooldown
     * LOW: 60 second cooldown
     */
    public enum Priority {
        CRITICAL(0),
        HIGH(100),
        NORMAL(600),
        LOW(1200);

        private final int cooldownTicks;

        Priority(int cooldownTicks) {
            this.cooldownTicks = cooldownTicks;
        }

        public int getCooldownTicks() {
            return cooldownTicks;
        }
    }

    public static String getMessage(Context context) {
        List<String> msgs = MESSAGES.getOrDefault(context, MESSAGES.get(Context.IDLE));
        return msgs.get(RANDOM.nextInt(msgs.size()));
    }

    public static List<String> getIntroSequence() {
        return MESSAGES.get(Context.FIRST_JOIN);
    }
    
    /**
     * Get the priority level for a given context.
     * Used to determine message cooldown behavior.
     */
    public static Priority getPriority(Context context) {
        return switch (context) {
            case NO_FILTER, LOW_HEALTH -> Priority.CRITICAL; // Immediate danger
            case HIGH_RADIATION, LOW_FILTER -> Priority.HIGH; // Urgent but not immediate
            case LOW_HYDRATION, STORM -> Priority.NORMAL;
            case NIGHT, IDLE -> Priority.LOW; // Contextual, can wait
            case BIOME_WASTELAND, BIOME_TOXIC_SWAMP, BIOME_RUINED_CITY, BIOME_CRYOGENIC,
                 BIOME_CRASH_ZONE, BIOME_INDUSTRIAL_RUINS, BIOME_RADIATION_ZONE, BIOME_NEXUS_SCAR -> Priority.NORMAL;
            case ENTITY_DRONE, ENTITY_SCAVENGER, ENTITY_MUTANT -> Priority.HIGH;
            case ENTITY_GLOWING_GHOUL, ENTITY_CITY_STALKER, ENTITY_TOXIC_SLIME,
                 ENTITY_RUST_WALKER, ENTITY_ASH_WRAITH, ENTITY_STEAM_WRAITH, ENTITY_MUTATED_CRAWLER -> Priority.HIGH;
            case DISCOVERY_RARE_TECH, RESEARCH_UNLOCKED -> Priority.NORMAL;
            case STABILITY_GLITCH -> Priority.HIGH;
            case RADIO_STATION_ACTIVATED -> Priority.NORMAL;
            case FACTION_REPUTATION_POSITIVE, FACTION_REPUTATION_NEGATIVE -> Priority.HIGH;
            case PHASE_TRANSITION_BIO_TO_GEO, DENSE_ALLOY_NEEDED -> Priority.NORMAL;
            default -> Priority.NORMAL;
        };
    }
}
