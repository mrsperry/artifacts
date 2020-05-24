package io.github.mrsperry.artifacts.modules;

import com.google.common.collect.Lists;

import io.github.mrsperry.artifacts.Artifact;
import io.github.mrsperry.artifacts.Artifacts;
import io.github.mrsperry.artifacts.Config;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class PlayerSwapper extends Artifact {
    public PlayerSwapper() {
        super("player-swapper");
        // The time between player swaps, in game ticks
        final int interval = Config.getInt(this.id, "interval", 600) * 20;

        this.addRunnable(new BukkitRunnable() {
            /** A list of all current online players that meet the criteria for swapping (not in a higher game mode) */
            private ArrayList<Player> allPlayers;

            @Override
            public void run() {
                // Update the list of online players
                this.allPlayers = Lists.newArrayList(Bukkit.getOnlinePlayers());
                // Remove the players the swapping shouldn't effect
                this.allPlayers.removeIf((player) -> {
                    final GameMode gameMode = player.getGameMode();
                    return gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
                });

                // Prevent any teleports from happening if only one player can be teleported
                if (this.allPlayers.size() <= 1) {
                    return;
                }

                // Match all players into swapping pairs
                final HashMap<Player, Player> pairs = this.pairPlayers();
                for (final Player player : pairs.keySet()) {
                    // Swap every player
                    this.swapPlayers(player, pairs.get(player));
                }
            }

            /**
             * Puts all current eligible players into pairs
             * <br><br>
             * If there is an odd number (>1) of players to swap, a special operation is performed.
             * A random player is selected from the available pairs and put into a pair with the last player.
             * The target of the random player is then put into a pair with the last player.
             * @return A map of all pairs of players that should swap positions
             */
            private HashMap<Player, Player> pairPlayers() {
                final ArrayList<Player> players = Lists.newArrayList(this.allPlayers);
                final HashMap<Player, Player> pairs = new HashMap<>();

                // Gets a random player and removes them from the list of available players
                final Supplier<Player> randomPlayer = () -> players.remove(Artifacts.random(0, players.size() - 1));

                // Create pairs until there are less than 2 players left
                while (players.size() > 1) {
                    pairs.put(randomPlayer.get(), randomPlayer.get());
                }

                // Special case if a player doesn't have a pair
                if (players.size() == 1) {
                    final Player lastPlayer = players.get(0);
                    // Get a random player to swap positions with from the available pairs
                    final Player key = Lists.newArrayList(pairs.keySet()).get(Artifacts.random(0, pairs.size() - 1));

                    // Have the target of the pair swap with the last player
                    pairs.put(pairs.get(key), lastPlayer);
                    // Have the last player swap with the original player of the pair
                    pairs.put(lastPlayer, key);
                }

                return pairs;
            }

            /**
             * Swaps two player's locations and velocity
             * @param player1 The first player to swap
             * @param player2 The second player to swap
             */
            private void swapPlayers(final Player player1, final Player player2) {
                final Location location1 = player1.getLocation();
                final Location location2 = player2.getLocation();

                final Vector velocity1 = player1.getVelocity();
                final Vector velocity2 = player2.getVelocity();

                // Teleport the players to each other's locations and set their velocity for a seamless swap
                player1.teleport(location2);
                player1.setVelocity(velocity2);
                player2.teleport(location1);
                player2.setVelocity(velocity1);

                // Spawn particle and sound effects at each location
                final World world1 = location1.getWorld();
                if (world1 != null) {
                    world1.playSound(location1, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
                    world1.spawnParticle(Particle.PORTAL, location1, 100);
                }
                final World world2 = location2.getWorld();
                if (world2 != null) {
                    world2.playSound(location2, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
                    world2.spawnParticle(Particle.PORTAL, location2, 100);
                }
            }
        }, interval);
    }
}
