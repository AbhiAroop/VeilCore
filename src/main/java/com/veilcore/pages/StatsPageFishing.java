package com.veilcore.pages;

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
import com.veilcore.profile.ProfileStats;

import javax.annotation.Nonnull;

public class StatsPageFishing extends InteractiveCustomUIPage<StatsPageFishing.StatsEventData> {
    
    private final Profile profile;
    private final PlayerRef playerRef;
    
    public static class StatsEventData {
        public String action;
        
        public static final BuilderCodec<StatsEventData> CODEC =
                BuilderCodec.builder(StatsEventData.class, StatsEventData::new)
                    .append(new KeyedCodec<>("Action", Codec.STRING),
                        (StatsEventData o, String v) -> o.action = v,
                        (StatsEventData o) -> o.action)
                    .add()
                    .build();
    }
    
    public StatsPageFishing(@Nonnull PlayerRef playerRef, Profile profile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, StatsEventData.CODEC);
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
        cmd.append("Pages/StatsPageFishing.ui");
        
        ProfileStats stats = profile.getStats();
        
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        String displayName = player != null ? player.getDisplayName() : "Player";
        cmd.set("#ProfileName.Text", displayName + "'s Stats");
        cmd.set("#FishingFortune.Text", String.format("%.2f", stats.getFishingFortune()));
        cmd.set("#LurePotency.Text", String.valueOf(stats.getLurePotency()));
        cmd.set("#FishingResilience.Text", String.format("%.2f", stats.getFishingResilience()));
        cmd.set("#FishingFocus.Text", String.format("%.2f", stats.getFishingFocus()));
        cmd.set("#FishingPrecision.Text", String.format("%.2f", stats.getFishingPrecision()));
        cmd.set("#SeaMonsterAffinity.Text", String.format("%.2f", stats.getSeaMonsterAffinity()));
        cmd.set("#TreasureSense.Text", String.format("%.2f", stats.getTreasureSense()));
        
        // Event bindings for navigation
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CombatButton", new EventData().append("Action", "Combat"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#MiningButton", new EventData().append("Action", "Mining"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#FarmingButton", new EventData().append("Action", "Farming"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#FishingButton", new EventData().append("Action", "Fishing"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#GeneralButton", new EventData().append("Action", "General"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", new EventData().append("Action", "Close"));
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull StatsEventData data
    ) {
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        
        switch (data.action) {
            case "Combat":
                player.getPageManager().openCustomPage(ref, store, new StatsPageCombat(playerRef, profile));
                break;
            case "Mining":
                player.getPageManager().openCustomPage(ref, store, new StatsPageMining(playerRef, profile));
                break;
            case "Farming":
                player.getPageManager().openCustomPage(ref, store, new StatsPageFarming(playerRef, profile));
                break;
            case "General":
                player.getPageManager().openCustomPage(ref, store, new StatsPageGeneral(playerRef, profile));
                break;
            case "Close":
                player.getPageManager().setPage(ref, store, com.hypixel.hytale.protocol.packets.interface_.Page.None);
                break;
            default:
                // Already on Fishing page, do nothing
                break;
        }
    }
}
