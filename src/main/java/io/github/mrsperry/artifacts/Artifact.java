package io.github.mrsperry.artifacts;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Artifact {
    /** The current enable state of this artifact */
    private static boolean enabled;
    /** A map of any runnables and their timers this artifact handles */
    private static final Map<BukkitRunnable, Integer> runnables = new HashMap<>();

    /**
     * Sets the initial enable state of this artifact
     * @param artifactName The config name of this artifact
     */
    protected Artifact(final String artifactName) {
        Artifact.enabled = Config.getBoolean(artifactName, "enabled", false);
    }

    /**
     * Sets the artifact's enable state and starts or stops any handled runnables
     * @param enabled The new enable state
     */
    public static void setEnabled(final boolean enabled) {
        if (Artifact.enabled == enabled) {
            return;
        }

        Artifact.enabled = enabled;

        for (final BukkitRunnable runnable : Artifact.runnables.keySet()) {
            if (enabled) {
                final int interval = Artifact.runnables.get(runnable);
                runnable.runTaskTimer(Artifacts.getInstance(), interval, interval);
            } else {
                Bukkit.getScheduler().cancelTask(runnable.getTaskId());
            }
        }
    }

    /**
     * @return If this artifact is enabled
     */
    public static boolean isEnabled() {
        return Artifact.enabled;
    }

    /**
     * Adds a new runnable to be handled by this artifact
     * <br><br>
     * Runnables will be started or stopped whenever the artifact is enabled or disabled
     * @param runnable The runnable to be added
     * @param interval The interval at which the runnable should be run
     */
    public static void addRunnable(final BukkitRunnable runnable, final int interval) {
        Artifact.runnables.put(runnable, interval);

        if (Artifact.isEnabled()) {
            runnable.runTaskTimer(Artifacts.getInstance(), interval, interval);
        }
    }
}
