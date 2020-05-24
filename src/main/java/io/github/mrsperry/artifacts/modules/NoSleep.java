package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.ArtifactFlags;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class NoSleep extends Artifact implements Listener {
    /** If an explosion should be created when the player tries to use a bed */
    private final boolean createExplosion;
    /** The power of the created explosion */
    private final float explosionPower;
    /** If the explosion should light nearby blocks on fire */
    private final boolean createFire;

    public NoSleep() {
        super("no-sleep");
        this.createExplosion = Config.getBoolean("no-sleep", "create-explosion", true);
        this.explosionPower = Config.getFloat("no-sleep", "explosion-power", 5);
        this.createFire = Config.getBoolean("no-sleep", "create-fire", true);
    }

    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        // Check the game mode of the player
        final GameMode gameMode = event.getPlayer().getGameMode();
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
            return;
        }

        // Only trigger on use of the bed
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Ensure the event doesn't fire twice (once for each hand)
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Ensure the block is a bed
        final Block block = event.getClickedBlock();
        if (block == null || !block.getType().toString().endsWith("_BED")) {
            return;
        }

        // Don't set the spawn point of the player
        event.setCancelled(true);

        // Create the explosion
        if (this.createExplosion) {
            block.getWorld().createExplosion(block.getLocation(), this.explosionPower, this.createFire, Config.isFlagEnabled(ArtifactFlags.EXPLOSION_DAMAGE));
        }
    }
}
