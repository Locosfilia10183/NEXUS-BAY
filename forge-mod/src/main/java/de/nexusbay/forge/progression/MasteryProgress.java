package de.nexusbay.forge.progression;

import java.util.HashSet;
import java.util.Set;

public final class MasteryProgress {
    private int level = 1;
    private long experience;
    private final Set<String> unlockedSkills = new HashSet<>();

    public int level() {
        return level;
    }

    public long experience() {
        return experience;
    }

    public boolean isUnlocked(String skillId) {
        return unlockedSkills.contains(skillId);
    }

    public Set<String> unlockedSkills() {
        return Set.copyOf(unlockedSkills);
    }

    public void addExperience(long amount) {
        if (amount <= 0) return;
        experience = Math.addExact(experience, amount);
        while (level < 100 && experience >= experienceRequiredFor(level + 1)) {
            level++;
        }
    }

    public boolean unlock(MasteryTrack.MasterySkill skill) {
        if (level < skill.requiredLevel()) return false;
        return unlockedSkills.add(skill.id());
    }

    public static long experienceRequiredFor(int targetLevel) {
        if (targetLevel <= 1) return 0;
        return 100L * (targetLevel - 1) * (targetLevel - 1);
    }
}
