package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockBreakingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;
import com.veilcore.profile.ProfileStats;
import com.veilcore.skills.Skill;
import com.veilcore.skills.SkillLevel;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.subskills.mining.OreExtraction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * Listens for block break events using ECS event system
 * Awards mining XP when players break ore blocks
 * Applies mining fortune to multiply ore drops
 */
public class BlockBreakListener extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final VeilCorePlugin plugin;
    private final Random random;

    public BlockBreakListener(VeilCorePlugin plugin) {
        super(BreakBlockEvent.class);
        this.plugin = plugin;
        this.random = new Random();
        plugin.getLogger().at(Level.INFO).log("BlockBreakListener initialized with fortune system");
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
        
        // Get player's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        if (profile == null) {
            return; // Player doesn't have an active profile
        }
        
        // Check if the broken block is an ore
        OreExtraction.OreRarity rarity = OreExtraction.getOreRarity(blockId);
        if (rarity == null) {
            return; // Not an ore block, no XP or fortune processing
        }
        
        // Get mining fortune stat
        ProfileStats stats = profile.getStats();
        double miningFortune = stats.getMiningFortune();
        
        plugin.getLogger().at(Level.INFO).log("Player " + player.getDisplayName() + " mining " + blockId + " with fortune: " + miningFortune);
        
        // Calculate drop multiplier based on fortune
        // Formula: 100 fortune = 2x drops, 200 = 3x, etc.
        // Remainder gives % chance for +1 more
        int baseMultiplier = 1 + (int)(miningFortune / 100.0);
        double remainderChance = miningFortune % 100.0;
        
        int dropMultiplier = baseMultiplier;
        if (remainderChance > 0 && random.nextDouble() * 100.0 < remainderChance) {
            dropMultiplier++;
        }
        
        plugin.getLogger().at(Level.INFO).log("Drop multiplier calculated: " + dropMultiplier + " (base: " + baseMultiplier + ", chance: " + remainderChance + "%)");
        
        // Only spawn extra drops if multiplier > 1 (don't cancel, let vanilla break happen)
        if (dropMultiplier > 1) {
            // Get the default drops for this block
            List<ItemStack> defaultDrops = getBlockDrops(blockType);
            
            plugin.getLogger().at(Level.INFO).log("Default drops found: " + defaultDrops.size());
            for (ItemStack drop : defaultDrops) {
                plugin.getLogger().at(Level.INFO).log("  - " + drop.getItemId() + " x" + drop.getQuantity());
            }
            
            // Calculate extra drops (multiplier - 1 since vanilla will drop 1x)
            int extraMultiplier = dropMultiplier - 1;
            List<ItemStack> extraDrops = new ArrayList<>();
            for (ItemStack drop : defaultDrops) {
                // Create new ItemStack with extra quantity
                ItemStack extra = new ItemStack(
                    drop.getItemId(),
                    drop.getQuantity() * extraMultiplier
                );
                extraDrops.add(extra);
                plugin.getLogger().at(Level.INFO).log("Creating extra drop: " + extra.getItemId() + " x" + extra.getQuantity());
            }
            
            // Spawn the extra drops at the block location
            if (!extraDrops.isEmpty()) {
                Vector3d spawnPos = new Vector3d(
                    blockPos.getX() + 0.5,
                    blockPos.getY() + 0.5,
                    blockPos.getZ() + 0.5
                );
                Vector3f velocity = new Vector3f(0, 0.2f, 0);
                
                plugin.getLogger().at(Level.INFO).log("Spawning " + extraDrops.size() + " extra item stacks at " + spawnPos);
                
                // Generate item drop entities
                Holder<EntityStore>[] itemEntities = ItemComponent.generateItemDrops(store, extraDrops, spawnPos, velocity);
                
                // Queue each entity to be added to the world using the command buffer
                for (Holder<EntityStore> itemEntity : itemEntities) {
                    commandBuffer.addEntity(itemEntity, com.hypixel.hytale.component.AddReason.SPAWN);
                    plugin.getLogger().at(Level.INFO).log("Queued item entity to be added to world");
                }
            } else {
                plugin.getLogger().at(Level.WARNING).log("No extra drops to spawn!");
            }
        } else {
            plugin.getLogger().at(Level.INFO).log("Fortune multiplier is 1, skipping extra drops");
        }
        
        // Calculate XP from ore rarity
        long xpGained = OreExtraction.calculateXp(rarity);
        
        // Get current level before adding XP
        ProfileSkills skills = profile.getSkills();
        SkillLevel miningLevel = skills.getSkillLevel(Skill.MINING);
        int oldLevel = miningLevel.getLevel();
        
        // Add XP to mining skill
        int levelsGained = skills.addXp(Skill.MINING, xpGained);
        
        // Save profile
        plugin.getProfileManager().saveProfile(profile);
        
        // Get PlayerRef for notifications
        PlayerRef playerRef = Universe.get().getPlayer(player.getUuid());
        if (playerRef == null) {
            return;
        }
        
        PacketHandler packetHandler = playerRef.getPacketHandler();
        
        ItemWithAllMetadata icon = new ItemStack("Rubble_Calcite_Medium", 1).toPacket();
        
        // Send level up notification FIRST if leveled up
        if (levelsGained > 0) {
            int newLevel = oldLevel + levelsGained;
            
            Message levelUpPrimary = Message.raw(String.format("Mining Level %d", newLevel))
                .color("#FFD700")
                .bold(true);
            Message levelUpSecondary = Message.raw(String.format("Level up! +%d level%s", 
                levelsGained, levelsGained > 1 ? "s" : ""))
                .color("#FFFFFF");
            
            NotificationUtil.sendNotification(
                packetHandler,
                levelUpPrimary,
                levelUpSecondary,
                icon
            );
        }
        
        // Send XP gained notification with fortune info if applied
        String xpMessage = String.format("+%d Mining XP", xpGained);
        String secondaryMessage = String.format("Ore Extraction: %s ore", rarity.name());
        
        if (dropMultiplier > 1) {
            secondaryMessage += String.format(" | %dx drops (%.1f%% fortune)", dropMultiplier, miningFortune);
        }
        
        Message primaryMsg = Message.raw(xpMessage)
            .color("#FFD700")
            .bold(true);
        Message secondaryMsg = Message.raw(secondaryMessage)
            .color("#FFFFFF");
        
        NotificationUtil.sendNotification(
            packetHandler,
            primaryMsg,
            secondaryMsg,
            icon
        );
    }
    
    /**
     * Get the default drops for a block type
     * @param blockType The block type
     * @return List of item stacks that would normally drop
     */
    private List<ItemStack> getBlockDrops(BlockType blockType) {
        List<ItemStack> drops = new ArrayList<>();
        String blockId = blockType.getId();
        
        // Map ore blocks to their drop items
        String dropItemId = getOreDropItem(blockId);
        
        if (dropItemId != null) {
            drops.add(new ItemStack(dropItemId, 1));
            plugin.getLogger().at(Level.INFO).log("Mapped ore " + blockId + " to drop item: " + dropItemId);
        } else {
            plugin.getLogger().at(Level.WARNING).log("No drop mapping found for ore: " + blockId);
        }
        
        return drops;
    }
    
    /**
     * Get the item that drops from an ore block
     * @param blockId The block ID
     * @return The item ID that drops, or null if not an ore
     */
    private String getOreDropItem(String blockId) {
        // Map ore blocks to their corresponding ore items
        // All ore variants (Basalt, Sandstone, Shale, Slate, Stone, Volcanic, Magma) drop the base ore item
        
        if (blockId.startsWith("Ore_Adamantite_")) {
            return "Ore_Adamantite";
        } else if (blockId.startsWith("Ore_Cobalt_")) {
            return "Ore_Cobalt";
        } else if (blockId.startsWith("Ore_Copper_")) {
            return "Ore_Copper";
        } else if (blockId.startsWith("Ore_Gold_")) {
            return "Ore_Gold";
        } else if (blockId.startsWith("Ore_Iron_")) {
            return "Ore_Iron";
        } else if (blockId.startsWith("Ore_Mithril_")) {
            return "Ore_Mithril";
        } else if (blockId.startsWith("Ore_Onyxium_")) {
            return "Ore_Onyxium";
        } else if (blockId.startsWith("Ore_Silver_")) {
            return "Ore_Silver";
        } else if (blockId.startsWith("Ore_Thorium_")) {
            return "Ore_Thorium";
        } else if (blockId.startsWith("Ore_Coal_")) {
            return "Ore_Coal";
        }
        
        return null;
    }
}
