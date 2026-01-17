package com.veilcore;

import java.util.logging.Level;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.veilcore.commands.ProfileCommand;
import com.veilcore.commands.TestUICommand;
import com.veilcore.profile.PlayerProfileManager;
import com.veilcore.profile.ProfileRepository;
import com.veilcore.profile.ProfileStateManager;

public class VeilCorePlugin extends JavaPlugin {
    private static VeilCorePlugin instance;
    private PlayerProfileManager profileManager;
    private ProfileStateManager stateManager;

    public VeilCorePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("VeilCore plugin loaded successfully!");

        // Initialize profile system
        ProfileRepository repository = new ProfileRepository(getDataFolder(), java.util.logging.Logger.getLogger("VeilCore"));
        profileManager = new PlayerProfileManager(repository, java.util.logging.Logger.getLogger("VeilCore"));
        stateManager = new ProfileStateManager(repository, java.util.logging.Logger.getLogger("VeilCore"));
        
        getLogger().at(Level.INFO).log("Profile system initialized");

        // Register commands
        getCommandRegistry().registerCommand(new TestUICommand());
        getCommandRegistry().registerCommand(new ProfileCommand());
        
        getLogger().at(Level.INFO).log("VeilCore fully loaded - use /profile to manage profiles");
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
