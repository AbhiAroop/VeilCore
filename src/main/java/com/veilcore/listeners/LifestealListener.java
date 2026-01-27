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
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Heals the attacker for a percentage of physical damage dealt based on lifesteal stat.
 */
public class LifestealListener extends DamageEventSystem {
    private final VeilCorePlugin plugin;

    public LifestealListener(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        // Run in the inspect damage group to react after damage is applied
        return DamageModule.get().getInspectDamageGroup();
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
        Damage.Source source = damage.getSource();
        DamageCause damageCause = damage.getCause();

        Player attackerPlayer = null;
        Ref<EntityStore> attackerRef = null;
        boolean isMeleeAttack = false;

        // Check if it's projectile damage by examining the damage cause
        boolean isProjectileCause = damageCause != null && 
            ("projectile".equalsIgnoreCase(damageCause.getId()) || 
             damageCause.getId().contains("arrow") || 
             damageCause.getId().contains("bolt"));

        // Handle projectile attacks - we don't apply lifesteal to ranged attacks
        if (source instanceof Damage.ProjectileSource) {
            return; // No lifesteal on ranged attacks
        }
        // Handle EntitySource - could be melee OR arrow entity
        else if (source instanceof Damage.EntitySource) {
            Damage.EntitySource entitySource = (Damage.EntitySource) source;
            attackerRef = entitySource.getRef();

            if (!attackerRef.isValid()) {
                return;
            }

            // If it's projectile damage cause, it's a ranged attack (arrow entity)
            if (isProjectileCause) {
                return; // No lifesteal on ranged attacks
            } else {
                // Regular melee attack - apply lifesteal
                attackerPlayer = (Player) store.getComponent(attackerRef, Player.getComponentType());
                if (attackerPlayer != null) {
                    isMeleeAttack = true;
                }
            }
        }

        // Only apply lifesteal on melee attacks from players
        if (!isMeleeAttack || attackerPlayer == null || attackerRef == null) {
            return;
        }

        // Get the player's profile and lifesteal stat
        Profile profile = plugin.getProfileManager().getActiveProfile(attackerPlayer.getUuid());
        if (profile == null) {
            return;
        }

        double lifestealPercent = profile.getStats().getLifeSteal();
        if (lifestealPercent <= 0) {
            return; // No lifesteal to apply
        }

        // Calculate heal amount (percentage of damage dealt)
        float damageDealt = damage.getAmount();
        float healAmount = (float) (damageDealt * (lifestealPercent / 100.0));

        if (healAmount <= 0) {
            return;
        }

        // Heal the attacker by adding to their current health
        EntityStatMap attackerStatMap = store.getComponent(attackerRef, EntityStatMap.getComponentType());
        if (attackerStatMap == null) {
            return;
        }

        int healthIndex = DefaultEntityStatTypes.getHealth();
        EntityStatValue healthStat = attackerStatMap.get(healthIndex);
        if (healthStat == null) {
            return;
        }

        float currentHealth = healthStat.get();
        float maxHealth = healthStat.getMax();
        
        // Add heal amount, capped at max health
        float newHealth = Math.min(currentHealth + healAmount, maxHealth);
        attackerStatMap.setStatValue(healthIndex, newHealth);
    }
}
