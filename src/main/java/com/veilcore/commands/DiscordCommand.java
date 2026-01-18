package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Command to display the Discord server invite link.
 */
public class DiscordCommand extends AbstractPlayerCommand {
    
    private static final String DISCORD_LINK = "https://discord.gg/6SDWFb7ZTD";
    
    public DiscordCommand() {
        super("discord", "Get the Discord server invite link");
    }
    
    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        // Send Discord invite message with clickable link
        
        Message headerLine = Message.raw("================================")
                .color("#55FFFF")  // Aqua/cyan
                .bold(true);
        
        Message title = Message.raw("Join our Discord community!")
                .color("#55FFFF")  // Aqua/cyan
                .bold(true);
        
        Message instruction = Message.raw("Click the link below to join:")
                .color("#AAAAAA");  // Gray
        
        // This makes the link clickable!
        Message link = Message.raw(DISCORD_LINK)
                .color("#FFD700")  // Gold
                .link(DISCORD_LINK);
        
        playerRef.sendMessage(headerLine);
        playerRef.sendMessage(Message.empty());
        playerRef.sendMessage(title);
        playerRef.sendMessage(Message.empty());
        playerRef.sendMessage(instruction);
        playerRef.sendMessage(link);
        playerRef.sendMessage(Message.empty());
        playerRef.sendMessage(headerLine);
    }
}
