package com.veilcore.listeners;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;

/**
 * System that automatically adds nameplates to NPCs spawned via /npc spawn command
 */
public class NPCNameplateSystem extends HolderSystem<EntityStore> {
    
    private final ComponentType<EntityStore, NPCEntity> npcEntityType = NPCEntity.getComponentType();
    private final ComponentType<EntityStore, Nameplate> nameplateType = Nameplate.getComponentType();
    private final Query<EntityStore> query = Query.and(npcEntityType);
    
    @Override
    @Nonnull
    public Query<EntityStore> getQuery() {
        return query;
    }
    
    @Override
    public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
        // Check if this is an NPC entity
        NPCEntity npcEntity = holder.getComponent(npcEntityType);
        if (npcEntity == null) {
            return;
        }
        
        // Check if it already has a nameplate
        Archetype<EntityStore> archetype = holder.getArchetype();
        if (archetype.contains(nameplateType)) {
            return; // Already has a nameplate
        }
        
        // Get the NPC's role name for the nameplate
        String roleName = npcEntity.getRoleName();
        if (roleName == null || roleName.isEmpty()) {
            roleName = "NPC";
        }
        
        // Get health information
        EntityStatMap statMap = holder.getComponent(EntityStatMap.getComponentType());
        String healthText = "";
        if (statMap != null) {
            int healthIndex = DefaultEntityStatTypes.getHealth();
            EntityStatValue healthStat = statMap.get(healthIndex);
            if (healthStat != null) {
                float currentHealth = healthStat.get();
                float maxHealth = healthStat.getMax();
                healthText = String.format("\nHealth: %.0f/%.0f", currentHealth, maxHealth);
            }
        }
        
        // Add nameplate component with the role name and health
        String nameplateText = roleName + healthText;
        holder.addComponent(nameplateType, new Nameplate(nameplateText));
    }
    
    @Override
    public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
        // Nothing to do on removal
    }
}
