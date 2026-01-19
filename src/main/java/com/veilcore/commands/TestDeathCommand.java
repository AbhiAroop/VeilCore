package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;

/**
 * Temporary command to test death tracking manually.
 * Increments death counter without actually dying.
 */
public class TestDeathCommand extends AbstractPlayerCommand {
    
    private final VeilCorePlugin plugin;
    
    public TestDeathCommand(VeilCorePlugin plugin) {
        super("testdeath", "Manually increment death counter (testing)");
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
        
        Profile profile = plugin.getProfileManager().getActiveProfile(player.getUuid());
        
        if (profile == null) {
            playerRef.sendMessage(Message.raw("You don't have an active profile!").color("#FF5555"));
            return;
        }
        
        // Increment death counter
        profile.getStats().incrementDeaths();
        
        // Save profile
        plugin.getProfileManager().saveProfile(profile);
        
        playerRef.sendMessage(
            Message.raw("Death counter incremented! Total deaths: " + profile.getStats().getDeaths())
                .color("#55FF55")
        );
    }
}
