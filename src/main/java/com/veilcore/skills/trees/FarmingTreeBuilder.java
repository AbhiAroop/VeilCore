package com.veilcore.skills.trees;

import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;

/**
 * Builds the Farming skill tree
 */
public class FarmingTreeBuilder {

    /**
     * Build the Farming skill tree
     */
    public static SkillTree buildFarmingTree() {
        SkillTree tree = new SkillTree(Skill.FARMING);

        // Root node (always unlocked)
        SkillTreeNode rootNode = new SkillTreeNode(
            "root",
            "Farming",
            "The Farming skill - Grow and harvest crops",
            "hoe",
            Skill.FARMING.getColor(),
            0, 0,
            0,
            TokenTier.BASIC
        );
        tree.addNode(rootNode);

        // Placeholder nodes can be added here in the future

        return tree;
    }
}
