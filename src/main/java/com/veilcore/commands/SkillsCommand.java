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
import com.veilcore.pages.SkillsPageMining;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;

/**
 * Command to display player's skill levels and available tokens via UI
 */
public class SkillsCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;

    public SkillsCommand(VeilCorePlugin plugin) {
        super("skills", "View your skill levels and available tokens");
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
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());

        if (profile == null) {
            playerRef.sendMessage(Message.raw("§cYou don't have an active profile!"));
            return;
        }

        // Open the Skills UI page
        SkillsPageMining skillsPage = new SkillsPageMining(playerRef, profile);
        player.getPageManager().openCustomPage(ref, store, skillsPage);
    }
}

