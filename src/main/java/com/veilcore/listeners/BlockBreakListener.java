package com.veilcore.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;
import com.veilcore.skills.Skill;
import com.veilcore.skills.SkillLevel;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.subskills.mining.OreExtraction;

import javax.annotation.Nonnull;

/**
 * Listens for block break events using ECS event system
 * Awards mining XP when players break ore blocks
 */
public class BlockBreakListener extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final VeilCorePlugin plugin;

    public BlockBreakListener(VeilCorePlugin plugin) {
        super(BreakBlockEvent.class);
        this.plugin = plugin;
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
        
        // Get block type ID from the event
        String blockId = event.getBlockType().getId();
        
        // Get player's profile
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        if (profile == null) {
            return; // Player doesn't have an active profile
        }
        
        // Check if the broken block is an ore
        OreExtraction.OreRarity rarity = OreExtraction.getOreRarity(blockId);
        if (rarity == null) {
            return; // Not an ore block, no XP
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
        
        // Send XP gained notification AFTER level up
        Message primaryMsg = Message.raw(String.format("+%d Mining XP", xpGained))
            .color("#FFD700")
            .bold(true);
        Message secondaryMsg = Message.raw(String.format("Ore Extraction: %s ore", rarity.name()))
            .color("#FFFFFF");
        
        NotificationUtil.sendNotification(
            packetHandler,
            primaryMsg,
            secondaryMsg,
            icon
        );
    }
}
