package it.loneliness.mc.Controller;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import it.loneliness.mc.Model.LogHandler;
import it.loneliness.mc.Constants;

public class MobController {

    private static MobController thisHandler;

    public static MobController getMobController(LogHandler logger, Plugin plugin) {
        if (thisHandler == null) {
            thisHandler = new MobController(logger, plugin);
        }
        return thisHandler;
    }

    private Plugin plugin;
    public LogHandler logger;
    public MobSpawnArea mobSpawnArea;

    private MobController(LogHandler logger, Plugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
        this.mobSpawnArea = MobSpawnArea.getMobSpawnArea(this.logger);
    }

    public int despawnAllMobs(){
        List<Entity> mobs = this.listAllMobs();
        for(Entity mob : mobs)
            mob.remove();

        logger.info("Despawned "+mobs.size()+" from the server.");

        return mobs.size();
    }

    public List<Entity> listAllMobs(){
        ArrayList<Entity> results = new ArrayList<Entity>();
        for (World world : Bukkit.getWorlds())
            for (Entity entity : world.getEntities())
                if (entity.hasMetadata(Constants.SPAWNED_MOB_METADATA))
                    results.add(entity);
        return results;
    }

    public void spawnMobRandomly() {
        Location spawnP = mobSpawnArea.getRandomLocation(mobSpawnArea.getRandomWorld());
        this.spawnMob(spawnP);
    }

    @SuppressWarnings("deprecation")
    public void spawnMob(Location loc){
        loc.getChunk().addPluginChunkTicket(this.plugin);

        Wither spawnedEntity = ((Wither) loc.getWorld().spawnEntity(loc, EntityType.WITHER));
        spawnedEntity.setMetadata(Constants.SPAWNED_MOB_METADATA, new FixedMetadataValue(this.plugin, true));

        // Prevent natural despawn
        spawnedEntity.setCustomName("BOSS");
        spawnedEntity.setCustomNameVisible(true);

        spawnedEntity.setMaxHealth(spawnedEntity.getMaxHealth()/10);
    }
}
