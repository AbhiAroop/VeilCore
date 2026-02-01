package com.veilcore.skills.subskills.woodcutting;

import com.veilcore.skills.Skill;
import com.veilcore.skills.subskills.Subskill;

/**
 * Tree Felling subskill for Woodcutting
 * Awards XP when cutting down trees based on tree size (number of logs) and wood type rarity
 */
public class TreeFelling extends Subskill {
    
    public static final String ID = "tree_felling";
    
    public enum TreeSize {
        SAPLING(2, 5),          // 1-3 logs
        SMALL(4, 15),           // 4-7 logs
        MEDIUM(8, 30),          // 8-15 logs
        LARGE(16, 60),          // 16-31 logs
        MASSIVE(32, 120);       // 32+ logs
        
        private final int minLogs;
        private final long xpAmount;
        
        TreeSize(int minLogs, long xpAmount) {
            this.minLogs = minLogs;
            this.xpAmount = xpAmount;
        }
        
        public int getMinLogs() {
            return minLogs;
        }
        
        public long getXpAmount() {
            return xpAmount;
        }
    }
    
    public enum WoodRarity {
        // Common woods - 1.0x multiplier
        COMMON(1.0, "Oak", "Birch", "Fir", "Ash", "Aspen", "Beech", "Maple"),
        
        // Uncommon woods - 1.25x multiplier
        UNCOMMON(1.25, "Cedar", "Jungle", "Palm", "Bamboo", "Sallow", "Camphor"),
        
        // Rare woods - 1.5x multiplier
        RARE(1.5, "Redwood", "Banyan", "Gumboab", "Bottletree", "Palo", "Windwillow"),
        
        // Epic woods - 2.0x multiplier
        EPIC(2.0, "Amber", "Azure", "Crystal", "Wisteria", "Spiral", "Stormbark"),
        
        // Legendary woods - 2.5x multiplier
        LEGENDARY(2.5, "Fire", "Ice", "Petrified", "Poisoned", "Burnt", "Dry", "Gnarled");
        
        private final double multiplier;
        private final String[] woodTypes;
        
        WoodRarity(double multiplier, String... woodTypes) {
            this.multiplier = multiplier;
            this.woodTypes = woodTypes;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
        
        public String[] getWoodTypes() {
            return woodTypes;
        }
        
        /**
         * Get wood rarity from block ID
         * @param blockId The block ID to check
         * @return The wood rarity, defaults to COMMON if not found
         */
        public static WoodRarity fromBlockId(String blockId) {
            if (blockId == null) return COMMON;
            
            String lowerBlockId = blockId.toLowerCase();
            
            // Check each rarity tier
            for (WoodRarity rarity : values()) {
                for (String woodType : rarity.getWoodTypes()) {
                    if (lowerBlockId.contains(woodType.toLowerCase())) {
                        return rarity;
                    }
                }
            }
            
            return COMMON;
        }
    }
    
    public TreeFelling() {
        super(
            ID,
            "Tree Felling",
            "Gain woodcutting XP when breaking wood blocks. Rarer wood types grant more XP.",
            Skill.WOODCUTTING,
            SubskillType.PASSIVE
        );
    }
    
    /**
     * Calculate XP for breaking a single wood block based on wood type
     * @param woodType The type of wood (block ID)
     * @return XP amount to award
     */
    public static long calculateXpPerBlock(String woodType) {
        WoodRarity rarity = WoodRarity.fromBlockId(woodType);
        
        // Base XP per wood block is 2, multiplied by wood rarity
        long baseXp = 2;
        return (long) (baseXp * rarity.getMultiplier());
    }
    
    /**
     * Calculate XP for cutting down a tree based on size and wood type
     * @param logCount The number of logs destroyed in the tree
     * @param woodType The type of wood (block ID)
     * @return XP amount to award
     */
    public static long calculateXp(int logCount, String woodType) {
        TreeSize size = getTreeSize(logCount);
        WoodRarity rarity = WoodRarity.fromBlockId(woodType);
        
        // Base XP from tree size, multiplied by wood rarity
        return (long) (size.getXpAmount() * rarity.getMultiplier());
    }
    
    /**
     * Get tree size category from log count
     * @param logCount The number of logs in the tree
     * @return The tree size category
     */
    public static TreeSize getTreeSize(int logCount) {
        if (logCount >= TreeSize.MASSIVE.getMinLogs()) {
            return TreeSize.MASSIVE;
        } else if (logCount >= TreeSize.LARGE.getMinLogs()) {
            return TreeSize.LARGE;
        } else if (logCount >= TreeSize.MEDIUM.getMinLogs()) {
            return TreeSize.MEDIUM;
        } else if (logCount >= TreeSize.SMALL.getMinLogs()) {
            return TreeSize.SMALL;
        }
        return TreeSize.SAPLING;
    }
    
    /**
     * Check if a block ID is a wood/log block
     * @param blockId The block ID to check
     * @return true if it's a wood block, false otherwise
     */
    public static boolean isWoodBlock(String blockId) {
        if (blockId == null) return false;
        
        String lowerBlockId = blockId.toLowerCase();
        
        // Check for various wood block types
        return lowerBlockId.contains("wood") || 
               lowerBlockId.contains("log") ||
               lowerBlockId.contains("tree") ||
               lowerBlockId.startsWith("block_wood");
    }
    
    /**
     * Check if a block is a leaves block (not counted as logs but part of tree)
     * @param blockId The block ID to check
     * @return true if it's a leaves block, false otherwise
     */
    public static boolean isLeavesBlock(String blockId) {
        if (blockId == null) return false;
        
        String lowerBlockId = blockId.toLowerCase();
        
        // Check for various leaves block types
        return lowerBlockId.contains("leaves") ||
               lowerBlockId.contains("leaf") ||
               lowerBlockId.startsWith("block_leaves");
    }
}
