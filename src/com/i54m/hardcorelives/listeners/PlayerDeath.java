package com.i54m.hardcorelives.listeners;

import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.managers.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PlayerDeath implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        Player player = event.getEntity();
        player.spawnParticle(Particle.PORTAL, player.getLocation(), Main.getInstance().getDeathParticleAmount(), 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
        player.sendMessage(ChatColor.RED + "You have died and lost a life!");
        ItemStack lifeItem = Main.getInstance().getLifeItem();
        PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();
        int lives = playerDataManager.getPlayerData(player, false).getInt("lives");
        if (player.getInventory().contains(lifeItem) && lives <= 0) {
            HashMap<Integer, ? extends ItemStack> lifeItems = player.getInventory().all(lifeItem);
            lives = lives+lifeItems.values().size();
            playerDataManager.getPlayerData(player, false).set("lives", lives);
            player.sendMessage(ChatColor.GREEN + "Your inventory contained life orbs, they were automatically redeemed for you on death due to your low amount of lives!");
            playerDataManager.savePlayerData(player.getUniqueId());
        }
    }

}
