package com.veilcore.commands;

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

public class EntitySpawnCommand extends AbstractPlayerCommand {

    public EntitySpawnCommand() {
        super("entityspawn", "Spawn a test hologram entity");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Transform playerTransform = playerRef.getTransform();

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
                hologramTransform.getPosition().setY(hologramTransform.getPosition().getY() + 2.0);
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
                    new Nameplate("Test Hologram Entity")
                );
                
                // Spawn the entity
                world.getEntityStore().getStore().addEntity(holder, com.hypixel.hytale.component.AddReason.SPAWN);
                
                // Confirm to player
                playerRef.sendMessage(Message.raw("Spawned hologram entity 2 blocks above you!").color("#55FF55"));
                
            } catch (Exception e) {
                playerRef.sendMessage(Message.raw("Error spawning hologram: " + e.getMessage()).color("#FF5555"));
                e.printStackTrace();
            }
        });
    }
}
