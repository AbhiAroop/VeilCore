package com.veilcore.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.pages.ProfileCreationPage;
import com.veilcore.pages.ProfileSelectionPage;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Handles player connection/disconnection events for automatic profile management.
 */
public class PlayerEventListener {

    /**
     * Called when a player is ready (fully loaded and spawned in the world).
     * This event provides the Player component directly.
     *
     * @param event The PlayerReadyEvent
     */
    public static void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        // Get Player from event
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null || player.getReference() == null) {
            return; // Player not fully ready
        }
        
        UUID playerUUID = player.getUuid();
        
        // Get PlayerRef from Universe
        PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
        if (playerRef == null) {
            return;
        }
        
        // Get Store and Ref from player
        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();
        Ref<EntityStore> ref = player.getReference();
        
        VeilCorePlugin plugin = VeilCorePlugin.getInstance();
        
        // Check if player has any profiles
        List<Profile> profiles = plugin.getProfileManager().getProfiles(playerUUID);
        
        if (profiles.isEmpty()) {
            // First-time player - force profile creation (non-cancellable)
            playerRef.sendMessage(Message.raw("§eWelcome! Please create your first profile to begin playing."));
            ProfileCreationPage creationPage = new ProfileCreationPage(playerRef, false); // false = cannot cancel
            player.getPageManager().openCustomPage(ref, store, creationPage);
        } else {
            // Returning player - check for last active profile
            UUID lastActiveId = plugin.getProfileManager().getLastActiveProfileId(playerUUID);
            
            if (lastActiveId != null) {
                // Auto-load last active profile
                Profile lastProfile = plugin.getProfileManager().getProfile(playerUUID, lastActiveId);
                if (lastProfile != null) {
                    plugin.getProfileManager().setActiveProfile(playerUUID, lastActiveId);
                    playerRef.sendMessage(Message.raw("§aWelcome back! Loaded profile: " + lastProfile.getProfileName()));
                    playerRef.sendMessage(Message.raw("§7Use /profile to switch profiles"));
                    return;
                }
            }
            
            // No valid last active profile - show selection
            playerRef.sendMessage(Message.raw("§eSelect a profile to continue:"));
            ProfileSelectionPage selectionPage = new ProfileSelectionPage(playerRef, profiles);
            player.getPageManager().openCustomPage(ref, store, selectionPage);
        }
    }

    /**
     * Called when a player disconnects from the server.
     * Save the last active profile for next join.
     *
     * @param event The PlayerDisconnectEvent
     */
    public static void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        UUID playerUUID = playerRef.getUuid();
        
        VeilCorePlugin plugin = VeilCorePlugin.getInstance();
        
        // Get active profile
        UUID activeProfileId = plugin.getProfileManager().getActiveProfileId(playerUUID);
        
        if (activeProfileId != null) {
            // Save as last active (already done by setActiveProfile, but ensure persistence)
            Profile activeProfile = plugin.getProfileManager().getProfile(playerUUID, activeProfileId);
            if (activeProfile != null) {
                plugin.getProfileManager().saveProfile(activeProfile);
            }
        }
        
        // Clear from memory
        plugin.getProfileManager().clearActiveProfile(playerUUID);
    }
}
