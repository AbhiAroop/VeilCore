package com.veilcore.skills.notifications;

import java.util.Map;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;
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
        
        PacketHandler packetHandler = playerRef.getPacketHandler();
        
        // Calculate tokens awarded
        Map<TokenTier, Integer> tokensAwarded = calculateTokensAwarded(newLevel - levelsGained, newLevel);
        
        // Primary message: Skill name and level
        String levelUpText = skill.getSymbol() + " " + skill.getDisplayName().toUpperCase() + " LEVEL UP!";
        Message primaryMessage = Message.raw(levelUpText).color("#FFFFFF").bold(true);
        
        // Secondary message: Level progression and tokens
        int oldLevel = newLevel - levelsGained;
        StringBuilder secondaryText = new StringBuilder();
        secondaryText.append(oldLevel).append(" -> ").append(newLevel);
        
        if (!tokensAwarded.isEmpty()) {
            secondaryText.append("  |  ");
            boolean first = true;
            for (Map.Entry<TokenTier, Integer> entry : tokensAwarded.entrySet()) {
                if (!first) secondaryText.append(", ");
                secondaryText.append("+").append(entry.getValue()).append(" ")
                             .append(entry.getKey().getSymbol());
                first = false;
            }
        }
        
        Message secondaryMessage = Message.raw(secondaryText.toString()).color("#55FF55");
        
        // Get skill icon item (using a placeholder - you can customize per skill)
        String iconItem = getSkillIconItem(skill);
        ItemWithAllMetadata icon = new ItemStack(iconItem, 1).toPacket();
        
        // Send notification
        NotificationUtil.sendNotification(packetHandler, primaryMessage, secondaryMessage, icon);
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
     * Get the icon item for a skill's notification
     */
    private String getSkillIconItem(Skill skill) {
        return switch (skill.getId()) {
            case "mining" -> "Prefab_Stone";
            case "combat" -> "Weapon_Sword_Mithril";
            case "farming" -> "Prefab_Wheat";
            case "fishing" -> "Weapon_Fishing_Rod";
            default -> "Prefab_Stone"; // fallback
        };
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
