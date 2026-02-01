package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.util.ChunkUtil;
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
 * Scans tree structure to detect if breaking will cause collapse
 */
public class WoodcuttingListener extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final VeilCorePlugin plugin;

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
        
        // Get block type from the event
        BlockType blockType = event.getBlockType();
        String blockId = blockType.getId();
        Vector3i blockPos = event.getTargetBlock();
        
        // Check if the broken block is wood
        if (!TreeFelling.isWoodBlock(blockId)) {
            return; // Not a wood block, no woodcutting XP
        }
        
        UUID playerUuid = player.getUuid();
        World world = player.getWorld();
        
        // Scan the tree to see how many blocks will collapse
        int treeSize = scanTreeForCollapse(world, blockPos, blockId);
        
        // Award XP based on detected tree size
        awardWoodcuttingXp(player, playerUuid, treeSize, blockId);
    }
    
    /**
     * Scan the tree structure to determine how many blocks will collapse when this block is broken
     * Only counts blocks at or above the broken block's Y level and within horizontal radius
     */
    private int scanTreeForCollapse(World world, Vector3i breakPos, String woodType) {
        Set<String> scannedBlocks = new HashSet<>();
        Queue<Vector3i> toScan = new LinkedList<>();
        int woodCount = 1; // Count the block being broken
        int blocksChecked = 0;
        int woodBlocksFound = 0;
        final int MAX_HORIZONTAL_DISTANCE = 8; // Limit tree scan to 8 blocks radius horizontally
        
        plugin.getLogger().at(Level.INFO).log("Starting tree scan at " + breakPos.getX() + "," + breakPos.getY() + "," + breakPos.getZ());
        
        // First check if there are adjacent wood blocks at the same Y level horizontally
        // If so, the tree is still grounded and won't collapse (multi-block trunk base)
        int[][] horizontalDirections = {
            {1, 0, 0},   // East
            {-1, 0, 0},  // West
            {0, 0, 1},   // South
            {0, 0, -1}   // North
        };
        
        for (int[] dir : horizontalDirections) {
            int checkX = breakPos.getX() + dir[0];
            int checkY = breakPos.getY(); // Only check same Y level
            int checkZ = breakPos.getZ() + dir[2];
            
            BlockType adjacentBlock = getBlockAtCoords(world, checkX, checkY, checkZ);
            if (adjacentBlock != null && TreeFelling.isWoodBlock(adjacentBlock.getId())) {
                plugin.getLogger().at(Level.INFO).log("  Tree still grounded - found support at " + checkX + "," + checkY + "," + checkZ);
                return 1; // Tree won't collapse, only award XP for the single block broken
            }
        }
        
        // No ground support found, tree will collapse - scan upward
        // Start by scanning blocks connected to the one being broken
        toScan.add(new Vector3i(breakPos));
        scannedBlocks.add(breakPos.getX() + "," + breakPos.getY() + "," + breakPos.getZ());
        
        // BFS to find all connected wood blocks at or above this level
        while (!toScan.isEmpty() && woodCount < 500) { // Limit to prevent infinite loops
            Vector3i current = toScan.poll();
            
            // Only scan directly adjacent blocks (6 directions: up, down, north, south, east, west)
            // This ensures we only find truly connected wood blocks, not diagonal jumps
            int[][] directions = {
                {0, 1, 0},   // Up
                {0, -1, 0},  // Down
                {1, 0, 0},   // East
                {-1, 0, 0},  // West
                {0, 0, 1},   // South
                {0, 0, -1}   // North
            };
            
            for (int[] dir : directions) {
                int neighborX = current.getX() + dir[0];
                int neighborY = current.getY() + dir[1];
                int neighborZ = current.getZ() + dir[2];
                        
                // Only scan blocks at or above the break position Y level
                if (neighborY < breakPos.getY()) continue;
                
                // Only scan blocks within horizontal radius (prevents scanning across multiple trees)
                int horizontalDistSq = (neighborX - breakPos.getX()) * (neighborX - breakPos.getX()) + 
                                       (neighborZ - breakPos.getZ()) * (neighborZ - breakPos.getZ());
                if (horizontalDistSq > MAX_HORIZONTAL_DISTANCE * MAX_HORIZONTAL_DISTANCE) continue;
                
                String key = neighborX + "," + neighborY + "," + neighborZ;
                if (scannedBlocks.contains(key)) continue;
                scannedBlocks.add(key);
                
                blocksChecked++;
                
                // Get block at this position
                BlockType neighborBlock = getBlockAtCoords(world, neighborX, neighborY, neighborZ);
                if (neighborBlock != null) {
                    String blockId = neighborBlock.getId();
                    if (blocksChecked <= 10) { // Log first 10 blocks for debugging
                        plugin.getLogger().at(Level.INFO).log("  Checked block at " + neighborX + "," + neighborY + "," + neighborZ + ": " + blockId);
                    }
                    
                    if (TreeFelling.isWoodBlock(blockId)) {
                        woodBlocksFound++;
                        if (woodBlocksFound <= 5) {
                            plugin.getLogger().at(Level.INFO).log("  Found wood block: " + blockId);
                        }
                        woodCount++;
                        toScan.add(new Vector3i(neighborX, neighborY, neighborZ));
                    }
                } else if (blocksChecked <= 10) {
                    plugin.getLogger().at(Level.INFO).log("  Block at " + neighborX + "," + neighborY + "," + neighborZ + " was null");
                }
            }
        }
        
        plugin.getLogger().at(Level.INFO).log("Tree scan complete: checked " + blocksChecked + " positions, found " + woodCount + " wood blocks");
        return woodCount;
    }
    
    /**
     * Get the block type at specific world coordinates
     */
    private BlockType getBlockAtCoords(World world, int x, int y, int z) {
        try {
            // Use proper chunk index calculation from Hytale's ChunkUtil
            long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            
            WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);
            if (worldChunk == null) {
                plugin.getLogger().at(Level.INFO).log("    Chunk not loaded for " + x + "," + y + "," + z + " (index " + chunkIndex + ")");
                return null;
            }
            
            BlockChunk blockChunk = worldChunk.getBlockChunk();
            if (blockChunk == null) {
                plugin.getLogger().at(Level.INFO).log("    BlockChunk null for " + x + "," + y + "," + z);
                return null;
            }
            
            int blockId = blockChunk.getBlock(x, y, z);
            if (blockId == 0) {
                // Block ID 0 is air/empty
                return null;
            }
            
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType == null) {
                plugin.getLogger().at(Level.INFO).log("    BlockType null for ID " + blockId + " at " + x + "," + y + "," + z);
            }
            return blockType;
        } catch (Exception e) {
            plugin.getLogger().at(Level.WARNING).log("    Exception getting block at " + x + "," + y + "," + z + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Award woodcutting XP to the player
     */
    private void awardWoodcuttingXp(Player player, UUID playerUuid, int totalBlocks, String woodType) {
        // Get player's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(playerUuid);
        if (profile == null) {
            return;
        }
        
        // Calculate XP based on tree size
        long xpAmount = TreeFelling.calculateXp(totalBlocks, woodType);
        
        // Get wood rarity and tree size for display
        TreeFelling.WoodRarity rarity = TreeFelling.WoodRarity.fromBlockId(woodType);
        TreeFelling.TreeSize treeSize = TreeFelling.getTreeSize(totalBlocks);
        
        plugin.getLogger().at(Level.INFO).log(
            "Player " + player.getDisplayName() + " broke " + totalBlocks + " " + rarity.name() + " wood blocks (" + treeSize.name() + " tree), gaining " + xpAmount + " woodcutting XP"
        );
        
        // Award woodcutting XP
        ProfileSkills skills = profile.getSkills();
        SkillLevel oldWoodcuttingLevel = skills.getSkillLevel(Skill.WOODCUTTING);
        int oldLevel = oldWoodcuttingLevel.getLevel();
        
        int levelsGained = skills.addXp(Skill.WOODCUTTING, xpAmount);
        
        // Save profile
        plugin.getProfileManager().saveProfile(profile);
        
        // Get PlayerRef for notifications
        PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
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
        String secondaryMessage = String.format("Felled %d blocks (%s tree, %s wood)", 
            totalBlocks,
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
