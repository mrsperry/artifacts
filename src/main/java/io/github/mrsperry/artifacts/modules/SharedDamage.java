package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class SharedDamage extends Artifact implements Listener {
    public SharedDamage() {
        super("shared-damage");
    }

    @EventHandler (priority = EventPriority.LOWEST)
    private void onEntityDamage(final EntityDamageEvent event) {
        if (!SharedDamage.isEnabled()) {
            return;
        }

        final Entity entity = event.getEntity();
        if (entity.getType() != EntityType.PLAYER) {
            return;
        }

        // Loop through each player in each world
        for (final World world : Bukkit.getWorlds()) {
            for (final Player player : world.getEntitiesByClass(Player.class)) {
                // Ignore this player if they are in a higher game mode
                final GameMode gameMode = player.getGameMode();
                if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
                    continue;
                }

                // Don't damage the player who initially took the damage
                if (entity == player) {
                    continue;
                }

                // Calculate the final health of the player (can't use the damage method because of infinite event calls)
                final double health = player.getHealth() - event.getFinalDamage();
                // Share the damage to the player (clamp the value to 0-20)
                player.setHealth(health < 0 ? 0 : health > 20 ? 20 : health);
                player.playEffect(EntityEffect.HURT);
            }
        }
    }
}
