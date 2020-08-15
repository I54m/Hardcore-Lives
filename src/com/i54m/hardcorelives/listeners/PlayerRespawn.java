package com.i54m.hardcorelives.listeners;

import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.managers.PlayerDataManager;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawn implements Listener {

    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();
    private final Main plugin = Main.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("hardcorelives.bypassreset")) {
            player.setLevel(0);
            player.setExp(0);
            player.getInventory().clear();
            player.sendMessage(ChatColor.RED + "Your exp, inventory and advancements have been reset!");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "/advancement revoke " + player.getName() + " everything");
        }
        FileConfiguration config = playerDataManager.getPlayerData(player, false);
        int lives = config.getInt("lives");
        lives--;
        int deaths = config.getInt("deaths");
        deaths++;
        if (lives <= 0) {
            //player is dead, move to "death" world and set to dead
            config.set("alive", false);
            event.setRespawnLocation(plugin.getDeathRespawn());
            player.sendMessage(ChatColor.RED + "You do not have any lives left," + ChatColor.BOLD + " GAME OVER!");
            player.sendMessage(ChatColor.RED + "You may buy more on our store or get a player to gift you one with /giftlives!");
            player.sendTitle(ChatColor.RED + "GAME OVER!", ChatColor.RED + "You have no lives left!", 10, 5*20, 20);
            player.playSound(plugin.getDeathRespawn(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
        } else {
            //player has lifes respawn them at a random location if they have no respawn set and decrease lives
            if (!event.isAnchorSpawn() && !event.isBedSpawn()) {
                //missing bed/anchor to respawn you at
                player.sendMessage(ChatColor.RED + "Could not find a bed/charged respawn anchor to respawn you at!");
                event.setRespawnLocation(plugin.getRandomLocation());
            } else {
                //found bed/anchor to respawn you at
                player.sendMessage(ChatColor.GREEN + "Found a bed/charged respawn anchor to respawn you at!");
            }
            player.playSound(event.getRespawnLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
            player.spawnParticle(Particle.TOTEM, event.getRespawnLocation(), plugin.getRespawnParticleAmount());
            player.sendMessage(ChatColor.RED + "You now have: " + lives + " lives left. Use them wisely!");
            player.setInvulnerable(true);
            player.sendMessage(ChatColor.GREEN + "You are invincible for the next 10 seconds!");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.setInvulnerable(false);
                    player.sendMessage(ChatColor.RED + "You are no longer invincible. Don't die!");
                    player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
                }
            }, 10*20);
        }
        player.sendMessage(ChatColor.RED + "You have died: " + deaths + " times. You should be more careful!");
        config.set("deaths", deaths);
        config.set("lives", lives);
        playerDataManager.savePlayerData(player.getUniqueId());
    }
}
