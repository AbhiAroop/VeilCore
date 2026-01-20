package com.veilcore.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;
import com.veilcore.skills.Skill;
import com.veilcore.skills.SkillLevel;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.subskills.mining.OreExtraction;

import javax.annotation.Nonnull;

/**
 * Listens for block break events using ESC event system
 * Awards mining XP when players break ore blocks
 */
public class BlockBreakListener {

    /**
     * Called when a block is broken
     * This is an ESC event handler
     * 
     * @param blockId The ID of the block that was broken
     * @param ref Reference to the entity (player) who broke the block
     * @param store Entity store containing entity components
     */
    public static void onBlockBreak(@Nonnull String blockId, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        // Get the player who broke the block
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        
        // Get player's profile
        VeilCorePlugin plugin = VeilCorePlugin.getInstance();
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        if (profile == null) {
            return; // Player doesn't have an active profile
        }
        
        // Check if the broken block is an ore
        OreExtraction.OreRarity rarity = OreExtraction.getOreRarity(blockId);
        if (rarity == null) {
            return; // Not an ore block, no XP
        }
        
        // Calculate XP from ore rarity
        long xpGained = OreExtraction.calculateXp(rarity);
        
        // Get current level before adding XP
        ProfileSkills skills = profile.getSkills();
        SkillLevel miningLevel = skills.getSkillLevel(Skill.MINING);
        int oldLevel = miningLevel.getLevel();
        
        // Add XP to mining skill
        int levelsGained = skills.addXp(Skill.MINING, xpGained);
        
        // Save profile
        plugin.getProfileManager().saveProfile(profile);
        
        // Get PlayerRef for notifications
        PlayerRef playerRef = Universe.get().getPlayer(player.getUuid());
        if (playerRef == null) {
            return;
        }
        
        PacketHandler packetHandler = playerRef.getPacketHandler();
        
        // Send XP gained notification
        Message primaryMsg = Message.raw(String.format("+%d Mining XP", xpGained))
            .color("#FFD700")
            .bold(true);
        Message secondaryMsg = Message.raw(String.format("Ore Extraction: %s ore", rarity.name()))
            .color("#FFFFFF");
        
        ItemWithAllMetadata icon = new ItemStack("Rubble_Calcite_Medium", 1).toPacket();
        NotificationUtil.sendNotification(
            packetHandler,
            primaryMsg,
            secondaryMsg,
            icon
        );
        
        // Send level up notification if leveled up
        if (levelsGained > 0) {
            int newLevel = oldLevel + levelsGained;
            
            Message levelUpPrimary = Message.raw(String.format("Mining Level %d", newLevel))
                .color("#FFD700")
                .bold(true);
            Message levelUpSecondary = Message.raw(String.format("Level up! +%d level%s", 
                levelsGained, levelsGained > 1 ? "s" : ""))
                .color("#FFFFFF");
            
            NotificationUtil.sendNotification(
                packetHandler,
                levelUpPrimary,
                levelUpSecondary,
                icon
            );
        }
    }
}
