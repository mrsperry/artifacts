package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class KneeCracker extends Artifact implements Listener {
    private final float damageMultiplier;

    public KneeCracker() {
        super("knee-cracker");
        this.damageMultiplier = Config.getFloat(this.id, "damage-multiplier", 1.5f);
    }

    @EventHandler
    private void onEntityDamage(final EntityDamageEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(Math.round(event.getDamage() * this.damageMultiplier));
        }
    }
}
