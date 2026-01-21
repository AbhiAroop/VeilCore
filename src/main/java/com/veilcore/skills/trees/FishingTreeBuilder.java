package com.veilcore.skills.trees;

import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;

/**
 * Builds the Fishing skill tree
 */
public class FishingTreeBuilder {

    /**
     * Build the Fishing skill tree
     */
    public static SkillTree buildFishingTree() {
        SkillTree tree = new SkillTree(Skill.FISHING);

        // Root node (always unlocked)
        SkillTreeNode rootNode = new SkillTreeNode(
            "root",
            "Fishing",
            "The Fishing skill - Catch fish and treasures",
            "fishing_rod",
            Skill.FISHING.getColor(),
            0, 0,
            0,
            TokenTier.BASIC
        );
        tree.addNode(rootNode);

        // Placeholder nodes can be added here in the future

        return tree;
    }
}
