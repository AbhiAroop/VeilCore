package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.Skill;

import javax.annotation.Nonnull;

/**
 * Admin command to set a player's skill level
 * Usage: /setskilllevel <skill> <level> <player>
 */
public class SetSkillLevelCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;
    private final RequiredArg<String> skillArg;
    private final RequiredArg<Integer> levelArg;
    private final RequiredArg<String> targetArg;

    public SetSkillLevelCommand(VeilCorePlugin plugin) {
        super("setskilllevel", "Set a player's skill level");
        this.plugin = plugin;
        
        // Define arguments
        this.skillArg = withRequiredArg("skill", "Skill name: mining, combat, farming, fishing", ArgTypes.STRING);
        this.levelArg = withRequiredArg("level", "Target level (1-100)", ArgTypes.INTEGER);
        this.targetArg = withRequiredArg("player", "Target player's username", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        // Parse arguments
        String skillName = context.get(skillArg);
        int level = context.get(levelArg);
        String targetName = context.get(targetArg);
        
        // Find target player
        PlayerRef targetPlayerRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        
        if (targetPlayerRef == null) {
            playerRef.sendMessage(Message.raw("Player '" + targetName + "' not found or not online!").color("#FF5555"));
            return;
        }
        
        Player targetPlayer = store.getComponent(targetPlayerRef.getReference(), Player.getComponentType());
        if (targetPlayer == null) {
            playerRef.sendMessage(Message.raw("Target player not found!").color("#FF5555"));
            return;
        }
        
        // Parse skill
        Skill skill;
        try {
            skill = Skill.valueOf(skillName.toUpperCase());
        } catch (IllegalArgumentException e) {
            playerRef.sendMessage(Message.raw("Invalid skill! Use: mining, combat, farming, or fishing").color("#FF5555"));
            return;
        }
        
        // Validate level
        if (level < 1 || level > 100) {
            playerRef.sendMessage(Message.raw("Level must be between 1 and 100!").color("#FF5555"));
            return;
        }
        
        Profile profile = plugin.getProfileManager().getActiveProfile(targetPlayer.getUuid());
        if (profile == null) {
            playerRef.sendMessage(Message.raw("Target player doesn't have an active profile!").color("#FF5555"));
            return;
        }

        ProfileSkills skills = profile.getSkills();
        int oldLevel = skills.getLevel(skill);
        
        // Set level and reset XP
        skills.setLevel(skill, level);
        skills.setXp(skill, 0);

        // Save profile
        plugin.getProfileManager().saveProfile(profile);

        PacketHandler targetPacket = targetPlayerRef.getPacketHandler();
        Message targetPrimary = Message.raw(skill.getDisplayName().toUpperCase() + " LEVEL SET").color("#FFD700").bold(true);
        
        int totalTokens = skills.getTreeData().getAllTokenCounts(skill.getId())
            .values().stream().mapToInt(Integer::intValue).sum();
        Message targetSecondary = Message.raw(String.format("%d -> %d  |  %d tokens available", oldLevel, level, totalTokens)).color("#FFFFFF");
        
        String iconItem = getSkillIcon(skill);
        ItemWithAllMetadata targetIcon = new ItemStack(iconItem, 1).toPacket();
        NotificationUtil.sendNotification(targetPacket, targetPrimary, targetSecondary, targetIcon);
        
        // Notify command sender
        PacketHandler senderPacket = playerRef.getPacketHandler();
        Message senderPrimary = Message.raw("Level Set").color("#FFD700").bold(true);
        Message senderSecondary = Message.raw(String.format("%s's %s: %d -> %d", targetPlayer.getDisplayName(), skill.getDisplayName(), oldLevel, level)).color("#FFFFFF");
        String senderIconItem = getSkillIcon(skill);
        ItemWithAllMetadata senderIcon = new ItemStack(senderIconItem, 1).toPacket();
        NotificationUtil.sendNotification(senderPacket, senderPrimary, senderSecondary, senderIcon);
    }

    private String getSkillIcon(Skill skill) {
        return switch (skill) {
            case MINING -> "Rubble_Calcite_Medium";
            case COMBAT -> "Weapon_Sword_Mithril";
            case FARMING -> "Plant_Crop_Pumpkin_Item";
            case FISHING -> "Food_Fish_Raw";
        };
    }
}