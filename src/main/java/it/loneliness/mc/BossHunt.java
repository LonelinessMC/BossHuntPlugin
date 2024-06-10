package it.loneliness.mc;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import io.papermc.paper.event.entity.EntityMoveEvent;
import it.loneliness.mc.Controller.CommandHandler;
import it.loneliness.mc.Controller.MobController;
import it.loneliness.mc.Controller.MobKillListener;
import it.loneliness.mc.Controller.MobScheduler;
import it.loneliness.mc.Model.LogHandler;

public class BossHunt extends JavaPlugin implements Listener{
    CommandHandler commandHandler;
    MobController mobController;
    MobScheduler mobScheduler;
    BukkitTask mobSchedulerTask;
    LogHandler logger;
    
    @Override
    public void onEnable() {
        logger = new LogHandler(getLogger());
        logger.info("Hello, SpigotMC!");

        mobScheduler = MobScheduler.getMobScheduler(logger, this);
        mobController = MobController.getMobController(logger, this);
        commandHandler = new CommandHandler(this, logger, mobController);

        MobKillListener zombieKillListener = new MobKillListener(logger);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(zombieKillListener, this);

        getServer().getPluginManager().registerEvents(this, this);

        mobSchedulerTask = mobScheduler.runTaskTimer(this, 0, 20*60); //atm it runs every 10 seconds
    }
    @Override
    public void onDisable() {
        if (mobSchedulerTask!=null)
			mobSchedulerTask.cancel();
        else
            logger.severe("For some reason mobSchedulerTask is null, could not stop the periodic task");

        if(mobController != null)
            mobController.despawnAllMobs();
        else
            logger.severe("For some reason commandHandler is null, could not despawn mobs");

        logger.info("See you again, SpigotMC!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandHandler.onCommand(sender, cmd, label, args);
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        if (fromChunk.equals(toChunk)) {
            return;
        }

        Entity entity = event.getEntity();
        if (!entity.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
            return;
        }

        toChunk.addPluginChunkTicket(this);

        //if the old one still has one another BOS then do nothing otherwise remove the ticket
        Entity[] otherEs = fromChunk.getEntities();
        for(Entity otherE : otherEs){
            if (otherE.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
                return;
            }
        }
        fromChunk.removePluginChunkTicket(this);
    }

    /** Prevent withers bosses from launching stuff if not torwards players */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.WITHER_SKULL) {
            WitherSkull ws = (WitherSkull) event.getEntity();
            if(ws.getShooter() instanceof Wither){
                Wither w = (Wither) ws.getShooter();
                if (w.hasMetadata(Constants.SPAWNED_MOB_METADATA) && !(w.getTarget() instanceof Player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /** Prevent withers bosses from launching stuff if not torwards players */
    @EventHandler
    public void onTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        Entity source = event.getEntity();
        Entity target = event.getTarget();
        if(source.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
            if(!(target instanceof Player)){
                event.setCancelled(true);
            }
        }
    }

    /** Prevent withers bosses from launching stuff if not torwards players */
    @EventHandler
    public void onTargetLivingEntity(EntityTargetEvent event) {
        Entity source = event.getEntity();
        Entity target = event.getTarget();
        if(source.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
            if(!(target instanceof Player)){
                event.setCancelled(true);
            }
        }
    }

    /** Stop loading chunk if there is no entity in it */
    @EventHandler
    public void onTargetLivingEntity(EntityDeathEvent event) {
        Entity source = event.getEntity();
        if(source.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
            Chunk chunk = source.getLocation().getChunk();
            
            //if the old one still has one another BOS then do nothing otherwise remove the ticket
            Entity[] otherEs = chunk.getEntities();
            for(Entity otherE : otherEs){
                if (otherE.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
                    return; //if there is another boss do nothing
                }
            }
            chunk.removePluginChunkTicket(this); //otherwise remove the ticket to unload the chunk
        }
    }

    /** Stop loading chunk if there is no entity in it */
    @EventHandler
    public void onEntityRemoveEvent(EntityRemoveEvent event) {
        Entity source = event.getEntity();
        if(source.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
            Chunk chunk = source.getLocation().getChunk();
            
            //if the old one still has one another BOS then do nothing otherwise remove the ticket
            Entity[] otherEs = chunk.getEntities();
            for(Entity otherE : otherEs){
                if (otherE.hasMetadata(Constants.SPAWNED_MOB_METADATA)){
                    return; //if there is another boss do nothing
                }
            }
            chunk.removePluginChunkTicket(this); //otherwise remove the ticket to unload the chunk
        }
    }
}