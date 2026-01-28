package com.veilcore.listeners;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.awt.Color;

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
                
                // Calculate health percentage for coloring
                float healthPercent = (currentHealth / maxHealth) * 100.0f;
                Color healthColor = getHealthColor(healthPercent);
                
                // Create colored health text
                String healthValue = String.format("%.0f/%.0f", currentHealth, maxHealth);
                healthText = "\n" + Message.raw("Health: ").getAnsiMessage() + 
                            Message.raw(healthValue).color(healthColor).getAnsiMessage();
            }
        }
        
        // Color entity name based on health percentage
        String coloredName = roleName;
        if (statMap != null) {
            int healthIndex = DefaultEntityStatTypes.getHealth();
            EntityStatValue healthStat = statMap.get(healthIndex);
            if (healthStat != null) {
                float currentHealth = healthStat.get();
                float maxHealth = healthStat.getMax();
                float healthPercent = (currentHealth / maxHealth) * 100.0f;
                Color nameColor = getHealthColor(healthPercent);
                coloredName = Message.raw(roleName).color(nameColor).getAnsiMessage();
            }
        }
        
        // Add nameplate component with the colored name and health
        String nameplateText = coloredName + healthText;
        holder.addComponent(nameplateType, new Nameplate(nameplateText));
    }
    
    /**
     * Get color based on health percentage
     * Green: 75-100%, Yellow: 40-74%, Red: 0-39%
     */
    private Color getHealthColor(float healthPercent) {
        if (healthPercent >= 75.0f) {
            return new Color(0, 255, 0); // Green
        } else if (healthPercent >= 40.0f) {
            return new Color(255, 255, 0); // Yellow
        } else {
            return new Color(255, 0, 0); // Red
        }
    }
    
    @Override
    public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
        // Nothing to do on removal
    }
}
