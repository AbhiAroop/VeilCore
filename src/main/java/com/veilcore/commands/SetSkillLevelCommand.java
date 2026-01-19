package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
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

import javax.annotation.Nonnull;

/**
 * Admin command to set a player's skill level
 * Usage: /setskilllevel <skill> <level> [player]
 */
public class SetSkillLevelCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;
    private final RequiredArg<String> skillArg;
    private final RequiredArg<Integer> levelArg;
    private final DefaultArg<String> targetArg;

    public SetSkillLevelCommand(VeilCorePlugin plugin) {
        super("setskilllevel", "Set a player's skill level (admin)");
        this.plugin = plugin;
        
        // Define arguments
        this.skillArg = withRequiredArg("skill", "The skill to set (mining, combat, farming, fishing)", ArgTypes.STRING);
        this.levelArg = withRequiredArg("level", "Level to set (1-100)", ArgTypes.INTEGER);
        this.targetArg = withDefaultArg("player", "Target player name (defaults to self)", ArgTypes.STRING, null, "self");
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
        
        // Determine target player
        PlayerRef targetPlayerRef;
        Player targetPlayer;
        
        String targetName = context.get(targetArg);
        if (targetName != null && !targetName.isEmpty()) {
            // Target another player by name
            targetPlayerRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            
            if (targetPlayerRef == null) {
                playerRef.sendMessage(Message.raw("Player '" + targetName + "' not found or not online!").color("#FF5555"));
                return;
            }
            
            targetPlayer = store.getComponent(targetPlayerRef.getReference(), Player.getComponentType());
        } else {
            // Target self
            targetPlayerRef = playerRef;
            targetPlayer = store.getComponent(ref, Player.getComponentType());
        }
        
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

        String msg = String.format("Set %s level: %d -> %d",
            skill.getDisplayName(),
            oldLevel,
            level
        );
        targetPlayerRef.sendMessage(Message.raw(msg).color("#55FF55"));
        
        // Show token info to target
        int totalTokens = skills.getTreeData().getAllTokenCounts(skill.getId())
            .values().stream().mapToInt(Integer::intValue).sum();
        targetPlayerRef.sendMessage(Message.raw("Available tokens: " + totalTokens).color("#AAAAAA"));
        
        // Notify command sender if different from target
        if (!targetPlayerRef.equals(playerRef)) {
            playerRef.sendMessage(Message.raw(String.format("Set %s's %s level to %d",
                targetPlayer.getDisplayName(),
                skill.getDisplayName(),
                level
            )).color("#55FF55"));
        }
    }
}
