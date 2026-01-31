package com.veilcore.skills.subskills.woodcutting;

import com.veilcore.skills.Skill;
import com.veilcore.skills.subskills.Subskill;

/**
 * Tree Felling subskill for Woodcutting
 * Awards XP when cutting down trees based on tree size (number of logs)
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
    
    public TreeFelling() {
        super(
            ID,
            "Tree Felling",
            "Gain woodcutting XP when cutting down trees. Larger trees grant more XP.",
            Skill.WOODCUTTING,
            SubskillType.PASSIVE
        );
    }
    
    /**
     * Calculate XP for cutting down a tree based on size
     * @param logCount The number of logs destroyed in the tree
     * @return XP amount to award
     */
    public static long calculateXp(int logCount) {
        TreeSize size = getTreeSize(logCount);
        return size.getXpAmount();
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
