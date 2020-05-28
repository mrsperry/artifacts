package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Config;

import io.github.mrsperry.mcutils.RandomItems;
import io.github.mrsperry.mcutils.types.EntityTypes;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RandomDrops extends Artifact implements Listener {
    /** If animals should have randomized drops */
    private final boolean animalDrops;
    /** If monsters should have randomized drops */
    private final boolean monsterDrops;
    /** If blocks should have randomized drops */
    private final boolean blockDrops;
    /** A list of all materials that cannot be dropped */
    private final List<Material> blacklist;

    public RandomDrops() {
        super("random-drops");
        this.animalDrops = Config.getBoolean(this.id, "animal-drops", false);
        this.monsterDrops = Config.getBoolean(this.id, "monster-drops", false);
        this.blockDrops = Config.getBoolean(this.id, "block-drops", false);
        this.blacklist = Config.getMaterialList(this.id, "blacklist", new ArrayList<>());
    }

    @EventHandler
    private void onEntityDeath(final EntityDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        // Check if this entity type has randomized drops enabled
        final EntityType entityType = event.getEntityType();
        if (!this.animalDrops && EntityTypes.getNeutralTypes().contains(entityType)) {
            return;
        }

        if (!this.monsterDrops && EntityTypes.getHostileTypes().contains(entityType)) {
            return;
        }

        // Get the list of drops
        final List<ItemStack> drops = event.getDrops();
        // Create a new list for randomized drops
        final List<ItemStack> newDrops = new ArrayList<>();

        // Add one randomized drop for every regular drop
        for (int index = 0; index < drops.size(); index++) {
            final ItemStack item = RandomItems.getRandomItem();
            final Material type = item.getType();

            // Make sure blacklisted materials are not dropped
            if (this.blacklist.contains(type)) {
                continue;
            }

            newDrops.add(item);
        }

        // Clear the original drops and add the randomized drops
        drops.clear();
        drops.addAll(newDrops);
    }

    @EventHandler
    private void onBlockBreak(final BlockBreakEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        // Check if randomized block drops are enabled
        if (!this.blockDrops) {
            return;
        }

        final GameMode gameMode = event.getPlayer().getGameMode();
        if (gameMode == GameMode.CREATIVE) {
            return;
        }

        final Block block = event.getBlock();
        // Make sure there is a drop to replace
        if (block.getDrops().size() > 0) {
            return;
        }

        final Location location = block.getLocation();
        final World world = location.getWorld();
        if (world != null) {
            // Cancel the event as you cannot replace drops
            event.setCancelled(true);
            // Emulate the block breaking
            block.setType(Material.AIR);

            // Get a non-blacklisted item
            ItemStack item;
            do {
                item = RandomItems.getRandomItem();
            } while (this.blacklist.contains(item.getType()));

            world.dropItemNaturally(location, item);
        }
    }
}
