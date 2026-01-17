package com.veilcore;

import java.util.logging.Level;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.veilcore.commands.TestUICommand;

public class VeilCorePlugin extends JavaPlugin {
    private static VeilCorePlugin instance;

    public VeilCorePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("VeilCore plugin loaded successfully!");

        getCommandRegistry().registerCommand(new TestUICommand());
    }

    public static VeilCorePlugin getInstance() {
        return instance;
    }
}
