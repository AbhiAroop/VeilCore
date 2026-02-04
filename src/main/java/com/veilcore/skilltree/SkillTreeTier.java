package com.veilcore.skilltree;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tier in a skill tree with multiple reward options
 */
public class SkillTreeTier {
    private final int tierNumber;
    private final int unlockLevel;
    private final int requiredSelections;
    private final List<SkillTreeReward> rewards;
    
    public SkillTreeTier(int tierNumber, int unlockLevel, int requiredSelections) {
        this.tierNumber = tierNumber;
        this.unlockLevel = unlockLevel;
        this.requiredSelections = requiredSelections;
        this.rewards = new ArrayList<>();
    }
    
    public void addReward(SkillTreeReward reward) {
        rewards.add(reward);
    }
    
    public int getTierNumber() {
        return tierNumber;
    }
    
    public int getUnlockLevel() {
        return unlockLevel;
    }
    
    public int getRequiredSelections() {
        return requiredSelections;
    }
    
    public List<SkillTreeReward> getRewards() {
        return new ArrayList<>(rewards);
    }
    
    public SkillTreeReward getRewardById(String rewardId) {
        return rewards.stream()
                .filter(r -> r.getId().equals(rewardId))
                .findFirst()
                .orElse(null);
    }
}
