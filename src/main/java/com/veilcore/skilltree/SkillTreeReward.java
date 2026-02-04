package com.veilcore.skilltree;

/**
 * Represents a single reward option in a skill tree tier
 */
public class SkillTreeReward {
    private final String id;
    private final String name;
    private final String description;
    private final RewardType type;
    private final double value;
    
    public enum RewardType {
        MINING_FORTUNE,
        MINING_SPEED,
        MINING_EFFICIENCY,
        XP_BOOST,
        MAX_HEALTH,
        DEFENSE
    }
    
    public SkillTreeReward(String id, String name, String description, RewardType type, double value) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public RewardType getType() {
        return type;
    }
    
    public double getValue() {
        return value;
    }
}
