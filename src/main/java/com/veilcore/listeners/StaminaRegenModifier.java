package com.veilcore.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;

/**
 * Syncs the player's ProfileStats staminaRegen value to override the default stamina regen rate.
 * This allows custom stamina regeneration rates instead of using Hytale's built-in default.
 * This is not an ECS system, but uses regular event registration.
 */
public class StaminaRegenModifier {

    private final VeilCorePlugin plugin;

    public StaminaRegenModifier(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player is ready to sync their stamina regen.
     * Currently a no-op since we handle all stamina regen through StaminaRegenSystem.
     * This listener is kept for future use if Hytale's stamina regen stat needs to be overridden.
     */
    public void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        // All stamina regen is handled by StaminaRegenSystem
        // No additional override needed on player ready
    }

    /**
     * Helper method to update a player's stamina regen override by UUID.
     * Useful for commands and other systems that modify staminaRegen stat.
     * Currently a no-op since we handle all stamina regen through StaminaRegenSystem.
     * 
     * @param plugin The VeilCore plugin instance
     * @param playerUuid The player's UUID
     * @param newStaminaRegenStat The new stamina regen stat value
     */
    public static void updatePlayerStaminaRegenByUuid(VeilCorePlugin plugin, java.util.UUID playerUuid, double newStaminaRegenStat) {
        // All stamina regen is handled by StaminaRegenSystem
        // No additional override needed
    }
}
