package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class InstaKill extends Artifact implements Listener {
    public InstaKill() {
        super("insta-kill");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onEntityDamage(final EntityDamageEvent event) {
        if (!InstaKill.isEnabled()) {
            return;
        }

        final Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            // Add the final damage since setting health to zero will cause a double death event to occur
            ((LivingEntity) entity).setHealth(0 + event.getFinalDamage());
        }
    }
}
