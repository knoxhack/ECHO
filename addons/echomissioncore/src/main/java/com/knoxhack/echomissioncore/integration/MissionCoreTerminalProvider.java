package com.knoxhack.echomissioncore.integration;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.mission.IMissionProgressView;
import com.knoxhack.echocore.api.mission.IObjectiveView;
import com.knoxhack.echocore.api.mission.IRewardView;
import com.knoxhack.echocore.api.mission.MissionActionView;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionKind;
import com.knoxhack.echocore.api.mission.MissionStatus;
import com.knoxhack.echomissioncore.EchoMissionCore;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionPresentation;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRequirement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionReward;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoterminal.api.mission.TerminalMissionVisuals;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MissionCoreTerminalProvider implements TerminalMissionProvider {
    public static final MissionCoreTerminalProvider INSTANCE = new MissionCoreTerminalProvider();
    public static final Identifier CHAPTER_ID = Identifier.fromNamespaceAndPath(EchoMissionCore.MODID, "missions");

    private MissionCoreTerminalProvider() {
    }

    @Override
    public TerminalMissionChapter chapter() {
        return new TerminalMissionChapter(
                CHAPTER_ID,
                "MissionCore",
                "Shared mission feed from the ECHO backend service.",
                42,
                0x55FFDD,
                true);
    }

    @Override
    public List<TerminalMissionDefinition> missions(Player player) {
        return EchoCoreServices.missionService().missions(player).stream()
                .map(MissionCoreTerminalProvider::definition)
                .toList();
    }

    @Override
    public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
        return EchoCoreServices.missionService().mission(player, missionId)
                .map(MissionCoreTerminalProvider::snapshot)
                .orElseGet(() -> new TerminalMissionSnapshot(
                        missionId,
                        TerminalMissionStatus.LOCKED,
                        0.0F,
                        "Missing",
                        "MissionCore record not found.",
                        "Reload the terminal after content registration finishes.",
                        List.of()));
    }

    @Override
    public TerminalMissionPresentation presentation(
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        return new TerminalMissionPresentation(
                definition.title(),
                definition.briefing(),
                snapshot.actionHint(),
                definition.phaseTitle(),
                switch (snapshot.status()) {
                    case CLAIMABLE, COMPLETED, CLAIMED -> "success";
                    case UNLOCKED -> "active";
                    case LOCKED, VIEW_ONLY -> "muted";
                },
                List.of(definition.category(), definition.difficulty()),
                "");
    }

    @Override
    public TerminalMissionVisuals visuals(
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        return TerminalMissionVisuals.fallback(definition, snapshot);
    }

    @Override
    public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        return EchoCoreServices.missionService().mission(player, definition.id())
                .map(view -> view.definition().kind() == MissionKind.MAIN
                        ? TerminalMissionRole.MAIN
                        : TerminalMissionRole.OPTIONAL)
                .orElseGet(() -> TerminalMissionRole.fallback(definition, snapshot));
    }

    @Override
    public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
        return EchoCoreServices.handleMissionAction(player, missionId, actionId);
    }

    private static TerminalMissionDefinition definition(IMissionProgressView view) {
        MissionDefinition definition = view.definition();
        return new TerminalMissionDefinition(
                definition.id(),
                CHAPTER_ID,
                definition.phaseId(),
                phaseTitle(definition),
                definition.phaseOrder(),
                definition.missionOrder(),
                definition.title(),
                definition.briefing(),
                definition.fieldGuide(),
                definition.category(),
                definition.difficulty(),
                definition.icon(),
                definition.prerequisites().stream().map(Identifier::toString).toList(),
                view.objectives().stream().map(MissionCoreTerminalProvider::requirement).toList(),
                view.rewards().stream().map(MissionCoreTerminalProvider::reward).toList());
    }

    private static TerminalMissionSnapshot snapshot(IMissionProgressView view) {
        return new TerminalMissionSnapshot(
                view.id(),
                status(view.status()),
                view.progress(),
                view.statusLabel(),
                view.unlockReason(),
                view.actionHint(),
                view.actions().stream().map(MissionCoreTerminalProvider::action).toList());
    }

    private static TerminalMissionRequirement requirement(IObjectiveView objective) {
        return TerminalMissionRequirement.custom(
                objective.label(),
                objective.detail(),
                objective.icon(),
                objective.progress(),
                objective.required(),
                objective.complete());
    }

    private static TerminalMissionReward reward(IRewardView reward) {
        ItemStack stack = reward.stack();
        if (!stack.isEmpty()) {
            return new TerminalMissionReward(stack, reward.label(), reward.detail());
        }
        return TerminalMissionReward.text(reward.label(), reward.detail());
    }

    private static TerminalMissionAction action(MissionActionView action) {
        return action.enabled()
                ? TerminalMissionAction.enabled(action.id(), action.label())
                : TerminalMissionAction.disabled(action.id(), action.label(), action.disabledReason());
    }

    private static TerminalMissionStatus status(MissionStatus status) {
        return switch (status) {
            case LOCKED -> TerminalMissionStatus.LOCKED;
            case UNLOCKED, ACTIVE -> TerminalMissionStatus.UNLOCKED;
            case COMPLETED -> TerminalMissionStatus.COMPLETED;
            case CLAIMABLE -> TerminalMissionStatus.CLAIMABLE;
            case CLAIMED -> TerminalMissionStatus.CLAIMED;
            case VIEW_ONLY -> TerminalMissionStatus.VIEW_ONLY;
        };
    }

    private static String phaseTitle(MissionDefinition definition) {
        String source = definition.chapterId().getNamespace() + ":" + definition.chapterId().getPath();
        return definition.phaseTitle().isBlank() ? source : definition.phaseTitle() + " / " + source;
    }
}
