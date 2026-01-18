package com.veilcore.trackers;

import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Tracks and updates player playtime for active profiles.
 * Runs every second to increment playtime for all online players with active profiles.
 */
public class PlaytimeTracker implements Runnable {
    
    private final VeilCorePlugin plugin;
    
    public PlaytimeTracker(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        try {
            // Get all players with active profiles and increment their playtime
            for (UUID playerUUID : plugin.getProfileManager().getActivePlayers()) {
                UUID activeProfileId = plugin.getProfileManager().getActiveProfileId(playerUUID);
                if (activeProfileId != null) {
                    Profile profile = plugin.getProfileManager().getProfile(playerUUID, activeProfileId);
                    if (profile != null) {
                        // Increment playtime by 1 second
                        profile.getStats().incrementPlayTime(1);
                        // Save the profile to persist the playtime change
                        plugin.getProfileManager().saveProfile(profile);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().at(Level.WARNING).log("Error tracking playtime: " + e.getMessage());
        }
    }
}
