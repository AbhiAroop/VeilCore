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
 * Syncs the player's ProfileStats stamina value to their maximum stamina in the game.
 * This is not an ECS system, but uses regular event registration.
 */
public class StaminaSyncListener {

    private static final String STAMINA_MODIFIER_ID = "veilcore_stamina_stat";
    private final VeilCorePlugin plugin;

    public StaminaSyncListener(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player is ready to sync their stamina.
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

        // Apply stamina stat to max stamina
        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();
        updatePlayerMaxStamina(store, player.getReference(), profile.getStats().getStamina());
    }

    /**
     * Updates the player's maximum stamina based on their stamina stat.
     * Call this whenever the player's stamina stat changes.
     * 
     * @param store The entity store
     * @param playerRef The player entity reference
     * @param staminaStat The stamina stat value from ProfileStats
     */
    public static void updatePlayerMaxStamina(Store<EntityStore> store, Ref<EntityStore> playerRef, double staminaStat) {
        EntityStatMap statMap = store.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }

        int staminaIndex = DefaultEntityStatTypes.getStamina();
        
        // Remove old modifier if it exists (use Predictable.ALL to force sync)
        statMap.removeModifier(EntityStatMap.Predictable.ALL, staminaIndex, STAMINA_MODIFIER_ID);
        
        // Calculate max stamina: base game stamina + stat bonus
        // The stat represents total max stamina, so we need to add (stat - baseStamina) where baseStamina is typically 100
        float baseStamina = 100.0f;
        float staminaBonus = (float) staminaStat - baseStamina;
        
        if (staminaBonus != 0) {
            // Create a new modifier for max stamina
            StaticModifier maxStaminaModifier = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                staminaBonus
            );
            
            // Apply the modifier (use Predictable.ALL to force sync)
            statMap.putModifier(EntityStatMap.Predictable.ALL, staminaIndex, STAMINA_MODIFIER_ID, maxStaminaModifier);
        }
        
        // Restore player's stamina to full after changing max stamina (use Predictable.ALL to force sync)
        statMap.resetStatValue(EntityStatMap.Predictable.ALL, staminaIndex);
    }

    /**
     * Helper method to update a player's max stamina by UUID.
     * Useful for commands and other systems that modify stamina stat.
     * 
     * @param plugin The VeilCore plugin instance
     * @param playerUuid The player's UUID
     * @param newStaminaStat The new stamina stat value
     */
    public static void updatePlayerMaxStaminaByUuid(VeilCorePlugin plugin, java.util.UUID playerUuid, double newStaminaStat) {
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
        updatePlayerMaxStamina(store, playerEntity.getReference(), newStaminaStat);
        
        plugin.getLogger().at(Level.INFO).log("Updated max stamina for player " + playerUuid + " to " + newStaminaStat);
    }
}
