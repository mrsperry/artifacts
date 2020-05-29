package io.github.mrsperry.artifacts;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String cmdLine, final String[] args) {
        if (command.getName().equalsIgnoreCase("artifacts")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Not enough arguments");
                sender.sendMessage(ChatColor.RED + "Usage: /artifacts <list | enable | disable | randomize>");
                return true;
            } else if (args.length == 1) {
                final String arg = args[0].toLowerCase();

                switch (arg) {
                    case "list":
                        sender.sendMessage(this.getArtifactList());
                        return true;
                    case "randomize":
                        sender.sendMessage(ChatColor.GREEN + "Enabled " + this.randomizeEnabledArtifacts(-1) + " random artifacts!");
                        return true;
                    case "enable":
                        sender.sendMessage(ChatColor.RED + "Not enough arguments");
                        sender.sendMessage(ChatColor.RED + "Usage: /artifacts enable <all | artifact>");
                        return true;
                    case "disable":
                        sender.sendMessage(ChatColor.RED + "Not enough arguments");
                        sender.sendMessage(ChatColor.RED + "Usage: /artifacts disable <all | artifact>");
                        return true;
                    default:
                        sender.sendMessage(ChatColor.RED + "Invalid argument: " + arg);
                        sender.sendMessage(ChatColor.RED + "Usage: /artifacts <list | enable | disable | randomize>");
                        return true;
                }
            } else if (args.length == 2) {
                final String arg1 = args[0].toLowerCase();
                final String arg2 = args[1].toLowerCase();

                switch (arg1) {
                    case "randomize":
                        final int amount;
                        try {
                            amount = Integer.parseInt(arg2);
                        } catch (final NumberFormatException ex) {
                            sender.sendMessage(ChatColor.RED + "Invalid amount: " + arg2);
                            sender.sendMessage(ChatColor.RED + "Usage: /artifacts randomize [amount]");
                            return true;
                        }

                        if (amount <= 0) {
                            sender.sendMessage(ChatColor.RED + "Amount must be greater than zero: " + arg2);
                            sender.sendMessage(ChatColor.RED + "Usage: /artifacts randomize [amount]");
                            return true;
                        }

                        sender.sendMessage(ChatColor.GREEN + "Enabled " + this.randomizeEnabledArtifacts(amount) + " random artifacts!");
                        return true;
                    case "enable":
                        if (arg2.equals("all")) {
                            this.setAllArtifactEnable(true);
                            sender.sendMessage(ChatColor.GREEN + "All artifacts are now enabled.");
                        } else if (this.setArtifactEnable(arg2, true)) {
                            sender.sendMessage(ChatColor.GREEN + "Artifact successfully enabled: " + arg2);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Could not enable artifact: " + arg2);
                        }
                        return true;
                    case "disable":
                        if (arg2.equals("all")) {
                            this.setAllArtifactEnable(false);
                            sender.sendMessage(ChatColor.GREEN + "All artifacts are now disabled.");
                        } else if (this.setArtifactEnable(arg2, false)) {
                            sender.sendMessage(ChatColor.GREEN + "Artifact successfully disabled: " + arg2);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Could not disable artifact: " + arg2);
                        }
                        return true;
                    default:
                        sender.sendMessage(ChatColor.RED + "Invalid argument: " + arg1);
                        sender.sendMessage(ChatColor.RED + "Usage: /artifacts <list | enable | disable | randomize>");
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets a string containing all artifact names color coded to their enable state
     */
    private String getArtifactList() {
        final StringBuilder builder = new StringBuilder();

        // Add all artifact names
        for (final Artifact artifact : Artifacts.getArtifactInstances()) {
            builder.append(artifact.isEnabled() ? ChatColor.GREEN : ChatColor.RED)
                .append(artifact.getClass().getSimpleName())
                .append(ChatColor.GRAY)
                .append(", ");
        }

        // Add the header and append the artifact names
        return ChatColor.DARK_GRAY + "=======" + ChatColor.GRAY + " Artifacts " + ChatColor.DARK_GRAY + "======="
            + "\n" + builder.substring(0, builder.length() - 2);
    }

    /**
     * Disables all artifacts then randomly enables a number of them
     * @param amount The number of artifacts to enable; must be above zero and will be set the the number of artifacts available if the amount is higher
     * @return The number of artifacts that were enabled
     */
    private int randomizeEnabledArtifacts(int amount) {
        this.setAllArtifactEnable(false);

        final List<Artifact> artifacts = new ArrayList<>(Artifacts.getArtifactInstances());
        // Clamp the amount of artifacts that are enabled
        if (amount > artifacts.size() || amount == -1) {
            amount = artifacts.size();
        }

        // Enable the artifacts
        int enabled = 0;
        for (int index = 0; index < amount; index++) {
            final Artifact artifact = artifacts.remove(Artifacts.random(0, artifacts.size() - 1));
            artifact.setEnabled(true);
            Config.setArtifactEnable(artifact.getID(), true);
            enabled++;
        }

        return enabled;
    }

    /**
     * Enables or disables all available artifacts
     * @param enabled If all artifact should be enabled or disabled
     */
    private void setAllArtifactEnable(final boolean enabled) {
        for (final Artifact artifact : Artifacts.getArtifactInstances()) {
            artifact.setEnabled(enabled);
        }
    }

    /**
     * Sets an artifact's enabled state by its class name
     * @param artifactName The class name of the artifact
     * @param enabled If the artifact should be enabled or disabled
     * @return If the artifact enable state was modified
     */
    private boolean setArtifactEnable(final String artifactName, final boolean enabled) {
        for (final Artifact artifact : Artifacts.getArtifactInstances()) {
            if (artifact.getClass().getSimpleName().equalsIgnoreCase(artifactName)) {
                artifact.setEnabled(enabled);
                Config.setArtifactEnable(artifact.getID(), enabled);
                return true;
            }
        }

        return false;
    }
}
