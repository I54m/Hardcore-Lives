package com.i54m.hardcorelives.listeners;

import com.i54m.hardcorelives.Main;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.spawnParticle(Particle.PORTAL, player.getLocation(), Main.getInstance().getDeathParticleAmount(), 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
        player.sendMessage(ChatColor.RED + "You have died and lost a life!");
//        player.spigot().respawn();//force respawn to avoid bugs
        //drop soul gem?
    }

}
