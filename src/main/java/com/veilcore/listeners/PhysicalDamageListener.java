package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.logging.Level;

/**
 * Adds physical damage bonus from player stats to melee attacks and ranged damage bonus to projectile attacks.
 * Also applies critical hit mechanics to melee attacks based on criticalChance and criticalDamage stats.
 */
public class PhysicalDamageListener extends DamageEventSystem {
    
    private final VeilCorePlugin plugin;
    private final Random random;
    
    public PhysicalDamageListener(VeilCorePlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }
    
    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        // Run in the filter damage group to adjust damage before it's applied
        return DamageModule.get().getFilterDamageGroup();
    }
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // We don't filter by query - we check the damage source instead
        return Query.any();
    }
    
    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull Damage damage
    ) {
        // Check if the damage source is a player (EntitySource or ProjectileSource)
        Damage.Source source = damage.getSource();
        DamageCause damageCause = damage.getCause();
        
        Player attackerPlayer = null;
        boolean isMeleeAttack = false;
        boolean isRangedAttack = false;
        
        // Check if it's projectile damage by examining the damage cause
        boolean isProjectileCause = damageCause != null && 
            ("projectile".equalsIgnoreCase(damageCause.getId()) || 
             damageCause.getId().contains("arrow") || 
             damageCause.getId().contains("bolt"));
        
        // Handle projectile attacks (bows, crossbows)
        if (source instanceof Damage.ProjectileSource) {
            Damage.ProjectileSource projectileSource = (Damage.ProjectileSource) source;
            Ref<EntityStore> shooterRef = projectileSource.getRef();
            
            if (!shooterRef.isValid()) {
                return;
            }
            
            // Get the Player component from the shooter
            attackerPlayer = (Player) store.getComponent(shooterRef, Player.getComponentType());
            if (attackerPlayer != null) {
                isRangedAttack = true;
            }
        }
        // Handle EntitySource - could be melee OR arrow entity
        else if (source instanceof Damage.EntitySource) {
            Damage.EntitySource entitySource = (Damage.EntitySource) source;
            Ref<EntityStore> sourceRef = entitySource.getRef();
            
            if (!sourceRef.isValid()) {
                return;
            }
            
            // If it's projectile damage cause, it's a ranged attack (arrow entity)
            if (isProjectileCause) {
                // The entity is the arrow/projectile itself
                // The Player component on the arrow entity represents the shooter
                attackerPlayer = (Player) store.getComponent(sourceRef, Player.getComponentType());
                
                if (attackerPlayer != null) {
                    isRangedAttack = true;
                } else {
                    return;
                }
            } else {
                // Regular melee attack
                attackerPlayer = (Player) store.getComponent(sourceRef, Player.getComponentType());
                if (attackerPlayer != null) {
                    isMeleeAttack = true;
                }
            }
        }
        
        // If the attacker is not a player, ignore
        if (attackerPlayer == null) {
            return;
        }
        
        // Get the player's active profile
        Profile profile = plugin.getProfileManager().getActiveProfile(attackerPlayer.getUuid());
        if (profile == null) {
            return;
        }
        
        float currentDamage = damage.getAmount();
        float newDamage = currentDamage;
        boolean isCriticalHit = false;
        
        // Apply damage bonuses based on attack type
        if (isMeleeAttack) {
            // Get the physical damage stat for melee
            int physicalDamage = profile.getStats().getPhysicalDamage();
            
            // Add physical damage to the base damage, minus 1 to account for Hytale's base damage
            newDamage = currentDamage + (physicalDamage - 1);
            
            // Calculate critical hit for melee attacks
            double criticalChance = profile.getStats().getCriticalChance();
            
            // Roll for critical hit (criticalChance is a percentage)
            if (random.nextDouble() * 100 < criticalChance) {
                isCriticalHit = true;
                
                // Multiply total damage by criticalDamage multiplier
                double criticalDamage = profile.getStats().getCriticalDamage();
                newDamage *= criticalDamage;
            }
        } else if (isRangedAttack) {
            // Get the ranged damage stat for projectiles
            int rangedDamage = profile.getStats().getRangedDamage();
            
            // Add ranged damage to the base damage, minus 1 to account for Hytale's base damage
            newDamage = currentDamage + (rangedDamage - 1);
        }
        
        damage.setAmount(newDamage);
        
        // Debug logging
        if (isCriticalHit) {
            plugin.getLogger().at(Level.FINE).log(
                "CRITICAL HIT! " + attackerPlayer.getDisplayName() + 
                " dealt " + currentDamage + " -> " + newDamage + " damage (×" + 
                profile.getStats().getCriticalDamage() + " multiplier)"
            );
        } else if (isMeleeAttack) {
            plugin.getLogger().at(Level.FINE).log(
                "Physical damage applied: " + attackerPlayer.getDisplayName() + 
                " dealt " + currentDamage + " -> " + newDamage + " damage (+" + 
                (profile.getStats().getPhysicalDamage() - 1) + " physical)"
            );
        } else if (isRangedAttack) {
            plugin.getLogger().at(Level.FINE).log(
                "Ranged damage applied: " + attackerPlayer.getDisplayName() + 
                " dealt " + currentDamage + " -> " + newDamage + " damage (+" + 
                (profile.getStats().getRangedDamage() - 1) + " ranged)"
            );
        }
    }
}
