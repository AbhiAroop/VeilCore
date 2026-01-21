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
        COMMON(5),      // Copper
        UNCOMMON(10),   // Iron
        RARE(25),       // Gold
        EPIC(50),       // Adamantite
        LEGENDARY(100); // Thorium
        
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
     * @param blockId The block ID (e.g., "Ore_Copper", "Ore_Thorium")
     * @return The ore rarity, or null if not an ore
     */
    public static OreRarity getOreRarity(String blockId) {
        if (blockId == null) return null;
        
        String lowerBlockId = blockId.toLowerCase();
        
        // Common ores - Copper (includes all variants: Ore_Copper, Ore_Copper_Basalt, Ore_Copper_Volcanic)
        if (lowerBlockId.startsWith("ore_copper")) {
            return OreRarity.COMMON;
        }
        
        // Uncommon ores - Iron (includes all variants: Ore_Iron, Ore_Iron_Slate)
        if (lowerBlockId.startsWith("ore_iron")) {
            return OreRarity.UNCOMMON;
        }
        
        // Rare ores - Gold (includes all variants: Ore_Gold, Ore_Gold_Volcanic)
        if (lowerBlockId.startsWith("ore_gold")) {
            return OreRarity.RARE;
        }
        
        // Epic ores - Adamantite (includes all variants: Ore_Adamantite, Ore_Adamantite_Basalt)
        if (lowerBlockId.startsWith("ore_adamantite")) {
            return OreRarity.EPIC;
        }
        
        // Legendary ores - Thorium (includes all variants: Ore_Thorium, Ore_Thorium_Stone)
        if (lowerBlockId.startsWith("ore_thorium")) {
            return OreRarity.LEGENDARY;
        }
        
        return null; // Not an ore
    }
}
