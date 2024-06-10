package it.loneliness.mc.Controller;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;

import it.loneliness.mc.Model.LogHandler;

public class CommandHandler {

    private Plugin plugin;
    private LogHandler logger;
    private MobController mobController;

    public CommandHandler(Plugin plugin, LogHandler logger, MobController mobController) {
        this.plugin = plugin;
        this.logger = logger;
        this.mobController = mobController;
    }

    /**
     * Sends a message to the given CommandSender
     */
    private void sendMessage(CommandSender sender, String message) {
        // make the message formatted
        if (sender instanceof Player) {
            Announcement.sendMessage((Player) sender, message);
        } else {
            sender.sendMessage(message);
        }
    }

    private boolean spawnBossHereCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bosshunt.spawnbosshere")) {
                // Spawn mob at player's location

                this.mobController.spawnMob(player.getLocation());

                sendMessage(sender, "A boss has been spawned at "+player.getLocation().toString()+".");
            } else {
                sendMessage(sender, "You don't have permission to use this command.");
            }
        } else {
            sendMessage(sender, "Only players can use this command.");
        }
        return true;
    }

    private boolean listScoreCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission("bosshunt.listscore")) {
            sendMessage(sender, "You don't have permission to use this command.");
            return false;
        }
        
        Objective scoreObjective = ScoreboardController.getScoreboardHandler(logger).getScoreObjective();
        List<String> playersUsernameInScoreboard = ScoreboardController.getScoreboardHandler(logger).getSortedPlayersByScore();

        String result = "";
        for(String user : playersUsernameInScoreboard){
            result += user +": " +  scoreObjective.getScore(user).getScore()+"\n";
        } 

        sendMessage(sender, "Classifica: \n "+result);
        return true;
    }

    private boolean despawnAllMobsCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission("bosshunt.despawnall")) {
            sendMessage(sender, "You don't have permission to use this command.");
            return false;
        }
        int foundMobs = mobController.despawnAllMobs();

        sendMessage(sender, "Despawned "+foundMobs+" from the server.");
        return true;
    }



    private boolean resetAllPlayersScoreCommand(CommandSender sender, Command cmd, String label, String[] args){
        if ((sender instanceof Player) && !sender.hasPermission("bosshunt.resetallplayersscore")) {
            sendMessage(sender, "You don't have permission to use this command.");
            return false;
        }
        if(
            ScoreboardController.getScoreboardHandler(logger).resetAllPlayersScore()
        )
            sendMessage(sender, "Punteggio di tutti i giocatori resettato");
        else 
            sendMessage(sender, "Errore resettando il punteggio dei giocatori, controlla i log");

        return true;
    }

    private boolean trackClosestBossCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "This command can only be run by a player, use /locateegg instead.");
        } else {
            if (sender.hasPermission("bosshunt.trackboss")) {
                Player player = (Player) sender;
                // get the item the player is holding
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType().equals(Material.COMPASS)) {

                    List<Entity> mobs = mobController.listAllMobs();

                    if(mobs.size() > 0) {
                        Location closestMobLocation = null;
                        double closestDistanceSquared = Double.MAX_VALUE;
                        for (Entity mob : mobs) {
                            if (mob.getWorld().equals(player.getWorld())) {
                                double distanceSquared = mob.getLocation().distanceSquared(player.getLocation());
                                if (distanceSquared < closestDistanceSquared) {
                                    closestDistanceSquared = distanceSquared;
                                    closestMobLocation = mob.getLocation();
                                }
                            }
                        }
    
                        if (closestMobLocation != null) {
                            CompassMeta compassMeta = (CompassMeta) heldItem.getItemMeta();
                            compassMeta.setLodestoneTracked(false);
                            compassMeta.setLodestone(closestMobLocation);
                            heldItem.setItemMeta(compassMeta);
                            sendMessage(sender, "La bussola punta al mob più vicino.");
    
                        } else {
                            sendMessage(sender, "Non ci sono mob in questo mondo");
                        }
                    } else {
                        sendMessage(sender, "Non ci sono mob nel server");
                    }
                } else {
                    sendMessage(sender, "Devi avere in mano una bussola per poter usare questo comando");
                }
            } else {
                sendMessage(sender, "Non hai permessi per eseguire questo comando");
            }
        }
        return true;
    }

    private boolean setSpawnEnabledCommand(CommandSender sender, Command cmd, String label, String[] args, boolean enabled) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("bosshunt.listbosses")) {
                sendMessage(sender, "You don't have permission to use this command.");
                return true;
            }
        }

        if(!enabled){
            despawnAllMobsCommand(sender, cmd, label, args);
        }

        MobScheduler.getMobScheduler(logger, plugin).setEnabled(enabled);

        return getStatusCommand(sender, cmd, label, args);
    }

    private boolean getStatusCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("bosshunt.listbosses")) {
                sendMessage(sender, "You don't have permission to use this command.");
                return true;
            }
        }

        boolean isSchedulerEnabled = MobScheduler.getMobScheduler(logger, plugin).isEnabled();

        sendMessage(sender, "scheduler enabled: "+isSchedulerEnabled);

        return true;
    }

    private boolean listAllBossesInServer(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("bosshunt.stopspawn")) {
                sendMessage(sender, "You don't have permission to use this command.");
                return true;
            }
        }
        List<Entity> mobs = this.mobController.listAllMobs();

        String formattedMesage = "";
        if(mobs.size() > 0){
            for (Entity mob : mobs) {
                formattedMesage += mob.getWorld().getName() + " " + mob.getLocation().getX() +" "+mob.getLocation().getY() +" "+mob.getLocation().getZ() +"\n";
            }
        } else {
            formattedMesage = "Nessun boss nel server";
        }

        sendMessage(sender, formattedMesage);

        return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("spawnbosshere")) {
            return spawnBossHereCommand(sender, cmd, label, args);
        } 
        
        if (cmd.getName().equalsIgnoreCase("trackboss")) {
            return trackClosestBossCommand(sender, cmd, label, args);
        }
        
        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("trackboss")) {
            return trackClosestBossCommand(sender, cmd, label, args);
        }
        
        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("despawnall")) {
            return despawnAllMobsCommand(sender, cmd, label, args);
        }
        
        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("resetallplayersscore")) {
            return resetAllPlayersScoreCommand(sender, cmd, label, args);
        }

        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("listbosses")) {
            return listAllBossesInServer(sender, cmd, label, args);
        }

        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("stopspawn")) {
            return setSpawnEnabledCommand(sender, cmd, label, args, false);
        }

        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("startspawn")) {
            return setSpawnEnabledCommand(sender, cmd, label, args, true);
        }

        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("status")) {
            return getStatusCommand(sender, cmd, label, args);
        }

        if (cmd.getName().equalsIgnoreCase("bh") && args.length == 1 && args[0].equalsIgnoreCase("listscore")) {
            return listScoreCommand(sender, cmd, label, args);
        }

        // if no command is found
        return false;
    }
}
