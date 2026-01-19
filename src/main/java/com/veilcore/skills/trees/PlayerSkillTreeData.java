package com.veilcore.skills.trees;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.veilcore.skills.tokens.SkillToken.TokenTier;

/**
 * Stores a player's progress in skill trees
 * Tracks unlocked nodes, node levels, and available skill tokens per tier
 */
public class PlayerSkillTreeData {
    // Map: skillId -> (nodeId -> level)
    private Map<String, Map<String, Integer>> unlockedNodeLevels;
    
    // Map: skillId -> (tierKey -> tokenCount)
    // tierKey format: "tier_1", "tier_2", "tier_3"
    private Map<String, Map<String, Integer>> tieredSkillTokens;

    public PlayerSkillTreeData() {
        this.unlockedNodeLevels = new HashMap<>();
        this.tieredSkillTokens = new HashMap<>();
    }

    /**
     * Get the number of tokens a player has for a skill and tier
     */
    public int getTokenCount(String skillId, TokenTier tier) {
        String tierKey = "tier_" + tier.getLevel();
        return tieredSkillTokens
                .getOrDefault(skillId, new HashMap<>())
                .getOrDefault(tierKey, 0);
    }

    /**
     * Set token count for a skill and tier
     */
    public void setTokenCount(String skillId, TokenTier tier, int count) {
        String tierKey = "tier_" + tier.getLevel();
        tieredSkillTokens.computeIfAbsent(skillId, k -> new HashMap<>())
                .put(tierKey, Math.max(0, count));
    }

    /**
     * Add tokens of a specific tier to a skill
     */
    public void addTokens(String skillId, TokenTier tier, int amount) {
        int current = getTokenCount(skillId, tier);
        setTokenCount(skillId, tier, current + amount);
    }

    /**
     * Use tokens for unlocking/upgrading a node
     * Higher tier tokens can be used for lower tier requirements
     * @return True if tokens were successfully used
     */
    public boolean useTokens(String skillId, TokenTier requiredTier, int cost) {
        // Try to use tokens from required tier or higher
        for (TokenTier tier : TokenTier.values()) {
            if (tier.canUseFor(requiredTier)) {
                int available = getTokenCount(skillId, tier);
                if (available >= cost) {
                    setTokenCount(skillId, tier, available - cost);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get all token counts for a skill (by tier)
     */
    public Map<TokenTier, Integer> getAllTokenCounts(String skillId) {
        Map<TokenTier, Integer> counts = new HashMap<>();
        for (TokenTier tier : TokenTier.values()) {
            counts.put(tier, getTokenCount(skillId, tier));
        }
        return counts;
    }

    /**
     * Check if player can afford a node upgrade
     */
    public boolean canAffordNode(String skillId, SkillTreeNode node, int currentLevel) {
        int cost = node.getTokenCost(currentLevel + 1);
        TokenTier requiredTier = node.getRequiredTokenTier();

        // Check if player has enough tokens of required tier or higher
        int availableTokens = 0;
        for (TokenTier tier : TokenTier.values()) {
            if (tier.canUseFor(requiredTier)) {
                availableTokens += getTokenCount(skillId, tier);
            }
        }

        return availableTokens >= cost;
    }

    /**
     * Get all unlocked nodes for a skill
     */
    public Set<String> getUnlockedNodes(String skillId) {
        return unlockedNodeLevels.getOrDefault(skillId, new HashMap<>()).keySet();
    }

    /**
     * Check if a node is unlocked (at any level)
     */
    public boolean isNodeUnlocked(String skillId, String nodeId) {
        return unlockedNodeLevels.getOrDefault(skillId, new HashMap<>())
                .containsKey(nodeId);
    }

    /**
     * Get the current level of an unlocked node
     * @return Node level, or 0 if not unlocked
     */
    public int getNodeLevel(String skillId, String nodeId) {
        return unlockedNodeLevels.getOrDefault(skillId, new HashMap<>())
                .getOrDefault(nodeId, 0);
    }

    /**
     * Get all node levels for a skill
     */
    public Map<String, Integer> getNodeLevels(String skillId) {
        return new HashMap<>(unlockedNodeLevels.getOrDefault(skillId, new HashMap<>()));
    }

    /**
     * Unlock a node at level 1
     */
    public void unlockNode(String skillId, String nodeId) {
        unlockNodeAtLevel(skillId, nodeId, 1);
    }

    /**
     * Unlock a node at a specific level
     */
    public void unlockNodeAtLevel(String skillId, String nodeId, int level) {
        unlockedNodeLevels.computeIfAbsent(skillId, k -> new HashMap<>())
                .put(nodeId, Math.max(1, level));
    }

    /**
     * Set node level directly (for upgrades)
     */
    public void setNodeLevel(String skillId, String nodeId, int level) {
        if (level > 0) {
            unlockNodeAtLevel(skillId, nodeId, level);
        } else {
            removeNode(skillId, nodeId);
        }
    }

    /**
     * Remove a node (lock it)
     */
    public void removeNode(String skillId, String nodeId) {
        if (unlockedNodeLevels.containsKey(skillId)) {
            unlockedNodeLevels.get(skillId).remove(nodeId);
        }
    }

    /**
     * Upgrade a node to the next level
     * @return New level, or 0 if node is not unlocked
     */
    public int upgradeNode(String skillId, String nodeId) {
        if (!isNodeUnlocked(skillId, nodeId)) {
            return 0;
        }

        int currentLevel = getNodeLevel(skillId, nodeId);
        int newLevel = currentLevel + 1;
        unlockNodeAtLevel(skillId, nodeId, newLevel);
        return newLevel;
    }

    /**
     * Reset a skill tree, refunding tokens
     * @return Map of refunded tokens by tier
     */
    public Map<TokenTier, Integer> resetSkillTree(String skillId, SkillTree tree) {
        Map<TokenTier, Integer> refunds = new HashMap<>();
        for (TokenTier tier : TokenTier.values()) {
            refunds.put(tier, 0);
        }

        // Get current node levels
        Map<String, Integer> nodeLevels = getNodeLevels(skillId);

        // Calculate refunds (skip root and special nodes)
        for (Map.Entry<String, Integer> entry : nodeLevels.entrySet()) {
            String nodeId = entry.getKey();
            int level = entry.getValue();

            if (nodeId.equals("root")) {
                continue; // Don't refund root node
            }

            SkillTreeNode node = tree.getNode(nodeId);
            if (node == null || node.isSpecialNode()) {
                continue; // Don't refund special nodes
            }

            TokenTier tier = node.getRequiredTokenTier();
            
            if (node.isUpgradable()) {
                // Refund cost for each level
                for (int i = 1; i <= level; i++) {
                    int cost = node.getTokenCost(i);
                    refunds.put(tier, refunds.get(tier) + cost);
                }
            } else {
                // Refund base cost
                refunds.put(tier, refunds.get(tier) + node.getTokenCost());
            }
        }

        // Clear non-special nodes
        if (unlockedNodeLevels.containsKey(skillId)) {
            Map<String, Integer> skillNodes = unlockedNodeLevels.get(skillId);
            skillNodes.entrySet().removeIf(entry -> {
                String nodeId = entry.getKey();
                SkillTreeNode node = tree.getNode(nodeId);
                return node != null && !node.isSpecialNode() && !nodeId.equals("root");
            });
        }

        // Refund tokens
        for (TokenTier tier : TokenTier.values()) {
            int refund = refunds.get(tier);
            if (refund > 0) {
                addTokens(skillId, tier, refund);
            }
        }

        return refunds;
    }

    /**
     * Get total token refund if tree was reset (for display)
     */
    public int getTotalTokensInTree(String skillId, SkillTree tree) {
        Map<String, Integer> nodeLevels = getNodeLevels(skillId);
        int total = 0;

        for (Map.Entry<String, Integer> entry : nodeLevels.entrySet()) {
            String nodeId = entry.getKey();
            int level = entry.getValue();

            if (nodeId.equals("root")) continue;

            SkillTreeNode node = tree.getNode(nodeId);
            if (node == null || node.isSpecialNode()) continue;

            if (node.isUpgradable()) {
                for (int i = 1; i <= level; i++) {
                    total += node.getTokenCost(i);
                }
            } else {
                total += node.getTokenCost();
            }
        }

        return total;
    }
}
