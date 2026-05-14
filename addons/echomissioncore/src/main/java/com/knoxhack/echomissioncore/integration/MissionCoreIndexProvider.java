package com.knoxhack.echomissioncore.integration;

import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echocore.api.mission.ObjectiveDefinition;
import com.knoxhack.echocore.api.mission.RewardDefinition;
import com.knoxhack.echomissioncore.EchoMissionCore;
import com.knoxhack.echomissioncore.service.MissionCoreService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public enum MissionCoreIndexProvider implements IIndexRecipeProvider {
    INSTANCE;

    private static final Identifier CATEGORY_MISSION_REWARDS =
            Identifier.fromNamespaceAndPath(EchoMissionCore.MODID, "recipe/mission_rewards");

    @Override
    public Identifier id() {
        return Identifier.fromNamespaceAndPath(EchoMissionCore.MODID, "provider/index_recipes");
    }

    @Override
    public List<IndexRecipeCategory> recipeCategories(Player player) {
        return List.of(new IndexRecipeCategory(
                CATEGORY_MISSION_REWARDS,
                "Mission Rewards",
                new ItemStack(Items.WRITABLE_BOOK),
                0xFFFFD166,
                590));
    }

    @Override
    public List<IndexRecipeView> recipes(Player player) {
        List<IndexRecipeView> views = new ArrayList<>();
        for (MissionDefinition mission : MissionCoreService.INSTANCE.missionDefinitions()) {
            IndexRecipeView view = missionView(mission);
            if (view != null) {
                views.add(view);
            }
        }
        return List.copyOf(views);
    }

    private static IndexRecipeView missionView(MissionDefinition mission) {
        List<IndexRecipeSlot> slots = new ArrayList<>();
        boolean hasDiscoveryValue = false;
        for (ObjectiveDefinition objective : mission.objectives()) {
            IndexRecipeSlot slot = objectiveSlot(objective);
            if (slot != null) {
                slots.add(slot);
                hasDiscoveryValue = true;
            }
        }
        for (RewardDefinition reward : mission.rewards()) {
            IndexRecipeSlot slot = rewardSlot(reward);
            if (slot != null) {
                slots.add(slot);
                hasDiscoveryValue = true;
            }
        }
        if (!mission.prerequisites().isEmpty()) {
            hasDiscoveryValue = true;
            slots.add(new IndexRecipeSlot(IndexSlotRole.CATALYST, List.of(),
                    "Unlocks after: " + joinIds(mission.prerequisites())));
        }
        if (!hasDiscoveryValue) {
            return null;
        }
        if (mission.rewards().isEmpty()) {
            slots.add(new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(), "Mission progress: " + mission.title()));
        }
        ItemStack machine = mission.icon().isEmpty() ? new ItemStack(Items.WRITABLE_BOOK) : mission.icon();
        slots.add(IndexRecipeSlot.machine(machine));
        List<String> notes = new ArrayList<>();
        if (!mission.briefing().isBlank()) {
            notes.add(mission.briefing());
        }
        if (!mission.fieldGuide().isBlank()) {
            notes.add(mission.fieldGuide());
        }
        notes.add("Chapter: " + mission.chapterId());
        if (!mission.phaseTitle().isBlank()) {
            notes.add("Phase: " + mission.phaseTitle());
        }
        if (!mission.category().isBlank()) {
            notes.add("Category: " + mission.category());
        }
        if (!mission.difficulty().isBlank()) {
            notes.add("Difficulty: " + mission.difficulty());
        }
        notes.add("Repeat: " + mission.repeatPolicy().name().toLowerCase(Locale.ROOT));
        if (mission.hidden()) {
            notes.add("Hidden until discovered or unlocked.");
        }
        if (!mission.metadata().isEmpty()) {
            notes.add("Metadata: " + compactMetadata(mission.metadata()));
        }
        return new IndexRecipeView(
                Identifier.fromNamespaceAndPath(EchoMissionCore.MODID,
                        "recipe/mission/" + sanitize(mission.id().getNamespace()) + "/" + sanitize(mission.id().getPath())),
                CATEGORY_MISSION_REWARDS,
                mission.title(),
                machine,
                slots,
                notes,
                0,
                false,
                mission.id().getNamespace());
    }

    private static IndexRecipeSlot objectiveSlot(ObjectiveDefinition objective) {
        ItemStack stack = objective.icon();
        Identifier target = objectiveTarget(objective.criteria());
        if (target != null) {
            ItemStack targetStack = stackForId(target, objective.required());
            if (!targetStack.isEmpty()) {
                stack = targetStack;
            }
        }
        String label = objective.required() + "x " + objective.label();
        if (itemObjective(objective.type())) {
            return stack.isEmpty()
                    ? new IndexRecipeSlot(IndexSlotRole.INPUT, List.of(), label)
                    : new IndexRecipeSlot(IndexSlotRole.INPUT, List.of(stack), label);
        }
        if (sourceObjective(objective.type()) || target != null || !objective.detail().isBlank()) {
            return stack.isEmpty()
                    ? new IndexRecipeSlot(IndexSlotRole.CATALYST, List.of(), label)
                    : new IndexRecipeSlot(IndexSlotRole.CATALYST, List.of(stack), label);
        }
        return null;
    }

    private static IndexRecipeSlot rewardSlot(RewardDefinition reward) {
        ItemStack stack = reward.stack();
        if (stack.isEmpty()) {
            Identifier itemId = Identifier.tryParse(reward.metadata().getOrDefault("item", ""));
            stack = stackForId(itemId, count(reward.metadata()));
        }
        String label = reward.label();
        return stack.isEmpty()
                ? new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(), label)
                : new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(stack), label);
    }

    private static boolean itemObjective(MissionObjectiveType type) {
        return type == MissionObjectiveType.OBTAIN_ITEM
                || type == MissionObjectiveType.CRAFT_ITEM
                || type == MissionObjectiveType.DELIVER_ITEM
                || type == MissionObjectiveType.PLACE_BLOCK;
    }

    private static boolean sourceObjective(MissionObjectiveType type) {
        return type == MissionObjectiveType.DISCOVER_STRUCTURE
                || type == MissionObjectiveType.ENTER_REGION
                || type == MissionObjectiveType.UNLOCK_RESEARCH
                || type == MissionObjectiveType.BUILD_MULTIBLOCK
                || type == MissionObjectiveType.ESTABLISH_ROUTE
                || type == MissionObjectiveType.COMPLETE_ORBITAL_SCAN
                || type == MissionObjectiveType.REPAIR_MACHINE;
    }

    private static Identifier objectiveTarget(Map<String, String> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return null;
        }
        for (String key : List.of("target", "id", "item", "block", "structure", "region", "unlock")) {
            Identifier id = Identifier.tryParse(criteria.getOrDefault(key, ""));
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    private static ItemStack stackForId(Identifier id, int count) {
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (item != Items.AIR) {
            return new ItemStack(item, Math.max(1, count));
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        if (block != null && block.asItem() != Items.AIR) {
            return new ItemStack(block.asItem(), Math.max(1, count));
        }
        return ItemStack.EMPTY;
    }

    private static int count(Map<String, String> metadata) {
        try {
            return Math.max(1, Integer.parseInt(metadata.getOrDefault("count", "1")));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private static String joinIds(List<Identifier> ids) {
        return ids.stream().map(Identifier::toString).reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String compactMetadata(Map<String, String> metadata) {
        return metadata.entrySet().stream()
                .limit(6)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String sanitize(String path) {
        String clean = path == null ? "unknown" : path.trim().toLowerCase(Locale.ROOT);
        clean = clean.replace('\\', '/').replace(':', '/').replaceAll("[^a-z0-9_./-]", "_");
        while (clean.contains("//")) {
            clean = clean.replace("//", "/");
        }
        return clean.isBlank() ? "unknown" : clean;
    }
}
