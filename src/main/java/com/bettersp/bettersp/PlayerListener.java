package com.bettersp.bettersp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final SpawnFinder spawnFinder;
    private final String mainWorldName;

    public PlayerListener(SpawnFinder spawnFinder, String mainWorldName) {
        this.spawnFinder = spawnFinder;
        this.mainWorldName = mainWorldName;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only assign a random spawn the FIRST time a player joins
        if (!player.hasPlayedBefore()) {
            World mainWorld = Bukkit.getWorld(mainWorldName);
            if (mainWorld == null) return;

            Location safeLoc = spawnFinder.findSafeLocation(mainWorld);
            player.teleport(safeLoc);
            // NOTE: do NOT call setRespawnLocation here — leave their bed spawn unset
            // so PlayerRespawnEvent correctly falls back to a random location on death.
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Location respawnLoc = event.getRespawnLocation();
        World respawnWorld = respawnLoc.getWorld();

        // Check if this respawn location is a "default" world spawn (no bed/anchor set)
        // by comparing against the world's actual spawn point.
        if (respawnWorld != null) {
            Location worldSpawn = respawnWorld.getSpawnLocation();

            boolean isDefaultSpawn =
                    respawnLoc.getBlockX() == worldSpawn.getBlockX()
                            && respawnLoc.getBlockZ() == worldSpawn.getBlockZ();

            if (isDefaultSpawn) {
                World mainWorld = Bukkit.getWorld(mainWorldName);
                if (mainWorld == null) return;

                Location safeLoc = spawnFinder.findSafeLocation(mainWorld);
                event.setRespawnLocation(safeLoc);
            }
        }
    }
}