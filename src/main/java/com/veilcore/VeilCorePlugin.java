package com.veilcore;

import java.util.logging.Level;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.veilcore.commands.DiscordCommand;
import com.veilcore.commands.EntitySpawnCommand;
import com.veilcore.commands.GiveSkillTokensCommand;
import com.veilcore.commands.GiveSkillXpCommand;
import com.veilcore.commands.NameplateTestCommand;
import com.veilcore.commands.ProfileCommand;
import com.veilcore.commands.SetSkillLevelCommand;
import com.veilcore.commands.SkillsCommand;
import com.veilcore.commands.StatsCommand;
import com.veilcore.commands.TestDeathCommand;
import com.veilcore.commands.TestMineOreCommand;
import com.veilcore.listeners.PlayerDeathListener;
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

        // Register ECS systems
        getEntityStoreRegistry().registerSystem(new PlayerDeathListener(this));
        getLogger().at(Level.INFO).log("ECS systems registered");

        // Register event listeners
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEventListener::onPlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerEventListener::onPlayerDisconnect);
        
        getLogger().at(Level.INFO).log("Event listeners registered");

        // Register commands
        // Disabled TestUICommand - dynamic UIs not supported
        // getCommandRegistry().registerCommand(new TestUICommand());
        getCommandRegistry().registerCommand(new ProfileCommand());
        getCommandRegistry().registerCommand(new StatsCommand(this));
        getCommandRegistry().registerCommand(new DiscordCommand());
        getCommandRegistry().registerCommand(new EntitySpawnCommand());
        getCommandRegistry().registerCommand(new NameplateTestCommand());
        getCommandRegistry().registerCommand(new TestDeathCommand(this));
        getCommandRegistry().registerCommand(new SkillsCommand(this));
        getCommandRegistry().registerCommand(new GiveSkillXpCommand(this));
        getCommandRegistry().registerCommand(new SetSkillLevelCommand(this));
        getCommandRegistry().registerCommand(new GiveSkillTokensCommand(this));
        getCommandRegistry().registerCommand(new TestMineOreCommand(this));
        
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
    
    public java.util.concurrent.ScheduledExecutorService getScheduler() {
        return playtimeScheduler;
    }
    
    private java.io.File getDataFolder() {
        java.io.File dataFolder = new java.io.File("plugins/VeilCore");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }
}
