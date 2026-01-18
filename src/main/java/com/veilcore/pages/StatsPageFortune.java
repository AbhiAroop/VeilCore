package com.veilcore.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.profile.Profile;
import com.veilcore.profile.ProfileStats;

import javax.annotation.Nonnull;

public class StatsPageFortune extends InteractiveCustomUIPage<StatsPageFortune.StatsEventData> {
    
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
    
    public StatsPageFortune(@Nonnull PlayerRef playerRef, Profile profile) {
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
        cmd.append("Pages/StatsPageFortune.ui");
        
        ProfileStats stats = profile.getStats();
        
        cmd.set("#ProfileName.Text", profile.getProfileName());
        
        // Fortune Stats
        cmd.set("#Mining.Text", String.format("%.2f", stats.getMiningFortune()));
        cmd.set("#Farming.Text", String.format("%.2f", stats.getFarmingFortune()));
        cmd.set("#Fishing.Text", String.format("%.1f%%", stats.getFishingFortune()));
        cmd.set("#Looting.Text", String.format("%.2f", stats.getLootingFortune()));
        
        // Event bindings for navigation
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CombatButton", new EventData().append("Action", "Combat"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#FortuneButton", new EventData().append("Action", "Fortune"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#ResourceButton", new EventData().append("Action", "Resource"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#GameplayButton", new EventData().append("Action", "Gameplay"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", new EventData().append("Action", "Close"));
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull StatsEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        
        switch (data.action) {
            case "Combat":
                player.getPageManager().openCustomPage(ref, store, new StatsPageCombat(playerRef, profile));
                break;
            case "Resource":
                player.getPageManager().openCustomPage(ref, store, new StatsPageResource(playerRef, profile));
                break;
            case "Gameplay":
                player.getPageManager().openCustomPage(ref, store, new StatsPageGameplay(playerRef, profile));
                break;
            case "Close":
                player.getPageManager().setPage(ref, store, Page.None);
                break;
            default:
                // Already on Fortune page, do nothing
                break;
        }
    }
}
