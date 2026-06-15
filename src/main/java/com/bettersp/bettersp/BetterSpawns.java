package com.bettersp.bettersp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterSpawns extends JavaPlugin {

    // CHANGE THIS to the exact name of your main world (not the spawn lobby world)
    private static final String MAIN_WORLD_NAME = "world";

    private SpawnFinder spawnFinder;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // creates config.yml in plugins/BetterSpawns/ if it doesn't exist

        spawnFinder = new SpawnFinder(getConfig());

        getServer().getPluginManager().registerEvents(
                new PlayerListener(spawnFinder, MAIN_WORLD_NAME), this
        );
        getLogger().info("Better Spawns enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Better Spawns disabled.");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (spawnFinder != null) {
            spawnFinder.loadUnsafeBlocks(getConfig());
            spawnFinder.loadSpawnRadius(getConfig());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("play")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player.");
                return true;
            }

            Player player = (Player) sender;
            World mainWorld = Bukkit.getWorld(MAIN_WORLD_NAME);

            if (mainWorld == null) {
                player.sendMessage("Main world is not available right now.");
                return true;
            }

            Location safeLoc = spawnFinder.findSafeLocation(mainWorld);
            player.teleport(safeLoc);

            String message = getConfig().getString("play-message", "Teleporting you to a random location...");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            return true;
        }

        if (label.equalsIgnoreCase("bsreload")) {
            reloadConfig();
            sender.sendMessage("Better Spawns config reloaded.");
            return true;
        }

        return false;
    }
}