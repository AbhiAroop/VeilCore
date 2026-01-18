package com.veilcore.commands;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;

/**
 * Test command to spawn a hologram nameplate that follows the player.
 * This creates a tracking system that updates the hologram position every tick.
 */
public class NameplateTestCommand extends AbstractPlayerCommand {

    // Track holograms attached to entities (EntityUUID -> HologramRef)
    private static final ConcurrentHashMap<UUID, Ref<EntityStore>> entityToHologram = new ConcurrentHashMap<>();
    private static boolean trackerRunning = false;

    public NameplateTestCommand() {
        super("nameplatetest", "Spawn a nameplate hologram that follows you");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        UUID playerUUID = playerRef.getUuid();
        Transform playerTransform = playerRef.getTransform();

        // Remove existing hologram if any
        if (entityToHologram.containsKey(playerUUID)) {
            playerRef.sendMessage(Message.raw("Removing existing nameplate...").color("#FFAA00"));
            entityToHologram.remove(playerUUID);
        }

        // Execute on world thread
        world.execute(() -> {
            try {
                // Create entity holder
                Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
                
                // Create projectile component (invisible entity shell)
                ProjectileComponent projectileComponent = new ProjectileComponent("Projectile");
                
                // Add projectile component
                holder.putComponent(ProjectileComponent.getComponentType(), projectileComponent);
                
                // Set position (2 blocks above player)
                Transform hologramTransform = playerTransform.clone();
                hologramTransform.getPosition().setY(hologramTransform.getPosition().getY() + 2.5);
                holder.putComponent(
                    TransformComponent.getComponentType(),
                    new TransformComponent(hologramTransform.getPosition().clone(), hologramTransform.getRotation().clone())
                );
                
                // Ensure UUID component
                holder.ensureComponent(UUIDComponent.getComponentType());
                
                // Initialize projectile
                if (projectileComponent.getProjectile() == null) {
                    projectileComponent.initialize();
                    if (projectileComponent.getProjectile() == null) {
                        playerRef.sendMessage(Message.raw("Failed to initialize projectile component").color("#FF5555"));
                        return;
                    }
                }
                
                // Add network ID
                holder.addComponent(
                    NetworkId.getComponentType(),
                    new NetworkId(world.getEntityStore().getStore().getExternalData().takeNextNetworkId())
                );
                
                // Add nameplate (the hologram text)
                holder.addComponent(
                    Nameplate.getComponentType(),
                    new Nameplate("§e" + playerRef.getUsername() + "'s Nameplate")
                );
                
                // Spawn the entity and get its Ref
                Ref<EntityStore> hologramRef = world.getEntityStore().getStore().addEntity(holder, com.hypixel.hytale.component.AddReason.SPAWN);
                
                // Track this hologram
                entityToHologram.put(playerUUID, hologramRef);
                
                // Start the position tracker if not already running
                if (!trackerRunning) {
                    startPositionTracker(world, store);
                }
                
                // Confirm to player
                playerRef.sendMessage(Message.raw("Spawned nameplate hologram! It will follow you.").color("#55FF55"));
                
            } catch (Exception e) {
                playerRef.sendMessage(Message.raw("Error spawning hologram: " + e.getMessage()).color("#FF5555"));
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Start a repeating task that updates hologram positions to follow their parent entities.
     */
    private void startPositionTracker(World world, Store<EntityStore> store) {
        trackerRunning = true;
        
        // Schedule repeating task (every 50ms / 20 ticks per second)
        VeilCorePlugin.getInstance().getScheduler().scheduleAtFixedRate(() -> {
            if (entityToHologram.isEmpty()) {
                return; // No holograms to track
            }
            
            world.execute(() -> {
                // Update each hologram's position
                entityToHologram.forEach((entityUUID, hologramRef) -> {
                    try {
                        // Get player's current position
                        PlayerRef playerRef = com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(entityUUID);
                        if (playerRef == null) {
                            // Player disconnected, remove tracking
                            entityToHologram.remove(entityUUID);
                            return;
                        }
                        
                        Transform playerTransform = playerRef.getTransform();
                        
                        // Update hologram position (2.5 blocks above player)
                        Transform newTransform = playerTransform.clone();
                        newTransform.getPosition().setY(newTransform.getPosition().getY() + 2.5);
                        
                        // Update the hologram's TransformComponent
                        store.putComponent(
                            hologramRef, 
                            TransformComponent.getComponentType(), 
                            new TransformComponent(newTransform.getPosition().clone(), newTransform.getRotation().clone())
                        );
                        
                    } catch (Exception e) {
                        // Silently fail - entity might have been removed
                        e.printStackTrace();
                    }
                });
            });
        }, 50, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
