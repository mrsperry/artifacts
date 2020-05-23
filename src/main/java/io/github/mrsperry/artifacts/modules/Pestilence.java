package io.github.mrsperry.artifacts.modules;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.mcutils.types.CropTypes;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class Pestilence extends Artifact implements Listener {
    public Pestilence() {
        super("pestilence");
    }

    @EventHandler
    private void onBlockPlace(final BlockPlaceEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        final Material type = event.getBlockPlaced().getType();

        // Check if the block places is a plant type
        if (CropTypes.getAllTypes().contains(type)) {
            event.getPlayer().sendMessage(ChatColor.RED + "The land is infertile and nothing can be planted");
            event.setCancelled(true);
        }
    }
}
