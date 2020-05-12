package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Artifacts;
import io.github.mrsperry.artifacts.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class HotbarSwapper extends Artifact {
    private static final String ARTIFACT_ID = "hotbar-swapper";
    private final int coolDown;
    private final BukkitRunnable swappingRunnable;

    public HotbarSwapper() {
        super(ARTIFACT_ID);

        //Translate minutes into ticks.
        coolDown = Config.getInt(ARTIFACT_ID, "swap-cooldown", 10) * 60 * 20;

        //runnable to swap items on the hotbar
        swappingRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    mixHotBar(player);
                }
            }

            //randomly selects two slots and swaps the items, 15 times;
            private void mixHotBar(Player player) {
                for(int i = 0; i < 15; i++) {
                    Inventory inventory = player.getInventory();
                    int slot1 = Artifacts.random(0, 8);
                    int  slot2 = Artifacts.random(0, 8);
                    ItemStack temp = inventory.getItem(slot1);
                    inventory.setItem(slot1, inventory.getItem(slot2));
                    inventory.setItem(slot2, temp);
                }
            }
        };

        HotbarSwapper.addRunnable(swappingRunnable, coolDown);
    }
}
