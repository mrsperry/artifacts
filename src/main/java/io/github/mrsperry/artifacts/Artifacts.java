package io.github.mrsperry.artifacts;

import io.github.mrsperry.artifacts.modules.CraftingTimer;
import io.github.mrsperry.artifacts.modules.DeathTNT;

import org.bukkit.plugin.PluginManager;
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

        // Register artifact events
        final PluginManager manager = this.getServer().getPluginManager();
        manager.registerEvents(new DeathTNT(), this);
        manager.registerEvents(new CraftingTimer(), this);
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

    /**
     * Gets a random whole number between the provided minimum and maximum values (inclusive)
     * @param min The minimum number
     * @param max The maximum number
     * @return A random whole number between the min and max
     */
    public static int random(final int min, final int max) {
        return  (int) (min + (Math.random() * ((max - min) + 1)));
    }
}
