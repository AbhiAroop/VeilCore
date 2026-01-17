package com.veilcore.profile;

import javax.annotation.Nonnull;

/**
 * Represents a player's location in the world for a specific profile.
 */
public class ProfileLocation {
    
    private String worldId;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    
    /**
     * Default constructor - spawn location.
     */
    public ProfileLocation() {
        this.worldId = "default";
        this.x = 0.0;
        this.y = 100.0;
        this.z = 0.0;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
    }
    
    /**
     * Full constructor.
     */
    public ProfileLocation(@Nonnull String worldId, double x, double y, double z, float yaw, float pitch) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    @Nonnull
    public String getWorldId() {
        return worldId;
    }
    
    public void setWorldId(@Nonnull String worldId) {
        this.worldId = worldId;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getZ() {
        return z;
    }
    
    public void setZ(double z) {
        this.z = z;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
