package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Level;

/**
 * Adds physical damage bonus from player stats to all melee damage dealt.
 * This applies to both fist attacks and weapon attacks.
 */
public class PhysicalDamageListener extends DamageEventSystem {
    
    private final VeilCorePlugin plugin;
    
    public PhysicalDamageListener(VeilCorePlugin plugin) {
        this.plugin = plugin;
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
        
        Player attackerPlayer = null;
        
        // Handle direct player attacks (melee)
        if (source instanceof Damage.EntitySource) {
            Damage.EntitySource entitySource = (Damage.EntitySource) source;
            Ref<EntityStore> sourceRef = entitySource.getRef();
            
            if (!sourceRef.isValid()) {
                return;
            }
            
            // Get the Player component from the attacker
            attackerPlayer = (Player) store.getComponent(sourceRef, Player.getComponentType());
        }
        // Handle projectile attacks (optional - can add rangedDamage here too)
        else if (source instanceof Damage.ProjectileSource) {
            Damage.ProjectileSource projectileSource = (Damage.ProjectileSource) source;
            Ref<EntityStore> shooterRef = projectileSource.getRef();
            
            if (!shooterRef.isValid()) {
                return;
            }
            
            // Get the Player component from the shooter
            attackerPlayer = (Player) store.getComponent(shooterRef, Player.getComponentType());
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
        
        // Get the physical damage stat
        int physicalDamage = profile.getStats().getPhysicalDamage();
        
        // Add physical damage to the base damage, minus 1 to account for Hytale's base damage
        float currentDamage = damage.getAmount();
        float newDamage = currentDamage + (physicalDamage - 1);
        
        damage.setAmount(newDamage);
        
        // Debug logging (can be removed in production)
        plugin.getLogger().at(Level.FINE).log(
            "Physical damage applied: " + attackerPlayer.getDisplayName() + 
            " dealt " + currentDamage + " -> " + newDamage + " damage (+" + (physicalDamage - 1) + ")"
        );
    }
}
