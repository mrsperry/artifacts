package io.github.mrsperry.artifacts;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Artifact {
    /** The config name of this artifact */
    protected String id;
    /** The current enable state of this artifact */
    private boolean enabled;
    /** All runnables handled by this artifact */
    private Map<BukkitRunnable, Integer> runnables;

    /**
     * Sets the initial enable state of this artifact
     * @param id The config name of this artifact
     */
    protected Artifact(final String id) {
        this.id = id;
        this.enabled = Config.getBoolean(id, "enabled", false);
        this.runnables = new HashMap<>();
    }

    /**
     * @return The name used to get values of this artifact from the config
     */
    public String getID() {
        return this.id;
    }

    /**
     * Sets the artifact's enable state and starts or stops any handled runnables
     * @param enabled The new enable state
     */
    public void setEnabled(final boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        for (final BukkitRunnable runnable : this.runnables.keySet()) {
            if (enabled) {
                final int interval = this.runnables.get(runnable);
                runnable.runTaskTimer(Artifacts.getInstance(), interval, interval);
            } else {
                Bukkit.getScheduler().cancelTask(runnable.getTaskId());
            }
        }
    }

    /**
     * @return If this artifact is enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Adds a new runnable to be handled by this artifact
     * <br><br>
     * Runnables will be started or stopped whenever the artifact is enabled or disabled
     * @param runnable The runnable to be added
     * @param interval The interval at which the runnable should be run
     */
    public void addRunnable(final BukkitRunnable runnable, final int interval) {
        this.runnables.put(runnable, interval);

        if (this.isEnabled()) {
            runnable.runTaskTimer(Artifacts.getInstance(), interval, interval);
        }
    }
}
