package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
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
 * System that updates NPC nameplates when their health changes
 */
public class NPCNameplateUpdateSystem extends EntityTickingSystem<EntityStore> {
    
    private final ComponentType<EntityStore, NPCEntity> npcEntityType = NPCEntity.getComponentType();
    private final ComponentType<EntityStore, Nameplate> nameplateType = Nameplate.getComponentType();
    private final ComponentType<EntityStore, EntityStatMap> statMapType = EntityStatMap.getComponentType();
    private final Query<EntityStore> query = Query.and(npcEntityType, nameplateType, statMapType);
    
    private float updateTimer = 0.0f;
    private static final float UPDATE_INTERVAL = 0.5f; // Update every 0.5 seconds
    
    @Override
    @Nonnull
    public Query<EntityStore> getQuery() {
        return query;
    }
    
    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        updateTimer += dt;
        if (updateTimer < UPDATE_INTERVAL) {
            return;
        }
        updateTimer = 0.0f;
        
        // Get components
        NPCEntity npcEntity = chunk.getComponent(index, npcEntityType);
        Nameplate nameplate = chunk.getComponent(index, nameplateType);
        EntityStatMap statMap = chunk.getComponent(index, statMapType);
        
        if (npcEntity == null || nameplate == null || statMap == null) {
            return;
        }
        
        // Get role name
        String roleName = npcEntity.getRoleName();
        if (roleName == null || roleName.isEmpty()) {
            roleName = "NPC";
        }
        
        // Get current health
        int healthIndex = DefaultEntityStatTypes.getHealth();
        EntityStatValue healthStat = statMap.get(healthIndex);
        if (healthStat == null) {
            return;
        }
        
        float currentHealth = healthStat.get();
        float maxHealth = healthStat.getMax();
        
        // Calculate health percentage for coloring
        float healthPercent = (currentHealth / maxHealth) * 100.0f;
        Color healthColor = getHealthColor(healthPercent);
        
        // Create colored name and health text
        String coloredName = Message.raw(roleName).color(healthColor).getAnsiMessage();
        String healthValue = String.format("%.0f/%.0f", currentHealth, maxHealth);
        String coloredHealth = Message.raw("Health: ").getAnsiMessage() + 
                              Message.raw(healthValue).color(healthColor).getAnsiMessage();
        
        // Format nameplate text with colors
        String nameplateText = coloredName + "\n" + coloredHealth;
        
        // Update nameplate if text changed
        if (!nameplate.getText().equals(nameplateText)) {
            nameplate.setText(nameplateText);
        }
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
}
