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
        
        // Dynamic UIs not supported yet - show profile info via chat
        List<Profile> profiles = VeilCorePlugin.getInstance().getProfileManager()
            .getProfiles(player.getUuid());
        
        if (profiles.isEmpty()) {
            playerRef.sendMessage(Message.raw("No profiles found. Profile system coming soon!").color("#FF5555"));
        } else {
            Profile activeProfile = VeilCorePlugin.getInstance().getProfileManager()
                .getActiveProfile(player.getUuid());
                
            playerRef.sendMessage(Message.raw("=== Your Profiles ===").color("#FFD700").bold(true));
            
            for (Profile profile : profiles) {
                String marker = (activeProfile != null && profile.getProfileId().equals(activeProfile.getProfileId())) ? " (ACTIVE)" : "";
                playerRef.sendMessage(Message.raw("  - " + profile.getProfileName() + marker).color("#FFFFFF"));
            }
            
            playerRef.sendMessage(Message.raw("Profile UI coming soon!").color("#AAAAAA"));
        }
    }
}
