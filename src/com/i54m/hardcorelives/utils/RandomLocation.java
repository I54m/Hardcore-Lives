package com.i54m.hardcorelives.utils;


import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.managers.WorkerManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

public class RandomLocation implements Callable<Location> {

    private final Main plugin = Main.getInstance();
    private final HashMap<String, Integer> cachedHighestY = new HashMap<>();

    @Override
    public Location call() throws Exception {
        return getRandomLocation();
    }

    private Location getRandomLocation() throws InterruptedException {
        Location location = getRandomLocationUnSafe();

        if (!plugin.isSafeLocationRespawn(location)) {
            double x = location.getBlockX() + 0.5;
            double z = location.getBlockZ() + 0.5;
            //if location not safe check downwards until we find a safe one
            for (double y = location.getBlockY(); y > 0; y--) {
                Location feet = new Location(location.getWorld(), x, y, z);
                if (plugin.isSafeLocation(feet))
                    return feet;
            }
            //if downwards location check failed then check in a 20x20 radius for a safe location
            for (int x2 = location.getBlockX() - 10; x2 <= location.getBlockX() + 10; x2++) {
                for (int z2 = location.getBlockZ() - 10; z2 <= location.getBlockZ() + 10; z2++) {
                    Location feet = new Location(location.getWorld(), x2, getHighestY(location.getWorld(), x2, z2), z2);
                    feet.add(0.5, 1, 0.5);
                    if (plugin.isSafeLocation(feet)) return feet;
                }
            }
            Thread.sleep(500);
            return getRandomLocation();
        } else {
            generateNearbyChunks(location);
            return location;
        }
    }

    private void generateNearbyChunks(Location location) {
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> {
            World world = location.getWorld();
            if (world == null) world = Bukkit.getWorlds().get(0);
            Chunk centerChunk = location.getChunk();
            int x = location.getBlockX();
            int z = location.getBlockZ();
            ArrayList<Chunk> chunks = new ArrayList<>();
            chunks.add(world.getChunkAt(x + 16, z - 16));
            chunks.add(world.getChunkAt(x + 16, z));
            chunks.add(world.getChunkAt(x + 16, z + 16));
            chunks.add(world.getChunkAt(x + 16, z));
            chunks.add(centerChunk);
            chunks.add(world.getChunkAt(x - 16, z));
            chunks.add(world.getChunkAt(x - 16, z + 16));
            chunks.add(world.getChunkAt(x - 16, z));
            chunks.add(world.getChunkAt(x - 16, z + 16));
            for (Chunk chunk : chunks) {
                if (!chunk.isLoaded())
                    plugin.getServer().getScheduler().callSyncMethod(plugin, () -> chunk.load(true));
            }
        }));
    }

    private Location getRandomLocationUnSafe() {
        World world = plugin.getServer().getWorld("world");
        if (world == null) world = plugin.getServer().getWorlds().get(0);
        Random rndGen = new Random();
        int r = rndGen.nextInt(plugin.getMaxDistance());
        int x = rndGen.nextInt(r);
        int z = (int) Math.sqrt(Math.pow(r, 2) - Math.pow(x, 2));
        if (rndGen.nextBoolean()) x *= -1;
        if (rndGen.nextBoolean()) z *= -1;
        Location randomLoc = new Location(world, x, getHighestY(world, x, z), z);
        return randomLoc.add(0.5, 1, 0.5);
    }

    private int getHighestY(World world, double x, double z) {
        if (cachedHighestY.containsKey(world.toString() + ":" + x + ":" + z))
            return cachedHighestY.get(world.toString() + ":" + x + ":" + z);
        int i = 255;
        while (i > 0) {
            Block block = new Location(world, x, i, z).getBlock();
            if (!block.isEmpty() && !block.isLiquid() && !block.isPassable()) {
                cachedHighestY.put(world.toString() + ":" + x + ":" + z, i);
                return i;
            }
            i--;
        }
        return i;
    }
}
