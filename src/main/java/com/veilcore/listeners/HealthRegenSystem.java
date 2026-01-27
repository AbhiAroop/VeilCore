package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;

/**
 * ECS System that handles passive health regeneration based on the player's healthRegen stat.
 * Runs every tick and accumulates regeneration over time.
 */
public class HealthRegenSystem extends ArchetypeTickingSystem<EntityStore> {

    private static final Query<EntityStore> QUERY = Query.and(
        Player.getComponentType(),
        PlayerRef.getComponentType(),
        EntityStatMap.getComponentType()
    );

    private final VeilCorePlugin plugin;
    private float accumulatedTime = 0.0f;
    private static final float REGEN_INTERVAL = 1.0f; // Regenerate every 1 second

    public HealthRegenSystem(VeilCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @Nonnull
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(float dt, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Accumulate time
        accumulatedTime += dt;
        
        // Only process regeneration once per second
        if (accumulatedTime < REGEN_INTERVAL) {
            return;
        }
        
        // Reset accumulated time (keep remainder for precision)
        accumulatedTime -= REGEN_INTERVAL;
        
        // Process all players in this chunk
        for (int index = 0; index < chunk.size(); index++) {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            processPlayerRegen(ref, store);
        }
    }

    private void processPlayerRegen(Ref<EntityStore> playerRef, Store<EntityStore> store) {
        // Get player component
        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) {
            return;
        }

        // Get player's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        if (profile == null) {
            return;
        }

        // Get health regen stat
        double healthRegenStat = profile.getStats().getHealthRegen();
        if (healthRegenStat <= 0) {
            return; // No regeneration if stat is 0 or negative
        }

        // Get entity stat map for health modification
        EntityStatMap statMap = store.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }

        int healthIndex = DefaultEntityStatTypes.getHealth();
        EntityStatValue healthStat = statMap.get(healthIndex);
        if (healthStat == null) {
            return;
        }

        float currentHealth = healthStat.get();
        float maxHealth = healthStat.getMax();

        // Don't heal if already at max health
        if (currentHealth >= maxHealth) {
            return;
        }

        // Calculate heal amount (healthRegen per second)
        float healAmount = (float) healthRegenStat;
        
        // Apply healing, capped at max health
        float newHealth = Math.min(currentHealth + healAmount, maxHealth);
        statMap.setStatValue(healthIndex, newHealth);
    }
}
