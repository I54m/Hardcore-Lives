package com.i54m.hardcorelives.listeners;

import com.i54m.hardcorelives.managers.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleport implements Listener {

    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("hardcorelives.bypassteleport")) return;
        FileConfiguration config = playerDataManager.getPlayerData(player, false);
        if (!config.getBoolean("alive")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are dead! You may not teleport anywhere!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
        }
    }
}
