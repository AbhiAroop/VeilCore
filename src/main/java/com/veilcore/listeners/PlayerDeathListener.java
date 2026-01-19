package com.veilcore.listeners;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Tracks player deaths using the OnDeathSystem.
 * Increments death counter in profile stats when a player dies.
 */
public class PlayerDeathListener extends DeathSystems.OnDeathSystem {
    
    private final VeilCorePlugin plugin;
    
    public PlayerDeathListener(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Only track deaths for entities that have the Player component
        return Query.and(Player.getComponentType());
    }
    
    @Override
    public void onComponentAdded(
            @Nonnull Ref ref,
            @Nonnull DeathComponent component,
            @Nonnull Store store,
            @Nonnull CommandBuffer commandBuffer
    ) {
        // Debug log to see if this is even being called
        plugin.getLogger().at(Level.INFO).log("PlayerDeathListener.onComponentAdded called!");
        
        // Get the Player component from the entity that died
        Player playerComponent = (Player) store.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            plugin.getLogger().at(Level.WARNING).log("Player component is null in death listener");
            return;
        }
        
        plugin.getLogger().at(Level.INFO).log("Player died: " + playerComponent.getDisplayName());
        
        // Get the player's active profile
        Profile profile = plugin.getProfileManager().getActiveProfile(playerComponent.getUuid());
        if (profile == null) {
            plugin.getLogger().at(Level.WARNING).log("No active profile for " + playerComponent.getDisplayName());
            return;
        }
        
        // Increment death counter
        profile.getStats().incrementDeaths();
        
        // Save profile asynchronously
        plugin.getScheduler().execute(() -> {
            try {
                plugin.getProfileManager().saveProfile(profile);
                plugin.getLogger().at(Level.INFO).log(
                    "Player " + playerComponent.getDisplayName() + " died. Total deaths: " + 
                    profile.getStats().getDeaths()
                );
            } catch (Exception e) {
                plugin.getLogger().at(Level.WARNING).log(
                    "Failed to save profile after death for " + playerComponent.getDisplayName() + 
                    ": " + e.getMessage()
                );
            }
        });
        
        // Optional: Get death damage information
        Damage deathInfo = component.getDeathInfo();
        if (deathInfo != null) {
            plugin.getLogger().at(Level.INFO).log(
                playerComponent.getDisplayName() + " died from " + deathInfo.getAmount() + " damage"
            );
        }
    }
}
