package com.veilcore.skills.trees;

import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;

/**
 * Builds the Woodcutting skill tree
 */
public class WoodcuttingTreeBuilder {

    /**
     * Build the Woodcutting skill tree
     */
    public static SkillTree buildWoodcuttingTree() {
        SkillTree tree = new SkillTree(Skill.WOODCUTTING);

        // Root node (always unlocked)
        SkillTreeNode rootNode = new SkillTreeNode(
            "root",
            "Woodcutting",
            "The Woodcutting skill - Chop trees for lumber",
            "axe",
            Skill.WOODCUTTING.getColor(),
            0, 0,
            0,
            TokenTier.BASIC
        );
        tree.addNode(rootNode);

        // Placeholder nodes can be added here in the future

        return tree;
    }
}
