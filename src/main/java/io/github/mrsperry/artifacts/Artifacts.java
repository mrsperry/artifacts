package io.github.mrsperry.artifacts;

import org.bukkit.plugin.java.JavaPlugin;

public class Artifacts extends JavaPlugin {
    /** The single instance of Artifacts */
    private static Artifacts instance;

    @Override
    public void onEnable() {
        Artifacts.instance = this;

        // Set default config values if a config isn't found
        this.saveDefaultConfig();
        // Read flag and artifact config values
        Config.initialize(this);
    }

    @Override
    public void onDisable() {

    }

    /**
     * Gets the running instance of this
     * @return The current Artifacts instance
     */
    public static Artifacts getInstance() {
        return Artifacts.instance;
    }
}
