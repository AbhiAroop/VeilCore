package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.Message;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.Skill;
import com.veilcore.skills.SkillLevel;
import com.veilcore.skills.subskills.woodcutting.TreeFelling;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

/**
 * Listens for block break events using ECS event system
 * Awards woodcutting XP when players break wood blocks and trees
 */
public class WoodcuttingListener extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final VeilCorePlugin plugin;
    private static final int MAX_TREE_SEARCH_DEPTH = 3;
    private static final int MAX_LOGS_TO_TRACK = 256;

    public WoodcuttingListener(VeilCorePlugin plugin) {
        super(BreakBlockEvent.class);
        this.plugin = plugin;
        plugin.getLogger().at(Level.INFO).log("WoodcuttingListener initialized");
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Only process break events for entities that have the Player component
        return Query.and(Player.getComponentType());
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreakBlockEvent event
    ) {
        // Get the ref for this entity in the chunk
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        
        // Get the player who broke the block
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        
        // Get block type and position from the event
        BlockType blockType = event.getBlockType();
        String blockId = blockType.getId();
        Vector3i blockPos = event.getTargetBlock();
        
        // Check if the broken block is wood
        if (!TreeFelling.isWoodBlock(blockId)) {
            return; // Not a wood block, no woodcutting XP
        }
        
        // Get player's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        if (profile == null) {
            return; // Player doesn't have an active profile
        }
        
        // Get player's world to count connected wood blocks (tree size)
        World world = player.getWorld();
        if (world == null) {
            return;
        }
        
        // Count the tree size by finding connected wood blocks
        int logCount = countConnectedWoodBlocks(world, blockPos, blockId);
        
        if (logCount > 0) {
            // Calculate XP based on tree size and wood type
            long xpAmount = TreeFelling.calculateXp(logCount, blockId);
            
            // Get wood rarity for logging
            TreeFelling.WoodRarity rarity = TreeFelling.WoodRarity.fromBlockId(blockId);
            TreeFelling.TreeSize treeSize = TreeFelling.getTreeSize(logCount);
            
            plugin.getLogger().at(Level.INFO).log(
                "Player " + player.getDisplayName() + " cut " + rarity.name() + " tree (" + blockId + ") with " + logCount + " logs, gaining " + xpAmount + " woodcutting XP"
            );
            
            // Award woodcutting XP
            ProfileSkills skills = profile.getSkills();
            SkillLevel oldWoodcuttingLevel = skills.getSkillLevel(Skill.WOODCUTTING);
            int oldLevel = oldWoodcuttingLevel.getLevel();
            
            int levelsGained = skills.addXp(Skill.WOODCUTTING, xpAmount);
            
            // Save profile
            plugin.getProfileManager().saveProfile(profile);
            
            // Get PlayerRef for notifications
            PlayerRef playerRef = Universe.get().getPlayer(player.getUuid());
            if (playerRef == null) {
                return;
            }
            
            PacketHandler handler = playerRef.getPacketHandler();
            
            ItemWithAllMetadata icon = new ItemStack("Hytale_Menu_Inventory", 1).toPacket();
            
            // Notify player if leveled up
            if (levelsGained > 0) {
                int newLevel = oldLevel + levelsGained;
                
                Message levelUpPrimary = Message.raw(String.format("Woodcutting Level %d", newLevel))
                    .color("#8B4513")
                    .bold(true);
                Message levelUpSecondary = Message.raw(String.format("Level up! +%d level%s", 
                    levelsGained, levelsGained > 1 ? "s" : ""))
                    .color("#FFFFFF");
                
                NotificationUtil.sendNotification(
                    handler,
                    levelUpPrimary,
                    levelUpSecondary,
                    icon
                );
            }
            
            // Send XP gained notification with tree info
            String xpMessage = String.format("+%d Woodcutting XP", xpAmount);
            String secondaryMessage = String.format("Tree Felling: %s tree (%s wood)", 
                treeSize.name(), 
                rarity.name());
            
            Message primaryMsg = Message.raw(xpMessage)
                .color("#8B4513")
                .bold(true);
            Message secondaryMsg = Message.raw(secondaryMessage)
                .color("#FFFFFF");
            
            NotificationUtil.sendNotification(
                handler,
                primaryMsg,
                secondaryMsg,
                icon
            );
        }
    }
    
    /**
     * Count connected wood blocks starting from a position using BFS
     * This estimates the total tree size
     * 
     * @param world The world to search in
     * @param startPos The starting position
     * @param woodBlockId The wood block type to match
     * @return The number of connected wood blocks found (limited to MAX_LOGS_TO_TRACK)
     */
    private int countConnectedWoodBlocks(@Nonnull World world, @Nonnull Vector3i startPos, String woodBlockId) {
        Set<String> positionKey = new HashSet<>();
        Queue<Vector3i> queue = new ArrayDeque<>();
        int logCount = 0;
        
        queue.add(new Vector3i(startPos));
        positionKey.add(vectorToKey(startPos));
        
        // BFS to find all connected wood blocks (including leaves)
        while (!queue.isEmpty() && logCount < MAX_LOGS_TO_TRACK) {
            Vector3i currentPos = queue.poll();
            logCount++;
            
            // Check all 6 adjacent blocks (up, down, north, south, east, west)
            Vector3i[] adjacentPositions = {
                new Vector3i(currentPos.x + 1, currentPos.y, currentPos.z),
                new Vector3i(currentPos.x - 1, currentPos.y, currentPos.z),
                new Vector3i(currentPos.x, currentPos.y + 1, currentPos.z),
                new Vector3i(currentPos.x, currentPos.y - 1, currentPos.z),
                new Vector3i(currentPos.x, currentPos.y, currentPos.z + 1),
                new Vector3i(currentPos.x, currentPos.y, currentPos.z - 1)
            };
            
            for (Vector3i adjacentPos : adjacentPositions) {
                String key = vectorToKey(adjacentPos);
                if (positionKey.contains(key) || logCount >= MAX_LOGS_TO_TRACK) {
                    continue; // Already visited or limit reached
                }
                
                // Get the block at this position
                try {
                    BlockType block = getBlockAtPosition(world, adjacentPos);
                    if (block != null) {
                        String blockId = block.getId();
                        
                        // Add to queue if it's wood or leaves (leaves are part of tree but count towards size)
                        if (TreeFelling.isWoodBlock(blockId) || TreeFelling.isLeavesBlock(blockId)) {
                            positionKey.add(key);
                            queue.add(new Vector3i(adjacentPos));
                        }
                    }
                } catch (Exception e) {
                    // Ignore errors getting block info, continue searching
                }
            }
        }
        
        return logCount;
    }
    
    /**
     * Get the block type at a specific position in the world
     * 
     * @param world The world to query
     * @param position The position to check
     * @return The BlockType at that position, or null if no block or can't access
     */
    private BlockType getBlockAtPosition(@Nonnull World world, @Nonnull Vector3i position) {
        try {
            WorldChunk worldChunk = world.getChunkIfLoaded(
                com.hypixel.hytale.math.util.ChunkUtil.indexChunkFromBlock(position.x, position.z)
            );
            if (worldChunk == null) {
                return null;
            }
            
            BlockChunk blockChunk = worldChunk.getBlockChunk();
            if (blockChunk == null) {
                return null;
            }
            
            int blockTypeId = blockChunk.getBlock(position.x, position.y, position.z);
            return BlockType.getAssetMap().getAsset(blockTypeId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Convert a Vector3i position to a unique string key for Set storage
     */
    private String vectorToKey(Vector3i vec) {
        return vec.x + "," + vec.y + "," + vec.z;
    }
}
