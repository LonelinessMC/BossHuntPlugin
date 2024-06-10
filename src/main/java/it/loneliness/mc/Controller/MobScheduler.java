package it.loneliness.mc.Controller;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import it.loneliness.mc.Model.LogHandler;

public class MobScheduler extends BukkitRunnable {

    public static int MAX_NUMBER_OF_BOSSES = 20;
    public static int INCREASE_OF_BOSSES_PER_PLAYER = 2;

    private static MobScheduler thisHandler;

    public static MobScheduler getMobScheduler(LogHandler logger, Plugin plugin) {
        if (thisHandler == null) {
            thisHandler = new MobScheduler(logger, plugin);
        }
        return thisHandler;
    }

    private LogHandler logger;
    @SuppressWarnings("unused")
    private Plugin plugin;
    private MobController mobController;
    private boolean enabled;

    public MobScheduler(LogHandler logger, Plugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
        this.enabled = true;
        mobController = MobController.getMobController(logger, plugin);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /*
     * This is supposed to be called every minute to check if it has to spawn new
     * mobs
     */
    @Override
    public void run() {
        if (this.enabled) {
            int currentSpawnedBosses = mobController.listAllMobs().size();
            if (currentSpawnedBosses < MAX_NUMBER_OF_BOSSES) {
                int numberOfPlayers = Bukkit.getServer().getOnlinePlayers().size();

                int maxNumberOfBossesConsideringPlayers = Math.min(MAX_NUMBER_OF_BOSSES,
                        numberOfPlayers * INCREASE_OF_BOSSES_PER_PLAYER);
                int bossesToSpawn = maxNumberOfBossesConsideringPlayers - currentSpawnedBosses;
                if (bossesToSpawn > 0) {
                    logger.info("Need to spawn " + bossesToSpawn + " more bosses");

                    for (int i = 0; i < bossesToSpawn; i++) {
                        mobController.spawnMobRandomly();
                    }
                }

                else if (bossesToSpawn < 0) {
                    int bossesToDespawn = Math.abs(bossesToSpawn);
                    logger.info("Need to despawn " + bossesToDespawn + " bosses");

                    if (bossesToDespawn == currentSpawnedBosses) {
                        mobController.despawnAllMobs();
                    } else {
                        // Create a priority queue to keep track of the top X furthest entities
                        PriorityQueue<Entity> furthestEntities = new PriorityQueue<>(bossesToDespawn,
                                Comparator.comparingDouble(
                                        entity -> -entity.getLocation()
                                                .distanceSquared(getClosestPlayerLocation(entity))));

                        // Iterate through all entities and players
                        for (Entity entity : mobController.listAllMobs()) { // Assuming all entities are in
                                                                                        // the
                                                                                        // same world
                            Location entityLocation = entity.getLocation();
                            Location closestPlayerLocation = getClosestPlayerLocation(entity);

                            // Calculate the squared distance from the entity to the closest player
                            double distance = entityLocation.distanceSquared(closestPlayerLocation);

                            // Add the entity to the priority queue if it is further away than the furthest
                            // entity in the queue
                            if (furthestEntities.size() < bossesToDespawn || distance > furthestEntities.peek().getLocation()
                                    .distanceSquared(closestPlayerLocation)) {
                                furthestEntities.offer(entity);
                                if (furthestEntities.size() > bossesToDespawn) {
                                    furthestEntities.poll(); // Remove the furthest entity if the queue size exceeds X
                                }
                            }
                        }

                        // Despawn the top X furthest entities
                        while (!furthestEntities.isEmpty()) {
                            Entity entity = furthestEntities.poll();
                            entity.remove();
                        }
                    }

                }
            }
        }
    }

    private Location getClosestPlayerLocation(Entity entity) {
        double minDistance = Double.MAX_VALUE;
        Location closestLocation = null;
        for (Player player : Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(entity.getWorld()))
                .toList()) {
            double distance = player.getLocation().distanceSquared(entity.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                closestLocation = player.getLocation();
            }
        }
        return closestLocation;
    }

}