package io.github.mrsperry.artifacts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.*;

public class Commands implements TabExecutor {
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (!command.getName().equalsIgnoreCase("artifacts")) {
            return false;
        }

        switch (args.length) {
            case 1:
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
            case 2:
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
                            sender.sendMessage(ChatColor.GREEN + "Artifact successfully enabled: " + args[1]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Could not enable artifact: " + args[1]);
                        }
                        return true;
                    case "disable":
                        if (arg2.equals("all")) {
                            this.setAllArtifactEnable(false);
                            sender.sendMessage(ChatColor.GREEN + "All artifacts are now disabled.");
                        } else if (this.setArtifactEnable(arg2, false)) {
                            sender.sendMessage(ChatColor.GREEN + "Artifact successfully disabled: " + args[1]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Could not disable artifact: " + args[1]);
                        }
                        return true;
                    default:
                        sender.sendMessage(ChatColor.RED + "Invalid argument: " + arg1);
                        sender.sendMessage(ChatColor.RED + "Usage: /artifacts <list | enable | disable | randomize>");
                        return true;
                }
            default:
                sender.sendMessage(ChatColor.RED + "Not enough arguments");
                sender.sendMessage(ChatColor.RED + "Usage: /artifacts <list | enable | disable | randomize>");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final ArrayList<String> options = new ArrayList<>();

        if (!command.getName().equalsIgnoreCase("artifacts")) {
            return options;
        }

        if (args.length == 1) {
            // Add default arguments
            StringUtil.copyPartialMatches(args[0], Arrays.asList("list", "enable", "disable", "randomize"), options);
        } else if (args.length == 2) {
            switch (args[0]) {
                case "enable":
                case "disable":
                    // Get each artifact's name
                    final Set<String> names = new HashSet<>();
                    for (final Artifact instance : Artifacts.getArtifactInstances()) {
                        names.add(instance.getClass().getSimpleName());
                    }
                    // Add special option for modifying all artifacts
                    names.add("all");

                    StringUtil.copyPartialMatches(args[1], names, options);
                    break;
                case "randomize":
                    StringUtil.copyPartialMatches(args[1], Collections.singletonList("[amount]"), options);
                    break;
            }
        }

        return options;
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
            Config.setArtifactEnable(artifact.getID(), enabled);
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
