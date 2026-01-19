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

import javax.annotation.Nonnull;

/**
 * Admin command to set a player's skill level
 * Usage: /setskilllevel <skill> <level> [player]
 */
public class SetSkillLevelCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;

    public SetSkillLevelCommand(VeilCorePlugin plugin) {
        super("setskilllevel", "Set a player's skill level (admin)");
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
        
        // Simplified version: Set Mining to level 10 (args not working yet)
        // TODO: Add command args when API supports it
        Skill skill = Skill.MINING;
        int level = 10;
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        
        if (profile == null) {
            playerRef.sendMessage(Message.raw("§cYou don't have an active profile!"));
            return;
        }

        ProfileSkills skills = profile.getSkills();
        int oldLevel = skills.getLevel(skill);
        
        // Set level and reset XP
        skills.setLevel(skill, level);
        skills.setXp(skill, 0);

        // Save profile
        plugin.getProfileManager().saveProfile(profile);

        String msg = String.format("§aSet %s level: §7%d §8→ §a%d",
            skill.getDisplayName(),
            oldLevel,
            level
        );
        playerRef.sendMessage(Message.raw(msg));
        
        // Show token info
        int totalTokens = skills.getTreeData().getAllTokenCounts(skill.getId())
            .values().stream().mapToInt(Integer::intValue).sum();
        playerRef.sendMessage(Message.raw("§7Available tokens: §e" + totalTokens));
    }
}
