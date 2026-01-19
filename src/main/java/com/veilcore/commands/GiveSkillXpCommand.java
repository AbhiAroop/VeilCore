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
import com.veilcore.skills.notifications.SkillLevelUpNotifier;

import javax.annotation.Nonnull;

/**
 * Admin command to give skill XP to a player
 * Usage: /giveskillxp <skill> <amount> <player>
 */
public class GiveSkillXpCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;
    private final RequiredArg<String> skillArg;
    private final RequiredArg<Integer> amountArg;
    private final RequiredArg<String> targetArg;

    public GiveSkillXpCommand(VeilCorePlugin plugin) {
        super("giveskillxp", "Give skill XP to a player");
        this.plugin = plugin;
        
        // Define arguments
        this.skillArg = withRequiredArg("skill", "Skill name: mining, combat, farming, fishing", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount of XP to award", ArgTypes.INTEGER);
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
        int xpAmount = context.get(amountArg);
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
        
        Profile profile = plugin.getProfileManager().getActiveProfile(targetPlayer.getUuid());
        if (profile == null) {
            playerRef.sendMessage(Message.raw("Target player doesn't have an active profile!").color("#FF5555"));
            return;
        }

        ProfileSkills skills = profile.getSkills();
        int oldLevel = skills.getLevel(skill);
        int levelsGained = skills.addXp(skill, xpAmount);
        int newLevel = skills.getLevel(skill);

        // Save profile
        plugin.getProfileManager().saveProfile(profile);

        // Notify target player
        if (levelsGained > 0) {
            SkillLevelUpNotifier notifier = new SkillLevelUpNotifier();
            notifier.notifyLevelUp(targetPlayerRef, skill, newLevel, levelsGained, skills.getTreeData());
        } else {
            PacketHandler targetPacket = targetPlayerRef.getPacketHandler();
            Message primary = Message.raw(String.format("+%d %s XP", xpAmount, skill.getDisplayName())).color("#55FF55");
            Message secondary = Message.raw(String.format("Level %d: %d/%d XP", newLevel, skills.getXp(skill), skills.getXpToNextLevel(skill))).color("#AAAAAA");
            String iconItem = getSkillIcon(skill);
            ItemWithAllMetadata icon = new ItemStack(iconItem, 1).toPacket();
            NotificationUtil.sendNotification(targetPacket, primary, secondary, icon);
        }
        
        // Notify command sender
        PacketHandler senderPacket = playerRef.getPacketHandler();
        Message senderPrimary = Message.raw("XP Awarded").color("#55FF55").bold(true);
        Message senderSecondary = Message.raw(String.format("+%d %s XP to %s", xpAmount, skill.getDisplayName(), targetPlayer.getDisplayName())).color("#FFFFFF");
        String iconItem = getSkillIcon(skill);
        ItemWithAllMetadata senderIcon = new ItemStack(iconItem, 1).toPacket();
        NotificationUtil.sendNotification(senderPacket, senderPrimary, senderSecondary, senderIcon);
    }

    private String getSkillIcon(Skill skill) {
        return switch (skill) {
            case MINING -> "Prefab_Stone";
            case COMBAT -> "Weapon_Sword_Mithril";
            case FARMING -> "Prefab_Wheat";
            case FISHING -> "Weapon_Fishing_Rod";
        };
    }
}