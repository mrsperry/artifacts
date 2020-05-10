package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.ArtifactFlags;
import io.github.mrsperry.artifacts.Artifacts;
import io.github.mrsperry.artifacts.Config;

import io.github.mrsperry.mcutils.types.EntityTypes;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DeathTNT extends Artifact implements Listener {
    /** The minimum number of ticks before the TNT explodes */
    private final int minFuseTicks;
    /** The maximum number of ticks before the TNT explodes */
    private final int maxFuseTicks;

    /**
     * Initializes config values for DeathTNT
     */
    public DeathTNT() {
        super("death-tnt");
        this.minFuseTicks = Config.getInt("death-tnt", "min-fuse-ticks", 60);
        this.maxFuseTicks = Config.getInt("death-tnt", "max-fuse-ticks", 100);
    }

    @EventHandler
    private void onEntityDeath(final EntityDeathEvent event) {
        // Check if this artifact is enabled
        if (DeathTNT.isEnabled()) {
            return;
        }

        final LivingEntity entity = event.getEntity();
        final World world = event.getEntity().getWorld();

        // Check if the entity is a hostile monster
        if (EntityTypes.getHostileTypes().contains(entity.getType())) {
            // Spawn TNT where the entity was killed
            final TNTPrimed tnt = (TNTPrimed) world.spawnEntity(entity.getLocation(), EntityType.PRIMED_TNT);
            // Set the fuse timer to a random value between the min and max ticks
            tnt.setFuseTicks(Artifacts.random(this.minFuseTicks, this.maxFuseTicks));

            // Check if the explosion can do block damage (cast to a byte as persistent data can't hold booleans)
            final byte canDoDamage = (byte) (Config.isFlagEnabled(ArtifactFlags.EXPLOSION_DAMAGE) ? 0 : 1);
            // Set the persistent data
            final PersistentDataContainer data = tnt.getPersistentDataContainer();
            data.set(new NamespacedKey(Artifacts.getInstance(), "tnt"), PersistentDataType.BYTE, canDoDamage);
        }
    }

    @EventHandler
    private void onEntityExplode(final EntityExplodeEvent event) {
        final Entity entity = event.getEntity();

        // Ignore any entity that isn't TNT
        if (!(entity instanceof TNTPrimed)) {
            return;
        }

        // Get the TNT's persistent data
        final PersistentDataContainer data = entity.getPersistentDataContainer();
        final Byte canDoDamage = data.get(new NamespacedKey(Artifacts.getInstance(), "tnt"), PersistentDataType.BYTE);
        // Check if this TNT can do block damage
        if (canDoDamage != null && canDoDamage == 1) {
            // If the TNT can't do block damage, clear the list of affected blocks
            event.blockList().clear();
        }
    }
}
