package it.loneliness.mc.Controller;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import it.loneliness.mc.Model.LogHandler;
import it.loneliness.mc.Constants;

public class MobKillListener implements Listener {
    private LogHandler logger;
    private int killMobPoints = 10;
    private int assistKillMobPoints = 5;
    private int maxDistanceToMobToReceivePoints = 100;
        
    public MobKillListener(LogHandler logger) {
        this.logger = logger;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();
                    
        // Check if the killed mob spawned by the plugin 
        if (e.hasMetadata(Constants.SPAWNED_MOB_METADATA)) {

            ScoreboardController scoreboardController = ScoreboardController.getScoreboardHandler(logger);


            Player killer = event.getEntity().getKiller();
            if(killer != null){
                scoreboardController.incrementScore(killer.getName(), killMobPoints);
                Announcement.sendMessage(killer, "Hai ucciso il Boss ed hai vinto "+killMobPoints+" punti!");
                Announcement.announce(killer.getName() + " ha aiutato a liberare il server da un altro BOSS!", logger);
            }

            for (Player p : e.getWorld().getPlayers()){
                if(p.getLocation().distance(e.getLocation()) <= maxDistanceToMobToReceivePoints && !p.equals(killer)){
                    scoreboardController.incrementScore(p.getName(), assistKillMobPoints);
                    Announcement.sendMessage(p, "Hai contribuito ad uccidere il Boss e hai vinto "+assistKillMobPoints+" punti!");
                }
            }



        }
    }

}
