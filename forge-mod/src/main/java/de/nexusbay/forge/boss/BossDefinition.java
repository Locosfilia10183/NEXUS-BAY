package de.nexusbay.forge.boss;

import java.util.List;
import java.util.Objects;

public record BossDefinition(
        String id,
        String displayName,
        int level,
        double maxHealth,
        double scale,
        BossRank rank,
        String islandId,
        String fightingStyle,
        List<String> abilities,
        List<String> drops
) {
    public BossDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(rank, "rank");
        Objects.requireNonNull(islandId, "islandId");
        Objects.requireNonNull(fightingStyle, "fightingStyle");
        abilities = List.copyOf(abilities);
        drops = List.copyOf(drops);
        if (level < 1 || maxHealth <= 0 || scale <= 0) {
            throw new IllegalArgumentException("Boss level, health and scale must be positive");
        }
    }
}
