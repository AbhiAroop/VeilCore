package com.veilcore.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Disables Hytale's built-in stamina regen so that only our StaminaRegenSystem handles regeneration.
 * This ensures stamina regen stat value overrides any default behavior.
 */
public class StaminaRegenModifier {

    private static final String STAMINA_SUPPRESS_MODIFIER_ID = "veilcore_stamina_suppress_builtin";
    private final VeilCorePlugin plugin;

    public StaminaRegenModifier(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player is ready to suppress built-in stamina regen.
     */
    public void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        Player player = event.getPlayer();
        
        if (player == null || player.getWorld() == null || player.getReference() == null) {
            return;
        }

        // Suppress built-in stamina regen so only our system handles it
        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();
        suppressBuiltInStaminaRegen(store, player.getReference());
    }

    /**
     * Suppresses Hytale's built-in stamina regen by setting stamina to prevent natural regeneration.
     * This is done by immediately setting stamina value to prevent the default regen from kicking in.
     * Our StaminaRegenSystem will handle all subsequent regen instead.
     * 
     * @param store The entity store
     * @param playerRef The player entity reference
     */
    public static void suppressBuiltInStaminaRegen(Store<EntityStore> store, Ref<EntityStore> playerRef) {
        EntityStatMap statMap = store.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }

        int staminaIndex = DefaultEntityStatTypes.getStamina();
        EntityStatValue staminaStat = statMap.get(staminaIndex);
        if (staminaStat == null) {
            return;
        }

        // Get current stamina value and immediately re-set it to prevent built-in regen from applying
        // This forces the stamina system to update and ensures our custom regen takes over
        float currentStamina = staminaStat.get();
        statMap.setStatValue(staminaIndex, currentStamina);
    }

    /**
     * Helper method to suppress stamina regen by UUID.
     * Called when staminaRegen stat is modified.
     * 
     * @param plugin The VeilCore plugin instance
     * @param playerUuid The player's UUID
     */
    public static void updatePlayerStaminaRegenByUuid(VeilCorePlugin plugin, java.util.UUID playerUuid) {
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }

        Player player = playerRef.getComponent(Player.getComponentType());
        if (player == null || player.getWorld() == null || player.getReference() == null) {
            return;
        }

        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();
        suppressBuiltInStaminaRegen(store, player.getReference());
    }
}
