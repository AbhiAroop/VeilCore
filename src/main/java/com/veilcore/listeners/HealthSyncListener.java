package com.veilcore.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Syncs the player's ProfileStats health value to their maximum health in the game.
 * This is not an ECS system, but uses regular event registration.
 */
public class HealthSyncListener {

    private static final String HEALTH_MODIFIER_ID = "veilcore_health_stat";
    private final VeilCorePlugin plugin;

    public HealthSyncListener(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player is ready to sync their health.
     */
    public void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        Player player = event.getPlayer();
        
        if (player == null || player.getWorld() == null || player.getReference() == null) {
            return;
        }

        // Get player's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        if (profile == null) {
            return;
        }

        // Apply health stat to max health
        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();
        updatePlayerMaxHealth(store, player.getReference(), profile.getStats().getHealth());
    }

    /**
     * Updates the player's maximum health based on their health stat.
     * Call this whenever the player's health stat changes.
     * 
     * @param store The entity store
     * @param playerRef The player entity reference
     * @param healthStat The health stat value from ProfileStats
     */
    public static void updatePlayerMaxHealth(Store<EntityStore> store, Ref<EntityStore> playerRef, double healthStat) {
        EntityStatMap statMap = store.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }

        int healthIndex = DefaultEntityStatTypes.getHealth();
        
        // Remove old modifier if it exists (use Predictable.ALL to force sync)
        statMap.removeModifier(EntityStatMap.Predictable.ALL, healthIndex, HEALTH_MODIFIER_ID);
        
        // Calculate max health: base game health + stat bonus
        // The stat represents total max health, so we need to add (stat - baseHealth) where baseHealth is typically 100
        float baseHealth = 100.0f;
        float healthBonus = (float) healthStat - baseHealth;
        
        if (healthBonus != 0) {
            // Create a new modifier for max health
            StaticModifier maxHealthModifier = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                healthBonus
            );
            
            // Apply the modifier (use Predictable.ALL to force sync)
            statMap.putModifier(EntityStatMap.Predictable.ALL, healthIndex, HEALTH_MODIFIER_ID, maxHealthModifier);
        }
        
        // Heal player to full health after changing max health (use Predictable.ALL to force sync)
        statMap.resetStatValue(EntityStatMap.Predictable.ALL, healthIndex);
    }

    /**
     * Helper method to update a player's max health by UUID.
     * Useful for commands and other systems that modify health stat.
     * 
     * @param plugin The VeilCore plugin instance
     * @param playerUuid The player's UUID
     * @param newHealthStat The new health stat value
     */
    public static void updatePlayerMaxHealthByUuid(VeilCorePlugin plugin, java.util.UUID playerUuid, double newHealthStat) {
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef = 
            com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(playerUuid);
        
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }

        // Get Player component from PlayerRef
        com.hypixel.hytale.server.core.entity.entities.Player playerEntity = 
            playerRef.getComponent(com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        
        if (playerEntity == null || playerEntity.getWorld() == null || playerEntity.getReference() == null) {
            return;
        }

        Store<EntityStore> store = playerEntity.getWorld().getEntityStore().getStore();
        updatePlayerMaxHealth(store, playerEntity.getReference(), newHealthStat);
        
        plugin.getLogger().at(Level.INFO).log("Updated max health for player " + playerUuid + " to " + newHealthStat);
    }
}
