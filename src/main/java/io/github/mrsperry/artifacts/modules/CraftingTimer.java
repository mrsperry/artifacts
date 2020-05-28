package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Artifacts;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CraftingTimer extends Artifact implements Listener {
    /** Custom death messages to use when a player dies while crafting */
    private final String[] deathMessages = {
        "%player% failed to craft",
        "Crafting proved to be too much for %player%",
        "%player% has crafted themselves into the grave",
        "%player% needed just a bit more time"
    };

    /** Number of seconds before a player dies while crafting */
    private final int maxCraftingTime;
    /** A map of all players currently crafting */
    private final Map<UUID, BukkitRunnable> crafters;
    private final List<UUID> deadCrafters;

    public CraftingTimer() {
        super("crafting-timer");
        this.maxCraftingTime = Config.getInt(this.id, "max-crafting-time", 10) * 20;
        this.crafters = new HashMap<>();
        this.deadCrafters = new ArrayList<>();
    }

    @EventHandler
    private void onCraftingTableOpen(final InventoryOpenEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (event.getInventory().getType() == InventoryType.WORKBENCH) {
            final Player player = (Player) event.getPlayer();
            final UUID id = player.getUniqueId();

            //put player as crafting and start runnable
            this.crafters.put(id, new BukkitRunnable() {
                private int elapsedTime = 0;
                private int nextTick = 0;

                @Override
                public void run() {
                    if(this.elapsedTime >= maxCraftingTime) {
                        outOfTime(player);
                    } else {
                        //Handles the ticking sound, gets faster as time ticks down
                        this.nextTick = tick(player, this.elapsedTime, this.nextTick);
                    }

                    this.elapsedTime += 1;
                }
            });

            this.crafters.get(id).runTaskTimer(Artifacts.getInstance(), 0, 1);
        }
    }

    @EventHandler
    private void onPlayerDeath(final PlayerDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        final Player player = event.getEntity();
        final EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage != null && lastDamage.getCause() == EntityDamageEvent.DamageCause.CUSTOM) {
            // Pick random death message
            final String message = this.deathMessages[Artifacts.random(0, this.deathMessages.length - 1)];
            // Insert the player's name into the message
            event.setDeathMessage(message.replace("%player%", player.getDisplayName()));
        }
    }

    @EventHandler
    private void onCraftingTableClose(final InventoryCloseEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        final UUID uuid = event.getPlayer().getUniqueId();
        // If player was crafting, cancel runnable and remove from map
        if (this.crafters.containsKey(uuid)) {
            this.crafters.get(uuid).cancel();
            this.crafters.remove(uuid);

            // Special death scenario, killed on crafting inventory close
            if (this.deadCrafters.contains(uuid)) {
                final Player player = (Player) event.getPlayer();
                player.getWorld().createExplosion(player.getLocation(), 0);
                player.damage(player.getHealth());
                this.deadCrafters.remove(player.getUniqueId());
            }
        }
    }

    private void outOfTime(final Player player) {
        final Location location = player.getLocation();

        // 10% chance to get special death scenario
        if (Artifacts.random(1, 10) > 1) {
            // Kill the player with an explosion that does no damage
            player.getWorld().createExplosion(location, 0);
            player.damage(player.getHealth());
        } else {
            // Add player to list so they die when they close crafting inventory
            player.sendMessage(ChatColor.DARK_PURPLE + "Ill leave this one up to you");
            player.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);

            final UUID id = player.getUniqueId();
            this.deadCrafters.add(id);
            this.crafters.get(id).cancel();
        }
    }

    // Ticking sound, max (a) ticks apart, min (b) ticks apart
    private final int max = 12, min = 2;
    private int tick(final Player player, final int elapsed, final int nextTick) {
        if (elapsed >= nextTick) {
            final double percent = (double) elapsed / this.maxCraftingTime;
            player.playSound(player.getLocation(), Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, 0.5f, 2f);

            // Lerp between max spacing and min spacing given time left
            return elapsed + (int) Math.floor(this.max + (percent * (this.min - this.max)));
        }

        return nextTick;
    }
}
