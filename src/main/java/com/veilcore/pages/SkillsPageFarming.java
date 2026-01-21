package com.veilcore.pages;

import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.profile.Profile;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.Skill;
import com.veilcore.skills.SkillLevel;
import com.veilcore.skills.tokens.SkillToken.TokenTier;

public class SkillsPageFarming extends InteractiveCustomUIPage<SkillsPageFarming.SkillsEventData> {
    
    private final Profile profile;
    private final PlayerRef playerRef;
    
    public static class SkillsEventData {
        public String action;
        
        public static final BuilderCodec<SkillsEventData> CODEC =
                BuilderCodec.builder(SkillsEventData.class, SkillsEventData::new)
                    .append(new KeyedCodec<>("Action", Codec.STRING),
                        (SkillsEventData o, String v) -> o.action = v,
                        (SkillsEventData o) -> o.action)
                    .add()
                    .build();
    }
    
    public SkillsPageFarming(@Nonnull PlayerRef playerRef, Profile profile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, SkillsEventData.CODEC);
        this.profile = profile;
        this.playerRef = playerRef;
    }
    
    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/SkillsPageFarming.ui");
        
        ProfileSkills skills = profile.getSkills();
        SkillLevel farmingLevel = skills.getSkillLevel(Skill.FARMING);
        
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        String displayName = player != null ? player.getDisplayName() : "Player";
        
        // Set level info
        cmd.set("#CurrentLevel.Text", String.valueOf(farmingLevel.getLevel()));
        if (farmingLevel.isMaxLevel()) {
            cmd.set("#NextLevel.Text", "MAX");
            cmd.set("#XPToNext.Text", "0");
            cmd.set("#Progress.Text", "100%");
        } else {
            cmd.set("#NextLevel.Text", String.valueOf(farmingLevel.getLevel() + 1));
            cmd.set("#XPToNext.Text", String.valueOf(farmingLevel.getXpToNextLevel()));
            cmd.set("#Progress.Text", String.format("%.1f%%", farmingLevel.getProgressPercent() * 100));
        }
        
        cmd.set("#CurrentXP.Text", String.valueOf(farmingLevel.getCurrentXp()));
        
        // Calculate total XP
        long totalXp = SkillLevel.getTotalXpForLevel(farmingLevel.getLevel()) + farmingLevel.getCurrentXp();
        cmd.set("#TotalXP.Text", String.valueOf(totalXp));
        
        // Set token counts
        Map<TokenTier, Integer> tokens = skills.getTreeData().getAllTokenCounts(Skill.FARMING.getId());
        cmd.set("#BasicTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.BASIC, 0)));
        cmd.set("#AdvancedTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.ADVANCED, 0)));
        cmd.set("#MasterTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.MASTER, 0)));
        
        // Event bindings for navigation
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#MiningButton", new EventData().append("Action", "Mining"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CombatButton", new EventData().append("Action", "Combat"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#FarmingButton", new EventData().append("Action", "Farming"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#WoodcuttingButton", new EventData().append("Action", "Woodcutting"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#FishingButton", new EventData().append("Action", "Fishing"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", new EventData().append("Action", "Close"));
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull SkillsEventData data
    ) {
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        
        switch (data.action) {
            case "Mining":
                player.getPageManager().openCustomPage(ref, store, new SkillsPageMining(playerRef, profile));
                break;
            case "Combat":
                player.getPageManager().openCustomPage(ref, store, new SkillsPageCombat(playerRef, profile));
                break;
            case "Woodcutting":
                player.getPageManager().openCustomPage(ref, store, new SkillsPageWoodcutting(playerRef, profile));
                break;
            case "Fishing":
                player.getPageManager().openCustomPage(ref, store, new SkillsPageFishing(playerRef, profile));
                break;
            case "Close":
                player.getPageManager().setPage(ref, store, com.hypixel.hytale.protocol.packets.interface_.Page.None);
                break;
            default:
                // Already on Farming page
                break;
        }
    }
}
