package com.veilcore.profile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Handles persistence of player profiles to/from JSON files.
 * Storage structure: plugins/VeilCore/profiles/{playerUUID}/{profileUUID}.json
 */
public class ProfileRepository {
    
    private final File profilesDir;
    private final Gson gson;
    private final Logger logger;
    
    public ProfileRepository(@Nonnull File dataFolder, @Nonnull Logger logger) {
        this.profilesDir = new File(dataFolder, "profiles");
        this.logger = logger;
        
        // Create profiles directory if it doesn't exist
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
        
        // Configure Gson with pretty printing and custom adapters
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();
    }
    
    /**
     * Save a profile to disk.
     *
     * @param profile The profile to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveProfile(@Nonnull Profile profile) {
        File playerDir = getPlayerDirectory(profile.getPlayerUUID());
        if (!playerDir.exists()) {
            playerDir.mkdirs();
        }
        
        File profileFile = new File(playerDir, profile.getProfileId().toString() + ".json");
        
        try (FileWriter writer = new FileWriter(profileFile)) {
            gson.toJson(profile, writer);
            logger.log(Level.INFO, "Saved profile: " + profile.getProfileName() + " (" + profile.getProfileId() + ")");
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save profile: " + profile.getProfileId(), e);
            return false;
        }
    }
    
    /**
     * Load a specific profile by ID.
     *
     * @param playerUUID The player's UUID
     * @param profileId The profile's UUID
     * @return The loaded profile, or null if not found
     */
    @Nullable
    public Profile loadProfile(@Nonnull UUID playerUUID, @Nonnull UUID profileId) {
        File playerDir = getPlayerDirectory(playerUUID);
        File profileFile = new File(playerDir, profileId.toString() + ".json");
        
        if (!profileFile.exists()) {
            return null;
        }
        
        try (FileReader reader = new FileReader(profileFile)) {
            Profile profile = gson.fromJson(reader, Profile.class);
            logger.log(Level.INFO, "Loaded profile: " + profile.getProfileName() + " (" + profileId + ")");
            return profile;
        } catch (IOException | JsonSyntaxException e) {
            logger.log(Level.SEVERE, "Failed to load profile: " + profileId, e);
            return null;
        }
    }
    
    /**
     * Load all profiles for a player.
     *
     * @param playerUUID The player's UUID
     * @return List of all profiles for the player (empty list if none exist)
     */
    @Nonnull
    public List<Profile> loadAllProfiles(@Nonnull UUID playerUUID) {
        File playerDir = getPlayerDirectory(playerUUID);
        List<Profile> profiles = new ArrayList<>();
        
        if (!playerDir.exists()) {
            return profiles;
        }
        
        File[] profileFiles = playerDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (profileFiles == null) {
            return profiles;
        }
        
        for (File file : profileFiles) {
            try (FileReader reader = new FileReader(file)) {
                Profile profile = gson.fromJson(reader, Profile.class);
                if (profile != null) {
                    profiles.add(profile);
                }
            } catch (IOException | JsonSyntaxException e) {
                logger.log(Level.WARNING, "Failed to load profile file: " + file.getName(), e);
            }
        }
        
        // Sort by last played time (most recent first)
        profiles.sort((p1, p2) -> p2.getLastPlayedAt().compareTo(p1.getLastPlayedAt()));
        
        return profiles;
    }
    
    /**
     * Delete a profile.
     *
     * @param playerUUID The player's UUID
     * @param profileId The profile's UUID to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteProfile(@Nonnull UUID playerUUID, @Nonnull UUID profileId) {
        File playerDir = getPlayerDirectory(playerUUID);
        File profileFile = new File(playerDir, profileId.toString() + ".json");
        
        if (!profileFile.exists()) {
            return false;
        }
        
        try {
            Files.delete(profileFile.toPath());
            logger.log(Level.INFO, "Deleted profile: " + profileId);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to delete profile: " + profileId, e);
            return false;
        }
    }
    
    /**
     * Check if a player has any profiles.
     *
     * @param playerUUID The player's UUID
     * @return true if player has at least one profile
     */
    public boolean hasProfiles(@Nonnull UUID playerUUID) {
        File playerDir = getPlayerDirectory(playerUUID);
        if (!playerDir.exists()) {
            return false;
        }
        
        File[] files = playerDir.listFiles((dir, name) -> name.endsWith(".json"));
        return files != null && files.length > 0;
    }
    
    /**
     * Get the number of profiles a player has.
     *
     * @param playerUUID The player's UUID
     * @return The number of profiles
     */
    public int getProfileCount(@Nonnull UUID playerUUID) {
        File playerDir = getPlayerDirectory(playerUUID);
        if (!playerDir.exists()) {
            return 0;
        }
        
        File[] files = playerDir.listFiles((dir, name) -> name.endsWith(".json"));
        return files != null ? files.length : 0;
    }
    
    /**
     * Get the directory for a player's profiles.
     */
    private File getPlayerDirectory(@Nonnull UUID playerUUID) {
        return new File(profilesDir, playerUUID.toString());
    }
}
