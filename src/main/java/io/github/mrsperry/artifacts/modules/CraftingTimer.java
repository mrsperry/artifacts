package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Artifacts;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.ChatColor;
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
    //Max time to craft before dying
    private final int timeThreshold;
    private final String[] DEATH_MSGS = {
            "%player% failed to craft",
            "Crafting proved to be too much for %player%",
            "%player% has crafted themselves into the grave",
            "%player% needed just a bit more time"
    };
    //Players currently crafting
    private Map<UUID, BukkitRunnable> crafters;
    private List<UUID> deadCrafters;

    public CraftingTimer() {
        super("crafting-timer");
        this.timeThreshold = Config.getInt(this.id, "max-crafting-time", 10) * 20;
        this.crafters = new HashMap<>();
        this.deadCrafters = new ArrayList<>();
    }

    @EventHandler
    public void onCraftingTableOpen(InventoryOpenEvent event) {
        if(this.isEnabled()) {
            if(event.getInventory().getType().equals(InventoryType.WORKBENCH)) {
                Player player = (Player) event.getPlayer();

                //put player as crafting and start runnable
                crafters.put(player.getUniqueId(), new BukkitRunnable() {
                    private int elapsed = 0;
                    private int nextTick = 0;
                    @Override
                    public void run() {
                        if(elapsed >= timeThreshold) {
                            outOfTime(player);
                        } else {
                            //Handles the ticking sound, gets faster as time ticks down
                            nextTick = tick(player, elapsed, nextTick);
                        }
                        elapsed += 1;
                    }
                });
                crafters.get(player.getUniqueId()).runTaskTimer(Artifacts.getInstance(), 0, 1);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(this.isEnabled()) {
            if (event.getEntity().getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.CUSTOM)) {
                //Pick random death message and set it for a player
                String deathMsg = this.DEATH_MSGS[Artifacts.random(0, this.DEATH_MSGS.length-1)];
                deathMsg = deathMsg.replace("%player%", event.getEntity().getDisplayName());
                event.setDeathMessage(deathMsg);
            }
        }
    }

    @EventHandler
    public void onCraftingTableClose(InventoryCloseEvent event) {
        if(this.isEnabled()) {
            UUID uuid = event.getPlayer().getUniqueId();
            //If player was crafting, cancel runnable and remove from map
            if(crafters.containsKey(uuid)) {
                crafters.get(uuid).cancel();
                crafters.remove(uuid);

                //Special death scenario, killed on crafting inventory close
                if(deadCrafters.contains(uuid)) {
                    Player player = (Player) event.getPlayer();
                    player.getWorld().createExplosion(player.getLocation(), 0);
                    player.damage(player.getHealth());
                    deadCrafters.remove(player.getUniqueId());
                }
            }
        }
    }

    private void outOfTime(Player player) {
        int rand = Artifacts.random(1, 10);
        //10% chance to get special death scenario
        if(rand > 1) {
            //kill player
            player.getWorld().createExplosion(player.getLocation(), 0);
            player.damage(player.getHealth());
        } else {
            //add player to list so they die when they close crafting inventory
            player.sendMessage(ChatColor.DARK_PURPLE + "Ill leave this one up to you");
            this.deadCrafters.add(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
            crafters.get(player.getUniqueId()).cancel();
        }
    }

    //Ticking sound, max (a) ticks apart, min (b) ticks apart
    private int a = 12, b = 2;
    private int tick(Player player, int elapsed, int nextTick) {
        if(elapsed >= nextTick) {
            double perc = (double) elapsed / this.timeThreshold;
            player.playSound(player.getLocation(), Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, 0.5f, 2f);
            //lerp between max spacing and min spacing given time left
            return elapsed + (int)Math.floor(a + (perc * (b - a)));
        }
        return nextTick;
    }
}
