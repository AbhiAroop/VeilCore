package com.veilcore.skilltree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a player's skill tree progress for a specific skill
 * Now uses tier-based system with multiple reward choices per tier
 */
public class SkillTree {
    private final String skillName;
    private final List<SkillTreeTier> tiers;
    private final Map<Integer, Set<String>> claimedRewards; // Tier -> Set of claimed reward IDs
    private int skillLevel; // Current skill level (1-100)
    
    public SkillTree(String skillName) {
        this.skillName = skillName;
        this.tiers = new ArrayList<>();
        this.claimedRewards = new HashMap<>();
        this.skillLevel = 1;
        
        // Initialize tiers
        initializeTiers();
    }
    
    private void initializeTiers() {
        if ("mining".equalsIgnoreCase(skillName)) {
            initializeMiningTiers();
        }
    }
    
    private void initializeMiningTiers() {
        // Tier 1 - Level 5 (2 rewards, select 1)
        SkillTreeTier tier1 = new SkillTreeTier(1, 5, 1);
        tier1.addReward(new SkillTreeReward("t1_fortune1", "Lucky Strike I", "+2 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 2));
        tier1.addReward(new SkillTreeReward("t1_speed1", "Swift Miner I", "+5% Mining Speed", SkillTreeReward.RewardType.MINING_SPEED, 0.05));
        tiers.add(tier1);
        
        // Tier 2 - Level 10 (2 rewards, select 1)
        SkillTreeTier tier2 = new SkillTreeTier(2, 10, 1);
        tier2.addReward(new SkillTreeReward("t2_fortune2", "Lucky Strike II", "+3 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 3));
        tier2.addReward(new SkillTreeReward("t2_efficiency1", "Efficient Mining I", "+10% Mining Efficiency", SkillTreeReward.RewardType.MINING_EFFICIENCY, 0.10));
        tiers.add(tier2);
        
        // Tier 3 - Level 15 (3 rewards, select 1)
        SkillTreeTier tier3 = new SkillTreeTier(3, 15, 1);
        tier3.addReward(new SkillTreeReward("t3_fortune3", "Lucky Strike III", "+4 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 4));
        tier3.addReward(new SkillTreeReward("t3_speed2", "Swift Miner II", "+8% Mining Speed", SkillTreeReward.RewardType.MINING_SPEED, 0.08));
        tier3.addReward(new SkillTreeReward("t3_xp1", "Mining Scholar I", "+10% Mining XP", SkillTreeReward.RewardType.XP_BOOST, 0.10));
        tiers.add(tier3);
        
        // Tier 4 - Level 20 (3 rewards, select 1)
        SkillTreeTier tier4 = new SkillTreeTier(4, 20, 1);
        tier4.addReward(new SkillTreeReward("t4_fortune4", "Lucky Strike IV", "+5 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 5));
        tier4.addReward(new SkillTreeReward("t4_efficiency2", "Efficient Mining II", "+15% Mining Efficiency", SkillTreeReward.RewardType.MINING_EFFICIENCY, 0.15));
        tier4.addReward(new SkillTreeReward("t4_health1", "Miner's Endurance I", "+4 Max Health", SkillTreeReward.RewardType.MAX_HEALTH, 4));
        tiers.add(tier4);
        
        // Tier 5 - Level 30 (4 rewards, select 2)
        SkillTreeTier tier5 = new SkillTreeTier(5, 30, 2);
        tier5.addReward(new SkillTreeReward("t5_fortune5", "Lucky Strike V", "+6 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 6));
        tier5.addReward(new SkillTreeReward("t5_speed3", "Swift Miner III", "+12% Mining Speed", SkillTreeReward.RewardType.MINING_SPEED, 0.12));
        tier5.addReward(new SkillTreeReward("t5_xp2", "Mining Scholar II", "+15% Mining XP", SkillTreeReward.RewardType.XP_BOOST, 0.15));
        tier5.addReward(new SkillTreeReward("t5_defense1", "Stone Skin I", "+2 Defense", SkillTreeReward.RewardType.DEFENSE, 2));
        tiers.add(tier5);
        
        // Tier 6 - Level 40 (4 rewards, select 2)
        SkillTreeTier tier6 = new SkillTreeTier(6, 40, 2);
        tier6.addReward(new SkillTreeReward("t6_fortune6", "Lucky Strike VI", "+8 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 8));
        tier6.addReward(new SkillTreeReward("t6_efficiency3", "Efficient Mining III", "+20% Mining Efficiency", SkillTreeReward.RewardType.MINING_EFFICIENCY, 0.20));
        tier6.addReward(new SkillTreeReward("t6_health2", "Miner's Endurance II", "+6 Max Health", SkillTreeReward.RewardType.MAX_HEALTH, 6));
        tier6.addReward(new SkillTreeReward("t6_xp3", "Mining Scholar III", "+20% Mining XP", SkillTreeReward.RewardType.XP_BOOST, 0.20));
        tiers.add(tier6);
        
        // Tier 7 - Level 50 (5 rewards, select 2)
        SkillTreeTier tier7 = new SkillTreeTier(7, 50, 2);
        tier7.addReward(new SkillTreeReward("t7_fortune7", "Lucky Strike VII", "+10 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 10));
        tier7.addReward(new SkillTreeReward("t7_speed4", "Swift Miner IV", "+15% Mining Speed", SkillTreeReward.RewardType.MINING_SPEED, 0.15));
        tier7.addReward(new SkillTreeReward("t7_efficiency4", "Efficient Mining IV", "+25% Mining Efficiency", SkillTreeReward.RewardType.MINING_EFFICIENCY, 0.25));
        tier7.addReward(new SkillTreeReward("t7_defense2", "Stone Skin II", "+4 Defense", SkillTreeReward.RewardType.DEFENSE, 4));
        tier7.addReward(new SkillTreeReward("t7_health3", "Miner's Endurance III", "+8 Max Health", SkillTreeReward.RewardType.MAX_HEALTH, 8));
        tiers.add(tier7);
        
        // Tier 8 - Level 65 (5 rewards, select 2)
        SkillTreeTier tier8 = new SkillTreeTier(8, 65, 2);
        tier8.addReward(new SkillTreeReward("t8_fortune8", "Lucky Strike VIII", "+12 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 12));
        tier8.addReward(new SkillTreeReward("t8_speed5", "Swift Miner V", "+18% Mining Speed", SkillTreeReward.RewardType.MINING_SPEED, 0.18));
        tier8.addReward(new SkillTreeReward("t8_xp4", "Mining Scholar IV", "+25% Mining XP", SkillTreeReward.RewardType.XP_BOOST, 0.25));
        tier8.addReward(new SkillTreeReward("t8_health4", "Miner's Endurance IV", "+10 Max Health", SkillTreeReward.RewardType.MAX_HEALTH, 10));
        tier8.addReward(new SkillTreeReward("t8_efficiency5", "Efficient Mining V", "+30% Mining Efficiency", SkillTreeReward.RewardType.MINING_EFFICIENCY, 0.30));
        tiers.add(tier8);
        
        // Tier 9 - Level 80 (5 rewards, select 3)
        SkillTreeTier tier9 = new SkillTreeTier(9, 80, 3);
        tier9.addReward(new SkillTreeReward("t9_fortune9", "Master's Fortune", "+15 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 15));
        tier9.addReward(new SkillTreeReward("t9_speed6", "Master's Speed", "+22% Mining Speed", SkillTreeReward.RewardType.MINING_SPEED, 0.22));
        tier9.addReward(new SkillTreeReward("t9_efficiency6", "Master's Efficiency", "+35% Mining Efficiency", SkillTreeReward.RewardType.MINING_EFFICIENCY, 0.35));
        tier9.addReward(new SkillTreeReward("t9_defense3", "Stone Skin III", "+6 Defense", SkillTreeReward.RewardType.DEFENSE, 6));
        tier9.addReward(new SkillTreeReward("t9_xp5", "Mining Master", "+30% Mining XP", SkillTreeReward.RewardType.XP_BOOST, 0.30));
        tiers.add(tier9);
        
        // Tier 10 - Level 100 (6 rewards, select 3)
        SkillTreeTier tier10 = new SkillTreeTier(10, 100, 3);
        tier10.addReward(new SkillTreeReward("t10_fortune10", "Legendary Fortune", "+20 Mining Fortune", SkillTreeReward.RewardType.MINING_FORTUNE, 20));
        tier10.addReward(new SkillTreeReward("t10_speed7", "Legendary Speed", "+25% Mining Speed", SkillTreeReward.RewardType.MINING_SPEED, 0.25));
        tier10.addReward(new SkillTreeReward("t10_efficiency7", "Legendary Efficiency", "+40% Mining Efficiency", SkillTreeReward.RewardType.MINING_EFFICIENCY, 0.40));
        tier10.addReward(new SkillTreeReward("t10_health5", "Legendary Endurance", "+15 Max Health", SkillTreeReward.RewardType.MAX_HEALTH, 15));
        tier10.addReward(new SkillTreeReward("t10_defense4", "Legendary Stone Skin", "+8 Defense", SkillTreeReward.RewardType.DEFENSE, 8));
        tier10.addReward(new SkillTreeReward("t10_xp6", "Mining Grandmaster", "+40% Mining XP", SkillTreeReward.RewardType.XP_BOOST, 0.40));
        tiers.add(tier10);
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public int getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(int level) {
        this.skillLevel = Math.max(1, Math.min(100, level));
    }
    
    public List<SkillTreeTier> getTiers() {
        return new ArrayList<>(tiers);
    }
    
    public SkillTreeTier getTier(int tierNumber) {
        return tiers.stream()
                .filter(t -> t.getTierNumber() == tierNumber)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if a tier is unlocked based on skill level
     */
    public boolean isTierUnlocked(int tierNumber) {
        SkillTreeTier tier = getTier(tierNumber);
        return tier != null && skillLevel >= tier.getUnlockLevel();
    }
    
    /**
     * Check if a tier is fully claimed
     */
    public boolean isTierClaimed(int tierNumber) {
        SkillTreeTier tier = getTier(tierNumber);
        if (tier == null) return false;
        
        Set<String> claimed = claimedRewards.getOrDefault(tierNumber, new HashSet<>());
        return claimed.size() >= tier.getRequiredSelections();
    }
    
    /**
     * Get the status of a tier
     */
    public TierStatus getTierStatus(int tierNumber) {
        if (!isTierUnlocked(tierNumber)) {
            return TierStatus.LOCKED;
        }
        if (isTierClaimed(tierNumber)) {
            return TierStatus.CLAIMED;
        }
        return TierStatus.AVAILABLE;
    }
    
    /**
     * Check if a specific reward is claimed
     */
    public boolean isRewardClaimed(int tierNumber, String rewardId) {
        Set<String> claimed = claimedRewards.getOrDefault(tierNumber, new HashSet<>());
        return claimed.contains(rewardId);
    }
    
    /**
     * Claim a reward from a tier
     */
    public boolean claimReward(int tierNumber, String rewardId) {
        SkillTreeTier tier = getTier(tierNumber);
        if (tier == null) return false;
        
        // Check if tier is unlocked
        if (!isTierUnlocked(tierNumber)) {
            return false;
        }
        
        // Check if already claimed
        if (isRewardClaimed(tierNumber, rewardId)) {
            return false;
        }
        
        // Check if tier is already full
        Set<String> claimed = claimedRewards.computeIfAbsent(tierNumber, k -> new HashSet<>());
        if (claimed.size() >= tier.getRequiredSelections()) {
            return false;
        }
        
        // Check if reward exists
        if (tier.getRewardById(rewardId) == null) {
            return false;
        }
        
        // Claim the reward
        claimed.add(rewardId);
        return true;
    }
    
    /**
     * Get all claimed rewards across all tiers
     */
    public List<SkillTreeReward> getAllClaimedRewards() {
        List<SkillTreeReward> allRewards = new ArrayList<>();
        
        for (Map.Entry<Integer, Set<String>> entry : claimedRewards.entrySet()) {
            int tierNumber = entry.getKey();
            SkillTreeTier tier = getTier(tierNumber);
            if (tier == null) continue;
            
            for (String rewardId : entry.getValue()) {
                SkillTreeReward reward = tier.getRewardById(rewardId);
                if (reward != null) {
                    allRewards.add(reward);
                }
            }
        }
        
        return allRewards;
    }
    
    /**
     * Reset all claimed rewards (respec)
     */
    public void resetAllRewards() {
        claimedRewards.clear();
    }
    
    /**
     * Get number of claimed rewards in a tier
     */
    public int getClaimedCount(int tierNumber) {
        return claimedRewards.getOrDefault(tierNumber, new HashSet<>()).size();
    }
    
    public enum TierStatus {
        LOCKED,
        AVAILABLE,
        CLAIMED
    }
}
