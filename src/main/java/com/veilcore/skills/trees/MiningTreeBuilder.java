package com.veilcore.skills.trees;

import java.util.HashMap;
import java.util.Map;

import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;

/**
 * Builds the Mining skill tree
 * For Phase 1: Only creates root node and one example upgradable node
 */
public class MiningTreeBuilder {

    /**
     * Build the Mining skill tree
     * Phase 1: Root node + Mining Fortune upgradable node (50 levels)
     */
    public static SkillTree buildMiningTree() {
        SkillTree tree = new SkillTree(Skill.MINING);

        // =====================================================================
        // ROOT NODE (Always unlocked by default)
        // =====================================================================
        SkillTreeNode rootNode = new SkillTreeNode(
            "root",
            "Mining",
            "The Mining skill - Extract valuable resources from the earth",
            "pickaxe",
            Skill.MINING.getColor(),
            0, 0, // Center position (0, 0)
            0, // Free to unlock
            TokenTier.BASIC
        );
        tree.addNode(rootNode);

        // =====================================================================
        // MINING FORTUNE NODE (50 upgradable levels)
        // Example of upgradable node with varying costs per tier
        // =====================================================================

        // Create per-level descriptions and costs
        Map<Integer, String> fortuneDescriptions = new HashMap<>();
        Map<Integer, Integer> fortuneCosts = new HashMap<>();

        for (int i = 1; i <= 50; i++) {
            double fortuneValue = i * 0.5;
            fortuneDescriptions.put(i, 
                "Level " + i + "/50: +" + fortuneValue + " Mining Fortune\n" +
                "Increases drop rates from mining\n" +
                "Better chance for rare resources"
            );

            // Progressive cost: 1 token for levels 1-10, 2 for 11-25, 3 for 26-50
            if (i <= 10) {
                fortuneCosts.put(i, 1);
            } else if (i <= 25) {
                fortuneCosts.put(i, 2);
            } else {
                fortuneCosts.put(i, 3);
            }
        }

        SkillTreeNode miningFortuneNode = new SkillTreeNode(
            "mining_fortune",
            "Mining Fortune",
            "golden_pickaxe",
            "#FFD700", // Gold color
            -2, 0, // Left side of root with 1 slot gap
            fortuneDescriptions,
            fortuneCosts,
            TokenTier.BASIC // Requires basic tokens
        );
        tree.addNode(miningFortuneNode);
        tree.addConnection("root", "mining_fortune"); // Connect from root

        return tree;
    }
}
