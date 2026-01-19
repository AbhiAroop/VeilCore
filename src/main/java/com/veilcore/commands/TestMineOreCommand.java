package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
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
 * Test command to simulate mining ore blocks
 * This is temporary until BlockBreakEvent is available in the Hytale API
 * Usage: /testmineore <rarity>
 */
public class TestMineOreCommand extends AbstractPlayerCommand {
    
    private final VeilCorePlugin plugin;
    private final RequiredArg<String> rarityArg;
    
    public TestMineOreCommand(VeilCorePlugin plugin) {
        super("testmineore", "Test ore extraction subskill by simulating mining an ore");
        this.plugin = plugin;
        
        this.rarityArg = withRequiredArg("rarity", 
            "Ore rarity: common, uncommon, rare, epic, legendary", 
            ArgTypes.STRING);
    }
    
    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        
        if (profile == null) {
            playerRef.sendMessage(Message.raw("You don't have an active profile!").color("#FF5555"));
            return;
        }
        
        String rarityName = context.get(rarityArg);
        OreExtraction.OreRarity rarity;
        
        try {
            rarity = OreExtraction.OreRarity.valueOf(rarityName.toUpperCase());
        } catch (IllegalArgumentException e) {
            playerRef.sendMessage(Message.raw("Invalid rarity! Use: common, uncommon, rare, epic, legendary").color("#FF5555"));
            return;
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
        
        // Send XP gained notification
        PacketHandler packetHandler = playerRef.getPacketHandler();
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
        
        // Send chat confirmation
        playerRef.sendMessage(Message.raw(String.format("Mined %s ore! +%d XP (Level: %d)", 
            rarity.name(), xpGained, miningLevel.getLevel())).color("#55FF55"));
    }
}
