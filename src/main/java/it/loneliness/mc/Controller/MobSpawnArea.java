package it.loneliness.mc.Controller;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import it.loneliness.mc.Model.LogHandler;

public class MobSpawnArea {    
    private static MobSpawnArea thisHandler;

    public static MobSpawnArea getMobSpawnArea(LogHandler logger) {
        if (thisHandler == null) {
            thisHandler = new MobSpawnArea(logger);
        }
        return thisHandler;
    }
    
    private Random random;
    @SuppressWarnings("unused")
    private LogHandler logger;

    public MobSpawnArea(LogHandler logger) {
        this.random = new Random();
        this.logger = logger;
    }

    public World getRandomWorld() {
        return Bukkit.getWorld("world");
    }

    public Location getRandomLocation(World world){
        // Minus 300 just to account for off by one errors and to stay away from the border
        int distanzaMassimaDalCentro = Math.floorDiv((int) Math.floor(world.getWorldBorder().getSize()), 2*2); //first divide by two to get real border, then divide by two again cause I don't want to spawn them in the whole world
        Location centroDelMondo = world.getWorldBorder().getCenter();

        int maxX = distanzaMassimaDalCentro + (int) centroDelMondo.getX();
        int minX = (-1 * distanzaMassimaDalCentro) + (int) centroDelMondo.getX();
        int maxZ = distanzaMassimaDalCentro + (int) centroDelMondo.getZ();
        int minZ = (-1 * distanzaMassimaDalCentro) + (int) centroDelMondo.getZ();

        int x = random.nextInt(maxX + Math.abs(minX)) + minX;
        int z = random.nextInt(maxZ + Math.abs(minZ)) + minZ;

        // Y is plus 3 to compensate for bigger mobs
        return new Location(world, x, world.getHighestBlockYAt(x, z) + 3, z);
    }
}
