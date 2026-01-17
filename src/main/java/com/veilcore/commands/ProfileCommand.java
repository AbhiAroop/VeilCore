package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
        
        // Check if player has any profiles
        List<Profile> profiles = VeilCorePlugin.getInstance().getProfileManager()
            .getProfiles(player.getUuid());
        
        if (profiles.isEmpty()) {
            // No profiles - open FORCED creation page (cannot cancel)
            ProfileCreationPage page = new ProfileCreationPage(playerRef, false);
            player.getPageManager().openCustomPage(ref, store, page);
        } else {
            // Has profiles - check if player has an active profile loaded
            Profile activeProfile = VeilCorePlugin.getInstance().getProfileManager()
                .getActiveProfile(player.getUuid());
            
            if (activeProfile == null) {
                // No active profile - try to load last active profile
                java.util.UUID lastActiveId = VeilCorePlugin.getInstance().getProfileManager()
                    .getLastActiveProfileId(player.getUuid());
                
                if (lastActiveId != null) {
                    Profile lastProfile = VeilCorePlugin.getInstance().getProfileManager()
                        .getProfile(player.getUuid(), lastActiveId);
                    
                    if (lastProfile != null) {
                        // Load the last active profile
                        VeilCorePlugin.getInstance().getProfileManager()
                            .setActiveProfile(player.getUuid(), lastProfile.getProfileId());
                        VeilCorePlugin.getInstance().getStateManager()
                            .loadProfileStateToPlayer(ref, store, player, lastProfile);
                        
                        // Open selection page to show the loaded profile
                        ProfileSelectionPage page = new ProfileSelectionPage(playerRef, profiles);
                        player.getPageManager().openCustomPage(ref, store, page);
                        return;
                    }
                }
                
                // No last active profile or it was deleted - open selection page
                ProfileSelectionPage page = new ProfileSelectionPage(playerRef, profiles);
                player.getPageManager().openCustomPage(ref, store, page);
            } else {
                // Has active profile - open selection page
                ProfileSelectionPage page = new ProfileSelectionPage(playerRef, profiles);
                player.getPageManager().openCustomPage(ref, store, page);
            }
        }
    }
}
