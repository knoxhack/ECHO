package com.knoxhack.echosoundcore.client.music;

import com.knoxhack.echosoundcore.EchoSoundCore;
import com.knoxhack.echosoundcore.SoundCoreAudioPriority;
import com.knoxhack.echosoundcore.SoundCoreChapter;
import com.knoxhack.echosoundcore.SoundCoreCombatIntensity;
import com.knoxhack.echosoundcore.api.SoundCoreMusicProfile;
import com.knoxhack.echosoundcore.api.context.SoundCoreContext;
import com.knoxhack.echosoundcore.api.context.SoundCoreContextStack;
import com.knoxhack.echosoundcore.client.config.SoundCoreConfig;
import com.knoxhack.echosoundcore.data.SoundCoreDataReloadListener;
import com.knoxhack.echosoundcore.registry.SoundCoreSounds;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class SoundCoreMusicManager {
    private static final Minecraft MC = Minecraft.getInstance();
    private static Identifier currentTrackId = null;
    private static SoundCoreAudioPriority currentPriority = SoundCoreAudioPriority.IDLE;
    private static long lastChangeTick = -9999;
    private static long trackStartTick = -9999;
    private static SimpleSoundInstance currentInstance = null;

    private SoundCoreMusicManager() {}

    public static void tick() {
        if (!SoundCoreConfig.ENABLE_ADAPTIVE_MUSIC.get()) {
            stopControlled();
            return;
        }
        if (MC.player == null || MC.level == null) {
            return;
        }

        long now = MC.level.getGameTime();
        SoundCoreContext ctx = SoundCoreContextStack.current();
        DesiredTrack desired = selectDesiredTrack(ctx);

        if (desired == null || desired.sound() == null) {
            if (currentTrackId != null) {
                stopControlled();
            }
            return;
        }

        if (currentTrackId != null && currentTrackId.equals(desired.id())) {
            if (currentInstance != null && !MC.getSoundManager().isActive(currentInstance)) {
                currentTrackId = null;
                currentInstance = null;
            }
            return;
        }

        long cooldown = SoundCoreConfig.MUSIC_CHANGE_COOLDOWN_TICKS.get();
        long minPlay = SoundCoreConfig.MINIMUM_TRACK_PLAY_TICKS.get();
        if (now - lastChangeTick < cooldown) {
            return;
        }
        if (currentTrackId != null && now - trackStartTick < minPlay) {
            return;
        }

        playTrack(desired, now);
    }

    public static void stopControlled() {
        if (currentInstance != null) {
            MC.getSoundManager().stop(currentInstance);
            currentInstance = null;
        }
        currentTrackId = null;
        currentPriority = SoundCoreAudioPriority.IDLE;
    }

    public static Identifier currentTrackId() {
        return currentTrackId;
    }

    public static SoundCoreAudioPriority currentPriority() {
        return currentPriority;
    }

    private static void playTrack(DesiredTrack desired, long now) {
        stopControlled();
        SoundEvent sound = desired.sound();
        if (sound == null) return;

        float volume = getVolumeMultiplier(desired.priority());
        currentInstance = createMusicInstance(sound, volume);
        MC.getSoundManager().play(currentInstance);
        currentTrackId = desired.id();
        currentPriority = desired.priority();
        lastChangeTick = now;
        trackStartTick = now;
    }

    private static SimpleSoundInstance createMusicInstance(SoundEvent sound, float volume) {
        return new SimpleSoundInstance(
            sound.location(),
            SoundSource.MUSIC,
            volume,
            1.0F,
            RandomSource.create(0L),
            false,
            0,
            SoundInstance.Attenuation.NONE,
            0.0,
            0.0,
            0.0,
            true
        );
    }

    private static float getVolumeMultiplier(SoundCoreAudioPriority priority) {
        double base = SoundCoreConfig.MUSIC_VOLUME_MULTIPLIER.get();
        if (priority == SoundCoreAudioPriority.COMBAT || priority == SoundCoreAudioPriority.BOSS || priority == SoundCoreAudioPriority.SIEGE) {
            base *= SoundCoreConfig.COMBAT_MUSIC_VOLUME_MULTIPLIER.get();
        }
        return (float) base;
    }

    private static DesiredTrack selectDesiredTrack(SoundCoreContext ctx) {
        // Scripted / boss / combat overrides
        if (ctx.bossId() != null) {
            Identifier bossTrack = resolveBossTrack(ctx.bossId());
            if (bossTrack != null) {
                SoundEvent se = findRegisteredSound(bossTrack);
                if (se != null) return new DesiredTrack(bossTrack, se, SoundCoreAudioPriority.BOSS);
            }
        }

        if (ctx.combatIntensity() == SoundCoreCombatIntensity.SIEGE) {
            SoundEvent se = SoundCoreSounds.MUSIC_COMBAT_SIEGE.get();
            if (se != null && SoundCoreConfig.ENABLE_COMBAT_MUSIC.get()) return new DesiredTrack(id("music.combat.siege"), se, SoundCoreAudioPriority.SIEGE);
        } else if (ctx.combatIntensity() == SoundCoreCombatIntensity.BOSS) {
            SoundEvent se = SoundCoreSounds.MUSIC_COMBAT_HEAVY.get();
            if (se != null && SoundCoreConfig.ENABLE_COMBAT_MUSIC.get()) return new DesiredTrack(id("music.combat.heavy"), se, SoundCoreAudioPriority.BOSS);
        } else if (ctx.combatIntensity() == SoundCoreCombatIntensity.HEAVY || ctx.combatIntensity() == SoundCoreCombatIntensity.ELITE) {
            SoundEvent se = SoundCoreSounds.MUSIC_COMBAT_HEAVY.get();
            if (se != null && SoundCoreConfig.ENABLE_COMBAT_MUSIC.get()) return new DesiredTrack(id("music.combat.heavy"), se, SoundCoreAudioPriority.COMBAT);
        } else if (ctx.combatIntensity() == SoundCoreCombatIntensity.LIGHT) {
            SoundEvent se = SoundCoreSounds.MUSIC_COMBAT_LIGHT.get();
            if (se != null && SoundCoreConfig.ENABLE_COMBAT_MUSIC.get()) return new DesiredTrack(id("music.combat.light"), se, SoundCoreAudioPriority.COMBAT);
        }

        if (ctx.terminalOpen() && SoundCoreConfig.TERMINAL_MUSIC_BED_ENABLED.get()) {
            if (ctx.nexusCorruptionLevel() > 0.5f) {
                SoundEvent se = SoundCoreSounds.MUSIC_TERMINAL_NEXUS_CORRUPTED.get();
                if (se != null) return new DesiredTrack(id("music.terminal.nexus_corrupted"), se, SoundCoreAudioPriority.STRUCTURE);
            }
            SoundEvent se = SoundCoreSounds.MUSIC_TERMINAL_COMMAND_BED.get();
            if (se != null) return new DesiredTrack(id("music.terminal.command_bed"), se, SoundCoreAudioPriority.STRUCTURE);
        }

        if (ctx.missionId() != null) {
            SoundEvent se = tryFindChapterTrack(ctx.chapter());
            if (se != null) return new DesiredTrack(id("music.chapter." + ctx.chapter().name().toLowerCase()), se, SoundCoreAudioPriority.MISSION);
        }

        if (ctx.structure() != null) {
            SoundEvent se = tryFindStructureTrack(ctx.structure());
            if (se != null) return new DesiredTrack(ctx.structure(), se, SoundCoreAudioPriority.STRUCTURE);
        }

        if (ctx.biome() != null) {
            SoundEvent se = tryFindBiomeTrack(ctx.biome());
            if (se != null && SoundCoreConfig.ENABLE_BIOME_MUSIC.get()) return new DesiredTrack(ctx.biome(), se, SoundCoreAudioPriority.BIOME);
        }

        if (ctx.underground()) {
            SoundEvent se = SoundCoreSounds.MUSIC_GAMEPLAY_UNDERGROUND.get();
            if (se != null) return new DesiredTrack(id("music.gameplay.underground"), se, SoundCoreAudioPriority.BASE);
        }

        // Safe base / exploration fallback
        SoundEvent se = SoundCoreSounds.MUSIC_GAMEPLAY_EXPLORATION.get();
        if (se != null) return new DesiredTrack(id("music.gameplay.exploration"), se, SoundCoreAudioPriority.IDLE);

        // Try data-driven profiles
        return tryDataProfile(ctx);
    }

    private static DesiredTrack tryDataProfile(SoundCoreContext ctx) {
        List<SoundCoreMusicProfile> profiles = SoundCoreDataReloadListener.getMusicProfiles();
        SoundCoreMusicProfile best = null;
        int bestScore = -1;
        for (SoundCoreMusicProfile p : profiles) {
            int score = scoreProfile(p, ctx);
            if (score > bestScore) {
                bestScore = score;
                best = p;
            }
        }
        if (best != null && bestScore > 0) {
            SoundEvent se = findRegisteredSound(best.sound());
            if (se != null) return new DesiredTrack(best.id(), se, best.priority());
        }
        return null;
    }

    private static int scoreProfile(SoundCoreMusicProfile p, SoundCoreContext ctx) {
        int score = p.priority().weight() * 10;
        if (p.chapter() != SoundCoreChapter.UNKNOWN && p.chapter() == ctx.chapter()) score += 100;
        if (p.biome() != null && p.biome().equals(ctx.biome())) score += 80;
        if (p.structure() != null && p.structure().equals(ctx.structure())) score += 90;
        if (p.faction() != null && p.faction().equals(ctx.faction())) score += 70;
        if (p.combatIntensity() != SoundCoreCombatIntensity.NONE && p.combatIntensity() == ctx.combatIntensity()) score += 85;
        if (p.boss() != null && p.boss().equals(ctx.bossId())) score += 120;
        return score;
    }

    private static Identifier resolveBossTrack(Identifier bossId) {
        String path = bossId.getPath();
        Map<String, DeferredHolder<SoundEvent, SoundEvent>> map = java.util.Map.ofEntries(
            Map.entry("warden", SoundCoreSounds.MUSIC_BOSS_WARDEN),
            Map.entry("guardian.wasteland", SoundCoreSounds.MUSIC_BOSS_GUARDIAN_WASTELAND),
            Map.entry("guardian.toxic", SoundCoreSounds.MUSIC_BOSS_GUARDIAN_TOXIC),
            Map.entry("guardian.radiation", SoundCoreSounds.MUSIC_BOSS_GUARDIAN_RADIATION),
            Map.entry("guardian.cryo", SoundCoreSounds.MUSIC_BOSS_GUARDIAN_CRYO),
            Map.entry("guardian.industrial", SoundCoreSounds.MUSIC_BOSS_GUARDIAN_INDUSTRIAL),
            Map.entry("guardian.city", SoundCoreSounds.MUSIC_BOSS_GUARDIAN_CITY),
            Map.entry("guardian.nexus", SoundCoreSounds.MUSIC_BOSS_GUARDIAN_NEXUS),
            Map.entry("corruption_bloom", SoundCoreSounds.MUSIC_BOSS_CORRUPTION_BLOOM),
            Map.entry("severance_engine", SoundCoreSounds.MUSIC_BOSS_SEVERANCE_ENGINE),
            Map.entry("mirror_command", SoundCoreSounds.MUSIC_BOSS_MIRROR_COMMAND),
            Map.entry("station_mother", SoundCoreSounds.MUSIC_BOSS_STATION_MOTHER)
        );
        var holder = map.get(path);
        return holder == null ? null : holder.getId();
    }

    private static SoundEvent tryFindChapterTrack(SoundCoreChapter chapter) {
        return switch (chapter) {
            case ASHFALL -> SoundCoreSounds.MUSIC_CHAPTER_ASHFALL_PROTOCOL.get();
            case ORBITAL -> SoundCoreSounds.MUSIC_CHAPTER_ORBITAL_REMNANTS.get();
            case AGRICULTURE -> SoundCoreSounds.MUSIC_CHAPTER_AGRICULTURE_RECLAMATION.get();
            case STATIONFALL -> SoundCoreSounds.MUSIC_CHAPTER_STATIONFALL.get();
            case NEXUS -> SoundCoreSounds.MUSIC_CHAPTER_NEXUS_PROTOCOL.get();
            case INDUSTRIAL -> SoundCoreSounds.MUSIC_CHAPTER_INDUSTRIAL_NEXUS.get();
            case LOGISTICS -> SoundCoreSounds.MUSIC_CHAPTER_LOGISTICS_NETWORK.get();
            case CONVOY -> SoundCoreSounds.MUSIC_CHAPTER_CONVOY_PROTOCOL.get();
            case ARMORY -> SoundCoreSounds.MUSIC_CHAPTER_ARMORY.get();
            case BLACKBOX -> SoundCoreSounds.MUSIC_CHAPTER_BLACKBOX_PROTOCOL.get();
            default -> null;
        };
    }

    private static SoundEvent tryFindBiomeTrack(Identifier biome) {
        String path = biome.getPath();
        if (path.contains("wasteland")) return SoundCoreSounds.MUSIC_BIOME_WASTELAND.get();
        if (path.contains("crash")) return SoundCoreSounds.MUSIC_BIOME_CRASH_ZONE.get();
        if (path.contains("toxic") || path.contains("swamp")) return SoundCoreSounds.MUSIC_BIOME_TOXIC_SWAMP.get();
        if (path.contains("radiation")) return SoundCoreSounds.MUSIC_BIOME_RADIATION_ZONE.get();
        if (path.contains("cryo")) return SoundCoreSounds.MUSIC_BIOME_CRYOGENIC_RUINS.get();
        if (path.contains("city")) return SoundCoreSounds.MUSIC_BIOME_RUINED_CITY.get();
        if (path.contains("industrial")) return SoundCoreSounds.MUSIC_BIOME_INDUSTRIAL_RUINS.get();
        if (path.contains("nexus")) return SoundCoreSounds.MUSIC_BIOME_NEXUS_SCAR.get();
        return null;
    }

    private static SoundEvent tryFindStructureTrack(Identifier structure) {
        String path = structure.getPath();
        if (path.contains("nexus")) return SoundCoreSounds.MUSIC_NEXUS_CORE_AMBIENCE.get();
        if (path.contains("blackbox")) return SoundCoreSounds.MUSIC_BLACKBOX_MEMORY_FRAGMENT.get();
        if (path.contains("station")) return SoundCoreSounds.MUSIC_CHAPTER_STATIONFALL.get();
        return null;
    }

    private static SoundEvent findRegisteredSound(Identifier id) {
        for (var entry : SoundCoreSounds.getEntries()) {
            if (entry.getId().equals(id)) {
                return entry.get();
            }
        }
        return null;
    }

    private static Identifier id(String path) {
        return EchoSoundCore.id(path);
    }

    private record DesiredTrack(Identifier id, SoundEvent sound, SoundCoreAudioPriority priority) {}
}
