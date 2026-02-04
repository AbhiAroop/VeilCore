package com.veilcore.skills.trees;

import java.util.HashMap;
import java.util.Map;

import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;

/**
 * Builds the Mining skill tree with tiered token requirements
 * Features BASIC, ADVANCED, and MASTER tier nodes
 */
public class MiningTreeBuilder {

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
            0, 0, // Center position
            0, // Free to unlock
            TokenTier.BASIC
        );
        tree.addNode(rootNode);

        // =====================================================================
        // BASIC TIER NODES (Require Basic Tokens)
        // =====================================================================

        // Mining Fortune - 50 upgradable levels
        Map<Integer, String> fortuneDescriptions = new HashMap<>();
        Map<Integer, Integer> fortuneCosts = new HashMap<>();
        for (int i = 1; i <= 50; i++) {
            double fortuneValue = i * 0.5;
            fortuneDescriptions.put(i, 
                "Level " + i + "/50: +" + fortuneValue + " Mining Fortune\n" +
                "Increases drop rates from mining"
            );
            fortuneCosts.put(i, i <= 10 ? 1 : (i <= 25 ? 2 : 3));
        }
        SkillTreeNode miningFortuneNode = new SkillTreeNode(
            "mining_fortune",
            "Mining Fortune",
            "golden_pickaxe",
            "#FFD700",
            -2, 0,
            fortuneDescriptions,
            fortuneCosts,
            TokenTier.BASIC
        );
        tree.addNode(miningFortuneNode);
        tree.addConnection("root", "mining_fortune");

        // Mining Speed - 50 upgradable levels
        Map<Integer, String> speedDescriptions = new HashMap<>();
        Map<Integer, Integer> speedCosts = new HashMap<>();
        for (int i = 1; i <= 50; i++) {
            double speedValue = i * 1.0;
            speedDescriptions.put(i, 
                "Level " + i + "/50: +" + speedValue + "% Mining Speed\n" +
                "Mine blocks faster"
            );
            speedCosts.put(i, i <= 10 ? 1 : (i <= 25 ? 2 : 3));
        }
        SkillTreeNode miningSpeedNode = new SkillTreeNode(
            "mining_speed",
            "Mining Speed",
            "diamond_pickaxe",
            "#00D4FF",
            2, 0,
            speedDescriptions,
            speedCosts,
            TokenTier.BASIC
        );
        tree.addNode(miningSpeedNode);
        tree.addConnection("root", "mining_speed");

        // Ore Finder - Single unlock
        SkillTreeNode oreFinderNode = new SkillTreeNode(
            "ore_finder",
            "Ore Finder",
            "Highlights nearby ores within 16 blocks",
            "compass",
            "#55FF55",
            0, -2,
            3, // 3 basic tokens
            TokenTier.BASIC
        );
        tree.addNode(oreFinderNode);
        tree.addConnection("root", "ore_finder");

        // Efficient Mining - 25 levels
        Map<Integer, String> efficiencyDescriptions = new HashMap<>();
        Map<Integer, Integer> efficiencyCosts = new HashMap<>();
        for (int i = 1; i <= 25; i++) {
            double effValue = i * 2.0;
            efficiencyDescriptions.put(i, 
                "Level " + i + "/25: +" + effValue + "% Mining Efficiency\n" +
                "Reduced tool durability loss"
            );
            efficiencyCosts.put(i, i <= 10 ? 2 : 3);
        }
        SkillTreeNode efficiencyNode = new SkillTreeNode(
            "mining_efficiency",
            "Efficient Miner",
            "iron_pickaxe",
            "#C0C0C0",
            0, 2,
            efficiencyDescriptions,
            efficiencyCosts,
            TokenTier.BASIC
        );
        tree.addNode(efficiencyNode);
        tree.addConnection("root", "mining_efficiency");

        // =====================================================================
        // ADVANCED TIER NODES (Require Advanced Tokens)
        // =====================================================================

        // Master Fortune - 30 levels
        Map<Integer, String> masterFortuneDescriptions = new HashMap<>();
        Map<Integer, Integer> masterFortuneCosts = new HashMap<>();
        for (int i = 1; i <= 30; i++) {
            double fortuneValue = 25 + (i * 1.0);
            masterFortuneDescriptions.put(i, 
                "Level " + i + "/30: +" + fortuneValue + " Total Mining Fortune\n" +
                "Significantly better drop rates"
            );
            masterFortuneCosts.put(i, i <= 15 ? 1 : 2);
        }
        SkillTreeNode masterFortuneNode = new SkillTreeNode(
            "master_fortune",
            "Master Fortune",
            "netherite_pickaxe",
            "#8B008B",
            -4, 0,
            masterFortuneDescriptions,
            masterFortuneCosts,
            TokenTier.ADVANCED
        );
        tree.addNode(masterFortuneNode);
        tree.addConnection("mining_fortune", "master_fortune");

        // Vein Miner - Single unlock
        SkillTreeNode veinMinerNode = new SkillTreeNode(
            "vein_miner",
            "Vein Miner",
            "Mine entire ore veins at once (up to 8 blocks)\nHold SHIFT while mining",
            "emerald",
            "#00FF00",
            -2, -2,
            5, // 5 advanced tokens
            TokenTier.ADVANCED
        );
        tree.addNode(veinMinerNode);
        tree.addConnection("mining_fortune", "vein_miner");

        // Double Drop Chance - 20 levels
        Map<Integer, String> doubleDropDescriptions = new HashMap<>();
        Map<Integer, Integer> doubleDropCosts = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            double chanceValue = i * 2.5;
            doubleDropDescriptions.put(i, 
                "Level " + i + "/20: " + chanceValue + "% Double Drop Chance\n" +
                "Chance to get 2x drops when mining"
            );
            doubleDropCosts.put(i, i <= 10 ? 1 : 2);
        }
        SkillTreeNode doubleDropNode = new SkillTreeNode(
            "double_drop",
            "Double Drop",
            "diamond",
            "#00FFFF",
            2, -2,
            doubleDropDescriptions,
            doubleDropCosts,
            TokenTier.ADVANCED
        );
        tree.addNode(doubleDropNode);
        tree.addConnection("mining_speed", "double_drop");

        // XP Boost - 25 levels
        Map<Integer, String> xpBoostDescriptions = new HashMap<>();
        Map<Integer, Integer> xpBoostCosts = new HashMap<>();
        for (int i = 1; i <= 25; i++) {
            double xpValue = i * 4.0;
            xpBoostDescriptions.put(i, 
                "Level " + i + "/25: +" + xpValue + "% Mining XP\n" +
                "Gain more XP from mining"
            );
            xpBoostCosts.put(i, i <= 12 ? 1 : 2);
        }
        SkillTreeNode xpBoostNode = new SkillTreeNode(
            "mining_xp_boost",
            "XP Hunter",
            "experience_bottle",
            "#7CFC00",
            4, 0,
            xpBoostDescriptions,
            xpBoostCosts,
            TokenTier.ADVANCED
        );
        tree.addNode(xpBoostNode);
        tree.addConnection("mining_speed", "mining_xp_boost");

        // Treasure Hunter - Single unlock
        SkillTreeNode treasureHunterNode = new SkillTreeNode(
            "treasure_hunter",
            "Treasure Hunter",
            "Small chance to find treasure chests while mining",
            "chest",
            "#FFD700",
            0, -4,
            4, // 4 advanced tokens
            TokenTier.ADVANCED
        );
        tree.addNode(treasureHunterNode);
        tree.addConnection("ore_finder", "treasure_hunter");

        // =====================================================================
        // MASTER TIER NODES (Require Master Tokens)
        // =====================================================================

        // Legendary Fortune - 20 levels
        Map<Integer, String> legendaryFortuneDescriptions = new HashMap<>();
        Map<Integer, Integer> legendaryFortuneCosts = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            double fortuneValue = 55 + (i * 2.0);
            legendaryFortuneDescriptions.put(i, 
                "Level " + i + "/20: +" + fortuneValue + " Total Mining Fortune\n" +
                "LEGENDARY drop rates - extremely rare finds"
            );
            legendaryFortuneCosts.put(i, i <= 10 ? 1 : 2);
        }
        SkillTreeNode legendaryFortuneNode = new SkillTreeNode(
            "legendary_fortune",
            "Legendary Fortune",
            "dragon_egg",
            "#FF00FF",
            -6, 0,
            legendaryFortuneDescriptions,
            legendaryFortuneCosts,
            TokenTier.MASTER
        );
        tree.addNode(legendaryFortuneNode);
        tree.addConnection("master_fortune", "legendary_fortune");

        // Mega Vein Miner - Single unlock
        SkillTreeNode megaVeinMinerNode = new SkillTreeNode(
            "mega_vein_miner",
            "Mega Vein Miner",
            "Mine entire ore veins at once (up to 27 blocks)\nHold SHIFT while mining",
            "nether_star",
            "#FFFF00",
            -4, -2,
            3, // 3 master tokens
            TokenTier.MASTER
        );
        tree.addNode(megaVeinMinerNode);
        tree.addConnection("vein_miner", "mega_vein_miner");

        // Triple Drop Chance - 15 levels
        Map<Integer, String> tripleDropDescriptions = new HashMap<>();
        Map<Integer, Integer> tripleDropCosts = new HashMap<>();
        for (int i = 1; i <= 15; i++) {
            double chanceValue = i * 3.0;
            tripleDropDescriptions.put(i, 
                "Level " + i + "/15: " + chanceValue + "% Triple Drop Chance\n" +
                "Chance to get 3x drops when mining"
            );
            tripleDropCosts.put(i, i <= 7 ? 1 : 2);
        }
        SkillTreeNode tripleDropNode = new SkillTreeNode(
            "triple_drop",
            "Triple Drop",
            "beacon",
            "#FF1493",
            2, -4,
            tripleDropDescriptions,
            tripleDropCosts,
            TokenTier.MASTER
        );
        tree.addNode(tripleDropNode);
        tree.addConnection("double_drop", "triple_drop");

        // Mining Mastery - 10 levels
        Map<Integer, String> masteryDescriptions = new HashMap<>();
        Map<Integer, Integer> masteryCosts = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            double bonusValue = i * 5.0;
            masteryDescriptions.put(i, 
                "Level " + i + "/10: +" + bonusValue + "% All Mining Bonuses\n" +
                "Multiplicative boost to Fortune, Speed, and XP"
            );
            masteryCosts.put(i, i <= 5 ? 2 : 3);
        }
        SkillTreeNode masteryNode = new SkillTreeNode(
            "mining_mastery",
            "Mining Mastery",
            "enchanted_book",
            "#9400D3",
            0, 4,
            masteryDescriptions,
            masteryCosts,
            TokenTier.MASTER
        );
        tree.addNode(masteryNode);
        tree.addConnection("mining_efficiency", "mining_mastery");

        // Auto-Smelt - Single unlock
        SkillTreeNode autoSmeltNode = new SkillTreeNode(
            "auto_smelt",
            "Auto-Smelt",
            "Automatically smelts ores when mined\nGet ingots directly!",
            "furnace",
            "#FF4500",
            -2, 2,
            4, // 4 master tokens
            TokenTier.MASTER
        );
        tree.addNode(autoSmeltNode);
        tree.addConnection("mining_efficiency", "auto_smelt");

        return tree;
    }
}
