package de.nexusbay.forge.progression;

import java.util.List;
import java.util.Objects;

public record MasteryTrack(
        String id,
        String displayName,
        MasteryType type,
        List<MasterySkill> skills
) {
    public MasteryTrack {
        Objects.requireNonNull(id);
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(type);
        skills = List.copyOf(skills);
    }

    public enum MasteryType {
        DEVIL_FRUIT,
        SWORD,
        WEAPON,
        FIGHTING_STYLE
    }

    public record MasterySkill(
            String id,
            String displayName,
            int requiredLevel,
            int masteryXpCost,
            int cooldownTicks,
            String animationId
    ) {
        public MasterySkill {
            Objects.requireNonNull(id);
            Objects.requireNonNull(displayName);
            Objects.requireNonNull(animationId);
            if (requiredLevel < 1 || masteryXpCost < 0 || cooldownTicks < 0) {
                throw new IllegalArgumentException("Invalid mastery skill values");
            }
        }
    }
}
