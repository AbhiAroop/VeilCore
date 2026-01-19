# VeilCore Skill System - Phase 5 Implementation Plan

## Overview
Phase 5 aims to implement automatic XP gain from gameplay events, making the skill system fully functional without requiring admin commands.

## Current Status: **BLOCKED - Awaiting Hytale API Updates**

The Hytale Server API (v1.0.0-alpha) does not currently provide the necessary event hooks for automatic XP gain:

### Missing API Events:
1. **BlockBreakEvent** - Required for Mining XP
   - Package: `com.hypixel.hytale.server.universe.world.block` (does not exist)
   - Needed to detect when players break blocks
   - Would allow granting Mining XP based on block type

2. **DeathEvent/EntityDeathEvent** - Required for Combat XP  
   - Package: `com.hypixel.hytale.server.core.entity.damage` (DeathEvent not available)
   - Needed to detect when players kill entities
   - Would allow granting Combat XP based on entity type

3. **HarvestEvent** - Required for Farming XP (future)
   - Not yet available in API
   - Would detect crop harvesting for Farming skill

4. **FishCatchEvent** - Required for Fishing XP (future)
   - Not yet available in API  
   - Would detect successful fishing for Fishing skill

## Planned Implementation

### 1. Mining XP Listener
```java
@EventListener
public void onBlockBreak(BlockBreakEvent event, Ref<EntityStore> ref, Store<EntityStore> store) {
    Player player = store.getComponent(ref, Player.getComponentType());
    Profile profile = getProfile(player);
    
    // Calculate XP based on block type/rarity
    long xp = calculateMiningXp(event.getBlock());
    int levelsGained = profile.getSkills().addXp(Skill.MINING, xp);
    
    // Notify on level up
    if (levelsGained > 0) {
        SkillLevelUpNotifier.sendLevelUpNotification(...);
    }
}
```

**XP Scaling Ideas:**
- Common blocks: 5 XP (dirt, stone)
- Uncommon blocks: 10 XP (coal ore, iron ore)
- Rare blocks: 25 XP (gold ore, diamond ore)
- Epic blocks: 50 XP (ancient debris, special ores)

### 2. Combat XP Listener
```java
@EventListener
public void onEntityDeath(DeathEvent event, Ref<EntityStore> victimRef, Store<EntityStore> store) {
    // Check if killer was a player
    if (event.getDamageSource().getAttacker() != null) {
        Player killer = getPlayer(event.getDamageSource().getAttacker());
        Profile profile = getProfile(killer);
        
        // Calculate XP based on entity type/difficulty
        long xp = calculateCombatXp(victim);
        int levelsGained = profile.getSkills().addXp(Skill.COMBAT, xp);
        
        // Notify on level up
        if (levelsGained > 0) {
            SkillLevelUpNotifier.sendLevelUpNotification(...);
        }
    }
}
```

**XP Scaling Ideas:**
- Passive mobs: 10 XP (chicken, pig, sheep)
- Neutral mobs: 15 XP (wolf, spider)
- Hostile mobs: 25 XP (zombie, skeleton)
- Boss mobs: 100-500 XP (mini-bosses, world bosses)

### 3. Farming XP Listener (Future)
```java
@EventListener
public void onHarvest(HarvestEvent event, ...) {
    // Grant Farming XP when crops are harvested
    // Scale based on crop tier/growth time
}
```

### 4. Fishing XP Listener (Future)
```java
@EventListener  
public void onFishCatch(FishCatchEvent event, ...) {
    // Grant Fishing XP when fish are caught
    // Scale based on fish rarity
}
```

## Current Workaround

Until the API events are available, players can gain XP through admin commands:
- `/giveskillxp <skill> <amount>` - Grant XP to yourself
- `/setskilllevel <skill> <level>` - Set skill level directly

## Files Prepared (Not Implemented)
The following files were created but removed due to missing API:
- `MiningXpListener.java` - Mining XP from block breaks
- `CombatXpListener.java` - Combat XP from entity kills

## When API Becomes Available

1. Restore the listener files from git history
2. Update imports to use actual event classes
3. Register listeners in `VeilCorePlugin.setup()`
4. Test XP gain rates and balance
5. Add configuration for XP multipliers
6. Add XP boost items/effects

## Timeline
**Estimated Implementation Time:** 2-4 hours (once API is available)

**Dependencies:**
- Hytale Server API v1.1.0+ (estimated)
- Block and Entity event systems
- Damage source tracking

## Testing Plan
1. Test Mining XP gain from various block types
2. Test Combat XP gain from different mob kills
3. Verify level-up notifications work correctly
4. Balance XP rates for reasonable progression
5. Test edge cases (creative mode, admin kills, etc.)

---

**Last Updated:** January 19, 2026  
**Status:** Waiting on Hytale API updates
**Phase:** 5 - Automatic XP Gain (Planned)
