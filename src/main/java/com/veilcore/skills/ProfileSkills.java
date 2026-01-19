package com.veilcore.skills;

import java.util.HashMap;
import java.util.Map;

import com.veilcore.skills.tokens.SkillToken.TokenTier;
import com.veilcore.skills.trees.PlayerSkillTreeData;

/**
 * Manages all skills for a player's profile
 * Tracks skill levels, XP, and skill tree progress
 */
public class ProfileSkills {
    // Map: Skill -> SkillLevel
    private Map<Skill, SkillLevel> skillLevels;
    
    // Player's skill tree progress
    private PlayerSkillTreeData treeData;

    public ProfileSkills() {
        this.skillLevels = new HashMap<>();
        this.treeData = new PlayerSkillTreeData();
        
        // Initialize all skills at level 1
        for (Skill skill : Skill.values()) {
            skillLevels.put(skill, new SkillLevel());
        }
    }

    /**
     * Get the level data for a specific skill
     */
    public SkillLevel getSkillLevel(Skill skill) {
        return skillLevels.computeIfAbsent(skill, k -> new SkillLevel());
    }

    /**
     * Get the current level for a skill
     */
    public int getLevel(Skill skill) {
        return getSkillLevel(skill).getLevel();
    }

    /**
     * Get current XP for a skill
     */
    public long getXp(Skill skill) {
        return getSkillLevel(skill).getCurrentXp();
    }

    /**
     * Add XP to a skill and handle level-ups
     * @return Number of levels gained
     */
    public int addXp(Skill skill, long xp) {
        SkillLevel level = getSkillLevel(skill);
        int levelsGained = level.addXp(xp);
        
        // Award skill tokens on level-up
        if (levelsGained > 0) {
            awardTokensForLevels(skill, level.getLevel() - levelsGained, level.getLevel());
        }
        
        return levelsGained;
    }

    /**
     * Award skill tokens based on level milestones
     * Follows MMO pattern: Basic tokens early, Advanced mid-game, Master late-game
     */
    private void awardTokensForLevels(Skill skill, int fromLevel, int toLevel) {
        for (int level = fromLevel + 1; level <= toLevel; level++) {
            if (level % 5 == 0) { // Award tokens every 5 levels
                if (level >= 70) {
                    // Master tokens at levels 70+
                    treeData.addTokens(skill.getId(), TokenTier.MASTER, 1);
                } else if (level >= 30) {
                    // Advanced tokens at levels 30-69
                    treeData.addTokens(skill.getId(), TokenTier.ADVANCED, 1);
                } else {
                    // Basic tokens at levels 5-29
                    treeData.addTokens(skill.getId(), TokenTier.BASIC, 1);
                }
            }
        }
    }

    /**
     * Set skill level directly (admin/debug)
     */
    public void setLevel(Skill skill, int level) {
        getSkillLevel(skill).setLevel(level);
    }

    /**
     * Set skill XP directly (admin/debug)
     */
    public void setXp(Skill skill, long xp) {
        getSkillLevel(skill).setCurrentXp(xp);
    }

    /**
     * Get the skill tree data
     */
    public PlayerSkillTreeData getTreeData() {
        return treeData;
    }

    /**
     * Get all skill levels
     */
    public Map<Skill, SkillLevel> getAllSkillLevels() {
        return new HashMap<>(skillLevels);
    }

    /**
     * Get total level across all skills
     */
    public int getTotalLevel() {
        int total = 0;
        for (SkillLevel level : skillLevels.values()) {
            total += level.getLevel();
        }
        return total;
    }

    /**
     * Check if a skill is at max level
     */
    public boolean isMaxLevel(Skill skill) {
        return getSkillLevel(skill).isMaxLevel();
    }

    /**
     * Get progress to next level for a skill
     */
    public double getProgressPercent(Skill skill) {
        return getSkillLevel(skill).getProgressPercent();
    }

    /**
     * Get XP needed for next level
     */
    public long getXpToNextLevel(Skill skill) {
        return getSkillLevel(skill).getXpToNextLevel();
    }
}
