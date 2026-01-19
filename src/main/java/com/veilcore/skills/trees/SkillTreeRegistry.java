package com.veilcore.skills.trees;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.veilcore.skills.Skill;

/**
 * Central registry for all skill trees
 * Manages creation and access to skill trees for each skill
 */
public class SkillTreeRegistry {
    private static SkillTreeRegistry instance;
    private final Map<String, SkillTree> skillTrees;

    private SkillTreeRegistry() {
        this.skillTrees = new HashMap<>();
        initializeSkillTrees();
    }

    /**
     * Get the singleton instance
     */
    public static SkillTreeRegistry getInstance() {
        if (instance == null) {
            instance = new SkillTreeRegistry();
        }
        return instance;
    }

    /**
     * Initialize all skill trees
     */
    private void initializeSkillTrees() {
        // Phase 1: Only Mining tree is implemented
        skillTrees.put(Skill.MINING.getId(), MiningTreeBuilder.buildMiningTree());

        // Phase 2+: Add other skill trees
        // skillTrees.put(Skill.COMBAT.getId(), CombatTreeBuilder.buildCombatTree());
        // skillTrees.put(Skill.FARMING.getId(), FarmingTreeBuilder.buildFarmingTree());
        // skillTrees.put(Skill.FISHING.getId(), FishingTreeBuilder.buildFishingTree());
    }

    /**
     * Get a skill tree by skill
     */
    @Nullable
    public SkillTree getSkillTree(Skill skill) {
        return skillTrees.get(skill.getId());
    }

    /**
     * Get a skill tree by skill ID
     */
    @Nullable
    public SkillTree getSkillTree(String skillId) {
        return skillTrees.get(skillId);
    }

    /**
     * Check if a skill tree exists
     */
    public boolean hasSkillTree(Skill skill) {
        return skillTrees.containsKey(skill.getId());
    }

    /**
     * Get all registered skill trees
     */
    public Map<String, SkillTree> getAllSkillTrees() {
        return new HashMap<>(skillTrees);
    }
}
