package com.veilcore.skills.subskills.mining;

import com.veilcore.skills.Skill;
import com.veilcore.skills.subskills.Subskill;

/**
 * Ore Extraction subskill for Mining
 * Awards XP when mining ore blocks based on ore rarity
 */
public class OreExtraction extends Subskill {
    
    public static final String ID = "ore_extraction";
    
    public enum OreRarity {
        COMMON(5),      // Calcite, limestone
        UNCOMMON(10),   // Copper, tin
        RARE(25),       // Iron, silver
        EPIC(50),       // Gold, feranite
        LEGENDARY(100); // Mithril, varyn
        
        private final long xpAmount;
        
        OreRarity(long xpAmount) {
            this.xpAmount = xpAmount;
        }
        
        public long getXpAmount() {
            return xpAmount;
        }
    }
    
    public OreExtraction() {
        super(
            ID,
            "Ore Extraction",
            "Gain mining XP when mining ore blocks. Rarer ores grant more XP.",
            Skill.MINING,
            SubskillType.PASSIVE
        );
    }
    
    /**
     * Calculate XP for mining a specific ore type
     * @param rarity The rarity of the ore mined
     * @return XP amount to award
     */
    public static long calculateXp(OreRarity rarity) {
        return rarity.getXpAmount();
    }
    
    /**
     * Get ore rarity from block ID
     * @param blockId The block ID (e.g., "Ore_Calcite", "Ore_Mithril")
     * @return The ore rarity, or null if not an ore
     */
    public static OreRarity getOreRarity(String blockId) {
        if (blockId == null) return null;
        
        String lowerBlockId = blockId.toLowerCase();
        
        // Common ores
        if (lowerBlockId.contains("calcite") || lowerBlockId.contains("limestone")) {
            return OreRarity.COMMON;
        }
        
        // Uncommon ores
        if (lowerBlockId.contains("copper") || lowerBlockId.contains("tin")) {
            return OreRarity.UNCOMMON;
        }
        
        // Rare ores
        if (lowerBlockId.contains("iron") || lowerBlockId.contains("silver")) {
            return OreRarity.RARE;
        }
        
        // Epic ores
        if (lowerBlockId.contains("gold") || lowerBlockId.contains("feranite")) {
            return OreRarity.EPIC;
        }
        
        // Legendary ores
        if (lowerBlockId.contains("mithril") || lowerBlockId.contains("varyn")) {
            return OreRarity.LEGENDARY;
        }
        
        return null; // Not an ore
    }
}
