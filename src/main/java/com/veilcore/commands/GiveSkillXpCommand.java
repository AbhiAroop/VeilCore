package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
            String msg = String.format("+%d %s XP (Level %d: %d/%d XP)",
                xpAmount,
                skill.getDisplayName(),
                newLevel,
                skills.getXp(skill),
                skills.getXpToNextLevel(skill)
            );
            targetPlayerRef.sendMessage(Message.raw(msg).color("#55FF55"));
        }
        
        // Notify command sender
        playerRef.sendMessage(Message.raw(String.format("Gave %d %s XP to %s",
            xpAmount,
            skill.getDisplayName(),
            targetPlayer.getDisplayName()
        )).color("#55FF55"));
    }
}