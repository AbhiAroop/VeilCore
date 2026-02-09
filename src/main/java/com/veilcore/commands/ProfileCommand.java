package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;
import com.veilcore.VeilCorePlugin;
import com.veilcore.pages.ProfileCreationPage;
import com.veilcore.pages.ProfileSelectionPage;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Command to open the profile management UI.
 */
public class ProfileCommand extends AbstractPlayerCommand {

    public ProfileCommand() {
        super("profile", "Manage your profiles");
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
        
        List<Profile> profiles = VeilCorePlugin.getInstance().getProfileManager()
            .getProfiles(player.getUuid());
        
        if (profiles.isEmpty()) {
            // No profiles exist - open creation page (not cancellable - player must create one)
            ProfileCreationPage creationPage = new ProfileCreationPage(playerRef, false);
            player.getPageManager().openCustomPage(ref, store, creationPage);
        } else {
            // Profiles exist - open selection/management page
            ProfileSelectionPage selectionPage = new ProfileSelectionPage(playerRef, profiles);
            player.getPageManager().openCustomPage(ref, store, selectionPage);
        }
    }
}
