package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.lore.OrbitalLore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class AshfallCompat {
    private static final String CHAPTER_ID = "orbital_remnants";
    private static final String MIRROR_ROOT = "echoorbitalremnants_ashfall_mirror";

    private AshfallCompat() {
    }

    public static void registerAddonChapter() {
        if (EchoAddonRegistry.isRegistered(CHAPTER_ID)) {
            return;
        }
        EchoAddonRegistry.register(new EchoAddonChapter() {
            @Override
            public String id() {
                return CHAPTER_ID;
            }

            @Override
            public String modId() {
                return EchoOrbitalRemnants.MODID;
            }

            @Override
            public String displayName() {
                return "ECHO: Orbital Remnants";
            }

            @Override
            public String summary() {
                return "Post-Nexus orbital survival chapter: launch chain, Station ECHO debris, route worlds, and ECHO-0.";
            }

            @Override
            public boolean isAvailable(Player player) {
                return hasPostNexusChoice(player);
            }

            @Override
            public String statusLine(Player player) {
                return isAvailable(player)
                        ? "ORBITAL REMNANTS: Nexus choice confirmed. Earth orbital calibration available."
                        : "ORBITAL REMNANTS: Locked until a Nexus choice is made.";
            }
        });
    }

    public static boolean isAshfallLoaded() {
        return ModList.get().isLoaded(OrbitalLore.ASHFALL_MODID);
    }

    public static boolean isOrbitalCalibrationLocked(Player player) {
        return isAshfallLoaded() && !hasPostNexusChoice(player);
    }

    public static boolean hasPostNexusChoice(Player player) {
        if (player == null) {
            return false;
        }
        if (!isAshfallLoaded()) {
            return true;
        }
        return EchoCoreServices.hasPostNexusChoice(player);
    }

    public static void mirrorMilestone(Player player, String id, String title, String content) {
        if (!isAshfallLoaded() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CompoundTag mirrored = player.getPersistentData().getCompoundOrEmpty(MIRROR_ROOT);
        if (mirrored.getBooleanOr(id, false)) {
            return;
        }
        mirrored.putBoolean(id, true);
        player.getPersistentData().put(MIRROR_ROOT, mirrored);

        EchoCoreServices.mirrorIntel(serverPlayer, EchoOrbitalRemnants.MODID, "orbital_" + id, title, content);
    }
}
