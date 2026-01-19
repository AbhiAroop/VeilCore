package com.veilcore.skills.notifications;

import java.util.Map;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;
import com.veilcore.skills.trees.PlayerSkillTreeData;

/**
 * Handles skill level-up notifications to players
 * Shows level gained, tokens awarded, and progress milestones
 */
public class SkillLevelUpNotifier {

    /**
     * Notify player of skill level-up
     * @param playerRef The player to notify
     * @param skill The skill that leveled up
     * @param newLevel The new skill level
     * @param levelsGained Number of levels gained (usually 1, but can be more)
     * @param treeData Player's skill tree data (for checking token counts)
     */
    public void notifyLevelUp(PlayerRef playerRef, Skill skill, int newLevel, 
                              int levelsGained, PlayerSkillTreeData treeData) {
        
        // Header
        String header = "================================";
        playerRef.sendMessage(Message.raw(header).color("#888888").bold(true));
        playerRef.sendMessage(Message.raw(""));
        
        // Main level-up message
        String levelUpMsg = "      " + skill.getSymbol() + " " + skill.getDisplayName().toUpperCase() + 
                          " LEVEL UP! " + skill.getSymbol();
        playerRef.sendMessage(Message.raw(levelUpMsg).color("#FFFFFF").bold(true));
        
        // Level progression
        int oldLevel = newLevel - levelsGained;
        String levelProgress = "        " + oldLevel + " -> " + newLevel;
        playerRef.sendMessage(Message.raw(levelProgress).color("#55FF55").bold(true));
        
        playerRef.sendMessage(Message.raw(""));
        
        // Check for token rewards
        Map<TokenTier, Integer> tokensAwarded = calculateTokensAwarded(oldLevel, newLevel);
        if (!tokensAwarded.isEmpty()) {
            playerRef.sendMessage(Message.raw("   REWARDS").color("#FFD700").bold(true));
            playerRef.sendMessage(Message.raw(""));
            
            for (Map.Entry<TokenTier, Integer> entry : tokensAwarded.entrySet()) {
                TokenTier tier = entry.getKey();
                int count = entry.getValue();
                
                String tokenMsg = "      " + tier.getSymbol() + " +" + count + " " +
                                tier.getDisplayName() + " Token" + (count > 1 ? "s" : "");
                playerRef.sendMessage(Message.raw(tokenMsg).color(tier.getColor()));
            }
            
            playerRef.sendMessage(Message.raw(""));
            
            // Show total tokens
            Map<TokenTier, Integer> totalTokens = treeData.getAllTokenCounts(skill.getId());
            int total = totalTokens.values().stream().mapToInt(Integer::intValue).sum();
            String totalMsg = "      Total Tokens: " + total;
            playerRef.sendMessage(Message.raw(totalMsg).color("#AAAAAA"));
        }
        
        playerRef.sendMessage(Message.raw(""));
        
        // Next milestone
        int nextMilestone = getNextMilestone(newLevel);
        if (nextMilestone > 0) {
            String milestoneMsg = "   Next reward at level " + nextMilestone;
            playerRef.sendMessage(Message.raw(milestoneMsg).color("#AAAAAA"));
        } else {
            playerRef.sendMessage(Message.raw("   MAX LEVEL REACHED!").color("#FFD700").bold(true));
        }
        
        playerRef.sendMessage(Message.raw(""));
        playerRef.sendMessage(Message.raw(header).color("#888888").bold(true));
    }

    /**
     * Calculate which tokens were awarded in this level range
     */
    private Map<TokenTier, Integer> calculateTokensAwarded(int fromLevel, int toLevel) {
        Map<TokenTier, Integer> tokens = new java.util.HashMap<>();
        
        for (int level = fromLevel + 1; level <= toLevel; level++) {
            if (level % 5 == 0) { // Award every 5 levels
                TokenTier tier;
                if (level >= 70) {
                    tier = TokenTier.MASTER;
                } else if (level >= 30) {
                    tier = TokenTier.ADVANCED;
                } else {
                    tier = TokenTier.BASIC;
                }
                
                tokens.put(tier, tokens.getOrDefault(tier, 0) + 1);
            }
        }
        
        return tokens;
    }

    /**
     * Get the next level milestone that awards tokens
     */
    private int getNextMilestone(int currentLevel) {
        if (currentLevel >= 100) {
            return 0; // Max level
        }
        
        // Next multiple of 5
        return ((currentLevel / 5) + 1) * 5;
    }

    /**
     * Send a compact XP gain notification (optional, for frequent updates)
     */
    public void notifyXpGain(PlayerRef playerRef, Skill skill, long xpGained, long currentXp, long xpNeeded) {
        double percent = (double) currentXp / xpNeeded * 100;
        String xpMsg = String.format("+%d %s XP (%.1f%%)", xpGained, skill.getDisplayName(), percent);
        
        // Send as action bar instead of chat (less spammy)
        // For now, just send as message but could be enhanced with action bar API
        playerRef.sendMessage(Message.raw(xpMsg).color("#AAAAAA"));
    }
}
