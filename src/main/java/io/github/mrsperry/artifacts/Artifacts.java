package io.github.mrsperry.artifacts;

import com.google.common.collect.Lists;

import io.github.mrsperry.artifacts.modules.*;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class Artifacts extends JavaPlugin {
    /** The single instance of Artifacts */
    private static Artifacts instance;
    /** A set of all artifact instances */
    private static Set<Artifact> artifactInstances;

    @Override
    public void onEnable() {
        Artifacts.instance = this;
        Artifacts.artifactInstances = new HashSet<>();

        // Set default config values if a config isn't found
        this.saveDefaultConfig();
        // Read flag and artifact config values
        Config.initialize(this);

        // Collection of all artifact instances
        Artifacts.artifactInstances.addAll(Lists.newArrayList(
            new DeathTNT(),
            new Pestilence(),
            new SharedDeath(),
            new SharedDamage(),
            new NoSleep(),
            new Teleportitis(),
            new InstaKill(),
            new PlayerSwapper(),
            new KneeCracker(),
            new DepthMeter()
        ));

        // Register artifact events
        final PluginManager manager = this.getServer().getPluginManager();
        for (final Artifact artifact : Artifacts.artifactInstances) {
            if (artifact instanceof Listener) {
                manager.registerEvents((Listener) artifact, this);
            }
        }

        // Register commands
        final PluginCommand command = this.getCommand("artifacts");
        if (command != null) {
            command.setExecutor(new Commands());
        } else {
            this.getLogger().severe("Could not bing executor for the plugin's command!");
        }
    }

    /**
     * Gets the running instance of this
     * @return The current Artifacts instance
     */
    public static Artifacts getInstance() {
        return Artifacts.instance;
    }

    /**
     * @return A set of all artifact instances
     */
    public static Set<Artifact> getArtifactInstances() {
        return Artifacts.artifactInstances;
    }

    /**
     * Gets a random whole number between the provided minimum and maximum values (inclusive)
     * @param min The minimum number
     * @param max The maximum number
     * @return A random whole number between the min and max
     */
    public static int random(final int min, final int max) {
        return (int) (min + (Math.random() * ((max - min) + 1)));
    }
}
