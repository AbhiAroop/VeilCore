package com.veilcore.profile;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Represents a player's game profile.
 * Each profile contains isolated game state including inventory, location, stats, and level.
 */
public class Profile {
    
    private final UUID profileId;
    private final UUID playerUUID;
    private String profileName;
    private final Instant createdAt;
    private Instant lastPlayedAt;
    
    // Game state data
    private ProfileLocation location;
    private int level;
    private long experience;
    private ProfileInventory inventory;
    private ProfileStats stats;
    
    /**
     * Create a new profile.
     *
     * @param playerUUID The UUID of the player who owns this profile
     * @param profileName The name of this profile
     */
    public Profile(@Nonnull UUID playerUUID, @Nonnull String profileName) {
        this.profileId = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.profileName = profileName;
        this.createdAt = Instant.now();
        this.lastPlayedAt = Instant.now();
        
        // Initialize with defaults
        this.location = new ProfileLocation();
        this.level = 1;
        this.experience = 0;
        this.inventory = new ProfileInventory();
        this.stats = new ProfileStats();
    }
    
    /**
     * Constructor for deserialization.
     */
    public Profile(@Nonnull UUID profileId, @Nonnull UUID playerUUID, @Nonnull String profileName,
                   @Nonnull Instant createdAt, @Nonnull Instant lastPlayedAt,
                   @Nonnull ProfileLocation location, int level, long experience,
                   @Nonnull ProfileInventory inventory, @Nonnull ProfileStats stats) {
        this.profileId = profileId;
        this.playerUUID = playerUUID;
        this.profileName = profileName;
        this.createdAt = createdAt;
        this.lastPlayedAt = lastPlayedAt;
        this.location = location;
        this.level = level;
        this.experience = experience;
        this.inventory = inventory;
        this.stats = stats;
    }
    
    // Getters
    @Nonnull
    public UUID getProfileId() {
        return profileId;
    }
    
    @Nonnull
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    @Nonnull
    public String getProfileName() {
        return profileName;
    }
    
    public void setProfileName(@Nonnull String profileName) {
        this.profileName = profileName;
    }
    
    @Nonnull
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    @Nonnull
    public Instant getLastPlayedAt() {
        return lastPlayedAt;
    }
    
    public void updateLastPlayed() {
        this.lastPlayedAt = Instant.now();
    }
    
    @Nonnull
    public ProfileLocation getLocation() {
        return location;
    }
    
    public void setLocation(@Nonnull ProfileLocation location) {
        this.location = location;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public long getExperience() {
        return experience;
    }
    
    public void setExperience(long experience) {
        this.experience = experience;
    }
    
    @Nonnull
    public ProfileInventory getInventory() {
        return inventory;
    }
    
    public void setInventory(@Nonnull ProfileInventory inventory) {
        this.inventory = inventory;
    }
    
    @Nonnull
    public ProfileStats getStats() {
        return stats;
    }
    
    public void setStats(@Nonnull ProfileStats stats) {
        this.stats = stats;
    }
}
