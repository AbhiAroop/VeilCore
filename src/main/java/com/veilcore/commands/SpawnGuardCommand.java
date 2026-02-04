package com.veilcore.commands;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.RoleUtils;

public class SpawnGuardCommand extends AbstractPlayerCommand {

    public SpawnGuardCommand() {
        super("spawnguard", "Spawns an armored guard NPC");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        world.execute(() -> {
            try {
                // Get player's position and rotation
                Transform playerTransform = playerRef.getTransform();
                Vector3d playerPos = playerTransform.getPosition();
                Vector3f playerRot = playerTransform.getRotation();
                
                // Calculate spawn position (2 blocks in front of player)
                double yaw = Math.toRadians(playerRot.getY());
                double spawnX = playerPos.getX() - Math.sin(yaw) * 2;
                double spawnY = playerPos.getY();
                double spawnZ = playerPos.getZ() + Math.cos(yaw) * 2;
                Vector3d spawnPosition = new Vector3d(spawnX, spawnY, spawnZ);
                
                // Face the NPC towards the player
                Vector3f npcRotation = new Vector3f(0, playerRot.getY() + 180, 0);
                
                NPCPlugin npcPlugin = NPCPlugin.get();
                
                // Try Test_tag_PlayerTestModel_V first
                String roleName = "Test_tag_PlayerTestModel_V";
                int roleIndex = npcPlugin.getIndex(roleName);
                
                if (roleIndex == Integer.MIN_VALUE || roleIndex < 0) {
                    // Try VeilCore:GuardRole
                    playerRef.sendMessage(Message.raw("Test_tag_PlayerTestModel_V not found, trying VeilCore:GuardRole...").color("#FFAA00"));
                    roleName = "VeilCore:GuardRole";
                    roleIndex = npcPlugin.getIndex(roleName);
                    
                    if (roleIndex == Integer.MIN_VALUE || roleIndex < 0) {
                        // Try without namespace
                        playerRef.sendMessage(Message.raw("VeilCore:GuardRole not found, trying GuardRole...").color("#FFAA00"));
                        roleName = "GuardRole";
                        roleIndex = npcPlugin.getIndex(roleName);
                        
                        if (roleIndex == Integer.MIN_VALUE || roleIndex < 0) {
                            // Fallback to TEST_PLAYER_HIDDEN_TEMPLATE
                            playerRef.sendMessage(Message.raw("Custom GuardRole not found, trying TEST_PLAYER_HIDDEN_TEMPLATE...").color("#FFAA00"));
                            roleName = "TEST_PLAYER_HIDDEN_TEMPLATE";
                            roleIndex = npcPlugin.getIndex(roleName);
                            
                            if (roleIndex == Integer.MIN_VALUE || roleIndex < 0) {
                                playerRef.sendMessage(Message.raw("Failed to find any valid NPC role").color("#FF5555"));
                            
                                // Get list of all available roles
                                java.util.List<String> availableRoles = npcPlugin.getRoleTemplateNames(true);
                                if (!availableRoles.isEmpty()) {
                                    playerRef.sendMessage(Message.raw("Available spawnable roles (" + availableRoles.size() + " total):").color("#FFAA00"));
                                    int limit = Math.min(10, availableRoles.size());
                                    for (int i = 0; i < limit; i++) {
                                        playerRef.sendMessage(Message.raw("  - " + availableRoles.get(i)).color("#AAAAAA"));
                                    }
                                    if (availableRoles.size() > 10) {
                                        playerRef.sendMessage(Message.raw("... and " + (availableRoles.size() - 10) + " more").color("#AAAAAA"));
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
                
                playerRef.sendMessage(Message.raw("Spawning guard NPC with role: " + roleName).color("#55FF55"));
                
                // Spawn the NPC
                npcPlugin.spawnEntity(
                    store,
                    roleIndex,
                    spawnPosition,
                    npcRotation,
                    null, // spawnModel
                    null, // preAddToWorld callback
                    (npcEntity, npcRef, npcStore) -> {
                        // Post-spawn callback - equip the NPC with armor and weapon
                        try {
                            equipGuard(npcEntity, npcRef, npcStore, playerRef);
                        } catch (Exception e) {
                            playerRef.sendMessage(Message.raw("Failed to equip guard: " + e.getMessage()).color("#FF5555"));
                            e.printStackTrace();
                        }
                    }
                );
                
                playerRef.sendMessage(Message.raw("Guard NPC spawned successfully!").color("#55FF55"));
                
            } catch (Exception e) {
                playerRef.sendMessage(Message.raw("Error spawning guard: " + e.getMessage()).color("#FF5555"));
                e.printStackTrace();
            }
        });
    }
    
    private void equipGuard(NPCEntity npcEntity, Ref<EntityStore> npcRef, Store<EntityStore> store, PlayerRef playerRef) {
        try {
            // Set weapon in hotbar - this works!
            String[] weaponPatterns = {
                "Weapon_Spear_Iron",
                "Hytale:Weapon_Spear_Iron"
            };
            
            boolean weaponEquipped = false;
            for (String weaponName : weaponPatterns) {
                try {
                    String[] hotbarItems = {weaponName};
                    RoleUtils.setHotbarItems(npcEntity, hotbarItems);
                    playerRef.sendMessage(Message.raw("Equipped weapon: " + weaponName).color("#55FF55"));
                    weaponEquipped = true;
                    break;
                } catch (Exception e) {
                    // Try next pattern
                }
            }
            
            if (!weaponEquipped) {
                playerRef.sendMessage(Message.raw("Note: Armor system requires proper ItemArmor configuration").color("#FFAA00"));
            }
            
        } catch (Exception e) {
            playerRef.sendMessage(Message.raw("Equipment error: " + e.getMessage()).color("#FF5555"));
            e.printStackTrace();
        }
    }
}
