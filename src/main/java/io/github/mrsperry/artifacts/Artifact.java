package io.github.mrsperry.artifacts;

public class Artifact {
    /** The current enable state of this artifact */
    private static boolean enabled;

    /**
     * Sets the initial enable state of this artifact
     * @param artifactName The config name of this artifact
     */
    protected Artifact(final String artifactName) {
        Artifact.enabled = Config.getBoolean(artifactName, "enabled", false);
    }

    /**
     * Sets the artifact's enable state
     * @param enabled The new enable state
     */
    public static void setEnabled(final boolean enabled) {
        Artifact.enabled = enabled;
    }

    /**
     * @return If this artifact is enabled
     */
    public static boolean isEnabled() {
        return Artifact.enabled;
    }
}
