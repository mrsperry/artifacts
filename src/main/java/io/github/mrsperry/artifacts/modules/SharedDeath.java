package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Artifacts;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SharedDeath extends Artifact implements Listener {
    public SharedDeath() {
        super("shared-death");
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        // Key to be applied to players after death
        final NamespacedKey key = new NamespacedKey(Artifacts.getInstance(), "shared-death");

        // Find each player on each world
        for (final World world : Bukkit.getWorlds()) {
            for (final Player player : world.getEntitiesByClass(Player.class)) {
                final GameMode gameMode = player.getGameMode();
                if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
                    continue;
                }

                final PersistentDataContainer data = player.getPersistentDataContainer();

                // Don't kill any player that has already been killed
                if (data.has(key, PersistentDataType.STRING)) {
                    data.remove(key);
                    continue;
                }

                // Don't kill the player who was originally killed
                if (player == event.getEntity()) {
                    continue;
                }

                // Add metadata to not kill players infinitely
                data.set(key, PersistentDataType.STRING, "shared-death");
                // Kill the player
                player.setHealth(0);
                player.playEffect(EntityEffect.HURT);
            }
        }
    }
}
