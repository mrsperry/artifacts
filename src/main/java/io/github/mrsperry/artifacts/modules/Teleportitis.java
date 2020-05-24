package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Config;

import io.github.mrsperry.mcutils.LocationUtils;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Teleportitis extends Artifact {
    public Teleportitis() {
        super("teleportitis");
        // How many seconds should pass before teleporting players
        final int teleportInterval = Config.getInt(this.id, "teleport-interval", 600) * 20;
        // How many blocks a player can be teleported on each axis
        final int teleportRadius = Config.getInt(this.id, "teleport-radius", 50);
        // How many times the teleport should look for a valid location (higher will take more time, lower will result in more failures)
        final int teleportTries = Config.getInt(this.id, "teleport-tries", 100);

        this.addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                for (final World world : Bukkit.getWorlds()) {
                    for (final Player player : world.getEntitiesByClass(Player.class)) {
                        final GameMode gameMode = player.getGameMode();
                        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
                            continue;
                        }

                        // Get the original position
                        final Location origin = player.getLocation();
                        // Get the new position
                        final Location location = LocationUtils.getRandomSafeLocation(origin, teleportRadius, teleportTries);

                        // Cancel if a new position could not be found
                        if (location == null) {
                            continue;
                        }

                        // Spawn effects at the original position
                        world.playSound(origin, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
                        world.spawnParticle(Particle.PORTAL, origin, 100);

                        // Teleport the player
                        player.teleport(location);
                        player.sendMessage(ChatColor.DARK_PURPLE + "Your surroundings suddenly seem different...");

                        // Spawn effects at the new position
                        world.playSound(location, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
                        world.spawnParticle(Particle.PORTAL, location, 100);
                    }
                }
            }
        }, teleportInterval);
    }
}
