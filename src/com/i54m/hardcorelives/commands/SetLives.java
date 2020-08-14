package com.i54m.hardcorelives.commands;

import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.handlers.ErrorHandler;
import com.i54m.hardcorelives.managers.PlayerDataManager;
import com.i54m.hardcorelives.utils.NameFetcher;
import com.i54m.hardcorelives.utils.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SetLives implements CommandExecutor {

    private final Main plugin = Main.getInstance();
    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (s.equalsIgnoreCase("setlives")) {
            if (!commandSender.hasPermission("hardcorelives.setlives")) {
                commandSender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
                return true;
            }
            if (strings.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Incorrect Usage: ");
                commandSender.sendMessage(ChatColor.WHITE + "/setlives <player> <amount>");
                return false;
            } else {
                final Player target = Bukkit.getPlayer(strings[0]);
                if (target != null && target.isOnline()) {
                    final FileConfiguration targetData = playerDataManager.getPlayerData(target, false);
                    int lives;
                    try {
                        lives = Integer.parseInt(strings[1]);
                    } catch (NumberFormatException nfe) {
                        commandSender.sendMessage(ChatColor.RED + strings[1] + " is not a valid number of lives to set!");
                        return false;
                    }
                    targetData.set("lives", lives);
                    playerDataManager.savePlayerData(target.getUniqueId());
                    commandSender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s lives to: " + strings[1]);
                    target.sendMessage(ChatColor.RED + "your lives have been set to: " + strings[1] + "!");
                    if (lives > 0){
                        target.sendMessage(ChatColor.RED + "You now have: " + lives + " lives left. Use them wisely!");
                        if (!targetData.getBoolean("alive")) plugin.respawn(target, targetData);
                    } else {
                        target.sendMessage(ChatColor.RED + "You do not have any lives left! You will now be killed!");
                        target.setHealth(0);
                    }
                } else {
                    UUID uuid;
                    //use uuid and name fetcher
                    UUIDFetcher uuidFetcher = new UUIDFetcher();
                    uuidFetcher.fetch(strings[0]);
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    Future<UUID> future = executorService.submit(uuidFetcher);
                    try {
                        uuid = future.get(1, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        ErrorHandler errorHandler = ErrorHandler.getINSTANCE();
                        errorHandler.log(e);
                        errorHandler.alert(e, commandSender);
                        executorService.shutdown();
                        return false;
                    }
                    executorService.shutdown();
                    String name = NameFetcher.getName(uuid);
                    final FileConfiguration targetData = playerDataManager.getPlayerData(uuid, false);
                    int lives;
                    try {
                        lives = Integer.parseInt(strings[1]);
                    } catch (NumberFormatException nfe) {
                        commandSender.sendMessage(ChatColor.RED + strings[1] + " is not a valid number of lives to set!");
                        return false;
                    }
                    targetData.set("lives", lives);
                    playerDataManager.savePlayerData(uuid);
                    commandSender.sendMessage(ChatColor.GREEN + "Set " + name + "'s lives to: " + strings[1]);
                    return true;
                }
            }
            return true;
        }
        return false;
    }
}