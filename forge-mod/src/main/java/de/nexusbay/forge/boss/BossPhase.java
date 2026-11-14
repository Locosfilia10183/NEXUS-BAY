package de.nexusbay.forge.boss;

import java.util.List;
import java.util.Objects;

public record BossPhase(String id, double healthThresholdPercent, List<String> abilities) {
    public BossPhase {
        Objects.requireNonNull(id);
        abilities = List.copyOf(abilities);
        if (healthThresholdPercent < 0 || healthThresholdPercent > 100) {
            throw new IllegalArgumentException("Health threshold must be between 0 and 100");
        }
    }
}
