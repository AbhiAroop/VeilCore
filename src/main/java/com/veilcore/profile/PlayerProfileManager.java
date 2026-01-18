package com.veilcore.profile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Manages player profiles - creation, loading, deletion, and tracking active profiles.
 */
public class PlayerProfileManager {
    
    private final ProfileRepository repository;
    private final Logger logger;
    private final File dataFolder;
    private final Gson gson;
    
    // Track active profile per online player (in-memory only)
    private final Map<UUID, UUID> activeProfiles; // playerUUID -> profileUUID
    
    // Track last active profile per player (persisted to disk)
    private final Map<String, String> lastActiveProfiles; // playerUUID.toString() -> profileUUID.toString()
    private final File lastActiveFile;
    
    public static final int MAX_PROFILES_PER_PLAYER = 3;
    
    public PlayerProfileManager(@Nonnull ProfileRepository repository, @Nonnull Logger logger, @Nonnull File dataFolder) {
        this.repository = repository;
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.activeProfiles = new ConcurrentHashMap<>();
        this.lastActiveProfiles = new HashMap<>();
        this.lastActiveFile = new File(dataFolder, "last_active_profiles.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Load persisted last active profiles
        loadLastActiveProfiles();
    }
    
    /**
     * Create a new profile for a player.
     *
     * @param playerUUID The player's UUID
     * @param profileName The name for the new profile
     * @return The created profile, or null if max profiles reached or name invalid
     */
    @Nullable
    public Profile createProfile(@Nonnull UUID playerUUID, @Nonnull String profileName) {
        // Validate profile name
        if (profileName.trim().isEmpty() || profileName.length() > 20) {
            return null;
        }
        
        // Check max profiles
        if (repository.getProfileCount(playerUUID) >= MAX_PROFILES_PER_PLAYER) {
            logger.warning("Player " + playerUUID + " already has max profiles (" + MAX_PROFILES_PER_PLAYER + ")");
            return null;
        }
        
        // Check for duplicate name
        List<Profile> existingProfiles = repository.loadAllProfiles(playerUUID);
        for (Profile profile : existingProfiles) {
            if (profile.getProfileName().equalsIgnoreCase(profileName)) {
                logger.warning("Profile name already exists: " + profileName);
                return null;
            }
        }
        
        // Create and save new profile
        Profile newProfile = new Profile(playerUUID, profileName);
        if (repository.saveProfile(newProfile)) {
            return newProfile;
        }
        
        return null;
    }
    
    /**
     * Get all profiles for a player.
     *
     * @param playerUUID The player's UUID
     * @return List of profiles (empty if none exist)
     */
    @Nonnull
    public List<Profile> getProfiles(@Nonnull UUID playerUUID) {
        return repository.loadAllProfiles(playerUUID);
    }
    
    /**
     * Load a specific profile.
     *
     * @param playerUUID The player's UUID
     * @param profileId The profile's UUID
     * @return The profile, or null if not found
     */
    @Nullable
    public Profile getProfile(@Nonnull UUID playerUUID, @Nonnull UUID profileId) {
        return repository.loadProfile(playerUUID, profileId);
    }
    
    /**
     * Delete a profile.
     *
     * @param playerUUID The player's UUID
     * @param profileId The profile's UUID to delete
     * @return true if deleted successfully
     */
    public boolean deleteProfile(@Nonnull UUID playerUUID, @Nonnull UUID profileId) {
        // Allow deleting any profile, including active one
        // The caller is responsible for handling active profile deletion (switching to another profile)
        boolean deleted = repository.deleteProfile(playerUUID, profileId);
        
        // If the deleted profile was active, clear it from active profiles
        UUID activeProfileId = activeProfiles.get(playerUUID);
        if (deleted && profileId.equals(activeProfileId)) {
            activeProfiles.remove(playerUUID);
            logger.info("Cleared active profile after deletion: " + profileId);
        }
        
        return deleted;
    }
    
    /**
     * Set the active profile for a player and persist it.
     *
     * @param playerUUID The player's UUID
     * @param profileId The profile's UUID to activate
     */
    public void setActiveProfile(@Nonnull UUID playerUUID, @Nonnull UUID profileId) {
        activeProfiles.put(playerUUID, profileId);
        // Persist last active profile
        lastActiveProfiles.put(playerUUID.toString(), profileId.toString());
        saveLastActiveProfiles();
    }
    
    /**
     * Get the active profile ID for a player.
     *
     * @param playerUUID The player's UUID
     * @return The active profile's UUID, or null if none active
     */
    @Nullable
    public UUID getActiveProfileId(@Nonnull UUID playerUUID) {
        return activeProfiles.get(playerUUID);
    }
    
    /**
     * Get the active profile for a player.
     *
     * @param playerUUID The player's UUID
     * @return The active profile, or null if none active
     */
    @Nullable
    public Profile getActiveProfile(@Nonnull UUID playerUUID) {
        UUID profileId = activeProfiles.get(playerUUID);
        if (profileId == null) {
            return null;
        }
        return repository.loadProfile(playerUUID, profileId);
    }
    
    /**
     * Clear the active profile for a player (on disconnect).
     *
     * @param playerUUID The player's UUID
     */
    public void clearActiveProfile(@Nonnull UUID playerUUID) {
        activeProfiles.remove(playerUUID);
    }
    
    /**
     * Get all players with active profiles.
     *
     * @return Set of player UUIDs with active profiles
     */
    @Nonnull
    public java.util.Set<UUID> getActivePlayers() {
        return activeProfiles.keySet();
    }
    
    /**
     * Save a profile.
     *
     * @param profile The profile to save
     * @return true if saved successfully
     */
    public boolean saveProfile(@Nonnull Profile profile) {
        return repository.saveProfile(profile);
    }
    
    /**
     * Check if a player has any profiles.
     *
     * @param playerUUID The player's UUID
     * @return true if player has at least one profile
     */
    public boolean hasProfiles(@Nonnull UUID playerUUID) {
        return repository.hasProfiles(playerUUID);
    }
    
    /**
     * Check if a player can create more profiles.
     *
     * @param playerUUID The player's UUID
     * @return true if player can create another profile
     */
    
    /**
     * Get the last active profile ID for a player (persisted).
     *
     * @param playerUUID The player's UUID
     * @return The last active profile UUID, or null if none
     */
    @Nullable
    public UUID getLastActiveProfileId(@Nonnull UUID playerUUID) {
        String profileIdStr = lastActiveProfiles.get(playerUUID.toString());
        if (profileIdStr == null) {
            return null;
        }
        try {
            return UUID.fromString(profileIdStr);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid UUID in last active profiles: " + profileIdStr);
            return null;
        }
    }
    
    /**
     * Load the last active profiles from disk.
     */
    private void loadLastActiveProfiles() {
        if (!lastActiveFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(lastActiveFile)) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                lastActiveProfiles.putAll(loaded);
                logger.info("Loaded last active profiles for " + loaded.size() + " players");
            }
        } catch (Exception e) {
            logger.warning("Failed to load last active profiles: " + e.getMessage());
        }
    }
    
    /**
     * Save the last active profiles to disk.
     */
    private void saveLastActiveProfiles() {
        try (FileWriter writer = new FileWriter(lastActiveFile)) {
            gson.toJson(lastActiveProfiles, writer);
        } catch (Exception e) {
            logger.warning("Failed to save last active profiles: " + e.getMessage());
        }
    }
    public boolean canCreateProfile(@Nonnull UUID playerUUID) {
        return repository.getProfileCount(playerUUID) < MAX_PROFILES_PER_PLAYER;
    }
}
