package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Listens for block break events using ECS event system
 * Awards woodcutting XP when players break wood blocks
 * Detects entire tree structures to award XP for tree felling
 */
public class WoodcuttingListener extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final VeilCorePlugin plugin;
    private static final int MAX_LOGS_TO_TRACK = 256;
    
    // Track recently processed blocks to avoid double XP when trees auto-break
    private final Map<String, Long> recentlyProcessedBlocks = new ConcurrentHashMap<>();
    private static final long BLOCK_TRACKING_DURATION_MS = 2000; // 2 seconds

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
        
        // Check if this block was recently processed (part of auto-collapsed tree)
        String blockKey = vectorToKey(blockPos);
        Long lastProcessed = recentlyProcessedBlocks.get(blockKey);
        if (lastProcessed != null && (System.currentTimeMillis() - lastProcessed) < BLOCK_TRACKING_DURATION_MS) {
            return; // This block was already counted as part of a tree
        }
        
        // Get player's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        if (profile == null) {
            return; // Player doesn't have an active profile
        }
        
        // Get player's world to count connected wood blocks
        World world = player.getWorld();
        if (world == null) {
            return;
        }
        
        // Count the tree size by finding connected wood blocks
        Set<Vector3i> treeBlocks = findConnectedWoodBlocks(world, blockPos, blockId);
        int logCount = treeBlocks.size();
        
        // Mark all blocks in this tree as processed
        long currentTime = System.currentTimeMillis();
        for (Vector3i pos : treeBlocks) {
            recentlyProcessedBlocks.put(vectorToKey(pos), currentTime);
        }
        
        // Clean up old entries (older than tracking duration)
        recentlyProcessedBlocks.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > BLOCK_TRACKING_DURATION_MS
        );
        
        // Calculate XP based on number of logs and wood type
        long xpAmount = TreeFelling.calculateXp(logCount, blockId);
        
        // Get wood rarity and tree size for logging
        TreeFelling.WoodRarity rarity = TreeFelling.WoodRarity.fromBlockId(blockId);
        TreeFelling.TreeSize treeSize = TreeFelling.getTreeSize(logCount);
        
        plugin.getLogger().at(Level.INFO).log(
            "Player " + player.getDisplayName() + " felled " + rarity.name() + " tree (" + blockId + ") with " + logCount + " logs, gaining " + xpAmount + " woodcutting XP"
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
        
        // Send XP gained notification
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
    
    /**
     * Find all connected wood blocks starting from a position using BFS
     * 
     * @param world The world to search in
     * @param startPos The starting position
     * @param woodBlockId The wood block type to match
     * @return Set of all connected wood block positions
     */
    private Set<Vector3i> findConnectedWoodBlocks(@Nonnull World world, @Nonnull Vector3i startPos, String woodBlockId) {
        Set<Vector3i> found = new HashSet<>();
        Queue<Vector3i> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        
        queue.add(new Vector3i(startPos));
        visited.add(vectorToKey(startPos));
        
        // BFS to find all connected wood blocks
        while (!queue.isEmpty() && found.size() < MAX_LOGS_TO_TRACK) {
            Vector3i currentPos = queue.poll();
            
            // Get block at current position
            BlockType currentBlock = getBlockAtPosition(world, currentPos);
            if (currentBlock == null) {
                continue;
            }
            
            String currentBlockId = currentBlock.getId();
            
            // Only count wood blocks (not leaves)
            if (TreeFelling.isWoodBlock(currentBlockId)) {
                found.add(new Vector3i(currentPos));
            }
            
            // Check all 6 adjacent blocks
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
                if (visited.contains(key) || found.size() >= MAX_LOGS_TO_TRACK) {
                    continue;
                }
                
                BlockType adjacentBlock = getBlockAtPosition(world, adjacentPos);
                if (adjacentBlock != null) {
                    String adjacentBlockId = adjacentBlock.getId();
                    
                    // Add to queue if it's wood or leaves (to traverse through)
                    if (TreeFelling.isWoodBlock(adjacentBlockId) || TreeFelling.isLeavesBlock(adjacentBlockId)) {
                        visited.add(key);
                        queue.add(new Vector3i(adjacentPos));
                    }
                }
            }
        }
        
        return found;
    }
    
    /**
     * Get the block type at a specific position in the world
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
     * Convert a Vector3i position to a unique string key
     */
    private String vectorToKey(Vector3i vec) {
        return vec.x + "," + vec.y + "," + vec.z;
    }
}
