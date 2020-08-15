package com.i54m.hardcorelives.commands;

import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.managers.PlayerDataManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GiftLives implements CommandExecutor {

    private final Main plugin = Main.getInstance();
    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }
        final Player player = (Player) commandSender;
        if (s.equalsIgnoreCase("giftlives")) {
            if (strings.length < 1) {
                player.sendMessage(ChatColor.RED + "Incorrect Usage: ");
                player.sendMessage(ChatColor.WHITE + "/giftlives <player>");
                return true;
            } else {
                final Player target = Bukkit.getPlayer(strings[0]);
                if (target != null && target.isOnline()) {
                    final FileConfiguration playerData = playerDataManager.getPlayerData(player, false);
                    final FileConfiguration targetData = playerDataManager.getPlayerData(target, false);
                    if (targetData.getBoolean("alive")) {
                        player.sendMessage(ChatColor.RED + "You may only gift lives to dead players!");
                        return true;
                    }
                    int playerLives = Math.max(playerData.getInt("lives"), 0);
                    int targetLives = Math.max(targetData.getInt("lives"), 0);
                    if (playerLives <= 1) {
                        player.sendMessage(ChatColor.RED + "You only have one live left you cannot give someone your last live, you would die!");
                        return true;
                    }
                    playerLives--;
                    targetLives++;
                    playerData.set("lives", playerLives);
                    targetData.set("lives", targetLives);
                    playerDataManager.savePlayerData(player.getUniqueId());
                    playerDataManager.savePlayerData(target.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "You have given " + target.getName() + " one of your extra lives, you feel a weird sensation as it leaves your body....");
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1, 1);
                    player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 5*20, 1));

                    target.sendMessage(ChatColor.GREEN + player.getName() + " has generously given you one of their extra lives!");
                    plugin.respawn(target, targetData);

                    //gift lives
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
