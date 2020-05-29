package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HotbarSwapper extends Artifact {
    public HotbarSwapper() {
        super("hotbar-swapper");

        // The number of seconds between each swap
        final int swapCooldown = Config.getInt(this.id, "swap-cooldown", 600) * 20;

        this.addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    final GameMode gameMode = player.getGameMode();
                    if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
                        continue;
                    }

                    final PlayerInventory inventory = player.getInventory();

                    // Get the items in the player's hotbar
                    final List<ItemStack> items = new ArrayList<>();
                    for (int index = 0; index < 9; index++) {
                        items.add(inventory.getItem(index));
                    }

                    // Shuffle the order and set the items
                    Collections.shuffle(items);
                    for (int index = 0; index < 9; index++) {
                        inventory.setItem(index, items.get(index));
                    }
                }
            }
        }, swapCooldown);
    }
}
