package com.veilcore.pages;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.profile.Profile;
import com.veilcore.profile.ProfileStats;

import javax.annotation.Nonnull;

public class StatsPage extends InteractiveCustomUIPage<StatsPage.StatsEventData> {
    
    private final Profile profile;
    
    public static class StatsEventData {
        public static final BuilderCodec<StatsEventData> CODEC =
                BuilderCodec.builder(StatsEventData.class, StatsEventData::new).build();
    }
    
    public StatsPage(@Nonnull PlayerRef playerRef, Profile profile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, StatsEventData.CODEC);
        this.profile = profile;
    }
    
    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/StatsPage.ui");
        
        ProfileStats stats = profile.getStats();
        
        cmd.set("#ProfileName.Text", profile.getProfileName());
        
        // Combat Stats
        cmd.set("#Health.Text", String.valueOf(stats.getHealth()));
        cmd.set("#Stamina.Text", String.valueOf(stats.getStamina()));
        cmd.set("#Mana.Text", stats.getMana() + "/" + stats.getTotalMana());
        cmd.set("#Armor.Text", String.valueOf(stats.getArmor()));
        cmd.set("#Speed.Text", String.format("%.2f", stats.getSpeed()));
        cmd.set("#CritDamage.Text", String.format("%.1f%%", stats.getCriticalDamage() * 100));
        cmd.set("#CritChance.Text", String.format("%.1f%%", stats.getCriticalChance() * 100));
        cmd.set("#AttackSpeed.Text", String.format("%.2f", stats.getAttackSpeed()));
        
        // Fortune Stats
        cmd.set("#MiningFortune.Text", String.format("%.2f", stats.getMiningFortune()));
        cmd.set("#FarmingFortune.Text", String.format("%.2f", stats.getFarmingFortune()));
        cmd.set("#FishingFortune.Text", String.format("%.1f%%", stats.getFishingFortune()));
        cmd.set("#LootingFortune.Text", String.format("%.2f", stats.getLootingFortune()));
        
        // Resource Stats
        cmd.set("#ManaRegen.Text", stats.getManaRegen() + "/s");
        cmd.set("#Luck.Text", String.valueOf(stats.getLuck()));
        
        // Gameplay Stats
        cmd.set("#Kills.Text", String.valueOf(stats.getKills()));
        cmd.set("#Deaths.Text", String.valueOf(stats.getDeaths()));
        
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull StatsEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        player.getPageManager().setPage(ref, store, Page.None);
    }
}
