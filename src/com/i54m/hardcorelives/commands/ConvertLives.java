package com.i54m.hardcorelives.commands;

import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.managers.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConvertLives implements CommandExecutor {

    private final Main plugin = Main.getInstance();
    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }
        final Player player = (Player) commandSender;
        if (s.equalsIgnoreCase("convertlives")) {
            if (strings.length < 1) {
                player.sendMessage(ChatColor.RED + "Incorrect Usage: ");
                player.sendMessage(ChatColor.WHITE + "/convertlives <amount>");
                return true;
            } else {
                final FileConfiguration playerData = playerDataManager.getPlayerData(player, false);
                int playerLives = Math.max(playerData.getInt("lives"), 0);
                int livesToMinus;
                try {
                    livesToMinus = Integer.parseInt(strings[0]);
                } catch (NumberFormatException nfe) {
                    player.sendMessage(ChatColor.RED + strings[0] + " is not a valid number of lives!");
                    return true;
                }
                if (playerLives-livesToMinus <= 1) {
                    player.sendMessage(ChatColor.RED + "You do not have enough lives left to convert! You only have " + playerLives + " lives left!");
                    return true;
                }
                playerLives -= livesToMinus;
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "You do not have enough space in your inventory! Drop some items and try again!");
                    return true;
                }
                playerData.set("lives", playerLives);
                playerDataManager.savePlayerData(player.getUniqueId());
                ItemStack item = plugin.getLifeItem().clone();
                item.setAmount(livesToMinus);
                player.getInventory().addItem(item);
                player.updateInventory();
                player.sendMessage(ChatColor.GREEN + "You have converted one of your extra lives into an item, you feel a weird sensation as it leaves your body....");
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1, 1);
                player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 5 * 20, 1));
            }
            return true;
        }
        return false;
    }
}
