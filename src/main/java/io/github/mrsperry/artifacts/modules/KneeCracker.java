package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class KneeCracker extends Artifact implements Listener {
    /** If the damage multiplier should be applied only to players */
    private final boolean onlyPlayers;
    /** Multiplier used when applying any fall damage to an entity */
    private final float damageMultiplier;

    public KneeCracker() {
        super("knee-cracker");
        this.onlyPlayers = Config.getBoolean(this.id, "only-players", true);
        this.damageMultiplier = Config.getFloat(this.id, "damage-multiplier", 1.5f);
    }

    @EventHandler
    private void onEntityDamage(final EntityDamageEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (this.onlyPlayers && event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(Math.round(event.getDamage() * this.damageMultiplier));
        }
    }
}
