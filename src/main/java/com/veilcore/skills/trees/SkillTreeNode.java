package com.veilcore.skills.trees;

import java.util.HashMap;
import java.util.Map;

import com.veilcore.skills.tokens.SkillToken.TokenTier;

/**
 * Represents a node in a skill tree
 * Can be either a simple unlock node or an upgradable node with multiple levels
 * Supports per-level descriptions, costs, and token tier requirements
 */
public class SkillTreeNode {
    private final String id;
    private final String name;
    private final String description;
    private final String iconName; // For future UI implementation
    private final String color; // Hex color for UI
    private final TreeGridPosition gridPosition;
    private final int tokenCost; // Base cost for simple nodes
    private final int maxLevel; // 1 for simple nodes, >1 for upgradable
    private final Map<Integer, String> levelDescriptions; // Descriptions per level
    private final Map<Integer, Integer> levelCosts; // Token costs per level
    private final TokenTier requiredTokenTier;
    private boolean isSpecialNode; // Special nodes preserve progress on tree reset

    /**
     * Create a simple non-upgradable node (max level 1)
     */
    public SkillTreeNode(String id, String name, String description, String iconName,
                         String color, int gridX, int gridY, int tokenCost,
                         TokenTier requiredTier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.color = color;
        this.gridPosition = new TreeGridPosition(gridX, gridY);
        this.tokenCost = tokenCost;
        this.maxLevel = 1;
        this.requiredTokenTier = requiredTier;
        this.isSpecialNode = false;
        this.levelDescriptions = new HashMap<>();
        this.levelCosts = new HashMap<>();
        this.levelDescriptions.put(1, description);
        this.levelCosts.put(1, tokenCost);
    }

    /**
     * Create an upgradable node with a fixed cost per level
     */
    public SkillTreeNode(String id, String name, String description, String iconName,
                         String color, int gridX, int gridY, int tokenCost, int maxLevel,
                         TokenTier requiredTier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.color = color;
        this.gridPosition = new TreeGridPosition(gridX, gridY);
        this.tokenCost = tokenCost;
        this.maxLevel = Math.max(1, maxLevel);
        this.requiredTokenTier = requiredTier;
        this.isSpecialNode = false;
        this.levelDescriptions = new HashMap<>();
        this.levelCosts = new HashMap<>();
        
        // Set default description and cost for all levels
        for (int i = 1; i <= maxLevel; i++) {
            levelDescriptions.put(i, description);
            levelCosts.put(i, tokenCost);
        }
    }

    /**
     * Create a fully customized upgradable node with per-level descriptions and costs
     */
    public SkillTreeNode(String id, String name, String iconName, String color,
                        int gridX, int gridY, Map<Integer, String> levelDescriptions,
                        Map<Integer, Integer> levelCosts, TokenTier requiredTier) {
        this.id = id;
        this.name = name;
        this.description = levelDescriptions.getOrDefault(1, "");
        this.iconName = iconName;
        this.color = color;
        this.gridPosition = new TreeGridPosition(gridX, gridY);
        this.tokenCost = levelCosts.getOrDefault(1, 1);
        this.levelDescriptions = new HashMap<>(levelDescriptions);
        this.levelCosts = new HashMap<>(levelCosts);
        this.maxLevel = Math.max(1, levelCosts.size());
        this.requiredTokenTier = requiredTier;
        this.isSpecialNode = false;
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

    /**
     * Get description for a specific level
     */
    public String getDescription(int level) {
        return levelDescriptions.getOrDefault(level, description);
    }

    public String getIconName() {
        return iconName;
    }

    public String getColor() {
        return color;
    }

    public TreeGridPosition getGridPosition() {
        return gridPosition;
    }

    /**
     * Get base token cost
     */
    public int getTokenCost() {
        return tokenCost;
    }

    /**
     * Get token cost for a specific level
     */
    public int getTokenCost(int level) {
        return levelCosts.getOrDefault(level, tokenCost);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public TokenTier getRequiredTokenTier() {
        return requiredTokenTier;
    }

    /**
     * Check if this node has multiple upgrade levels
     */
    public boolean isUpgradable() {
        return maxLevel > 1;
    }

    public boolean isSpecialNode() {
        return isSpecialNode;
    }

    public void setSpecialNode(boolean specialNode) {
        isSpecialNode = specialNode;
    }

    /**
     * Set custom description for a specific level
     */
    public void setLevelDescription(int level, String description) {
        if (level > 0 && level <= maxLevel) {
            levelDescriptions.put(level, description);
        }
    }

    /**
     * Set custom cost for a specific level
     */
    public void setLevelCost(int level, int cost) {
        if (level > 0 && level <= maxLevel) {
            levelCosts.put(level, cost);
        }
    }

    /**
     * Get all level descriptions
     */
    public Map<Integer, String> getLevelDescriptions() {
        return new HashMap<>(levelDescriptions);
    }

    /**
     * Get all level costs
     */
    public Map<Integer, Integer> getLevelCosts() {
        return new HashMap<>(levelCosts);
    }

    @Override
    public String toString() {
        return name + " [" + id + "] at " + gridPosition + 
               (isUpgradable() ? " (Max Lv " + maxLevel + ")" : "");
    }
}
