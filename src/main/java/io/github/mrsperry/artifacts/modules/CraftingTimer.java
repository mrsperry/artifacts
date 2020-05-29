package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Artifacts;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CraftingTimer extends Artifact implements Listener {
    /** Number of seconds before a player dies while crafting */
    private final int maxCraftingTime;
    /** Custom death messages to use when a player dies while crafting */
    private final List<String> deathMessages;
    /** A map of all players currently crafting */
    private final Map<UUID, BukkitRunnable> crafters;

    public CraftingTimer() {
        super("crafting-timer");
        this.deathMessages = Config.getStringList(this.id, "death-messages", new ArrayList<>());
        this.maxCraftingTime = Config.getInt(this.id, "max-crafting-time", 10) * 20;
        this.crafters = new HashMap<>();
    }

    @EventHandler
    private void onCraftingTableOpen(final InventoryOpenEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        final Player player = (Player) event.getPlayer();
        final GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
            return;
        }

        if (event.getInventory().getType() == InventoryType.WORKBENCH) {
            final UUID id = player.getUniqueId();
            final int maxCraftingTime = this.maxCraftingTime;

            this.crafters.put(id, new BukkitRunnable() {
                /** The amount of ticks that have passed since this runnable has started */
                private int elapsedTime = 0;
                /** The target number of ticks used to check when a new ticking sound should be played */
                private int nextTick = 0;

                @Override
                public void run() {
                    final Location location = player.getLocation();

                    if (this.elapsedTime >= maxCraftingTime) {
                        // Mark this player as crafting; used to check if a custom death message should be displayed
                        final PersistentDataContainer container = player.getPersistentDataContainer();
                        container.set(new NamespacedKey(Artifacts.getInstance(), "crafting-timer"), PersistentDataType.BYTE, (byte) 0);

                        // Kill the player with an explosion that does no damage
                        player.getWorld().createExplosion(location, 0);
                        player.damage(player.getHealth());
                    } else {
                        if (this.elapsedTime >= this.nextTick) {
                            player.playSound(location, Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, 0.5f, 2f);

                            // Get the percentage to use for lerping
                            final double percent = (double) this.elapsedTime / maxCraftingTime;
                            // Lerp between max spacing (12) and min spacing (-10) given time left
                            this.nextTick = this.elapsedTime + (int) Math.floor(12 + (percent * -10));
                        }
                    }

                    this.elapsedTime += 1;
                }
            });

            // Run the crafting runnable every tick
            this.crafters.get(id).runTaskTimer(Artifacts.getInstance(), 0, 1);
        }
    }

    @EventHandler
    private void onPlayerDeath(final PlayerDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        // Make sure there are custom messages to use
        if (this.deathMessages.size() == 0) {
            return;
        }

        final Player player = event.getEntity();

        // Check if this player is marked for a custom death message
        final PersistentDataContainer container = player.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(Artifacts.getInstance(), "crafting-timer");
        if (!container.has(key, PersistentDataType.BYTE)) {
            return;
        }

        final EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage != null && lastDamage.getCause() == EntityDamageEvent.DamageCause.CUSTOM) {
            // Set a random death message
            event.setDeathMessage(" " + this.deathMessages.get(Artifacts.random(0, this.deathMessages.size() - 1)));

            // Remove the mark for a custom death message
            container.remove(key);
        }
    }

    @EventHandler
    private void onCraftingTableClose(final InventoryCloseEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        final UUID id = event.getPlayer().getUniqueId();
        // If player was crafting, cancel runnable and remove from map
        if (this.crafters.containsKey(id)) {
            this.crafters.get(id).cancel();
            this.crafters.remove(id);
        }
    }
}
