package com.veilcore.profile;

/**
 * Represents player statistics for a profile.
 * Tracks various gameplay metrics including combat, fortune, fishing, and resource stats.
 */
public class ProfileStats {
    
    // Combat Stats
    private int health;
    private int armor;
    private int magicResist;
    private int physicalDamage;
    private int magicDamage;
    private int mana;
    private int totalMana;
    private int stamina;
    private double speed;
    private double criticalDamage;
    private double criticalChance;
    private double burstDamage;
    private double burstChance;
    private int cooldownReduction;
    private double lifeSteal;
    private int rangedDamage;
    private double attackSpeed;
    private double omnivamp;
    private double healthRegen;
    private double staminaRegen;
    
    // Fortune Stats
    private double miningFortune;
    private double farmingFortune;
    private double lootingFortune;
    private double fishingFortune;
    
    // Fishing Stats
    private int lurePotency;
    private double fishingResilience;
    private double fishingFocus;
    private double fishingPrecision;
    private double seaMonsterAffinity;
    private double treasureSense;
    
    // Resource Stats
    private int manaRegen;
    private int luck;
    
    // Minecraft Base Stats
    private double currentHealth;
    private int foodLevel;
    private float saturation;
    private float exhaustion;
    private int expLevel;
    private float expProgress;
    private double attackRange;
    private double buildRange;
    private double size;
    private double miningSpeed;
    
    // Gameplay Tracking
    private int kills;
    private int deaths;
    private long playTime;  // in seconds

    // Default Values - Combat
    private int defaultHealth = 100;
    private int defaultStamina = 100;
    private int defaultArmor = 0;
    private int defaultMR = 0;
    private int defaultPhysicalDamage = 5;
    private int defaultMagicDamage = 5;
    private int defaultMana = 100;
    private double defaultSpeed = 0.1;
    private double defaultCritDmg = 1.5;
    private double defaultCritChance = 0.00;
    private double defaultBurstDmg = 2.0;
    private double defaultBurstChance = 0.01;
    private double defaultCDR = 0;
    private double defaultLifeSteal = 0;
    private int defaultRangedDamage = 5;
    private double defaultAttackSpeed = 0.5;
    private double defaultOmnivamp = 0;
    private double defaultHealthRegen = 0.3;
    private double defaultStaminaRegen = 0.3;
    
    // Default Values - Fortune
    private double defaultMiningFortune = 1.00;
    private double defaultFarmingFortune = 0.0;
    private double defaultLootingFortune = 1.00;
    private double defaultFishingFortune = 0.0;
    
    // Default Values - Fishing
    private int defaultLurePotency = 0;
    private double defaultFishingResilience = 0.0;
    private double defaultFishingFocus = 0.0;
    private double defaultFishingPrecision = 0.0;
    private double defaultSeaMonsterAffinity = 0.0;
    private double defaultTreasureSense = 0.0;
    
    // Default Values - Resource
    private int defaultManaRegen = 1;
    private int defaultLuck = 0;
    
    // Default Values - Minecraft Stats
    private double defaultCurrentHealth = 100.0;
    private int defaultFoodLevel = 20;
    private float defaultSaturation = 5.0f;
    private float defaultExhaustion = 0.0f;
    private int defaultExpLevel = 0;
    private float defaultExpProgress = 0.0f;
    private double defaultAttackRange = 3.0;
    private double defaultBuildRange = 5.0;
    private double defaultSize = 1.0;
    private double defaultMiningSpeed = 0.5;
    
    public ProfileStats() {
        // Initialize combat stats with defaults
        this.health = defaultHealth;
        this.armor = defaultArmor;
        this.magicResist = defaultMR;
        this.physicalDamage = defaultPhysicalDamage;
        this.magicDamage = defaultMagicDamage;
        this.mana = defaultMana;
        this.totalMana = defaultMana;
        this.stamina = defaultStamina;
        this.speed = defaultSpeed;
        this.criticalDamage = defaultCritDmg;
        this.criticalChance = defaultCritChance;
        this.burstDamage = defaultBurstDmg;
        this.burstChance = defaultBurstChance;
        this.cooldownReduction = (int) defaultCDR;
        this.lifeSteal = defaultLifeSteal;
        this.rangedDamage = defaultRangedDamage;
        this.attackSpeed = defaultAttackSpeed;
        this.omnivamp = defaultOmnivamp;
        this.healthRegen = defaultHealthRegen;
        this.staminaRegen = defaultStaminaRegen;
        
        // Initialize fortune stats with defaults
        this.miningFortune = defaultMiningFortune;
        this.farmingFortune = defaultFarmingFortune;
        this.lootingFortune = defaultLootingFortune;
        this.fishingFortune = defaultFishingFortune;
        
        // Initialize fishing stats with defaults
        this.lurePotency = defaultLurePotency;
        this.fishingResilience = defaultFishingResilience;
        this.fishingFocus = defaultFishingFocus;
        this.fishingPrecision = defaultFishingPrecision;
        this.seaMonsterAffinity = defaultSeaMonsterAffinity;
        this.treasureSense = defaultTreasureSense;
        
        // Initialize resource stats with defaults
        this.manaRegen = defaultManaRegen;
        this.luck = defaultLuck;
        
        // Initialize Minecraft stats with defaults
        this.currentHealth = defaultCurrentHealth;
        this.foodLevel = defaultFoodLevel;
        this.saturation = defaultSaturation;
        this.exhaustion = defaultExhaustion;
        this.expLevel = defaultExpLevel;
        this.expProgress = defaultExpProgress;
        this.attackRange = defaultAttackRange;
        this.buildRange = defaultBuildRange;
        this.size = defaultSize;
        this.miningSpeed = defaultMiningSpeed;
        
        // Initialize gameplay tracking
        this.kills = 0;
        this.deaths = 0;
        this.playTime = 0;
    }
    
    // Combat Stats Getters/Setters
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public int getArmor() {
        return armor;
    }
    
    public void setArmor(int armor) {
        this.armor = armor;
    }
    
    public int getMagicResist() {
        return magicResist;
    }
    
    public void setMagicResist(int magicResist) {
        this.magicResist = magicResist;
    }
    
    public int getPhysicalDamage() {
        return physicalDamage;
    }
    
    public void setPhysicalDamage(int physicalDamage) {
        this.physicalDamage = physicalDamage;
    }
    
    public int getMagicDamage() {
        return magicDamage;
    }
    
    public void setMagicDamage(int magicDamage) {
        this.magicDamage = magicDamage;
    }
    
    public int getMana() {
        return mana;
    }
    
    public void setMana(int mana) {
        this.mana = mana;
    }
    
    public int getTotalMana() {
        return totalMana;
    }
    
    public void setTotalMana(int totalMana) {
        this.totalMana = totalMana;
    }
    
    public int getStamina() {
        return stamina;
    }
    
    public void setStamina(int stamina) {
        this.stamina = stamina;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public double getCriticalDamage() {
        return criticalDamage;
    }
    
    public void setCriticalDamage(double criticalDamage) {
        this.criticalDamage = criticalDamage;
    }
    
    public double getCriticalChance() {
        return criticalChance;
    }
    
    public void setCriticalChance(double criticalChance) {
        this.criticalChance = criticalChance;
    }
    
    public double getBurstDamage() {
        return burstDamage;
    }
    
    public void setBurstDamage(double burstDamage) {
        this.burstDamage = burstDamage;
    }
    
    public double getBurstChance() {
        return burstChance;
    }
    
    public void setBurstChance(double burstChance) {
        this.burstChance = burstChance;
    }
    
    public int getCooldownReduction() {
        return cooldownReduction;
    }
    
    public void setCooldownReduction(int cooldownReduction) {
        this.cooldownReduction = cooldownReduction;
    }
    
    public double getLifeSteal() {
        return lifeSteal;
    }
    
    public void setLifeSteal(double lifeSteal) {
        this.lifeSteal = lifeSteal;
    }
    
    public int getRangedDamage() {
        return rangedDamage;
    }
    
    public void setRangedDamage(int rangedDamage) {
        this.rangedDamage = rangedDamage;
    }
    
    public double getAttackSpeed() {
        return attackSpeed;
    }
    
    public void setAttackSpeed(double attackSpeed) {
        this.attackSpeed = attackSpeed;
    }
    
    public double getOmnivamp() {
        return omnivamp;
    }
    
    public void setOmnivamp(double omnivamp) {
        this.omnivamp = omnivamp;
    }
    
    public double getHealthRegen() {
        return healthRegen;
    }
    
    public void setHealthRegen(double healthRegen) {
        this.healthRegen = healthRegen;
    }
    
    public double getStaminaRegen() {
        return staminaRegen;
    }
    
    public void setStaminaRegen(double staminaRegen) {
        this.staminaRegen = staminaRegen;
    }
    
    // Fortune Stats Getters/Setters
    public double getMiningFortune() {
        return miningFortune;
    }
    
    public void setMiningFortune(double miningFortune) {
        this.miningFortune = miningFortune;
    }
    
    public double getFarmingFortune() {
        return farmingFortune;
    }
    
    public void setFarmingFortune(double farmingFortune) {
        this.farmingFortune = farmingFortune;
    }
    
    public double getLootingFortune() {
        return lootingFortune;
    }
    
    public void setLootingFortune(double lootingFortune) {
        this.lootingFortune = lootingFortune;
    }
    
    public double getFishingFortune() {
        return fishingFortune;
    }
    
    public void setFishingFortune(double fishingFortune) {
        this.fishingFortune = fishingFortune;
    }
    
    // Fishing Stats Getters/Setters
    public int getLurePotency() {
        return lurePotency;
    }
    
    public void setLurePotency(int lurePotency) {
        this.lurePotency = lurePotency;
    }
    
    public double getFishingResilience() {
        return fishingResilience;
    }
    
    public void setFishingResilience(double fishingResilience) {
        this.fishingResilience = fishingResilience;
    }
    
    public double getFishingFocus() {
        return fishingFocus;
    }
    
    public void setFishingFocus(double fishingFocus) {
        this.fishingFocus = fishingFocus;
    }
    
    public double getFishingPrecision() {
        return fishingPrecision;
    }
    
    public void setFishingPrecision(double fishingPrecision) {
        this.fishingPrecision = fishingPrecision;
    }
    
    public double getSeaMonsterAffinity() {
        return seaMonsterAffinity;
    }
    
    public void setSeaMonsterAffinity(double seaMonsterAffinity) {
        this.seaMonsterAffinity = seaMonsterAffinity;
    }
    
    public double getTreasureSense() {
        return treasureSense;
    }
    
    public void setTreasureSense(double treasureSense) {
        this.treasureSense = treasureSense;
    }
    
    // Resource Stats Getters/Setters
    public int getManaRegen() {
        return manaRegen;
    }
    
    public void setManaRegen(int manaRegen) {
        this.manaRegen = manaRegen;
    }
    
    public int getLuck() {
        return luck;
    }
    
    public void setLuck(int luck) {
        this.luck = luck;
    }
    
    // Minecraft Stats Getters/Setters
    public double getCurrentHealth() {
        return currentHealth;
    }
    
    public void setCurrentHealth(double currentHealth) {
        this.currentHealth = currentHealth;
    }
    
    public int getFoodLevel() {
        return foodLevel;
    }
    
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }
    
    public float getSaturation() {
        return saturation;
    }
    
    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }
    
    public float getExhaustion() {
        return exhaustion;
    }
    
    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }
    
    public int getExpLevel() {
        return expLevel;
    }
    
    public void setExpLevel(int expLevel) {
        this.expLevel = expLevel;
    }
    
    public float getExpProgress() {
        return expProgress;
    }
    
    public void setExpProgress(float expProgress) {
        this.expProgress = expProgress;
    }
    
    public double getAttackRange() {
        return attackRange;
    }
    
    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }
    
    public double getBuildRange() {
        return buildRange;
    }
    
    public void setBuildRange(double buildRange) {
        this.buildRange = buildRange;
    }
    
    public double getSize() {
        return size;
    }
    
    public void setSize(double size) {
        this.size = size;
    }
    
    public double getMiningSpeed() {
        return miningSpeed;
    }
    
    public void setMiningSpeed(double miningSpeed) {
        this.miningSpeed = miningSpeed;
    }
    
    // Gameplay Tracking Getters/Setters
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
    
    public void incrementDeaths() {
        this.deaths++;
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
    
    // Default Values Getters
    public int getDefaultHealth() {
        return defaultHealth;
    }
    
    public int getDefaultStamina() {
        return defaultStamina;
    }
    
    public int getDefaultArmor() {
        return defaultArmor;
    }
    
    public int getDefaultMR() {
        return defaultMR;
    }
    
    public int getDefaultPhysicalDamage() {
        return defaultPhysicalDamage;
    }
    
    public int getDefaultMagicDamage() {
        return defaultMagicDamage;
    }
    
    public int getDefaultMana() {
        return defaultMana;
    }
    
    public double getDefaultSpeed() {
        return defaultSpeed;
    }
    
    public double getDefaultCritDmg() {
        return defaultCritDmg;
    }
    
    public double getDefaultCritChance() {
        return defaultCritChance;
    }
    
    public double getDefaultBurstDmg() {
        return defaultBurstDmg;
    }
    
    public double getDefaultBurstChance() {
        return defaultBurstChance;
    }
    
    // Add methods for skill tree node bonuses
    public void addMaxHealth(double amount) {
        this.health += (int) amount;
    }
    
    public void addDamage(double amount) {
        this.physicalDamage += (int) amount;
        this.magicDamage += (int) amount;
    }
    
    public void addDefense(double amount) {
        this.armor += (int) amount;
    }
    
    public void addLuck(double amount) {
        this.luck += (int) amount;
        this.miningFortune += amount / 100.0; // Convert to fortune multiplier
    }
    
    public void addSpeed(double amount) {
        this.speed += amount;
    }
}
