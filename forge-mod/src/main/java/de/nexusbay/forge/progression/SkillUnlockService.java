package de.nexusbay.forge.progression;

public final class SkillUnlockService {
    private SkillUnlockService() {}

    public static UnlockResult tryUnlock(MasteryProgress progress, MasteryTrack.MasterySkill skill) {
        if (progress.isUnlocked(skill.id())) return UnlockResult.ALREADY_UNLOCKED;
        if (progress.level() < skill.requiredLevel()) return UnlockResult.LEVEL_TOO_LOW;
        return progress.unlock(skill) ? UnlockResult.UNLOCKED : UnlockResult.ALREADY_UNLOCKED;
    }

    public enum UnlockResult {
        UNLOCKED,
        ALREADY_UNLOCKED,
        LEVEL_TOO_LOW
    }
}
