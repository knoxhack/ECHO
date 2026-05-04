package com.knoxhack.echoterminal.mission;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRequirement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionReward;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class VanillaJourneyProvider implements TerminalMissionProvider {
    public static final VanillaJourneyProvider INSTANCE = new VanillaJourneyProvider();
    public static final Identifier CHAPTER_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "vanilla_journey");
    public static final Identifier TAB_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "vanilla_journey");
    private static final Identifier REFRESH_ID =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "vanilla_journey_refresh");
    private static final String ACTION_REFRESH = "refresh";
    private static final String ACTION_CLAIM = "claim_reward";

    private static final List<VanillaMission> MISSIONS = List.of(
            root("story/root", "Minecraft", "The overworld survival path.", "Punch trees, craft tools, mine stone, and push toward the End.", "Story", 0, Items.GRASS_BLOCK),
            task("story/mine_stone", "Stone Age", "Mine stone with your first pickaxe.", "Craft wood tools, gather cobblestone, and start upgrading the basics.", "Story", 1, Items.COBBLESTONE),
            task("story/upgrade_tools", "Getting an Upgrade", "Construct a stone pickaxe.", "A stone pick unlocks faster mining and the first real resource push.", "Story", 2, Items.STONE_PICKAXE),
            task("story/smelt_iron", "Acquire Hardware", "Smelt an iron ingot.", "Find iron ore, smelt it, and begin the metal progression chain.", "Story", 3, Items.IRON_INGOT),
            task("story/iron_tools", "Isn't It Iron Pick", "Craft an iron pickaxe.", "Iron tools unlock diamonds, redstone, and deeper survival infrastructure.", "Story", 4, Items.IRON_PICKAXE),
            goal("story/lava_bucket", "Hot Stuff", "Fill a bucket with lava.", "Lava enables Nether portals, fuel strategy, and route preparation.", "Story", 5, Items.LAVA_BUCKET),
            goal("story/enter_the_nether", "We Need to Go Deeper", "Enter the Nether.", "Build and light a Nether portal when your overworld kit is stable.", "Story", 6, Items.OBSIDIAN),
            goal("story/mine_diamond", "Diamonds!", "Acquire diamonds.", "Diamond gear opens high-risk caves, enchanting, and End preparation.", "Story", 7, Items.DIAMOND),
            goal("story/enchant_item", "Enchanter", "Enchant an item.", "Build an enchanting setup and start turning raw gear into a real build.", "Story", 8, Items.ENCHANTING_TABLE),
            goal("story/follow_ender_eye", "Eye Spy", "Follow an Eye of Ender.", "Locate a stronghold once blaze powder and ender pearls are secured.", "Story", 9, Items.ENDER_EYE),
            challenge("story/enter_the_end", "The End?", "Enter the End dimension.", "Commit to the dragon route with enough gear, blocks, food, and ranged damage.", "Story", 10, Items.END_PORTAL_FRAME),

            root("nether/root", "Nether", "The hostile resource route.", "Recover blaze rods, ancient debris, fortress access, and beacon materials.", "Nether", 0, Items.NETHERRACK),
            goal("nether/return_to_sender", "Return to Sender", "Deflect a ghast fireball.", "Practice Nether combat and ranged timing in open terrain.", "Nether", 1, Items.FIRE_CHARGE),
            task("nether/find_bastion", "Those Were the Days", "Enter a Bastion Remnant.", "Scout bastions carefully; piglin territory rewards preparation and escape routes.", "Nether", 2, Items.GILDED_BLACKSTONE),
            goal("nether/obtain_ancient_debris", "Hidden in the Depths", "Obtain Ancient Debris.", "Start the Netherite path with controlled mining or blast-resistant routes.", "Nether", 3, Items.ANCIENT_DEBRIS),
            goal("nether/obtain_blaze_rod", "Into Fire", "Obtain a Blaze Rod.", "Blaze rods power brewing and Eyes of Ender for stronghold routing.", "Nether", 4, Items.BLAZE_ROD),
            task("nether/brew_potion", "Local Brewery", "Brew a potion.", "Use blaze powder and a brewing stand to prepare utility effects.", "Nether", 5, Items.POTION),
            challenge("nether/summon_wither", "Withering Heights", "Summon the Wither.", "Only attempt this with an arena, backup gear, and recovery plan.", "Nether", 6, Items.WITHER_SKELETON_SKULL),
            challenge("nether/create_beacon", "Bring Home the Beacon", "Construct and power a beacon.", "Defeat the Wither, gather a pyramid base, and establish a late-game anchor.", "Nether", 7, Items.BEACON),
            challenge("nether/all_potions", "A Furious Cocktail", "Have every potion effect.", "Create a controlled brewing and effect application checklist.", "Nether", 8, Items.BREWING_STAND),
            challenge("nether/all_effects", "How Did We Get Here?", "Have every effect at once.", "A full endgame logistics puzzle. Treat it as a planned operation.", "Nether", 9, Items.DRAGON_BREATH),

            root("end/root", "The End", "The dragon and outer-islands route.", "Defeat the dragon, reach gateways, recover Elytra, and secure shulker storage.", "End", 0, Items.END_STONE),
            challenge("end/kill_dragon", "Free the End", "Defeat the Ender Dragon.", "Destroy crystals, manage the arena, and keep a recovery path open.", "End", 1, Items.DRAGON_HEAD),
            goal("end/dragon_egg", "The Next Generation", "Hold the Dragon Egg.", "Recover the egg after the fight as proof of route completion.", "End", 2, Items.DRAGON_EGG),
            goal("end/enter_end_gateway", "Remote Getaway", "Escape through an End gateway.", "Bridge or pearl safely to the gateway and enter the outer islands.", "End", 3, Items.ENDER_PEARL),
            challenge("end/elytra", "Sky's the Limit", "Find Elytra.", "Raid an End city ship and secure your first long-range flight system.", "End", 4, Items.ELYTRA),

            root("adventure/root", "Adventure", "Exploration, combat, and settlement systems.", "Explore villages, fight mobs, improve routes, and build wider world mastery.", "Adventure", 0, Items.MAP),
            task("adventure/kill_a_mob", "Monster Hunter", "Kill any hostile monster.", "Establish basic combat readiness and safe retreat habits.", "Adventure", 1, Items.IRON_SWORD),
            task("adventure/trade", "What a Deal!", "Trade with a villager.", "Villagers convert resources into gear, maps, food, and long-term infrastructure.", "Adventure", 2, Items.EMERALD),
            task("adventure/shoot_arrow", "Take Aim", "Shoot something with an arrow.", "Ranged damage keeps dangerous fights under control.", "Adventure", 3, Items.BOW),
            task("adventure/sleep_in_bed", "Sweet Dreams", "Sleep in a bed.", "A spawn anchor and night skip are basic survival infrastructure.", "Adventure", 4, Items.RED_BED),
            goal("adventure/hero_of_the_village", "Hero of the Village", "Defend a village from a raid.", "Prepare walls, bells, ranged weapons, and backup food before starting raids.", "Adventure", 5, Items.TOTEM_OF_UNDYING),
            challenge("adventure/kill_all_mobs", "Monsters Hunted", "Kill every hostile monster type.", "A long-form combat checklist across dimensions and rare spawns.", "Adventure", 6, Items.DIAMOND_SWORD),

            root("husbandry/root", "Husbandry", "Food, farming, animals, and renewable resources.", "Build sustainable food, leather, honey, crops, and animal utility systems.", "Husbandry", 0, Items.HAY_BLOCK),
            task("husbandry/plant_seed", "A Seedy Place", "Plant a seed.", "Start a renewable food system before travel distance expands.", "Husbandry", 1, Items.WHEAT_SEEDS),
            task("husbandry/breed_an_animal", "The Parrots and the Bats", "Breed two animals.", "Animal farms stabilize food, leather, wool, and backup materials.", "Husbandry", 2, Items.WHEAT),
            task("husbandry/tame_an_animal", "Best Friends Forever", "Tame an animal.", "Tamed companions and mounts add utility when routes stretch farther.", "Husbandry", 3, Items.BONE),
            goal("husbandry/safely_harvest_honey", "Bee Our Guest", "Safely harvest honey.", "Use smoke and careful placement to make bees renewable instead of hostile.", "Husbandry", 4, Items.HONEY_BOTTLE),
            challenge("husbandry/balanced_diet", "A Balanced Diet", "Eat everything edible.", "A full food-system checklist for farms, fishing, combat drops, and rare items.", "Husbandry", 5, Items.CAKE));

    private VanillaJourneyProvider() {
    }

    @Override
    public TerminalMissionChapter chapter() {
        return new TerminalMissionChapter(
                CHAPTER_ID,
                "Baseline",
                "Recovered Minecraft advancement route with claimable ECHO utility caches.",
                50,
                0xFF92F7A6,
                true);
    }

    @Override
    public List<TerminalMissionDefinition> missions(Player player) {
        VanillaJourneyData data = VanillaJourneyData.get(player);
        return MISSIONS.stream()
                .map(mission -> definition(mission, data))
                .toList();
    }

    @Override
    public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
        VanillaMission mission = mission(missionId);
        if (mission == null) {
            return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                    "LOCKED", "Baseline record not found in the current ECHO index.",
                    "No active Baseline record is available for this signal.", List.of());
        }
        VanillaJourneyData data = VanillaJourneyData.get(player);
        boolean completed = data.isCompleted(mission.id());
        boolean claimed = data.isClaimed(mission.id());
        if (mission.type() == RewardTier.ROOT) {
            return new TerminalMissionSnapshot(mission.id(), completed ? TerminalMissionStatus.COMPLETED : TerminalMissionStatus.UNLOCKED,
                    completed ? 1.0F : 0.0F, completed ? "OPEN" : "GUIDE", "",
                    "Guide header. Complete the linked advancement route to fill this chapter.", List.of());
        }
        List<TerminalMissionAction> actions = List.of(claimed
                ? TerminalMissionAction.disabled(ACTION_CLAIM, "CLAIM CACHE", "Reward cache already claimed.")
                : completed
                        ? TerminalMissionAction.enabled(ACTION_CLAIM, "CLAIM CACHE")
                        : TerminalMissionAction.disabled(ACTION_CLAIM, "CLAIM CACHE", "Complete the vanilla advancement first."));
        TerminalMissionStatus status = claimed
                ? TerminalMissionStatus.CLAIMED
                : completed ? TerminalMissionStatus.CLAIMABLE : TerminalMissionStatus.UNLOCKED;
        return new TerminalMissionSnapshot(
                mission.id(),
                status,
                completed ? 1.0F : 0.0F,
                claimed ? "CLAIMED" : completed ? "CLAIMABLE" : "ADVANCEMENT",
                completed ? "" : "Complete the vanilla advancement on the server.",
                claimed ? "Cache claimed. Keep following the vanilla route."
                        : completed ? "Advancement complete. Cache ready to claim."
                        : "Server validates advancement progress before cache claims.",
                actions);
    }

    @Override
    public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
        if (ACTION_REFRESH.equals(actionId)) {
            refresh(player);
            return true;
        }
        if (!ACTION_CLAIM.equals(actionId)) {
            return false;
        }
        VanillaMission mission = mission(missionId);
        if (mission == null || mission.type() == RewardTier.ROOT) {
            return false;
        }
        VanillaJourneyData data = VanillaJourneyData.get(player);
        refresh(player, data);
        if (!data.isCompleted(mission.id()) || data.isClaimed(mission.id())) {
            VanillaJourneyData.saveAndSync(player, data);
            return false;
        }
        for (ItemStack stack : rewards(mission.type())) {
            ItemStack copy = stack.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }
        data.markClaimed(mission.id());
        VanillaJourneyData.saveAndSync(player, data);
        return true;
    }

    @Override
    public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        VanillaMission mission = mission(definition.id());
        if (mission == null) {
            return TerminalMissionRole.MAIN;
        }
        if (mission.type() == RewardTier.ROOT) {
            return TerminalMissionRole.REFERENCE;
        }
        return switch (mission.phase()) {
            case "Adventure", "Husbandry" -> TerminalMissionRole.OPTIONAL;
            default -> TerminalMissionRole.MAIN;
        };
    }

    public void refresh(ServerPlayer player) {
        VanillaJourneyData data = VanillaJourneyData.get(player);
        refresh(player, data);
        VanillaJourneyData.saveAndSync(player, data);
    }

    private void refresh(ServerPlayer player, VanillaJourneyData data) {
        List<Identifier> completed = new ArrayList<>();
        for (VanillaMission mission : MISSIONS) {
            if (hasAdvancement(player, mission.id())) {
                completed.add(mission.id());
            }
        }
        data.setCompleted(completed);
    }

    private static TerminalMissionDefinition definition(VanillaMission mission, VanillaJourneyData data) {
        boolean completed = data.isCompleted(mission.id());
        List<TerminalMissionRequirement> requirements = mission.type() == RewardTier.ROOT
                ? List.of()
                : List.of(TerminalMissionRequirement.custom(
                        "Vanilla advancement",
                        completed ? "Complete" : "Complete advancement: " + mission.title(),
                        new ItemStack(mission.icon()),
                        completed ? 1 : 0,
                        1,
                        completed));
        return new TerminalMissionDefinition(
                mission.id(),
                CHAPTER_ID,
                mission.phase().toLowerCase(java.util.Locale.ROOT),
                mission.phase(),
                phaseOrder(mission.phase()),
                mission.order(),
                mission.title(),
                mission.briefing(),
                mission.guide(),
                mission.phase(),
                mission.type().label(),
                new ItemStack(mission.icon()),
                List.of(),
                requirements,
                rewards(mission.type()).stream().map(TerminalMissionReward::of).toList());
    }

    private static boolean hasAdvancement(ServerPlayer player, Identifier id) {
        if (player.level().getServer() == null) {
            return false;
        }
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(id);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    private static VanillaMission mission(Identifier id) {
        return MISSIONS.stream()
                .filter(mission -> mission.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private static List<ItemStack> rewards(RewardTier tier) {
        return switch (tier) {
            case ROOT -> List.of();
            case TASK -> List.of(
                    new ItemStack(Items.BREAD, 4),
                    new ItemStack(Items.TORCH, 12),
                    new ItemStack(Items.EXPERIENCE_BOTTLE, 2));
            case GOAL -> List.of(
                    new ItemStack(Items.IRON_INGOT, 6),
                    new ItemStack(Items.EMERALD, 3),
                    new ItemStack(Items.EXPERIENCE_BOTTLE, 8));
            case CHALLENGE -> List.of(
                    new ItemStack(Items.DIAMOND, 1),
                    new ItemStack(Items.GOLDEN_APPLE, 2),
                    new ItemStack(Items.EXPERIENCE_BOTTLE, 16));
        };
    }

    private static int phaseOrder(String phase) {
        return switch (phase) {
            case "Story" -> 0;
            case "Nether" -> 1;
            case "The End" -> 2;
            case "Adventure" -> 3;
            case "Husbandry" -> 4;
            default -> 99;
        };
    }

    private static VanillaMission root(String id, String title, String briefing, String guide, String phase, int order, net.minecraft.world.item.Item icon) {
        return mission(id, title, briefing, guide, phase, order, icon, RewardTier.ROOT);
    }

    private static VanillaMission task(String id, String title, String briefing, String guide, String phase, int order, net.minecraft.world.item.Item icon) {
        return mission(id, title, briefing, guide, phase, order, icon, RewardTier.TASK);
    }

    private static VanillaMission goal(String id, String title, String briefing, String guide, String phase, int order, net.minecraft.world.item.Item icon) {
        return mission(id, title, briefing, guide, phase, order, icon, RewardTier.GOAL);
    }

    private static VanillaMission challenge(String id, String title, String briefing, String guide, String phase, int order, net.minecraft.world.item.Item icon) {
        return mission(id, title, briefing, guide, phase, order, icon, RewardTier.CHALLENGE);
    }

    private static VanillaMission mission(String id, String title, String briefing, String guide,
            String phase, int order, net.minecraft.world.item.Item icon, RewardTier tier) {
        return new VanillaMission(Identifier.withDefaultNamespace(id), title, briefing, guide, phase, order, icon, tier);
    }

    private record VanillaMission(
            Identifier id,
            String title,
            String briefing,
            String guide,
            String phase,
            int order,
            net.minecraft.world.item.Item icon,
            RewardTier type) {
    }

    private enum RewardTier {
        ROOT("Guide"),
        TASK("Task Cache"),
        GOAL("Goal Cache"),
        CHALLENGE("Challenge Cache");

        private final String label;

        RewardTier(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }
}
