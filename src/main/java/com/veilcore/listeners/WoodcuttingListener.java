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
import java.util.logging.Level;

/**
 * Listens for block break events using ECS event system
 * Awards woodcutting XP when players break wood blocks
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
        
        // Calculate XP based on wood type (per block broken)
        long xpAmount = TreeFelling.calculateXpPerBlock(blockId);
        
        // Get wood rarity for logging
        TreeFelling.WoodRarity rarity = TreeFelling.WoodRarity.fromBlockId(blockId);
        
        plugin.getLogger().at(Level.INFO).log(
            "Player " + player.getDisplayName() + " broke " + rarity.name() + " wood block (" + blockId + "), gaining " + xpAmount + " woodcutting XP"
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
        String secondaryMessage = String.format("Tree Felling: %s wood", rarity.name());
        
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
