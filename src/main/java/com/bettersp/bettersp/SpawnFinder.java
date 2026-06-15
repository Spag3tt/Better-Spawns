package com.bettersp.bettersp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SpawnFinder {

    private static final int MAX_ATTEMPTS = 50;
    private static final int DEFAULT_RADIUS = 625;

    private final Random random = new Random();
    private final Set<Material> unsafeGroundBlocks = new HashSet<>();
    private int worldRadius;

    public SpawnFinder(FileConfiguration config) {
        loadUnsafeBlocks(config);
        loadSpawnRadius(config);
    }

    public void loadUnsafeBlocks(FileConfiguration config) {
        unsafeGroundBlocks.clear();
        List<String> blockNames = config.getStringList("unsafe-ground-blocks");

        for (String name : blockNames) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                unsafeGroundBlocks.add(mat);
            }
        }
    }

    public void loadSpawnRadius(FileConfiguration config) {
        this.worldRadius = config.getInt("spawn-radius", DEFAULT_RADIUS);
    }

    public Location findSafeLocation(World world) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int x = random.nextInt(worldRadius * 2) - worldRadius;
            int z = random.nextInt(worldRadius * 2) - worldRadius;

            Location candidate = getSafeYAt(world, x, z);
            if (candidate != null) {
                return candidate;
            }
        }
        // Fallback: world spawn if no safe spot found
        return world.getSpawnLocation();
    }

    private Location getSafeYAt(World world, int x, int z) {
        int highestY = world.getHighestBlockYAt(x, z);

        // Reject if too low (likely ocean floor / cave opening) or too high
        if (highestY <= world.getSeaLevel() || highestY >= world.getMaxHeight() - 2) {
            return null;
        }

        Block ground = world.getBlockAt(x, highestY, z);
        Block feet = world.getBlockAt(x, highestY + 1, z);
        Block head = world.getBlockAt(x, highestY + 2, z);

        Material groundType = ground.getType();

        if (!ground.getType().isSolid() || unsafeGroundBlocks.contains(groundType)) {
            return null;
        }

        // Feet and head space must be air/passable
        if (feet.getType() != Material.AIR || head.getType() != Material.AIR) {
            return null;
        }

        return new Location(world, x + 0.5, highestY + 1, z + 0.5);
    }
}