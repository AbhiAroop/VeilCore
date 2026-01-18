package com.veilcore;

import java.util.logging.Level;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.veilcore.commands.ProfileCommand;
import com.veilcore.commands.StatsCommand;
import com.veilcore.commands.TestUICommand;
import com.veilcore.listeners.PlayerEventListener;
import com.veilcore.profile.PlayerProfileManager;
import com.veilcore.profile.ProfileRepository;
import com.veilcore.profile.ProfileStateManager;
import com.veilcore.trackers.PlaytimeTracker;

public class VeilCorePlugin extends JavaPlugin {
    private static VeilCorePlugin instance;
    private PlayerProfileManager profileManager;
    private ProfileStateManager stateManager;
    private java.util.concurrent.ScheduledExecutorService playtimeScheduler;

    public VeilCorePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("VeilCore plugin loaded successfully!");

        // Initialize profile system
        java.io.File dataFolder = getDataFolder();
        ProfileRepository repository = new ProfileRepository(dataFolder, java.util.logging.Logger.getLogger("VeilCore"));
        profileManager = new PlayerProfileManager(repository, java.util.logging.Logger.getLogger("VeilCore"), dataFolder);
        stateManager = new ProfileStateManager(repository, java.util.logging.Logger.getLogger("VeilCore"));
        
        getLogger().at(Level.INFO).log("Profile system initialized");

        // Register event listeners
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEventListener::onPlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerEventListener::onPlayerDisconnect);
        // Note: Player death tracking will be added when the death event is available in the API
        
        getLogger().at(Level.INFO).log("Event listeners registered");

        // Register commands
        getCommandRegistry().registerCommand(new TestUICommand());
        getCommandRegistry().registerCommand(new ProfileCommand());
        getCommandRegistry().registerCommand(new StatsCommand(this));
        
        // Start playtime tracker (runs every second)
        playtimeScheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        playtimeScheduler.scheduleAtFixedRate(
            new PlaytimeTracker(this),
            1, // initial delay
            1, // period
            java.util.concurrent.TimeUnit.SECONDS
        );
        
        getLogger().at(Level.INFO).log("VeilCore fully loaded - Profile system active");
    }
    
    protected void teardown() {
        // Shutdown playtime tracker
        if (playtimeScheduler != null && !playtimeScheduler.isShutdown()) {
            playtimeScheduler.shutdown();
            try {
                if (!playtimeScheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    playtimeScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                playtimeScheduler.shutdownNow();
            }
        }
        
        getLogger().at(Level.INFO).log("VeilCore plugin unloaded");
    }
    
    public static VeilCorePlugin getInstance() {
        return instance;
    }
    
    public PlayerProfileManager getProfileManager() {
        return profileManager;
    }
    
    public ProfileStateManager getStateManager() {
        return stateManager;
    }
    
    private java.io.File getDataFolder() {
        java.io.File dataFolder = new java.io.File("plugins/VeilCore");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }
}
