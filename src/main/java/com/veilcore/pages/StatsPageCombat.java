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
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.veilcore.profile.Profile;
import com.veilcore.profile.ProfileStats;

import javax.annotation.Nonnull;

public class StatsPageCombat extends InteractiveCustomUIPage<StatsPageCombat.StatsEventData> {
    
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
    
    public StatsPageCombat(@Nonnull PlayerRef playerRef, Profile profile) {
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
        cmd.append("Pages/StatsPageCombat.ui");
        
        ProfileStats stats = profile.getStats();
        
        Player player = store.getComponent(ref, Player.getComponentType());
        String displayName = player != null ? player.getDisplayName() : "Player";
        cmd.set("#ProfileName.Text", displayName + "'s Stats");
        
        // Get current health from EntityStatMap
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        int healthIndex = DefaultEntityStatTypes.getHealth();
        EntityStatValue healthStat = statMap != null ? statMap.get(healthIndex) : null;
        
        float currentHealth = healthStat != null ? healthStat.get() : 0;
        float maxHealth = healthStat != null ? healthStat.getMax() : stats.getHealth();
        
        // Combat Stats
        cmd.set("#Health.Text", String.format("%.0f / %.0f", currentHealth, maxHealth));
        cmd.set("#Stamina.Text", String.valueOf(stats.getStamina()));
        cmd.set("#Mana.Text", stats.getMana() + "/" + stats.getTotalMana());
        cmd.set("#Armor.Text", String.valueOf(stats.getArmor()));
        cmd.set("#MagicResist.Text", String.valueOf(stats.getMagicResist()));
        cmd.set("#Speed.Text", String.format("%.2f", stats.getSpeed()));
        cmd.set("#PhysDmg.Text", String.valueOf(stats.getPhysicalDamage()));
        cmd.set("#MagicDmg.Text", String.valueOf(stats.getMagicDamage()));
        cmd.set("#RangedDmg.Text", String.valueOf(stats.getRangedDamage()));
        cmd.set("#AttackSpeed.Text", String.format("%.2f", stats.getAttackSpeed()));
        cmd.set("#CritDamage.Text", String.format("%.1f%%", stats.getCriticalDamage() * 100));
        cmd.set("#CritChance.Text", String.format("%.1f%%", stats.getCriticalChance() * 100));
        cmd.set("#BurstDmg.Text", String.format("%.1fx", stats.getBurstDamage()));
        cmd.set("#BurstChance.Text", String.format("%.1f%%", stats.getBurstChance() * 100));
        cmd.set("#CDR.Text", String.valueOf(stats.getCooldownReduction()));
        cmd.set("#LifeSteal.Text", String.format("%.1f%%", stats.getLifeSteal() * 100));
        cmd.set("#Omnivamp.Text", String.format("%.1f%%", stats.getOmnivamp() * 100));
        cmd.set("#HealthRegen.Text", String.format("%.1f", stats.getHealthRegen()));
        
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
        Player player = store.getComponent(ref, Player.getComponentType());
        
        switch (data.action) {
            case "Mining":
                player.getPageManager().openCustomPage(ref, store, new StatsPageMining(playerRef, profile));
                break;
            case "Farming":
                player.getPageManager().openCustomPage(ref, store, new StatsPageFarming(playerRef, profile));
                break;
            case "Fishing":
                player.getPageManager().openCustomPage(ref, store, new StatsPageFishing(playerRef, profile));
                break;
            case "General":
                player.getPageManager().openCustomPage(ref, store, new StatsPageGeneral(playerRef, profile));
                break;
            case "Close":
                player.getPageManager().setPage(ref, store, Page.None);
                break;
            default:
                // Already on Combat page, do nothing
                break;
        }
    }
}
