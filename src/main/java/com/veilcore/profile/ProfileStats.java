package com.veilcore.profile;

/**
 * Represents player statistics for a profile.
 * Tracks various gameplay metrics.
 */
public class ProfileStats {
    
    private double health;
    private double maxHealth;
    private double mana;
    private double maxMana;
    private int kills;
    private int deaths;
    private long playTime;  // in seconds
    
    public ProfileStats() {
        this.health = 100.0;
        this.maxHealth = 100.0;
        this.mana = 100.0;
        this.maxMana = 100.0;
        this.kills = 0;
        this.deaths = 0;
        this.playTime = 0;
    }
    
    public ProfileStats(double health, double maxHealth, double mana, double maxMana,
                       int kills, int deaths, long playTime) {
        this.health = health;
        this.maxHealth = maxHealth;
        this.mana = mana;
        this.maxMana = maxMana;
        this.kills = kills;
        this.deaths = deaths;
        this.playTime = playTime;
    }
    
    public double getHealth() {
        return health;
    }
    
    public void setHealth(double health) {
        this.health = health;
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }
    
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }
    
    public double getMana() {
        return mana;
    }
    
    public void setMana(double mana) {
        this.mana = mana;
    }
    
    public double getMaxMana() {
        return maxMana;
    }
    
    public void setMaxMana(double maxMana) {
        this.maxMana = maxMana;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public long getPlayTime() {
        return playTime;
    }
    
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
    
    public void incrementPlayTime(long seconds) {
        this.playTime += seconds;
    }
}
