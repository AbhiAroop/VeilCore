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
import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;
import com.veilcore.skills.trees.PlayerSkillTreeData;

import javax.annotation.Nonnull;

/**
 * Admin command to give skill tree tokens to a player.
 * Usage: /givetokens <skill> <tier> <amount> <player>
 */
public class GiveSkillTokensCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;
    private final RequiredArg<String> skillArg;
    private final RequiredArg<String> tierArg;
    private final RequiredArg<Integer> amountArg;
    private final RequiredArg<String> targetArg;

    public GiveSkillTokensCommand(VeilCorePlugin plugin) {
        super("givetokens", "Give skill tree tokens to a player");
        this.plugin = plugin;
        
        // Define arguments
        this.skillArg = withRequiredArg("skill", "Skill name: mining, combat, farming, fishing", ArgTypes.STRING);
        this.tierArg = withRequiredArg("tier", "Token tier: basic, advanced, master", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount of tokens to give", ArgTypes.INTEGER);
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
        String tierName = context.get(tierArg);
        int amount = context.get(amountArg);
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

        // Parse token tier
        TokenTier tier;
        try {
            tier = TokenTier.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException e) {
            playerRef.sendMessage(Message.raw("Invalid tier! Use: basic, advanced, or master").color("#FF5555"));
            return;
        }

        // Get target's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(targetPlayer.getUuid());
        if (profile == null) {
            playerRef.sendMessage(Message.raw(targetName + " doesn't have an active profile!").color("#FF5555"));
            return;
        }

        // Add tokens
        PlayerSkillTreeData treeData = profile.getSkills().getTreeData();
        treeData.addTokens(skill.getId(), tier, amount);

        // Save profile
        plugin.getProfileManager().saveProfile(profile);

        // Notify target player
        PacketHandler targetPacket = targetPlayerRef.getPacketHandler();
        Message targetPrimary = Message.raw(String.format("+%d %s %s Tokens", amount, tier.getDisplayName(), skill.getDisplayName()))
            .color("#FFD700").bold(true);
        Message targetSecondary = Message.raw(String.format("Total: %d tokens", 
            treeData.getTokenCount(skill.getId(), tier))).color("#FFFFFF");
        ItemWithAllMetadata targetIcon = new ItemStack(getSkillIcon(skill), 1).toPacket();
        NotificationUtil.sendNotification(targetPacket, targetPrimary, targetSecondary, targetIcon);

        // Notify command sender
        PacketHandler senderPacket = playerRef.getPacketHandler();
        Message senderPrimary = Message.raw("Tokens Awarded").color("#55FF55").bold(true);
        Message senderSecondary = Message.raw(String.format("+%d %s %s tokens to %s", 
            amount, tier.getDisplayName(), skill.getDisplayName(), targetName)).color("#FFFFFF");
        ItemWithAllMetadata senderIcon = new ItemStack(getSkillIcon(skill), 1).toPacket();
        NotificationUtil.sendNotification(senderPacket, senderPrimary, senderSecondary, senderIcon);
    }

    private String getSkillIcon(Skill skill) {
        return switch (skill) {
            case MINING -> "Tool_Pickaxe_Stone";
            case COMBAT -> "Weapon_Sword_Mithril";
            case FARMING -> "Tool_Hoe_Wood";
            case WOODCUTTING -> "Tool_Hatchet_Iron";
            case FISHING -> "Weapon_Fishing_Rod";
        };
    }
}
