package de.nexusbay.forge.boss;

import java.util.Comparator;
import java.util.List;

public final class BossPhaseController {
    private final List<BossPhase> phases;

    public BossPhaseController(List<BossPhase> phases) {
        this.phases = phases.stream()
                .sorted(Comparator.comparingDouble(BossPhase::healthThresholdPercent).reversed())
                .toList();
        if (this.phases.isEmpty()) throw new IllegalArgumentException("At least one phase is required");
    }

    public BossPhase phaseFor(double currentHealth, double maxHealth) {
        if (maxHealth <= 0) throw new IllegalArgumentException("Max health must be positive");
        double percent = Math.max(0, Math.min(100, currentHealth / maxHealth * 100));
        return phases.stream()
                .filter(phase -> percent <= phase.healthThresholdPercent())
                .min(Comparator.comparingDouble(BossPhase::healthThresholdPercent))
                .orElse(phases.get(0));
    }
}
