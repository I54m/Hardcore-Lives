package com.i54m.hardcorelives.listeners;

import com.i54m.hardcorelives.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityDamage implements Listener {

    private static final List<Player> INVINCIBLE = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (INVINCIBLE.contains(player))
                e.setCancelled(true);// prevent invincible players taking damage
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player player = (Player) e.getDamager();
            if (INVINCIBLE.contains(player)) {
                player.sendMessage(ChatColor.RED + "You may not attack other players while you are invincible!");
                e.setCancelled(true); // prevent invincible players damaging other players
            }
        }
    }

    public static void setInvincible(final Player player, int seconds) {
        player.setInvulnerable(true);
        INVINCIBLE.add(player);
        player.sendMessage(ChatColor.GREEN + "You are invincible for the next " + seconds + " seconds, use them wisely!");
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            INVINCIBLE.remove(player);
            player.setInvulnerable(false);
            if (player.isOnline()) {
                player.sendMessage(ChatColor.RED + "You are no longer invincible, don't die!");
                player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
            }
        }, seconds * 20);
    }
}
