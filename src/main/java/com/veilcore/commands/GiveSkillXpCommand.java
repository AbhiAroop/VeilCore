package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
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

    public GiveSkillXpCommand(VeilCorePlugin plugin) {
        super("giveskillxp", "Give skill XP to a player (admin)");
        this.plugin = plugin;
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
        
        // Simplified version: Just award 1000 Mining XP (args not working yet)
        // TODO: Add command args when API supports it
        Skill skill = Skill.MINING;
        long xpAmount = 1000;
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        
        if (profile == null) {
            playerRef.sendMessage(Message.raw("§cYou don't have an active profile!"));
            return;
        }

        ProfileSkills skills = profile.getSkills();
        int oldLevel = skills.getLevel(skill);
        int levelsGained = skills.addXp(skill, xpAmount);
        int newLevel = skills.getLevel(skill);

        // Save profile
        plugin.getProfileManager().saveProfile(profile);

        // Notify
        if (levelsGained > 0) {
            SkillLevelUpNotifier notifier = new SkillLevelUpNotifier();
            notifier.notifyLevelUp(playerRef, skill, newLevel, levelsGained, skills.getTreeData());
        } else {
            String msg = String.format("§a+%d %s XP §7(Level %d: %d/%d XP)",
                xpAmount,
                skill.getDisplayName(),
                newLevel,
                skills.getXp(skill),
                skills.getXpToNextLevel(skill)
            );
            playerRef.sendMessage(Message.raw(msg));
        }
    }
}
