package com.veilcore.skills;

/**
 * Tracks a player's progress in a specific skill
 * Handles level (1-100), current XP, and XP-to-next-level calculations
 */
public class SkillLevel {
    private int level;
    private long currentXp;
    private static final int MAX_LEVEL = 100;

    public SkillLevel() {
        this.level = 1;
        this.currentXp = 0;
    }

    public SkillLevel(int level, long currentXp) {
        this.level = Math.max(1, Math.min(level, MAX_LEVEL));
        this.currentXp = Math.max(0, currentXp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(level, MAX_LEVEL));
    }

    public long getCurrentXp() {
        return currentXp;
    }

    public void setCurrentXp(long currentXp) {
        this.currentXp = Math.max(0, currentXp);
    }

    /**
     * Calculate XP required to reach the next level
     * Uses a progressive formula that increases with level
     * @return XP needed for next level, or 0 if at max level
     */
    public long getXpToNextLevel() {
        if (level >= MAX_LEVEL) {
            return 0;
        }
        // Formula: 100 * level^1.5 (progressive curve)
        return (long) (100 * Math.pow(level, 1.5));
    }

    /**
     * Calculate total XP required to reach a specific level from level 1
     * @param targetLevel The level to calculate total XP for
     * @return Total XP required
     */
    public static long getTotalXpForLevel(int targetLevel) {
        long totalXp = 0;
        for (int i = 1; i < targetLevel; i++) {
            totalXp += (long) (100 * Math.pow(i, 1.5));
        }
        return totalXp;
    }

    /**
     * Add XP and handle level-ups
     * @param xp Amount of XP to add
     * @return Number of levels gained (0 if none)
     */
    public int addXp(long xp) {
        if (level >= MAX_LEVEL) {
            return 0;
        }

        currentXp += xp;
        int levelsGained = 0;

        // Check for level-ups
        while (level < MAX_LEVEL && currentXp >= getXpToNextLevel()) {
            currentXp -= getXpToNextLevel();
            level++;
            levelsGained++;
        }

        // Cap XP at max level
        if (level >= MAX_LEVEL) {
            currentXp = 0;
        }

        return levelsGained;
    }

    /**
     * Get progress to next level as a percentage
     * @return Progress percentage (0.0 to 1.0)
     */
    public double getProgressPercent() {
        if (level >= MAX_LEVEL) {
            return 1.0;
        }
        long xpNeeded = getXpToNextLevel();
        if (xpNeeded == 0) {
            return 1.0;
        }
        return Math.min(1.0, (double) currentXp / xpNeeded);
    }

    /**
     * Check if this skill is at max level
     * @return True if level 100
     */
    public boolean isMaxLevel() {
        return level >= MAX_LEVEL;
    }

    @Override
    public String toString() {
        return "Level " + level + " (" + currentXp + "/" + getXpToNextLevel() + " XP)";
    }
}
