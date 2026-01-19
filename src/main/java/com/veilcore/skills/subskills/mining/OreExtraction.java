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
        COMMON(5),      // Coal, copper
        UNCOMMON(10),   // Iron, tin
        RARE(25),       // Gold, silver
        EPIC(50),       // Diamond, emerald
        LEGENDARY(100); // Ancient debris, mythril
        
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
     * @param blockId The block ID (e.g., "Ore_Coal", "Ore_Diamond")
     * @return The ore rarity, or null if not an ore
     */
    public static OreRarity getOreRarity(String blockId) {
        if (blockId == null) return null;
        
        String lowerBlockId = blockId.toLowerCase();
        
        // Common ores
        if (lowerBlockId.contains("coal") || lowerBlockId.contains("copper")) {
            return OreRarity.COMMON;
        }
        
        // Uncommon ores
        if (lowerBlockId.contains("iron") || lowerBlockId.contains("tin")) {
            return OreRarity.UNCOMMON;
        }
        
        // Rare ores
        if (lowerBlockId.contains("gold") || lowerBlockId.contains("silver")) {
            return OreRarity.RARE;
        }
        
        // Epic ores
        if (lowerBlockId.contains("diamond") || lowerBlockId.contains("emerald")) {
            return OreRarity.EPIC;
        }
        
        // Legendary ores
        if (lowerBlockId.contains("ancient") || lowerBlockId.contains("mythril") || 
            lowerBlockId.contains("mithril") || lowerBlockId.contains("debris")) {
            return OreRarity.LEGENDARY;
        }
        
        return null; // Not an ore
    }
}
