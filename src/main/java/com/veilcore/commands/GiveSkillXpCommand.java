package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.Skill;
import com.veilcore.skills.notifications.SkillLevelUpNotifier;

import javax.annotation.Nonnull;

/**
 * Admin command to give skill XP to a player (or self)
 * Usage: /giveskillxp <skill> <amount> [player]
 */
public class GiveSkillXpCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;
    private final RequiredArg<String> skillArg;
    private final RequiredArg<Integer> amountArg;
    private final OptionalArg<PlayerRef> targetArg;

    public GiveSkillXpCommand(VeilCorePlugin plugin) {
        super("giveskillxp", "Give skill XP to a player (admin)");
        this.plugin = plugin;
        
        // Define arguments
        this.skillArg = withRequiredArg("skill", "The skill to give XP to (mining, combat, farming, fishing)", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount of XP to give", ArgTypes.INTEGER);
        this.targetArg = withOptionalArg("player", "Target player (defaults to self)", ArgTypes.PLAYER_REF);
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
        PlayerRef targetPlayerRef = context.provided(targetArg) ? context.get(targetArg) : playerRef;
        
        // Parse skill
        Skill skill;
        try {
            skill = Skill.valueOf(skillName.toUpperCase());
        } catch (IllegalArgumentException e) {
            playerRef.sendMessage(Message.raw("§cInvalid skill! Use: mining, combat, farming, or fishing"));
            return;
        }
        
        // Get target player's profile
        Player targetPlayer = store.getComponent(targetPlayerRef.getReference(), Player.getComponentType());
        if (targetPlayer == null) {
            playerRef.sendMessage(Message.raw("§cTarget player not found!"));
            return;
        }
        
        Profile profile = plugin.getProfileManager().getActiveProfile(targetPlayer.getUuid());
        if (profile == null) {
            playerRef.sendMessage(Message.raw("§cTarget player doesn't have an active profile!"));
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
            String msg = String.format("§a+%d %s XP §7(Level %d: %d/%d XP)",
                xpAmount,
                skill.getDisplayName(),
                newLevel,
                skills.getXp(skill),
                skills.getXpToNextLevel(skill)
            );
            targetPlayerRef.sendMessage(Message.raw(msg));
        }
        
        // Notify command sender if different from target
        if (!targetPlayerRef.equals(playerRef)) {
            playerRef.sendMessage(Message.raw(String.format("§aGave %d %s XP to %s",
                xpAmount,
                skill.getDisplayName(),
                targetPlayer.getDisplayName()
            )));
        }
    }
}
