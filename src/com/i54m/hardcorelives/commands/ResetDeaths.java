package com.i54m.hardcorelives.commands;

import com.i54m.hardcorelives.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ResetDeaths implements CommandExecutor {

    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }
        final Player player = (Player) commandSender;
        if (s.equalsIgnoreCase("resetdeaths")) {
            if (!commandSender.hasPermission("hardcorelives.resetdeaths")) {
                commandSender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
                return true;
            }
            if (strings.length < 1) {
                player.sendMessage(ChatColor.RED + "Incorrect Usage: ");
                player.sendMessage(ChatColor.WHITE + "/resetdeaths <player>");
                return true;
            } else {
                final Player target = Bukkit.getPlayer(strings[0]);
                if (target != null && target.isOnline()) {
                    final FileConfiguration targetData = playerDataManager.getPlayerData(target, false);
                    targetData.set("deaths", 0);
                    playerDataManager.savePlayerData(target.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s deaths!");
                } else {
                    player.sendMessage(ChatColor.RED + strings[0] + " is not an online player's name!");
                    return true;
                }
            }
            return true;
        }
        return false;
    }
}
