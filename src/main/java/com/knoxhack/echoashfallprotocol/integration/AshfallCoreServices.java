package com.knoxhack.echoashfallprotocol.integration;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.echo.EchoIntel;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import java.util.Locale;
import net.minecraft.server.level.ServerPlayer;

/**
 * Ashfall-owned service implementations exposed through ECHO Core.
 */
public final class AshfallCoreServices {
    private static final String CHAPTER_ID = "ashfall_protocol";

    private AshfallCoreServices() {
    }

    public static void register() {
        EchoAddonRegistry.register(new EchoAddonChapter() {
            @Override
            public String id() {
                return CHAPTER_ID;
            }

            @Override
            public String modId() {
                return EchoAshfallProtocol.MODID;
            }

            @Override
            public String displayName() {
                return "ECHO: Ashfall Protocol";
            }

            @Override
            public String summary() {
                return "Survival progression, missions, drones, Nexus path data, and wasteland field intel.";
            }

            @Override
            public String statusLine(net.minecraft.world.entity.player.Player player) {
                if (player == null) {
                    return "Ashfall systems available.";
                }
                QuestData quest = QuestData.get(player);
                return "Phase " + (quest.getCurrentPhase() + 1)
                        + " / Mission " + (quest.getCurrentMissionIndex() + 1);
            }
        });

        EchoCoreServices.registerNexusPathService(player -> player != null && PostNexusData.get(player).hasMadeChoice());
        EchoCoreServices.registerIntelMirrorService(AshfallCoreServices::mirrorIntel);
    }

    private static void mirrorIntel(ServerPlayer player, String sourceModId, String id, String title, String content) {
        String safeSource = sanitize(sourceModId, "addon");
        String safeId = sanitize(id, "entry");
        String safeTitle = title == null || title.isBlank() ? safeId : title;
        String safeContent = content == null ? "" : content;

        QuestData quest = QuestData.get(player);
        quest.addToArchive("[" + safeSource.toUpperCase(Locale.ROOT) + "] " + safeTitle);
        QuestData.saveAndSync(player, quest);

        EchoIntel intel = EchoIntel.get(player);
        intel.discoverLore(safeSource + "_" + safeId, safeTitle, safeContent);
        EchoIntel.saveAndSync(player, intel);
    }

    private static String sanitize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
    }
}
