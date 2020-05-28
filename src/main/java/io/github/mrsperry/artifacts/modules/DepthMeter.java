package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DepthMeter extends Artifact implements Listener {
    /** The level at which player's health starts to scale down */
    private final int baseLevel;

    public DepthMeter() {
        super("depth-meter");
        this.baseLevel = Config.getInt(this.id, "base-level", 64);
    }

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent event) {
        final Location destination = event.getTo();
        if (destination != null) {
            this.setHealth(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler
    private void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Location destination = event.getTo();
        if (destination != null) {
            this.setHealth(event.getPlayer(), event.getTo());
        }
    }

    /**
     * Sets the max health of a player based on their Y coordinate distance from the base level
     * @param player The moving player
     * @param location The player's location
     */
    private void setHealth(final Player player, final Location location) {
        final float y = location.getBlockY();
        // Ignore any Y level above the base level
        if (y >= this.baseLevel) {
            return;
        }

        // Get the max health of the player
        final AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            // Calculate the new max health
            int newMaxHealth = Math.max(Math.round(20 * (y / this.baseLevel)), 2);
            // Only set max health to full hearts
            if (newMaxHealth % 2 != 0) {
                newMaxHealth++;
            }

            // Get the current actual health value
            final double health = player.getHealth();
            // Set the new max health
            maxHealth.setBaseValue(newMaxHealth);
            // Lower the player's health to the new max if it is above the new max
            if (newMaxHealth < health) {
                player.setHealth(newMaxHealth);
            }
        }
    }
}
