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
        
        // Add nameplate component with the role name
        holder.addComponent(nameplateType, new Nameplate(roleName));
    }
    
    @Override
    public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
        // Nothing to do on removal
    }
}
