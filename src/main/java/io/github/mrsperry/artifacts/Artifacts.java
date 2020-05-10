package io.github.mrsperry.artifacts;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Artifacts extends JavaPlugin {
    /** The single instance of Artifacts */
    private static Artifacts instance;
    /**
     * A set of global artifact settings represented as true/false flags.
     * <br><br>
     * Only enabled flags will be put into this set; disabled flags are ignored.
     * <br><br>
     * Use {@code Artifacts.isFlagEnabled()} to check if a flag is currently enabled.
     */
    private static Set<ArtifactFlags> flags;

    @Override
    public void onEnable() {
        Artifacts.instance = this;
        Artifacts.flags = new HashSet<>();

        // Set default config values if a config isn't found
        this.saveDefaultConfig();

        final Logger logger = this.getLogger();
        final FileConfiguration config = this.getConfig();
        // Get the list of flags
        final ConfigurationSection flags = config.getConfigurationSection("flags");

        if (flags == null) {
            logger.warning("Could not find flags list");
        } else {
            logger.info("===== Flags =====");
            for (String name : flags.getKeys(false)) {
                // Check if this flag is enabled; if it isn't found then assume it should be disabled
                final boolean enabled = config.getBoolean("flags." + name, false);
                logger.info(name + ": " + enabled);

                // Don't add disabled flags
                if (enabled) {
                    final ArtifactFlags flag;
                    try {
                        // Convert config name to enum name
                        flag = ArtifactFlags.valueOf(name.toUpperCase().replace("-", "_"));
                    } catch (final IllegalArgumentException ex) {
                        logger.severe("Could not find flag: " + name);
                        continue;
                    }

                    Artifacts.flags.add(flag);
                }
            }
        }
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
     * Checks if a flag has been enabled.
     * @param flag The flag to check
     * @return If the flag is enabled
     */
    public static boolean isFlagEnabled(final ArtifactFlags flag) {
        return Artifacts.flags.contains(flag);
    }
}
