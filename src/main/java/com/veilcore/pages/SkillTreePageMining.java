package com.veilcore.pages;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.profile.Profile;
import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;
import com.veilcore.skills.trees.PlayerSkillTreeData;

/**
 * Simple skill tree page - displays tokens and coming soon message
 */
public class SkillTreePageMining extends InteractiveCustomUIPage<SkillTreePageMining.TreeEventData> {
    
    private final Profile profile;
    private final PlayerRef playerRef;
    
    public static class TreeEventData {
        public String action;
        
        public static final BuilderCodec<TreeEventData> CODEC =
                BuilderCodec.builder(TreeEventData.class, TreeEventData::new)
                    .append(new KeyedCodec<>("Action", Codec.STRING),
                        (TreeEventData o, String v) -> o.action = v,
                        (TreeEventData o) -> o.action)
                    .add()
                    .build();
    }
    
    public SkillTreePageMining(@Nonnull PlayerRef playerRef, Profile profile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, TreeEventData.CODEC);
        this.profile = profile;
        this.playerRef = playerRef;
    }
    
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd, 
                     @Nonnull UIEventBuilder evt, @Nonnull Store<EntityStore> store) {
        
        // Load the UI file
        cmd.append("Pages/SkillTreePageMining.ui");
        
        // Get player's token count
        PlayerSkillTreeData treeData = profile.getSkills().getTreeData();
        int tokenCount = treeData.getTokenCount(Skill.MINING.getId(), TokenTier.BASIC);
        
        // Update token display
        cmd.set("#TokenDisplay.Text", "Basic Tokens: " + tokenCount);
        
        // Add close button event
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
            new EventData().append("Action", "Close"));
    }
    
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, 
                                TreeEventData data) {
        super.handleDataEvent(ref, store, data);
        
        Player player = store.getComponent(ref, Player.getComponentType());
        
        if ("Close".equals(data.action)) {
            player.getPageManager().setPage(ref, store, 
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
        }
    }
}
