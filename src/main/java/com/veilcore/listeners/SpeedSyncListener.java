package com.veilcore.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;

/**
 * Syncs the player's ProfileStats speed value to their movement speed in the game.
 * This is not an ECS system, but uses regular event registration.
 */
public class SpeedSyncListener {

    private final VeilCorePlugin plugin;

    public SpeedSyncListener(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player is ready to sync their speed.
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

        // Apply speed stat to movement
        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();
        updatePlayerSpeed(store, player.getReference(), profile.getStats().getSpeed());
    }

    /**
     * Updates the player's movement speed based on their speed stat.
     * Call this whenever the player's speed stat changes.
     * 
     * @param store The entity store
     * @param playerRef The player entity reference
     * @param speedStat The speed stat value from ProfileStats (e.g., 100 = default, 200 = 2x speed)
     */
    public static void updatePlayerSpeed(Store<EntityStore> store, Ref<EntityStore> playerRef, double speedStat) {
        MovementManager movementManager = store.getComponent(playerRef, MovementManager.getComponentType());
        if (movementManager == null) {
            return;
        }

        // Get the current settings
        MovementSettings settings = movementManager.getSettings();
        if (settings == null) {
            return;
        }

        // Calculate the speed multiplier
        // speedStat is a multiplier where 100 is default (100% speed)
        // If speedStat is 200, that means 200% speed (2x faster)
        // Base speed in the game is 5.5, so we multiply by (speedStat / 100)
        float baseSpeed = 5.5f;
        float speedMultiplier = (float) (speedStat / 100.0);
        settings.baseSpeed = baseSpeed * speedMultiplier;

        // Get packet handler to send update to client
        PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRefComponent != null) {
            movementManager.update(playerRefComponent.getPacketHandler());
        }
    }

    /**
     * Helper method to update a player's speed by UUID.
     * Useful for commands and other systems that modify speed stat.
     * 
     * @param plugin The VeilCore plugin instance
     * @param playerUuid The player's UUID
     * @param newSpeedStat The new speed stat value
     */
    public static void updatePlayerSpeedByUuid(VeilCorePlugin plugin, java.util.UUID playerUuid, double newSpeedStat) {
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef = 
            com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(playerUuid);
        
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }

        // Get Player component from PlayerRef
        Player playerEntity = playerRef.getComponent(Player.getComponentType());
        
        if (playerEntity == null || playerEntity.getWorld() == null || playerEntity.getReference() == null) {
            return;
        }

        Store<EntityStore> store = playerEntity.getWorld().getEntityStore().getStore();
        updatePlayerSpeed(store, playerEntity.getReference(), newSpeedStat);
    }
}
