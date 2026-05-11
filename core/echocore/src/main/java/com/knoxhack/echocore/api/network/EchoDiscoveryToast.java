package com.knoxhack.echocore.api.network;

import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.api.EchoDiscoveryEntry;
import net.minecraft.resources.Identifier;

/**
 * Core-owned discovery toast contract. NetCore turns this data into the wire
 * packet; optional client consumers may render it however they like.
 */
public record EchoDiscoveryToast(
        Identifier featureId,
        String category,
        String title,
        String subtitle,
        String iconArt,
        String heroArt,
        int accentColor) {
    public EchoDiscoveryToast(EchoDiscoveryEntry entry) {
        this(entry == null ? null : entry.id(),
                entry == null ? "" : entry.category().displayName(),
                entry == null ? "" : entry.revealedTitle(),
                "Added to Discovery Grid",
                entry == null ? "" : art(entry.iconArt()),
                entry == null ? "" : art(entry.heroArt()),
                entry == null ? 0xFF66E8FF : entry.accentColor());
    }

    public EchoDiscoveryToast {
        featureId = featureId == null ? Identifier.fromNamespaceAndPath(EchoCore.MODID, "unknown") : featureId;
        category = safe(category);
        title = safe(title);
        subtitle = safe(subtitle);
        iconArt = safe(iconArt);
        heroArt = safe(heroArt);
        accentColor = accentColor == 0 ? 0xFF66E8FF : accentColor;
    }

    private static String art(Identifier id) {
        return id == null ? "" : id.toString();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
