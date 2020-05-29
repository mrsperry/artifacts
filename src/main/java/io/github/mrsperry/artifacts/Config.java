package io.github.mrsperry.artifacts;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

public class Config {
    /** The owning plugin of this class */
    private static JavaPlugin plugin;
    /**
     * A map containing all settings pertaining to each artifact.
     * <br><br>
     * Use the getter methods to access the settings.
     */
    private static final Map<String, Map<String, Object>> artifactSettings = new HashMap<>();

    /**
     * Reads flag and artifact settings from the provided plugin's configuration file
     * @param plugin The owning plugin
     */
    public static void initialize(final JavaPlugin plugin) {
        Config.plugin = plugin;

        final Logger logger = plugin.getLogger();
        final FileConfiguration config = plugin.getConfig();
        // Get the map of settings
        final ConfigurationSection settings = config.getConfigurationSection("settings");

        if (settings == null) {
            logger.warning("Could not find settings map");
            return;
        }

        logger.info("===== Settings =====");
        for (String artifact : settings.getKeys(false)) {
            logger.info(artifact + ":");

            // Get the map of this artifact's settings
            final ConfigurationSection artifactSettings = config.getConfigurationSection("settings." + artifact);
            if (artifactSettings == null) {
                logger.info("    < no settings found >");
                continue;
            }

            // Add all settings found in the config to a map
            final Map<String, Object> currentSettings = new HashMap<>();
            for (String name : artifactSettings.getKeys(false)) {
                final Object value = config.get("settings." + artifact + "." + name);
                logger.info("    " + name + ": " + value);

                currentSettings.put(name, value);
            }

            Config.artifactSettings.put(artifact, currentSettings);
        }
    }

    /**
     * Sets an artifact's enable or disable state in the config
     * @param id The artifact's ID
     * @param enabled If the artifact is enabled or disabled
     */
    public static void setArtifactEnable(final String id, final boolean enabled) {
        final JavaPlugin plugin = Config.plugin;
        plugin.getConfig().set("settings." + id + ".enabled", enabled);
        plugin.saveConfig();
    }

    /**
     * Gets a setting from the map of artifact settings
     * @param artifact The artifact the setting should come from
     * @param key The name of the setting
     * @return The artifact setting if it could be found, otherwise null
     */
    private static Object getArtifactSetting(final String artifact, final String key) {
        if (!Config.artifactSettings.containsKey(artifact)) {
            return null;
        }

        return Config.artifactSettings.get(artifact).getOrDefault(key, null);
    }

    /**
     * Gets a string artifact setting
     * @param artifact The artifact the setting should come from
     * @param key The name of the setting
     * @param defaultValue The default value to use if the setting could not be found
     * @return The string setting of the artifact or the default value if it could not be found
     */
    public static String getString(final String artifact, final String key, final String defaultValue) {
        final Object value = Config.getArtifactSetting(artifact, key);

        if (value instanceof String) {
            return value.toString();
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets a boolean artifact setting
     * @param artifact The artifact the setting should come from
     * @param key The name of the setting
     * @param defaultValue The default value to use if the setting could not be found
     * @return The boolean setting of the artifact or the default value if it could not be found
     */
    public static boolean getBoolean(final String artifact, final String key, final boolean defaultValue) {
        final Object value = Config.getArtifactSetting(artifact, key);

        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets a integer artifact setting
     * @param artifact The artifact the setting should come from
     * @param key The name of the setting
     * @param defaultValue The default value to use if the setting could not be found
     * @return The integer setting of the artifact or the default value if it could not be found
     */
    public static int getInt(final String artifact, final String key, final int defaultValue) {
        final Object value = Config.getArtifactSetting(artifact, key);

        if (value instanceof Integer) {
            return (int) value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets a float artifact setting
     * @param artifact The artifact the setting should come from
     * @param key The name of the setting
     * @param defaultValue The default value to use if the setting could not be found
     * @return The float setting of the artifact or the default value if it could not be found
     */
    public static float getFloat(final String artifact, final String key, final float defaultValue) {
        final Object value = Config.getArtifactSetting(artifact, key);

        if (value instanceof Float) {
            return (float) value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets a list of strings or objects represented as strings
     * @param artifact The artifact the setting should come from
     * @param key The name of the setting
     * @param defaultValue The default value to use if the setting could not be found
     * @return The list of strings setting of the artifact or the default value if it could not be found
     */
    public static List<String> getStringList(final String artifact, final String key, final List<String> defaultValue) {
        final Object value = Config.getArtifactSetting(artifact, key);

        // Make sure the value is a list
        if (value instanceof List) {
            final List<?> values = (List<?>) value;
            final List<String> strings = new ArrayList<>();

            // Convert each value into a string
            for (final Object index : values) {
                strings.add(index.toString());
            }

            return strings;
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets a list of materials
     * @param artifact The artifact the setting should come from
     * @param key The name of the setting
     * @param defaultValue The default value to use if the setting could not be found
     * @return The list of materials setting of the artifact or the default value if it could not be found
     */
    public static List<Material> getMaterialList(final String artifact, final String key, final List<Material> defaultValue) {
        final Object value = Config.getArtifactSetting(artifact, key);

        // Make sure the value is a list
        if (value instanceof List) {
            final List<?> values = (List<?>) value;
            final List<Material> materials = new ArrayList<>();

            for (final Object index : values) {
                // Convert string type to material enum name
                final String materialName = index.toString().toUpperCase().replaceAll(" ", "_");

                try {
                    materials.add(Material.valueOf(materialName));
                } catch (final IllegalArgumentException ex) {
                    Config.plugin.getLogger().severe("Could not parse material for '" + artifact + "' under '" + key + "': " + materialName);
                }
            }

            return materials;
        } else {
            return defaultValue;
        }
    }
}
