package com.veilcore.trackers;

import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Tracks and updates player playtime for active profiles.
 * Runs every second to increment playtime, saves every 60 seconds to avoid log spam.
 */
public class PlaytimeTracker implements Runnable {
    
    private final VeilCorePlugin plugin;
    private int tickCounter = 0;
    private static final int SAVE_INTERVAL = 60; // Save every 60 seconds
    
    public PlaytimeTracker(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        try {
            tickCounter++;
            boolean shouldSave = (tickCounter >= SAVE_INTERVAL);
            
            // Get all players with active profiles and increment their playtime
            for (UUID playerUUID : plugin.getProfileManager().getActivePlayers()) {
                UUID activeProfileId = plugin.getProfileManager().getActiveProfileId(playerUUID);
                if (activeProfileId != null) {
                    Profile profile = plugin.getProfileManager().getProfile(playerUUID, activeProfileId);
                    if (profile != null) {
                        // Increment playtime by 1 second (in memory)
                        profile.getStats().incrementPlayTime(1);
                        
                        // Save to disk every 60 seconds
                        if (shouldSave) {
                            plugin.getProfileManager().saveProfile(profile);
                        }
                    }
                }
            }
            
            // Reset counter after save
            if (shouldSave) {
                tickCounter = 0;
            }
        } catch (Exception e) {
            plugin.getLogger().at(Level.WARNING).log("Error tracking playtime: " + e.getMessage());
        }
    }
}
