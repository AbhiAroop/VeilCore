package com.veilcore.commands;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;

public class DebugHealthCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;

    public DebugHealthCommand(VeilCorePlugin plugin) {
        super("debughealth", "Debug health stat values");
        this.plugin = plugin;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        
        if (statMap == null) {
            playerRef.sendMessage(Message.raw("Failed to get stat map").color("#FF0000"));
            return;
        }

        int healthIndex = DefaultEntityStatTypes.getHealth();
        EntityStatValue healthStat = statMap.get(healthIndex);
        
        if (healthStat == null) {
            playerRef.sendMessage(Message.raw("Failed to get health stat").color("#FF0000"));
            return;
        }

        playerRef.sendMessage(Message.raw("=== Health Stat Debug ===").color("#FFAA00"));
        playerRef.sendMessage(Message.raw("Current: " + healthStat.get()).color("#AAAAAA"));
        playerRef.sendMessage(Message.raw("Min: " + healthStat.getMin()).color("#AAAAAA"));
        playerRef.sendMessage(Message.raw("Max: " + healthStat.getMax()).color("#AAAAAA"));
        playerRef.sendMessage(Message.raw("Percentage: " + (healthStat.asPercentage() * 100) + "%").color("#AAAAAA"));
        playerRef.sendMessage(Message.raw("Modifiers: " + healthStat.getModifiers().size()).color("#AAAAAA"));
        
        // Show all modifiers
        healthStat.getModifiers().forEach((id, modifier) -> {
            playerRef.sendMessage(Message.raw("  - " + id + ": " + modifier.toString()).color("#FFFFFF"));
        });
    }
}
